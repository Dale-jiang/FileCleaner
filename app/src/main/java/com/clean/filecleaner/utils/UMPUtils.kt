package com.clean.filecleaner.utils

import androidx.appcompat.app.AppCompatActivity
import com.clean.filecleaner.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object UMPUtils {
    fun requestUMPInfo(context: AppCompatActivity, onConsentEnd: () -> Unit = {}) {
        val paramsBuilder = ConsentRequestParameters.Builder()

        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("")
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val consentInformation = UserMessagingPlatform.getConsentInformation(context)

        consentInformation.requestConsentInfoUpdate(
            context,
            paramsBuilder.build(),
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(context) { onConsentEnd() } },
            { onConsentEnd() }
        )

        if (BuildConfig.DEBUG) {
            consentInformation.reset()
        }
    }

}