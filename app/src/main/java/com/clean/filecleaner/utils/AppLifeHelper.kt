package com.clean.filecleaner.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.canInteractive
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.SettingTipsActivity
import com.clean.filecleaner.ui.module.SplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.cancellation.CancellationException

object AppLifeHelper {

    private const val TAG = "AppLifeHelper"

    private val activities = CopyOnWriteArrayList<WeakReference<Activity>>()
    var jumpToSettings = false
    private var needHotRestart = false
    private var finishJob: Job? = null
    private var foregroundActivityCount = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun isAppForeground(): Boolean = foregroundActivityCount > 0

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(AppLifecycleCallbacks())
        LogUtils.d(TAG, "AppLifeHelper initialized")
    }

    private class AppLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(WeakReference(activity))
            LogUtils.d(TAG, "Activity created: ${activity.localClassName}")
        }

        override fun onActivityStarted(activity: Activity) {
            LogUtils.d(TAG, "Activity started: ${activity.localClassName}")
            finishJob?.cancel()
            foregroundActivityCount++
            if (needHotRestart) {
                LogUtils.d(TAG, "Need hot restart triggered")
                needHotRestart = false

                if (!app.canInteractive()) {
                    LogUtils.d(TAG, "App cannot interact, aborting SplashActivity launch")
                    return
                }

                if (jumpToSettings) {
                    LogUtils.d(TAG, "Returning from settings, skipping SplashActivity launch")
                    jumpToSettings = false
                    return
                }

                launchSplashActivity(activity)
            }
        }

        override fun onActivityResumed(activity: Activity) {
            LogUtils.d(TAG, "Activity resumed: ${activity.localClassName}")
        }

        override fun onActivityPaused(activity: Activity) {
            LogUtils.d(TAG, "Activity paused: ${activity.localClassName}")
        }

        override fun onActivityStopped(activity: Activity) {
            LogUtils.d(TAG, "Activity stopped: ${activity.localClassName}")
            finishJob?.cancel()
            foregroundActivityCount--
            if (foregroundActivityCount <= 0) {
                if (jumpToSettings || activity is SettingTipsActivity) return
                LogUtils.d(TAG, "App moved to background, scheduling finish all activities")
                finishJob = scope.launch {
                    try {
                        delay(3000L)
                        LogUtils.d(TAG, "Closing all activities except MainActivity")
                        activities.forEach { weakRef ->
                            weakRef.get()?.let { act ->
                                if (act !is MainActivity) {
                                    LogUtils.d(TAG, "Finishing activity: ${act.localClassName}")
                                    act.finish()
                                }
                            }
                        }
                        needHotRestart = true
                    } catch (e: CancellationException) {
                        LogUtils.d(TAG, "Finish job was cancelled")
                    } catch (e: Exception) {
                        LogUtils.e(TAG, "Error in finishJob: ${e.message}")
                    }
                }
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            LogUtils.d(TAG, "Activity destroyed: ${activity.localClassName}")
            activities.removeAll { it.get() == activity }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            LogUtils.d(TAG, "Activity save instance state: ${activity.localClassName}")
        }

        private fun launchSplashActivity(activity: Activity) {
            activity.startActivity(
                Intent(activity, SplashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            LogUtils.d(TAG, "SplashActivity launched from ${activity.localClassName}")
        }

    }

}