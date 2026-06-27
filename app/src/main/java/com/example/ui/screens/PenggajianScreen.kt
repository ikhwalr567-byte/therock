package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.Proyek
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.TukangGajiRow
import com.example.utils.DateUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenggajianScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val proyekList by viewModel.proyekList.collectAsState()
    val filterProyekId by viewModel.selectedFilterProyekId.collectAsState()
    val weekRange by viewModel.gajiWeekRange.collectAsState()

    val context = LocalContext.current
    var showProjectDropdown by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Membaca list gaji tukang secara reaktif berdasarkan filter
    val payrollList by viewModel.getPenggajianList(weekRange.first, weekRange.second, filterProyekId)
        .collectAsState(initial = emptyList())

    val selectedProyekName = proyekList.find { it.id == filterProyekId }?.nama ?: "Semua Proyek"

    // Perhitungan Tanggal (Navigasi Mingguan)
    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    
    fun adjustWeek(weeks: Int) {
        try {
            val currentSunday = dbDateFormat.parse(weekRange.first) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = currentSunday
            cal.add(Calendar.WEEK_OF_YEAR, weeks)
            val newSunday = dbDateFormat.format(cal.time)
            
            cal.add(Calendar.DATE, 6)
            val newSaturday = dbDateFormat.format(cal.time)
            
            viewModel.setGajiWeekRange(newSunday, newSaturday)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rincian Gaji Mingguan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Ekspor Laporan Gaji", tint = MaterialTheme.colorScheme.primary)
                    }
                }
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
            // 1. Selector Proyek & Navigasi Periode Mingguan
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FILTER BERDASARKAN PROYEK:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
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
                                Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedProyekName,
                                    fontSize = 14.sp,
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
                            DropdownMenuItem(
                                text = { Text("Semua Proyek (Gabungan)") },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Periode Mingguan (Minggu s/d Sabtu)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { adjustWeek(-1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = "Minggu Lalu", tint = MaterialTheme.colorScheme.primary)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "PERIODE MINGGUAN (MINGGU - SABTU)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = DateUtils.formatIndonesianDateRange(weekRange.first, weekRange.second),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { adjustWeek(1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        ) {
                            Icon(Icons.Default.NavigateNext, contentDescription = "Minggu Depan", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 2. Konten Gaji
            if (payrollList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tidak ada data upah atau pekerja aktif.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            } else {
                val totalGajiSemuaTukang = payrollList.sumOf { it.totalGaji }

                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text(
                        text = "UPAH DETIL PER PEKERJA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 20.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payrollList) { row ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = row.tukang.nama,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ActiveGreen.copy(alpha = 0.12f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${row.hadirCount} Hari Hadir",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ActiveGreen
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Upah @${formatRupiah(row.tukang.upahHarian)}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "TOTAL UPAH",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = formatRupiah(row.totalGaji),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentOrange
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Ringkasan Total Gaji & ATM Rekomendasi
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL GAJI SEMUA TUKANG",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = formatRupiah(totalGajiSemuaTukang),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Highlight ATM Withdrawal Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.08f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(AccentOrange.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Atm,
                                            contentDescription = null,
                                            tint = AccentOrange
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Total Uang yang Harus Diambil di ATM",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AccentOrange
                                        )
                                        Text(
                                            text = formatRupiah(totalGajiSemuaTukang),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentOrange
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG EKSPOR ---
    if (showExportDialog) {
        ExportPayrollDialog(
            weekRange = weekRange,
            projectName = selectedProyekName,
            rows = payrollList,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun ExportPayrollDialog(
    weekRange: Pair<String, String>,
    projectName: String,
    rows: List<TukangGajiRow>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Fungsi Share file Excel (CSV)
    fun shareCsv() {
        try {
            val fileName = "Laporan_Gaji_${projectName.replace(" ", "_")}_${weekRange.first}.csv"
            val file = File(context.cacheDir, fileName)
            val fileOutput = FileOutputStream(file)
            
            // Header baris CSV
            val csvContent = StringBuilder()
            csvContent.append("LAPORAN GAJI TUKANG MINGGUAN\n")
            csvContent.append("Proyek;${projectName}\n")
            csvContent.append("Periode;${weekRange.first} s/d ${weekRange.second}\n\n")
            csvContent.append("Nama Tukang;Upah Harian (Rp);Hari Hadir;Total Gaji (Rp)\n")
            
            rows.forEach { row ->
                csvContent.append("${row.tukang.nama};${row.tukang.upahHarian.toInt()};${row.hadirCount};${row.totalGaji.toInt()}\n")
            }
            
            val total = rows.sumOf { it.totalGaji }
            csvContent.append("\nTOTAL GAJI; ; ;${total.toInt()}\n")
            
            fileOutput.write(csvContent.toString().toByteArray())
            fileOutput.close()

            // Trigger Share Intent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Gaji Tukang Proyek $projectName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan CSV (Excel)"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fungsi Share file HTML (siap cetak PDF)
    fun shareHtmlPdf() {
        try {
            val fileName = "Laporan_Gaji_${projectName.replace(" ", "_")}_${weekRange.first}.html"
            val file = File(context.cacheDir, fileName)
            val fileOutput = FileOutputStream(file)

            val total = rows.sumOf { it.totalGaji }
            
            val htmlBuilder = StringBuilder()
            htmlBuilder.append("""
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: sans-serif; padding: 20px; color: #333; }
                        h2 { color: #0D47A1; text-transform: uppercase; margin-bottom: 5px; }
                        p { margin: 5px 0; font-size: 14px; }
                        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                        th { background-color: #F3F7FA; color: #0D47A1; }
                        tr:nth-child(even) { background-color: #f9f9f9; }
                        .total-row { font-weight: bold; background-color: #E65100; color: white; }
                        .summary { margin-top: 25px; padding: 15px; background-color: #FFF3E0; border: 1px solid #FFE0B2; border-radius: 8px; }
                    </style>
                </head>
                <body>
                    <h2>Laporan Penggajian Proyek</h2>
                    <p><strong>Proyek:</strong> $projectName</p>
                    <p><strong>Periode:</strong> ${DateUtils.formatIndonesianDateRange(weekRange.first, weekRange.second)}</p>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>Nama Pekerja</th>
                                <th>Upah Harian</th>
                                <th>Jumlah Kehadiran</th>
                                <th>Total Upah</th>
                            </tr>
                        </thead>
                        <tbody>
            """.trimIndent())

            rows.forEach { row ->
                htmlBuilder.append("""
                    <tr>
                        <td>${row.tukang.nama}</td>
                        <td>${formatRupiah(row.tukang.upahHarian)}</td>
                        <td>${row.hadirCount} hari</td>
                        <td><strong>${formatRupiah(row.totalGaji)}</strong></td>
                    </tr>
                """.trimIndent())
            }

            htmlBuilder.append("""
                        </tbody>
                    </table>
                    
                    <div class="summary">
                        <p style="font-size: 16px; margin: 0; color: #E65100;"><strong>Total Pengeluaran Gaji:</strong></p>
                        <p style="font-size: 24px; font-weight: bold; margin: 5px 0; color: #E65100;">${formatRupiah(total)}</p>
                        <p style="font-size: 13px; color: #666; margin: 0;">Rekomendasi penarikan cash di ATM untuk pembayaran lapangan.</p>
                    </div>
                </body>
                </html>
            """.trimIndent())

            fileOutput.write(htmlBuilder.toString().toByteArray())
            fileOutput.close()

            // Trigger Share Intent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Gaji HTML (PDF-Ready)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Cetak (PDF/HTML)"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Metode Ekspor", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ekspor laporan upah mingguan proyek '$projectName' ke format file berikut:")
                
                // Pilihan Excel / CSV
                Button(
                    onClick = {
                        shareCsv()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen)
                ) {
                    Icon(Icons.Default.TableView, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EKSPOR EXCEL (CSV)", fontWeight = FontWeight.Bold)
                }

                // Pilihan PDF / HTML
                Button(
                    onClick = {
                        shareHtmlPdf()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EKSPOR PDF / HTML CETAK", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("BATAL")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
