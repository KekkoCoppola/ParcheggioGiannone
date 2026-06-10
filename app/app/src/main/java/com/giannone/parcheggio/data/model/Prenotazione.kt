package com.giannone.parcheggio.data.model

import com.google.firebase.Timestamp

data class Prenotazione(
    val id: String = "",
    val nome: String = "",
    val cognome: String = "",
    val targa: String = "",
    val modelloAuto: String = "",
    val piano: String = "",
    val data: String = "",          // "2024-05-24"
    val orarioArrivo: String = "",  // "14:30"
    val tipoTariffa: String = "Tariffa Oraria",
    val abbonamento: Boolean = false,
    val timestampPrenotazione: Timestamp = Timestamp.now(),
    val timestampIngresso: Timestamp? = null,
    val timestampUscita: Timestamp? = null,
    val statoIngresso: Boolean = false,   // true = già entrato
    val totaleOre: Double = 0.0,
    val totalePagato: Double = 0.0
)
