package com.trailmedic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrailMedicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Model initialization is safely deferred to on-demand usage to protect against native driver faults
    }
}
