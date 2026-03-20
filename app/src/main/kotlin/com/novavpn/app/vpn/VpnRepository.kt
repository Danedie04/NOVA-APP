package com.novavpn.app.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that bridges the ViewModel and the VPN Service.
 *
 * Responsibilities:
 *  - Provide the current VPN state as a Flow
 *  - Start / stop the VPN service
 *  - Build and validate VPN configurations
 *  - Check if VPN permission has been granted
 */
@Singleton
class VpnRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Exposes the VPN state from the service as a StateFlow */
    val vpnState: StateFlow<VpnState> = NovaVpnService.vpnState

    /**
     * Returns an Intent to request VPN permission, or null if already granted.
     * The caller must launch this Intent with startActivityForResult().
     */
    fun prepareVpn(): Intent? = VpnService.prepare(context)

    /**
     * Connects to the VPN using the given [config].
     * Assumes VPN permission has already been granted.
     */
    fun connect(config: VpnConfig = VpnConfig()) {
        val intent = Intent(context, NovaVpnService::class.java).apply {
            action = NovaVpnService.ACTION_CONNECT
        }
        context.startForegroundService(intent)
    }

    /** Sends a disconnect command to the VPN service. */
    fun disconnect() {
        val intent = Intent(context, NovaVpnService::class.java).apply {
            action = NovaVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    /** Returns the list of available server presets. */
    fun getServers(): List<VpnConfig> = VpnServerPresets.servers
}
