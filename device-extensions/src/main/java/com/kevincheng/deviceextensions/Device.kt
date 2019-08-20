package com.kevincheng.deviceextensions

import android.annotation.SuppressLint
import android.provider.Settings
import com.kevincheng.appextensions.App
import com.stericson.RootTools.RootTools
import java.util.concurrent.TimeoutException

class Device {
    companion object {
        val androidId: String
            @SuppressLint("HardwareIds")
            get() = Settings.Secure.getString(App.context.contentResolver, Settings.Secure.ANDROID_ID)

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

        fun reboot() {
            if (isRooted) RootTools.restartAndroid()
        }
    }
}