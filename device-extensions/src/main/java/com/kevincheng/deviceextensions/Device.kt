package com.kevincheng.deviceextensions

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.core.content.edit
import com.kevincheng.extensions.setAlarm
import com.stericson.RootTools.RootTools
import java.util.Calendar
import java.util.concurrent.TimeoutException

class Device(private val applicationContext: Context) {
    companion object {
        private lateinit var shared: Device

        internal fun install(application: Application) {
            when (::shared.isInitialized) {
                false -> shared = Device(application.applicationContext)
            }
        }

        val androidId: String
            @SuppressLint("HardwareIds")
            get() = Settings.Secure.getString(shared.applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

        val UUID: String
            get() {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(shared.applicationContext)
                val key = shared.applicationContext.getString(R.string.pref_deviceextensions_uuid_key)
                var uuid = sharedPreferences.getString(key, null)
                if (uuid == null) {
                    uuid = java.util.UUID.randomUUID().toString()
                    sharedPreferences.edit(commit = true) {
                        putString(key, uuid)
                    }
                }
                return uuid
            }

        val isRooted: Boolean
            get() = RootTools.isRootAvailable()

        val isRootAccessGiven: Boolean
            get() = RootTools.isAccessGiven()

        fun isRootAccessGiven(timeout: Int, retries: Int): Boolean {
            return try {
                RootTools.isAccessGiven(timeout, retries)
            } catch (e: TimeoutException) {
                false
            }
        }

        fun restart(): Boolean {
            if (isRooted) RootTools.restartAndroid()
            return isRooted
        }

        fun scheduleRestart(time: Calendar) {
            val intent = Intent("${shared.applicationContext.packageName}.DEVICE_EXTENSIONS_SCHEDULE_RESTART")
            val pendingIntent =
                PendingIntent.getBroadcast(shared.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = shared.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarm(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
        }
    }
}