package it.imposteur.ui.componenti

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import it.imposteur.ui.theme.DURATA_VELOCE_MS
import it.imposteur.ui.theme.rilevaRiduciAnimazioni
import it.imposteur.ui.theme.Spazio

private val RaggioRiposo = 28.dp // meta' di 56 dp: pillola
private val RaggioPremuto = 12.dp // --raggio-m

/** Forma a pillola che passa a --raggio-m mentre il pulsante e' premuto, con scala 0,97. */
private class MorphPulsante(val forma: RoundedCornerShape, val scala: Float)

@Composable
private fun morphPulsante(sorgente: MutableInteractionSource, altezza: androidx.compose.ui.unit.Dp): MorphPulsante {
    val premuto by sorgente.collectIsPressedAsState()
    val ridotto = rilevaRiduciAnimazioni()
    val durata: AnimationSpec<Float> = if (ridotto) snap() else tween(DURATA_VELOCE_MS)
    val raggio by animateDpAsState(
        targetValue = if (premuto) RaggioPremuto else altezza / 2,
        animationSpec = if (ridotto) snap() else tween(DURATA_VELOCE_MS),
        label = "raggioPulsante",
    )
    val scala by animateFloatAsState(
        targetValue = if (premuto) 0.97f else 1f,
        animationSpec = durata,
        label = "scalaPulsante",
    )
    return MorphPulsante(RoundedCornerShape(raggio), scala)
}

@Composable
private fun ContenutoPulsante(testo: String, icona: ImageVector?) {
    if (icona != null) {
        Icon(icona, contentDescription = null, modifier = Modifier.size(18.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.width(Spazio.s2))
    }
    Text(testo, style = MaterialTheme.typography.titleMedium)
}

/** Pulsante pieno (azione principale): pillola da 56 dp, morph da premuto. */
@Composable
fun PulsantePieno(
    testo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    abilitato: Boolean = true,
    icona: ImageVector? = null,
) {
    val sorgente = remember { MutableInteractionSource() }
    val morph = morphPulsante(sorgente, Spazio.altezzaPulsante)
    Button(
        onClick = onClick,
        enabled = abilitato,
        interactionSource = sorgente,
        shape = morph.forma,
        modifier = modifier
            .heightIn(min = Spazio.altezzaPulsante)
            .graphicsLayer { scaleX = morph.scala; scaleY = morph.scala },
    ) { ContenutoPulsante(testo, icona) }
}

/** Pulsante tonale (secondario). */
@Composable
fun PulsanteTonale(
    testo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    abilitato: Boolean = true,
    icona: ImageVector? = null,
) {
    val sorgente = remember { MutableInteractionSource() }
    val morph = morphPulsante(sorgente, Spazio.altezzaPulsante)
    FilledTonalButton(
        onClick = onClick,
        enabled = abilitato,
        interactionSource = sorgente,
        shape = morph.forma,
        modifier = modifier
            .heightIn(min = Spazio.altezzaPulsante)
            .graphicsLayer { scaleX = morph.scala; scaleY = morph.scala },
    ) { ContenutoPulsante(testo, icona) }
}

/** Pulsante con contorno: bordo 1 dp outline, testo primary. */
@Composable
fun PulsanteContorno(
    testo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    abilitato: Boolean = true,
    icona: ImageVector? = null,
) {
    val sorgente = remember { MutableInteractionSource() }
    val morph = morphPulsante(sorgente, Spazio.altezzaPulsante)
    OutlinedButton(
        onClick = onClick,
        enabled = abilitato,
        interactionSource = sorgente,
        shape = morph.forma,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (abilitato) 1f else 0.12f)),
        modifier = modifier
            .heightIn(min = Spazio.altezzaPulsante)
            .graphicsLayer { scaleX = morph.scala; scaleY = morph.scala },
    ) { ContenutoPulsante(testo, icona) }
}

/** Pulsante testo: senza contenitore, altezza minima 48 dp. */
@Composable
fun PulsanteTesto(
    testo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    abilitato: Boolean = true,
    icona: ImageVector? = null,
) {
    TextButton(
        onClick = onClick,
        enabled = abilitato,
        shape = RoundedCornerShape(RaggioRiposo),
        colors = ButtonDefaults.textButtonColors(),
        modifier = modifier.heightIn(min = Spazio.altezzaTocco),
    ) {
        if (icona != null) {
            Icon(icona, contentDescription = null, modifier = Modifier.size(18.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.width(Spazio.s2))
        }
        Text(testo, style = MaterialTheme.typography.titleMedium)
    }
}
