package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Proyek
import com.example.data.model.Tukang
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.CompletedGray
import com.example.ui.viewmodel.AppViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TukangScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val tukangList by viewModel.tukangList.collectAsState()
    val proyekList by viewModel.proyekList.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTukang by remember { mutableStateOf<Tukang?>(null) }
    var deletingTukang by remember { mutableStateOf<Tukang?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Tukang & Pekerja", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("TAMBAH TUKANG", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("add_tukang_fab")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (tukangList.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Tukang",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Ketuk tombol tambah untuk mendaftarkan pekerja atau tukang bangunan baru.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tukangList) { tukang ->
                        val assignedProyek = proyekList.find { it.id == tukang.proyekId }
                        TukangCard(
                            tukang = tukang,
                            proyek = assignedProyek,
                            onEdit = { editingTukang = tukang },
                            onDelete = { deletingTukang = tukang },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${tukang.noHp}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                    // Bottom spacing for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // --- ADD DIALOG ---
    if (showAddDialog) {
        TukangFormDialog(
            title = "Tambah Tukang Baru",
            proyekList = proyekList,
            onDismiss = { showAddDialog = false },
            onSave = { nama, noHp, alamat, upah, aktif, proyekId ->
                viewModel.addTukang(nama, noHp, alamat, upah, aktif, proyekId)
                showAddDialog = false
            }
        )
    }

    // --- EDIT DIALOG ---
    if (editingTukang != null) {
        TukangFormDialog(
            title = "Ubah Data Tukang",
            tukang = editingTukang,
            proyekList = proyekList,
            onDismiss = { editingTukang = null },
            onSave = { nama, noHp, alamat, upah, aktif, proyekId ->
                viewModel.editTukang(editingTukang!!.id, nama, noHp, alamat, upah, aktif, proyekId)
                editingTukang = null
            }
        )
    }

    // --- DELETE CONFIRMATION ---
    if (deletingTukang != null) {
        AlertDialog(
            onDismissRequest = { deletingTukang = null },
            title = { Text("Hapus Tukang?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus data pekerja '${deletingTukang!!.nama}'? Seluruh rekap kehadiran terkait akan tetap tersimpan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTukang(deletingTukang!!)
                        deletingTukang = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("HAPUS")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTukang = null }) {
                    Text("BATAL")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun TukangCard(
    tukang: Tukang,
    proyek: Proyek?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tukang.nama,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Upah: ${formatRupiah(tukang.upahHarian)} / hari",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Active Badge
                val badgeColor = if (tukang.statusAktif) ActiveGreen else CompletedGray
                val badgeText = if (tukang.statusAktif) "Aktif" else "Nonaktif"
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(10.dp))

            // Project Assignment Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (proyek != null) "Proyek: ${proyek.nama}" else "Belum Ditugaskan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (proyek != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Phone Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCall() }
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tukang.noHp,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Address Info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tukang.alamat,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Call quick button
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TELEPON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Tukang",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Tukang",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TukangFormDialog(
    title: String,
    tukang: Tukang? = null,
    proyekList: List<Proyek>,
    onDismiss: () -> Unit,
    onSave: (nama: String, noHp: String, alamat: String, upah: Double, aktif: Boolean, proyekId: Int?) -> Unit
) {
    var nama by remember { mutableStateOf(tukang?.nama ?: "") }
    var noHp by remember { mutableStateOf(tukang?.noHp ?: "") }
    var alamat by remember { mutableStateOf(tukang?.alamat ?: "") }
    var upahText by remember { mutableStateOf(tukang?.upahHarian?.toInt()?.toString() ?: "150000") }
    var aktif by remember { mutableStateOf(tukang?.statusAktif ?: true) }
    
    // Proyek dropdown selection
    var selectedProyekId by remember { mutableStateOf(tukang?.proyekId) }
    var expandedDropdown by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Tukang") },
                    placeholder = { Text("contoh: Budi Santoso") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = noHp,
                    onValueChange = { noHp = it },
                    label = { Text("Nomor HP") },
                    placeholder = { Text("contoh: 08123456789") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = upahText,
                    onValueChange = { upahText = it },
                    label = { Text("Upah Harian (Rp)") },
                    placeholder = { Text("contoh: 180000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = { Text("Alamat Tinggal") },
                    placeholder = { Text("contoh: Bantul, Yogyakarta") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown untuk memilih Proyek
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tugaskan ke Proyek:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    val selectedProyekName = proyekList.find { it.id == selectedProyekId }?.nama ?: "Belum Ditugaskan / Non-Proyek"
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                            .clickable { expandedDropdown = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedProyekName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Belum Ditugaskan") },
                                onClick = {
                                    selectedProyekId = null
                                    expandedDropdown = false
                                }
                            )
                            
                            proyekList.forEach { proyek ->
                                DropdownMenuItem(
                                    text = { Text(proyek.nama) },
                                    onClick = {
                                        selectedProyekId = proyek.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Status Toggle if editing
                if (tukang != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Status Tukang Aktif", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = aktif,
                            onCheckedChange = { aktif = it }
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val upahVal = upahText.toDoubleOrNull()
                    if (nama.isBlank() || noHp.isBlank() || alamat.isBlank() || upahVal == null) {
                        errorMsg = "Semua bidang harus diisi dengan benar!"
                    } else {
                        onSave(nama.trim(), noHp.trim(), alamat.trim(), upahVal, aktif, selectedProyekId)
                    }
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text("SIMPAN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("BATAL")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
