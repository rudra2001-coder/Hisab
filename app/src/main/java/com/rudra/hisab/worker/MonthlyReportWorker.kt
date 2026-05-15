package com.rudra.hisab.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rudra.hisab.MainActivity
import androidx.room.Room
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.preferences.AppPreferences
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MonthlyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            showNotification()
        } catch (_: Exception) {
            return Result.retry()
        }
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "hisab_monthly_report"
        val channel = NotificationChannel(
            channelId,
            "Monthly Report",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "মাসিক রিপোর্ট রিমাইন্ডার" }
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = android.net.Uri.parse("hisab://analytics")
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("মাসিক রিপোর্ট তৈরি করুন")
            .setContentText("এই মাসের হিসাব দেখুন এবং রিপোর্ট জেনারেট করুন।")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            nm.notify(1003, notification)
        }
    }

    companion object {
        private const val WORK_NAME = "monthly_report_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MonthlyReportWorker>(
                30, TimeUnit.DAYS
            )
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun calculateInitialDelay(): Long {
            val now = LocalTime.now()
            val targetTime = LocalTime.of(10, 0)
            val targetInMillis = targetTime.toSecondOfDay() * 1000L
            val nowInMillis = now.toSecondOfDay() * 1000L

            return if (nowInMillis < targetInMillis) {
                targetInMillis - nowInMillis
            } else {
                (24 * 60 * 60 * 1000L) - (nowInMillis - targetInMillis)
            }
        }
    }
}
