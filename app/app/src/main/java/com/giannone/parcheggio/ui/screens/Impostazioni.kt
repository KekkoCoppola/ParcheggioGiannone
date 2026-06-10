package com.giannone.parcheggio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.giannone.parcheggio.data.model.Configurazione
import com.giannone.parcheggio.data.model.Piano
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpostazioniScreen(
    viewModel: ParcheggioViewModel,
    onBack: () -> Unit
) {
    val config by viewModel.config.collectAsState()
    val piani by viewModel.piani.collectAsState()

    // Local editable state for config
    var tariffaOraria by remember(config) { mutableStateOf(config.tariffaOraria.toString()) }
    var tariffaGiornaliera by remember(config) { mutableStateOf(config.tariffaGiornaliera.toString()) }
    var tariffaNotturna by remember(config) { mutableStateOf(config.tariffaNotturna.toString()) }
    var tariffaEventi by remember(config) { mutableStateOf(config.tariffaEventi.toString()) }

    var showAddPianoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni", style = MaterialTheme.typography.headlineLarge.copy(color = Primary, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Primary)
                    }
                },
                actions = {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Primary, modifier = Modifier.padding(end = 16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Subtitolo
            Text(
                "PARCHEGGIO GIANNONE",
                style = MaterialTheme.typography.labelSmall,
                color = Outline,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // ─── GESTIONE PIANI ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Gestione Piani",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Button(
                    onClick = { showAddPianoDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aggiungi Piano", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Piani list
            piani.forEach { piano ->
                PianoCard(
                    piano = piano,
                    onUpdate = { updated -> viewModel.updatePiano(updated) },
                    onDelete = { viewModel.deletePiano(piano.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (piani.isEmpty()) {
                Text(
                    "Nessun piano configurato.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── TARIFFA ORARIA ──────────────────────────────────────
            Text(
                "Tariffa Oraria",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = OnSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TariffaField("Prezzo per ora di sosta", tariffaOraria, "€/ora") { tariffaOraria = it }
                    TariffaField("Tariffa Giornaliera", tariffaGiornaliera, "€/giorno") { tariffaGiornaliera = it }
                    TariffaField("Tariffa Notturna", tariffaNotturna, "€/notte") { tariffaNotturna = it }
                    TariffaField("Tariffa Eventi", tariffaEventi, "€/evento") { tariffaEventi = it }
                    Text(
                        "La tariffa verrà applicata a tutte le nuove prenotazioni.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Salva
            Button(
                onClick = {
                    viewModel.saveConfig(
                        config.copy(
                            tariffaOraria = tariffaOraria.toDoubleOrNull() ?: config.tariffaOraria,
                            tariffaGiornaliera = tariffaGiornaliera.toDoubleOrNull() ?: config.tariffaGiornaliera,
                            tariffaNotturna = tariffaNotturna.toDoubleOrNull() ?: config.tariffaNotturna,
                            tariffaEventi = tariffaEventi.toDoubleOrNull() ?: config.tariffaEventi
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Text("Salva Modifiche", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddPianoDialog) {
        AddPianoDialog(
            onConfirm = { nome, posti ->
                viewModel.addPiano(nome, posti)
                showAddPianoDialog = false
            },
            onDismiss = { showAddPianoDialog = false }
        )
    }
}

@Composable
private fun PianoCard(piano: Piano, onUpdate: (Piano) -> Unit, onDelete: () -> Unit) {
    var nomeEdit by remember(piano.id) { mutableStateOf(piano.nome) }
    var postiEdit by remember(piano.id) { mutableStateOf(piano.postiTotali.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Nome Piano", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                OutlinedTextField(
                    value = nomeEdit,
                    onValueChange = { nomeEdit = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = Background
                    ),
                    singleLine = true,
                    onFocusChanged = { if (!it.isFocused) onUpdate(piano.copy(nome = nomeEdit, postiTotali = postiEdit.toIntOrNull() ?: piano.postiTotali)) }
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.width(90.dp)) {
                Text("Posti totali", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                OutlinedTextField(
                    value = postiEdit,
                    onValueChange = { postiEdit = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = Background
                    ),
                    singleLine = true,
                    onFocusChanged = { if (!it.isFocused) onUpdate(piano.copy(nome = nomeEdit, postiTotali = postiEdit.toIntOrNull() ?: piano.postiTotali)) }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Elimina piano", tint = Error)
            }
        }
    }
}

@Composable
private fun TariffaField(label: String, value: String, suffix: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            suffix = { Text(suffix, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant) },
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Primary, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = OutlineVariant,
                focusedContainerColor = SurfaceContainerLow,
                unfocusedContainerColor = SurfaceContainerLow
            ),
            singleLine = true
        )
    }
}

@Composable
private fun AddPianoDialog(onConfirm: (String, Int) -> Unit, onDismiss: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var posti by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aggiungi Piano", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome piano") },
                    placeholder = { Text("es. Piano 0") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = posti,
                    onValueChange = { posti = it },
                    label = { Text("Posti totali") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nome.isNotBlank()) onConfirm(nome.trim(), posti.toIntOrNull() ?: 0) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Aggiungi") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
