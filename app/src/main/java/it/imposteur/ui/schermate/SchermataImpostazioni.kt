package it.imposteur.ui.schermate

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import it.imposteur.data.Segnalazione
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.data.Aspetto
import it.imposteur.data.Tema

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataImpostazioni(
    aspetto: Aspetto,
    segnalazioniSalvate: Int,
    onInviaSuggerimento: (Segnalazione) -> Unit,
    onCancellaSegnalazioni: () -> Unit,
    onCambia: (Aspetto) -> Unit,
    onIndietro: () -> Unit,
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
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.impostazioni_titolo)) },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.indietro))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.tema_titolo),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Column(Modifier.selectableGroup()) {
                opzioni.forEach { (tema, etichetta) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = aspetto.tema == tema,
                                onClick = { onCambia(aspetto.copy(tema = tema)) },
                                role = Role.RadioButton,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = aspetto.tema == tema, onClick = null)
                        Text(
                            stringResource(etichetta),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val attivo = aspetto.tema != Tema.ALTO_CONTRASTO
                val colore = if (attivo) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .toggleable(
                            value = aspetto.coloriDinamici,
                            enabled = attivo,
                            role = Role.Switch,
                            onValueChange = { onCambia(aspetto.copy(coloriDinamici = it)) },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.colori_telefono), style = MaterialTheme.typography.bodyLarge, color = colore)
                        Text(
                            stringResource(R.string.colori_telefono_descrizione),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (attivo) MaterialTheme.colorScheme.onSurfaceVariant else colore,
                        )
                    }
                    Switch(checked = aspetto.coloriDinamici, onCheckedChange = null, enabled = attivo)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(
                stringResource(R.string.segnalazioni_titolo),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            OutlinedTextField(
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
            Button(
                enabled = suggerimento.isNotBlank(),
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
            ) { Text(stringResource(R.string.segnalazioni_invia)) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.segnalazioni_salvate, segnalazioniSalvate),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(onClick = { chiediCancella = true }, enabled = segnalazioniSalvate > 0) {
                    Text(stringResource(R.string.segnalazioni_cancella))
                }
            }
            Text(
                stringResource(R.string.segnalazioni_locale),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (chiediCancella) {
        AlertDialog(
            onDismissRequest = { chiediCancella = false },
            title = { Text(stringResource(R.string.segnalazioni_cancella_conferma)) },
            confirmButton = {
                TextButton(onClick = {
                    chiediCancella = false
                    onCancellaSegnalazioni()
                }) { Text(stringResource(R.string.segnalazioni_cancella_si)) }
            },
            dismissButton = { TextButton(onClick = { chiediCancella = false }) { Text(stringResource(R.string.annulla)) } },
        )
    }
}
