package com.novavpn.app.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Shows a short toast message from any Context. */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/** Applies an alpha value via graphicsLayer (inline helper). */
fun Modifier.alpha(value: Float): Modifier = this.graphicsLayer { alpha = value }
