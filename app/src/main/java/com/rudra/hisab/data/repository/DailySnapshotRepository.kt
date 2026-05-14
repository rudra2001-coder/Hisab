package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.DailySnapshotDao
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailySnapshotRepository @Inject constructor(
    private val dailySnapshotDao: DailySnapshotDao
) {
    fun getAllSnapshots(): Flow<List<DailySnapshotEntity>> = dailySnapshotDao.getAllSnapshots()

    suspend fun getSnapshotById(id: Long): DailySnapshotEntity? =
        dailySnapshotDao.getSnapshotById(id)

    suspend fun getSnapshotByDate(date: Long): DailySnapshotEntity? =
        dailySnapshotDao.getSnapshotByDate(date)

    fun getSnapshotsByRange(startDate: Long, endDate: Long): Flow<List<DailySnapshotEntity>> =
        dailySnapshotDao.getSnapshotsByRange(startDate, endDate)

    suspend fun getTotalProfitInRange(startDate: Long, endDate: Long): Double =
        dailySnapshotDao.getTotalProfitInRange(startDate, endDate)

    suspend fun insert(snapshot: DailySnapshotEntity): Long =
        dailySnapshotDao.insert(snapshot)

    suspend fun hasSnapshotForDate(date: Long): Boolean =
        dailySnapshotDao.hasSnapshotForDate(date)
}
