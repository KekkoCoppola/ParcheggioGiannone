package com.giannone.parcheggio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.giannone.parcheggio.theme.*
import com.giannone.parcheggio.ui.screens.*
import com.giannone.parcheggio.ui.viewmodel.ParcheggioViewModel

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Prenotazioni)
    val viewModel: ParcheggioViewModel = viewModel()

    // Determine active tab from top of backstack
    val currentDest = backStack.lastOrNull()
    val isPrenotazioniTab = currentDest is Prenotazioni || currentDest is DettaglioPrenotazione || currentDest is ResocontoUscita
    val isParcheggioTab = currentDest is StatoParcheggio

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Prenotazioni> {
                    PrenotazioniScreen(
                        viewModel = viewModel,
                        onPrenotazioneClick = { id -> backStack.add(DettaglioPrenotazione(id)) },
                        onSettingsClick = { backStack.add(Impostazioni) }
                    )
                }
                entry<StatoParcheggio> {
                    StatoParcheggioScreen(
                        viewModel = viewModel,
                        onPrenotazioneClick = { id -> backStack.add(DettaglioPrenotazione(id)) },
                        onSettingsClick = { backStack.add(Impostazioni) }
                    )
                }
                entry<DettaglioPrenotazione> { key ->
                    DettaglioPrenotazioneScreen(
                        prenotazioneId = key.prenotazioneId,
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() },
                        onUscitaRegistrata = {
                            backStack.removeLastOrNull()
                            backStack.add(ResocontoUscita)
                        }
                    )
                }
                entry<ResocontoUscita> {
                    ResocontoUscitaScreen(
                        viewModel = viewModel,
                        onBack = {
                            // Pop back to Prenotazioni
                            while (backStack.size > 1) backStack.removeLastOrNull()
                        }
                    )
                }
                entry<Impostazioni> {
                    ImpostazioniScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )

        // Bottom Nav Bar — only show on main tabs (not detail/settings screens)
        if (currentDest is Prenotazioni || currentDest is StatoParcheggio) {
            BottomNavBar(
                isPrenotazioniActive = isPrenotazioniTab,
                modifier = Modifier.align(Alignment.BottomCenter),
                onPrenotazioniClick = {
                    if (currentDest !is Prenotazioni) {
                        while (backStack.size > 1) backStack.removeLastOrNull()
                        if (backStack.lastOrNull() !is Prenotazioni) backStack.add(Prenotazioni)
                    }
                },
                onParcheggioClick = {
                    if (currentDest !is StatoParcheggio) {
                        while (backStack.size > 1) backStack.removeLastOrNull()
                        backStack.add(StatoParcheggio)
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavBar(
    isPrenotazioniActive: Boolean,
    modifier: Modifier = Modifier,
    onPrenotazioniClick: () -> Unit,
    onParcheggioClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceContainerLowest,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Prenotazioni Tab
            if (isPrenotazioniActive) {
                Button(
                    onClick = onPrenotazioniClick,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Prenotazioni", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                TextButton(onClick = onPrenotazioniClick) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OnSurface)
                        Text("Prenotazioni", style = MaterialTheme.typography.labelMedium, color = OnSurface)
                    }
                }
            }

            // Parcheggio Tab
            if (!isPrenotazioniActive) {
                Button(
                    onClick = onParcheggioClick,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text("P", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Parcheggio", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                TextButton(onClick = onParcheggioClick) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("P", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text("Parcheggio", style = MaterialTheme.typography.labelMedium, color = OnSurface)
                    }
                }
            }
        }
    }
}
