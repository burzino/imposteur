package it.imposteur.ui.componenti

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.imposteur.ui.theme.ColoriAvatar
import it.imposteur.ui.theme.Spazio

enum class StatoAvatar { NORMALE, VISTO, CORRENTE, SUCCESSIVO }

fun inizialeDi(nome: String) = nome.trim().take(1).uppercase()

/**
 * Cerchio con l'iniziale sul colore del giocatore [indice] (stessa palette in tutti i temi).
 * VISTO: spunta bianca; CORRENTE: anello 3 dp primary a 3 dp di distanza, ingrandito a 1,15;
 * SUCCESSIVO: opacita' 38%. L'anello e' disegnato fuori dal riquadro: non cambia il layout.
 */
@Composable
fun Avatar(
    nome: String,
    indice: Int,
    modifier: Modifier = Modifier,
    misura: Dp = Spazio.avatarRiga,
    stato: StatoAvatar = StatoAvatar.NORMALE,
) {
    val colore = ColoriAvatar[Math.floorMod(indice, ColoriAvatar.size)]
    val anello = MaterialTheme.colorScheme.primary
    val densita = LocalDensity.current
    val corrente = stato == StatoAvatar.CORRENTE
    Box(
        modifier = modifier
            .size(misura)
            .graphicsLayer {
                val s = if (corrente) 1.15f else 1f
                scaleX = s
                scaleY = s
            }
            .then(if (stato == StatoAvatar.SUCCESSIVO) Modifier.alpha(0.38f) else Modifier)
            .drawBehind {
                if (corrente) {
                    val spessore = with(densita) { 3.dp.toPx() }
                    drawCircle(
                        color = anello,
                        radius = size.minDimension / 2 + with(densita) { 3.dp.toPx() } + spessore / 2,
                        center = Offset(size.width / 2, size.height / 2),
                        style = Stroke(spessore),
                    )
                }
            }
            .clip(CircleShape)
            .background(colore),
        contentAlignment = Alignment.Center,
    ) {
        if (stato == StatoAvatar.VISTO) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        } else {
            Text(
                inizialeDi(nome),
                style = if (misura >= 64.dp) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

/** Fila di avatar centrata sul corrente: gia' visti con spunta, corrente con anello, successivi attenuati. */
@Composable
fun FilaAvatar(giocatori: List<String>, corrente: Int, modifier: Modifier = Modifier) {
    fun statoDi(i: Int) = when {
        i < corrente -> StatoAvatar.VISTO
        i == corrente -> StatoAvatar.CORRENTE
        else -> StatoAvatar.SUCCESSIVO
    }
    // Decorativa: l'informazione e' nel testo "Giocatore n di N".
    val m = modifier.fillMaxWidth().clearAndSetSemantics { }
    if (giocatori.size <= 6) {
        Row(
            modifier = m,
            horizontalArrangement = Arrangement.spacedBy(Spazio.s3, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) { giocatori.forEachIndexed { i, nome -> Avatar(nome, i, stato = statoDi(i)) } }
    } else {
        val lista = rememberLazyListState()
        val densita = LocalDensity.current
        LaunchedEffect(corrente) {
            val larghezza = lista.layoutInfo.viewportSize.width
            val offset = -(larghezza / 2 - with(densita) { (Spazio.avatarRiga / 2).roundToPx() })
            lista.animateScrollToItem(corrente, offset)
        }
        LazyRow(
            state = lista,
            modifier = m,
            horizontalArrangement = Arrangement.spacedBy(Spazio.s3),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = Spazio.s4, vertical = Spazio.s2),
        ) { itemsIndexed(giocatori) { i, nome -> Avatar(nome, i, stato = statoDi(i)) } }
    }
}
