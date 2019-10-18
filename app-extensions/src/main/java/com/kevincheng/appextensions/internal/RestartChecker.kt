package com.kevincheng.appextensions.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kevincheng.appextensions.App

internal class RestartChecker : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        /** adb shell am broadcast -a com.kevincheng.rootexample.APP_EXTENSIONS_RESTART_CHECKING -p com.kevincheng.rootexample */
        if (App.currentActivity == null) {
            App.context.startActivity(App.launchIntent)
        }
    }
}