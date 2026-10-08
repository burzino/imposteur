package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import it.imposteur.data.FormatoSegnalazioni
import it.imposteur.data.MotivoSegnalazione
import it.imposteur.data.Segnalazione
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SchermataRivela(partitaViva: Partita?, onSegnala: (Segnalazione) -> Unit, onNuovaPartita: () -> Unit, onCambiaImpostazioni: () -> Unit, onHome: () -> Unit) {
    BackHandler(onBack = onCambiaImpostazioni)
    // Fissa la partita svelata: un cambio di partita durante la transizione non la altera.
    var fissata by remember { mutableStateOf(partitaViva) }
    if (fissata == null && partitaViva != null) fissata = partitaViva
    val partita = fissata ?: return

    val nomiImpostori = partita.impostori.sorted().map { partita.giocatori[it] }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var segnalando by rememberSaveable { mutableStateOf(false) }
    val messaggioSalvata = stringResource(R.string.segnala_salvata)
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(stringResource(R.string.rivela_titolo)) },
                actions = { AzioneHome(onHome) },
            )
        },
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
        val testoImpostori = if (nomiImpostori.isEmpty()) {
            stringResource(R.string.rivela_trappola)
        } else if (nomiImpostori.size == 1) {
            stringResource(R.string.rivela_impostore, nomiImpostori.first())
        } else {
            stringResource(R.string.rivela_impostori, nomiImpostori.joinToString(", "))
        }
        if (partita.trappola) {
            TestoAdattivo(
                testoImpostori,
                MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.tertiary,
                maxRighe = 3,
            )
        } else {
            TestoAdattivo(testoImpostori, MaterialTheme.typography.headlineMedium, maxRighe = 3)
        }
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
        if (partita.promemoriaUltimaPossibilita && !partita.trappola) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.rivela_promemoria),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onNuovaPartita, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.rivela_rigioca), style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(onClick = onCambiaImpostazioni, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.rivela_cambia_impostazioni), style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = { segnalando = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.segnala_pulsante), style = MaterialTheme.typography.bodyMedium)
        }
    }
    }
    }
    if (segnalando) {
        DialogoSegnalaCoppia(
            parola = partita.voce.parola,
            affine = partita.voce.affine.takeIf { partita.modalita == Modalita.PAROLA_AFFINE },
            categoria = partita.voce.categoriaNome,
            onAnnulla = { segnalando = false },
            onSalva = { motivi, nota, propParola, propAffine ->
                segnalando = false
                onSegnala(
                    Segnalazione(
                        tipo = "coppia",
                        istante = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString(),
                        categoriaId = partita.voce.categoriaId,
                        parola = partita.voce.parola,
                        affine = partita.voce.affine,
                        modalita = partita.modalita.name,
                        motivi = motivi,
                        nota = nota,
                        propostaParola = propParola,
                        propostaAffine = propAffine,
                    ),
                )
                scope.launch { snackbar.showSnackbar(messaggioSalvata) }
            },
        )
    }
}

private const val MAX_COMMENTO = 500
private const val MAX_PROPOSTA = 40

@Composable
private fun DialogoSegnalaCoppia(
    parola: String,
    affine: String?,
    categoria: String,
    onAnnulla: () -> Unit,
    onSalva: (List<MotivoSegnalazione>, String, String?, String?) -> Unit,
) {
    var motivi by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var nota by rememberSaveable { mutableStateOf("") }
    var propParola by rememberSaveable { mutableStateOf("") }
    var propAffine by rememberSaveable { mutableStateOf("") }
    val proposta = FormatoSegnalazioni.normalizza(
        Segnalazione(tipo = "coppia", istante = "", propostaParola = propParola, propostaAffine = propAffine),
    )
    val propostaValida = FormatoSegnalazioni.propostaValida(proposta)
    val propostaMezza = (proposta.propostaParola == null) != (proposta.propostaAffine == null)
    val propostaUguale = proposta.propostaParola != null && proposta.propostaAffine != null && !propostaValida
    val etichette = listOf(
        MotivoSegnalazione.TROPPO_SIMILI to R.string.segnala_motivo_simili,
        MotivoSegnalazione.TROPPO_DIVERSE to R.string.segnala_motivo_diverse,
        MotivoSegnalazione.POCO_CONOSCIUTA to R.string.segnala_motivo_poco_conosciuta,
        MotivoSegnalazione.CATEGORIA_SBAGLIATA to R.string.segnala_motivo_categoria,
    )
    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text(stringResource(R.string.segnala_titolo)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    if (affine != null) stringResource(R.string.segnala_sottotitolo_coppia, parola, affine) else parola,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(stringResource(R.string.segnala_categoria, categoria), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                etichette.forEach { (motivo, testo) ->
                    val scelto = motivo.name in motivi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .toggleable(
                                value = scelto,
                                role = Role.Checkbox,
                                onValueChange = { motivi = if (it) motivi + motivo.name else motivi - motivo.name },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = scelto, onCheckedChange = null)
                        Text(stringResource(testo), modifier = Modifier.padding(start = 12.dp))
                    }
                }
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it.take(MAX_COMMENTO) },
                    label = { Text(stringResource(R.string.segnala_commento)) },
                    supportingText = {
                        Text(
                            stringResource(R.string.segnala_contatore, nota.length, MAX_COMMENTO),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.segnala_proponi), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = propParola,
                    onValueChange = { propParola = it.take(MAX_PROPOSTA) },
                    label = { Text(stringResource(R.string.segnala_proponi_parola)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = propAffine,
                    onValueChange = { propAffine = it.take(MAX_PROPOSTA) },
                    label = { Text(stringResource(R.string.segnala_proponi_affine)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (propostaMezza || propostaUguale) {
                    Text(
                        stringResource(if (propostaMezza) R.string.segnala_proponi_manca else R.string.segnala_proponi_uguali),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = (motivi.isNotEmpty() || nota.isNotBlank() || propostaValida) && !propostaMezza && !propostaUguale,
                onClick = {
                    onSalva(
                        MotivoSegnalazione.entries.filter { it.name in motivi },
                        nota.trim(),
                        proposta.propostaParola,
                        proposta.propostaAffine,
                    )
                },
            ) { Text(stringResource(R.string.segnala_salva)) }
        },
        dismissButton = { TextButton(onClick = onAnnulla) { Text(stringResource(R.string.annulla)) } },
    )
}
