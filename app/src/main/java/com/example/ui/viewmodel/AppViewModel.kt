package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Absensi
import com.example.data.model.Proyek
import com.example.data.model.Tukang
import com.example.data.repository.AppRepository
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // --- FITUR LOGIN ---
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    fun loginAdmin(pin: String): Boolean {
        // Mendukung PIN '1234' atau 'admin' sebagai kata sandi
        val success = pin == "1234" || pin.lowercase() == "admin"
        if (success) {
            _isAdminLoggedIn.value = true
        }
        return success
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    // --- DATA STREAMS ---
    val proyekList: StateFlow<List<Proyek>> = repository.allProyek
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tukangList: StateFlow<List<Tukang>> = repository.allTukang
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DASHBOARD METRICS ---
    private val _dashboardMetrics = MutableStateFlow(DashboardMetrics())
    val dashboardMetrics: StateFlow<DashboardMetrics> = _dashboardMetrics.asStateFlow()

    // --- ABSENSI STATE ---
    private val _selectedAbsensiProyekId = MutableStateFlow<Int?>(null)
    val selectedAbsensiProyekId: StateFlow<Int?> = _selectedAbsensiProyekId.asStateFlow()

    private val _selectedAbsensiDate = MutableStateFlow(DateUtils.getTodayString())
    val selectedAbsensiDate: StateFlow<String> = _selectedAbsensiDate.asStateFlow()

    private val _currentAbsensiMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val currentAbsensiMap: StateFlow<Map<Int, Boolean>> = _currentAbsensiMap.asStateFlow()

    // --- REKAP & PENGGAJIAN FILTER ---
    private val _selectedFilterProyekId = MutableStateFlow<Int?>(null)
    val selectedFilterProyekId: StateFlow<Int?> = _selectedFilterProyekId.asStateFlow()

    private val _gajiWeekRange = MutableStateFlow(DateUtils.getCurrentWeekRange())
    val gajiWeekRange: StateFlow<Pair<String, String>> = _gajiWeekRange.asStateFlow()

    init {
        // Sinkronisasi metrik dashboard setiap kali daftar proyek, tukang, atau absensi berubah
        viewModelScope.launch {
            combine(
                proyekList,
                tukangList,
                repository.getAllAbsensiByDate(DateUtils.getTodayString()),
                // Absensi minggu ini
                gajiWeekRange.flatMapLatest { range ->
                    repository.getAbsensiByDateRange(range.first, range.second)
                }
            ) { proyeks, tukangs, absensiHariIni, absensiMinggu ->
                val proyekAktifCount = proyeks.count { it.statusAktif }
                val tukangCount = tukangs.size
                val hadirHariIniCount = absensiHariIni.count { it.hadir }

                // Hitung total gaji minggu ini
                var totalGajiMingguIni = 0.0
                absensiMinggu.filter { it.hadir }.forEach { abs ->
                    val t = tukangs.find { it.id == abs.tukangId }
                    if (t != null) {
                        totalGajiMingguIni += t.upahHarian
                    }
                }

                DashboardMetrics(
                    proyekAktifCount = proyekAktifCount,
                    tukangCount = tukangCount,
                    hadirHariIniCount = hadirHariIniCount,
                    totalGajiMingguIni = totalGajiMingguIni
                )
            }.collect {
                _dashboardMetrics.value = it
            }
        }

        // Auto select first project for attendance when project list loads
        viewModelScope.launch {
            proyekList.collect { list ->
                if (_selectedAbsensiProyekId.value == null && list.isNotEmpty()) {
                    _selectedAbsensiProyekId.value = list.firstOrNull { it.statusAktif }?.id ?: list.firstOrNull()?.id
                }
            }
        }

        // Sinkronisasi absensi map saat proyek absensi atau tanggal berubah
        viewModelScope.launch {
            combine(
                selectedAbsensiProyekId,
                selectedAbsensiDate
            ) { proyekId, tanggal ->
                Pair(proyekId, tanggal)
            }.flatMapLatest { (proyekId, tanggal) ->
                if (proyekId != null) {
                    repository.getAbsensiByDateAndProyek(tanggal, proyekId)
                } else {
                    flowOf(emptyList())
                }
            }.collect { absensiList ->
                val map = absensiList.associate { it.tukangId to it.hadir }
                _currentAbsensiMap.value = map
            }
        }
    }

    // --- PROYEK CRUD ACTIONS ---
    fun addProyek(nama: String, alamat: String, tanggalMulai: String, statusAktif: Boolean) {
        viewModelScope.launch {
            repository.insertProyek(
                Proyek(
                    nama = nama,
                    alamat = alamat,
                    tanggalMulai = tanggalMulai,
                    statusAktif = statusAktif
                )
            )
        }
    }

    fun editProyek(id: Int, nama: String, alamat: String, tanggalMulai: String, statusAktif: Boolean) {
        viewModelScope.launch {
            repository.insertProyek(
                Proyek(
                    id = id,
                    nama = nama,
                    alamat = alamat,
                    tanggalMulai = tanggalMulai,
                    statusAktif = statusAktif
                )
            )
        }
    }

    fun deleteProyek(proyek: Proyek) {
        viewModelScope.launch {
            repository.deleteProyek(proyek)
        }
    }

    // --- TUKANG CRUD ACTIONS ---
    fun addTukang(nama: String, noHp: String, alamat: String, upahHarian: Double, statusAktif: Boolean, proyekId: Int?) {
        viewModelScope.launch {
            repository.insertTukang(
                Tukang(
                    nama = nama,
                    noHp = noHp,
                    alamat = alamat,
                    upahHarian = upahHarian,
                    statusAktif = statusAktif,
                    proyekId = proyekId
                )
            )
        }
    }

    fun editTukang(id: Int, nama: String, noHp: String, alamat: String, upahHarian: Double, statusAktif: Boolean, proyekId: Int?) {
        viewModelScope.launch {
            repository.insertTukang(
                Tukang(
                    id = id,
                    nama = nama,
                    noHp = noHp,
                    alamat = alamat,
                    upahHarian = upahHarian,
                    statusAktif = statusAktif,
                    proyekId = proyekId
                )
            )
        }
    }

    fun deleteTukang(tukang: Tukang) {
        viewModelScope.launch {
            repository.deleteTukang(tukang)
        }
    }

    // --- ABSENSI ACTIONS ---
    fun setAbsensiProyek(proyekId: Int) {
        _selectedAbsensiProyekId.value = proyekId
    }

    fun setAbsensiDate(tanggal: String) {
        _selectedAbsensiDate.value = tanggal
    }

    fun toggleKehadiran(tukangId: Int, hadir: Boolean) {
        val current = _currentAbsensiMap.value.toMutableMap()
        current[tukangId] = hadir
        _currentAbsensiMap.value = current
    }

    fun simpanAbsensi() {
        val proyekId = _selectedAbsensiProyekId.value ?: return
        val tanggal = _selectedAbsensiDate.value
        val map = _currentAbsensiMap.value

        viewModelScope.launch {
            // Ambil daftar tukang di proyek ini yang aktif atau terdaftar
            repository.getTukangByProyek(proyekId).first().forEach { tk ->
                val isHadir = map[tk.id] ?: false
                repository.insertAbsensi(
                    Absensi(
                        tukangId = tk.id,
                        proyekId = proyekId,
                        tanggal = tanggal,
                        hadir = isHadir
                    )
                )
            }
        }
    }

    // --- PENGGAJIAN & REKAP ---
    fun setFilterProyek(proyekId: Int?) {
        _selectedFilterProyekId.value = proyekId
    }

    fun setGajiWeekRange(start: String, end: String) {
        _gajiWeekRange.value = Pair(start, end)
    }

    /**
     * Memperoleh daftar kehadiran tukang dan upah untuk penggajian periode tertentu.
     */
    fun getPenggajianList(startDate: String, endDate: String, filterProyekId: Int?): Flow<List<TukangGajiRow>> {
        val absensiFlow = if (filterProyekId != null) {
            repository.getAbsensiByDateRangeAndProyek(startDate, endDate, filterProyekId)
        } else {
            repository.getAbsensiByDateRange(startDate, endDate)
        }

        return combine(tukangList, absensiFlow) { tukangs, absens ->
            tukangs.filter { tk ->
                // Filter berdasarkan proyek jika diset
                filterProyekId == null || tk.proyekId == filterProyekId
            }.map { tk ->
                val tkAbsensi = absens.filter { it.tukangId == tk.id }
                val hadirCount = tkAbsensi.count { it.hadir }
                val totalGaji = hadirCount * tk.upahHarian
                TukangGajiRow(
                    tukang = tk,
                    hadirCount = hadirCount,
                    totalGaji = totalGaji
                )
            }
        }
    }

    /**
     * Memperoleh rekap kehadiran harian.
     */
    fun getRekapHarian(tanggal: String, filterProyekId: Int?): Flow<List<RekapRow>> {
        val absensiFlow = if (filterProyekId != null) {
            repository.getAbsensiByDateAndProyek(tanggal, filterProyekId)
        } else {
            repository.getAllAbsensiByDate(tanggal)
        }

        return combine(tukangList, absensiFlow) { tukangs, absens ->
            absens.mapNotNull { abs ->
                val tk = tukangs.find { it.id == abs.tukangId }
                if (tk != null && (filterProyekId == null || tk.proyekId == filterProyekId)) {
                    RekapRow(
                        namaTukang = tk.nama,
                        proyekNama = tk.proyekId?.let { pid -> "Proyek ID $pid" } ?: "Tidak ada", // can display project name
                        statusHadir = if (abs.hadir) "Hadir" else "Sakit/Alpha",
                        upah = if (abs.hadir) tk.upahHarian else 0.0
                    )
                } else null
            }
        }
    }
}

// --- DTO CLASSES ---
data class DashboardMetrics(
    val proyekAktifCount: Int = 0,
    val tukangCount: Int = 0,
    val hadirHariIniCount: Int = 0,
    val totalGajiMingguIni: Double = 0.0
)

data class TukangGajiRow(
    val tukang: Tukang,
    val hadirCount: Int,
    val totalGaji: Double
)

data class RekapRow(
    val namaTukang: String,
    val proyekNama: String,
    val statusHadir: String,
    val upah: Double
)

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
