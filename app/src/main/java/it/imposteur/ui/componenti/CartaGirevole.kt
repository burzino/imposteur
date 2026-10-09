package it.imposteur.ui.componenti

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import it.imposteur.ui.theme.Forme
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

/**
 * Carta che si gira (Rivelazione): surfaceContainerHigh, raggio 36, padding 32, altezza minima 280.
 * [rotazione] va da 180 (retro) a 0 (fronte) intorno all'asse Y; [retro] si mostra oltre 90 gradi.
 * Stesso colore e stessa forma per tutti i ruoli. Con [alpha] < 1 e rotazione ferma si ottiene la dissolvenza
 * usata da "Riduci animazioni".
 */
@Composable
fun CartaGirevole(
    rotazione: Float,
    retro: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    fronte: @Composable () -> Unit,
) {
    val densita = LocalDensity.current.density
    val mostraRetro = rotazione > 90f
    Surface(
        shape = Forme.XXL,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = bordoLivello(),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotazione
                cameraDistance = 12f * densita
                this.alpha = alpha
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp)
                .padding(Spazio.s6)
                // Il retro e' specchiato dalla rotazione: lo raddrizziamo.
                .graphicsLayer { rotationY = if (mostraRetro) 180f else 0f },
            contentAlignment = Alignment.Center,
        ) {
            if (mostraRetro) retro() else fronte()
        }
    }
}
