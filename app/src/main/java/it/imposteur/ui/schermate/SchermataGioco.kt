package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.Partita

@Composable
fun SchermataGioco(
    partita: Partita?,
    onRivela: () -> Unit,
    onRivedi: () -> Unit,
    onInterrompiPartita: () -> Unit,
) {
    var chiediInterruzione by rememberSaveable { mutableStateOf(false) }
    var chiediRivela by rememberSaveable { mutableStateOf(false) }
    BackHandler { chiediInterruzione = true }

    if (partita != null) {
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
            ) {
                Text(
                    stringResource(R.string.gioco_titolo),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                TestoAdattivo(
                    testo = stringResource(R.string.gioco_inizia, partita.giocatori[partita.primoGiocatore]),
                    stile = MaterialTheme.typography.displaySmall,
                )
                Text(
                    stringResource(R.string.gioco_istruzioni),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                modifier = Modifier.navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onRivedi) {
                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.gioco_rivedi))
                }
                FilledTonalButton(
                    onClick = { chiediRivela = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text(stringResource(R.string.gioco_rivela), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (chiediRivela) {
        DialogoConferma(
            titolo = stringResource(R.string.gioco_conferma_rivela),
            messaggio = stringResource(R.string.gioco_conferma_messaggio),
            etichettaSi = stringResource(R.string.gioco_conferma_si),
            etichettaNo = stringResource(R.string.gioco_conferma_no),
            onSi = {
                chiediRivela = false
                onRivela()
            },
            onNo = { chiediRivela = false },
        )
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
