package com.clean.filecleaner.data

import android.Manifest
import com.clean.filecleaner.app.MyApplication

/**---------------------APP-----------------------------**/
lateinit var app: MyApplication
val storagePermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
typealias Callback = () -> Unit

const val WEB_URL = "WEB_URL"

/**---------------------SP-----------------------------**/
const val FIRST_LAUNCH = "FIRST_LAUNCH"