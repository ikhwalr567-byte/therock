package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Proyek
import com.example.data.model.Tukang
import com.example.ui.theme.ActiveGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsensiScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val proyekList by viewModel.proyekList.collectAsState()
    val selectedProyekId by viewModel.selectedAbsensiProyekId.collectAsState()
    val selectedDate by viewModel.selectedAbsensiDate.collectAsState()
    val attendanceMap by viewModel.currentAbsensiMap.collectAsState()
    val allTukang by viewModel.tukangList.collectAsState()

    val context = LocalContext.current
    var showProjectDropdown by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Filter tukang yang terdaftar di proyek yang dipilih
    val filteredTukangList = remember(allTukang, selectedProyekId) {
        allTukang.filter { it.proyekId == selectedProyekId && it.statusAktif }
    }

    val selectedProyekName = proyekList.find { it.id == selectedProyekId }?.nama ?: "Pilih Proyek"

    // Helper DatePickerDialog
    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val calendar = Calendar.getInstance()
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val mStr = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val dStr = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            viewModel.setAbsensiDate("$year-$mStr-$dStr")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Navigasi Tanggal (Kurangi 1 hari atau tambah 1 hari)
    fun adjustDate(days: Int) {
        try {
            val date = dbDateFormat.parse(selectedDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, days)
            viewModel.setAbsensiDate(dbDateFormat.format(cal.time))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pencatatan Absensi Harian", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
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
            // 1. Selector Proyek di bagian atas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PILIH PROYEK KONSTRUKSI:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
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
                                Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedProyekName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }

                        DropdownMenu(
                            expanded = showProjectDropdown,
                            onDismissRequest = { showProjectDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            if (proyekList.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada proyek dibuat") },
                                    onClick = { showProjectDropdown = false }
                                )
                            }
                            proyekList.forEach { proyek ->
                                DropdownMenuItem(
                                    text = { Text(proyek.nama) },
                                    onClick = {
                                        viewModel.setAbsensiProyek(proyek.id)
                                        showProjectDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Bar Selector Tanggal
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { adjustDate(-1) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Hari Sebelumnya", tint = MaterialTheme.colorScheme.primary)
                        }

                        // Menampilkan Tanggal format Indonesia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateUtils.formatToDisplay(selectedDate),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { adjustDate(1) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Hari Selanjutnya", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 3. Status Kehadiran Ringkas & Daftar Pekerja
            if (selectedProyekId == null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Silakan buat atau pilih proyek terlebih dahulu di atas.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else if (filteredTukangList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Tukang di Proyek Ini",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Silakan tambahkan tukang baru atau tugaskan tukang ke proyek ini di menu 'Data Tukang'.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Kehadiran Banner Ringkas
                val totalTukang = filteredTukangList.size
                val hadirCount = filteredTukangList.count { attendanceMap[it.id] == true }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAFTAR PEKERJA ($totalTukang)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$hadirCount Hadir / ${totalTukang - hadirCount} Sakit-Alpha",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hadirCount == totalTukang) ActiveGreen else MaterialTheme.colorScheme.primary
                    )
                }

                // List Tukang dengan Checkbox Baris Besar
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTukangList) { tukang ->
                        val isChecked = attendanceMap[tukang.id] ?: false
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) ActiveGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = if (isChecked) borderStroke(1.dp, ActiveGreen.copy(alpha = 0.5f)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.toggleKehadiran(tukang.id, !isChecked)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isChecked) ActiveGreen.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isChecked) ActiveGreen else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = tukang.nama,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Upah: ${formatRupiah(tukang.upahHarian)}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                // Checkbox Besar dan Mudah Diklik
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        viewModel.toggleKehadiran(tukang.id, it)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ActiveGreen,
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Tombol Simpan Absensi Sticky di Bagian Bawah
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.simpanAbsensi()
                            showSuccessDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_absensi_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIMPAN ABSENSI HARI INI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Absensi Disimpan", fontWeight = FontWeight.Bold, color = ActiveGreen) },
            text = { Text("Data absensi proyek '$selectedProyekName' pada tanggal ${DateUtils.formatToDisplay(selectedDate)} telah berhasil disimpan ke database lokal.") },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Helper border stroke
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
