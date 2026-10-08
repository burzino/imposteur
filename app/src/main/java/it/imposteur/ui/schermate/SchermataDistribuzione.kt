package it.imposteur.ui.schermate

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import it.imposteur.R
import it.imposteur.game.ContenutoRuolo
import it.imposteur.game.Modalita
import it.imposteur.game.Partita
import it.imposteur.game.StatoDistribuzione
import it.imposteur.ui.UiState

@Composable
fun SchermataDistribuzione(
    stato: UiState,
    onSono: (StatoDistribuzione) -> Unit,
    onNascondiEPassa: (StatoDistribuzione) -> Unit,
    onInterrompiRivelazione: () -> Unit,
    onFineDistribuzione: () -> Unit,
    onInterrompiPartita: () -> Unit,
    onHome: () -> Unit,
) {
    var chiediInterruzione by rememberSaveable { mutableStateOf(false) }
    BackHandler { chiediInterruzione = true }

    // Schermo sempre acceso durante la distribuzione.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Niente screenshot né anteprima in Recents mentre un ruolo può essere visibile.
    SchermoProtetto()

    // Il ruolo non resta visibile se l'app va in background (include rotazione).
    val lifecycleOwner = LocalLifecycleOwner.current
    val interrompi by rememberUpdatedState(onInterrompiRivelazione)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) interrompi()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val partita = stato.partita
    val fase = stato.distribuzione
    LaunchedEffect(fase) {
        if (fase is StatoDistribuzione.Gioco) onFineDistribuzione()
    }

    if (partita != null) {
        ScaffoldConHome(titolo = R.string.distribuzione_titolo, onHome = onHome) {
            when (fase) {
                is StatoDistribuzione.Passaggio -> Passaggio(partita, fase.indice) { onSono(fase) }
                is StatoDistribuzione.Rivelazione -> Rivelazione(partita, fase.indice) { onNascondiEPassa(fase) }
                StatoDistribuzione.Gioco -> Unit
            }
        }
    }

    if (chiediInterruzione) {
        DialogoConferma(
            titolo = stringResource(R.string.interrompere_partita),
            messaggio = stringResource(R.string.interrompere_messaggio),
            etichettaSi = stringResource(R.string.interrompere_conferma),
            etichettaNo = stringResource(R.string.interrompere_continua),
            onSi = {
                chiediInterruzione = false
                onInterrompiPartita()
            },
            onNo = { chiediInterruzione = false },
        )
    }
}

/** Attiva FLAG_SECURE finché il composable è in composizione. */
@Composable
internal fun SchermoProtetto() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.trovaActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

private tailrec fun Context.trovaActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.trovaActivity()
    else -> null
}

/** Struttura fissa: contenuto al centro, pulsante ancorato in basso (zona del pollice). */
@Composable
internal fun Contenitore(azione: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.navigationBarsPadding()) { azione() }
    }
}

@Composable
internal fun Passaggio(partita: Partita, indice: Int, onSono: () -> Unit) {
    val nome = partita.giocatori[indice]
    Contenitore(
        azione = {
            Button(onClick = onSono, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Text(stringResource(R.string.distribuzione_sono, nome), style = MaterialTheme.typography.titleLarge)
            }
        },
    ) {
        Text(
            stringResource(R.string.distribuzione_indicatore, indice + 1, partita.giocatori.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TestoAdattivo(
            testo = stringResource(R.string.distribuzione_passa, nome),
            stile = MaterialTheme.typography.displaySmall,
        )
        Text(
            stringResource(R.string.distribuzione_non_guardare),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Ritardo anti doppio tocco prima di abilitare "Nascondi e passa". */
private const val RITARDO_NASCONDI_MS = 400L

@Composable
internal fun Rivelazione(
    partita: Partita,
    indice: Int,
    testoPulsante: Int? = null,
    onNascondiEPassa: () -> Unit,
) {
    val contenuto = partita.contenutoPer(indice)
    val ultimo = indice == partita.giocatori.size - 1
    val etichettaPulsante = testoPulsante
        ?: if (ultimo) R.string.distribuzione_nascondi_ultimo else R.string.distribuzione_nascondi
    val haptic = LocalHapticFeedback.current

    // Segnale alla comparsa del ruolo: un solo impulso, identico per tutti (impostore compreso).
    LaunchedEffect(indice) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    // Il pulsante si abilita dopo un breve ritardo, per evitare il doppio tocco.
    var abilitato by remember(indice) { mutableStateOf(false) }
    LaunchedEffect(indice) {
        delay(RITARDO_NASCONDI_MS)
        abilitato = true
    }

    Contenitore(
        azione = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNascondiEPassa()
                },
                enabled = abilitato,
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Text(stringResource(etichettaPulsante), style = MaterialTheme.typography.titleLarge)
            }
        },
    ) {
        Text(
            stringResource(R.string.distribuzione_indicatore, indice + 1, partita.giocatori.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when (contenuto) {
            is ContenutoRuolo.ParolaSegreta -> {
                val etichetta = if (partita.modalita == Modalita.PAROLA_AFFINE) {
                    R.string.ruolo_la_tua_parola_e
                } else {
                    R.string.ruolo_la_parola_e
                }
                Text(
                    stringResource(etichetta),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                TestoAdattivo(
                    testo = contenuto.testo,
                    stile = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is ContenutoRuolo.Impostore -> {
                TestoAdattivo(
                    testo = stringResource(R.string.ruolo_sei_impostore),
                    stile = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                contenuto.categoria?.let {
                    Text(
                        stringResource(R.string.ruolo_categoria, it),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
