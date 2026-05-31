package com.example.data

import androidx.room.Entity

@Entity(tableName = "prayer_records", primaryKeys = ["dateStr", "prayerName"])
data class PrayerRecord(
    val dateStr: String,       // format: YYYY-MM-DD
    val prayerName: String,    // Fajr, Dhuhr, Asr, Maghrib, Isha
    val isDone: Boolean
)
