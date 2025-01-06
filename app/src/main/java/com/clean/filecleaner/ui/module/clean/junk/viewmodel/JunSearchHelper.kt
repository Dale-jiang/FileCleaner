package com.clean.filecleaner.ui.module.clean.junk.viewmodel

import java.io.File
import java.util.LinkedList

object JunSearchHelper {


    private fun getFolderTrashRegex(folder: String): String {
        return ".*([\\\\/])$folder([\\\\/]|$).*"
    }

    private fun getFileTrashRegex(file: String): String {
        return ".+" + file.replace(".", "\\.") + "$"
    }

    private fun buildTrashFilters(
        filePatterns: List<String> = emptyList(),
        folderPatterns: List<String> = emptyList(),
        isFolder: Boolean = true
    ): List<Regex> {
        val regexList = mutableListOf<Regex>()
        regexList += if (isFolder) {
            folderPatterns.map { getFolderTrashRegex(it.lowercase()).toRegex() }
        } else {
            filePatterns.map { getFileTrashRegex(it.lowercase()).toRegex() }
        }
        return regexList
    }

    val adTrashFilters by lazy {
        val filePatterns = listOf("splashad", ".exo", ".Trash")
        val folderPatterns = listOf(
            "Analytics",
            "desktop.ini",
            ".Trash",
            "LOST.DIR",
            ".DS_Store",
            "leakcanary",
            "supersonicads",
            "fseventsd",
            ".spotlight-V100",
            "splashad",
            "UnityAdsVideoCache"
        )
        buildTrashFilters(
            filePatterns = filePatterns,
            folderPatterns = folderPatterns,
            isFolder = true
        )
    }

    val logFilters by lazy {
        buildTrashFilters(
            folderPatterns = listOf("logs", "Bugreport", "bugreports", "debug_log", "MiPushLog")
        )
    }

    val tempFilters by lazy {
        val filePatterns = listOf("thumbs?.db", ".thumb[0-9]")
        val folderPatterns = listOf("temp", "temporary", "thumbnails?", ".thumbnails")
        buildTrashFilters(
            filePatterns = filePatterns,
            folderPatterns = folderPatterns,
            isFolder = true
        )
    }


    fun getFileSize(file: File): Long {
        val pool = LinkedList<File>().also { it.offer(file) }
        var size = 0L
        runCatching {
            while (pool.isNotEmpty()) {
                val pop = pool.pop()
                if (!pop.exists()) continue
                if (pop.isDirectory) {
                    pop.listFiles()?.forEach {
                        pool.offer(it)
                    }
                    size += 4096
                    continue
                }
                size += pop.length()
            }
        }
        return size
    }

}