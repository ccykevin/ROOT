package com.kevincheng.deviceextensions

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
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

        private val context: Context get() = shared.applicationContext
        private val scheduleRestartIntent: PendingIntent
            get() {
                val intent = Intent("${context.packageName}.DEVICE_EXTENSIONS_SCHEDULE_RESTART")
                return PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        @JvmStatic
        val androidId: String
            @SuppressLint("HardwareIds")
            get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        @JvmStatic
        val UUID: String
            get() {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val key = shared
                    .applicationContext
                    .getString(R.string.pref_deviceextensions_uuid_key)
                var uuid = sharedPreferences.getString(key, null)
                if (uuid == null) {
                    uuid = java.util.UUID.randomUUID().toString()
                    sharedPreferences.edit(commit = true) {
                        putString(key, uuid)
                    }
                }
                return uuid
            }

        @JvmStatic
        val displayMetrics: DisplayMetrics
            get() = DisplayMetrics().also {
                (shared.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay
                    .getRealMetrics(it)
            }

        @JvmStatic
        val screenWidth: Int
            get() = displayMetrics.widthPixels

        @JvmStatic
        val screenHeight: Int
            get() = displayMetrics.heightPixels

        @JvmStatic
        val densityDpi: Int
            get() = displayMetrics.densityDpi

        @JvmStatic
        val density: Float
            get() = displayMetrics.density

        @JvmStatic
        val scaledDensity: Float
            get() = displayMetrics.scaledDensity

        @JvmStatic
        val isRooted: Boolean
            get() = RootTools.isRootAvailable()

        @JvmStatic
        val isRootAccessGiven: Boolean
            get() = RootTools.isAccessGiven()

        @JvmStatic
        fun isRootAccessGiven(timeout: Int, retries: Int): Boolean {
            return try {
                RootTools.isAccessGiven(timeout, retries)
            } catch (e: TimeoutException) {
                false
            }
        }

        @JvmStatic
        fun restart(): Boolean {
            if (isRooted) RootTools.restartAndroid()
            return isRooted
        }

        @JvmStatic
        fun scheduleRestart(time: Calendar) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarm(AlarmManager.RTC_WAKEUP, time.timeInMillis, scheduleRestartIntent)
        }

        @JvmStatic
        fun cancelScheduledRestart() {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(scheduleRestartIntent.apply { cancel() })
        }
    }
}