package com.kevincheng.appextensions

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.jaredrummler.android.shell.Shell
import com.kevincheng.extensions.isGrantedRequiredPermissions
import com.kevincheng.extensions.launchIntent
import com.kevincheng.extensions.requiredPermissions
import com.kevincheng.extensions.setAlarm
import com.kevincheng.extensions.toHex
import com.orhanobut.logger.Logger
import java.io.File
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.util.Calendar
import kotlin.system.exitProcess

class App(private val applicationContext: Context) : Application.ActivityLifecycleCallbacks {
    companion object {
        private lateinit var shared: App

        internal fun install(application: Application) {
            when (::shared.isInitialized) {
                false -> {
                    shared = App(application.applicationContext)
                    application.registerActivityLifecycleCallbacks(shared)
                }
            }
        }

        val context: Context get() = shared.applicationContext
        val currentActivity: Activity? get() = shared.currentActivityWeakReference?.get()
        val launchIntent: Intent? get() = context.launchIntent
        val requiredPermissions: Array<String> get() = context.requiredPermissions
        val isGrantedRequiredPermissions: Boolean get() = context.isGrantedRequiredPermissions
        val signatures: String
            get() = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                        .signingInfo.let {
                        when {
                            it.hasMultipleSigners() -> it.apkContentsSigners
                            else -> it.signingCertificateHistory
                        }
                    }
                }
                else -> context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures
            }.joinToString("") {
                MessageDigest.getInstance("SHA").apply { update(it.toByteArray()) }.digest().toHex
            }

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
            scheduleRestartChecker()
            exitProcess(0)
        }

        fun scheduleRestart(time: Calendar) {
            val intent = Intent("${context.packageName}.APP_EXTENSIONS_SCHEDULE_RESTART")
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarm(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
        }

        fun update(apk: File) {
            when (Shell.SU.available()) {
                true -> {
                    val launcherComponent = launchIntent?.component?.flattenToString()
                    val restartCommand = launcherComponent?.let { "am start -n $launcherComponent" }
                        ?: "monkey -p ${context.packageName} -c android.intent.category.LAUNCHER 1"
                    Handler(Looper.getMainLooper()).post {
                        scheduleRestartChecker()
                        val result = Shell.SU.run("pm install -rdg ${apk.absolutePath}", restartCommand)
                        Logger.d(
                            "${when (result.isSuccessful) {
                                true -> "update successfully"
                                false -> "update unsuccessfully"
                            }
                            } -> ${when (result.isSuccessful) {
                                true -> result.stdout
                                false -> result.stderr
                            }}"
                        )
                    }
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
                    val mimeType = apk.let {
                        MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(it.absolutePath))
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

        private fun scheduleRestartChecker() {
            val intent = Intent("${context.packageName}.APP_EXTENSIONS_RESTART_CHECKING")
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarm(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000 * 60,
                pendingIntent
            )
        }
    }

    private var currentActivityWeakReference: WeakReference<Activity>? = null

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