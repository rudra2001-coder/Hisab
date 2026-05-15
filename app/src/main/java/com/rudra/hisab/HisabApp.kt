package com.rudra.hisab

import android.app.Application
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.worker.BackupWorker
import com.rudra.hisab.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HisabApp : Application() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        ReminderWorker.schedule(this)
        
        CoroutineScope(Dispatchers.IO).launch {
            val settings = appPreferences.settings.first()
            if (settings.autoBackupEnabled) {
                BackupWorker.schedule(this@HisabApp, settings.backupFrequency)
            }
        }
    }
}
