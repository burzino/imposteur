package it.imposteur.ui.schermate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.ui.UiState
import it.imposteur.ui.componenti.DialogoConferma
import it.imposteur.ui.componenti.PulsanteContorno
import it.imposteur.ui.componenti.PulsantePieno
import it.imposteur.ui.componenti.PulsanteTonale
import it.imposteur.ui.theme.Forme
import it.imposteur.ui.theme.Spazio

@Composable
fun SchermataHome(
    stato: UiState,
    onNuovaPartita: () -> Unit,
    onRiprendi: () -> Unit,
    onRegole: () -> Unit,
    onImpostazioni: () -> Unit,
) {
    var chiediNuova by rememberSaveable { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        // Medaglione e titolo centrati nello spazio libero, pulsanti ancorati in basso (design 4.1).
        // Scorre solo se lo schermo e' troppo basso (landscape).
        BoxWithConstraints(modifier = Modifier.widthIn(max = Spazio.larghezzaMax).fillMaxSize()) {
            val altezzaMinima = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = altezzaMinima)
                    .padding(horizontal = Spazio.margineSchermata, vertical = Spazio.s5),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(0.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spazio.s5),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = Spazio.s5),
                ) {
                    Medaglione()
                    Text(
                        text = stringResource(R.string.home_titolo),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spazio.s3),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (stato.erroreCaricamento) {
                        Text(
                            text = stringResource(R.string.errore_caricamento_parole),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    val nuovaAbilitata = !stato.caricamento && !stato.erroreCaricamento
                    val nuova = { if (stato.ripristinabile != null) chiediNuova = true else onNuovaPartita() }
                    if (stato.ripristinabile != null) {
                        PulsantePieno(
                            testo = stringResource(R.string.riprendi_partita),
                            onClick = onRiprendi,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        PulsanteTonale(
                            testo = stringResource(R.string.nuova_partita),
                            onClick = nuova,
                            abilitato = nuovaAbilitata,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        PulsantePieno(
                            testo = stringResource(R.string.nuova_partita),
                            onClick = nuova,
                            abilitato = nuovaAbilitata,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    PulsanteContorno(
                        testo = stringResource(R.string.come_si_gioca),
                        onClick = onRegole,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        // Dopo la Column, così sta sopra: la Column scorrevole intercetterebbe il tocco
        IconButton(onClick = onImpostazioni, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.impostazioni_apri))
        }
    }
    if (chiediNuova) {
        DialogoConferma(
            titolo = stringResource(R.string.partita_in_corso_conferma),
            messaggio = null,
            etichettaSi = stringResource(R.string.nuova_partita),
            etichettaNo = stringResource(R.string.annulla),
            onSi = {
                chiediNuova = false
                onNuovaPartita()
            },
            onNo = { chiediNuova = false },
        )
    }
}

/** Medaglione 120 dp con la maschera dell'icona (sfondo scuro, raggio 36). */
@Composable
private fun Medaglione() {
    Box(
        modifier = Modifier.size(120.dp).clip(Forme.XXL),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_launcher_background), contentDescription = null, modifier = Modifier.fillMaxSize())
        Image(
            painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.requiredSize(168.dp),
        )
    }
}
