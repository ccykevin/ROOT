package com.kevincheng.logger

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.orhanobut.logger.LogStrategy
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Abstract class that takes care of background threading the file log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 *
 * Writes all logs to the disk with CSV format.
 */
internal class DiskLogStrategy(private val handler: Handler) : LogStrategy {

    override fun log(priority: Int, tag: String?, message: String) {
        // do nothing on the calling thread, simply pass the tag/msg to the background thread
        handler.sendMessage(handler.obtainMessage(priority, message))
    }

    internal class WriteLogHandler(context: Context, looper: Looper) : Handler(looper) {

        private val CSV_FILE_NAME_FORMAT = "%s_%s_%d.csv"
        private val MAXIMUM_LOG_FILE_SIZE = 1 * 1024 * 1024 // 1MB
        private val packageName = context.packageName
        private val LOG_FILES_DIRECTORY = File(Environment.getExternalStorageDirectory(), "logger/$packageName")
        private val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        private val dailyLogFileCounter: HashMap<String, Int> = hashMapOf()

        override fun handleMessage(msg: Message?) {
            val log = msg?.obj as? String ?: return

            val logFile = getLogFile()
            var fileWriter: FileWriter? = null

            try {
                fileWriter = FileWriter(logFile, true)
                writeLog(fileWriter, log)
            } catch (e: IOException) {
                Log.e("Logger", "IOException occurred")
            } finally {
                fileWriter?.flush()
                fileWriter?.close()
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        @Throws(IOException::class)
        private fun writeLog(fileWriter: FileWriter, content: String) {
            fileWriter.append(content)
        }

        private fun getLogFile(): File {
            val date = dateFormatter.format(Calendar.getInstance().time)

            if (!LOG_FILES_DIRECTORY.exists()) LOG_FILES_DIRECTORY.mkdirs()

            var fileCounter = dailyLogFileCounter[date] ?: 0
            while (true) {
                val logFileName = String.format(CSV_FILE_NAME_FORMAT, packageName, date, fileCounter)
                val logFile = File(LOG_FILES_DIRECTORY, logFileName)
                when {
                    !logFile.exists() || (logFile.exists() && logFile.length() < MAXIMUM_LOG_FILE_SIZE) -> {
                        dailyLogFileCounter[date] = fileCounter
                        return logFile
                    }
                    else -> fileCounter++
                }
            }
        }
    }
}
