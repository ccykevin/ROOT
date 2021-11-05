package com.kevincheng.appextensions

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.FileProvider
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jaredrummler.android.shell.Shell
import com.kevincheng.appextensions.internal.AppKeeper
import com.kevincheng.extensions.isGrantedRequiredPermissions
import com.kevincheng.extensions.launchIntent
import com.kevincheng.extensions.requiredPermissions
import com.kevincheng.extensions.toHex
import com.orhanobut.logger.Logger
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesException
import org.threeten.bp.zone.ZoneRulesProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
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
                    shared.initThreeTen(application)
                    application.registerActivityLifecycleCallbacks(shared)
                }
            }
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
        val smallestWidth: Float
            get() {
                val smallestWidth = when {
                    screenWidth < screenHeight -> screenWidth
                    else -> screenHeight
                }
                return smallestWidth / density
            }

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
        val canDrawOverlays: Boolean
            get() = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context) -> false
                else -> true
            }

        @JvmStatic
        val canRequestPackageInstalls: Boolean
            get() = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context.packageManager.canRequestPackageInstalls() -> true
                else -> when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    true -> false
                    false -> true
                }
            }

        @JvmStatic
        fun relaunch() {
            launchIntent?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                val activity = currentActivity
                if (activity != null) {
                    activity.startActivity(this)
                    finishAllActivities()
                } else {
                    context.startActivity(this)
                }
            }
        }

        @JvmStatic
        fun restart() {
            finishAllActivities()
            AppKeeper.scheduleRelaunch(context, LocalDateTime.now().plusSeconds(2))
            Handler(Looper.getMainLooper()).postDelayed({
                exitProcess(0)
            }, 1000)
        }

        @JvmStatic
        fun finishAllActivities() {
            shared.aliveActivities.forEach { it.finishAffinity() }
        }

        @JvmStatic
        fun scheduleRelaunch(time: LocalTime) {
            val now = LocalDateTime.now()
            var target = time.atDate(now.toLocalDate())
            if (target.isBefore(now)) target = target.plusDays(1)
            scheduleRelaunch(target)
        }

        @JvmStatic
        fun scheduleRelaunch(dateTime: LocalDateTime) {
            AppKeeper.scheduleRelaunch(context, dateTime)
        }

        @JvmStatic
        fun cancelScheduledRelaunch() {
            AppKeeper.cancelScheduledRelaunch(context)
        }

        @JvmStatic
        fun scheduleRestart(time: LocalTime) {
            val now = LocalDateTime.now()
            var target = time.atDate(now.toLocalDate())
            if (target.isBefore(now)) target = target.plusDays(1)
            scheduleRestart(target)
        }

        @JvmStatic
        fun scheduleRestart(dateTime: LocalDateTime) {
            AppKeeper.scheduleRestart(context, dateTime)
        }

        @JvmStatic
        fun cancelScheduledRestart() {
            AppKeeper.cancelScheduledRestart(context)
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
                        AppKeeper.schedulePatrol(context)
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

        @JvmStatic
        fun grantDrawOverlaysPermission() {
            val intent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.fromParts("package", context.packageName, null)
                ).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                else -> null
            }
            intent?.run {
                context.startActivity(this)
                finishAllActivities()
            }
        }

        @JvmStatic
        fun grantRequestPackageInstallsPermission() {
            val intent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.fromParts("package", context.packageName, null)
                ).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                else -> null
            }
            intent?.run {
                context.startActivity(this)
                finishAllActivities()
            }
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

    private fun initThreeTen(application: Application) {
        try {
            AndroidThreeTen.init(application)
        } catch (e: Exception) {
            forceSetInitializer(application)
        }
    }

    /**
     * Copied logic from [com.jakewharton.threetenabp.AssetsZoneRulesInitializer]
     */
    private fun forceSetInitializer(context: Context) {
        val assetPath = "org/threeten/bp/TZDB.dat"
        val provider: TzdbZoneRulesProvider
        var inputStream: InputStream? = null
        try {
            inputStream = context.assets.open(assetPath)
            provider = TzdbZoneRulesProvider(inputStream)
        } catch (e: IOException) {
            throw IllegalStateException("$assetPath missing from assets", e)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ignored: IOException) {
                }
            }
        }
        try {
            ZoneRulesProvider.registerProvider(provider)
        } catch (ignored: ZoneRulesException) {
            // if this exception is thrown - means it is already initialized and we are good
        }
    }
}