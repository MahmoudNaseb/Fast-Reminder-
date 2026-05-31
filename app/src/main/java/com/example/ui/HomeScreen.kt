package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FastingRecord
import com.example.data.PrayerRecord
import com.example.ui.theme.*
import com.example.util.CalculationMethod
import com.example.util.FastingReminderState
import com.example.util.FastingReminderType
import com.example.util.FontHelper
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Observe StateFlow lists from ViewModel
    val todayHijri by viewModel.todayHijriDate.collectAsStateWithLifecycle()
    val todayPrayers by viewModel.todayPrayerTimes.collectAsStateWithLifecycle()
    val fastingReminderState by viewModel.fastingReminder.collectAsStateWithLifecycle()
    
    val todayPrayersDone by viewModel.todayPrayersDone.collectAsStateWithLifecycle()
    val todayFastingDone by viewModel.todayFastingDone.collectAsStateWithLifecycle()
    val fastingHistory by viewModel.fastingHistory.collectAsStateWithLifecycle()
    
    val locationName by viewModel.locationName.collectAsStateWithLifecycle()
    val latitude by viewModel.latitude.collectAsStateWithLifecycle()
    val longitude by viewModel.longitude.collectAsStateWithLifecycle()

    val currentGregorianDate by viewModel.currentGregorianDateStr.collectAsStateWithLifecycle()
    val currentDayOfWeek by viewModel.currentDayOfWeekAr.collectAsStateWithLifecycle()

    // Setup GPS Launch Permissions
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.fetchLiveLocation()
        }
    }

    // Attempt GPS updating on startup
    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = IslamicLightSurface,
                contentColor = IslamicCalmGreen,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "الصلاة") },
                    label = { Text("مواقيت الصلاة", fontFamily = FontHelper.BodyArabicFontFamily, fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = IslamicCalmGreen,
                        indicatorColor = IslamicCalmGreen,
                        unselectedIconColor = IslamicCalmGreen.copy(alpha = 0.6f),
                        unselectedTextColor = IslamicCalmGreen.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "سجل الصيام") },
                    label = { Text("مذكرة الصيام", fontFamily = FontHelper.BodyArabicFontFamily, fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = IslamicCalmGreen,
                        indicatorColor = IslamicCalmGreen,
                        unselectedIconColor = IslamicCalmGreen.copy(alpha = 0.6f),
                        unselectedTextColor = IslamicCalmGreen.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "الاعدادات") },
                    label = { Text("الإعدادات", fontFamily = FontHelper.BodyArabicFontFamily, fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = IslamicCalmGreen,
                        indicatorColor = IslamicCalmGreen,
                        unselectedIconColor = IslamicCalmGreen.copy(alpha = 0.6f),
                        unselectedTextColor = IslamicCalmGreen.copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { innerPadding ->
        OrnamentalBackground(
            modifier = Modifier.padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header displaying Adjusted Arabic Hijri Calendar Date
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = todayHijri.formattedAr,
                        fontFamily = FontHelper.ThuluthStyleFontFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicCalmGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                    )
                    
                    Text(
                        text = "$currentDayOfWeek، $currentGregorianDate مـ",
                        fontFamily = FontHelper.BodyArabicFontFamily,
                        fontSize = 14.sp,
                        color = DarkGreen.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "tab_transition"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> PrayersAndFastingTab(
                                todayPrayers = todayPrayers,
                                fastingReminder = fastingReminderState,
                                todayPrayersDone = todayPrayersDone,
                                todayFastingDone = todayFastingDone,
                                locationName = locationName,
                                onTogglePrayer = { name, isDone -> viewModel.togglePrayerCompleted(name, isDone) },
                                onToggleFasting = { isFasted, type -> viewModel.toggleFastingCompleted(isFasted, type) },
                                onRequestLocation = {
                                    locationPermissionRequest.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            )
                            1 -> FastingJournalTab(
                                fastingHistory = fastingHistory,
                                isTodayFastingCompleted = todayFastingDone != null
                            )
                            2 -> ConfigurationTab(
                                viewModel = viewModel,
                                locationPermissionLauncher = locationPermissionRequest
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================= TAB 1: PRAYER TIMES & FAST REMINDERS =======================
@Composable
fun PrayersAndFastingTab(
    todayPrayers: com.example.util.PrayerTimes,
    fastingReminder: FastingReminderState,
    todayPrayersDone: List<PrayerRecord>,
    todayFastingDone: FastingRecord?,
    locationName: String,
    onTogglePrayer: (String, Boolean) -> Unit,
    onToggleFasting: (Boolean, String) -> Unit,
    onRequestLocation: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        
        // Fasting Reminder Zone (Handles Ramadan Intention and voluntary fasting exceptions)
        item {
            FastingReminderCard(
                reminderState = fastingReminder,
                userFastedTodayState = todayFastingDone != null,
                onFastingToggled = { isFasted ->
                    val typeStr = if (fastingReminder.isRamadan) "RAMADAN_OBLIGATORY" else fastingReminder.type.name
                    onToggleFasting(isFasted, typeStr)
                }
            )
        }

        // Location Info and Updates Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(0.5.dp, IslamicCalmGreen.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "الموقع",
                            tint = IslamicCalmGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = locationName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontHelper.BodyArabicFontFamily,
                            color = DarkGreen.copy(alpha = 0.9f)
                        )
                    }
                    Button(
                        onClick = onRequestLocation,
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicCalmGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("gps_update_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تحديث GPS", fontSize = 11.sp, fontFamily = FontHelper.BodyArabicFontFamily)
                    }
                }
            }
        }

        // Daily Checklist Headers
        item {
            Text(
                text = "مواعيد الصلوات ومذكرة الأداء",
                fontFamily = FontHelper.ThuluthStyleFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicCalmGreen,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        // Daily Prayers List checkmarks
        val prayers = listOf(
            Triple("الفجر", todayPrayers.fajr, "Fajr"),
            Triple("الشروق", todayPrayers.sunrise, "Sunrise"),
            Triple("الظهر", todayPrayers.dhuhr, "Dhuhr"),
            Triple("العصر", todayPrayers.asr, "Asr"),
            Triple("المغرب", todayPrayers.maghrib, "Maghrib"),
            Triple("العشاء", todayPrayers.isha, "Isha")
        )

        items(prayers) { (displayName, displayTime, keyName) ->
            val isChecked = todayPrayersDone.any { it.prayerName == keyName && it.isDone }
            PrayerTimeRow(
                displayName = displayName,
                timeStr = displayTime,
                keyName = keyName,
                isChecked = isChecked,
                onCheckedChange = { onTogglePrayer(keyName, it) }
            )
        }
    }
}

// Beautiful Fasting Card incorporating the critical Ramadan daily reminder exceptions
@Composable
fun FastingReminderCard(
    reminderState: FastingReminderState,
    userFastedTodayState: Boolean,
    onFastingToggled: (Boolean) -> Unit
) {
    val isRamadan = reminderState.isRamadan
    val cardBg = if (isRamadan) {
        Brush.verticalGradient(listOf(IslamicDarkBg, IslamicCalmGreen))
    } else {
        Brush.verticalGradient(listOf(Color.White, IslamicLightSurface))
    }
    
    val textColor = if (isRamadan) Color.White else DarkGreen
    val subTextColor = if (isRamadan) Color.White.copy(alpha = 0.85f) else DarkGreen.copy(alpha = 0.75f)
    val goldAccentColor = if (isRamadan) IslamicGold else IslamicCalmGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fasting_reminder_card"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (isRamadan) IslamicGold else IslamicGold.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = reminderState.titleAr,
                        fontFamily = FontHelper.ThuluthStyleFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRamadan) IslamicGold else IslamicCalmGreen
                    )
                    
                    // Cute badge for Ramadan fard
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isRamadan) IslamicGold else IslamicCalmGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isRamadan) "صوم فرض" else "صوم تطوع",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontHelper.BodyArabicFontFamily,
                            color = if (isRamadan) DarkGreen else IslamicCalmGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful description (Contains "جدد نية الصيام" during Ramadan)
                Text(
                    text = reminderState.descriptionAr,
                    fontFamily = FontHelper.BodyArabicFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // HADITH QUOTE PANEL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardColors(
                        containerColor = if (isRamadan) Color.Black.copy(alpha = 0.25f) else IslamicCalmGreen.copy(alpha = 0.05f),
                        contentColor = subTextColor,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = reminderState.hadithAr,
                        fontFamily = FontHelper.BodyArabicFontFamily,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mark fast completed today switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isRamadan) IslamicGold.copy(alpha = 0.15f) else IslamicCalmGreen.copy(alpha = 0.1f))
                        .clickable { onFastingToggled(!userFastedTodayState) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (userFastedTodayState) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "تثبيت الصيام",
                            tint = goldAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (userFastedTodayState) "تم تسجيل صيام اليوم ✓" else "اضغط لتسجيل صيام هذا اليوم",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontHelper.BodyArabicFontFamily,
                            color = textColor
                        )
                    }
                    Text(
                        text = if (isRamadan) "تجديد النية" else "سنة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldAccentColor,
                        fontFamily = FontHelper.BodyArabicFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerTimeRow(
    displayName: String,
    timeStr: String,
    keyName: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isSunrise = keyName == "Sunrise"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_$keyName"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = if (isChecked) 1.5.dp else 0.5.dp,
            color = if (isChecked) IslamicCalmGreen else IslamicCalmGreen.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sunrise doesn't have a prayer checkmark box, just a sun indicator!
                if (!isSunrise) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = onCheckedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = IslamicCalmGreen,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "شروق",
                        tint = IslamicGold,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                }
                Text(
                    text = displayName,
                    fontFamily = FontHelper.BodyArabicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkGreen
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeStr,
                    fontFamily = FontHelper.ThuluthStyleFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isChecked) IslamicCalmGreen else DarkGreen
                )
                
                if (isChecked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تم",
                        tint = IslamicCalmGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ======================= TAB 2: SPIRITUAL DIARY / FASTING JOURNAL =======================
@Composable
fun FastingJournalTab(
    fastingHistory: List<FastingRecord>,
    isTodayFastingCompleted: Boolean
) {
    val totalFasts = fastingHistory.filter { it.isFasted }.size
    val ramadanFasts = fastingHistory.filter { it.isFasted && it.fastType == "RAMADAN_OBLIGATORY" }.size
    val voluntaryFasts = fastingHistory.filter { it.isFasted && it.fastType != "RAMADAN_OBLIGATORY" }.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("fasting_summary_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, IslamicGold),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "سجل العبادات وصيام التطوع",
                        fontFamily = FontHelper.ThuluthStyleFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicCalmGreen
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SpiritualMetric("إجمالي الأيام", totalFasts.toString(), IslamicGold)
                        SpiritualMetric("صيام رمضان", ramadanFasts.toString(), IslamicCalmGreen)
                        SpiritualMetric("السنن والتطوع", voluntaryFasts.toString(), IslamicEmerald)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "«عَلَيْكَ بِالصَّوْمِ فَإِنَّهُ لَا مِثْلَ لَهُ»",
                        fontFamily = FontHelper.BodyArabicFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicCalmGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Text(
                text = "أيّامك المباركة المسجّلة 📜",
                fontFamily = FontHelper.ThuluthStyleFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicCalmGreen,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        if (fastingHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChromeReaderMode,
                            contentDescription = "سجل فارغ",
                            tint = IslamicCalmGreen.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد أيام صيام مسجلة حتى الآن.\nابدأ بتسجيل صيامك من الصفحة الرئيسية!",
                            fontSize = 13.sp,
                            fontFamily = FontHelper.BodyArabicFontFamily,
                            textAlign = TextAlign.Center,
                            color = DarkGreen.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(fastingHistory) { record ->
                val localizedTypeStr = when (record.fastType) {
                    "RAMADAN_OBLIGATORY" -> "صوم رمضان المبارك"
                    "MONDAY" -> "صوم يوم الإثنين سنّة"
                    "THURSDAY" -> "صوم يوم الخميس سنّة"
                    "WHITE_DAY_13", "WHITE_DAY_14", "WHITE_DAY_15" -> "صيام الأيام البيض"
                    "TASUA" -> "صيام يوم تاسوعاء سنّة"
                    "ASHURA" -> "صيام يوم عاشوراء سنّة"
                    "ARAFAH" -> "صيام يوم عرفة سنّة مؤكدة"
                    "DHUL_HIJJAH_TEN" -> "صيام العشر الأوائل من ذي الحجة"
                    "SHAWWAL_SIX" -> "صيام الست من شوّال سنّة"
                    else -> "صيام النوافل والتطوع"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, IslamicCalmGreen.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = record.hijriDateFormatted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontHelper.BodyArabicFontFamily,
                                color = IslamicCalmGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = localizedTypeStr,
                                fontSize = 12.sp,
                                fontFamily = FontHelper.BodyArabicFontFamily,
                                color = DarkGreen.copy(alpha = 0.7f)
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = IslamicLightBg),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "تقبل الله ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicCalmGreen,
                                fontFamily = FontHelper.BodyArabicFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpiritualMetric(title: String, score: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = score,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontHelper.ThuluthStyleFontFamily,
            color = color
        )
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen.copy(alpha = 0.7f),
            fontFamily = FontHelper.BodyArabicFontFamily
        )
    }
}

// ======================= TAB 3: CONFIGURATION / SETTINGS =======================
@Composable
fun ConfigurationTab(
    viewModel: MainViewModel,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val calculationMethod by viewModel.calculationMethod.collectAsStateWithLifecycle()
    val hijriOffset by viewModel.hijriOffset.collectAsStateWithLifecycle()
    val latitude by viewModel.latitude.collectAsStateWithLifecycle()
    val longitude by viewModel.longitude.collectAsStateWithLifecycle()

    var customLatText by remember { mutableStateOf(latitude.toString()) }
    var customLngText by remember { mutableStateOf(longitude.toString()) }
    var customNameText by remember { mutableStateOf("موقعي المخصص") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_calculation_method_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, IslamicCalmGreen.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "منهجية حساب الأوقات الرصدية",
                        fontFamily = FontHelper.ThuluthStyleFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicCalmGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CalculationMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setCalculationMethod(method) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = calculationMethod == method,
                                onClick = { viewModel.setCalculationMethod(method) },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicCalmGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = method.displayNameAr,
                                fontSize = 14.sp,
                                fontWeight = if (calculationMethod == method) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontHelper.BodyArabicFontFamily,
                                color = DarkGreen
                            )
                        }
                        HorizontalDivider(color = IslamicCalmGreen.copy(alpha = 0.08f))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_hijri_offset_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, IslamicCalmGreen.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التعديل الهجري (رؤية الهلال)",
                        fontFamily = FontHelper.ThuluthStyleFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicCalmGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضبط الأيام بزيادة أو نقصان لمطابقة تاريخ الرؤية المحلي لبلدك.",
                        fontSize = 12.sp,
                        fontFamily = FontHelper.BodyArabicFontFamily,
                        color = DarkGreen.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(-2, -1, 0, 1, 2).forEach { offset ->
                            val sign = if (offset > 0) "+" else ""
                            FilterChip(
                                selected = hijriOffset == offset,
                                onClick = { viewModel.setHijriOffset(offset) },
                                label = {
                                    Text(
                                        text = "$sign$offset يوم",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontHelper.BodyArabicFontFamily
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IslamicCalmGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Custom Manual coordinate setter to run flawlessly in various emulators
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_coordinates_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, IslamicCalmGreen.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الإدخال اليدوي للموقع الجغرافي",
                        fontFamily = FontHelper.ThuluthStyleFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicCalmGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "إذا لم يعمل الـ GPS في جهازك، يمكنك إدخال إحداثيات مدينتك يدوياً للحصول على أوقات صلاة دقيقة للغاية.",
                        fontSize = 12.sp,
                        fontFamily = FontHelper.BodyArabicFontFamily,
                        color = DarkGreen.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customNameText,
                        onValueChange = { customNameText = it },
                        label = { Text("اسم المدينة / الدولة", fontFamily = FontHelper.BodyArabicFontFamily) },
                        modifier = Modifier.fillMaxWidth().testTag("location_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicCalmGreen)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = customLatText,
                            onValueChange = { customLatText = it },
                            label = { Text("خط العرض Lat", fontFamily = FontHelper.BodyArabicFontFamily) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("latitude_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicCalmGreen)
                        )

                        OutlinedTextField(
                            value = customLngText,
                            onValueChange = { customLngText = it },
                            label = { Text("خط الطول Lng", fontFamily = FontHelper.BodyArabicFontFamily) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("longitude_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicCalmGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val latVal = customLatText.toDoubleOrNull() ?: 21.4225
                            val lngVal = customLngText.toDoubleOrNull() ?: 39.8262
                            viewModel.setManualCoordinates(latVal, lngVal, customNameText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicCalmGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_coords_button")
                    ) {
                        Text(
                            text = "حفظ الإحداثيات وحساب المواعيد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontHelper.BodyArabicFontFamily
                        )
                    }
                }
            }
        }
    }
}
