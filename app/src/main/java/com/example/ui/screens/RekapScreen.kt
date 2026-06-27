package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Proyek
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.CompletedGray
import com.example.ui.theme.AccentOrange
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.RekapRow
import com.example.ui.viewmodel.TukangGajiRow
import com.example.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val proyekList by viewModel.proyekList.collectAsState()
    val filterProyekId by viewModel.selectedFilterProyekId.collectAsState()
    
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Harian, 1: Mingguan, 2: Bulanan
    var showProjectDropdown by remember { mutableStateOf(false) }

    // State untuk masing-masing tipe rekap
    var selectedDate by remember { mutableStateOf(DateUtils.getTodayString()) }
    var weekRange by remember { mutableStateOf(DateUtils.getCurrentWeekRange()) }
    
    // Month selector state
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) } // 1-12
    var showMonthDropdown by remember { mutableStateOf(false) }

    val selectedProyekName = proyekList.find { it.id == filterProyekId }?.nama ?: "Semua Proyek"

    // Query Rekap Data
    val rekapHarianList by viewModel.getRekapHarian(selectedDate, filterProyekId)
        .collectAsState(initial = emptyList())

    val rekapMingguanList by viewModel.getPenggajianList(weekRange.first, weekRange.second, filterProyekId)
        .collectAsState(initial = emptyList())

    // Bulanan menggunakan range bulan tersebut
    val monthRange = remember(selectedYear, selectedMonth) { DateUtils.getMonthRange(selectedYear, selectedMonth) }
    val rekapBulananList by viewModel.getPenggajianList(monthRange.first, monthRange.second, filterProyekId)
        .collectAsState(initial = emptyList())

    // DatePicker Dialog untuk Harian
    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val mStr = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val dStr = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            selectedDate = "$year-$mStr-$dStr"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Helper navigasi harian & mingguan
    fun adjustDate(days: Int) {
        try {
            val date = dbDateFormat.parse(selectedDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, days)
            selectedDate = dbDateFormat.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun adjustWeek(weeks: Int) {
        try {
            val currentSunday = dbDateFormat.parse(weekRange.first) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = currentSunday
            cal.add(Calendar.WEEK_OF_YEAR, weeks)
            val newSunday = dbDateFormat.format(cal.time)
            cal.add(Calendar.DATE, 6)
            val newSaturday = dbDateFormat.format(cal.time)
            weekRange = Pair(newSunday, newSaturday)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekap Kehadiran & Upah", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Filter Proyek & Tab Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FILTER PROYEK:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { showProjectDropdown = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FilterAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(selectedProyekName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }

                        DropdownMenu(
                            expanded = showProjectDropdown,
                            onDismissRequest = { showProjectDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Proyek") },
                                onClick = {
                                    viewModel.setFilterProyek(null)
                                    showProjectDropdown = false
                                }
                            )
                            proyekList.forEach { proyek ->
                                DropdownMenuItem(
                                    text = { Text(proyek.nama) },
                                    onClick = {
                                        viewModel.setFilterProyek(proyek.id)
                                        showProjectDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Tab Menu Rekap (Harian, Mingguan, Bulanan)
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("HARIAN", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("MINGGUAN", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("BULANAN", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // 3. Date / Week / Month Navigation Bar based on active tab
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    when (activeTab) {
                        0 -> { // Harian
                            IconButton(onClick = { adjustDate(-1) }) { Icon(Icons.Default.NavigateBefore, contentDescription = "Sebelumnya") }
                            Row(
                                modifier = Modifier.clickable { datePickerDialog.show() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, size16Modifier(), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DateUtils.formatToDisplay(selectedDate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }
                            IconButton(onClick = { adjustDate(1) }) { Icon(Icons.Default.NavigateNext, contentDescription = "Berikutnya") }
                        }
                        1 -> { // Mingguan
                            IconButton(onClick = { adjustWeek(-1) }) { Icon(Icons.Default.NavigateBefore, contentDescription = "Sebelumnya") }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, size16Modifier(), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DateUtils.formatIndonesianDateRange(weekRange.first, weekRange.second), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                            IconButton(onClick = { adjustWeek(1) }) { Icon(Icons.Default.NavigateNext, contentDescription = "Berikutnya") }
                        }
                        2 -> { // Bulanan
                            IconButton(
                                onClick = {
                                    if (selectedMonth == 1) {
                                        selectedMonth = 12
                                        selectedYear -= 1
                                    } else {
                                        selectedMonth -= 1
                                    }
                                }
                            ) { Icon(Icons.Default.NavigateBefore, contentDescription = "Bulan Sebelumnya") }
                            
                            Row(
                                modifier = Modifier.clickable { showMonthDropdown = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarViewMonth, contentDescription = null, size16Modifier(), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = DateUtils.formatToMonthYearDisplay(selectedYear, selectedMonth),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            
                            IconButton(
                                onClick = {
                                    if (selectedMonth == 12) {
                                        selectedMonth = 1
                                        selectedYear += 1
                                    } else {
                                        selectedMonth += 1
                                    }
                                }
                            ) { Icon(Icons.Default.NavigateNext, contentDescription = "Bulan Selanjutnya") }

                            // Month selection dropdown (simply picker years & months)
                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false }
                            ) {
                                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                for (y in (currentYear - 2)..(currentYear + 1)) {
                                    for (m in 1..12) {
                                        DropdownMenuItem(
                                            text = { Text(DateUtils.formatToMonthYearDisplay(y, m)) },
                                            onClick = {
                                                selectedYear = y
                                                selectedMonth = m
                                                showMonthDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Data Sheet / Breakdown List based on Tab
            when (activeTab) {
                0 -> { // Harian
                    if (rekapHarianList.isEmpty()) {
                        EmptyRekapView("Tidak ada catatan kehadiran pada tanggal ini.")
                    } else {
                        val totalUpahHariIni = rekapHarianList.sumOf { it.upah }
                        
                        Text(
                            text = "RINCIAN HADIR & ESTIMASI UPAH HARIAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(rekapHarianList) { row ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(row.namaTukang, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(row.statusHadir, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (row.statusHadir == "Hadir") ActiveGreen else CompletedGray)
                                        }
                                        Text(
                                            text = if (row.upah > 0) formatRupiah(row.upah) else "-",
                                            fontWeight = FontWeight.Bold,
                                            color = if (row.upah > 0) AccentOrange else CompletedGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Summary
                        BottomRekapSummary("Total Estimasi Upah Hari Ini", totalUpahHariIni)
                    }
                }
                1 -> { // Mingguan
                    if (rekapMingguanList.isEmpty()) {
                        EmptyRekapView("Tidak ada rekap upah minggu ini.")
                    } else {
                        val totalUpahMingguan = rekapMingguanList.sumOf { it.totalGaji }
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(rekapMingguanList) { row ->
                                RekapPayrollRowCard(row)
                            }
                        }

                        BottomRekapSummary("Total Gaji Mingguan Semua Tukang", totalUpahMingguan)
                    }
                }
                2 -> { // Bulanan
                    if (rekapBulananList.isEmpty()) {
                        EmptyRekapView("Tidak ada rekap upah bulan ini.")
                    } else {
                        val totalUpahBulanan = rekapBulananList.sumOf { it.totalGaji }
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(rekapBulananList) { row ->
                                RekapPayrollRowCard(row)
                            }
                        }

                        BottomRekapSummary("Total Gaji Bulanan Semua Tukang", totalUpahBulanan)
                    }
                }
            }
        }
    }
}

@Composable
fun RekapPayrollRowCard(row: TukangGajiRow) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(row.tukang.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${row.hadirCount} hari hadir @ ${formatRupiah(row.tukang.upahHarian)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
            Text(
                text = formatRupiah(row.totalGaji),
                fontWeight = FontWeight.Bold,
                color = AccentOrange,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun EmptyRekapView(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 13.sp)
        }
    }
}

@Composable
fun BottomRekapSummary(label: String, value: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(formatRupiah(value), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AccentOrange)
        }
    }
}

private fun size16Modifier() = Modifier.size(16.dp)
