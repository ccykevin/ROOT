package com.kevincheng.rootexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kevincheng.extensions.isGrantedRequiredPermissions
import com.kevincheng.extensions.requiredPermissions
import com.orhanobut.logger.Logger

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.d("Required Permissions: ${requiredPermissions.map { it }}")
        Logger.d("Is Granted Required Permissions? $isGrantedRequiredPermissions")
    }
}
