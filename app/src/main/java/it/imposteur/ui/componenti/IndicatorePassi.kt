package it.imposteur.ui.componenti

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.mollaSpaziale
import it.imposteur.ui.theme.rilevaRiduciAnimazioni
import androidx.compose.animation.core.snap

/**
 * Indicatore dei passi a pillole: un segmento per voce di [nomi], alti [Spazio.passoAltezza].
 * Fatto: pieno primary. Corrente: pieno e largo [Spazio.PASSO_CORRENTE_PESO] volte gli altri (la larghezza si
 * anima con la molla spaziale). Da fare: solo contorno 2 dp outline. I segmenti dei passi fatti sono toccabili
 * (area alta 48 dp) e chiamano [onPasso] con il numero del passo (da 1).
 * [passo] e' il passo corrente, da 1; [etichetta] e' il testo "Passo k di N · Nome", composto dal chiamante,
 * mostrato sotto; [descrizione] (default: [etichetta]) e' il testo letto dai servizi di accessibilita'
 * (indicatore di avanzamento da k a N).
 */
@Composable
fun IndicatorePassi(
    passo: Int,
    nomi: List<String>,
    etichetta: String,
    onPasso: (Int) -> Unit,
    modifier: Modifier = Modifier,
    descrizione: String = etichetta,
) {
    val totale = nomi.size
    val ridotto = rilevaRiduciAnimazioni()
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(passo.toFloat(), 1f..totale.toFloat(), (totale - 2).coerceAtLeast(0))
                    contentDescription = descrizione
                },
            horizontalArrangement = Arrangement.spacedBy(Spazio.passoSpazio),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            nomi.forEachIndexed { i, nome ->
                val numero = i + 1
                val peso by animateFloatAsState(
                    targetValue = if (numero == passo) Spazio.PASSO_CORRENTE_PESO else 1f,
                    animationSpec = if (ridotto) snap() else mollaSpaziale(),
                    label = "pesoPasso",
                )
                val fatto = numero <= passo
                val toccabile = numero < passo
                Box(
                    modifier = Modifier
                        .weight(peso.coerceAtLeast(0.1f))
                        .height(Spazio.altezzaTocco)
                        .then(
                            if (toccabile) {
                                Modifier
                                    .clickable(onClick = { onPasso(numero) })
                                    .semantics { role = Role.Button; contentDescription = nome }
                            } else {
                                Modifier.clearAndSetSemantics { }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(Spazio.passoAltezza)
                            .clip(CircleShape)
                            .then(
                                if (fatto) {
                                    Modifier.background(MaterialTheme.colorScheme.primary)
                                } else {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                },
                            ),
                    )
                }
            }
        }
        Text(
            etichetta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spazio.s1).clearAndSetSemantics { },
        )
    }
}
