package com.kevincheng.logger

import android.content.Context
import android.os.HandlerThread
import android.os.Process
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.FormatStrategy

internal class CsvFileLogAdapter(formatStrategy: FormatStrategy) : DiskLogAdapter(formatStrategy) {
    companion object {
        fun create(context: Context): CsvFileLogAdapter {
            val writeLogHandlerThread =
                HandlerThread("${context.packageName}.logger", Process.THREAD_PRIORITY_BACKGROUND)
            writeLogHandlerThread.start()
            val diskLogStrategy = DiskLogStrategy(
                DiskLogStrategy.WriteLogHandler(
                    context,
                    writeLogHandlerThread.looper
                )
            )
            return CsvFileLogAdapter(CsvFormatStrategy(diskLogStrategy))
        }
    }
}
