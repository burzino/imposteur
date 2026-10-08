package it.imposteur.ui.schermate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import it.imposteur.ui.UiState

@Composable
fun SchermataHome(stato: UiState, onNuovaPartita: () -> Unit, onRiprendi: () -> Unit, onRegole: () -> Unit) {
    var chiediNuova by rememberSaveable { mutableStateOf(false) }
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
            text = stringResource(R.string.home_titolo),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        if (stato.erroreCaricamento) {
            Text(
                text = stringResource(R.string.errore_caricamento_parole),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
        if (stato.ripristinabile != null) {
            Button(
                onClick = onRiprendi,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(stringResource(R.string.riprendi_partita), style = MaterialTheme.typography.titleMedium) }
        }
        Button(
            onClick = { if (stato.ripristinabile != null) chiediNuova = true else onNuovaPartita() },
            enabled = !stato.caricamento && !stato.erroreCaricamento,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(stringResource(R.string.nuova_partita), style = MaterialTheme.typography.titleMedium) }
        OutlinedButton(
            onClick = onRegole,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(stringResource(R.string.come_si_gioca), style = MaterialTheme.typography.titleMedium) }
    }
    if (chiediNuova) {
        AlertDialog(
            onDismissRequest = { chiediNuova = false },
            title = { Text(stringResource(R.string.partita_in_corso_conferma)) },
            confirmButton = {
                TextButton(onClick = {
                    chiediNuova = false
                    onNuovaPartita()
                }) { Text(stringResource(R.string.nuova_partita)) }
            },
            dismissButton = { TextButton(onClick = { chiediNuova = false }) { Text(stringResource(R.string.annulla)) } },
        )
    }
}
