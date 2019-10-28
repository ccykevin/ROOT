package com.kevincheng.logger

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import java.io.File
import java.util.Calendar

internal class AppLogger {
    companion object {
        private var installed: Boolean = false

        internal fun install(application: Application) {
            if (!installed) {
                val context = application.applicationContext
                when (context.resources.getBoolean(R.bool.logger_loggable)) {
                    true -> {
                        Logger.addLogAdapter(AndroidLogAdapter())
                        when (context.resources.getBoolean(R.bool.logger_logFile)) {
                            true -> Logger.addLogAdapter(CsvFileLogAdapter.create(context))
                        }
                    }
                }
                /** log files are only kept for 30 days */
                try {
                    val root = DiskLogStrategy.rootDirectory(context)
                    val last30DaysDirectories = Array(30) {
                        Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -it)
                        }.let { File(root, DiskLogStrategy.dateDirectoryFormatter.format(it.time)) }
                    }
                    root.listFiles().filter { !last30DaysDirectories.contains(it) }.forEach { it.deleteRecursively() }
                } catch (ignore: Exception) {
                }
                installed = true
            }
        }
    }
}