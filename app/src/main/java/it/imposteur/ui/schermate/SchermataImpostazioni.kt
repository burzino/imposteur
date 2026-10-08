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
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun SchermataImpostazioni(aspetto: Aspetto, onCambia: (Aspetto) -> Unit, onIndietro: () -> Unit) {
    val opzioni = listOf(
        Tema.SISTEMA to R.string.tema_sistema,
        Tema.CHIARO to R.string.tema_chiaro,
        Tema.SCURO to R.string.tema_scuro,
        Tema.ALTO_CONTRASTO to R.string.tema_alto_contrasto,
    )
    Scaffold(
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
        }
    }
}
