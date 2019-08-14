package com.kevincheng.logger

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

internal class LoggerInstaller : ContentProvider() {

    override fun onCreate(): Boolean {
        val appContext = context!!.applicationContext as Application

        when (LoggerBuildConfig.isLoggable(appContext)) {
            true -> {
                Logger.addLogAdapter(AndroidLogAdapter())
                when (LoggerBuildConfig.createLogFile(appContext)) {
                    true -> Logger.addLogAdapter(CsvFileLogAdapter.create(appContext))
                }
            }
        }

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}
