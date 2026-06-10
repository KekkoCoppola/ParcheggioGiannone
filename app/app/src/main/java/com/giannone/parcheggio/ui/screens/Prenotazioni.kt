package com.giannone.parcheggio.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giannone.parcheggio.data.model.Prenotazione
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrenotazioniScreen(
    viewModel: ParcheggioViewModel,
    onPrenotazioneClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val piani by viewModel.piani.collectAsState()
    val prenotazioniRaw by viewModel.prenotazioni.collectAsState()
    val prenotazioni = remember(prenotazioniRaw, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) prenotazioniRaw
        else prenotazioniRaw.filter {
            it.nome.lowercase().contains(q) ||
            it.cognome.lowercase().contains(q) ||
            it.targa.lowercase().contains(q)
        }
    }

    var showNewBookingSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var prenotazioneDaEliminare by remember { mutableStateOf<Prenotazione?>(null) }

    // Converti selectedDate (yyyy-MM-dd) in millis UTC per il DatePicker
    val selectedDateMillis = remember(selectedDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val localDate = sdf.parse(selectedDate) ?: java.util.Date()
            val localCal = Calendar.getInstance()
            localCal.time = localDate
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCal.set(
                localCal.get(Calendar.YEAR),
                localCal.get(Calendar.MONTH),
                localCal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
            )
            utcCal.set(Calendar.MILLISECOND, 0)
            utcCal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prenotazioni",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    // Calendario — apre DatePickerDialog
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleziona data", tint = Primary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewBookingSheet = true },
                containerColor = PrimaryContainer,
                contentColor = OnPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 68.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuova Prenotazione")
            }
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 160.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Sottotitolo
            item {
                Text(
                    "PARCHEGGIO GIANNONE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Outline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Day selector (frecce)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.goToPreviousDay() }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Giorno precedente", tint = Primary)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showDatePicker = true }
                        ) {
                            Text(
                                "DATA SELEZIONATA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Outline
                            )
                            Text(
                                viewModel.displayDate(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { viewModel.goToNextDay() }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Giorno successivo", tint = Primary)
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Cerca per nome o targa...", color = Outline) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    ),
                    singleLine = true
                )
            }

            // Booking cards
            if (prenotazioni.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nessuna prenotazione per questa data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                items(prenotazioni, key = { it.id }) { prenotazione ->
                    BookingCard(
                        prenotazione = prenotazione,
                        onClick = { onPrenotazioneClick(prenotazione.id) },
                        onLongClick = { prenotazioneDaEliminare = prenotazione }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // ─── DatePicker popup ─────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Converti UTC millis in data locale
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            utcCal.timeInMillis = millis
                            val localCal = Calendar.getInstance()
                            localCal.set(
                                utcCal.get(Calendar.YEAR),
                                utcCal.get(Calendar.MONTH),
                                utcCal.get(Calendar.DAY_OF_MONTH),
                                0, 0, 0
                            )
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            viewModel.setSelectedDate(sdf.format(localCal.time))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Conferma", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla", color = OnSurfaceVariant)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Primary,
                    todayDateBorderColor = Primary
                )
            )
        }
    }

    // ─── New booking bottom sheet ────────────────────────────────────
    if (showNewBookingSheet) {
        NuovaPrenotazioneBottomSheet(
            viewModel = viewModel,
            piani = piani,
            onDismiss = { showNewBookingSheet = false }
        )
    }

    // ─── Delete booking confirmation dialog ──────────────────────────
    if (prenotazioneDaEliminare != null) {
        val pren = prenotazioneDaEliminare!!
        AlertDialog(
            onDismissRequest = { prenotazioneDaEliminare = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePrenotazione(pren.id)
                        prenotazioneDaEliminare = null
                    }
                ) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { prenotazioneDaEliminare = null }) {
                    Text("Annulla", color = OnSurfaceVariant)
                }
            },
            title = { Text("Elimina Prenotazione") },
            text = { Text("Sei sicuro di voler eliminare la prenotazione per ${pren.nome} ${pren.cognome} (Targa: ${pren.targa})?") }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookingCard(
    prenotazione: Prenotazione,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Name + floor
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${prenotazione.nome} ${prenotazione.cognome}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            prenotazione.piano,
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                val (tagText, tagBgColor, tagTextColor) = when {
                    prenotazione.timestampUscita != null -> Triple("Prenotazione chiusa", Outline.copy(alpha = 0.12f), Outline)
                    prenotazione.statoIngresso -> Triple("Parcheggiata", Tertiary.copy(alpha = 0.15f), Tertiary)
                    else -> Triple("In arrivo", Primary.copy(alpha = 0.15f), Primary)
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = tagBgColor
                ) {
                    Text(
                        tagText,
                        style = MaterialTheme.typography.labelMedium,
                        color = tagTextColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = OutlineVariant.copy(alpha = 0.4f)
            )

            // Targa + Modello
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = Outline,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        prenotazione.targa,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Outline,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(prenotazione.modelloAuto, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
            }
        }
    }
}
