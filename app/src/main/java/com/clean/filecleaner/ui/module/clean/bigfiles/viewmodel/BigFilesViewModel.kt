package com.clean.filecleaner.ui.module.clean.bigfiles.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.ui.module.filemanager.FileInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BigFilesViewModel : ViewModel() {

    companion object {

        val bigFiles = mutableListOf<FileInfo>()

        private const val MIN_FILE_SIZE = 10_000_000L // 10MB
        private const val PAGE_LIMIT = 500
        private val FILE_PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
    }

    private val _onCompleted = MutableLiveData<Boolean>()
    val onCompleted: LiveData<Boolean> = _onCompleted


    fun queryFiles(context: Context) {

        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            LogUtils.e("Error in queryFiles: ${throwable.message}")
        }

        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            var lastId = 0
            bigFiles.clear()
            // 循环查询，直到查询结果不足一个批次
            while (true) {
                val newLastId = queryBatch(context, lastId)
                // 若本次查询没有返回新数据，则退出循环
                if (newLastId == null || newLastId == lastId) break
                lastId = newLastId
            }

            _onCompleted.postValue(true)
        }
    }

    private fun queryBatch(context: Context, startId: Int): Int? {
        val externalUri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Files.FileColumns.SIZE} > ? AND ${MediaStore.Files.FileColumns._ID} > ?"
        val selectionArgs = arrayOf(MIN_FILE_SIZE.toString(), startId.toString())

        val cursor: Cursor? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val queryArgs = Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                putString(ContentResolver.QUERY_ARG_SORT_COLUMNS, MediaStore.Files.FileColumns._ID)
                putInt(ContentResolver.QUERY_ARG_LIMIT, PAGE_LIMIT)
            }
            context.contentResolver.query(externalUri, FILE_PROJECTION, queryArgs, null)
        } else {
            context.contentResolver.query(
                externalUri,
                FILE_PROJECTION,
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns._ID} LIMIT $PAGE_LIMIT"
            )
        }

        // 没有数据时返回 null
        if (cursor == null || cursor.count == 0) return null

        var newLastId = startId
        var count = cursor.count
        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val dataIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateAddedIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val displayNameIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val mimeTypeIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                do {
                    val id = c.getIntOrNull(idIndex) ?: continue
                    newLastId = id

                    val path = c.getStringOrNull(dataIndex) ?: continue
                    val size = c.getLongOrNull(sizeIndex) ?: 0L
                    val dateAdded = c.getLongOrNull(dateAddedIndex) ?: 0L
                    val displayName = c.getStringOrNull(displayNameIndex) ?: ""
                    val mimeType = c.getStringOrNull(mimeTypeIndex) ?: "*/*"

                    if (path.isNotEmpty() && File(path).exists()) {
                        val fileType = BigFilesHelper.getFileTypeByMimeType(mimeType)
                        val fileInfo = FileInfo(
                            size = size,
                            sizeString = Formatter.formatFileSize(context, size),
                            addTime = dateAdded * 1000L,
                            path = path,
                            name = displayName,
                            mimetype = mimeType,
                            filetype = fileType
                        )
                        bigFiles.add(fileInfo)
                    }
                } while (c.moveToNext())
            }
        }
        return if (count >= PAGE_LIMIT) newLastId else null
    }


}