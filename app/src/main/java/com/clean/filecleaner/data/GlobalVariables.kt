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

val docMatchList = arrayOf(
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
    "application/pdf",
    "text/plain",
    "application/rtf",
    "text/html",
    "application/xml",
    "text/xml",
    "text/csv"
)

val apkMatchList = arrayOf("application/vnd.android.package-archive")

val audioMatchList = arrayOf(
    "audio/3gpp",      // 3gp
    "audio/mpeg",      // mp3
    "audio/ogg",       // ogg
    "audio/flac",      // flac
    "audio/midi",      // mid
    "audio/x-midi",    // mid alternative
    "audio/wav",       // wav
    "audio/x-wav",     // wav alternative
    "audio/mp4",       // m4a
    "audio/x-m4a",     // m4a alternative
    "audio/aac",       // aac
    "audio/x-ms-wma",  // wma
    "audio/aiff",      // aiff
    "audio/x-aiff"     // aiff alternative
)
