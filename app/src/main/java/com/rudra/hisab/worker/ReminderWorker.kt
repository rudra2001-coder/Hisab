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

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val db = Room.databaseBuilder(
                applicationContext,
                HisabDatabase::class.java,
                HisabDatabase.DATABASE_NAME
            ).build()

            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val saleCount = db.transactionDao().getTodaySaleCount(startOfDay, endOfDay)

            if (saleCount == 0) {
                showSaleReminderNotification()
            }

            val lowStockList = db.productDao().getLowStockProductsOnce()
            if (lowStockList.isNotEmpty()) {
                showLowStockNotification(lowStockList.size)
            }

            db.close()
        } catch (_: Exception) {
            return Result.retry()
        }

        return Result.success()
    }

    private fun showSaleReminderNotification() {
        val channelId = "hisab_reminder"
        createChannel(channelId, "Hisab Reminder", "বিক্রয় রিমাইন্ডার", NotificationManager.IMPORTANCE_HIGH)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
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

        if (canNotify()) {
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1001, notification)
        }
    }

    private fun showLowStockNotification(count: Int) {
        val channelId = "hisab_low_stock"
        createChannel(channelId, "Low Stock", "কম স্টক সতর্কতা", NotificationManager.IMPORTANCE_DEFAULT)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = android.net.Uri.parse("hisab://inventory")
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$count টি পণ্যের স্টক কম!")
            .setContentText("স্টক আপডেট করতে ট্যাপ করুন")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$count টি পণ্যের স্টক কমে গেছে। দয়া করে স্টক ইন করুন।"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (canNotify()) {
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1002, notification)
        }
    }

    private fun createChannel(id: String, name: String, desc: String, importance: Int) {
        val channel = NotificationChannel(id, name, importance).apply { description = desc }
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun canNotify(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
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

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
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
