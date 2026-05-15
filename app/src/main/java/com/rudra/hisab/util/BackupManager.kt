package com.rudra.hisab.util

import android.content.Context
import com.rudra.hisab.data.local.HisabDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun performBackup(): Result<File> {
        return try {
            val dbFile = context.getDatabasePath(HisabDatabase.DATABASE_NAME)
            if (!dbFile.exists()) return Result.failure(Exception("Database file not found"))

            // Also handle WAL and SHM files for full consistency
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "hisab_backup_$timestamp.db")
            
            copyFile(dbFile, backupFile)
            if (walFile.exists()) copyFile(walFile, File(backupFile.path + "-wal"))
            if (shmFile.exists()) copyFile(shmFile, File(backupFile.path + "-shm"))

            // Clean up old backups (keep last 7)
            val backups = backupDir.listFiles { f -> f.name.endsWith(".db") }
                ?.sortedByDescending { it.lastModified() }
            
            if (backups != null && backups.size > 7) {
                backups.drop(7).forEach { 
                    it.delete()
                    File(it.path + "-wal").delete()
                    File(it.path + "-shm").delete()
                }
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyFile(source: File, destination: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                input.channel.transferTo(0, input.channel.size(), output.channel)
            }
        }
    }
}
