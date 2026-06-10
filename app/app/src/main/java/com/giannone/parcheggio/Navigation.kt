package com.giannone.parcheggio

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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

    val currentDest = backStack.lastOrNull()
    val isPrenotazioniActive = currentDest is Prenotazioni

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = {
                val initialKey = initialState.key
                val targetKey = targetState.key
                val isTabSwitch = (initialKey is Prenotazioni && targetKey is StatoParcheggio) ||
                                  (initialKey is StatoParcheggio && targetKey is Prenotazioni)
                if (isTabSwitch) {
                    fadeIn(animationSpec = tween(220, easing = LinearEasing)) togetherWith
                    fadeOut(animationSpec = tween(220, easing = LinearEasing))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOutCubic)
                    ) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300, easing = EaseInOutCubic)
                    ) + fadeOut(animationSpec = tween(300))
                }
            },
            popTransitionSpec = {
                val initialKey = initialState.key
                val targetKey = targetState.key
                val isTabSwitch = (initialKey is Prenotazioni && targetKey is StatoParcheggio) ||
                                  (initialKey is StatoParcheggio && targetKey is Prenotazioni)
                if (isTabSwitch) {
                    fadeIn(animationSpec = tween(220, easing = LinearEasing)) togetherWith
                    fadeOut(animationSpec = tween(220, easing = LinearEasing))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300, easing = EaseInOutCubic)
                    ) + fadeIn(animationSpec = tween(300)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOutCubic)
                    )
                }
            },
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

        // Bottom nav: solo sulle schermate principali
        if (currentDest is Prenotazioni || currentDest is StatoParcheggio) {
            BottomNavBar(
                isPrenotazioniActive = isPrenotazioniActive,
                modifier = Modifier.align(Alignment.BottomCenter),
                onPrenotazioniClick = {
                    if (currentDest !is Prenotazioni) {
                        while (backStack.size > 1) backStack.removeLastOrNull()
                        backStack.add(Prenotazioni)
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
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tab Prenotazioni
            NavTabItem(
                isActive = isPrenotazioniActive,
                label = "Prenotazioni",
                onClick = onPrenotazioniClick,
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isPrenotazioniActive) OnPrimary else OnSurface
                    )
                }
            )
            // Tab Parcheggio
            NavTabItem(
                isActive = !isPrenotazioniActive,
                label = "Parcheggio",
                onClick = onParcheggioClick,
                modifier = Modifier.weight(1f),
                icon = {
                    Text(
                        "P",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (!isPrenotazioniActive) OnPrimary else OnSurface
                    )
                }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    isActive: Boolean,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(999.dp),
        color = if (isActive) Primary else SurfaceContainer,
        contentColor = if (isActive) OnPrimary else OnSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium),
                color = if (isActive) OnPrimary else OnSurface
            )
        }
    }
}
