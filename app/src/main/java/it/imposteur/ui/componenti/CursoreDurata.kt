package it.imposteur.ui.componenti

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.DurataPressione
import kotlin.math.abs

/** Larghezza della maniglia predefinita dello Slider: il suo binario e' inset di meta' di questa. */
private val MANIGLIA_SLIDER = 20.dp

/**
 * Cursore della durata della pressione (0..1000 ms, passo 50, 21 posizioni). Usa lo Slider stabile
 * di material3 per input e semantica (incremento/decremento, valore letto) con i colori resi
 * trasparenti; binario, tacche e maniglia a barra sono disegnati sotto di esso.
 * Salva a ogni cambio non da trascinamento e al rilascio del trascinamento ([onDurata], in ms);
 * [onAnteprima] riceve il valore anche durante il trascinamento.
 */
@Composable
fun CursoreDurata(
    durataMs: Int,
    onDurata: (Int) -> Unit,
    onAnteprima: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var valore by remember(durataMs) { mutableFloatStateOf(durataMs.toFloat()) }
    val interazione = remember { MutableInteractionSource() }
    val trascinando by interazione.collectIsDraggedAsState()
    val ms = valore.toInt()
    val descrizione = if (DurataPressione.soloTocco(ms)) {
        stringResource(R.string.impostazioni_pressione_a11y_zero)
    } else {
        stringResource(R.string.impostazioni_pressione_a11y, ms)
    }
    val passi = (DurataPressione.MAX_MS - DurataPressione.MIN_MS) / DurataPressione.PASSO_MS - 1
    val frazione = (valore - DurataPressione.MIN_MS) / (DurataPressione.MAX_MS - DurataPressione.MIN_MS)
    val colori = MaterialTheme.colorScheme
    val invisibile = SliderDefaults.colors(
        thumbColor = Color.Transparent,
        activeTrackColor = Color.Transparent,
        inactiveTrackColor = Color.Transparent,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxWidth().height(44.dp)) {
            disegnaCursore(
                frazione = frazione.coerceIn(0f, 1f),
                trascinando = trascinando,
                attivo = colori.primary,
                inattivo = colori.secondaryContainer,
                tacca = colori.onPrimary,
                inset = MANIGLIA_SLIDER.toPx() / 2f,
            )
        }
        // Dichiarato dopo il disegno: riceve i tocchi.
        Slider(
            value = valore,
            onValueChange = {
                valore = it
                onAnteprima(it.toInt())
                if (!trascinando) onDurata(it.toInt())
            },
            onValueChangeFinished = { onDurata(valore.toInt()) },
            valueRange = DurataPressione.MIN_MS.toFloat()..DurataPressione.MAX_MS.toFloat(),
            steps = passi,
            colors = invisibile,
            interactionSource = interazione,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { stateDescription = descrizione },
        )
    }
}

private fun DrawScope.disegnaCursore(
    frazione: Float,
    trascinando: Boolean,
    attivo: Color,
    inattivo: Color,
    tacca: Color,
    inset: Float,
) {
    val altBinario = 16.dp.toPx()
    val top = (size.height - altBinario) / 2f
    val pieno = altBinario / 2f
    val interno = 4.dp.toPx()
    val vuoto = 6.dp.toPx()
    val largBinario = size.width - 2 * inset
    val x = inset + largBinario * frazione
    val fineAttivo = x - vuoto
    val inizioInattivo = x + vuoto
    if (fineAttivo > inset) {
        barra(inset, fineAttivo, top, altBinario, CornerRadius(pieno), CornerRadius(interno), attivo)
    }
    if (inizioInattivo < size.width - inset) {
        barra(inizioInattivo, size.width - inset, top, altBinario, CornerRadius(interno), CornerRadius(pieno), inattivo)
    }
    val posizioni = (DurataPressione.MAX_MS - DurataPressione.MIN_MS) / DurataPressione.PASSO_MS + 1
    val raggio = 2.dp.toPx()
    for (i in 0 until posizioni) {
        val cx = inset + largBinario * i / (posizioni - 1)
        if (abs(cx - x) < vuoto) continue
        drawCircle(if (cx < x) tacca else attivo, raggio, Offset(cx, size.height / 2f))
    }
    val larghezzaManiglia = if (trascinando) 2.dp.toPx() else 4.dp.toPx()
    drawRoundRect(
        color = attivo,
        topLeft = Offset(x - larghezzaManiglia / 2f, (size.height - 44.dp.toPx()) / 2f),
        size = androidx.compose.ui.geometry.Size(larghezzaManiglia, 44.dp.toPx()),
        cornerRadius = CornerRadius(larghezzaManiglia / 2f),
    )
}

private fun DrawScope.barra(
    da: Float,
    a: Float,
    top: Float,
    altezza: Float,
    sinistra: CornerRadius,
    destra: CornerRadius,
    colore: Color,
) {
    val percorso = Path().apply {
        addRoundRect(
            RoundRect(
                left = da, top = top, right = a, bottom = top + altezza,
                topLeftCornerRadius = sinistra, bottomLeftCornerRadius = sinistra,
                topRightCornerRadius = destra, bottomRightCornerRadius = destra,
            ),
        )
    }
    drawPath(percorso, colore)
}
