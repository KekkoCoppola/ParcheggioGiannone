package com.giannone.parcheggio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giannone.parcheggio.data.firebase.FirebaseRepository
import com.giannone.parcheggio.data.model.Configurazione
import com.giannone.parcheggio.data.model.Piano
import com.giannone.parcheggio.data.model.Prenotazione
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

class ParcheggioViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // ─── Selected Date ──────────────────────────────────────────────
    private val _selectedDate = MutableStateFlow(todayString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // ─── Prenotazioni del giorno ─────────────────────────────────────
    private val _prenotazioni = MutableStateFlow<List<Prenotazione>>(emptyList())
    val prenotazioni: StateFlow<List<Prenotazione>> = _prenotazioni.asStateFlow()

    // ─── Auto attualmente parcheggiate (ingresso senza uscita) ──────
    private val _autoAttive = MutableStateFlow<List<Prenotazione>>(emptyList())
    val autoAttive: StateFlow<List<Prenotazione>> = _autoAttive.asStateFlow()

    // ─── Piani ──────────────────────────────────────────────────────
    private val _piani = MutableStateFlow<List<Piano>>(emptyList())
    val piani: StateFlow<List<Piano>> = _piani.asStateFlow()

    // ─── Configurazione ─────────────────────────────────────────────
    private val _config = MutableStateFlow(Configurazione())
    val config: StateFlow<Configurazione> = _config.asStateFlow()

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

    init {
        loadPrenotazioniPerData(_selectedDate.value)
        loadAutoAttive()
        loadPiani()
        loadConfig()
    }

    // ─── Date navigation ─────────────────────────────────────────────
    fun goToPreviousDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        cal.time = sdf.parse(_selectedDate.value) ?: Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val newDate = sdf.format(cal.time)
        _selectedDate.value = newDate
        loadPrenotazioniPerData(newDate)
    }

    fun goToNextDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        cal.time = sdf.parse(_selectedDate.value) ?: Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val newDate = sdf.format(cal.time)
        _selectedDate.value = newDate
        loadPrenotazioniPerData(newDate)
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
        viewModelScope.launch {
            repository.getPrenotazioniPerData(data).collect {
                _prenotazioni.value = it
            }
        }
    }

    private fun loadAutoAttive() {
        viewModelScope.launch {
            repository.getAllPrenotazioniAttive().collect {
                _autoAttive.value = it
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

    // ─── Actions ─────────────────────────────────────────────────────
    fun addPrenotazione(prenotazione: Prenotazione) {
        viewModelScope.launch {
            _isLoading.value = true
            try { repository.addPrenotazione(prenotazione) }
            catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun registraIngresso(id: String) {
        viewModelScope.launch {
            try { repository.registraIngresso(id) }
            catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun registraUscita(prenotazione: Prenotazione, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val tariffa = when (prenotazione.tipoTariffa) {
                    "Tariffa Giornaliera" -> _config.value.tariffaGiornaliera
                    "Tariffa Notturna"    -> _config.value.tariffaNotturna
                    "Tariffa Eventi"      -> _config.value.tariffaEventi
                    else                  -> _config.value.tariffaOraria
                }
                val (ingresso, uscita, totale) = repository.registraUscita(prenotazione.id, tariffa)
                val oreDouble = (uscita.seconds - ingresso.seconds) / 3600.0
                _resocontoState.value = ResocontoState(
                    nomeCliente = "${prenotazione.nome} ${prenotazione.cognome}",
                    targa = prenotazione.targa,
                    piano = prenotazione.piano,
                    timestampIngresso = ingresso,
                    timestampUscita = uscita,
                    totaleOre = maxOf(oreDouble, 0.0),
                    tipoTariffa = prenotazione.tipoTariffa,
                    totalePagato = totale
                )
                onComplete()
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

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

    // ─── Helpers ─────────────────────────────────────────────────────
    fun displayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = todayString()
        return when (_selectedDate.value) {
            today -> "Oggi"
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
