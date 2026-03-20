package com.novavpn.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.novavpn.app.R
import com.novavpn.app.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * Core VPN Service that extends Android's VpnService.
 *
 * Lifecycle:
 *  1. startService() is called with ACTION_CONNECT
 *  2. onStartCommand() builds the VPN interface and starts the tunnel
 *  3. onDestroy() tears down the tunnel
 *
 * =============================================================================
 * WireGuard Integration Point:
 *   Replace the mock tunnel section (marked with TODO:WIREGUARD) with:
 *   1. Add the wireguard-android library dependency in build.gradle.kts
 *   2. Initialize Backend (GoBackend or UserSpaceBackend)
 *   3. Create a Tunnel object implementing Tunnel interface
 *   4. Call backend.setState(tunnel, Tunnel.State.UP, config)
 *   The VpnService.Builder is still used in the same way.
 * =============================================================================
 */
class NovaVpnService : VpnService() {

    companion object {
        private const val TAG = "NovaVpnService"
        const val ACTION_CONNECT    = "com.novavpn.app.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.novavpn.app.vpn.DISCONNECT"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "novavpn_channel"

        // Shared state — observed by ViewModel
        private val _vpnState = MutableStateFlow<VpnState>(VpnState.Disconnected)
        val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunnelJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeConfig: VpnConfig = VpnConfig()

    // -------------------------------------------------------------------------
    // Service lifecycle
    // -------------------------------------------------------------------------

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_CONNECT -> {
                startVpn(VpnConfig())
                START_STICKY
            }
            ACTION_DISCONNECT -> {
                stopVpn()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    override fun onRevoke() {
        // Called by the system when the user revokes VPN permission
        Log.d(TAG, "VPN permission revoked by user")
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        serviceScope.cancel()
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // VPN start / stop
    // -------------------------------------------------------------------------

    private fun startVpn(config: VpnConfig) {
        Log.d(TAG, "Starting VPN with config: ${config.serverName}")
        activeConfig = config
        _vpnState.value = VpnState.Connecting

        serviceScope.launch {
            try {
                // 1. Establish the virtual network interface
                vpnInterface = buildVpnInterface(config)
                    ?: throw IllegalStateException("Failed to build VPN interface")

                // Start foreground notification
                startForeground(NOTIFICATION_ID, buildNotification(config))

                // 2. Start the tunnel coroutine
                _vpnState.value = VpnState.Connected(serverIp = config.serverIp)
                Log.d(TAG, "VPN interface established — starting tunnel")

                // 3. Run the packet processing loop
                tunnelJob = launch { runTunnel(vpnInterface!!, config) }
                tunnelJob?.join()

            } catch (e: Exception) {
                Log.e(TAG, "VPN start failed", e)
                _vpnState.value = VpnState.Error(e.message ?: "Unknown error")
                cleanup()
            }
        }
    }

    private fun stopVpn() {
        Log.d(TAG, "Stopping VPN")
        _vpnState.value = VpnState.Disconnecting
        tunnelJob?.cancel()
        cleanup()
        _vpnState.value = VpnState.Disconnected
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cleanup() {
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing VPN interface", e)
        }
        vpnInterface = null
    }

    // -------------------------------------------------------------------------
    // Virtual interface builder
    // -------------------------------------------------------------------------

    /**
     * Configures and establishes the virtual network interface.
     * This sets up the TUN device that intercepts all device traffic.
     */
    private fun buildVpnInterface(config: VpnConfig): ParcelFileDescriptor? {
        return try {
            Builder().apply {
                // Assign the client's virtual IP address
                addAddress(config.clientIp, config.clientSubnet)

                // Route all traffic through the VPN
                // For split-tunneling, specify individual subnets instead of 0.0.0.0/0
                addRoute("0.0.0.0", 0)          // IPv4 default route
                addRoute("::", 0)                // IPv6 default route

                // DNS server
                addDnsServer(config.dnsServer)
                addDnsServer("8.8.8.8")          // Google DNS fallback

                // MTU (Maximum Transmission Unit) — 1420 is WireGuard standard
                setMtu(config.mtu)

                // App name shown in system VPN settings
                setSession(config.serverName)

                // Blocking mode: true = read() blocks until packet is available
                setBlocking(true)

                // Allow LAN traffic to bypass VPN (optional — comment out for full tunnel)
                // allowFamily(OsConstants.AF_INET)

                // Exclude the app itself from the VPN to avoid routing loops
                try {
                    addDisallowedApplication(packageName)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not exclude own package", e)
                }

            }.establish()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN interface", e)
            null
        }
    }

    // -------------------------------------------------------------------------
    // Tunnel packet processing
    // =========================================================================
    // TODO:WIREGUARD — Replace this entire section with WireGuard backend.
    //
    // WireGuard integration steps:
    //   1. val backend = GoBackend(this)           // Native WireGuard-Go backend
    //   2. val tunnel  = object : Tunnel { ... }   // Implement Tunnel interface
    //   3. val wgConfig = Config.parse(configText) // Parse WireGuard config
    //   4. backend.setState(tunnel, UP, wgConfig)  // Bring tunnel UP
    //
    // The GoBackend handles all encryption, handshake, and packet routing
    // internally — you don't need a manual read/write loop.
    // =========================================================================

    /**
     * Mock tunnel loop: reads packets from the TUN device and echoes them back.
     *
     * In a real VPN:
     *  - Outbound: read from [tun] → encrypt → send to [serverSocket]
     *  - Inbound:  receive from [serverSocket] → decrypt → write to [tun]
     */
    private suspend fun runTunnel(tun: ParcelFileDescriptor, config: VpnConfig) {
        Log.d(TAG, "Tunnel loop started")

        val inputStream  = FileInputStream(tun.fileDescriptor)
        val outputStream = FileOutputStream(tun.fileDescriptor)
        val buffer       = ByteBuffer.allocate(config.mtu)

        var bytesIn  = 0L
        var bytesOut = 0L

        // Stats update interval
        val statsInterval = 2000L
        var lastStatsUpdate = System.currentTimeMillis()

        try {
            while (currentCoroutineContext().isActive) {
                buffer.clear()

                // Read a packet from the TUN device (blocks until a packet arrives)
                val length = inputStream.read(buffer.array())
                if (length <= 0) continue

                bytesOut += length

                // ============================================================
                // TODO:WIREGUARD — Send packet to WireGuard backend here:
                //   backend.sendPacket(buffer.array(), 0, length)
                //
                // TODO:OPENVPN — Or wrap in OpenVPN's TLS session and write to socket.
                //
                // For now: loopback (mock) — echo packet back to device
                // ============================================================
                if (length > 0) {
                    outputStream.write(buffer.array(), 0, length)
                    bytesIn += length
                }

                // Periodically emit traffic stats to the UI
                val now = System.currentTimeMillis()
                if (now - lastStatsUpdate > statsInterval) {
                    _vpnState.value = VpnState.Connected(
                        serverIp = config.serverIp,
                        bytesIn = bytesIn,
                        bytesOut = bytesOut
                    )
                    lastStatsUpdate = now
                }

                // Yield to allow cancellation
                yield()
            }
        } catch (e: Exception) {
            if (currentCoroutineContext().isActive) {
                Log.e(TAG, "Tunnel error", e)
                _vpnState.value = VpnState.Error("Tunnel error: ${e.message}")
            }
        } finally {
            Log.d(TAG, "Tunnel loop ended — total in: $bytesIn, out: $bytesOut bytes")
        }
    }

    // -------------------------------------------------------------------------
    // Foreground notification
    // -------------------------------------------------------------------------

    private fun buildNotification(config: VpnConfig): Notification {
        createNotificationChannel()

        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = PendingIntent.getService(
            this, 1,
            Intent(this, NovaVpnService::class.java).apply { action = ACTION_DISCONNECT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NovaVPN Active")
            .setContentText("Connected to ${config.serverName}")
            .setSmallIcon(R.drawable.ic_vpn_key)
            .setContentIntent(tapIntent)
            .addAction(R.drawable.ic_vpn_key, "Disconnect", disconnectIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "NovaVPN Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows VPN connection status"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
