package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.DateUtils
import java.text.NumberFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val todayStr = remember { DateUtils.formatToDisplay(DateUtils.getTodayString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Welcome Header Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Halo Mandor Proyek!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Kelola proyek, absensi, dan upah dengan mudah.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = todayStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Text(
            text = "IKHTISAR KINERJA MINGGU INI",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        // Metrics Grid/Columns (Responsive layout helper based on width)
        BoxWithConstraints {
            val columns = if (maxWidth > 600.dp) 2 else 1
            
            if (columns == 1) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DashboardMetricCard(
                        title = "Jumlah Proyek Aktif",
                        value = "${metrics.proyekAktifCount} Proyek",
                        icon = Icons.Default.Business,
                        iconTint = MaterialTheme.colorScheme.primary,
                        bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                    
                    DashboardMetricCard(
                        title = "Jumlah Tukang",
                        value = "${metrics.tukangCount} Orang",
                        icon = Icons.Default.People,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        bgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    )

                    DashboardMetricCard(
                        title = "Total Hadir Hari Ini",
                        value = "${metrics.hadirHariIniCount} Hadir",
                        icon = Icons.Default.HowToReg,
                        iconTint = ActiveGreen,
                        bgColor = ActiveGreen.copy(alpha = 0.08f)
                    )

                    DashboardMetricCard(
                        title = "Total Gaji Minggu Ini",
                        value = formatRupiah(metrics.totalGajiMingguIni),
                        icon = Icons.Default.Payments,
                        iconTint = MaterialTheme.colorScheme.primary,
                        bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )

                    // Card Special: ATM withdrawal recommendation
                    DashboardMetricCard(
                        title = "Ambil Cash di ATM (Minggu Ini)",
                        value = formatRupiah(metrics.totalGajiMingguIni),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = AccentOrange,
                        bgColor = AccentOrange.copy(alpha = 0.1f),
                        isHighlight = true
                    )
                }
            } else {
                // Wide Grid Layout for Tablet/Laptop
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DashboardMetricCard(
                                title = "Jumlah Proyek Aktif",
                                value = "${metrics.proyekAktifCount} Proyek",
                                icon = Icons.Default.Business,
                                iconTint = MaterialTheme.colorScheme.primary,
                                bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DashboardMetricCard(
                                title = "Jumlah Tukang",
                                value = "${metrics.tukangCount} Orang",
                                icon = Icons.Default.People,
                                iconTint = MaterialTheme.colorScheme.secondary,
                                bgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DashboardMetricCard(
                                title = "Total Hadir Hari Ini",
                                value = "${metrics.hadirHariIniCount} Hadir",
                                icon = Icons.Default.HowToReg,
                                iconTint = ActiveGreen,
                                bgColor = ActiveGreen.copy(alpha = 0.08f)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DashboardMetricCard(
                                title = "Total Gaji Minggu Ini",
                                value = formatRupiah(metrics.totalGajiMingguIni),
                                icon = Icons.Default.Payments,
                                iconTint = MaterialTheme.colorScheme.primary,
                                bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        }
                    }
                    
                    DashboardMetricCard(
                        title = "Ambil Cash di ATM (Minggu Ini)",
                        value = formatRupiah(metrics.totalGajiMingguIni),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = AccentOrange,
                        bgColor = AccentOrange.copy(alpha = 0.1f),
                        isHighlight = true
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) Color.White else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlight) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isHighlight) BorderStroke(2.dp, AccentOrange) else null,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    fontSize = if (isHighlight) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlight) AccentOrange else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}
