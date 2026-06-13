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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.giannone.parcheggio.data.model.Prenotazione
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

val TIPI_TARIFFA = listOf("Tariffa Oraria", "Tariffa Giornaliera", "Tariffa Notturna", "Tariffa Eventi")

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

    // ─── Stato ricerca e filtri ────────────────────────────────────────
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var filtroTariffa by remember { mutableStateOf<String?>(null) }
    var filtroAbbonamento by remember { mutableStateOf<Boolean?>(null) }

    // Numero filtri attivi (per il badge)
    val filtriAttiviCount = remember(filtroTariffa, filtroAbbonamento) {
        listOf<Any?>(filtroTariffa, filtroAbbonamento).count { it != null }
    }

    // Auto filtrate
    val autoFiltrate = remember(autoAttive, searchQuery, filtroTariffa, filtroAbbonamento) {
        autoAttive.filter { pren ->
            val q = searchQuery.trim().lowercase()
            val matchSearch = q.isEmpty() ||
                pren.nome.lowercase().contains(q) ||
                pren.cognome.lowercase().contains(q) ||
                pren.targa.lowercase().contains(q)
            val matchTariffa = filtroTariffa == null || pren.tipoTariffa == filtroTariffa
            val matchAbbonamento = filtroAbbonamento == null || pren.abbonamento == filtroAbbonamento
            matchSearch && matchTariffa && matchAbbonamento
        }
    }

    val hasFiltroAttivo = searchQuery.isNotEmpty() || filtroTariffa != null || filtroAbbonamento != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Parcheggio",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
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

            // Sezione Grafico Incassi
            item {
                val last7Days = remember { viewModel.getLast7Days() }
                val prenotazioniLast7Days by viewModel.prenotazioniLast7Days.collectAsState()

                val incassiPerGiorno = remember(prenotazioniLast7Days, last7Days) {
                    last7Days.map { date ->
                        val sum = prenotazioniLast7Days
                            .filter { it.data == date && it.timestampUscita != null }
                            .sumOf { it.totalePagato }
                        date to sum
                    }
                }

                val totaleIncassi7Giorni = remember(incassiPerGiorno) {
                    incassiPerGiorno.sumOf { it.second }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "INCASSI ULTIMI 7 GIORNI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    "€ ${String.format(Locale.getDefault(), "%.2f", totaleIncassi7Giorni)}",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Primary
                                )
                            }
                            Icon(
                                Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val maxIncasso = remember(incassiPerGiorno) {
                            val max = incassiPerGiorno.maxOfOrNull { it.second } ?: 0.0
                            if (max == 0.0) 10.0 else max
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            incassiPerGiorno.forEach { (date, value) ->
                                val barHeightFraction = (value / maxIncasso).toFloat()
                                val dayLabel = remember(date) {
                                    try {
                                        val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val d = inputSdf.parse(date) ?: Date()
                                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                        if (date == today) "Oggi"
                                        else SimpleDateFormat("dd MMM", Locale.ITALIAN).format(d)
                                    } catch (e: Exception) { "" }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (value > 0.0) {
                                        Text(
                                            "€${value.toInt()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .fillMaxHeight(barHeightFraction.coerceAtLeast(0.04f))
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                if (value > 0.0) Primary
                                                else OutlineVariant.copy(alpha = 0.5f)
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        dayLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = OnSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── Search bar + pulsante filtro ─────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Cerca per nome o targa...", color = Outline) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancella", tint = OnSurfaceVariant)
                                }
                            }
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

                    // Pulsante filtro con badge
                    Box {
                        Surface(
                            onClick = { showFilterMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (filtriAttiviCount > 0) PrimaryContainer else SurfaceContainerLow,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filtri",
                                    tint = if (filtriAttiviCount > 0) OnPrimaryContainer else OnSurfaceVariant
                                )
                            }
                        }

                        // Badge numero filtri attivi
                        if (filtriAttiviCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Primary)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    filtriAttiviCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = OnPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ─── Popup filtri ──────────────────────────────────
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            properties = PopupProperties(focusable = true),
                            containerColor = SurfaceContainerLowest
                        ) {
                            // Sezione Abbonamento
                            Text(
                                "ABBONAMENTO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Outline,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Switch(
                                            checked = filtroAbbonamento == true,
                                            onCheckedChange = { checked ->
                                                filtroAbbonamento = if (checked) true else null
                                                if (checked) filtroTariffa = null
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = OnPrimary,
                                                checkedTrackColor = Primary
                                            )
                                        )
                                        Text(
                                            "Solo Abbonati",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = OnSurface
                                        )
                                    }
                                },
                                onClick = {
                                    val nuovoValore = filtroAbbonamento != true
                                    filtroAbbonamento = if (nuovoValore) true else null
                                    if (nuovoValore) filtroTariffa = null
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = OutlineVariant.copy(alpha = 0.4f)
                            )

                            // Sezione Tariffa
                            Text(
                                "TIPO TARIFFA",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (filtroAbbonamento == true)
                                    Outline.copy(alpha = 0.35f)
                                else
                                    Outline,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            TIPI_TARIFFA.forEach { tariffa ->
                                val isSelected = filtroTariffa == tariffa
                                DropdownMenuItem(
                                    enabled = filtroAbbonamento != true,
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                tariffa,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (filtroAbbonamento == true)
                                                    OnSurface.copy(alpha = 0.3f)
                                                else
                                                    OnSurface
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        filtroTariffa = if (isSelected) null else tariffa
                                    }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = OutlineVariant.copy(alpha = 0.4f)
                            )

                            // Reset filtri
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Rimuovi filtri",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.FilterListOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    filtroTariffa = null
                                    filtroAbbonamento = null
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // ─── Piani espandibili ─────────────────────────────────────────
            items(piani) { piano ->
                val autoInPiano = remember(autoFiltrate, piano) {
                    autoFiltrate.filter { it.piano == piano.nome }
                }
                val autoInPianoTotali = viewModel.autoPerPiano(piano.nome)
                val isExpanded = piano.id in expandedPiani

                // Se c'è filtro attivo e il piano è vuoto, non mostrarlo
                if (hasFiltroAttivo && autoInPiano.isEmpty()) return@items

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
                                val label = if (hasFiltroAttivo && autoInPiano.size != autoInPianoTotali.size)
                                    "${autoInPiano.size}/${autoInPianoTotali.size} Auto"
                                else
                                    "${autoInPianoTotali.size} Auto"
                                Text(
                                    label,
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
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp)
                            ) {
                                if (autoInPiano.isEmpty()) {
                                    Text(
                                        "Nessuna auto corrisponde ai filtri",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
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
