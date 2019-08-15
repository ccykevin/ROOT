package com.kevincheng.deviceextensions

import android.annotation.SuppressLint
import android.provider.Settings
import com.kevincheng.appextensions.App
import com.stericson.RootTools.RootTools

class Device {
    companion object {
        val androidId: String
            @SuppressLint("HardwareIds")
            get() = Settings.Secure.getString(App.context.contentResolver, Settings.Secure.ANDROID_ID)

        val isRooted: Boolean
            get() = when (RootTools.isRootAvailable()) {
                true -> RootTools.isAccessGiven()
                false -> false
            }

        fun reboot() {
            if (isRooted) RootTools.restartAndroid()
        }
    }
}