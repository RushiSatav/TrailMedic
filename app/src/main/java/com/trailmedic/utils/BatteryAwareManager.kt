package com.trailmedic.utils

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryAwareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _batteryPercent = MutableStateFlow(100)
    val batteryPercent: StateFlow<Int> = _batteryPercent.asStateFlow()

    private val _isBatteryUnder15 = MutableStateFlow(false)
    val isBatteryUnder15: StateFlow<Boolean> = _isBatteryUnder15.asStateFlow()

    private val _isBatteryUnder5 = MutableStateFlow(false)
    val isBatteryUnder5: StateFlow<Boolean> = _isBatteryUnder5.asStateFlow()

    private val _isLowMemoryDevice = MutableStateFlow(false)
    val isLowMemoryDevice: StateFlow<Boolean> = _isLowMemoryDevice.asStateFlow()

    private val _totalMemoryGB = MutableStateFlow(4.0f)
    val totalMemoryGB: StateFlow<Float> = _totalMemoryGB.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            intent?.let { updateBatteryStatus(it) }
        }
    }

    init {
        checkDeviceMemory()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialStatus = context.registerReceiver(batteryReceiver, filter)
        initialStatus?.let { updateBatteryStatus(it) }
    }

    private fun checkDeviceMemory() {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val totalGB = memInfo.totalMem / (1024f * 1024f * 1024f)
            _totalMemoryGB.value = totalGB
            _isLowMemoryDevice.value = totalGB < 3.0f || memInfo.lowMemory
        } catch (e: Exception) {
            _isLowMemoryDevice.value = false
        }
    }

    private fun updateBatteryStatus(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        _isCharging.value = charging

        if (level >= 0 && scale > 0) {
            val percent = ((level.toFloat() / scale.toFloat()) * 100).toInt()
            _batteryPercent.value = percent
            _isBatteryUnder15.value = percent < 15 && !charging
            _isBatteryUnder5.value = percent < 5 && !charging
        }
    }

    /**
     * Compact token budget for brief, fast, and concise emergency responses.
     */
    fun getRecommendedMaxTokens(): Int {
        return when {
            _isBatteryUnder5.value -> 80
            _isBatteryUnder15.value -> 128
            _totalMemoryGB.value >= 8.0f -> 256
            else -> 180
        }
    }
}
