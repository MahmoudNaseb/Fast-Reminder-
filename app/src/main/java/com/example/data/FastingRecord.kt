package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_records")
data class FastingRecord(
    @PrimaryKey val dateStr: String,  // format: YYYY-MM-DD
    val hijriDateFormatted: String,   // Arabic formatted Hijri Date
    val isFasted: Boolean,
    val fastType: String              // RAMADAN_OBLIGATORY, MONDAY, THURSDAY, WHITE_DAYS, OTHER
)
