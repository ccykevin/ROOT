package com.kevincheng.logger

import android.content.Context

internal class LoggerBuildConfig {
    companion object {
        fun isLoggable(context: Context): Boolean {
            return try {
                val clazz = Class.forName(context.packageName + ".BuildConfig")
                val field = clazz.getField("loggable")
                field.get(null)
            } catch (e: NoSuchFieldException) {
                null
            } catch (e: IllegalAccessException) {
                null
            } as? Boolean ?: true
        }

        fun createLogFile(context: Context): Boolean {
            return try {
                val clazz = Class.forName(context.packageName + ".BuildConfig")
                val field = clazz.getField("logFile")
                field.get(null)
            } catch (e: NoSuchFieldException) {
                null
            } catch (e: IllegalAccessException) {
                null
            } as? Boolean ?: true
        }
    }
}