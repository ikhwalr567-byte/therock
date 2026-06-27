package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi Database Room & Repositori secara lokal
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao())
        val viewModelFactory = AppViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                // Instansiasi ViewModel menggunakan Factory kita
                val appViewModel: AppViewModel = viewModel(factory = viewModelFactory)
                val isLoggedIn by appViewModel.isAdminLoggedIn.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn) {
                        // Jika belum login, tampilkan layar PIN login admin
                        LoginScreen(viewModel = appViewModel, modifier = Modifier.safeDrawingPadding())
                    } else {
                        // Jika sudah login, tampilkan panel utama responsif absensi & gaji
                        MainAppContent(viewModel = appViewModel)
                    }
                }
            }
        }
    }
}

// Representasi Tab menu
enum class AppTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Beranda", Icons.Default.Dashboard, "tab_dashboard"),
    ABSENSI("Absensi", Icons.Default.HowToReg, "tab_absensi"),
    PENGGAJIAN("Wajib ATM", Icons.Default.Payments, "tab_gaji"),
    REKAP("Rekap", Icons.Default.Assignment, "tab_rekap"),
    PROYEK("Proyek", Icons.Default.Business, "tab_proyek"),
    TUKANG("Tukang", Icons.Default.People, "tab_tukang")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Layout Laptop/Tablet: Navigation Rail di kiri, konten di kanan
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "MANDOR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    AppTab.values().forEach { tab ->
                        NavigationRailItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Tombol Keluar di bagian bawah rel navigasi
                    NavigationRailItem(
                        selected = false,
                        onClick = { showLogoutConfirm = true },
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = MaterialTheme.colorScheme.error) },
                        label = { Text("Keluar", color = MaterialTheme.colorScheme.error, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_logout_button")
                    )
                }

                VerticalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

                // Konten Kanan Utama
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RenderTabContent(
                        tab = selectedTab,
                        viewModel = viewModel,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                    )
                }
            }
        } else {
            // Layout HP: Scaffold standar dengan Bottom Navigation
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ABSENSI & GAJI",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showLogoutConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Keluar Log",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.navigationBarsPadding() // Hindari overlap gesture bar Android
                    ) {
                        AppTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars) // Notch safe area
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    RenderTabContent(
                        tab = selectedTab,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Dialog Konfirmasi Keluar Admin
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Keluar Sesi Mandor?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar dari sesi admin mandor? Anda harus memasukkan PIN kembali untuk masuk.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logoutAdmin()
                        showLogoutConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("KELUAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("BATAL")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RenderTabContent(
    tab: AppTab,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    when (tab) {
        AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel, modifier = modifier)
        AppTab.ABSENSI -> AbsensiScreen(viewModel = viewModel, modifier = modifier)
        AppTab.PENGGAJIAN -> PenggajianScreen(viewModel = viewModel, modifier = modifier)
        AppTab.REKAP -> RekapScreen(viewModel = viewModel, modifier = modifier)
        AppTab.PROYEK -> ProyekScreen(viewModel = viewModel, modifier = modifier)
        AppTab.TUKANG -> TukangScreen(viewModel = viewModel, modifier = modifier)
    }
}
