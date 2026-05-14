package com.rudra.hisab

import android.app.Application
import com.rudra.hisab.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HisabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderWorker.schedule(this)
    }
}
