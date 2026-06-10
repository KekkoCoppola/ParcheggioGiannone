package com.giannone.parcheggio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giannone.parcheggio.data.model.Prenotazione
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DettaglioPrenotazioneScreen(
    prenotazioneId: String,
    viewModel: ParcheggioViewModel,
    onBack: () -> Unit,
    onUscitaRegistrata: () -> Unit
) {
    val prenotazioni by viewModel.prenotazioni.collectAsState()
    val autoAttive by viewModel.autoAttive.collectAsState()

    val prenotazione = remember(prenotazioni, autoAttive, prenotazioneId) {
        (prenotazioni + autoAttive).firstOrNull { it.id == prenotazioneId }
    }

    if (prenotazione == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    val giaEntrato = prenotazione.statoIngresso
    val giaUscito = prenotazione.timestampUscita != null
    var showManualDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dettaglio Prenotazione",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Primary)
                    }
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "${prenotazione.nome} ${prenotazione.cognome}",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    InfoRow(icon = Icons.Default.Layers, label = "Piano", value = prenotazione.piano, showDivider = true)
                    InfoRowTarga(icon = Icons.Default.Badge, label = "Targa", targa = prenotazione.targa, showDivider = true)
                    InfoRow(icon = Icons.Default.DirectionsCar, label = "Modello", value = prenotazione.modelloAuto, showDivider = false)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!giaEntrato) {
                // ── INGRESSO ──────────────────────────────────────────────
                Button(
                    onClick = {
                        viewModel.registraIngresso(prenotazione.id)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary, contentColor = OnTertiary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingresso ora", style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Clicca per confermare l'ingresso del veicolo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = { showManualDialog = true }) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inserisci ora manualmente", color = Primary, style = MaterialTheme.typography.bodyMedium)
                }
            } else if (!giaUscito) {
                // ── USCITA (Parcheggiata, deve ancora uscire) ──────────────
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SurfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Entrato ${viewModel.formatTimestampFull(prenotazione.timestampIngresso)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.registraUscita(prenotazione) { onUscitaRegistrata() }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Error, contentColor = OnError)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uscita ora", style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Clicca per confermare l'uscita del veicolo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = { showManualDialog = true }) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inserisci ora manualmente", color = Primary, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // ── COMPLETATO (Già Uscito) ───────────────────────────────
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Tertiary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Servizio Completato",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Entrata: ${viewModel.formatTimestampFull(prenotazione.timestampIngresso)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface
                        )
                        Text(
                            "Uscita: ${viewModel.formatTimestampFull(prenotazione.timestampUscita)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface
                        )
                        Text(
                            "Tempo totale: ${String.format("%.1f ore", prenotazione.totaleOre)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = OutlineVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            "Tariffa: ${prenotazione.tipoTariffa}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface
                        )
                        Text(
                            "Totale Pagato: € ${String.format("%.2f", prenotazione.totalePagato)}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Primary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timestamp prenotazione
            Surface(shape = RoundedCornerShape(999.dp), color = SurfaceContainerLow) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Prenotato ${viewModel.formatTimestampFull(prenotazione.timestampPrenotazione)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ─── Dialogo inserimento ora manuale ──────────────────────────────
    if (showManualDialog) {
        val now = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = now.get(Calendar.HOUR_OF_DAY),
            initialMinute = now.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = {
                Text(
                    if (!giaEntrato) "Ora ingresso manuale" else "Ora uscita manuale",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (!giaEntrato) "Seleziona l'orario di ingresso" else "Seleziona l'orario di uscita",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialSelectedContentColor = OnPrimary,
                            clockDialUnselectedContentColor = OnSurface,
                            selectorColor = Primary,
                            containerColor = SurfaceContainerLow
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!giaEntrato) {
                            viewModel.registraIngressoManuale(
                                prenotazione.id,
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            showManualDialog = false
                            onBack()
                        } else {
                            viewModel.registraUscitaManuale(
                                prenotazione,
                                timePickerState.hour,
                                timePickerState.minute
                            ) {
                                showManualDialog = false
                                onUscitaRegistrata()
                            }
                            showManualDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Conferma", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text("Annulla", color = OnSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = SurfaceContainerLowest
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryContainer.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
    }
    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant.copy(alpha = 0.4f))
    }
}

@Composable
private fun InfoRowTarga(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    targa: String,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryContainer.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
        Surface(shape = RoundedCornerShape(8.dp), color = SurfaceContainerHigh) {
            Text(
                targa,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
                color = OnSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant.copy(alpha = 0.4f))
    }
}
