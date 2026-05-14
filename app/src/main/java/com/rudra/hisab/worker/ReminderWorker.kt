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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rudra.hisab.MainActivity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("hisab_preferences", Context.MODE_PRIVATE)
        val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
        if (!hasCompletedOnboarding) return Result.success()

        val todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val lastSaleDate = prefs.getLong("last_sale_date", 0L)

        if (lastSaleDate < todayStart) {
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        val channelId = "hisab_reminder"

        val channel = NotificationChannel(
            channelId,
            "Hisab Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "বিক্রয় রিমাইন্ডার"
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("আজকের বিক্রয় লিখুন")
            .setContentText("আজ কি কোনো বিক্রয় হয়েছে? হিসাব বন্ধ করার আগে আজকের সব বিক্রয় লিখুন।")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(1001, notification)
        }
    }

    companion object {
        private const val WORK_NAME = "daily_sale_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                24, TimeUnit.HOURS
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
            val targetTime = LocalTime.of(20, 0)
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
