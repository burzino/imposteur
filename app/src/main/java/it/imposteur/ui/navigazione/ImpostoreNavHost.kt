package it.imposteur.ui.navigazione

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.imposteur.ui.ImpostoreViewModel
import it.imposteur.ui.schermate.SchermataConfigurazione
import it.imposteur.ui.schermate.SchermataDistribuzione
import it.imposteur.ui.schermate.SchermataGioco
import it.imposteur.ui.schermate.SchermataHome
import it.imposteur.ui.schermate.SchermataRegole
import it.imposteur.ui.schermate.SchermataRivela

object Rotte {
    const val HOME = "home"
    const val REGOLE = "regole"
    const val CONFIGURAZIONE = "configurazione"
    const val DISTRIBUZIONE = "distribuzione"
    const val GIOCO = "gioco"
    const val RIVELA = "rivela"
}

private fun NavHostController.vaiAConfigurazione() {
    navigate(Rotte.CONFIGURAZIONE) {
        popUpTo(Rotte.HOME)
        launchSingleTop = true
    }
}

private fun NavHostController.inRotta(rotta: String) = currentDestination?.route == rotta

@Composable
fun ImpostoreNavHost(viewModel: ImpostoreViewModel, navController: NavHostController = rememberNavController()) {
    val stato by viewModel.stato.collectAsStateWithLifecycle()

    // D7: flush della configurazione quando l'app va in background.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) viewModel.salvaOra()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // D1: dopo process death la back stack può puntare a una partita che non esiste più.
    val voce by navController.currentBackStackEntryAsState()
    val rotta = voce?.destination?.route
    LaunchedEffect(rotta, stato.caricamento, stato.partita == null) {
        if (!stato.caricamento && stato.partita == null &&
            (rotta == Rotte.DISTRIBUZIONE || rotta == Rotte.GIOCO || rotta == Rotte.RIVELA)
        ) {
            navController.navigate(Rotte.HOME) { popUpTo(0) }
        }
    }

    NavHost(navController = navController, startDestination = Rotte.HOME) {
        composable(Rotte.HOME) {
            SchermataHome(
                stato = stato,
                onNuovaPartita = {
                    if (navController.inRotta(Rotte.HOME)) {
                        navController.navigate(Rotte.CONFIGURAZIONE) { launchSingleTop = true }
                    }
                },
                onRiprendi = {
                    if (navController.inRotta(Rotte.HOME) && stato.ripristinabile != null) {
                        val inGioco = viewModel.riprendiPartita()
                        navController.navigate(if (inGioco) Rotte.GIOCO else Rotte.DISTRIBUZIONE) {
                            launchSingleTop = true
                        }
                    }
                },
                onRegole = {
                    if (navController.inRotta(Rotte.HOME)) {
                        navController.navigate(Rotte.REGOLE) { launchSingleTop = true }
                    }
                },
            )
        }
        composable(Rotte.REGOLE) {
            SchermataRegole(onIndietro = { navController.popBackStack() })
        }
        composable(Rotte.CONFIGURAZIONE) {
            SchermataConfigurazione(
                stato = stato,
                viewModel = viewModel,
                onIndietro = {
                    viewModel.salvaOra()
                    navController.popBackStack(Rotte.HOME, inclusive = false)
                },
                onInizia = {
                    if (navController.inRotta(Rotte.CONFIGURAZIONE) && viewModel.iniziaPartita()) {
                        navController.navigate(Rotte.DISTRIBUZIONE) { launchSingleTop = true }
                    }
                },
            )
        }
        composable(Rotte.DISTRIBUZIONE) {
            SchermataDistribuzione(
                stato = stato,
                onSono = { da -> viewModel.avanza(da) },
                onNascondiEPassa = { da -> viewModel.avanza(da) },
                onInterrompiRivelazione = { viewModel.interrompiRivelazione() },
                onFineDistribuzione = {
                    navController.navigate(Rotte.GIOCO) {
                        popUpTo(Rotte.DISTRIBUZIONE) { inclusive = true }
                    }
                },
                onInterrompiPartita = {
                    viewModel.terminaPartita()
                    navController.vaiAConfigurazione()
                },
            )
        }
        composable(Rotte.GIOCO) {
            SchermataGioco(
                partita = stato.partita,
                onRivela = {
                    viewModel.entraInRivela()
                    navController.navigate(Rotte.RIVELA) {
                        popUpTo(Rotte.GIOCO) { inclusive = true }
                    }
                },
                onInterrompiPartita = {
                    viewModel.terminaPartita()
                    navController.vaiAConfigurazione()
                },
            )
        }
        composable(Rotte.RIVELA) {
            SchermataRivela(
                partitaViva = stato.partita,
                onNuovaPartita = {
                    if (navController.inRotta(Rotte.RIVELA)) {
                        if (viewModel.iniziaPartita()) {
                            navController.navigate(Rotte.DISTRIBUZIONE) {
                                popUpTo(Rotte.RIVELA) { inclusive = true }
                            }
                        } else {
                            navController.vaiAConfigurazione()
                        }
                    }
                },
                onCambiaImpostazioni = {
                    viewModel.terminaPartita()
                    navController.vaiAConfigurazione()
                },
            )
        }
    }
}
