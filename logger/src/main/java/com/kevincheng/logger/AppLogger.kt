package com.kevincheng.logger

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

internal class AppLogger {
    companion object {
        private var installed: Boolean = false

        internal fun install(application: Application) {
            if (!installed) {
                when (LoggerBuildConfig.isLoggable(application)) {
                    true -> {
                        Logger.addLogAdapter(AndroidLogAdapter())
                        when (LoggerBuildConfig.createLogFile(application)) {
                            true -> Logger.addLogAdapter(CsvFileLogAdapter.create(application))
                        }
                    }
                }
                installed = true
            }
        }
    }
}