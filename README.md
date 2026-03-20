<div align="center">

```
███╗   ██╗ ██████╗ ██╗   ██╗ █████╗     ██╗   ██╗██████╗ ███╗   ██╗
████╗  ██║██╔═══██╗██║   ██║██╔══██╗    ██║   ██║██╔══██╗████╗  ██║
██╔██╗ ██║██║   ██║██║   ██║███████║    ██║   ██║██████╔╝██╔██╗ ██║
██║╚██╗██║██║   ██║╚██╗ ██╔╝██╔══██║    ╚██╗ ██╔╝██╔═══╝ ██║╚██╗██║
██║ ╚████║╚██████╔╝ ╚████╔╝ ██║  ██║     ╚████╔╝ ██║     ██║ ╚████║
╚═╝  ╚═══╝ ╚═════╝   ╚═══╝  ╚═╝  ╚═╝      ╚═══╝  ╚═╝     ╚═╝  ╚═══╝
```

### **Production-grade Android VPN client · Kotlin · Jetpack Compose · WireGuard Ready**

<br/>

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API_26+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-2024.10-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/Hilt-2.52-FF6D00?style=for-the-badge&logo=google&logoColor=white)](https://dagger.dev/hilt)
[![License](https://img.shields.io/badge/License-MIT-00E676?style=for-the-badge)](LICENSE)

<br/>

> **NovaVPN** is a complete, compilable Android VPN client built on the native `VpnService` API.
> Clean MVVM architecture with Hilt DI, animated Compose UI, and clearly marked
> integration points for WireGuard or OpenVPN backends.

<br/>

</div>

---

## ✦ Screenshots

<div align="center">

| Disconnected | Connected | Connecting |
|:---:|:---:|:---:|
| ![Disconnected Screen](screenshots/disconnected.png) | ![Connected Screen](screenshots/connected.png) | ![Connecting Screen](screenshots/connecting.png) |
| *Idle — ready to connect* | *Active tunnel with live stats* | *Handshake in progress* |

</div>

> **Note:** Screenshots are rendered from the included [visual canvas](nova-canvas.html). Open it in a browser to see all UI states in full fidelity.

---

## ✦ Features

| Category | Detail |
|---|---|
| 🛡️ **VPN Core** | Full `VpnService` subclass · TUN interface via `ParcelFileDescriptor` · All-traffic routing |
| 🔑 **WireGuard Ready** | `TODO:WIREGUARD` markers at every integration point · Zero restructuring needed |
| 🎨 **Compose UI** | Animated connect button · Pulsing glow ring · Color-transitioning gradients |
| 📊 **Live Stats** | Real-time bytes in/out · Connection uptime counter · Server IP display |
| 🌍 **Multi-region** | 5 server presets · Live server switching · Auto-reconnect when active |
| 💉 **Hilt DI** | Singleton `VpnRepository` · Clean testable dependency graph |
| ⚡ **Coroutines** | Non-blocking tunnel loop · `StateFlow` state machine · `SharedFlow` for UI events |
| 🔐 **Permissions** | Full `VPN_SERVICE` permission flow · `ActivityResultContracts` · Revoke handling |
| 📡 **Foreground Service** | Persistent notification · Disconnect action button · Boot receiver |
| 🔔 **Error Handling** | Snackbar errors · `VpnState.Error` sealed class · Graceful cleanup |

---

## ✦ Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                      JETPACK COMPOSE UI                          │
│   MainScreen · ConnectButton · StatusCard · ServerSelector       │
└─────────────────────────┬────────────────────────────────────────┘
                          │ collectAsStateWithLifecycle()
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                        VPNVIEWMODEL                              │
│   StateFlow<VpnState> · SharedFlow<UiEvent>                      │
│   onToggleConnection() · onVpnPermissionGranted()                │
└─────────────────────────┬────────────────────────────────────────┘
                          │ @Inject (Hilt)
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                       VPNREPOSITORY                              │
│   prepareVpn() → Intent? · connect() · disconnect()              │
│   getServers() → List<VpnConfig>                                 │
└─────────────────────────┬────────────────────────────────────────┘
                          │ startForegroundService()
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                      NOVAVPNSERVICE                              │
│   extends VpnService · Builder → ParcelFileDescriptor            │
│   Companion StateFlow · Coroutine tunnel loop                    │
│                                                                  │
│   ┌──── TODO:WIREGUARD ─────────────────────────────────────┐   │
│   │  GoBackend.setState(tunnel, UP, wgConfig)               │   │
│   └────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### State Machine

```
 Disconnected ──[tap]──► Connecting ──[success]──► Connected
      ▲                       │                        │
      │                  [failure]              [tap disconnect]
      │                       ▼                        │
      └──────────────── Error ◄────────────────────────┘
                          │
                     [tap retry]
                          │
                     Connecting
```

---

## ✦ Project Structure

```
NovaVPN/
├── app/src/main/
│   ├── AndroidManifest.xml
│   └── kotlin/com/novavpn/app/
│       │
│       ├── NovaVpnApplication.kt          ← @HiltAndroidApp
│       │
│       ├── vpn/                           ← Core VPN layer
│       │   ├── NovaVpnService.kt          ← VpnService subclass (317 lines)
│       │   ├── VpnState.kt                ← Sealed state class
│       │   ├── VpnConfig.kt               ← Server config + presets
│       │   ├── VpnConfigParcel.kt         ← Parcelable wrapper
│       │   ├── VpnRepository.kt           ← Service ↔ ViewModel bridge
│       │   └── BootReceiver.kt            ← Auto-reconnect on boot
│       │
│       ├── viewmodel/
│       │   └── VpnViewModel.kt            ← MVVM + UiEvents
│       │
│       ├── ui/
│       │   ├── MainActivity.kt            ← Entry + permission handling
│       │   ├── SplashActivity.kt          ← Animated splash
│       │   ├── screens/
│       │   │   └── MainScreen.kt          ← Root Compose screen
│       │   ├── components/
│       │   │   ├── ConnectButton.kt       ← Animated power button
│       │   │   ├── StatusCard.kt          ← Live connection stats
│       │   │   └── ServerSelector.kt      ← Region picker
│       │   └── theme/
│       │       └── NovaVpnTheme.kt        ← Dark tech palette, M3
│       │
│       ├── di/
│       │   └── AppModule.kt               ← Hilt bindings
│       │
│       └── utils/
│           ├── FormatUtils.kt             ← formatBytes / formatDuration
│           └── ExtensionUtils.kt          ← Kotlin extensions
│
├── gradle/
│   ├── libs.versions.toml                 ← Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## ✦ Quick Start

### Requirements

| Tool | Version |
|---|---|
| Android Studio | Hedgehog 2023.1.1+ |
| JDK | 17 |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.5.2 |
| minSdk | 26 (Android 8.0) |
| targetSdk | 35 (Android 15) |

### Build

```bash
# 1. Clone
git clone https://github.com/yourusername/NovaVPN.git
cd NovaVPN

# 2. Build debug APK
./gradlew assembleDebug

# APK output → app/build/outputs/apk/debug/app-debug.apk

# 3. Install on device
./gradlew installDebug
```

Or open in **Android Studio → File → Open** → select folder → click **▶ Run**.

---

## ✦ WireGuard Integration

The service is structured so WireGuard drops in at a **single, clearly marked point** with no architectural changes.

### Step 1 — Add dependency

```kotlin
// app/build.gradle.kts
implementation("com.wireguard.android:tunnel:1.0.20230706")
```

### Step 2 — Replace mock tunnel in `NovaVpnService.kt`

Find `// TODO:WIREGUARD` and replace the `runTunnel()` call with:

```kotlin
val backend = GoBackend(this)

val tunnel = object : Tunnel {
    override fun getName() = config.serverName
    override fun onStateChange(state: Tunnel.State) {
        _vpnState.value = if (state == Tunnel.State.UP)
            VpnState.Connected(serverIp = config.serverIp)
        else VpnState.Disconnected
    }
}

val wgConfig = Config.parse(buildWireGuardConfig(config))
backend.setState(tunnel, Tunnel.State.UP, wgConfig)
```

### Step 3 — Build config string from `VpnConfig`

```kotlin
fun buildWireGuardConfig(config: VpnConfig): String = """
    [Interface]
    PrivateKey = ${config.clientPrivateKey}
    Address = ${config.clientIp}/${config.clientSubnet}
    DNS = ${config.dnsServer}

    [Peer]
    PublicKey = ${config.serverPublicKey}
    Endpoint = ${config.serverIp}:${config.serverPort}
    AllowedIPs = ${config.allowedIps}
    PersistentKeepalive = ${config.persistentKeepalive}
""".trimIndent()
```

### Step 4 — Fetch keys securely at runtime

```kotlin
// VpnRepository.kt — NEVER hardcode keys in the APK
suspend fun fetchConfig(userId: String, token: String): VpnConfig {
    val resp = apiService.getVpnConfig(userId, token)
    return VpnConfig(
        serverIp       = resp.endpoint,
        serverPublicKey = resp.serverPubKey,
        clientPrivateKey = resp.assignedPrivKey
    )
}
```

---

## ✦ VPN Permission Flow

```
User taps CONNECT
        │
        ▼
VpnViewModel.onToggleConnection()
        │
        ▼
VpnRepository.prepareVpn()
  = VpnService.prepare(context)
        │
   ┌────┴──────────────┐
   │ null              │ Intent (permission needed)
   ▼                   ▼
connect()         ActivityResultContracts
              (system permission dialog)
                        │
              ┌─────────┴──────────┐
              │ RESULT_OK          │ RESULT_CANCELED
              ▼                    ▼
   onPermissionGranted()     onPermissionDenied()
              │                    │
           connect()          Snackbar error
```

---

## ✦ Security Checklist

> Complete this checklist before any production release.

- [ ] Replace placeholder keys — never ship private keys in the APK
- [ ] Add authenticated API endpoint to fetch per-user WireGuard configs
- [ ] Enable certificate pinning on your backend API calls
- [ ] Implement kill switch (`allowFamily()` + traffic blocking on disconnect)
- [ ] DNS leak protection — force all DNS through the VPN tunnel interface
- [ ] Add token-based authentication for server access
- [ ] Set up release signing with a dedicated keystore (not debug)
- [ ] Enable R8 full-mode minification in release builds
- [ ] Rotate WireGuard keys periodically via backend API
- [ ] Add obfuscation layer (Shadowsocks / obfs4) for restricted regions

---

## ✦ Extending to a Full Product

| Feature | Path |
|---|---|
| **Subscription billing** | Google Play Billing Library + backend entitlement |
| **Multiple regions** | Fetch server list from REST API |
| **Kill switch** | Block traffic on `VpnState.Disconnected` |
| **Split tunneling** | Per-app routing via `addAllowedApplication()` |
| **Auto-connect** | Boot receiver (already wired) + settings toggle |
| **Protocol switch** | Strategy pattern in `VpnRepository` |
| **Analytics** | Firebase Events on state transitions |
| **Obfuscation** | Route through Shadowsocks before WireGuard |

---

## ✦ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose · Material 3 · Compose BOM 2024.10 |
| Architecture | MVVM · Repository pattern |
| DI | Hilt 2.52 · KSP |
| Async | Coroutines · StateFlow · SharedFlow |
| VPN | Android VpnService API · ParcelFileDescriptor |
| Protocol | WireGuard (pluggable) |
| Build | AGP 8.5.2 · Gradle 8.9 · Version Catalog |
| Min SDK | API 26 (Android 8.0 Oreo) |
| Target SDK | API 35 (Android 15) |

---

## ✦ License

```
MIT License

Copyright (c) 2024 NovaVPN

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software.
```

---

<div align="center">

**Built with Kotlin · Designed for engineers who ship.**

`VpnService` · `Jetpack Compose` · `Hilt` · `WireGuard` · `Coroutines`

<br/>

⭐ **Star this repo if it helped you** ⭐

</div>
