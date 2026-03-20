package com.novavpn.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novavpn.app.ui.components.ConnectButton
import com.novavpn.app.ui.components.ServerSelector
import com.novavpn.app.ui.components.StatusCard
import com.novavpn.app.ui.theme.*
import com.novavpn.app.vpn.VpnConfig
import com.novavpn.app.vpn.VpnState

/**
 * Main VPN screen composable.
 * Observes [VpnViewModel] state and renders the full UI.
 */
@Composable
fun MainScreen(
    vpnState: VpnState,
    selectedConfig: VpnConfig,
    servers: List<VpnConfig>,
    onToggleConnection: () -> Unit,
    onServerSelected: (VpnConfig) -> Unit
) {
    val scrollState = rememberScrollState()

    // Dynamic gradient based on connection state
    val gradientTop by animateColorAsState(
        targetValue = when (vpnState) {
            is VpnState.Connected    -> Color(0xFF001A10)
            is VpnState.Connecting,
            is VpnState.Disconnecting-> Color(0xFF1A0D00)
            is VpnState.Error        -> Color(0xFF1A0005)
            else                     -> NovaBlack
        },
        animationSpec = tween(800),
        label = "gradTop"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientTop, NovaBlack, NovaDarkBlue)
                )
            )
    ) {
        // Subtle grid overlay
        GridOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── App Header ───────────────────────────────────────────────────
            AppHeader()

            Spacer(Modifier.height(40.dp))

            // ── Status Card ──────────────────────────────────────────────────
            StatusCard(
                vpnState = vpnState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(48.dp))

            // ── Connect Button ───────────────────────────────────────────────
            ConnectButton(
                vpnState = vpnState,
                onClick = onToggleConnection
            )

            Spacer(Modifier.height(48.dp))

            // ── Server Selector ──────────────────────────────────────────────
            ServerSelector(
                servers = servers,
                selectedConfig = selectedConfig,
                onServerSelected = onServerSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AppHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "NOVA",
            color = NovaAccent,
            fontSize = 36.sp,
            fontWeight = FontWeight.W800,
            letterSpacing = 8.sp
        )
        Text(
            text = "VPN",
            color = NovaWhite.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 10.sp
        )
    }
}

/**
 * Subtle dot-grid background overlay for a tech aesthetic.
 */
@Composable
private fun GridOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NovaAccent.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    radius = 800f
                )
            )
    )
}
