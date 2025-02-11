package com.clean.filecleaner.ui.module.clean.recent.viewmodel

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.LongSparseArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ui.module.clean.recent.AppScreenTimeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.LinkedList

class ScreenTimeViewModel : ViewModel() {

    private val TAG = "TimeTrackerViewModel"
    private val usageStatsManager by lazy { app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    private val excludedAppPackages by lazy {
        setOf(
            app.packageName,
            "com.android.settings",
            "com.android.launcher",
            "com.google.android.apps.nexuslauncher",
            "com.samsung.android.launcher",
            "com.miui.home",
            "com.huawei.android.launcher",
            "com.oppo.launcher",
            "com.vivo.launcher",
        )
    }

    val timeData = MutableLiveData<LongSparseArray<Long>>()
    val appUsageDetails = MutableLiveData<MutableList<AppScreenTimeInfo>>()

    private fun retrieveUsageInfoInRange(start: Long, end: Long) {
        if (end - start > 259200000) {
            try {
                val usageMap = hashMapOf<String, Long>()
                val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
                usageStatsList.forEach { stats ->
                    if (stats.totalTimeInForeground > 0L && stats.lastTimeUsed > start) {
                        val time = usageMap.getOrDefault(stats.packageName, 0L) + stats.totalTimeInForeground
                        usageMap[stats.packageName] = time
                    }
                }
                val usageList = mutableListOf<AppScreenTimeInfo>()
                usageMap.forEach { (packageName, duration) ->
                    if (AppUtils.isAppInstalled(packageName) && !excludedAppPackages.any { it.contains(packageName) }) {
                        usageList.add(AppScreenTimeInfo(AppUtils.getAppName(packageName), packageName, AppUtils.getAppIcon(packageName), duration))
                    }
                }
                appUsageDetails.postValue(usageList)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return
        }

        runCatching {
            val usageEvents = usageStatsManager.queryEvents(start, end)
            val eventGroupList: LinkedList<LinkedList<UsageEvents.Event>> = LinkedList()
            while (usageEvents != null && usageEvents.hasNextEvent()) {
                val event = UsageEvents.Event()
                if (usageEvents.getNextEvent(event) && event.eventType != UsageEvents.Event.ACTIVITY_STOPPED && event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED) {
                    if (eventGroupList.isNotEmpty() && eventGroupList.last.last.packageName == event.packageName) {
                        eventGroupList.last.addLast(event)
                    } else {
                        val newPkgEventList: LinkedList<UsageEvents.Event> = LinkedList()
                        newPkgEventList.addLast(event)
                        eventGroupList.addLast(newPkgEventList)
                    }
                }
            }
            val usageMap = hashMapOf<String, Long>()
            eventGroupList.forEach { eventList ->
                val packageName = eventList.first.packageName
                if (AppUtils.isAppInstalled(packageName) && !excludedAppPackages.any { it.contains(packageName) }) {
                    var prevEvent: UsageEvents.Event? = null
                    var totalUsageTime = 0L
                    eventList.forEach { event ->
                        if (UsageEvents.Event.ACTIVITY_RESUMED == event.eventType && prevEvent == null) {
                            prevEvent = event
                        } else if (UsageEvents.Event.ACTIVITY_PAUSED == event.eventType && prevEvent != null) {
                            val timeSpent = event.timeStamp - (prevEvent?.timeStamp ?: 0L)
                            if (timeSpent > 0L) totalUsageTime += timeSpent
                            prevEvent = null
                        }
                    }
                    if (totalUsageTime > 0L) {
                        usageMap[packageName] = usageMap.getOrDefault(packageName, 0L) + totalUsageTime
                    }
                }
            }

            val usageList = mutableListOf<AppScreenTimeInfo>()
            usageMap.forEach { (packageName, duration) ->
                usageList.add(AppScreenTimeInfo(AppUtils.getAppName(packageName), packageName, AppUtils.getAppIcon(packageName), duration))
            }
            appUsageDetails.postValue(usageList)
        }
    }

    fun fetchUsageData(index: Int) = run {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            runCatching {
                val (start, end) = getTimeRange(index)
                retrieveUsageInfoInRange(start, end)
            }
        }
    }

    private fun getTimeRange(index: Int): Pair<Long, Long> = let {
        val calendar = Calendar.getInstance()

        val currentTime = System.currentTimeMillis()
        val startTime: Long

        when (index) {
            1 -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
            }

            2 -> {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
                return Pair(startTime, startTime + 86400000)
            }

            3 -> {
                calendar.add(Calendar.DAY_OF_MONTH, -6)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
            }

            else -> {
                return Pair(currentTime - 3600000, currentTime)
            }
        }

        return Pair(startTime, currentTime)
    }

