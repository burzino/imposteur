package it.imposteur.ui.navigazione

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
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
import it.imposteur.game.PassoConfigurazione
import it.imposteur.ui.ImpostoreViewModel
import it.imposteur.ui.schermate.SchermataConfigurazione
import it.imposteur.ui.schermate.SchermataDistribuzione
import it.imposteur.ui.schermate.SchermataGioco
import it.imposteur.ui.schermate.SchermataHome
import it.imposteur.ui.schermate.SchermataImpostazioni
import it.imposteur.ui.schermate.SchermataRegole
import it.imposteur.ui.schermate.SchermataRivela
import it.imposteur.ui.schermate.SchermataRivedi

object Rotte {
    const val HOME = "home"
    const val REGOLE = "regole"
    const val CONFIGURAZIONE = "configurazione/{passo}"
    const val DISTRIBUZIONE = "distribuzione"
    const val GIOCO = "gioco"
    const val RIVELA = "rivela"
    const val RIVEDI = "rivedi"
    const val IMPOSTAZIONI = "impostazioni"
}

/** Rotta del passo n (1..5) della Configurazione. */
private fun rottaPasso(passo: Int) = "configurazione/$passo"

private const val ARG_PASSO = "passo"

/** Numero del passo di una voce della Configurazione, o null per le altre destinazioni. */
private fun NavBackStackEntry.passo(): Int? =
    if (destination.route == Rotte.CONFIGURAZIONE) arguments?.getInt(ARG_PASSO) else null

/** Apre sempre il passo 1 (specifiche 4.2). */
private fun NavHostController.vaiAConfigurazione() {
    navigate(rottaPasso(1)) {
        popUpTo(Rotte.HOME)
    }
}

