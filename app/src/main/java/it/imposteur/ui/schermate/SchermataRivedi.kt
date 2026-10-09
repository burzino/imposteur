package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import it.imposteur.R
import it.imposteur.game.Partita
import it.imposteur.ui.Revisione
import it.imposteur.ui.componenti.Avatar
import it.imposteur.ui.componenti.BarraAzioni
import it.imposteur.ui.componenti.PulsanteContorno
import it.imposteur.ui.componenti.ScaffoldConBarra
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

@Composable
fun SchermataRivedi(
    partita: Partita?,
    revisione: Revisione?,
    onScegli: (Int) -> Unit,
    onSono: () -> Unit,
    onInterrompi: () -> Unit,
    onTornaAElenco: () -> Unit,
    onChiudi: () -> Unit,
    onHome: () -> Unit,
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
        ScaffoldConBarra(
            titolo = stringResource(R.string.rivedi_titolo),
            onIndietro = if (revisione.rivelato) onChiudi else onTornaAElenco,
            onHome = onHome,
        ) {
            if (revisione.rivelato) {
                Rivelazione(partita, revisione.indice, R.string.rivedi_nascondi, mostraFila = false) { onChiudi() }
            } else {
                Passaggio(partita, revisione.indice, mostraAvanzamento = false) { onSono() }
            }
        }
        return
    }

    ScaffoldConBarra(
        titolo = stringResource(R.string.rivedi_titolo),
        onIndietro = onChiudi,
        onHome = onHome,
        bottomBar = {
            BarraAzioni {
                PulsanteContorno(
                    testo = stringResource(R.string.rivedi_torna_gioco),
                    onClick = onChiudi,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = Spazio.margineSchermata, vertical = Spazio.s2)) {
                Text(
                    stringResource(R.string.rivedi_intestazione),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(R.string.rivedi_sottotitolo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Spazio.margineSchermata),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = bordoLivello(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(Spazio.s2)) {
                        partita.giocatori.forEachIndexed { i, nome ->
                            val descrizione = stringResource(R.string.rivedi_cd_giocatore, nome)
                            Surface(
                                onClick = { onScegli(i) },
                                shape = MaterialTheme.shapes.large,
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 64.dp)
                                    .semantics(mergeDescendants = true) { contentDescription = descrizione },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 64.dp)
                                        .padding(horizontal = Spazio.s3, vertical = Spazio.s2),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spazio.s4),
                                ) {
                                    Avatar(nome, i)
                                    TestoAdattivo(
                                        testo = nome,
                                        stile = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f),
                                        maxRighe = 2,
                                        textAlign = TextAlign.Start,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
