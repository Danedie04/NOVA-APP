package com.novavpn.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novavpn.app.ui.theme.*
import com.novavpn.app.vpn.VpnConfig

/**
 * Horizontally-scrollable server selection row (inline, not a bottom sheet).
 * Shows all available server presets; tapping one selects it.
 */
@Composable
fun ServerSelector(
    servers: List<VpnConfig>,
    selectedConfig: VpnConfig,
    onServerSelected: (VpnConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "SELECT SERVER",
            color = NovaGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.W600,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 260.dp)
        ) {
            items(servers) { server ->
                ServerRow(
                    config = server,
                    isSelected = server.serverIp == selectedConfig.serverIp,
                    onClick = { onServerSelected(server) }
                )
            }
        }
    }
}

@Composable
private fun ServerRow(
    config: VpnConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) NovaAccent.copy(alpha = 0.6f) else NovaGray.copy(alpha = 0.15f),
        animationSpec = tween(300),
        label = "serverBorder"
    )
    val bgStart by animateColorAsState(
        targetValue = if (isSelected) NovaAccent.copy(alpha = 0.08f) else NovaSurface,
        animationSpec = tween(300),
        label = "serverBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) NovaAccent else NovaWhite,
        animationSpec = tween(300),
        label = "serverText"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(bgStart, NovaCard)))
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Extract emoji flag from server name (first character sequence)
        val nameParts = config.serverName.split(" ", limit = 2)
        val flag = if (nameParts.size > 1) nameParts[0] else "🌐"
        val name = if (nameParts.size > 1) nameParts[1] else config.serverName

        Text(text = flag, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600
            )
            Text(
                text = config.serverIp,
                color = NovaGray,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(NovaAccent, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
