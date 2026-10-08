package it.imposteur.ui.schermate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.imposteur.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataRegole(onIndietro: () -> Unit, onHome: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.come_si_gioca)) },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.indietro))
                    }
                },
                actions = { AzioneHome(onHome) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Sezione(R.string.regole_modalita_senza_parola_titolo, R.string.regole_modalita_senza_parola)
            Sezione(R.string.regole_modalita_affine_titolo, R.string.regole_modalita_affine)
            Sezione(R.string.regole_fine_titolo, R.string.regole_fine)
        }
    }
}

@Composable
private fun Sezione(titolo: Int, testo: Int) {
    Text(stringResource(titolo), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    Text(stringResource(testo), style = MaterialTheme.typography.bodyLarge)
}