    fun getTotalUsageTimeByIndex(index: Int) = run {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            kotlin.runCatching {
                val startMillis = when (index) {
                    0 -> Calendar.getInstance().apply {
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    1 -> Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    2, 3 -> Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    else -> System.currentTimeMillis()
                }

                val intervalMillis: Long = when (index) {
                    0 -> 60000L
                    1, 2 -> 3600000L
                    3 -> 86400000L
                    else -> 3600000L
                }

                val endTimes = LongArray(
                    when (index) {
                        0 -> 60
                        1, 2 -> 25
                        3 -> 7
                        else -> 0
                    }
                ) {
                    startMillis - it * intervalMillis
                }

                val timeArray = LongSparseArray<Long>(endTimes.size)
                val tasks = (1 until endTimes.size).map { idx ->
                    async(Dispatchers.Default) {
                        val start = endTimes[idx]
                        val end = if (idx > 0) endTimes[idx - 1] else System.currentTimeMillis()
                        Pair(start, calculateTotalTimeInRange(start, end))
                    }
                }

                tasks.awaitAll().forEach { (start, total) ->
                    timeArray.append(start, total)
                }

                timeData.postValue(timeArray)
            }
        }
    }

    private suspend fun calculateTotalTimeInRange(start: Long, end: Long): Long = withContext(Dispatchers.IO + SupervisorJob()) {
        if (end - start > 259200000) {
            calculateForegroundTimeTotal(start, end)
        } else {
            calculateEventTimeTotal(start, end)
        }
    }

    private fun calculateForegroundTimeTotal(start: Long, end: Long): Long = let {
        val usageMap = hashMapOf<String, Long>()
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)

        usageStatsList.forEach { stats ->
            if (stats.totalTimeInForeground > 0L && stats.lastTimeUsed > start) {
                usageMap[stats.packageName] = usageMap.getOrDefault(stats.packageName, 0L) + stats.totalTimeInForeground
            }
        }

        aggregateTotalFromMap(usageMap)
    }

    private fun calculateEventTimeTotal(start: Long, end: Long): Long = let {
        val usageEvents = usageStatsManager.queryEvents(start, end)
        val eventList = LinkedList<UsageEvents.Event>()

        while (usageEvents != null && usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            if (usageEvents.getNextEvent(event) && event.eventType in listOf(UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED)) {
                eventList.add(event)
            }
        }

        val usageMap = hashMapOf<String, Long>()
        var prevEvent: UsageEvents.Event? = null

        eventList.forEach { event ->
            val packageName = event.packageName
            if (AppUtils.isAppInstalled(packageName) && !excludedAppPackages.any { it == packageName }) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    prevEvent = event
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && prevEvent != null) {
                    val duration = event.timeStamp - (prevEvent?.timeStamp ?: 0L)
                    if (duration > 0L) {
                        usageMap[packageName] = usageMap.getOrDefault(packageName, 0L) + duration
                    }
                    prevEvent = null
                }
            }
        }

        aggregateTotalFromMap(usageMap)
    }

    private fun aggregateTotalFromMap(usageMap: HashMap<String, Long>): Long = let {
        var total = 0L
        usageMap.forEach { (packageName, duration) ->
            if (!excludedAppPackages.contains(packageName) && AppUtils.isAppInstalled(packageName)) {
                total += duration
            }
        }
        total
    }

}