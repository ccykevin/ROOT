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

    private val dateFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)

    override fun log(priority: Int, onceOnlyTag: String?, message: String) {
        val logMessage = message.takeIf { it.contains(NEW_LINE) }?.replace(NEW_LINE.toRegex(), NEW_LINE_REPLACEMENT) ?: message

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

        // tag
        onceOnlyTag?.apply {
            builder.append(SEPARATOR)
            builder.append(this)
        }

        // message
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
