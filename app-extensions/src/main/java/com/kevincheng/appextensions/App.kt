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
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.FileProvider
import com.jaredrummler.android.shell.Shell
import com.kevincheng.extensions.isGrantedRequiredPermissions
import com.kevincheng.extensions.launchIntent
import com.kevincheng.extensions.requiredPermissions
import com.kevincheng.extensions.setAlarm
import com.kevincheng.extensions.toHex
import com.orhanobut.logger.Logger
import java.io.File
import java.security.MessageDigest
import java.util.Calendar
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.exitProcess

class App(private val applicationContext: Context) : Application.ActivityLifecycleCallbacks {
    companion object {
        private const val TAG = "APP_EXTENSIONS"
        const val REQUEST_CODE_INSTALL = 999

        private lateinit var shared: App

        internal fun install(application: Application) {
            when (::shared.isInitialized) {
                false -> {
                    shared = App(application.applicationContext)
                    application.registerActivityLifecycleCallbacks(shared)
                }
            }
        }

        private val scheduleRestartIntent: PendingIntent
            get() {
                val intent = Intent("${context.packageName}.APP_EXTENSIONS_SCHEDULE_RESTART")
                return PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        @JvmStatic
        val context: Context
            get() = shared.applicationContext

        @JvmStatic
        val currentActivity: Activity?
            get() = when (shared.aliveActivities.isNotEmpty()) {
                true -> shared.aliveActivities.last()
                false -> null
            }

        @JvmStatic
        val launchIntent: Intent?
            get() = context.launchIntent

        @JvmStatic
        val requiredPermissions: Array<String>
            get() = context.requiredPermissions

        @JvmStatic
        val isGrantedRequiredPermissions: Boolean
            get() = context.isGrantedRequiredPermissions

        @JvmStatic
        val displayMetrics: DisplayMetrics
            get() = DisplayMetrics().also {
                (shared.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay
                    .getMetrics(it)
            }

        @JvmStatic
        val screenWidth: Int
            get() = displayMetrics.widthPixels

        @JvmStatic
        val screenHeight: Int
            get() = displayMetrics.heightPixels

        @JvmStatic
        val densityDpi: Int
            get() = displayMetrics.densityDpi

        @JvmStatic
        val density: Float
            get() = displayMetrics.density

        @JvmStatic
        val scaledDensity: Float
            get() = displayMetrics.scaledDensity

        @JvmStatic
        val signatures: String
            get() = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                    )
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

        @JvmStatic
        fun relaunch() {
            launchIntent?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                val activity = currentActivity ?: return
                finishAllActivities()
                activity.startActivity(this)
            }
        }

        @JvmStatic
        fun restart() {
            finishAllActivities()
            val pendingIntent =
                PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_ONE_SHOT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarm(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 100,
                pendingIntent
            )
            scheduleRestartChecker()
            exitProcess(0)
        }

        @JvmStatic
        fun finishAllActivities() {
            shared.aliveActivities.forEach { it.finishAffinity() }
        }

        @JvmStatic
        fun scheduleRestart(time: Calendar) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarm(AlarmManager.RTC_WAKEUP, time.timeInMillis, scheduleRestartIntent)
        }

        @JvmStatic
        fun cancelScheduledRestart() {
            val intent = scheduleRestartIntent.apply { cancel() }
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(intent)
        }

        @JvmStatic
        fun update(apk: File) {
            when (Shell.SU.available()) {
                true -> {
                    val launcherComponent = launchIntent?.component?.flattenToString()
                    val restartCommand = launcherComponent?.let { "am start -n $launcherComponent" }
                        ?: "monkey -p ${context.packageName} -c android.intent.category.LAUNCHER 1"
                    Logger.t(TAG).d("Trying to install update silently")
                    Handler(Looper.getMainLooper()).post {
                        scheduleRestartChecker()
                        val result =
                            Shell.SU.run("pm install -r -d ${apk.absolutePath}", restartCommand)
                        Logger.t(TAG).d(
                            "Install ${when (result.isSuccessful) {
                                true -> "succeeded"
                                false -> "failed"
                            }} -> ${when (result.isSuccessful) {
                                true -> result.stdout
                                false -> result.stderr
                            }}"
                        )
                    }
                }
                else -> {
                    val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).also {
                        it.data = getUriForFile(apk)
                        it.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                        it.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                        it.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
                    }
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> intent.also {
                            it.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                    }
                    val activity = currentActivity
                    when {
                        activity != null -> {
                            activity.startActivityForResult(intent, REQUEST_CODE_INSTALL)
                            Logger.t(TAG)
                                .d("${activity.javaClass.simpleName} start PackageInstallerActivity for result")
                        }
                        else -> {
                            context.startActivity(intent.also {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            })
                            Logger.t(TAG).d("Start PackageInstallerActivity")
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun getUriForFile(file: File): Uri {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> FileProvider.getUriForFile(
                    context,
                    context.packageName,
                    file
                )
                else -> Uri.fromFile(file)
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

    private val aliveActivities = CopyOnWriteArrayList<Activity>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        aliveActivities.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        aliveActivities.takeIf { it.contains(activity) }?.also { it.remove(activity) }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
    }
}