package com.clean.filecleaner.ui.module.clean.recent.viewmodel

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ui.module.clean.recent.AppLaunchInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList

class LaunchesInfoViewModel : ViewModel() {

    private val systemPackages by lazy { setOf(app.packageName, "com.android.mms", "com.android.contacts", "com.android.settings") }
    private val usageStatsManager by lazy { app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    val launchInfosLiveData = MutableLiveData<MutableList<AppLaunchInfo>>()

    fun queryAppLaunches(start: Long, end: Long) = runCatching {
        viewModelScope.launch(Dispatchers.IO) {
            val groupedEvents = getGroupedEvents(start, end)
            val foregroundEventsMap = getForegroundEvents(start, end)

            val result = generateLaunchInfos(foregroundEventsMap, groupedEvents)

            // 过滤系统包
            launchInfosLiveData.postValue(result.filterNot { isSystemPackage(it.packageName) }.toMutableList())
        }
    }

    // 判断是否为系统包
    private fun isSystemPackage(packageName: String): Boolean {
        return systemPackages.contains(packageName)
    }

    // 获取事件并分组
    private fun getGroupedEvents(start: Long, end: Long): LinkedList<LinkedList<UsageEvents.Event>> {
        val eventGroups = LinkedList<LinkedList<UsageEvents.Event>>()
        val queryEvents = usageStatsManager.queryEvents(start, end)

        while (queryEvents?.hasNextEvent() == true) {
            val event = UsageEvents.Event()
            if (queryEvents.getNextEvent(event) &&
                event.eventType != UsageEvents.Event.ACTIVITY_STOPPED &&
                event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED
            ) {
                if (eventGroups.isNotEmpty() && eventGroups.last.last.packageName == event.packageName) {
                    eventGroups.last.addLast(event)
                } else {
                    eventGroups.add(LinkedList<UsageEvents.Event>().apply { add(event) })
                }
            }
        }
        return eventGroups
    }

    // 获取前台活动事件
    private fun getForegroundEvents(start: Long, end: Long): MutableMap<String, MutableList<UsageEvents.Event>> {
        val foregroundEventsMap = mutableMapOf<String, MutableList<UsageEvents.Event>>()
        var lastEvent: UsageEvents.Event? = null
        val queryEvents = usageStatsManager.queryEvents(start, end)

        while (queryEvents?.hasNextEvent() == true) {
            val event = UsageEvents.Event()
            if (queryEvents.getNextEvent(event) &&
                event.eventType != UsageEvents.Event.ACTIVITY_STOPPED &&
                event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED
            ) {
                if (lastEvent?.packageName != event.packageName &&
                    (lastEvent == null || lastEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) &&
                    event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                ) {
                    foregroundEventsMap.getOrPut(event.packageName) { mutableListOf() }.add(event)
                }
                lastEvent = event
            }
        }
        return foregroundEventsMap
    }

    // 生成应用启动信息
    private fun generateLaunchInfos(
        foregroundEventsMap: MutableMap<String, MutableList<UsageEvents.Event>>,
        eventGroups: LinkedList<LinkedList<UsageEvents.Event>>
    ): MutableList<AppLaunchInfo> {
        val launchCountMap = eventGroups
            .filter { it.size > 1 }
            .groupingBy { it.first.packageName }
            .eachCount()

        return foregroundEventsMap.mapNotNull { (packageName, events) ->
            if (AppUtils.isAppInstalled(packageName)) {
                val totalLaunches = maxOf(launchCountMap.getOrDefault(packageName, 0), events.size)
                AppLaunchInfo(
                    AppUtils.getAppName(packageName),
                    packageName,
                    AppUtils.getAppIcon(packageName),
                    totalLaunches,
                    events.size,
                    totalLaunches - events.size
                )
            } else null
        }.toMutableList()
    }
}
