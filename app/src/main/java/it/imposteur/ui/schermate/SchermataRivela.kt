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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.Modalita
import it.imposteur.game.Partita

@Composable
fun SchermataRivela(partitaViva: Partita?, onNuovaPartita: () -> Unit, onCambiaImpostazioni: () -> Unit) {
    BackHandler(onBack = onCambiaImpostazioni)
    // Fissa la partita svelata: un cambio di partita durante la transizione non la altera.
    var fissata by remember { mutableStateOf(partitaViva) }
    if (fissata == null && partitaViva != null) fissata = partitaViva
    val partita = fissata ?: return

    val nomiImpostori = partita.impostori.sorted().map { partita.giocatori[it] }
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
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.rivela_titolo),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        val testoImpostori = if (nomiImpostori.size == 1) {
            stringResource(R.string.rivela_impostore, nomiImpostori.first())
        } else {
            stringResource(R.string.rivela_impostori, nomiImpostori.joinToString(", "))
        }
        TestoAdattivo(testoImpostori, MaterialTheme.typography.headlineMedium, maxRighe = 3)
        Text(
            stringResource(R.string.rivela_parola, partita.voce.parola),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        if (partita.modalita == Modalita.PAROLA_AFFINE && partita.voce.affine != null) {
            Text(
                stringResource(R.string.rivela_affine, partita.voce.affine!!),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            stringResource(R.string.rivela_categoria, partita.voce.categoriaNome),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
    Column(
        modifier = Modifier.navigationBarsPadding().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onNuovaPartita, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.rivela_rigioca), style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(onClick = onCambiaImpostazioni, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.rivela_cambia_impostazioni), style = MaterialTheme.typography.titleMedium)
        }
    }
    }
}
