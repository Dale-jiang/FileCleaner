package com.clean.filecleaner.utils

object AndroidVersionUtils {

    fun isAndroid8OrAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
    }

    fun isAndroid9OrAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P
    }

    fun isAndroid10OrAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    }

    fun isAndroid11OrAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R
    }

    fun isAndroid12OrAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
    }

    fun isAndroid13OrAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
    }

    fun isAndroid14OrAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

}