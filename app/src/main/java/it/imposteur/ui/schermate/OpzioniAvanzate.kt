package it.imposteur.ui.schermate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.Configurazione
import it.imposteur.game.Modalita
import it.imposteur.game.Regole
import it.imposteur.ui.componenti.RigaInterruttore
import it.imposteur.ui.componenti.SelettoreSegmenti
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

/**
 * Opzioni avanzate del passo 2: tre schede di gruppo ("Ruoli", "Turni", "Fine partita") sempre aperte.
 * "Impostori a sorpresa" non e' qui: sta nel passo 1.
 */
@Composable
fun OpzioniAvanzate(
    config: Configurazione,
    onCambia: ((Configurazione) -> Configurazione) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spazio.s3)) {
        SchedaGruppo(stringResource(R.string.opz_gruppo_ruoli)) {
            if (config.modalita == Modalita.SENZA_PAROLA) {
                RigaInterruttore(
                    etichetta = stringResource(R.string.config_mostra_categoria),
                    descrizione = stringResource(R.string.opz_desc_vede_categoria),
                    attivo = config.mostraCategoria,
                    onCambia = { v -> onCambia { it.copy(mostraCategoria = v) } },
                )
            }
            RigaInterruttore(
                etichetta = stringResource(R.string.opz_non_primo),
                descrizione = stringResource(R.string.opz_desc_non_primo),
                attivo = config.impostoreNonPrimo,
                onCambia = { v -> onCambia { it.copy(impostoreNonPrimo = v) } },
            )
            RigaInterruttore(
                etichetta = stringResource(R.string.opz_trappola),
                descrizione = stringResource(R.string.opz_desc_trappola),
                attivo = config.partitaTrappola,
                onCambia = { v -> onCambia { it.copy(partitaTrappola = v) } },
            )
        }
        SchedaGruppo(stringResource(R.string.opz_gruppo_turni)) {
            RigaInterruttore(
                etichetta = stringResource(R.string.opz_ordine_casuale),
                descrizione = stringResource(R.string.opz_desc_ordine_casuale),
                attivo = config.ordineCasuale,
                onCambia = { v -> onCambia { it.copy(ordineCasuale = v) } },
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spazio.s2),
                verticalArrangement = Arrangement.spacedBy(Spazio.s2),
            ) {
                Text(stringResource(R.string.opz_giri), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.opz_desc_giri),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SelettoreSegmenti(
                    etichette = (1..Regole.MAX_GIRI).map { it.toString() },
                    selezionato = config.giriIndizi.coerceIn(1, Regole.MAX_GIRI) - 1,
                    onSeleziona = { i -> onCambia { it.copy(giriIndizi = i + 1) } },
                )
            }
        }
        SchedaGruppo(stringResource(R.string.opz_gruppo_fine)) {
            RigaInterruttore(
                etichetta = stringResource(R.string.opz_promemoria),
                descrizione = stringResource(R.string.opz_desc_promemoria),
                attivo = config.promemoriaUltimaPossibilita,
                onCambia = { v -> onCambia { it.copy(promemoriaUltimaPossibilita = v) } },
            )
        }
    }
}

/** Scheda di gruppo: surfaceContainerLow, raggio 28, padding 12 x 20, titolo in primary. */
@Composable
private fun SchedaGruppo(titolo: String, content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        border = bordoLivello(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = Spazio.s3)) {
            Text(
                titolo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = Spazio.s1, bottom = Spazio.s1),
            )
            content()
        }
    }
}
