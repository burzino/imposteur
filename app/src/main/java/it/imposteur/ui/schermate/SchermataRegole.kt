package it.imposteur.ui.schermate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.ui.componenti.ScaffoldConBarra
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

@Composable
fun SchermataRegole(onIndietro: () -> Unit, onHome: () -> Unit) {
    ScaffoldConBarra(
        titolo = stringResource(R.string.come_si_gioca),
        onIndietro = onIndietro,
        onHome = onHome,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spazio.margineSchermata),
            verticalArrangement = Arrangement.spacedBy(Spazio.s3),
        ) {
            Sezione(R.string.regole_modalita_senza_parola_titolo, R.string.regole_modalita_senza_parola)
            Sezione(R.string.regole_modalita_affine_titolo, R.string.regole_modalita_affine)
            Sezione(R.string.regole_fine_titolo, R.string.regole_fine)
        }
    }
}

@Composable
private fun Sezione(titolo: Int, testo: Int) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = bordoLivello(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(Spazio.s2)) {
            Text(stringResource(titolo), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(testo), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
