package it.imposteur.ui.navigazione

import androidx.compose.runtime.Composable
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

@Composable
fun ImpostoreNavHost(viewModel: ImpostoreViewModel, navController: NavHostController = rememberNavController()) {
    val stato by viewModel.stato.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Rotte.HOME) {
        composable(Rotte.HOME) {
            SchermataHome(
                stato = stato,
                onNuovaPartita = { navController.navigate(Rotte.CONFIGURAZIONE) },
                onRiprendi = {
                    val inGioco = viewModel.riprendiPartita()
                    navController.navigate(if (inGioco) Rotte.GIOCO else Rotte.DISTRIBUZIONE)
                },
                onRegole = { navController.navigate(Rotte.REGOLE) },
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
                    if (viewModel.iniziaPartita()) {
                        navController.navigate(Rotte.DISTRIBUZIONE) { launchSingleTop = true }
                    }
                },
            )
        }
        composable(Rotte.DISTRIBUZIONE) {
            SchermataDistribuzione(
                stato = stato,
                onSono = { viewModel.avanza() },
                onNascondiEPassa = { viewModel.avanza() },
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
                partita = stato.partita,
                onNuovaPartita = {
                    if (viewModel.iniziaPartita()) {
                        navController.navigate(Rotte.DISTRIBUZIONE) {
                            popUpTo(Rotte.RIVELA) { inclusive = true }
                        }
                    } else {
                        navController.vaiAConfigurazione()
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
