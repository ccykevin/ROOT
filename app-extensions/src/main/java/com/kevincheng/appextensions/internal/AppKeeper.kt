package com.kevincheng.appextensions.internal

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kevincheng.appextensions.App
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.concurrent.TimeUnit

internal class AppKeeper(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        if (tags.contains(relaunchAppTAG)) App.relaunch()

        if (tags.contains(restartAppTAG)) App.restart()

        if (tags.contains(patrolTAG) && App.currentActivity == null) {
            App.relaunch()
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        private const val relaunchAppTAG = "relaunchApp"
        private const val restartAppTAG = "restartApp"
        private const val patrolTAG = "patrol"

        fun scheduleRelaunch(context: Context, dateTime: LocalDateTime) {
            cancelScheduledRelaunch(context)
            val now = ZonedDateTime.now().toInstant().toEpochMilli()
            val target = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val delay = (target - now).takeIf { it > 0 } ?: 0
            val request = OneTimeWorkRequestBuilder<AppKeeper>()
                .addTag(relaunchAppTAG)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun cancelScheduledRelaunch(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(relaunchAppTAG)
        }

        fun scheduleRestart(context: Context, dateTime: LocalDateTime) {
            cancelScheduledRestart(context)
            val now = ZonedDateTime.now().toInstant().toEpochMilli()
            val target = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val delay = (target - now).takeIf { it > 0 } ?: 0
            val request = OneTimeWorkRequestBuilder<AppKeeper>()
                .addTag(restartAppTAG)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun cancelScheduledRestart(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(restartAppTAG)
        }

        fun schedulePatrol(context: Context) {
            cancelScheduledPatrol(context)
            val now = ZonedDateTime.now()
            val nowMillis = now.toInstant().toEpochMilli()
            val targetMillis = now.plusMinutes(1).toInstant().toEpochMilli()
            val delay = targetMillis - nowMillis
            val request = OneTimeWorkRequestBuilder<AppKeeper>()
                .addTag(patrolTAG)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun cancelScheduledPatrol(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(patrolTAG)
        }
    }
}