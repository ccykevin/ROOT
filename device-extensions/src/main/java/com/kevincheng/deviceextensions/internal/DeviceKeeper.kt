package com.kevincheng.deviceextensions.internal

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kevincheng.deviceextensions.Device
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.concurrent.TimeUnit

internal class DeviceKeeper(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        if (tags.contains(restartTAG)) Device.restart()
        return Result.success()
    }

    companion object {
        private const val restartTAG = "restart"

        fun scheduleRestart(context: Context, time: LocalDateTime) {
            cancelScheduledRestart(context)
            val now = ZonedDateTime.now()
            var target = time.atZone(ZoneId.systemDefault())
            if (target.isBefore(now)) target = target.plusDays(1)
            val delay = target.toInstant().toEpochMilli() - now.toInstant().toEpochMilli()
            val request = OneTimeWorkRequestBuilder<DeviceKeeper>()
                .addTag(restartTAG)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun cancelScheduledRestart(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(restartTAG)
        }
    }
}