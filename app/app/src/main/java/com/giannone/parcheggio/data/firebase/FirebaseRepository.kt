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
import java.util.Calendar

class FirebaseRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    // ─── PRENOTAZIONI ──────────────────────────────────────────────

    fun getPrenotazioniPerData(data: String): Flow<List<Prenotazione>> = callbackFlow {
        val listener = db.collection("prenotazioni")
            .whereEqualTo("data", data)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Prenotazione::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getPrenotazioniPerDate(dateList: List<String>): Flow<List<Prenotazione>> = callbackFlow {
        if (dateList.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("prenotazioni")
            .whereIn("data", dateList)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { return@addSnapshotListener }
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
                if (error != null) { return@addSnapshotListener }
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

    // Ingresso con timestamp automatico (ora corrente)
    suspend fun registraIngresso(id: String) {
        db.collection("prenotazioni").document(id).update(
            mapOf(
                "timestampIngresso" to Timestamp.now(),
                "statoIngresso" to true
            )
        ).await()
    }

    // Ingresso con timestamp manuale
    suspend fun registraIngressoConTimestamp(id: String, timestamp: Timestamp) {
        db.collection("prenotazioni").document(id).update(
            mapOf(
                "timestampIngresso" to timestamp,
                "statoIngresso" to true
            )
        ).await()
    }

    // Uscita con timestamp automatico (ora corrente)
    suspend fun registraUscita(
        id: String,
        tipoTariffa: String,
        tariffaOraria: Double,
        tariffaSpecifica: Double
    ): Triple<Timestamp, Timestamp, Double> {
        return registraUscitaConTimestamp(
            id, Timestamp.now(), tipoTariffa, tariffaOraria, tariffaSpecifica
        )
    }

    // Uscita con timestamp manuale (o automatico)
    // Recupera SEMPRE il timestampIngresso fresco da Firestore per evitare dati stale.
    suspend fun registraUscitaConTimestamp(
        id: String,
        uscita: Timestamp,
        tipoTariffa: String,
        tariffaOraria: Double,
        tariffaSpecifica: Double
    ): Triple<Timestamp, Timestamp, Double> {
        // Fetch fresco da Firestore: elimina il rischio di usare dati stale dal ViewModel
        val doc = db.collection("prenotazioni").document(id).get().await()
        val prenotazione = doc.toObject(Prenotazione::class.java)
            ?: throw IllegalStateException("Prenotazione non trovata (id=$id)")
        val ingresso = prenotazione.timestampIngresso
            ?: throw IllegalStateException("Ingresso non ancora registrato per questa prenotazione")

        val oreDouble = maxOf((uscita.seconds - ingresso.seconds) / 3600.0, 0.0)
        val totale = calcolaTotale(tipoTariffa, ingresso, uscita, oreDouble, tariffaOraria, tariffaSpecifica)

        db.collection("prenotazioni").document(id).update(
            mapOf(
                "timestampUscita" to uscita,
                "totaleOre" to oreDouble,
                "totalePagato" to totale
            )
        ).await()
        return Triple(ingresso, uscita, totale)
    }

    // ─── Calcolo tariffe ───────────────────────────────────────────────

    /**
     * Calcola il totale da pagare in base al tipo di tariffa:
     * - Oraria:      ceil(ore) × tariffaOraria
     * - Giornaliera: tariffa fissa per le prime 12h; poi ceil(oreExtra) × tariffaOraria
     * - Notturna:    tariffa fissa se la sosta è tutta nell'arco 23:00-06:00;
     *                se si sforano le 06:00 → fissa + ceil(oreExtra dal 06:00) × tariffaOraria;
     *                se l'ingresso è fuori dalla finestra notturna → solo tariffaOraria a ore
     * - Eventi:      sempre tariffa fissa, indipendentemente dalla durata
     */
    private fun calcolaTotale(
        tipoTariffa: String,
        ingresso: Timestamp,
        uscita: Timestamp,
        oreDouble: Double,
        tariffaOraria: Double,
        tariffaSpecifica: Double
    ): Double = when (tipoTariffa) {

        "Tariffa Oraria" -> kotlin.math.ceil(oreDouble) * tariffaOraria

        "Tariffa Giornaliera" -> {
            if (oreDouble <= 12.0) {
                tariffaSpecifica
            } else {
                val oreExtra = oreDouble - 12.0
                tariffaSpecifica + kotlin.math.ceil(oreExtra) * tariffaOraria
            }
        }

        "Tariffa Notturna" -> {
            val calIngresso = Calendar.getInstance()
            calIngresso.time = ingresso.toDate()
            val ingressoHour = calIngresso.get(Calendar.HOUR_OF_DAY)

            // L'ingresso è nella finestra notturna (23:00-06:00)?
            val ingressoInFinestra = ingressoHour >= 23 || ingressoHour < 6

            if (!ingressoInFinestra) {
                // Fuori dalla finestra notturna → tariffa oraria normale
                kotlin.math.ceil(oreDouble) * tariffaOraria
            } else {
                // Calcola il limite delle 06:00 del mattino successivo
                val cal06 = Calendar.getInstance()
                cal06.time = ingresso.toDate()
                if (ingressoHour >= 23) {
                    // Ingresso dopo le 23:00 → le 06:00 di riferimento sono il giorno dopo
                    cal06.add(Calendar.DAY_OF_YEAR, 1)
                }
                cal06.set(Calendar.HOUR_OF_DAY, 6)
                cal06.set(Calendar.MINUTE, 0)
                cal06.set(Calendar.SECOND, 0)
                cal06.set(Calendar.MILLISECOND, 0)
                val timestamp06 = Timestamp(cal06.time)

                if (uscita.seconds <= timestamp06.seconds) {
                    // Uscita entro le 06:00 → solo tariffa fissa notturna
                    tariffaSpecifica
                } else {
                    // Uscita oltre le 06:00 → fissa + ore extra a tariffa oraria
                    val secondiExtra = uscita.seconds - timestamp06.seconds
                    val oreExtra = secondiExtra / 3600.0
                    tariffaSpecifica + kotlin.math.ceil(oreExtra) * tariffaOraria
                }
            }
        }

        "Tariffa Eventi" -> tariffaSpecifica // Sempre fissa

        else -> kotlin.math.ceil(oreDouble) * tariffaOraria
    }

    suspend fun deletePrenotazione(id: String) {
        db.collection("prenotazioni").document(id).delete().await()
    }

    // ─── PIANI ─────────────────────────────────────────────────────

    fun getPiani(): Flow<List<Piano>> = callbackFlow {
        val listener = db.collection("piani")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { return@addSnapshotListener }
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
                if (error != null) { return@addSnapshotListener }
                val config = snapshot?.toObject(Configurazione::class.java) ?: Configurazione()
                trySend(config)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveConfigurazione(config: Configurazione) {
        db.collection("configurazione").document("config").set(config).await()
    }
}
