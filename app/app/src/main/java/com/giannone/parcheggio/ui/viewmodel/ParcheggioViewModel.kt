package com.giannone.parcheggio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giannone.parcheggio.data.firebase.FirebaseRepository
import com.giannone.parcheggio.data.model.Configurazione
import com.giannone.parcheggio.data.model.Piano
import com.giannone.parcheggio.data.model.Prenotazione
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ResocontoState(
    val nomeCliente: String = "",
    val targa: String = "",
    val piano: String = "",
    val timestampIngresso: Timestamp? = null,
    val timestampUscita: Timestamp? = null,
    val totaleOre: Double = 0.0,
    val tipoTariffa: String = "",
    val totalePagato: Double = 0.0
)

@OptIn(FlowPreview::class)
class ParcheggioViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // ─── Selected Date ──────────────────────────────────────────────
    private val _selectedDate = MutableStateFlow(todayString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // ─── Prenotazioni del giorno ─────────────────────────────────────
    private val _prenotazioni = MutableStateFlow<List<Prenotazione>>(emptyList())
    val prenotazioni: StateFlow<List<Prenotazione>> = _prenotazioni.asStateFlow()

    // ─── Auto attualmente parcheggiate ──────────────────────────────
    private val _autoAttive = MutableStateFlow<List<Prenotazione>>(emptyList())
    val autoAttive: StateFlow<List<Prenotazione>> = _autoAttive.asStateFlow()

    // ─── Piani ──────────────────────────────────────────────────────
    private val _piani = MutableStateFlow<List<Piano>>(emptyList())
    val piani: StateFlow<List<Piano>> = _piani.asStateFlow()

    // ─── Configurazione ─────────────────────────────────────────────
    private val _config = MutableStateFlow(Configurazione())
    val config: StateFlow<Configurazione> = _config.asStateFlow()

    private val _prenotazioniLast7Days = MutableStateFlow<List<Prenotazione>>(emptyList())
    val prenotazioniLast7Days: StateFlow<List<Prenotazione>> = _prenotazioniLast7Days.asStateFlow()

    // ─── Resoconto uscita ────────────────────────────────────────────
    private val _resocontoState = MutableStateFlow(ResocontoState())
    val resocontoState: StateFlow<ResocontoState> = _resocontoState.asStateFlow()

    // ─── Loading & Error ────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ─── Search query ────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Job per il listener prenotazioni (cancellabile quando cambia data)
    private var prenotazioniJob: Job? = null

    init {
        // Collect selectedDate changes with a debounce to prevent listener spam and UI locks
        viewModelScope.launch {
            _selectedDate
                .debounce(300)
                .collect { date ->
                    loadPrenotazioniPerData(date)
                }
        }
        loadAutoAttive()
        loadPiani()
        loadConfig()
        loadIncassiLast7Days()
    }

    // ─── Date navigation ─────────────────────────────────────────────
    fun goToPreviousDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(_selectedDate.value) ?: Date()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        setSelectedDate(sdf.format(cal.time))
    }

    fun goToNextDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(_selectedDate.value) ?: Date()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        setSelectedDate(sdf.format(cal.time))
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun filteredPrenotazioni(): List<Prenotazione> {
        val q = _searchQuery.value.trim().lowercase()
        return if (q.isEmpty()) _prenotazioni.value
        else _prenotazioni.value.filter {
            it.nome.lowercase().contains(q) ||
            it.cognome.lowercase().contains(q) ||
            it.targa.lowercase().contains(q)
        }
    }

    // ─── Loaders ─────────────────────────────────────────────────────
    private fun loadPrenotazioniPerData(data: String) {
        prenotazioniJob?.cancel()
        prenotazioniJob = viewModelScope.launch {
            repository.getPrenotazioniPerData(data).collect {
                _prenotazioni.value = it
            }
        }
    }

    private fun loadAutoAttive() {
        viewModelScope.launch {
            try {
                repository.getAllPrenotazioniAttive().collect { list ->
                    _autoAttive.value = list
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore nel caricamento auto attive: ${e.message}"
            }
        }
    }

    private fun loadPiani() {
        viewModelScope.launch {
            repository.getPiani().collect { _piani.value = it }
        }
    }

    private fun loadConfig() {
        viewModelScope.launch {
            repository.getConfigurazione().collect { _config.value = it }
        }
    }

    fun getLast7Days(): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun loadIncassiLast7Days() {
        viewModelScope.launch {
            repository.getPrenotazioniPerDate(getLast7Days()).collect { list ->
                _prenotazioniLast7Days.value = list
            }
        }
    }

    // ─── Actions ─────────────────────────────────────────────────────
    fun addPrenotazione(prenotazione: Prenotazione) {
        viewModelScope.launch {
            _isLoading.value = true
            try { repository.addPrenotazione(prenotazione) }
            catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun deletePrenotazione(id: String) {
        viewModelScope.launch {
            try { repository.deletePrenotazione(id) }
            catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun registraIngresso(id: String) {
        viewModelScope.launch {
            try { repository.registraIngresso(id) }
            catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun registraIngressoManuale(id: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                repository.registraIngressoConTimestamp(id, Timestamp(cal.time))
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun registraUscita(prenotazione: Prenotazione, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val tariffa = getTariffa(prenotazione.tipoTariffa)
                val (ingresso, uscita, totale) = repository.registraUscita(prenotazione.id, tariffa)
                val oreDouble = (uscita.seconds - ingresso.seconds) / 3600.0
                _resocontoState.value = buildResoconto(prenotazione, ingresso, uscita, maxOf(oreDouble, 0.0), totale)
                onComplete()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun registraUscitaManuale(prenotazione: Prenotazione, hour: Int, minute: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                val uscitaTimestamp = Timestamp(cal.time)
                val tariffa = getTariffa(prenotazione.tipoTariffa)
                val (ingresso, uscita, totale) = repository.registraUscitaConTimestamp(
                    id = prenotazione.id,
                    uscita = uscitaTimestamp,
                    tariffa = tariffa,
                    ingressoOverride = prenotazione.timestampIngresso
                )
                val oreDouble = (uscita.seconds - ingresso.seconds) / 3600.0
                _resocontoState.value = buildResoconto(prenotazione, ingresso, uscita, maxOf(oreDouble, 0.0), totale)
                onComplete()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    // ─── Helpers privati ─────────────────────────────────────────────
    private fun getTariffa(tipo: String): Double = when (tipo) {
        "Tariffa Giornaliera" -> _config.value.tariffaGiornaliera
        "Tariffa Notturna"    -> _config.value.tariffaNotturna
        "Tariffa Eventi"      -> _config.value.tariffaEventi
        else                  -> _config.value.tariffaOraria
    }

    private fun buildResoconto(
        prenotazione: Prenotazione,
        ingresso: Timestamp,
        uscita: Timestamp,
        ore: Double,
        totale: Double
    ) = ResocontoState(
        nomeCliente = "${prenotazione.nome} ${prenotazione.cognome}",
        targa = prenotazione.targa,
        piano = prenotazione.piano,
        timestampIngresso = ingresso,
        timestampUscita = uscita,
        totaleOre = ore,
        tipoTariffa = prenotazione.tipoTariffa,
        totalePagato = totale
    )

    // ─── Piani CRUD ──────────────────────────────────────────────────
    fun addPiano(nome: String, posti: Int) {
        viewModelScope.launch {
            try { repository.addPiano(Piano(nome = nome, postiTotali = posti)) }
            catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun updatePiano(piano: Piano) {
        viewModelScope.launch {
            try { repository.updatePiano(piano) }
            catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun deletePiano(id: String) {
        viewModelScope.launch {
            try { repository.deletePiano(id) }
            catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    // ─── Config ──────────────────────────────────────────────────────
    fun saveConfig(config: Configurazione) {
        viewModelScope.launch {
            try { repository.saveConfigurazione(config) }
            catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun clearError() { _errorMessage.value = null }

    // ─── Public helpers ──────────────────────────────────────────────
    fun displayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return when (_selectedDate.value) {
            todayString() -> "Oggi"
            else -> {
                val d = sdf.parse(_selectedDate.value) ?: Date()
                SimpleDateFormat("dd MMM yyyy", Locale.ITALIAN).format(d)
            }
        }
    }

    fun formatTimestamp(ts: Timestamp?): String {
        if (ts == null) return "--:--"
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(ts.toDate())
    }

    fun formatTimestampFull(ts: Timestamp?): String {
        if (ts == null) return "--"
        return SimpleDateFormat("dd MMM 'alle' HH:mm", Locale.ITALIAN).format(ts.toDate())
    }

    fun autoPerPiano(nomePiano: String): List<Prenotazione> =
        _autoAttive.value.filter { it.piano == nomePiano }

    fun postiLiberiPerPiano(piano: Piano): Int {
        val occupati = autoPerPiano(piano.nome).size
        return maxOf(piano.postiTotali - occupati, 0)
    }

    fun totaleAutoParcheggiate(): Int = _autoAttive.value.size

    fun totalePossiLiberi(): Int = _piani.value.sumOf { postiLiberiPerPiano(it) }
}

private fun todayString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
