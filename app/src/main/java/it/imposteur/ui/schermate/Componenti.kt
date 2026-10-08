package it.imposteur.ui.schermate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.stringResource
import it.imposteur.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/** Dialogo di conferma con etichette esplicite (azione a sinistra della conferma, mai Sì/No). */
@Composable
fun DialogoConferma(
    titolo: String,
    messaggio: String?,
    etichettaSi: String,
    etichettaNo: String,
    onSi: () -> Unit,
    onNo: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onNo,
        title = { Text(titolo) },
        text = messaggio?.let { { Text(it) } },
        confirmButton = { TextButton(onClick = onSi) { Text(etichettaSi) } },
        dismissButton = { TextButton(onClick = onNo) { Text(etichettaNo) } },
    )
}

/**
 * Testo che riduce la dimensione del 10% alla volta (fino al 40%) finche' sta nella larghezza,
 * in al massimo [maxRighe] righe e senza spezzare le parole a meta'.
 */
@Composable
fun TestoAdattivo(
    testo: String,
    stile: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxRighe: Int = 2,
) {
    var fattore by remember(testo, stile) { mutableFloatStateOf(1f) }
    Text(
        text = testo,
        style = stile.copy(
            fontSize = stile.fontSize * fattore,
            lineHeight = stile.lineHeight * fattore,
        ),
        color = color.takeIf { it != Color.Unspecified } ?: MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = maxRighe,
        overflow = TextOverflow.Clip,
        modifier = modifier,
        onTextLayout = { r ->
            val parolaSpezzata = (0 until r.lineCount - 1).any { riga ->
                val fine = r.getLineEnd(riga)
                fine in 1 until testo.length && !testo[fine - 1].isWhitespace() && !testo[fine].isWhitespace()
            }
            if ((r.didOverflowWidth || r.didOverflowHeight || parolaSpezzata) && fattore > 0.4f) {
                fattore *= 0.9f
            }
        },
    )
}

/** Tasto "Home" uniforme, da mettere nelle actions di una TopAppBar. */
@Composable
fun AzioneHome(onHome: () -> Unit) {
    IconButton(onClick = onHome) {
        Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.torna_home))
    }
}

/** Scaffold con TopAppBar (titolo + tasto Home) per le schermate senza barra propria. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldConHome(
    titolo: Int,
    onHome: () -> Unit,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titolo)) },
                actions = { AzioneHome(onHome) },
            )
        },
    ) { padding -> Box(Modifier.padding(padding)) { content(PaddingValues()) } }
}
