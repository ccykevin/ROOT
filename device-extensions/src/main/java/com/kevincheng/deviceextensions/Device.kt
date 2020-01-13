package com.kevincheng.deviceextensions

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.core.content.edit
import com.stericson.RootTools.RootTools
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
            get() = RootTools.isAccessGiven(0, Int.MAX_VALUE)

        fun isRootAccessGiven(timeout: Int = 0, retries: Int = 3): Boolean {
            return try {
                RootTools.isAccessGiven(timeout, retries)
            } catch (e: TimeoutException) {
                false
            }
        }

        fun reboot(): Boolean {
            if (isRooted) RootTools.restartAndroid()
            return isRooted
        }
    }
}