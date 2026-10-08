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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
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
            Text(
                stringResource(R.string.gioco_titolo),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            TestoAdattivo(
                testo = stringResource(R.string.gioco_inizia, partita.giocatori[partita.primoGiocatore]),
                stile = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.gioco_ordine_titolo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(8.dp))
            val ordine = partita.ordineDiParola()
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(ordine) { posizione, indice ->
                    val primo = posizione == 0
                    val sfondo = if (primo) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                    val testo = if (primo) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(sfondo, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(testo, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${posizione + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = sfondo,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        TestoAdattivo(
                            testo = partita.giocatori[indice],
                            stile = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (primo) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = testo,
                            maxRighe = 1,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
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
