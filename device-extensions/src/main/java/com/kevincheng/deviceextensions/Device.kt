package com.kevincheng.deviceextensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.kevincheng.deviceextensions.internal.DeviceKeeper
import com.stericson.RootTools.RootTools
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.concurrent.TimeoutException

class Device(private val applicationContext: Context) {
    companion object {
        private lateinit var shared: Device

        internal fun install(application: Application) {
            when (::shared.isInitialized) {
                false -> shared = Device(application.applicationContext)
            }
        }

        private val context: Context get() = shared.applicationContext

        @JvmStatic
        val androidId: String
            @SuppressLint("HardwareIds")
            get() {
                return when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                    }
                    else -> when (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED) {
                        true -> {
                            val tm =
                                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                                    tm.imei
                                }
                                else -> {
                                    tm.deviceId
                                }
                            }
                        }
                        false -> null
                    }
                } ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }

        @JvmStatic
        val UUID: String
            get() {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val key = shared
                    .applicationContext
                    .getString(R.string.pref_deviceextensions_uuid_key)
                var uuid = sharedPreferences.getString(key, null)
                if (uuid == null) {
                    uuid = java.util.UUID.randomUUID().toString()
                    sharedPreferences.edit(commit = true) {
                        putString(key, uuid)
                    }
                }
                return uuid
            }

        @JvmStatic
        val displayMetrics: DisplayMetrics
            get() = DisplayMetrics().also {
                (shared.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay
                    .getRealMetrics(it)
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
        val isPortrait: Boolean
            get() = shared
                .applicationContext
                .resources
                .configuration
                .orientation == Configuration.ORIENTATION_PORTRAIT

        @JvmStatic
        val isLandscape: Boolean
            get() = shared
                .applicationContext
                .resources
                .configuration
                .orientation == Configuration.ORIENTATION_LANDSCAPE

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
        val isRooted: Boolean
            get() = RootTools.isRootAvailable()

        @JvmStatic
        val isRootAccessGiven: Boolean
            get() = RootTools.isAccessGiven()

        @JvmStatic
        fun isRootAccessGiven(timeout: Int, retries: Int): Boolean {
            return try {
                RootTools.isAccessGiven(timeout, retries)
            } catch (e: TimeoutException) {
                false
            }
        }

        @JvmStatic
        fun restart(): Boolean {
            if (isRooted) RootTools.restartAndroid()
            return false
        }

        @JvmStatic
        fun scheduleRestart(time: LocalTime) {
            if (!isRooted) return
            val now = LocalDateTime.now()
            var target = time.atDate(now.toLocalDate())
            if (target.isBefore(now)) target = target.plusDays(1)
            scheduleRestart(target)
        }

        @JvmStatic
        fun scheduleRestart(dateTime: LocalDateTime) {
            if (!isRooted) return
            DeviceKeeper.scheduleRestart(context, dateTime)
        }

        @JvmStatic
        fun cancelScheduledRestart() {
            DeviceKeeper.cancelScheduledRestart(context)
        }
    }
}