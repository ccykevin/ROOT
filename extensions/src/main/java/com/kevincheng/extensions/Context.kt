package com.kevincheng.extensions

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
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
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            requiredPermissions.map {
                when {
                    it == Manifest.permission.FOREGROUND_SERVICE && Build.VERSION.SDK_INT < Build.VERSION_CODES.P -> true
                    it == Manifest.permission.SYSTEM_ALERT_WINDOW -> when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> Settings.canDrawOverlays(
                            this
                        )
                        else -> true
                    }
                    it == Manifest.permission.REQUEST_INSTALL_PACKAGES -> when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> packageManager.canRequestPackageInstalls()
                        else -> true
                    }
                    else -> when (ContextCompat.checkSelfPermission(this, it)) {
                        PackageManager.PERMISSION_GRANTED -> true
                        else -> false
                    }
                }
            }.none { !it }
        }
        else -> true
    }

inline val Context.launchIntent: Intent?
    get() = packageManager.getLaunchIntentForPackage(packageName)

inline val Context.defaultSharedPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

fun Context.getDrawable(name: String): Drawable? {
    val id = resources.getIdentifier(name, "drawable", packageName)
    /** if id equal 0 means drawable not found */
    if (id == 0) return null
    return ContextCompat.getDrawable(this, id)
}
