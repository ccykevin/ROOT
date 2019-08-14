package com.kevincheng.logger

import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.LogStrategy
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal class CsvFormatStrategy(private val logStrategy: LogStrategy) : FormatStrategy {
    companion object {
        val NEW_LINE = System.getProperty("line.separator") ?: "\n"
        const val NEW_LINE_REPLACEMENT = "<br>"
        const val SEPARATOR = ","

        const val LOGGER_VERBOSE_STRING = "VERBOSE"
        const val LOGGER_DEBUG_STRING = "DEBUG"
        const val LOGGER_INFO_STRING = "INFO"
        const val LOGGER_WARN_STRING = "WARN"
        const val LOGGER_ERROR_STRING = "ERROR"
        const val LOGGER_ASSERT_STRING = "ASSERT"
    }

    private val dateFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.getDefault())

    override fun log(priority: Int, onceOnlyTag: String?, message: String) {
        var logMessage = message

        val date = Calendar.getInstance().time

        val builder = StringBuilder()

        // machine-readable date/time
        builder.append(date.time.toString())

        // human-readable date/time
        builder.append(SEPARATOR)
        builder.append(dateFormatter.format(date))

        // level
        builder.append(SEPARATOR)
        builder.append(logLevel(priority))

        val currentThread = Thread.currentThread()
        val stackTrace = currentThread.stackTrace
        var index = -1
        for (i in 0 until stackTrace.size) {
            val element = stackTrace[i]
            if (element.className.equals("com.orhanobut.logger.Logger")) {
                index = i + 1
                break
            }
        }
        val caller = currentThread.stackTrace[index]

        // thread
        builder.append(SEPARATOR)
        builder.append("[${currentThread.name}:${currentThread.id}]")

        // file detail
        builder.append(SEPARATOR)
        builder.append("[${caller.fileName}:${caller.lineNumber}]")

        // caller detail
        builder.append(SEPARATOR)
        builder.append("[${caller.className}.${caller.methodName}]")

        // tag
        onceOnlyTag?.apply {
            builder.append(SEPARATOR)
            builder.append(this)
        }

        // message
        if (logMessage.contains(NEW_LINE)) {
            // a new line would break the CSV format, so we replace it here
            logMessage = logMessage.replace(NEW_LINE.toRegex(), NEW_LINE_REPLACEMENT)
        }
        builder.append(SEPARATOR)
        builder.append(logMessage)

        // new line
        builder.append(NEW_LINE)

        logStrategy.log(priority, onceOnlyTag, builder.toString())
    }

    private fun logLevel(value: Int): String {
        return when (value) {
            Logger.VERBOSE -> LOGGER_VERBOSE_STRING
            Logger.DEBUG -> LOGGER_DEBUG_STRING
            Logger.INFO -> LOGGER_INFO_STRING
            Logger.WARN -> LOGGER_WARN_STRING
            Logger.ERROR -> LOGGER_ERROR_STRING
            Logger.ASSERT -> LOGGER_ASSERT_STRING
            else -> "UNKNOWN"
        }
    }
}
