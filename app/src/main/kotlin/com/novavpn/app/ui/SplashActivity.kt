package com.novavpn.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novavpn.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Splash screen that displays the NovaVPN logo with animated entrance,
 * then transitions to [MainActivity].
 *
 * Note: Not annotated with @AndroidEntryPoint as Hilt isn't needed here.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NovaVpnTheme {
                SplashScreen(
                    onSplashComplete = {
                        startActivity(Intent(this, MainActivity::class.java))
                        // Smooth transition
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(onSplashComplete: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(2000)
        onSplashComplete()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(NovaDarkBlue, NovaBlack),
                    radius = 1000f
                )
            )
    ) {
        val transition = updateTransition(visible, label = "splash")

        val scale by transition.animateFloat(
            transitionSpec = {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            },
            label = "scale"
        ) { if (it) 1f else 0.6f }

        val alpha by transition.animateFloat(
            transitionSpec = { tween(600) },
            label = "alpha"
        ) { if (it) 1f else 0f }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .graphicsAlpha(alpha)
        ) {
            // Shield icon placeholder (using text art)
            Text(
                text = "⬡",
                color = NovaAccent,
                fontSize = 72.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "NOVA VPN",
                color = NovaWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.W800,
                letterSpacing = 6.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "SECURE · PRIVATE · FAST",
                color = NovaGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.W500,
                letterSpacing = 3.sp
            )
        }
    }
}

// Extension to apply alpha via graphicsLayer
private fun Modifier.graphicsAlpha(alpha: Float): Modifier =
    this.graphicsLayer { this.alpha = alpha }
