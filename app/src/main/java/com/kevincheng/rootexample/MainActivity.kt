package com.kevincheng.rootexample

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.kevincheng.appextensions.App
import com.kevincheng.deviceextensions.Device
import com.kevincheng.extensions.isGrantedRequiredPermissions
import com.kevincheng.extensions.requiredPermissions
import com.kevincheng.widget.IRadioButton
import com.kevincheng.widget.IRadioGroup
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(App.loadConfiguration(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.d("Required Permissions: ${requiredPermissions.map { it }}")
        Logger.d("Is Granted Required Permissions? $isGrantedRequiredPermissions")
        Logger.d(Device.androidId)
        Logger.d(Device.isRooted)
        Logger.d("Device.isPortrait@${Device.isPortrait} Device.isLandscape@${Device.isLandscape} Device.screenWidth@${Device.screenWidth} Device.screenHeight@${Device.screenHeight} Device.densityDpi@${Device.densityDpi} Device.density@${Device.density} Device.scaledDensity@${Device.scaledDensity} Device.smallestWidth@${Device.smallestWidth}")
        Logger.d("App.screenWidth@${App.screenWidth} App.screenHeight@${App.screenHeight} App.densityDpi@${App.densityDpi} App.density@${App.density} App.scaledDensity@${App.scaledDensity} App.smallestWidth@${App.smallestWidth}")

        rg_language.listener = object : IRadioGroup.Listener {
            override fun onCheckedChanged(
                radioGroup: IRadioGroup,
                previousCheckedButton: IRadioButton?,
                checkedButton: IRadioButton?
            ) {
                val locale = (requireNotNull(checkedButton).value as String).split("_").let {
                    when (it.size > 1) {
                        true -> Locale(it[0], it[1])
                        false -> Locale(it[0])
                    }
                }
                val context = App.setLocale(locale)
                textView_hello.text = context.getString(R.string.hello)
            }
        }

        when (Locale.getDefault().toString()) {
            getString(R.string.lang_en) -> rg_language.checkNoEvent(R.id.rb_en)
            getString(R.string.lang_hk) -> rg_language.checkNoEvent(R.id.rb_hk)
            getString(R.string.lang_es) -> rg_language.checkNoEvent(R.id.rb_es)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isGrantedRequiredPermissions) ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            permissions.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES) && !App.canRequestPackageInstalls -> App.grantRequestPackageInstallsPermission()
            permissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW) && !App.canDrawOverlays -> App.grantDrawOverlaysPermission()
        }
    }
}
