package com.giannone.parcheggio.data.firebase

import com.giannone.parcheggio.data.model.Configurazione
import com.giannone.parcheggio.data.model.Piano
import com.giannone.parcheggio.data.model.Prenotazione
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    // ─── PRENOTAZIONI ──────────────────────────────────────────────

    fun getPrenotazioniPerData(data: String): Flow<List<Prenotazione>> = callbackFlow {
        val listener = db.collection("prenotazioni")
            .whereEqualTo("data", data)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Prenotazione::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAllPrenotazioniAttive(): Flow<List<Prenotazione>> = callbackFlow {
        val listener = db.collection("prenotazioni")
            .whereEqualTo("statoIngresso", true)
            .whereEqualTo("timestampUscita", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Prenotazione::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPrenotazione(prenotazione: Prenotazione): String {
        val docRef = db.collection("prenotazioni").add(prenotazione).await()
        return docRef.id
    }

    suspend fun registraIngresso(id: String) {
        db.collection("prenotazioni").document(id).update(
            mapOf(
                "timestampIngresso" to Timestamp.now(),
                "statoIngresso" to true
            )
        ).await()
    }

    suspend fun registraUscita(id: String, tariffa: Double): Triple<Timestamp, Timestamp, Double> {
        val doc = db.collection("prenotazioni").document(id).get().await()
        val prenotazione = doc.toObject(Prenotazione::class.java)!!
        val ingresso = prenotazione.timestampIngresso ?: Timestamp.now()
        val uscita = Timestamp.now()
        val oreDouble = (uscita.seconds - ingresso.seconds) / 3600.0
        val ore = maxOf(oreDouble, 0.0)
        val totale = kotlin.math.ceil(ore) * tariffa

        db.collection("prenotazioni").document(id).update(
            mapOf(
                "timestampUscita" to uscita,
                "totaleOre" to ore,
                "totalePagato" to totale
            )
        ).await()
        return Triple(ingresso, uscita, totale)
    }

    suspend fun deletePrenotazione(id: String) {
        db.collection("prenotazioni").document(id).delete().await()
    }

    // ─── PIANI ─────────────────────────────────────────────────────

    fun getPiani(): Flow<List<Piano>> = callbackFlow {
        val listener = db.collection("piani")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Piano::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPiano(piano: Piano): String {
        val docRef = db.collection("piani").add(piano).await()
        return docRef.id
    }

    suspend fun updatePiano(piano: Piano) {
        db.collection("piani").document(piano.id).set(piano).await()
    }

    suspend fun deletePiano(id: String) {
        db.collection("piani").document(id).delete().await()
    }

    // ─── CONFIGURAZIONE ────────────────────────────────────────────

    fun getConfigurazione(): Flow<Configurazione> = callbackFlow {
        val listener = db.collection("configurazione").document("config")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val config = snapshot?.toObject(Configurazione::class.java) ?: Configurazione()
                trySend(config)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveConfigurazione(config: Configurazione) {
        db.collection("configurazione").document("config").set(config).await()
    }
}
