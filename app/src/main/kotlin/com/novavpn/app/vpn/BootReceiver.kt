package com.novavpn.app.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receives BOOT_COMPLETED broadcast to restore VPN connection after device restart.
 *
 * Only triggers if the user had enabled "auto-connect on boot" in settings.
 * This is an optional feature — wire up SharedPreferences to check the preference.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
        private const val PREF_AUTO_CONNECT = "auto_connect_on_boot"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Boot completed — checking auto-connect preference")

        val prefs = context.getSharedPreferences("novavpn_prefs", Context.MODE_PRIVATE)
        val autoConnect = prefs.getBoolean(PREF_AUTO_CONNECT, false)

        if (autoConnect) {
            Log.d(TAG, "Auto-connect enabled — starting VPN service")
            val serviceIntent = Intent(context, NovaVpnService::class.java).apply {
                action = NovaVpnService.ACTION_CONNECT
            }
            context.startForegroundService(serviceIntent)
        } else {
            Log.d(TAG, "Auto-connect disabled — skipping VPN start")
        }
    }
}
