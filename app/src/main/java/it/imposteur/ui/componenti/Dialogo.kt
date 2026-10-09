package it.imposteur.ui.componenti

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import it.imposteur.ui.theme.mollaEffetti
import it.imposteur.ui.theme.mollaSpaziale
import it.imposteur.ui.theme.rilevaRiduciAnimazioni

/**
 * Ingresso del dialogo: scala da 0,9 a 1 con la molla spaziale e dissolvenza.
 * Con "Riduci animazioni" solo dissolvenza, senza rimbalzo.
 */
@Composable
fun Modifier.ingressoDialogo(): Modifier {
    val ridotto = rilevaRiduciAnimazioni()
    var visibile by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visibile = true }
    val scala by animateFloatAsState(
        targetValue = if (visibile) 1f else 0.9f,
        animationSpec = mollaSpaziale(),
        label = "scalaDialogo",
    )
    val opacita by animateFloatAsState(
        targetValue = if (visibile) 1f else 0f,
        animationSpec = mollaEffetti(),
        label = "opacitaDialogo",
    )
    return graphicsLayer {
        val s = if (ridotto) 1f else scala
        scaleX = s
        scaleY = s
        alpha = opacita
    }
}

/** Dialogo di conferma con etichette esplicite (azione a sinistra della conferma, mai Si'/No). */
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
        modifier = Modifier.ingressoDialogo(),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(titolo, style = MaterialTheme.typography.titleLarge) },
        text = messaggio?.let { { Text(it, style = MaterialTheme.typography.bodyLarge) } },
        confirmButton = { PulsanteTesto(etichettaSi, onSi) },
        dismissButton = { PulsanteTesto(etichettaNo, onNo) },
    )
}
