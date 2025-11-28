package com.explorify.explorifyapp

import android.app.Application
import android.util.Log
import com.datadog.android.Datadog
import com.datadog.android.DatadogSite
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumConfiguration
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy
import com.datadog.android.BuildConfig
import com.datadog.android.rum.GlobalRumMonitor

class ExplorifyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val applicationId = "0872c3cb-fccb-4340-9e48-2f8d301e6acd"
        val clientToken = "pub47c383b7e14c477babd41e462a40c6d4"

        val environmentName = "prod"
        val appVariantName = if (BuildConfig.DEBUG) "debug" else "release"

        // CONFIGURACIÓN GENERAL
        val configuration = Configuration.Builder(
            clientToken = clientToken,
            env = environmentName,
            variant = appVariantName
        )
            .useSite(DatadogSite.US5)
            .build()

        Datadog.initialize(
            context = this,
            configuration = configuration,
            trackingConsent = TrackingConsent.GRANTED
        )


        // CONFIGURACIÓN RUM
        val rumConfiguration = RumConfiguration.Builder(applicationId)
            .trackUserInteractions()
            .trackLongTasks(300L) // 300ms
            .build()

        Rum.enable(rumConfiguration)

        Log.e("DATADOG", "Datadog está completamente inicializado")
    }
}