package com.giannone.parcheggio

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Prenotazioni : NavKey
@Serializable data object StatoParcheggio : NavKey
@Serializable data class DettaglioPrenotazione(val prenotazioneId: String) : NavKey
@Serializable data object ResocontoUscita : NavKey
@Serializable data object Impostazioni : NavKey
