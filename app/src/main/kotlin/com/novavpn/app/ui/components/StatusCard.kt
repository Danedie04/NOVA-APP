package com.novavpn.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novavpn.app.ui.theme.*
import com.novavpn.app.vpn.VpnState
import com.novavpn.app.utils.formatBytes
import com.novavpn.app.utils.formatDuration

/**
 * Card displaying connection status, server IP, and live traffic statistics.
 */
@Composable
fun StatusCard(
    vpnState: VpnState,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    val borderColor by animateColorAsState(
        targetValue = when (vpnState) {
            is VpnState.Connected    -> NovaGreen.copy(alpha = 0.4f)
            is VpnState.Connecting,
            is VpnState.Disconnecting-> NovaOrange.copy(alpha = 0.4f)
            is VpnState.Error        -> NovaRed.copy(alpha = 0.4f)
            else                     -> NovaGray.copy(alpha = 0.2f)
        },
        animationSpec = tween(600),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NovaCard, NovaSurface)
                )
            )
            .border(1.dp, borderColor, shape)
            .padding(20.dp)
    ) {
        AnimatedContent(
            targetState = vpnState,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "statusContent"
        ) { state ->
            when (state) {
                is VpnState.Connected -> ConnectedContent(state)
                is VpnState.Connecting -> TransitionContent("Establishing secure tunnel...", NovaOrange)
                is VpnState.Disconnecting -> TransitionContent("Closing tunnel...", NovaOrange)
                is VpnState.Error -> ErrorContent(state.message)
                is VpnState.Disconnected -> DisconnectedContent()
            }
        }
    }
}

@Composable
private fun ConnectedContent(state: VpnState.Connected) {
    // Uptime counter
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(state.connectedSince) {
        while (true) {
            elapsed = System.currentTimeMillis() - state.connectedSince
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Status row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingDot(color = NovaGreen)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CONNECTED",
                    color = NovaGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W700,
                    letterSpacing = 2.sp
                )
            }
            Text(
                text = formatDuration(elapsed),
                color = NovaGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.W500
            )
        }

        // Server IP
        LabelValue(label = "SERVER", value = state.serverIp)

        // Traffic stats
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TrafficStat(
                label = "↓ DOWNLOAD",
                value = formatBytes(state.bytesIn),
                color = NovaAccent,
                modifier = Modifier.weight(1f)
            )
            TrafficStat(
                label = "↑ UPLOAD",
                value = formatBytes(state.bytesOut),
                color = NovaGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TransitionContent(message: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        PulsingDot(color = color)
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.W500
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚠", color = NovaRed, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "CONNECTION FAILED",
                color = NovaRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = 2.sp
            )
        }
        Text(
            text = message,
            color = NovaGray,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DisconnectedContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(NovaGray, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "NOT PROTECTED",
            color = NovaGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.W600,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color.copy(alpha = alpha), androidx.compose.foundation.shape.CircleShape)
    )
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = NovaGray, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.W600)
        Text(value, color = NovaWhite, fontSize = 13.sp, fontWeight = FontWeight.W500)
    }
}

@Composable
private fun TrafficStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = NovaGray, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.W600)
        Spacer(Modifier.height(2.dp))
        Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.W700)
    }
}
