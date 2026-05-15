package com.rudra.hisab.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.util.BackupManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BackupWorkerEntryPoint {
        fun backupManager(): BackupManager
        fun appPreferences(): AppPreferences
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            BackupWorkerEntryPoint::class.java
        )
        val backupManager = entryPoint.backupManager()
        val appPreferences = entryPoint.appPreferences()

        val settings = appPreferences.settings.first()
        if (!settings.autoBackupEnabled) return Result.success()

        val result = backupManager.performBackup()
        return if (result.isSuccess) {
            appPreferences.setLastBackupTime(System.currentTimeMillis())
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "auto_backup_work"

        fun schedule(context: Context, frequency: String) {
            val interval = when (frequency) {
                "daily" -> 24L
                "weekly" -> 7 * 24L
                else -> 24L
            }
            
            val request = PeriodicWorkRequestBuilder<BackupWorker>(
                interval, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
