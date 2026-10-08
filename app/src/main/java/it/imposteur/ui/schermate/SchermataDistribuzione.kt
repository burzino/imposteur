package it.imposteur.ui.schermate

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
        when (fase) {
            is StatoDistribuzione.Passaggio -> Passaggio(partita, fase.indice) { onSono(fase) }
            is StatoDistribuzione.Rivelazione -> Rivelazione(partita, fase.indice) { onNascondiEPassa(fase) }
            StatoDistribuzione.Gioco -> Unit
        }
    }

    if (chiediInterruzione) {
        DialogoConferma(
            testo = stringResource(R.string.interrompere_partita),
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

@Composable
internal fun Contenitore(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

@Composable
internal fun Passaggio(partita: Partita, indice: Int, onSono: () -> Unit) {
    val nome = partita.giocatori[indice]
    Contenitore {
        Text(
            stringResource(R.string.distribuzione_indicatore, indice + 1, partita.giocatori.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.distribuzione_passa, nome),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onSono, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text(stringResource(R.string.distribuzione_sono, nome), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
internal fun Rivelazione(
    partita: Partita,
    indice: Int,
    testoPulsante: Int = R.string.distribuzione_nascondi,
    onNascondiEPassa: () -> Unit,
) {
    val contenuto = partita.contenutoPer(indice)
    Contenitore {
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
                Text(
                    contenuto.testo,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
            is ContenutoRuolo.Impostore -> {
                Text(
                    stringResource(R.string.ruolo_sei_impostore),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
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
        Button(onClick = onNascondiEPassa, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text(stringResource(testoPulsante), style = MaterialTheme.typography.titleLarge)
        }
    }
}
