package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tukang")
data class Tukang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val noHp: String,
    val alamat: String,
    val upahHarian: Double,
    val statusAktif: Boolean = true,
    val proyekId: Int? = null // ID Proyek yang sedang diikuti, null jika tidak ada
)
