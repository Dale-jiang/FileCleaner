package com.clean.filecleaner.data

import android.Manifest
import com.clean.filecleaner.app.MyApplication

/**---------------------APP-----------------------------**/
lateinit var app: MyApplication
val storagePermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
typealias Callback = () -> Unit

const val WEB_URL = "WEB_URL"
const val USER_TERMS = "www.baidu.com"
const val PRIVACY_POLICY = "www.baidu.com"

val docMatchList = listOf(
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.openxmlformats-officedocument.presentationml.template",
    "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
    "application/pdf"
)

/**---------------------SP-----------------------------**/
const val FIRST_LAUNCH = "FIRST_LAUNCH"
const val LAST_CLEAN_CACHE_TIME = "lastCleanCacheTime"