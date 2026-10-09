package it.imposteur.ui.componenti

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

/** Selettore a segmenti connessi alti 48 dp, con spunta sul selezionato (secondaryContainer). */
@Composable
fun SelettoreSegmenti(
    etichette: List<String>,
    selezionato: Int,
    onSeleziona: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        etichette.forEachIndexed { i, etichetta ->
            SegmentedButton(
                selected = i == selezionato,
                onClick = { onSeleziona(i) },
                shape = SegmentedButtonDefaults.itemShape(i, etichette.size),
                modifier = Modifier.heightIn(min = Spazio.altezzaTocco),
                label = { Text(etichetta, style = MaterialTheme.typography.labelLarge, maxLines = 1) },
            )
        }
    }
}

/**
 * Carta selezionabile (radio) per scelte esclusive: selezionata in primaryContainer con bordo 2 dp primary,
 * altrimenti surfaceContainerLow. Da mettere dentro un Column(Modifier.selectableGroup()).
 */
@Composable
fun CartaSelezionabile(
    selezionata: Boolean,
    onClick: () -> Unit,
    titolo: String,
    modifier: Modifier = Modifier,
    descrizione: String? = null,
) {
    val colori = MaterialTheme.colorScheme
    Surface(
        selected = selezionata,
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selezionata) colori.primaryContainer else colori.surfaceContainerLow,
        contentColor = if (selezionata) colori.onPrimaryContainer else colori.onSurface,
        border = if (selezionata) BorderStroke(2.dp, colori.primary) else bordoLivello(),
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp).semantics { role = Role.RadioButton },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spazio.s4, vertical = Spazio.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spazio.s3),
        ) {
            RadioButton(selected = selezionata, onClick = null)
            Column(Modifier.weight(1f)) {
                Text(titolo, style = MaterialTheme.typography.titleMedium)
                if (descrizione != null) {
                    Text(
                        descrizione,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selezionata) colori.onPrimaryContainer else colori.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Gruppo di carte selezionabili (colonna con distanza 8 dp e semantica di gruppo radio). */
@Composable
fun GruppoCarte(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(Spazio.s2)) { content() }
}

/** Riga con interruttore e descrizione: tutta la riga e' toccabile, altezza minima 64 dp. */
@Composable
fun RigaInterruttore(
    etichetta: String,
    attivo: Boolean,
    onCambia: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    descrizione: String? = null,
    abilitato: Boolean = true,
) {
    val colori = MaterialTheme.colorScheme
    val coloreTesto = if (abilitato) colori.onSurface else colori.onSurface.copy(alpha = 0.38f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(value = attivo, enabled = abilitato, role = Role.Switch, onValueChange = onCambia),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spazio.s4),
    ) {
        Column(Modifier.weight(1f)) {
            Text(etichetta, style = MaterialTheme.typography.titleMedium, color = coloreTesto)
            if (descrizione != null) {
                Text(
                    descrizione,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (abilitato) colori.onSurfaceVariant else coloreTesto,
                )
            }
        }
        Switch(
            checked = attivo,
            onCheckedChange = null,
            enabled = abilitato,
            thumbContent = if (attivo) {
                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
            } else {
                null
            },
        )
    }
}

/** Riga con pulsante radio per i gruppi di scelta semplici (es. tema). */
@Composable
fun RigaRadio(
    etichetta: String,
    selezionata: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Spazio.altezzaTocco)
            .selectable(selected = selezionata, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selezionata, onClick = null)
        Text(etichetta, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = Spazio.s4))
    }
}
