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

    private val TAG = "ScreenTimeViewModel"
    private val usageManager by lazy { app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    private val excludedPackages by lazy {
        setOf(
            app.packageName,
            "com.android.settings",
            "com.android.launcher",           //  Launcher
            "com.google.android.apps.nexuslauncher",  // Google Pixel Launcher
            "com.samsung.android.launcher",    // Samsung Launcher
            "com.miui.home",                   // MIUI Launcher
            "com.huawei.android.launcher",     // Huawei Launcher
            "com.oppo.launcher",               // OPPO Launcher
            "com.vivo.launcher",             // Vivo Launcher
        )
    }

    val timeChartData = MutableLiveData<LongSparseArray<Long>>()
    val appUsageList = MutableLiveData<MutableList<AppScreenTimeInfo>>()

    private fun getRangeTimeInfoList(start: Long, end: Long) = runCatching {
        if (end - start > 259200000) {
            val resultMap = hashMapOf<String, Long>()
            val usageStatsList = usageManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            usageStatsList.forEach { usageStats ->
                if (usageStats.totalTimeInForeground > 0L && usageStats.lastTimeUsed > start) {
                    val duration = resultMap.getOrDefault(usageStats.packageName, 0L) + usageStats.totalTimeInForeground
                    resultMap[usageStats.packageName] = duration
                }
            }
            val resultList = mutableListOf<AppScreenTimeInfo>()
            resultMap.forEach { (packageName, duration) ->
                if (AppUtils.isAppInstalled(packageName) && !excludedPackages.any { it.contains(packageName) }) {
                    resultList.add(AppScreenTimeInfo(AppUtils.getAppName(packageName), packageName, AppUtils.getAppIcon(packageName), duration))
                }
            }
            appUsageList.postValue(resultList)
        } else {
            val usageEvents = usageManager.queryEvents(start, end)
            val allEventLinkedList: LinkedList<LinkedList<UsageEvents.Event>> = LinkedList()
            while (usageEvents != null && usageEvents.hasNextEvent()) {
                val event = UsageEvents.Event()
                if (usageEvents.getNextEvent(event) && event.eventType != UsageEvents.Event.ACTIVITY_STOPPED && event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED) {
                    if (allEventLinkedList.isNotEmpty() && allEventLinkedList.last.last.packageName == event.packageName) {
                        allEventLinkedList.last.addLast(event)
                    } else {
                        val pkgEventList: LinkedList<UsageEvents.Event> = LinkedList()
                        pkgEventList.addLast(event)
                        allEventLinkedList.addLast(pkgEventList)
                    }
                }
            }
            val resultMap = hashMapOf<String, Long>()
            allEventLinkedList.forEach { eventList ->
                val packageName = eventList.first.packageName
                if (AppUtils.isAppInstalled(packageName) && !excludedPackages.any { it.contains(packageName) }) {
                    var previousEvent: UsageEvents.Event? = null
                    var totalTime = 0L
                    eventList.forEach { event ->
                        if (UsageEvents.Event.ACTIVITY_RESUMED == event.eventType && null == previousEvent) {
                            previousEvent = event
                        } else if (UsageEvents.Event.ACTIVITY_PAUSED == event.eventType && null != previousEvent) {
                            val duration = event.timeStamp - (previousEvent?.timeStamp ?: 0L)
                            if (duration > 0L) totalTime += duration
                            previousEvent = null
                        }
                    }
                    if (totalTime > 0L) {
                        if (resultMap.containsKey(packageName)) {
                            resultMap[packageName] = resultMap.getOrDefault(packageName, 0L) + totalTime
                        } else resultMap[packageName] = totalTime
                    }
                }
            }

            val resultList = mutableListOf<AppScreenTimeInfo>()
            resultMap.forEach { (packageName, duration) ->
                resultList.add(AppScreenTimeInfo(AppUtils.getAppName(packageName), packageName, AppUtils.getAppIcon(packageName), duration))
            }
            appUsageList.postValue(resultList)
        }
    }


    fun fetchListData(index: Int) = runCatching {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            val timePair = fetchRangeTime(index)
            getRangeTimeInfoList(timePair.first, timePair.second)
        }
    }

    private fun fetchRangeTime(index: Int): Pair<Long, Long> {
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

    fun getRangeTotalByIndex(index: Int) = run {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            kotlin.runCatching {
                val timeInMillis = when (index) {
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

                val interval: Long = when (index) {
                    0 -> 60000L // Minute intervals for index 0
                    1, 2 -> 3600000L // Hour intervals for index 1 and 2
                    3 -> 86400000L // Day intervals for index 3
                    else -> 3600000L
                }

                val endList = LongArray(
                    when (index) {
                        0 -> 60 // For minutes
                        1, 2 -> 25 // For hours
                        3 -> 7 // For days
                        else -> 0
                    }
                ) {
                    timeInMillis - it * interval
                }

                val longSparseArray = LongSparseArray<Long>(endList.size)
                val deferredList = (1 until endList.size).map { current ->
                    async(Dispatchers.Default) {
                        val startTime = endList[current]
                        val endTime = if (current > 0) endList[current - 1] else System.currentTimeMillis()
                        Pair(startTime, getRangeTimeTotal(startTime, endTime))
                    }
                }

                deferredList.awaitAll().forEach { (startTime, total) ->
                    longSparseArray.append(startTime, total)
                }

                timeChartData.postValue(longSparseArray)
            }
        }
    }


    private suspend fun getRangeTimeTotal(start: Long, end: Long): Long = withContext(Dispatchers.IO + SupervisorJob()) {
        if (end - start > 259200000) {
            calculateTotalForegroundTime(start, end)
        } else {
            calculateTotalEventTime(start, end)
        }
    }

    private fun calculateTotalForegroundTime(start: Long, end: Long): Long {
        val resultMap = hashMapOf<String, Long>()
        val usageStatsList = usageManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)

        usageStatsList.forEach { usageStats ->
            if (usageStats.totalTimeInForeground > 0L && usageStats.lastTimeUsed > start) {
                resultMap[usageStats.packageName] = resultMap.getOrDefault(usageStats.packageName, 0L) + usageStats.totalTimeInForeground
            }
        }

        return calculateTotalFromMap(resultMap)
    }

    private fun calculateTotalEventTime(start: Long, end: Long): Long {
        val usageEvents = usageManager.queryEvents(start, end)
        val allEventLinkedList = LinkedList<UsageEvents.Event>()

        while (usageEvents != null && usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            if (usageEvents.getNextEvent(event) && event.eventType in listOf(UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED)) {
                allEventLinkedList.add(event)
            }
        }

        val resultMap = hashMapOf<String, Long>()
        var previousEvent: UsageEvents.Event? = null

        allEventLinkedList.forEach { event ->
            val packageName = event.packageName
            if (AppUtils.isAppInstalled(packageName) && !excludedPackages.any { it == packageName }) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    previousEvent = event
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && previousEvent != null) {
                    val duration = event.timeStamp - (previousEvent?.timeStamp ?: 0L)
                    if (duration > 0L) {
                        resultMap[packageName] = resultMap.getOrDefault(packageName, 0L) + duration
                    }
                    previousEvent = null
                }
            }
        }

        return calculateTotalFromMap(resultMap)
    }

    private fun calculateTotalFromMap(resultMap: HashMap<String, Long>): Long {
        var total = 0L
        resultMap.forEach { (packageName, duration) ->
            if (AppUtils.isAppInstalled(packageName) && !excludedPackages.contains(packageName)) {
                total += duration
            }
        }
        return total
    }

}