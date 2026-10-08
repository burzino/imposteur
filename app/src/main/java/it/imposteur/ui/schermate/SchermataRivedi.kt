package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import it.imposteur.R
import it.imposteur.game.Partita
import it.imposteur.ui.Revisione

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataRivedi(
    partita: Partita?,
    revisione: Revisione?,
    onScegli: (Int) -> Unit,
    onSono: () -> Unit,
    onInterrompi: () -> Unit,
    onTornaAElenco: () -> Unit,
    onChiudi: () -> Unit,
) {
    SchermoProtetto()

    // Il ruolo non resta visibile se l'app va in background (include rotazione).
    val lifecycleOwner = LocalLifecycleOwner.current
    val interrompi by rememberUpdatedState(onInterrompi)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) interrompi()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler {
        when {
            revisione == null -> onChiudi()
            revisione.rivelato -> onChiudi() // come "Nascondi"
            else -> onTornaAElenco()
        }
    }

    if (partita == null) return

    if (revisione != null && revisione.indice in partita.giocatori.indices) {
        if (revisione.rivelato) {
            Rivelazione(partita, revisione.indice, R.string.rivedi_nascondi) { onChiudi() }
        } else {
            Passaggio(partita, revisione.indice) { onSono() }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rivedi_titolo)) },
                navigationIcon = {
                    IconButton(onClick = onChiudi) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.indietro))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            itemsIndexed(partita.giocatori) { i, nome ->
                ListItem(
                    headlineContent = { Text(nome) },
                    modifier = Modifier.fillMaxWidth().clickable { onScegli(i) },
                )
                HorizontalDivider()
            }
        }
    }
}
