package com.kevincheng.appextensions.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kevincheng.appextensions.App

class ScheduleRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        App.restart()
    }
}