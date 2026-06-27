package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.CompletedGray
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.DateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProyekScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val proyekList by viewModel.proyekList.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProyek by remember { mutableStateOf<Proyek?>(null) }
    var deletingProyek by remember { mutableStateOf<Proyek?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Proyek Bangunan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("TAMBAH PROYEK", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("add_proyek_fab")
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
            if (proyekList.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BusinessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Proyek",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Ketuk tombol tambah untuk mendaftarkan proyek konstruksi baru.",
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
                    items(proyekList) { proyek ->
                        ProyekCard(
                            proyek = proyek,
                            onEdit = { editingProyek = proyek },
                            onDelete = { deletingProyek = proyek }
                        )
                    }
                    // Padding at the bottom so the FAB doesn't cover items
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // --- ADD DIALOG ---
    if (showAddDialog) {
        ProyekFormDialog(
            title = "Tambah Proyek Baru",
            onDismiss = { showAddDialog = false },
            onSave = { nama, alamat, tglMulai, aktif ->
                viewModel.addProyek(nama, alamat, tglMulai, aktif)
                showAddDialog = false
            }
        )
    }

    // --- EDIT DIALOG ---
    if (editingProyek != null) {
        ProyekFormDialog(
            title = "Ubah Data Proyek",
            proyek = editingProyek,
            onDismiss = { editingProyek = null },
            onSave = { nama, alamat, tglMulai, aktif ->
                viewModel.editProyek(editingProyek!!.id, nama, alamat, tglMulai, aktif)
                editingProyek = null
            }
        )
    }

    // --- DELETE CONFIRMATION ---
    if (deletingProyek != null) {
        AlertDialog(
            onDismissRequest = { deletingProyek = null },
            title = { Text("Hapus Proyek?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus proyek '${deletingProyek!!.nama}'? Data absensi terkait juga mungkin akan terpengaruh.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProyek(deletingProyek!!)
                        deletingProyek = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("HAPUS")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProyek = null }) {
                    Text("BATAL")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ProyekCard(
    proyek: Proyek,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
                Text(
                    text = proyek.nama.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                // Status Badge
                val badgeColor = if (proyek.statusAktif) ActiveGreen else CompletedGray
                val badgeText = if (proyek.statusAktif) "Aktif" else "Selesai"
                
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = proyek.alamat,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mulai: ${DateUtils.formatToDisplay(proyek.tanggalMulai)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Proyek",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Proyek",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ProyekFormDialog(
    title: String,
    proyek: Proyek? = null,
    onDismiss: () -> Unit,
    onSave: (nama: String, alamat: String, tglMulai: String, aktif: Boolean) -> Unit
) {
    var nama by remember { mutableStateOf(proyek?.nama ?: "") }
    var alamat by remember { mutableStateOf(proyek?.alamat ?: "") }
    var tglMulai by remember { mutableStateOf(proyek?.tanggalMulai ?: DateUtils.getTodayString()) }
    var aktif by remember { mutableStateOf(proyek?.statusAktif ?: true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // DatePickerDialog launcher helper
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Format to YYYY-MM-DD
            val mStr = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val dStr = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            tglMulai = "$year-$mStr-$dStr"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
                    label = { Text("Nama Proyek") },
                    placeholder = { Text("contoh: Renovasi Ruko Sudirman") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = { Text("Alamat Proyek") },
                    placeholder = { Text("contoh: Jl. Sudirman No. 45") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date selection field
                OutlinedTextField(
                    value = DateUtils.formatToDisplay(tglMulai),
                    onValueChange = {},
                    label = { Text("Tanggal Mulai") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )

                // Status Toggle if editing
                if (proyek != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Status Proyek Aktif", fontWeight = FontWeight.Medium)
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
                    if (nama.isBlank() || alamat.isBlank()) {
                        errorMsg = "Nama proyek dan alamat harus diisi!"
                    } else {
                        onSave(nama.trim(), alamat.trim(), tglMulai, aktif)
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
