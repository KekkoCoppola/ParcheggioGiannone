package com.giannone.parcheggio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.giannone.parcheggio.data.model.Piano
import com.giannone.parcheggio.data.model.Prenotazione
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuovaPrenotazioneBottomSheet(
    viewModel: ParcheggioViewModel,
    piani: List<Piano>,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var targa by remember { mutableStateOf("") }
    var modelloAuto by remember { mutableStateOf("") }
    var pianoSelezionato by remember { mutableStateOf(piani.firstOrNull()?.nome ?: "Piano 0") }
    var data by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var orarioArrivo by remember { mutableStateOf("") }
    var tipoTariffa by remember { mutableStateOf("Tariffa Oraria") }
    var abbonamento by remember { mutableStateOf(false) }

    var pianoDdExpanded by remember { mutableStateOf(false) }
    var tariffaDdExpanded by remember { mutableStateOf(false) }

    val tariffe = listOf("Tariffa Oraria", "Tariffa Giornaliera", "Tariffa Notturna", "Tariffa Eventi")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = SurfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nuova Prenotazione",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = OnSurfaceVariant)
                }
            }

            // Nome + Cognome
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField(
                    label = "Nome *",
                    placeholder = "es. Mario",
                    value = nome,
                    onValueChange = { nome = it },
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = "Cognome",
                    placeholder = "es. Rossi",
                    value = cognome,
                    onValueChange = { cognome = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Targa
            FormField(
                label = "Targa *",
                placeholder = "ES. AA 123 BB",
                value = targa,
                onValueChange = { targa = it.uppercase() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Modello Auto
            FormField(
                label = "Modello Auto",
                placeholder = "es. Fiat 500",
                value = modelloAuto,
                onValueChange = { modelloAuto = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Piano Parcheggio
            Text("Piano Parcheggio", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = pianoDdExpanded,
                onExpandedChange = { pianoDdExpanded = it }
            ) {
                OutlinedTextField(
                    value = pianosSelezionato(piani, pianoSelezionato),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pianoDdExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    )
                )
                ExposedDropdownMenu(expanded = pianoDdExpanded, onDismissRequest = { pianoDdExpanded = false }) {
                    piani.forEach { piano ->
                        DropdownMenuItem(
                            text = { Text(piano.nome) },
                            onClick = { pianoSelezionato = piano.nome; pianoDdExpanded = false }
                        )
                    }
                    if (piani.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Piano 0") },
                            onClick = { pianoSelezionato = "Piano 0"; pianoDdExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Data + Orario
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Data", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = data,
                        onValueChange = { data = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("aaaa-mm-gg") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = OutlineVariant,
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow
                        ),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Orario Arrivo", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = orarioArrivo,
                        onValueChange = { orarioArrivo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("HH:mm") },
                        shape = RoundedCornerShape(10.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Tipo Tariffa
            Text("Tipo Tariffa", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = tariffaDdExpanded,
                onExpandedChange = { tariffaDdExpanded = it }
            ) {
                OutlinedTextField(
                    value = tipoTariffa,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tariffaDdExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    )
                )
                ExposedDropdownMenu(expanded = tariffaDdExpanded, onDismissRequest = { tariffaDdExpanded = false }) {
                    tariffe.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = { tipoTariffa = t; tariffaDdExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Abbonamento toggle
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Abbonamento", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
                        Text("Applica tariffa cliente abbonato", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                    Switch(
                        checked = abbonamento,
                        onCheckedChange = { abbonamento = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryContainer)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
                ) {
                    Text("Annulla", style = MaterialTheme.typography.bodyLarge)
                }
                Button(
                    onClick = {
                        if (nome.isNotBlank() && targa.isNotBlank()) {
                            viewModel.addPrenotazione(
                                Prenotazione(
                                    nome = nome.trim(),
                                    cognome = cognome.trim(),
                                    targa = targa.trim(),
                                    modelloAuto = modelloAuto.trim(),
                                    piano = pianosSelezionato(piani, pianoSelezionato),
                                    data = data,
                                    orarioArrivo = orarioArrivo,
                                    tipoTariffa = tipoTariffa,
                                    abbonamento = abbonamento
                                )
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                ) {
                    Text("Conferma", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

private fun pianosSelezionato(piani: List<Piano>, selected: String): String {
    return if (piani.isEmpty()) selected else selected
}

@Composable
private fun FormField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Outline) },
            shape = RoundedCornerShape(10.dp),
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
