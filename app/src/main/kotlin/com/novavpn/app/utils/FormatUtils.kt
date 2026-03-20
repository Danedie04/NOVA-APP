package com.novavpn.app.utils

import kotlin.math.log10
import kotlin.math.pow

/**
 * Formats a byte count into a human-readable string.
 * e.g. 1_500_000 → "1.43 MB"
 */
fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    val exp = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size)
    val value = bytes / 1024.0.pow(exp.toDouble())
    return "%.2f %s".format(value, units[exp - 1])
}

/**
 * Formats a duration in milliseconds to HH:MM:SS.
 * e.g. 3_661_000 → "01:01:01"
 */
fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours   = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
