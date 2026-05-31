package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val prayerDao: PrayerDao,
    private val fastingDao: FastingDao
) {
    // Prayer Records operations
    fun getPrayersForDate(dateStr: String): Flow<List<PrayerRecord>> = 
        prayerDao.getPrayersForDate(dateStr)

    suspend fun insertPrayer(record: PrayerRecord) {
        prayerDao.insertPrayer(record)
    }

    suspend fun clearPrayersForDate(dateStr: String) {
        prayerDao.clearPrayersForDate(dateStr)
    }

    // Fasting Records operations
    val allFastingRecords: Flow<List<FastingRecord>> = 
        fastingDao.getAllFastingRecords()

    fun getFastingRecordForDate(dateStr: String): Flow<FastingRecord?> = 
        fastingDao.getFastingRecordForDate(dateStr)

    suspend fun insertFastingRecord(record: FastingRecord) {
        fastingDao.insertFastingRecord(record)
    }

    suspend fun deleteFastingRecordForDate(dateStr: String) {
        fastingDao.deleteFastingRecordForDate(dateStr)
    }
}
