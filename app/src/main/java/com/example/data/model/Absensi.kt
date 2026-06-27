package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "absensi",
    indices = [
        Index(value = ["tukangId", "tanggal"], unique = true)
    ]
)
data class Absensi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tukangId: Int,
    val proyekId: Int,
    val tanggal: String, // format YYYY-MM-DD
    val hadir: Boolean
)
