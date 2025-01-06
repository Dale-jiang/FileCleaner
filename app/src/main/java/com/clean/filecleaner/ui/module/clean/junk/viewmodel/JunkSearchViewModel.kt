package com.clean.filecleaner.ui.module.clean.junk.viewmodel

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clean.filecleaner.ui.module.clean.junk.bean.CleanJunkType
import com.clean.filecleaner.ui.module.clean.junk.bean.JunkType
import com.clean.filecleaner.ui.module.clean.junk.bean.TrashItem
import com.clean.filecleaner.ui.module.clean.junk.bean.TrashItemParent
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.JunSearchHelper.adTrashFilters
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.JunSearchHelper.getFileSize
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.JunSearchHelper.logFilters
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.JunSearchHelper.tempFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

val allJunkDataList = mutableListOf<CleanJunkType>()

class JunkSearchViewModel : ViewModel() {

    private var searchTaskJob: Job? = null
    val pathChangeObserver = MutableLiveData<String>()
    val junkDetailsTempList = mutableListOf<CleanJunkType>()
    val itemChangeObserver = MutableLiveData<CleanJunkType>()
    val createJunkDataListObserver = MutableLiveData<JunkType?>()

    fun searchJunk() {
        searchTaskJob?.cancel()
        searchTaskJob = viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            try {
                doScanning()
                createJunkDataList()
            } catch (e: CancellationException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun doScanning(file: File = Environment.getExternalStorageDirectory()) {
        val files = file.listFiles() ?: return
        for (singleFile in files) {
            if (!singleFile.exists()) continue
            val filePath = singleFile.absolutePath
            pathChangeObserver.postValue(filePath)
            processFile(singleFile, filePath)
        }
    }

    private suspend fun processFile(singleFile: File, filePath: String) {
        if (singleFile.isDirectory) {
            val trashType = categorizeAndAddItem(singleFile, filePath)
            if (trashType == null) {
                doScanning(singleFile)
            }
        } else {
            categorizeAndAddItem(singleFile, filePath)
        }
    }

    private fun categorizeAndAddItem(singleFile: File, filePath: String): JunkType? {
        val lowerCasePath = filePath.lowercase()
        val trashType = when {
            lowerCasePath.endsWith(".log") || logFilters.any { lowerCasePath.matches(it) } -> JunkType.LOG_FILES
            lowerCasePath.contains(".thumb") || lowerCasePath.endsWith(".tmp") || tempFilters.any { lowerCasePath.matches(it) } -> JunkType.TEMP_FILES
            adTrashFilters.any { lowerCasePath.matches(it) } -> JunkType.AD_JUNK
            lowerCasePath.endsWith(".apk") || lowerCasePath.endsWith(".aab") -> JunkType.APK_FILES
            else -> null
        }

        trashType?.let { type ->
            val item = TrashItem(name = singleFile.name, path = filePath, fileSize = getFileSize(singleFile), trashType = type, select = true)
            synchronized(junkDetailsTempList) {
                junkDetailsTempList.add(item)
            }
            itemChangeObserver.postValue(item)
        }
        return trashType
    }


    private fun createJunkDataList() {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            allJunkDataList.clear()
            val trashTypesOrder = listOf(JunkType.APP_CACHE, JunkType.APK_FILES, JunkType.LOG_FILES, JunkType.AD_JUNK, JunkType.TEMP_FILES)
            val delayPerType = mapOf(JunkType.APP_CACHE to 200L, JunkType.APK_FILES to 200L, JunkType.LOG_FILES to 200L, JunkType.AD_JUNK to 200L, JunkType.TEMP_FILES to 500L)

            for (trashType in trashTypesOrder) {
                val filteredList = junkDetailsTempList.filter { it.trashType == trashType }
                if (filteredList.isNotEmpty()) {
                    val totalSize = filteredList.sumOf { it.fileSize }
                    val parentItem = TrashItemParent(subItems = filteredList.toMutableList(), isOpen = false, fileSize = totalSize, trashType = trashType, select = true)

                    synchronized(allJunkDataList) {
                        allJunkDataList.add(parentItem)
                    }

                    withContext(Dispatchers.Main) {
                        delay(delayPerType[trashType] ?: 200L)
                        createJunkDataListObserver.value = trashType
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        delay(delayPerType[trashType] ?: 200L)
                        createJunkDataListObserver.value = trashType
                    }
                }
            }
        }
    }

}