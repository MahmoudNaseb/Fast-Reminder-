package com.example.util

import java.util.Calendar

data class HijriDate(
    val year: Int,
    val month: Int,       // 1 to 12
    val day: Int,         // 1 to 30
    val monthNameAr: String,
    val formattedAr: String
)

enum class FastingReminderType {
    NONE,
    MONDAY,
    THURSDAY,
    WHITE_DAY_13,
    WHITE_DAY_14,
    WHITE_DAY_15,
    RAMADAN_DAILY,
    TASUA,
    ASHURA,
    ARAFAH,
    DHUL_HIJJAH_TEN,
    SHAWWAL_SIX,
    EID_FORBIDDEN
}

data class FastingReminderState(
    val type: FastingReminderType,
    val titleAr: String,
    val descriptionAr: String,
    val hadithAr: String,
    val isRamadan: Boolean
)

object HijriCalendarHelper {

    val MONTH_NAMES_AR = arrayOf(
        "محرّم",
        "صَفَر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    private const val ISLAMIC_EPOCH = 1948439.5

    // Dynamic converts Gregorian Calendar instance to Hijri Date representation
    fun convertGregorianToHijri(gregorianCalendar: Calendar, offsetDays: Int = 0): HijriDate {
        val year = gregorianCalendar.get(Calendar.YEAR)
        val month = gregorianCalendar.get(Calendar.MONTH) + 1 // 1-12
        val day = gregorianCalendar.get(Calendar.DAY_OF_MONTH)

        // Calculate Gregorian Julian Date
        var jd = PrayerTimesCalculator.getJulianDate(year, month, day)
        
        // Apply custom observation offset adjustment
        jd += offsetDays

        val daysSinceEpoch = Math.floor(jd - ISLAMIC_EPOCH).toInt()
        
        // Islamic cycle calculations: 30 year standard cycle has 10631 days
        val cycle = daysSinceEpoch / 10631
        var rem = daysSinceEpoch % 10631
        if (rem < 0) {
            rem += 10631
        }
        
        var yearInCycle = 1
        var accumulatedDays = 0
        val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        
        for (y in 1..30) {
            val isLeap = leapYears.contains(y)
            val daysInYear = if (isLeap) 355 else 354
            if (rem < accumulatedDays + daysInYear) {
                yearInCycle = y
                break
            }
            accumulatedDays += daysInYear
        }
        
        val hijriYear = cycle * 30 + yearInCycle
        var remainingDays = rem - accumulatedDays
        
        var hijriMonth = 1
        for (m in 1..12) {
            val daysInMonth = if (m % 2 != 0) 30 else {
                if (m == 12 && leapYears.contains(yearInCycle)) 30 else 29
            }
            if (remainingDays < daysInMonth) {
                hijriMonth = m
                break
            }
            remainingDays -= daysInMonth
        }
        
        val hijriDay = remainingDays + 1
        val monthName = MONTH_NAMES_AR[hijriMonth - 1]
        
        // Elegant localized Arabic formatted representation
        val arYear = toArabicDigits(hijriYear)
        val arDay = toArabicDigits(hijriDay)
        val formattedAr = "$arDay $monthName $arYear هـ"
        
        return HijriDate(
            year = hijriYear,
            month = hijriMonth,
            day = hijriDay,
            monthNameAr = monthName,
            formattedAr = formattedAr
        )
    }

    // High quality string Arabic conversion
    fun toArabicDigits(num: Int): String {
        val numStr = num.toString()
        val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val builder = StringBuilder()
        for (ch in numStr) {
            if (ch in '0'..'9') {
                builder.append(arabicChars[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    /**
     * Calculates the Fasting Reminder status which is shown today.
     * Fasting reminders are triggered the DAY BEFORE (i.e., we look at tomorrow's Hijri date).
     * If tomorrow has special yearly recommendations, we show those first.
     */
    fun determineFastingReminder(gregorianCalendar: Calendar, offsetDays: Int = 0): FastingReminderState {
        val todayHijri = convertGregorianToHijri(gregorianCalendar, offsetDays)
        
        // 1. Is it Ramadan (Month 9)? (Ramadan takes absolute precedence as whole-month obligatory fasting)
        if (todayHijri.month == 9) {
            return FastingReminderState(
                type = FastingReminderType.RAMADAN_DAILY,
                titleAr = "شَهْرُ رَمَضَانَ المُبَارَك 🌙",
                descriptionAr = "تَذْكِير: صِيَامُ كُلِّ يَوْمٍ فِي رَمَضَانَ فَرْضٌ. جَدِّدْ نِيَّةَ الصِّيَامِ المَفْرُوضِ قَبْلَ الفَجْرِ.",
                hadithAr = "عَنْ حَفْصَةَ رَضِيَ اللَّهُ عَنْهَا عَنِ النَّبِيِّ ﷺ قَالَ: «مَنْ لَمْ يُجْمِعِ الصِّيَامَ قَبْلَ الْفَجْرِ فَلَا صِيَامَ لَهُ».",
                isRamadan = true
            )
        }

        // 2. Let's see Tomorrow's Gregorian and Hijri day
        val tomorrowGregorian = (gregorianCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val tomorrowHijri = convertGregorianToHijri(tomorrowGregorian, offsetDays)
        val tomorrowDayOfWeek = tomorrowGregorian.get(Calendar.DAY_OF_WEEK)

        // 3. PRIORITY A: HIGH-VALUE ANNUAL OCCASIONS (Arafah, Tasua, Ashura, 10 Dhu al-Hijjah, Shawwal)
        
        // --- Eid al-Fitr (1 Shawwal) Greeting & Fasting Block ---
        if (tomorrowHijri.month == 10 && tomorrowHijri.day == 1) {
            return FastingReminderState(
                type = FastingReminderType.EID_FORBIDDEN,
                titleAr = "غداً يوم عيد الفطر السعيد 🎁 🎉",
                descriptionAr = "كل عام وأنتم بخير! يحرم صيام يوم العيد الأول شرعاً. احتفلوا واستمتعوا بالطعام والشراب وشكر نعم الله.",
                hadithAr = "عن عُمَرَ بن الخطاب رضي الله عنه: «هَذَانِ يَوْمَانِ نَهَى رَسُولُ اللَّهِ ﷺ عَنْ صِيَامِهِمَا: يَوْمُ فِطْرِكُمْ مِنْ صِيَامِكُمْ، وَالْيَوْمُ الآخَرُ تَأْكُلُونَ فِيهِ مِنْ نُسُكِكُمْ».",
                isRamadan = false
            )
        }

        // --- Eid al-Adha (10 Dhu al-Hijjah) Greeting & Fasting Block ---
        if (tomorrowHijri.month == 12 && tomorrowHijri.day == 10) {
            return FastingReminderState(
                type = FastingReminderType.EID_FORBIDDEN,
                titleAr = "غداً يوم عيد الأضحى المبارك 🕋 🎉",
                descriptionAr = "عساكم من عواده! يحرم صيام يوم النحر (العاشر من ذي الحجة) شرعاً. تقبل الله منكم صالح الأعمال والأضاحي.",
                hadithAr = "عَنْ أَبِي سَعِيدٍ الْخُدْرِيِّ رَضِيَ اللَّهُ عَنْهُ: «أَنَّ رَسُولَ اللَّهِ ﷺ نَهَى عَنْ صِيَامِ يَوْمَيْنِ: يَوْمِ الْفِطْرِ وَيَوْمِ النَّحْرِ».",
                isRamadan = false
            )
        }

        // --- Day of 'Arafah (9 Dhu al-Hijjah) ---
        if (tomorrowHijri.month == 12 && tomorrowHijri.day == 9) {
            return FastingReminderState(
                type = FastingReminderType.ARAFAH,
                titleAr = "غداً يوم عرفة المبارك (٩ ذو الحجة) 🕋",
                descriptionAr = "أفضل الأيام عند الله! صيام يوم عرفة لغير الحاج يكفر السنة الماضية والسنة اللاحقة. جدد النية الليلة وصم.",
                hadithAr = "سُئِلَ رَسُولُ اللَّهِ ﷺ عَنْ صَوْمِ يَوْمِ عَرَفَةَ فَقَالَ: «يُكَفِّرُ السَّنَةَ الْمَاضِيَةَ وَالْبَاقِيَةَ».",
                isRamadan = false
            )
        }

        // --- Tasu'a (9 Muharram) ---
        if (tomorrowHijri.month == 1 && tomorrowHijri.day == 9) {
            return FastingReminderState(
                type = FastingReminderType.TASUA,
                titleAr = "غداً تاسوعاء (٩ من محرّم) 🌟",
                descriptionAr = "صيام تاسوعاء مستحب مع عاشوراء لمخالفة غير المسلمين في الصيام ولنيل عظيم الأجر والمثوبة.",
                hadithAr = "قَالَ رَسُولُ اللَّهِ ﷺ: «لَئِنْ بَقِيتُ إِلَى قَابِلٍ لأَصُومَنَّ التَّاسِعَ».",
                isRamadan = false
            )
        }

        // --- 'Ashura (10 Muharram) ---
        if (tomorrowHijri.month == 1 && tomorrowHijri.day == 10) {
            return FastingReminderState(
                type = FastingReminderType.ASHURA,
                titleAr = "غداً عاشوراء العظيم (١٠ من محرّم) 🏆",
                descriptionAr = "صيام يوم عاشوراء يكفّر ذنوب السنة الماضية كلها، وهو يوم نجاة نبي الله موسى عليه السلام وقومه.",
                hadithAr = "عن ابن عباس رضي الله عنهما أن النبي ﷺ سئل عن صوم يوم عاشوراء فقال: «أَحْتَسِبُ عَلَى اللَّهِ أَنْ يُكَفِّرَ السَّنَةَ الَّتِي قَبْلَهُ».",
                isRamadan = false
            )
        }

        // --- First 9 Days of Dhu al-Hijjah (1 - 8 Dhu al-Hijjah) ---
        if (tomorrowHijri.month == 12 && tomorrowHijri.day in 1..8) {
            return FastingReminderState(
                type = FastingReminderType.DHUL_HIJJAH_TEN,
                titleAr = "غداً من العشر الأوائل من ذي الحجة (اليوم ${tomorrowHijri.day}) ✨",
                descriptionAr = "الأيام العشر الأوائل من ذي الحجة هي أفضل أيام الدنيا الصالحة، ويستحب صيام أياّمها والتقرب فيها بالطاعات.",
                hadithAr = "قَالَ رَسُولُ اللَّهِ ﷺ: «مَا مِنْ أَيَّامٍ الْعَمَلُ الصَّالِحُ فِيهَا أَحَبُّ إِلَى اللَّهِ مِنْ هَذِهِ الأَيَّامِ» يَعْنِي أَيَّامَ الْعَشْرِ.",
                isRamadan = false
            )
        }

        // --- Six Days of Shawwal — Daily Recommendation Reminder ---
        if (tomorrowHijri.month == 10 && tomorrowHijri.day in 2..29) {
            return FastingReminderState(
                type = FastingReminderType.SHAWWAL_SIX,
                titleAr = "صيام الست من شوّال المباركة 🌸",
                descriptionAr = "يسن للمسلم صيام 6 أيام متفرقة أو متتابعة في شهر شوال، ليكون أجر صيامها مع رمضان كصيام الدهر كاملاً.",
                hadithAr = "قَالَ رَسُولُ اللَّهِ ﷺ: «مَنْ صَامَ رَمَضَانَ ثُمَّ أَتْبَعَهُ سِتًّا مِنْ شَوَّالٍ كَانَ كَصِيَامِ الدَّهْرِ».",
                isRamadan = false
            )
        }

        // 4. PRIORITY B: WHITE DAYS (13, 14, 15) OF HIJRI MONTHS (Except Ramadan)
        when (tomorrowHijri.day) {
            13 -> {
                return FastingReminderState(
                    type = FastingReminderType.WHITE_DAY_13,
                    titleAr = "غداً أول الأيام البيض (١٣ ${tomorrowHijri.monthNameAr}) 🌟",
                    descriptionAr = "يسنُّ صيام الأيام البيض لشهر ${tomorrowHijri.monthNameAr}. تذكّر السحور والنية لغدٍ.",
                    hadithAr = "قَالَ رَسُولُ اللَّهِ ﷺ: «صِيَامُ ثَلَاثَةِ أَيَّامٍ مِنْ كُلِّ شَهْرٍ صِيَامُ الدَّهْرِ، وَهِيَ أَيَّامُ الْبِيضِ: ثَلَاثَ عَشْرَةَ، وَأَرْبَعَ عَشْرَةَ، وَخَمْسَ عَشْرَةَ».",
                    isRamadan = false
                )
            }
            14 -> {
                return FastingReminderState(
                    type = FastingReminderType.WHITE_DAY_14,
                    titleAr = "غداً ثاني الأيام البيض (١٤ ${tomorrowHijri.monthNameAr}) 🌟",
                    descriptionAr = "مستمرون في صيام الأيام البيض المباركة. لا تنسَ الإكثار من الدعاء والنية لغدٍ.",
                    hadithAr = "عَنْ جَرِيرٍ رَضِيَ اللَّهُ عَنْهُ عَنِ النَّبِيِّ ﷺ قَالَ: «صِيَامُ ثَلَاثَةِ أَيَّامٍ مِنْ كُلِّ شَهْرٍ صِيَامُ الدَّهْرِ».",
                    isRamadan = false
                )
            }
            15 -> {
                return FastingReminderState(
                    type = FastingReminderType.WHITE_DAY_15,
                    titleAr = "غداً آخر الأيام البيض (١٥ ${tomorrowHijri.monthNameAr}) 🌟",
                    descriptionAr = "غداً هو اليوم الثالث والأخير من أيام البيض لشهر ${tomorrowHijri.monthNameAr}. تقبل الله صيامكم.",
                    hadithAr = "قَالَ رَسُولُ اللَّهِ ﷺ: «إِنْ كُنْتَ صَائِمًا مِنَ الشَّهْرِ ثَلَاثًا فَصُمْ ثَلَاثَ عَشْرَةَ، وَأَرْبَعَ عَشْرَةَ، وَخَمْسَ عَشْرَةَ».",
                    isRamadan = false
                )
            }
        }

        // 5. PRIORITY C: WEEKLY FASTS (Monday and Thursday)
        if (tomorrowDayOfWeek == Calendar.MONDAY) {
            return FastingReminderState(
                type = FastingReminderType.MONDAY,
                titleAr = "تذكير: غداً صيامُ الإثنين المبارك 🌸",
                descriptionAr = "نذكّركم بصيام يوم الإثنين سنة مأثورة عن حبيبكم المصطفى ﷺ، جددوا النية الليلة وصوموا تطوعاً لله.",
                hadithAr = "عَنْ أَبِي قَتَادَةَ الأَنْصَارِيِّ رَضِيَ اللَّهُ عَنْهُ، أَنَّ رَسُولَ اللَّهِ ﷺ سُئِلَ عَنْ صَوْمِ يَوْمِ الاِثْنَيْنِ فَقَالَ: «ذَاكَ يَوْمٌ وُلِدْتُ فِيهِ، وَيَوْمٌ بُعِثْتُ أَوْ أُنْزِلَ عَلَيَّ فِيهِ».",
                isRamadan = false
            )
        }

        if (tomorrowDayOfWeek == Calendar.THURSDAY) {
            return FastingReminderState(
                type = FastingReminderType.THURSDAY,
                titleAr = "تذكير: غداً صيامُ الخميس المبارك 🌸",
                descriptionAr = "تفتح أبواب الجنة وترفع الأعمال إلى الله في يومي الإثنين والخميس، هنيئاً لمن رفع عمله وهو صائم.",
                hadithAr = "قَالَ رَسُولُ اللَّهِ ﷺ: «تُعْرَضُ الأَعْمَالُ يَوْمَ الاِثْنَيْنِ وَالْخَمِيسِ فَأُحِبُّ أَنْ يُعْرَضَ عَمَلِي وَأَنَا صَائِمٌ».",
                isRamadan = false
            )
        }

        return FastingReminderState(
            type = FastingReminderType.NONE,
            titleAr = "لا يوجد صيام مسنون غداً",
            descriptionAr = "اليوم هو يوم عادي. متاح صيام التطوع المطلق في أي وقت تبتغي فيه الأجر والثواب الجزيل.",
            hadithAr = "عَنْ أَبِي سَعِيدٍ الْخُدْرِيِّ رَضِيَ اللَّهُ عَنْهُ قَالَ: قَالَ رَسُولُ اللَّهِ ﷺ: «مَنْ صَامَ يَوْمًا فِي سَبِيلِ اللَّهِ بَعَّدَ اللَّهُ وَجْهَهُ عَنِ النَّارِ سَبْعِينَ خَرِيفًا».",
            isRamadan = false
        )
    }
}
