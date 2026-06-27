package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.Absensi
import com.example.data.model.Proyek
import com.example.data.model.Tukang
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- PROYEK ---
    val allProyek: Flow<List<Proyek>> = appDao.getAllProyek()

    suspend fun getProyekById(id: Int): Proyek? = appDao.getProyekById(id)

    suspend fun insertProyek(proyek: Proyek): Long = appDao.insertProyek(proyek)

    suspend fun deleteProyek(proyek: Proyek) = appDao.deleteProyek(proyek)

    // --- TUKANG ---
    val allTukang: Flow<List<Tukang>> = appDao.getAllTukang()

    suspend fun getTukangById(id: Int): Tukang? = appDao.getTukangById(id)

    fun getTukangByProyek(proyekId: Int): Flow<List<Tukang>> = appDao.getTukangByProyek(proyekId)

    suspend fun insertTukang(tukang: Tukang): Long = appDao.insertTukang(tukang)

    suspend fun deleteTukang(tukang: Tukang) = appDao.deleteTukang(tukang)

    // --- ABSENSI ---
    fun getAllAbsensiByDate(tanggal: String): Flow<List<Absensi>> = appDao.getAllAbsensiByDate(tanggal)

    fun getAbsensiByDateAndProyek(tanggal: String, proyekId: Int): Flow<List<Absensi>> =
        appDao.getAbsensiByDateAndProyek(tanggal, proyekId)

    fun getAbsensiByDateRange(startDate: String, endDate: String): Flow<List<Absensi>> =
        appDao.getAbsensiByDateRange(startDate, endDate)

    fun getAbsensiByDateRangeAndProyek(startDate: String, endDate: String, proyekId: Int): Flow<List<Absensi>> =
        appDao.getAbsensiByDateRangeAndProyek(startDate, endDate, proyekId)

    suspend fun insertAbsensi(absensi: Absensi) = appDao.insertAbsensi(absensi)

    suspend fun insertAbsensiList(absensiList: List<Absensi>) = appDao.insertAbsensiList(absensiList)

    suspend fun deleteAbsensi(tukangId: Int, tanggal: String) = appDao.deleteAbsensi(tukangId, tanggal)
}
