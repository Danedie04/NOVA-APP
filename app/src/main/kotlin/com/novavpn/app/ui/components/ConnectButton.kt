package com.novavpn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novavpn.app.R
import com.novavpn.app.ui.theme.*
import com.novavpn.app.vpn.VpnState

/**
 * The large central connect/disconnect power button.
 *
 * Features:
 *  - Pulsing glow ring when connected
 *  - Rotating animation while connecting
 *  - Color transitions between states
 *  - Scale press feedback
 */
@Composable
fun ConnectButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val isConnected     = vpnState is VpnState.Connected
    val isConnecting    = vpnState is VpnState.Connecting || vpnState is VpnState.Disconnecting
    val isTransitioning = vpnState.isTransitioning

    // ── Color animation ──────────────────────────────────────────────────────
    val buttonColor by animateColorAsState(
        targetValue = when (vpnState) {
            is VpnState.Connected    -> NovaGreen.copy(alpha = 0.15f)
            is VpnState.Connecting   -> NovaOrange.copy(alpha = 0.10f)
            is VpnState.Disconnecting-> NovaOrange.copy(alpha = 0.10f)
            is VpnState.Error        -> NovaRed.copy(alpha = 0.12f)
            else                     -> NovaAccent.copy(alpha = 0.08f)
        },
        animationSpec = tween(600),
        label = "buttonColor"
    )

    val ringColor by animateColorAsState(
        targetValue = when (vpnState) {
            is VpnState.Connected    -> NovaGreen
            is VpnState.Connecting   -> NovaOrange
            is VpnState.Disconnecting-> NovaOrange
            is VpnState.Error        -> NovaRed
            else                     -> NovaAccent
        },
        animationSpec = tween(600),
        label = "ringColor"
    )

    val iconColor by animateColorAsState(
        targetValue = when (vpnState) {
            is VpnState.Connected    -> NovaGreen
            is VpnState.Connecting   -> NovaOrange
            is VpnState.Disconnecting-> NovaOrange
            is VpnState.Error        -> NovaRed
            else                     -> NovaAccent
        },
        animationSpec = tween(400),
        label = "iconColor"
    )

    // ── Pulse animation (connected state) ────────────────────────────────────
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1.0f,
        targetValue  = if (isConnected) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 0.6f,
        targetValue  = if (isConnected) 0.0f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // ── Rotation animation (connecting state) ─────────────────────────────────
    val rotAnim = rememberInfiniteTransition(label = "rotation")
    val rotation by rotAnim.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // ── Interaction source for ripple ─────────────────────────────────────────
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size + 40.dp)
    ) {

        // Outer pulse ring
        if (isConnected) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(pulseScale)
                    .border(2.dp, ringColor.copy(alpha = pulseAlpha), CircleShape)
            )
        }

        // Rotating dashed ring (connecting state)
        if (isConnecting) {
            Box(
                modifier = Modifier
                    .size(size + 16.dp)
                    .graphicsLayer { rotationZ = rotation }
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, ringColor, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Main button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = if (isConnected) 24.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = ringColor,
                    spotColor = ringColor
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(buttonColor, NovaSurface)
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = ringColor.copy(alpha = 0.5f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(
                        bounded = false,
                        color = ringColor
                    ),
                    enabled = !isTransitioning,
                    onClick = onClick
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isConnected) R.drawable.ic_vpn_lock
                             else              R.drawable.ic_vpn_key
                    ),
                    contentDescription = "VPN Status",
                    tint = iconColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when (vpnState) {
                        is VpnState.Connected    -> "DISCONNECT"
                        is VpnState.Connecting   -> "CANCEL"
                        is VpnState.Disconnecting-> "WAIT..."
                        else                     -> "CONNECT"
                    },
                    color = iconColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
