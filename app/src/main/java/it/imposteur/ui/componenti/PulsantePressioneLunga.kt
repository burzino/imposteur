package it.imposteur.ui.componenti

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import it.imposteur.ui.theme.DURATA_BARRA_MS
import it.imposteur.ui.theme.DURATA_RILASCIO_BARRA_MS
import it.imposteur.ui.theme.Forme
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

/**
 * Pulsante a pressione lunga: la barra [primary] avanza da sinistra a destra in [DURATA_BARRA_MS]
 * mentre lo si tiene premuto, poi chiama [onCompletato]. Rilasciato prima, la barra torna a zero
 * in [DURATA_RILASCIO_BARRA_MS] senza effetti. Il testo ha due livelli identici: quello sotto in
 * onPrimaryContainer, quello sopra in onPrimary ritagliato dalla larghezza della barra.
 * [descrizioneAzione] e' l'azione di accessibilita' (tocco singolo) che equivale alla pressione lunga.
 * [chiave] azzera la barra quando cambia (es. il giocatore).
 */
@Composable
fun PulsantePressioneLunga(
    etichetta: String,
    descrizioneAzione: String,
    chiave: Any,
    abilitato: Boolean,
    onCompletato: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progresso = remember(chiave) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val completato by rememberUpdatedState(onCompletato)
    val abilitatoAttuale by rememberUpdatedState(abilitato)
    val colori = MaterialTheme.colorScheme
    val stile = MaterialTheme.typography.titleLarge
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Spazio.altezzaPulsanteGrande)
            .graphicsLayer { alpha = if (abilitato) 1f else 0.38f }
            .clip(Forme.XXL)
            .background(colori.primaryContainer)
            .then(bordoLivello()?.let { Modifier.border(it, Forme.XXL) } ?: Modifier)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick(label = descrizioneAzione) {
                    if (abilitatoAttuale) {
                        completato()
                        true
                    } else {
                        false
                    }
                }
            }
            .pointerInput(chiave) {
                detectTapGestures(
                    onPress = {
                        if (!abilitatoAttuale) return@detectTapGestures
                        val lavoro = scope.launch {
                            progresso.animateTo(1f, tween(DURATA_BARRA_MS, easing = LinearEasing))
                            completato()
                        }
                        tryAwaitRelease()
                        lavoro.cancel()
                        if (progresso.value < 1f) {
                            scope.launch { progresso.animateTo(0f, tween(DURATA_RILASCIO_BARRA_MS, easing = LinearEasing)) }
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(etichetta, style = stile, color = colori.onPrimaryContainer)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearAndSetSemantics { }
                .drawWithContent {
                    clipRect(right = size.width * progresso.value) { this@drawWithContent.drawContent() }
                }
                .background(colori.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(etichetta, style = stile, color = colori.onPrimary)
        }
    }
}
