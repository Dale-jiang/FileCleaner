package com.clean.filecleaner.ui.module.notification

import android.net.TrafficStats
import android.os.SystemClock
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object TrafficUtils {

    private const val TAG = "TrafficUtils"
    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var lastTime = 0L
    var currentRxSpeed = 0L
    var currentTxSpeed = 0L

    fun updateTotalTraffic() = runCatching {
        val totalRxBytes = TrafficStats.getTotalRxBytes()
        val totalTxBytes = TrafficStats.getTotalTxBytes()
        val currentTime = SystemClock.elapsedRealtime()
        if (lastTotalRxBytes != 0L) currentRxSpeed = (totalRxBytes - lastTotalRxBytes) / ((currentTime - lastTime) / 1000)
        if (lastTotalTxBytes != 0L) currentTxSpeed = (totalTxBytes - lastTotalTxBytes) / ((currentTime - lastTime) / 1000)
        lastTotalRxBytes = totalRxBytes
        lastTotalTxBytes = totalTxBytes
        lastTime = currentTime
    }

    fun formatTraffic(bytes: Long) = runCatching {
        val units by lazy { listOf("B", "KB", "MB", "GB", "TB", "PB") }
        if (bytes <= 0) return@runCatching "0 B"
        val digits = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return@runCatching "${DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digits.toDouble()))} ${units[digits]}"
    }.getOrDefault("0 B")

}