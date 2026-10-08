package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.Modalita
import it.imposteur.game.Partita

@Composable
fun SchermataRivela(partita: Partita?, onNuovaPartita: () -> Unit, onCambiaImpostazioni: () -> Unit) {
    BackHandler(onBack = onCambiaImpostazioni)
    if (partita == null) return

    val nomiImpostori = partita.impostori.sorted().map { partita.giocatori[it] }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
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
        Text(testoImpostori, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
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
        Button(onClick = onNuovaPartita, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.nuova_partita), style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(onClick = onCambiaImpostazioni, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.rivela_cambia_impostazioni), style = MaterialTheme.typography.titleMedium)
        }
    }
}
