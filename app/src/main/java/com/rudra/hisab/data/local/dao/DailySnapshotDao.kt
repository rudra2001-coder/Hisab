package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySnapshotDao {

    @Query("SELECT * FROM daily_snapshots ORDER BY date DESC")
    fun getAllSnapshots(): Flow<List<DailySnapshotEntity>>

    @Query("SELECT * FROM daily_snapshots WHERE id = :id")
    suspend fun getSnapshotById(id: Long): DailySnapshotEntity?

    @Query("SELECT * FROM daily_snapshots WHERE date = :date LIMIT 1")
    suspend fun getSnapshotByDate(date: Long): DailySnapshotEntity?

    @Query("SELECT * FROM daily_snapshots WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getSnapshotsByRange(startDate: Long, endDate: Long): Flow<List<DailySnapshotEntity>>

    @Query("SELECT COALESCE(SUM(netProfit), 0) FROM daily_snapshots WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalProfitInRange(startDate: Long, endDate: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: DailySnapshotEntity): Long

    @Query("DELETE FROM daily_snapshots WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM daily_snapshots WHERE date = :date)")
    suspend fun hasSnapshotForDate(date: Long): Boolean
}
