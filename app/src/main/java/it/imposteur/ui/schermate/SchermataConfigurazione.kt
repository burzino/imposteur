package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.ErroreConfigurazione
import it.imposteur.game.PassoConfigurazione
import it.imposteur.ui.ImpostoreViewModel
import it.imposteur.ui.UiState
import it.imposteur.ui.componenti.BarraAzioni
import it.imposteur.ui.componenti.DialogoConferma
import it.imposteur.ui.componenti.IndicatorePassi
import it.imposteur.ui.componenti.PulsanteContorno
import it.imposteur.ui.componenti.PulsantePieno
import it.imposteur.ui.componenti.PulsanteTesto
import it.imposteur.ui.componenti.ScaffoldConBarra
import it.imposteur.ui.theme.Spazio

private const val NUMERO_PASSI = 4

/**
 * Un passo della Configurazione (1..4). [onIndietro] vale per freccia, pulsante "Indietro" e tasto di sistema
 * (mai bloccati dalla validazione); [onVaiAPasso] per indicatore e "Modifica"; [onAvanti] per "Avanti";
 * [onInizia] per "Inizia" in barra e nel passo 4.
 */
@Composable
fun SchermataConfigurazione(
    passo: Int,
    stato: UiState,
    viewModel: ImpostoreViewModel,
    onIndietro: () -> Unit,
    onVaiAPasso: (Int) -> Unit,
    onAvanti: () -> Unit,
    onInizia: () -> Unit,
) {
    BackHandler(onBack = onIndietro)
    val corrente = PassoConfigurazione.entries[passo - 1]
    val errore = stato.errorePasso(corrente)
    var chiediAzzera by rememberSaveable { mutableStateOf(false) }

    val nomiPassi = listOf(
        stringResource(R.string.passo_giocatori),
        stringResource(R.string.passo_opzioni),
        stringResource(R.string.config_categorie),
        stringResource(R.string.passo_riepilogo),
    )
    val nomeCorrente = nomiPassi[passo - 1]

    ScaffoldConBarra(
        titolo = stringResource(R.string.config_titolo),
        onIndietro = onIndietro,
        azioni = {
            if (passo < NUMERO_PASSI) {
                PulsanteTesto(testo = stringResource(R.string.config_inizia), onClick = onInizia)
            }
        },
        bottomBar = {
            BarraAzioni {
                if (errore != null) {
                    Text(
                        stringResource(testoErrore(errore)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spazio.s2), modifier = Modifier.fillMaxWidth()) {
                    if (passo > 1) {
                        PulsanteContorno(
                            testo = stringResource(R.string.indietro),
                            onClick = onIndietro,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    PulsantePieno(
                        testo = stringResource(if (passo < NUMERO_PASSI) R.string.passo_avanti else R.string.config_inizia),
                        onClick = if (passo < NUMERO_PASSI) onAvanti else onInizia,
                        abilitato = errore == null,
                        modifier = Modifier.weight(if (passo > 1) 2f else 1f),
                    )
                }
            }
        },
    ) { _ ->
        Column(Modifier.fillMaxSize()) {
            IndicatorePassi(
                passo = passo,
                nomi = nomiPassi,
                etichetta = stringResource(R.string.passo_etichetta, passo, NUMERO_PASSI, nomeCorrente),
                descrizione = stringResource(R.string.passo_etichetta_cd, passo, NUMERO_PASSI, nomeCorrente),
                onPasso = onVaiAPasso,
                modifier = Modifier.padding(horizontal = Spazio.margineSchermata, vertical = Spazio.s2),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spazio.margineSchermata, vertical = Spazio.s2),
                verticalArrangement = Arrangement.spacedBy(Spazio.s4),
            ) {
                IntestazionePasso(passo, stato.opzioniAttive)
                when (corrente) {
                    PassoConfigurazione.GIOCATORI -> PassoGiocatori(stato, viewModel)
                    PassoConfigurazione.OPZIONI -> PassoOpzioni(stato, viewModel)
                    PassoConfigurazione.CATEGORIE ->
                        PassoCategorie(stato, viewModel, onChiediAzzera = { chiediAzzera = true })
                    PassoConfigurazione.RIEPILOGO -> PassoRiepilogo(stato, onModifica = onVaiAPasso)
                }
                Spacer(Modifier.height(96.dp))
            }
        }
    }

    if (chiediAzzera) {
        DialogoConferma(
            titolo = stringResource(R.string.config_azzera_conferma),
            messaggio = null,
            etichettaSi = stringResource(R.string.config_azzera),
            etichettaNo = stringResource(R.string.annulla),
            onSi = {
                chiediAzzera = false
                viewModel.azzeraParole()
            },
            onNo = { chiediAzzera = false },
        )
    }
}

/** Titolo e riga del passo; nel passo 2 a destra il badge "N attive" (assente se 0). */
@Composable
private fun IntestazionePasso(passo: Int, opzioniAttive: Int) {
    val (titolo, sottotitolo) = when (passo) {
        1 -> R.string.passo1_titolo to R.string.passo1_sottotitolo
        2 -> R.string.opz_titolo to R.string.passo2_sottotitolo
        3 -> R.string.config_categorie to R.string.passo3_sottotitolo
        else -> R.string.passo4_titolo to R.string.passo4_sottotitolo
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spazio.s1)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spazio.s3)) {
            Text(stringResource(titolo), style = MaterialTheme.typography.headlineSmall)
            if (passo == 2 && opzioniAttive > 0) BadgeAttive(opzioniAttive)
        }
        Text(
            stringResource(sottotitolo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun testoErrore(errore: ErroreConfigurazione): Int = when (errore) {
    ErroreConfigurazione.NessunaCategoria -> R.string.config_nessuna_categoria
    ErroreConfigurazione.PoolVuoto -> R.string.config_pool_vuoto
    is ErroreConfigurazione.NomeDuplicato -> R.string.config_nome_duplicato
    else -> R.string.config_non_valida
}
