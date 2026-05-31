package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_records WHERE dateStr = :dateStr")
    fun getPrayersForDate(dateStr: String): Flow<List<PrayerRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(record: PrayerRecord)

    @Query("DELETE FROM prayer_records WHERE dateStr = :dateStr")
    suspend fun clearPrayersForDate(dateStr: String)
}

@Dao
interface FastingDao {
    @Query("SELECT * FROM fasting_records ORDER BY dateStr DESC")
    fun getAllFastingRecords(): Flow<List<FastingRecord>>

    @Query("SELECT * FROM fasting_records WHERE dateStr = :dateStr LIMIT 1")
    fun getFastingRecordForDate(dateStr: String): Flow<FastingRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFastingRecord(record: FastingRecord)

    @Query("DELETE FROM fasting_records WHERE dateStr = :dateStr")
    suspend fun deleteFastingRecordForDate(dateStr: String)
}
