package com.giannone.parcheggio.ui.screens

import androidx.compose.foundation.clickable
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
import com.giannone.parcheggio.data.model.Prenotazione
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel

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
    val prenotazioni = viewModel.filteredPrenotazioni()

    var showNewBookingSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prenotazioni",
                        style = MaterialTheme.typography.displayMedium.copy(color = Primary, fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendario", tint = OnSurface)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni", tint = OnSurface)
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
            // Subtitolo parcheggio
            item {
                Text(
                    "PARCHEGGIO GIANNONE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Outline,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Day selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.goToPreviousDay() }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Giorno precedente", tint = Primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "DATA SELEZIONATA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Outline
                            )
                            Text(
                                viewModel.displayDate(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = OnSurface,
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("Cerca per nome o targa...", color = Outline) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
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
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
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
                        onClick = { onPrenotazioneClick(prenotazione.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // New booking bottom sheet
    if (showNewBookingSheet) {
        NuovaPrenotazioneBottomSheet(
            viewModel = viewModel,
            piani = piani,
            onDismiss = { showNewBookingSheet = false }
        )
    }
}

@Composable
private fun BookingCard(prenotazione: Prenotazione, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Name + floor
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "${prenotazione.nome} ${prenotazione.cognome}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(prenotazione.piano, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    }
                }
                // Entry status chip
                if (prenotazione.statoIngresso) {
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Tertiary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Entrato",
                            style = MaterialTheme.typography.labelMedium,
                            color = Tertiary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = OutlineVariant.copy(alpha = 0.4f))

            // Targa + Modello
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = Outline, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(prenotazione.targa, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Outline, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(prenotazione.modelloAuto, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
            }
        }
    }
}
