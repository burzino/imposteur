package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.imposteur.R
import it.imposteur.data.FormatoSegnalazioni
import it.imposteur.data.MotivoSegnalazione
import it.imposteur.data.Segnalazione
import it.imposteur.game.Modalita
import it.imposteur.game.Partita
import it.imposteur.ui.componenti.BarraAzioni
import it.imposteur.ui.componenti.PulsantePieno
import it.imposteur.ui.componenti.PulsanteTesto
import it.imposteur.ui.componenti.PulsanteTonale
import it.imposteur.ui.componenti.ScaffoldConBarra
import it.imposteur.ui.componenti.ingressoDialogo
import it.imposteur.ui.theme.Forme
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
    val colori = MaterialTheme.colorScheme
    ScaffoldConBarra(
        titolo = stringResource(R.string.rivela_titolo),
        onHome = onHome,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            BarraAzioni {
                PulsantePieno(
                    testo = stringResource(R.string.rivela_rigioca),
                    onClick = onNuovaPartita,
                    modifier = Modifier.fillMaxWidth(),
                )
                PulsanteTonale(
                    testo = stringResource(R.string.rivela_cambia_impostazioni),
                    onClick = onCambiaImpostazioni,
                    modifier = Modifier.fillMaxWidth(),
                )
                PulsanteTesto(
                    testo = stringResource(R.string.segnala_pulsante),
                    onClick = { segnalando = true },
                    icona = Icons.Filled.Warning,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spazio.margineSchermata),
            verticalArrangement = Arrangement.spacedBy(Spazio.s3),
        ) {
            val testoImpostori = if (nomiImpostori.isEmpty()) {
                stringResource(R.string.rivela_trappola)
            } else if (nomiImpostori.size == 1) {
                stringResource(R.string.rivela_impostore, nomiImpostori.first())
            } else {
                stringResource(R.string.rivela_impostori, nomiImpostori.joinToString(", "))
            }
            Surface(
                shape = Forme.XXL,
                color = colori.primaryContainer,
                contentColor = colori.onPrimaryContainer,
                border = bordoLivello(colori.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    Modifier.fillMaxWidth().padding(Spazio.s5),
                    contentAlignment = Alignment.Center,
                ) {
                    TestoAdattivo(
                        testoImpostori,
                        if (partita.trappola) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                        color = colori.onPrimaryContainer,
                        maxRighe = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = colori.surfaceContainerLow,
                border = bordoLivello(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(Spazio.s2)) {
                    Text(
                        stringResource(R.string.rivela_parola, partita.voce.parola),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (partita.modalita == Modalita.PAROLA_AFFINE && partita.voce.affine != null) {
                        Text(
                            stringResource(R.string.rivela_affine, partita.voce.affine!!),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    Text(
                        stringResource(R.string.rivela_categoria, partita.voce.categoriaNome),
                        style = MaterialTheme.typography.titleMedium,
                        color = colori.onSurfaceVariant,
                    )
                }
            }
            if (partita.promemoriaUltimaPossibilita && !partita.trappola) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = colori.tertiaryContainer,
                    contentColor = colori.onTertiaryContainer,
                    border = bordoLivello(colori.tertiary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(Spazio.s4),
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                        Spacer(Modifier.width(Spazio.s3))
                        Text(
                            stringResource(R.string.rivela_promemoria),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
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
    Dialog(
        onDismissRequest = onAnnulla,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = bordoLivello(),
            modifier = Modifier
                .ingressoDialogo()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth(),
        ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                stringResource(R.string.segnala_titolo),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
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
                TextField(
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
                TextField(
                    value = propParola,
                    onValueChange = { propParola = it.take(MAX_PROPOSTA) },
                    label = { Text(stringResource(R.string.segnala_proponi_parola)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                TextField(
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
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
            PulsanteTesto(stringResource(R.string.annulla), onAnnulla)
            PulsanteTesto(
                testo = stringResource(R.string.segnala_salva),
                abilitato = (motivi.isNotEmpty() || nota.isNotBlank() || propostaValida) && !propostaMezza && !propostaUguale,
                onClick = {
                    onSalva(
                        MotivoSegnalazione.entries.filter { it.name in motivi },
                        nota.trim(),
                        proposta.propostaParola,
                        proposta.propostaAffine,
                    )
                },
            )
            }
        }
        }
    }
}
