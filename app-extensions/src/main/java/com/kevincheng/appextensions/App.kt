package com.kevincheng.appextensions

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.FileProvider
import com.jaredrummler.android.shell.Shell
import com.kevincheng.extensions.defaultSharedPreferences
import com.kevincheng.extensions.isGrantedRequiredPermissions
import com.kevincheng.extensions.launchIntent
import com.kevincheng.extensions.requiredPermissions
import com.kevincheng.extensions.setAlarm
import java.io.File
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

class App : Application.ActivityLifecycleCallbacks {
    companion object {
        internal val shared = App()

        val context: Context get() = shared.applicationContext
        val currentActivity: Activity? get() = shared.currentActivityWeakReference?.get()
        val launchIntent: Intent? get() = context.launchIntent
        val requiredPermissions: Array<String> get() = context.requiredPermissions
        val isGrantedRequiredPermissions: Boolean get() = context.isGrantedRequiredPermissions
        val defaultSharedPreferences: SharedPreferences get() = context.defaultSharedPreferences

        fun relaunch() {
            launchIntent?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                val activity = currentActivity ?: return
                activity.finishAffinity()
                activity.startActivity(this)
            }
        }

        fun restart() {
            currentActivity?.finishAffinity()
            val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_ONE_SHOT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarm(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 100,
                pendingIntent
            )
            exitProcess(0)
        }

        fun installUpdate(apk: File, mimeType: String) {
            when (Shell.SU.available()) {
                true -> {
                    val launcherComponent = launchIntent?.component?.flattenToString()
                    val restartCommand = launcherComponent?.let { "am start -n $launcherComponent" }
                        ?: "monkey -p ${context.packageName} -c android.intent.category.LAUNCHER 1"
                    Shell.SU.run(
                        "pm install -rdg ${apk.absolutePath}",
                        restartCommand
                    )
                }
                else -> {
                    val fileUri = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> FileProvider.getUriForFile(
                            context,
                            context.packageName,
                            apk
                        )
                        else -> Uri.fromFile(apk)
                    }
                    val intent = Intent(Intent.ACTION_VIEW, fileUri)
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    intent.setDataAndType(fileUri, mimeType)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(intent)
                }
            }
        }

        fun getPreferenceAsString(strId: Int, defStrId: Int): String {
            return defaultSharedPreferences.getString(context.getString(strId), null) ?: context.getString(defStrId)
        }

        fun getPreferenceAsBoolean(strId: Int, defStrId: Int): Boolean {
            return defaultSharedPreferences.getBoolean(
                context.getString(strId),
                context.resources.getBoolean(defStrId)
            )
        }
    }

    private var currentActivityWeakReference: WeakReference<Activity>? = null
    internal lateinit var applicationContext: Context

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        activity?.apply {
            currentActivityWeakReference = WeakReference(this)
        }
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }
}