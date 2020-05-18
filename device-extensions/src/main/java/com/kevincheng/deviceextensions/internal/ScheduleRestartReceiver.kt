package com.kevincheng.deviceextensions.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kevincheng.deviceextensions.Device

class ScheduleRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Device.restart()
    }
}