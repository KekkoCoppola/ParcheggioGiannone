package com.giannone.parcheggio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel
import com.giannone.parcheggio.ui.viewmodel.ResocontoState
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResocontoUscitaScreen(
    viewModel: ParcheggioViewModel,
    onBack: () -> Unit
) {
    val resoconto by viewModel.resocontoState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resoconto Uscita", style = MaterialTheme.typography.headlineLarge, color = OnSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Primary)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Icona successo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Tertiary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Tertiary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Uscita Registrata",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
            Text(
                resoconto.nomeCliente,
                style = MaterialTheme.typography.headlineSmall,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resoconto card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ResocontoRow(
                        icon = Icons.Default.Login,
                        iconColor = Tertiary,
                        label = "Orario Ingresso",
                        value = viewModel.formatTimestamp(resoconto.timestampIngresso),
                        showDivider = true
                    )
                    ResocontoRow(
                        icon = Icons.Default.Logout,
                        iconColor = Error,
                        label = "Orario Uscita",
                        value = viewModel.formatTimestamp(resoconto.timestampUscita),
                        showDivider = true
                    )
                    ResocontoRow(
                        icon = Icons.Default.Schedule,
                        iconColor = Primary,
                        label = "Totale Ore",
                        value = String.format("%.1f ore", resoconto.totaleOre),
                        showDivider = true
                    )
                    ResocontoRow(
                        icon = Icons.Default.Receipt,
                        iconColor = Secondary,
                        label = "Tariffa Applicata",
                        value = resoconto.tipoTariffa,
                        showDivider = true
                    )
                    // Totale pagato — evidenziato
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Primary.copy(alpha = 0.06f), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EuroSymbol, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Totale da Pagare",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "€ %.2f".format(resoconto.totalePagato),
                            style = MaterialTheme.typography.displayMedium.copy(color = Primary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info targa/piano
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = Outline, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(resoconto.targa, style = MaterialTheme.typography.bodyMedium, color = OnSurface, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = Outline, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(resoconto.piano, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Torna alla lista
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 0.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Text("Torna alle Prenotazioni", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResocontoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
    }
    if (showDivider) {
        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant.copy(alpha = 0.3f))
    }
}
