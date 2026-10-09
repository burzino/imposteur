package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.Partita
import it.imposteur.ui.componenti.BarraAzioni
import it.imposteur.ui.componenti.DialogoConferma
import it.imposteur.ui.componenti.PulsantePieno
import it.imposteur.ui.componenti.PulsanteTesto
import it.imposteur.ui.componenti.ScaffoldConBarra
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SchermataGioco(
    partita: Partita?,
    onRivela: () -> Unit,
    onRivedi: () -> Unit,
    onInterrompiPartita: () -> Unit,
    onHome: () -> Unit,
) {
    var chiediInterruzione by rememberSaveable { mutableStateOf(false) }
    var chiediRivela by rememberSaveable { mutableStateOf(false) }
    BackHandler { chiediInterruzione = true }

    if (partita != null) {
        ScaffoldConBarra(
            titolo = stringResource(R.string.gioco_titolo),
            onHome = onHome,
            bottomBar = {
                BarraAzioni {
                    PulsanteTesto(
                        testo = stringResource(R.string.gioco_rivedi),
                        onClick = onRivedi,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    PulsantePieno(
                        testo = stringResource(R.string.gioco_rivela),
                        onClick = { chiediRivela = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spazio.margineSchermata),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    border = bordoLivello(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(Spazio.s5),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spazio.s2),
                    ) {
                        Text(
                            stringResource(R.string.gioco_inizia),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        TestoAdattivo(
                            testo = partita.giocatori[partita.primoGiocatore],
                            stile = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Spacer(Modifier.height(Spazio.s4))
                Text(
                    stringResource(R.string.gioco_istruzioni),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(Spazio.s4))
                val ordine = partita.ordineDiParola()
                val sfondoCarta = MaterialTheme.colorScheme.surfaceContainerLow
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = sfondoCarta,
                    border = bordoLivello(),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spazio.s3),
                        verticalArrangement = Arrangement.spacedBy(Spazio.s2),
                    ) {
                        item(key = "titolo") {
                            Text(
                                stringResource(R.string.gioco_ordine_titolo),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = Spazio.s2, vertical = Spazio.s1),
                            )
                        }
                        for (giro in 1..partita.giriIndizi.coerceIn(1, 3)) {
                            if (partita.giriIndizi > 1) {
                                stickyHeader(key = "giro$giro") {
                                    Text(
                                        stringResource(R.string.gioco_giro_n, giro),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(sfondoCarta)
                                            .padding(horizontal = Spazio.s2, vertical = Spazio.s1),
                                    )
                                }
                            }
                            itemsIndexed(ordine, key = { posizione, _ -> "g$giro-$posizione" }) { posizione, indice ->
                                val primo = giro == 1 && posizione == 0
                                val sfondo = if (primo) MaterialTheme.colorScheme.primaryContainer else sfondoCarta
                                val testo = if (primo) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(sfondo, MaterialTheme.shapes.medium)
                                        .padding(horizontal = Spazio.s3, vertical = Spazio.s2),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${posizione + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                    Spacer(Modifier.width(Spazio.s3))
                                    TestoAdattivo(
                                        testo = partita.giocatori[indice],
                                        stile = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (primo) FontWeight.Bold else FontWeight.Normal,
                                        ),
                                        textAlign = TextAlign.Start,
                                        color = testo,
                                        maxRighe = 1,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Spazio.s4))
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
