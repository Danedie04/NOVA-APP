package com.novavpn.app.vpn

/**
 * Represents all possible states of the VPN connection.
 * Using a sealed class ensures exhaustive when-expressions.
 */
sealed class VpnState {

    /** VPN is fully disconnected and idle. */
    object Disconnected : VpnState()

    /** VPN is in the process of connecting. */
    object Connecting : VpnState()

    /** VPN is fully connected and routing traffic. */
    data class Connected(
        val serverIp: String = "10.0.0.1",
        val bytesIn: Long = 0L,
        val bytesOut: Long = 0L,
        val connectedSince: Long = System.currentTimeMillis()
    ) : VpnState()

    /** VPN is in the process of disconnecting. */
    object Disconnecting : VpnState()

    /** An error has occurred. */
    data class Error(val message: String) : VpnState()

    /** Human-readable label for UI display */
    val displayName: String
        get() = when (this) {
            is Disconnected  -> "Disconnected"
            is Connecting    -> "Connecting..."
            is Connected     -> "Connected"
            is Disconnecting -> "Disconnecting..."
            is Error         -> "Error"
        }

    val isConnected: Boolean get() = this is Connected
    val isTransitioning: Boolean get() = this is Connecting || this is Disconnecting
}