private fun NavHostController.vaiAHome(rottaCorrente: String) {
    if (!inRotta(rottaCorrente)) return
    navigate(Rotte.HOME) {
        popUpTo(Rotte.HOME) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.inRotta(rotta: String) = currentDestination?.route == rotta

@Composable
fun ImpostoreNavHost(viewModel: ImpostoreViewModel, navController: NavHostController = rememberNavController()) {
    val stato by viewModel.stato.collectAsStateWithLifecycle()
    val revisione by viewModel.revisione.collectAsStateWithLifecycle()

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
            (rotta == Rotte.DISTRIBUZIONE || rotta == Rotte.GIOCO ||
                rotta == Rotte.RIVELA || rotta == Rotte.RIVEDI)
        ) {
            navController.navigate(Rotte.HOME) { popUpTo(0) }
        }
    }

    // Cambio passo: barra, indicatore e barra azioni restano fermi; scorre solo il contenuto
    // (SchermataConfigurazione), come sul web. Qui si azzerano le transizioni tra passi.
    fun trapassoTraPassi(iniziale: NavBackStackEntry, finale: NavBackStackEntry) =
        iniziale.passo() != null && finale.passo() != null
    val ingressoPasso: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
        if (trapassoTraPassi(initialState, targetState)) EnterTransition.None else null
    }
    val uscitaPasso: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
        if (trapassoTraPassi(initialState, targetState)) ExitTransition.None else null
    }
    // Ultimo passo mostrato: dà la direzione dello scorrimento (verso destra avanzando).
    val ultimoPasso = remember { intArrayOf(0) }

    NavHost(navController = navController, startDestination = Rotte.HOME) {
        composable(Rotte.HOME) {
            SchermataHome(
                stato = stato,
                onNuovaPartita = {
                    if (navController.inRotta(Rotte.HOME)) {
                        navController.navigate(rottaPasso(1)) { launchSingleTop = true }
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
                onImpostazioni = {
                    if (navController.inRotta(Rotte.HOME)) {
                        navController.navigate(Rotte.IMPOSTAZIONI) { launchSingleTop = true }
                    }
                },
            )
        }
        composable(Rotte.IMPOSTAZIONI) {
            val aspetto by viewModel.aspetto.collectAsStateWithLifecycle()
            val salvate by viewModel.segnalazioniSalvate.collectAsStateWithLifecycle()
            SchermataImpostazioni(
                aspetto = aspetto,
                segnalazioniSalvate = salvate,
                onInviaSuggerimento = { viewModel.salvaSegnalazione(it) },
                onCancellaSegnalazioni = { viewModel.cancellaSegnalazioni() },
                onCambia = { viewModel.impostaAspetto(it) },
                onDurataPressione = { viewModel.impostaDurataPressione(it) },
                onIndietro = { if (navController.inRotta(Rotte.IMPOSTAZIONI)) navController.popBackStack() },
                onHome = { navController.vaiAHome(Rotte.IMPOSTAZIONI) },
            )
        }
        composable(Rotte.REGOLE) {
            SchermataRegole(
                onIndietro = { navController.popBackStack() },
                onHome = { navController.vaiAHome(Rotte.REGOLE) },
            )
        }
        composable(
            route = Rotte.CONFIGURAZIONE,
            arguments = listOf(navArgument(ARG_PASSO) { type = NavType.IntType; defaultValue = 1 }),
            enterTransition = ingressoPasso,
            exitTransition = uscitaPasso,
            popEnterTransition = ingressoPasso,
            popExitTransition = uscitaPasso,
        ) { voce ->
            val passo = (voce.arguments?.getInt(ARG_PASSO) ?: 1).coerceIn(1, 5)
            val direzione = remember(voce.id) { if (passo >= ultimoPasso[0]) 1 else -1 }
            SideEffect { ultimoPasso[0] = passo }
            // Guardia contro il doppio tocco: si agisce solo se questo passo e' ancora quello in cima.
            fun inQuestoPasso() = navController.currentBackStackEntry?.passo() == passo
            fun vaiAPasso(n: Int) {
                if (!inQuestoPasso()) return
                viewModel.salvaOra()
                navController.navigate(rottaPasso(n.coerceIn(1, 5)))
            }
            SchermataConfigurazione(
                passo = passo,
                direzione = direzione,
                stato = stato,
                viewModel = viewModel,
                onIndietro = {
                    if (inQuestoPasso()) {
                        viewModel.salvaOra()
                        if (passo == 1) navController.popBackStack(Rotte.HOME, inclusive = false)
                        else navController.popBackStack()
                    }
                },
                onVaiAPasso = ::vaiAPasso,
                onAvanti = { if (stato.errorePasso(PassoConfigurazione.entries[passo - 1]) == null) vaiAPasso(passo + 1) },
                onInizia = {
                    if (inQuestoPasso()) {
                        val primo = stato.primoPassoNonValido
                        if (primo == null) {
                            if (viewModel.iniziaPartita()) {
                                navController.navigate(Rotte.DISTRIBUZIONE) { launchSingleTop = true }
                            }
                        } else if (primo.ordinal + 1 != passo) {
                            // Porta al primo passo non valido sostituendo quello corrente (CA-102).
                            viewModel.salvaOra()
                            navController.navigate(rottaPasso(primo.ordinal + 1)) {
                                popUpTo(Rotte.CONFIGURAZIONE) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }
        composable(Rotte.DISTRIBUZIONE) {
            val aspettoDistribuzione by viewModel.aspetto.collectAsStateWithLifecycle()
            SchermataDistribuzione(
                stato = stato,
                durataPressioneMs = aspettoDistribuzione.durataPressioneMs,
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
                onHome = {
                    if (navController.inRotta(Rotte.DISTRIBUZIONE)) {
                        viewModel.sospendiPartita()
                        navController.vaiAHome(Rotte.DISTRIBUZIONE)
                    }
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
                onRivedi = {
                    if (navController.inRotta(Rotte.GIOCO)) {
                        viewModel.chiudiRevisione()
                        navController.navigate(Rotte.RIVEDI) { launchSingleTop = true }
                    }
                },
                onInterrompiPartita = {
                    viewModel.terminaPartita()
                    navController.vaiAConfigurazione()
                },
                onHome = {
                    if (navController.inRotta(Rotte.GIOCO)) {
                        viewModel.sospendiPartita()
                        navController.vaiAHome(Rotte.GIOCO)
                    }
                },
            )
        }
        composable(Rotte.RIVEDI) {
            val aspettoRivedi by viewModel.aspetto.collectAsStateWithLifecycle()
            SchermataRivedi(
                partita = stato.partita,
                durataPressioneMs = aspettoRivedi.durataPressioneMs,
                revisione = revisione,
                onScegli = { viewModel.scegliRevisione(it) },
                onSono = { viewModel.rivelaRevisione() },
                onInterrompi = { viewModel.interrompiRevisione() },
                onTornaAElenco = { viewModel.tornaAElencoRevisione() },
                onChiudi = {
                    viewModel.chiudiRevisione()
                    if (navController.inRotta(Rotte.RIVEDI)) navController.popBackStack()
                },
                onHome = {
                    if (navController.inRotta(Rotte.RIVEDI)) {
                        viewModel.sospendiPartita()
                        navController.vaiAHome(Rotte.RIVEDI)
                    }
                },
            )
        }
        composable(Rotte.RIVELA) {
            SchermataRivela(
                partitaViva = stato.partita,
                onSegnala = { viewModel.salvaSegnalazione(it) },
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
                onHome = { navController.vaiAHome(Rotte.RIVELA) },
            )
        }
    }
}
