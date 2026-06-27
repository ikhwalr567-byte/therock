package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proyek")
data class Proyek(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val alamat: String,
    val tanggalMulai: String, // format YYYY-MM-DD
    val statusAktif: Boolean = true
)
