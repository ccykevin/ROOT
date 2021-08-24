package com.kevincheng.rootexample

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.kevincheng.appextensions.App
import com.kevincheng.deviceextensions.Device
import com.kevincheng.extensions.isGrantedRequiredPermissions
import com.kevincheng.extensions.requiredPermissions
import com.orhanobut.logger.Logger

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.d("Required Permissions: ${requiredPermissions.map { it }}")
        Logger.d("Is Granted Required Permissions? $isGrantedRequiredPermissions")
        Logger.d(Device.androidId)
        Logger.d(Device.isRooted)
        Logger.d("Device.isPortrait@${Device.isPortrait} Device.isLandscape@${Device.isLandscape} Device.screenWidth@${Device.screenWidth} Device.screenHeight@${Device.screenHeight} Device.densityDpi@${Device.densityDpi} Device.density@${Device.density} Device.scaledDensity@${Device.scaledDensity} Device.smallestWidth@${Device.smallestWidth}")
        Logger.d("App.screenWidth@${App.screenWidth} App.screenHeight@${App.screenHeight} App.densityDpi@${App.densityDpi} App.density@${App.density} App.scaledDensity@${App.scaledDensity} App.smallestWidth@${App.smallestWidth}")
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
