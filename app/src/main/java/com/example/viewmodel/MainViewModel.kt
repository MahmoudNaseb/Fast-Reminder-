package com.example.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.prayerDao(), database.fastingDao())

    // Standard location is set to Holy city of Makkah (Mecca) as a stunning baseline
    private val _latitude = MutableStateFlow(21.4225)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(39.8262)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _locationName = MutableStateFlow("مكة المكرمة (تلقائي)")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _calculationMethod = MutableStateFlow(CalculationMethod.UMM_AL_QURA)
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _hijriOffset = MutableStateFlow(0)
    val hijriOffset: StateFlow<Int> = _hijriOffset.asStateFlow()

    // Realtime date and time states
    private val _currentGregorianDateStr = MutableStateFlow("")
    val currentGregorianDateStr: StateFlow<String> = _currentGregorianDateStr.asStateFlow()

    private val _currentDayOfWeekAr = MutableStateFlow("")
    val currentDayOfWeekAr: StateFlow<String> = _currentDayOfWeekAr.asStateFlow()

    // Location Service Client
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        updateDateStates()
        // Record standard fast reminders
        viewModelScope.launch {
            // Pre-seed some items or load default preferences if any
        }
    }

    private fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Reactive Flow - Today's Hijri Date
    val todayHijriDate: StateFlow<HijriDate> = combine(_hijriOffset) { offset ->
        val cal = Calendar.getInstance()
        HijriCalendarHelper.convertGregorianToHijri(cal, offset[0])
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HijriCalendarHelper.convertGregorianToHijri(Calendar.getInstance(), 0)
    )

    // Reactive Flow - Today's calculated precise Prayer Times
    val todayPrayerTimes: StateFlow<PrayerTimes> = combine(
        _latitude, _longitude, _calculationMethod, todayHijriDate
    ) { lat, lng, method, hijri ->
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        
        // Check if current Hijri month is Ramadan
        val isRamadan = (hijri.month == 9)
        
        // Find timezone offset in hours
        val timeZone = TimeZone.getDefault()
        val offsetHours = timeZone.getOffset(System.currentTimeMillis()) / 3600000.0

        PrayerTimesCalculator.calculatePrayerTimes(
            year = year,
            month = month,
            day = day,
            latitude = lat,
            longitude = lng,
            timezoneOffsetHours = offsetHours,
            method = method,
            isRamadan = isRamadan
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrayerTimes("--:--", "--:--", "--:--", "--:--", "--:--", "--:--")
    )

    // Reactive Flow - Current Fasting reminders
    val fastingReminder: StateFlow<FastingReminderState> = combine(
        _hijriOffset
    ) { offset ->
        val cal = Calendar.getInstance()
        HijriCalendarHelper.determineFastingReminder(cal, offset[0])
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FastingReminderState(
            type = FastingReminderType.NONE,
            titleAr = "جاري الحساب...",
            descriptionAr = "",
            hadithAr = "",
            isRamadan = false
        )
    )

    // Reactive Flow - Room Database checking for Today's Prayer Check-offs
    val todayPrayersDone: StateFlow<List<PrayerRecord>> = todayPrayerTimes.flatMapLatest {
        repository.getPrayersForDate(getTodayDateKey())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reactive Flow - Room Database checking for Today's fasting completed status
    val todayFastingDone: StateFlow<FastingRecord?> = todayHijriDate.flatMapLatest {
        repository.getFastingRecordForDate(getTodayDateKey())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Reactive Flow - All fasting log history for Fasting Diary Tracker
    val fastingHistory: StateFlow<List<FastingRecord>> = repository.allFastingRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateDateStates() {
        val cal = Calendar.getInstance()
        val dFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
        _currentGregorianDateStr.value = dFormat.format(cal.time)

        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale("ar"))
        _currentDayOfWeekAr.value = dayOfWeekFormat.format(cal.time)
    }

    // Toggle check-off for specific prayers (Fajr, Dhuhr, etc.)
    fun togglePrayerCompleted(prayerName: String, isNowDone: Boolean) {
        viewModelScope.launch {
            repository.insertPrayer(
                PrayerRecord(
                    dateStr = getTodayDateKey(),
                    prayerName = prayerName,
                    isDone = isNowDone
                )
            )
        }
    }

    // Toggle Fasting completion for today
    fun toggleFastingCompleted(isFasted: Boolean, typeString: String) {
        viewModelScope.launch {
            if (isFasted) {
                repository.insertFastingRecord(
                    FastingRecord(
                        dateStr = getTodayDateKey(),
                        hijriDateFormatted = todayHijriDate.value.formattedAr,
                        isFasted = true,
                        fastType = typeString
                    )
                )
            } else {
                repository.deleteFastingRecordForDate(getTodayDateKey())
            }
        }
    }

    // Custom configuration adjustments
    fun setCalculationMethod(method: CalculationMethod) {
        _calculationMethod.value = method
    }

    fun setHijriOffset(offset: Int) {
        _hijriOffset.value = offset
    }

    fun setManualCoordinates(lat: Double, lng: Double, name: String) {
        _latitude.value = lat
        _longitude.value = lng
        _locationName.value = name
    }

    // Fetch live device location to update prayer schedules accurately
    @SuppressLint("MissingPermission")
    fun fetchLiveLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _latitude.value = location.latitude
                    _longitude.value = location.longitude
                    
                    // Format custom location coordinate tag
                    val latFormatted = String.format(Locale.US, "%.3f", location.latitude)
                    val lngFormatted = String.format(Locale.US, "%.3f", location.longitude)
                    _locationName.value = "موقعي الحالي ($latFormatted, $lngFormatted)"
                } else {
                    _locationName.value = "الشرق الأوسط (مبني على مكة)"
                    _latitude.value = 21.4225
                    _longitude.value = 39.8262
                }
            }.addOnFailureListener {
                _locationName.value = "الشرق الأوسط (موقع افتراضي)"
                _latitude.value = 21.4225
                _longitude.value = 39.8262
            }
        } catch (e: Exception) {
            _locationName.value = "موقع افتراضي (مكة)"
        }
    }
}
