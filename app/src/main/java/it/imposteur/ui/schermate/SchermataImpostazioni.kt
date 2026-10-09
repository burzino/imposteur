package it.imposteur.ui.schermate

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.data.Aspetto
import it.imposteur.data.Segnalazione
import it.imposteur.data.Tema
import it.imposteur.ui.componenti.DialogoConferma
import it.imposteur.ui.componenti.PulsantePieno
import it.imposteur.ui.componenti.PulsanteContorno
import it.imposteur.ui.componenti.RigaInterruttore
import it.imposteur.ui.componenti.ScaffoldConBarra
import it.imposteur.ui.componenti.SelettoreSegmenti
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun SchermataImpostazioni(
    aspetto: Aspetto,
    segnalazioniSalvate: Int,
    onInviaSuggerimento: (Segnalazione) -> Unit,
    onCancellaSegnalazioni: () -> Unit,
    onCambia: (Aspetto) -> Unit,
    onIndietro: () -> Unit,
    onHome: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var suggerimento by rememberSaveable { mutableStateOf("") }
    var chiediCancella by rememberSaveable { mutableStateOf(false) }
    val messaggioSalvata = stringResource(R.string.segnala_salvata)
    val opzioni = listOf(
        Tema.SISTEMA to R.string.tema_sistema,
        Tema.CHIARO to R.string.tema_chiaro,
        Tema.SCURO to R.string.tema_scuro,
        Tema.ALTO_CONTRASTO to R.string.tema_alto_contrasto,
    )
    ScaffoldConBarra(
        titolo = stringResource(R.string.impostazioni_titolo),
        onIndietro = onIndietro,
        onHome = onHome,
        snackbarHost = { SnackbarHost(snackbar) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spazio.margineSchermata),
            verticalArrangement = Arrangement.spacedBy(Spazio.s3),
        ) {
            Scheda {
                Text(
                    stringResource(R.string.tema_titolo),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                // Quattro posizioni in un unico gruppo connesso 2x2.
                SelettoreSegmenti(
                    etichette = opzioni.map { stringResource(it.second) },
                    selezionato = opzioni.indexOfFirst { it.first == aspetto.tema },
                    onSeleziona = { onCambia(aspetto.copy(tema = opzioni[it].first)) },
                    colonne = 2,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    RigaInterruttore(
                        etichetta = stringResource(R.string.colori_telefono),
                        descrizione = stringResource(R.string.colori_telefono_descrizione),
                        attivo = aspetto.coloriDinamici,
                        abilitato = aspetto.tema != Tema.ALTO_CONTRASTO,
                        onCambia = { onCambia(aspetto.copy(coloriDinamici = it)) },
                    )
                }
            }
            Scheda {
                Text(
                    stringResource(R.string.segnalazioni_titolo),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                TextField(
                    value = suggerimento,
                    onValueChange = { suggerimento = it.take(500) },
                    label = { Text(stringResource(R.string.segnalazioni_suggerimenti)) },
                    supportingText = {
                        Text(
                            stringResource(R.string.segnala_contatore, suggerimento.length, 500),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                PulsantePieno(
                    testo = stringResource(R.string.segnalazioni_invia),
                    abilitato = suggerimento.isNotBlank(),
                    onClick = {
                        onInviaSuggerimento(
                            Segnalazione(
                                tipo = "app",
                                istante = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString(),
                                nota = suggerimento.trim(),
                            ),
                        )
                        suggerimento = ""
                        scope.launch { snackbar.showSnackbar(messaggioSalvata) }
                    },
                    modifier = Modifier.align(Alignment.End),
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.segnalazioni_salvate, segnalazioniSalvate),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    PulsanteContorno(
                        testo = stringResource(R.string.segnalazioni_cancella),
                        onClick = { chiediCancella = true },
                        abilitato = segnalazioniSalvate > 0,
                    )
                }
                Text(
                    stringResource(R.string.segnalazioni_locale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (chiediCancella) {
        DialogoConferma(
            titolo = stringResource(R.string.segnalazioni_cancella_conferma),
            messaggio = null,
            etichettaSi = stringResource(R.string.segnalazioni_cancella_si),
            etichettaNo = stringResource(R.string.annulla),
            onSi = {
                chiediCancella = false
                onCancellaSegnalazioni()
            },
            onNo = { chiediCancella = false },
        )
    }
}

/** Scheda: contenitore surfaceContainerLow, raggio 28, padding 20. */
@Composable
private fun Scheda(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = bordoLivello(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(Spazio.s3),
            content = content,
        )
    }
}
