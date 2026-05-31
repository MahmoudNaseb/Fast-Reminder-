package com.example.util

import kotlin.math.*

enum class CalculationMethod(val displayNameAr: String, val fajrAngle: Double, val ishaAngle: Double, val isUmmAlQura: Boolean = false) {
    UMM_AL_QURA("تقويم أم القرى (مكة المكرمة)", 18.5, 0.0, true),
    MWL("رابطة العالم الإسلامي", 18.0, 17.0),
    EGYPT("الهيئة المصرية العامة للمساحة", 19.5, 17.5),
    ISNA("الجمعية الإسلامية لأمريكا الشمالية (ISNA)", 15.0, 15.0),
    KARACHI("جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0)
}

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

object PrayerTimesCalculator {

    // Converts Gregorian Date to Julian Date
    fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    // Main calculation of prayer times
    fun calculatePrayerTimes(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double,
        method: CalculationMethod,
        isRamadan: Boolean = false
    ): PrayerTimes {
        val jd = getJulianDate(year, month, day)
        val d = jd - 2451545.0
        
        // Sun mean anomaly (g) and mean longitude (q)
        val g = 357.529 + 0.98560028 * d
        val q = 280.459 + 0.98564736 * d
        
        val gRad = Math.toRadians(g)
        val L = q + 1.915 * sin(gRad) + 0.020 * sin(2 * gRad)
        val LRad = Math.toRadians(L)
        
        // Ecliptic obliquity
        val obliq = 23.439 - 0.00000036 * d
        val obliqRad = Math.toRadians(obliq)
        
        // Solar Declination (delta)
        val deltaRad = asin(sin(obliqRad) * sin(LRad))
        val delta = Math.toDegrees(deltaRad)
        
        // Solar Right Ascension (RA)
        var raRad = atan2(cos(obliqRad) * sin(LRad), cos(LRad))
        var ra = Math.toDegrees(raRad)
        if (ra < 0) ra += 360.0
        
        // Normalize Mean Longitude q
        var qNorm = q
        while (qNorm < 0) qNorm += 360.0
        while (qNorm >= 360.0) qNorm -= 360.0
        
        // Equation of Time (EqT) in hours
        var diff = qNorm - ra
        while (diff < -185.0) diff += 360.0
        while (diff > 185.0) diff -= 360.0
        val eqT = diff / 15.0
        
        // Transit time (Midday / Solar Noon)
        val noon = 12.0 + timezoneOffsetHours - (longitude / 15.0) - eqT
        
        val latRad = Math.toRadians(latitude)
        
        // Computes hour angle for a specific altitude below or above the horizon
        fun hourAngle(altitudeAngleBelowHorizon: Double): Double {
            val altRad = Math.toRadians(-altitudeAngleBelowHorizon)
            val denom = cos(latRad) * cos(deltaRad)
            val num = sin(altRad) - sin(latRad) * sin(deltaRad)
            val cosH = num / denom
            if (cosH > 1.0) return 0.0  // Always below horizon (polar night scenario)
            if (cosH < -1.0) return 12.0 // Always above horizon (polar day scenario)
            return Math.toDegrees(acos(cosH)) / 15.0
        }
        
        // 1. Fajr
        val fajrHA = hourAngle(method.fajrAngle)
        val fajrTime = noon - fajrHA
        
        // 2. Sunrise
        val sunriseHA = hourAngle(0.833) // Standard refraction constant: 50 arcminutes = 0.833 degrees
        val sunriseTime = noon - sunriseHA
        
        // 3. Dhuhr - Transit time plus a short safety addition (+4 minutes) when the sun leaves meridian
        val dhuhrTime = noon + (4.0 / 60.0)
        
        // 4. Asr - Shafi'i method standard (shadow length multiplication factor = 1.0)
        val asrAltitudeRad = atan(1.0 / (1.0 + tan(abs(latRad - deltaRad))))
        val numAsr = sin(asrAltitudeRad) - sin(latRad) * sin(deltaRad)
        val denomAsr = cos(latRad) * cos(deltaRad)
        val cosHAsr = numAsr / denomAsr
        val asrHA = if (cosHAsr in -1.0..1.0) Math.toDegrees(acos(cosHAsr)) / 15.0 else 0.0
        val asrTime = noon + asrHA
        
        // 5. Maghrib - sunset angle (0.833) plus 2 minutes traditional buffer for prayer call
        val maghribTime = noon + sunriseHA + (2.0 / 60.0)
        
        // 6. Isha
        val ishaTime = if (method.isUmmAlQura) {
            // Umm Al-Qura standard rules: Maghrib + 90 min (120 min during Ramadan)
            val offsetHours = if (isRamadan) 120.0 / 60.0 else 90.0 / 60.0
            maghribTime + offsetHours
        } else {
            val ishaHA = hourAngle(method.ishaAngle)
            noon + ishaHA
        }
        
        // Time format function (HH:MM in 24h)
        fun format(hours: Double): String {
            if (hours.isNaN() || hours < 0 || hours > 240) return "--:--"
            var totalMinutes = Math.round(hours * 60.0).toInt()
            totalMinutes %= 1440
            if (totalMinutes < 0) totalMinutes += 1440
            val h = totalMinutes / 60
            val m = totalMinutes % 60
            return String.format("%02d:%02d", h, m)
        }
        
        return PrayerTimes(
            fajr = format(fajrTime),
            sunrise = format(sunriseTime),
            dhuhr = format(dhuhrTime),
            asr = format(asrTime),
            maghrib = format(maghribTime),
            isha = format(ishaTime)
        )
    }
}
