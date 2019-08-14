package com.kevincheng.extensions

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat

fun Context.isServiceRunning(service: Class<*>): Boolean {
    return (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == service.name }
}

inline val Context.requiredPermissions: Array<String>
    get() = packageManager
        .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        .requestedPermissions

inline val Context.isGrantedRequiredPermissions: Boolean
    get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> requiredPermissions.map {
            ContextCompat.checkSelfPermission(this, it)
        }.none { it != PackageManager.PERMISSION_GRANTED }
        else -> true
    }

inline val Context.launchIntent: Intent?
    get() = packageManager.getLaunchIntentForPackage(packageName)

inline val Context.defaultSharedPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)
