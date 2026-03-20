package com.novavpn.app.vpn

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Parcelable wrapper so VpnConfig can be passed via Intent extras to the service.
 * Kotlin Parcelize generates the Parcelable implementation automatically.
 */
@Parcelize
data class VpnConfigParcel(
    val serverName: String,
    val serverIp: String,
    val serverPort: Int,
    val clientIp: String,
    val clientSubnet: Int,
    val dnsServer: String,
    val mtu: Int,
    val clientPrivateKey: String,
    val serverPublicKey: String,
    val presharedKey: String?,
    val allowedIps: String,
    val persistentKeepalive: Int
) : Parcelable {

    companion object {
        fun from(config: VpnConfig) = VpnConfigParcel(
            serverName         = config.serverName,
            serverIp           = config.serverIp,
            serverPort         = config.serverPort,
            clientIp           = config.clientIp,
            clientSubnet       = config.clientSubnet,
            dnsServer          = config.dnsServer,
            mtu                = config.mtu,
            clientPrivateKey   = config.clientPrivateKey,
            serverPublicKey    = config.serverPublicKey,
            presharedKey       = config.presharedKey,
            allowedIps         = config.allowedIps,
            persistentKeepalive = config.persistentKeepalive
        )
    }

    fun toConfig() = VpnConfig(
        serverName         = serverName,
        serverIp           = serverIp,
        serverPort         = serverPort,
        clientIp           = clientIp,
        clientSubnet       = clientSubnet,
        dnsServer          = dnsServer,
        mtu                = mtu,
        clientPrivateKey   = clientPrivateKey,
        serverPublicKey    = serverPublicKey,
        presharedKey       = presharedKey,
        allowedIps         = allowedIps,
        persistentKeepalive = persistentKeepalive
    )
}
