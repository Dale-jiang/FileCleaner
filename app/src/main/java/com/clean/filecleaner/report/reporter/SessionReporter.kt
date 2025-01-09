package com.clean.filecleaner.report.reporter

import com.clean.filecleaner.report.DataReportingConfig.ReportingIOScope
import kotlinx.coroutines.launch

object SessionReporter {

    fun postSession() = ReportingIOScope.launch {
        val jsonObj = EventReporter.buildCommonParams().apply {
            put("bluegill", "phosphor")
        }
        EventReporter.runRequest(jsonObj.toString(), "postSession")
    }

}