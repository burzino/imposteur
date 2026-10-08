package it.imposteur.ui.schermate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.ErroreConfigurazione
import it.imposteur.game.Modalita
import it.imposteur.game.Regole
import it.imposteur.ui.ImpostoreViewModel
import it.imposteur.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataConfigurazione(
    stato: UiState,
    viewModel: ImpostoreViewModel,
    onIndietro: () -> Unit,
    onInizia: () -> Unit,
) {
    BackHandler(onBack = onIndietro)
    val config = stato.config
    val errori = stato.errori
    val indiciDuplicati = errori.filterIsInstance<ErroreConfigurazione.NomeDuplicato>().flatMap { it.indici }.toSet()
    val maxImpostori = Regole.maxImpostori(config.numeroGiocatori)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.config_titolo)) },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.indietro))
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onInizia,
                    enabled = stato.puoIniziare,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                ) { Text(stringResource(R.string.config_inizia), style = MaterialTheme.typography.titleMedium) }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Stepper(
                etichetta = stringResource(R.string.config_numero_giocatori),
                valore = config.numeroGiocatori,
                min = Regole.MIN_GIOCATORI,
                max = Regole.MAX_GIOCATORI,
                onCambia = viewModel::impostaNumeroGiocatori,
            )

            Titolo(stringResource(R.string.config_nomi))
            for (i in 0 until config.numeroGiocatori) {
                val duplicato = i in indiciDuplicati
                OutlinedTextField(
                    value = config.nomi.getOrElse(i) { "" },
                    onValueChange = { viewModel.impostaNome(i, it) },
                    label = { Text(stringResource(R.string.config_giocatore_n, i + 1)) },
                    singleLine = true,
                    isError = duplicato,
                    supportingText = if (duplicato) {
                        { Text(stringResource(R.string.config_nome_duplicato)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HorizontalDivider()
            Stepper(
                etichetta = stringResource(R.string.config_numero_impostori),
                valore = config.numeroImpostori,
                min = 1,
                max = maxImpostori,
                onCambia = viewModel::impostaNumeroImpostori,
            )

            HorizontalDivider()
            Titolo(stringResource(R.string.config_modalita))
            RigaModalita(
                testo = stringResource(R.string.modalita_senza_parola),
                selezionata = config.modalita == Modalita.SENZA_PAROLA,
                onClick = { viewModel.impostaModalita(Modalita.SENZA_PAROLA) },
            )
            RigaModalita(
                testo = stringResource(R.string.modalita_affine),
                selezionata = config.modalita == Modalita.PAROLA_AFFINE,
                onClick = { viewModel.impostaModalita(Modalita.PAROLA_AFFINE) },
            )
            if (config.modalita == Modalita.SENZA_PAROLA) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = config.mostraCategoria,
                            role = Role.Switch,
                            onValueChange = viewModel::impostaMostraCategoria,
                        )
                        .padding(vertical = 8.dp),
                ) {
                    Text(
                        stringResource(R.string.config_mostra_categoria),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = config.mostraCategoria, onCheckedChange = null)
                }
            }

            HorizontalDivider()
            Titolo(stringResource(R.string.config_categorie))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.selezionaTutte(true) }) {
                    Text(stringResource(R.string.config_seleziona_tutte))
                }
                OutlinedButton(onClick = { viewModel.selezionaTutte(false) }) {
                    Text(stringResource(R.string.config_deseleziona_tutte))
                }
            }
            for (categoria in stato.categorie) {
                val selezionata = categoria.id in config.categorieSelezionate
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = selezionata,
                            role = Role.Checkbox,
                            onValueChange = { viewModel.impostaCategoria(categoria.id, it) },
                        )
                        .padding(vertical = 4.dp),
                ) {
                    Checkbox(checked = selezionata, onCheckedChange = null)
                    Text(
                        categoria.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
            if (ErroreConfigurazione.NessunaCategoria in errori) {
                Messaggio(stringResource(R.string.config_nessuna_categoria))
            } else if (ErroreConfigurazione.PoolVuoto in errori) {
                Messaggio(stringResource(R.string.config_pool_vuoto))
            }
        }
    }
}

@Composable
private fun Titolo(testo: String) {
    Text(testo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun Messaggio(testo: String) {
    Text(testo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun RigaModalita(testo: String, selezionata: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selezionata, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        RadioButton(selected = selezionata, onClick = null)
        Text(testo, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun Stepper(etichetta: String, valore: Int, min: Int, max: Int, onCambia: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(etichetta, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { onCambia(valore - 1) },
            enabled = valore > min,
            modifier = Modifier.height(48.dp),
        ) { Text("−", style = MaterialTheme.typography.titleLarge) }
        Text(
            valore.toString(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        OutlinedButton(
            onClick = { onCambia(valore + 1) },
            enabled = valore < max,
            modifier = Modifier.height(48.dp),
        ) { Text("+", style = MaterialTheme.typography.titleLarge) }
    }
}
