package com.novavpn.app.vpn

/**
 * Holds all configuration needed to establish a VPN tunnel.
 *
 * Currently contains mock/placeholder values.
 * ---------------------------------------------------------------------------
 * WireGuard Integration Point:
 *   Replace these fields with actual WireGuard config:
 *   - privateKey    → client WireGuard private key (Base64)
 *   - publicKey     → server WireGuard public key (Base64)
 *   - endpoint      → server IP:port (e.g. "203.0.113.1:51820")
 *   - allowedIPs    → "0.0.0.0/0" to route all traffic
 *   - dns           → preferred DNS (e.g. "1.1.1.1")
 *   - presharedKey  → optional pre-shared key for extra security
 * ---------------------------------------------------------------------------
 */
data class VpnConfig(
    val serverName: String = "NovaVPN - Auto",
    val serverIp: String = "10.0.0.1",          // Server virtual IP
    val serverPort: Int = 51820,                  // WireGuard default port
    val clientIp: String = "10.0.0.2",           // Client virtual IP
    val clientSubnet: Int = 24,                   // /24 subnet
    val dnsServer: String = "1.1.1.1",            // Cloudflare DNS
    val mtu: Int = 1420,                          // WireGuard recommended MTU

    // -------------------------------------------------------------------------
    // WireGuard Keys — populate these from a secure backend/config at runtime.
    // NEVER hardcode production keys in the APK.
    // -------------------------------------------------------------------------
    val clientPrivateKey: String = "PLACEHOLDER_CLIENT_PRIVATE_KEY",
    val serverPublicKey: String = "PLACEHOLDER_SERVER_PUBLIC_KEY",
    val presharedKey: String? = null,

    // Routes to push through the VPN (0.0.0.0/0 = all traffic)
    val allowedIps: String = "0.0.0.0/0",

    // Keepalive interval in seconds (prevents NAT timeout)
    val persistentKeepalive: Int = 25
)

/** Preset server regions — extend this list with real server endpoints */
object VpnServerPresets {
    val servers = listOf(
        VpnConfig(serverName = "🇺🇸 United States",  serverIp = "198.51.100.1"),
        VpnConfig(serverName = "🇩🇪 Germany",         serverIp = "203.0.113.1"),
        VpnConfig(serverName = "🇸🇬 Singapore",       serverIp = "192.0.2.1"),
        VpnConfig(serverName = "🇯🇵 Japan",           serverIp = "192.0.2.2"),
        VpnConfig(serverName = "🇬🇧 United Kingdom",  serverIp = "192.0.2.3"),
    )
    val default: VpnConfig get() = servers.first()
}
