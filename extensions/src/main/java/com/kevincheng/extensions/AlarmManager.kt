package com.kevincheng.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build

fun AlarmManager.setAlarm(type: Int, triggerAtMillis: Long, operation: PendingIntent) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> this.setExact(
            type,
            triggerAtMillis,
            operation
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> this.setExactAndAllowWhileIdle(
            type,
            triggerAtMillis,
            operation
        )
        else -> this.set(type, triggerAtMillis, operation)
    }
}