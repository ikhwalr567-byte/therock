package com.example.data.dao

import androidx.room.*
import com.example.data.model.Absensi
import com.example.data.model.Proyek
import com.example.data.model.Tukang
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- PROYEK ---
    @Query("SELECT * FROM proyek ORDER BY statusAktif DESC, nama ASC")
    fun getAllProyek(): Flow<List<Proyek>>

    @Query("SELECT * FROM proyek WHERE id = :id")
    suspend fun getProyekById(id: Int): Proyek?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProyek(proyek: Proyek): Long

    @Delete
    suspend fun deleteProyek(proyek: Proyek)

    // --- TUKANG ---
    @Query("SELECT * FROM tukang ORDER BY statusAktif DESC, nama ASC")
    fun getAllTukang(): Flow<List<Tukang>>

    @Query("SELECT * FROM tukang WHERE id = :id")
    suspend fun getTukangById(id: Int): Tukang?

    @Query("SELECT * FROM tukang WHERE proyekId = :proyekId ORDER BY nama ASC")
    fun getTukangByProyek(proyekId: Int): Flow<List<Tukang>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTukang(tukang: Tukang): Long

    @Delete
    suspend fun deleteTukang(tukang: Tukang)

    // --- ABSENSI ---
    @Query("SELECT * FROM absensi WHERE tanggal = :tanggal")
    fun getAllAbsensiByDate(tanggal: String): Flow<List<Absensi>>

    @Query("SELECT * FROM absensi WHERE tanggal = :tanggal AND proyekId = :proyekId")
    fun getAbsensiByDateAndProyek(tanggal: String, proyekId: Int): Flow<List<Absensi>>

    @Query("SELECT * FROM absensi WHERE tanggal BETWEEN :startDate AND :endDate")
    fun getAbsensiByDateRange(startDate: String, endDate: String): Flow<List<Absensi>>

    @Query("SELECT * FROM absensi WHERE proyekId = :proyekId AND tanggal BETWEEN :startDate AND :endDate")
    fun getAbsensiByDateRangeAndProyek(startDate: String, endDate: String, proyekId: Int): Flow<List<Absensi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsensi(absensi: Absensi)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsensiList(absensiList: List<Absensi>)

    @Query("DELETE FROM absensi WHERE tukangId = :tukangId AND tanggal = :tanggal")
    suspend fun deleteAbsensi(tukangId: Int, tanggal: String)
}
