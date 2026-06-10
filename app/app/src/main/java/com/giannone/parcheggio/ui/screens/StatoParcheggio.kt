package com.giannone.parcheggio.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giannone.parcheggio.data.model.Prenotazione
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatoParcheggioScreen(
    viewModel: ParcheggioViewModel,
    onPrenotazioneClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val autoAttive by viewModel.autoAttive.collectAsState()
    val piani by viewModel.piani.collectAsState()
    
    val totaleAuto = remember(autoAttive) { autoAttive.size }
    val totalLiberi = remember(autoAttive, piani) {
        piani.sumOf { piano ->
            val occupati = autoAttive.filter { it.piano == piano.nome }.size
            maxOf(piano.postiTotali - occupati, 0)
        }
    }

    var expandedPiani by remember { mutableStateOf<Set<String>>(setOf()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Parcheggio",
                        style = MaterialTheme.typography.displayMedium.copy(color = Primary, fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
        ) {
            // Counter: Auto Parcheggiate
            item {
                StatCard(
                    icon = Icons.Default.DirectionsCar,
                    iconBg = Primary,
                    label = "AUTO PARCHEGGIATE",
                    value = totaleAuto.toString()
                )
            }
            // Counter: Posti Liberi
            item {
                StatCard(
                    icon = Icons.Default.LocalParking,
                    iconBg = Tertiary,
                    label = "POSTI LIBERI",
                    value = totalLiberi.toString()
                )
            }

            // Piani espandibili
            items(piani) { piano ->
                val autoInPiano = viewModel.autoPerPiano(piano.nome)
                val isExpanded = piano.id in expandedPiani

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Piano header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedPiani = if (isExpanded)
                                        expandedPiani - piano.id
                                    else
                                        expandedPiani + piano.id
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Layers,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                piano.nome,
                                style = MaterialTheme.typography.headlineLarge,
                                color = OnSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SecondaryContainer
                            ) {
                                Text(
                                    "${autoInPiano.size} Auto",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        }

                        // Auto list (expandable)
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                                if (autoInPiano.isEmpty()) {
                                    Text(
                                        "Nessuna auto presente",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    autoInPiano.forEach { prenotazione ->
                                        AutoCard(
                                            prenotazione = prenotazione,
                                            onClick = { onPrenotazioneClick(prenotazione.id) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (piani.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nessun piano configurato.\nVai in Impostazioni per aggiungerne uno.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Text(value, style = MaterialTheme.typography.displayMedium, color = OnSurface)
            }
        }
    }
}

@Composable
private fun AutoCard(prenotazione: Prenotazione, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${prenotazione.nome} ${prenotazione.cognome}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface
                )
                Text(
                    "Targa: ${prenotazione.targa}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}
