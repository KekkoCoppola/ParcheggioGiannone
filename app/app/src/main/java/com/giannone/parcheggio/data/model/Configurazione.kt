package com.giannone.parcheggio.data.model

data class Configurazione(
    val id: String = "config",
    val nomeParcheggio: String = "Parcheggio Giannone",
    val tariffaOraria: Double = 2.50,
    val tariffaGiornaliera: Double = 15.00,
    val tariffaNotturna: Double = 10.00,
    val tariffaEventi: Double = 5.00
)
