package it.imposteur.ui.schermate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.Configurazione
import it.imposteur.game.Modalita
import it.imposteur.game.Regole

/** Nomi brevi delle opzioni avanzate attive (mostraCategoria, attiva di default, non conta). */
@Composable
private fun nomiOpzioniAttive(c: Configurazione): List<String> = buildList {
    if (c.impostoreNonPrimo) add(stringResource(R.string.opz_breve_non_primo))
    if (c.impostoriSorpresa) add(stringResource(R.string.opz_breve_sorpresa))
    if (c.partitaTrappola) add(stringResource(R.string.opz_breve_trappola))
    if (c.ordineCasuale) add(stringResource(R.string.opz_breve_ordine_casuale))
    if (c.giriIndizi > 1) add(stringResource(R.string.opz_breve_giri, c.giriIndizi))
    if (c.promemoriaUltimaPossibilita) add(stringResource(R.string.opz_breve_promemoria))
}

/** Card a fisarmonica con le opzioni avanzate; chiusa per default. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PannelloOpzioniAvanzate(
    config: Configurazione,
    onCambia: ((Configurazione) -> Configurazione) -> Unit,
    modifier: Modifier = Modifier,
) {
    var aperto by rememberSaveable { mutableStateOf(false) }
    val rotazione by animateFloatAsState(if (aperto) 180f else 0f, label = "freccia")
    val attive = nomiOpzioniAttive(config)
    val resources = LocalContext.current.resources

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(role = Role.Button) { aperto = !aperto }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.opz_titolo),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!aperto) {
                    Text(
                        if (attive.isEmpty()) stringResource(R.string.opz_regole_classiche)
                        else attive.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (attive.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        resources.getQuantityString(R.plurals.opz_attive, attive.size, attive.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(
                    if (aperto) R.string.opz_comprimi else R.string.opz_espandi,
                ),
                modifier = Modifier.rotate(rotazione),
            )
        }

        AnimatedVisibility(
            visible = aperto,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            ) {
                EtichettaGruppo(stringResource(R.string.opz_gruppo_ruoli))
                if (config.modalita == Modalita.SENZA_PAROLA) {
                    RigaOpzione(
                        titolo = stringResource(R.string.config_mostra_categoria),
                        descrizione = stringResource(R.string.opz_desc_vede_categoria),
                        checked = config.mostraCategoria,
                        onChange = { v -> onCambia { it.copy(mostraCategoria = v) } },
                    )
                }
                RigaOpzione(
                    titolo = stringResource(R.string.opz_non_primo),
                    descrizione = stringResource(R.string.opz_desc_non_primo),
                    checked = config.impostoreNonPrimo,
                    onChange = { v -> onCambia { it.copy(impostoreNonPrimo = v) } },
                )
                RigaOpzione(
                    titolo = stringResource(R.string.opz_sorpresa),
                    descrizione = stringResource(R.string.opz_desc_sorpresa),
                    checked = config.impostoriSorpresa,
                    onChange = { v -> onCambia { it.copy(impostoriSorpresa = v) } },
                )
                RigaOpzione(
                    titolo = stringResource(R.string.opz_trappola),
                    descrizione = stringResource(R.string.opz_desc_trappola),
                    checked = config.partitaTrappola,
                    onChange = { v -> onCambia { it.copy(partitaTrappola = v) } },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                EtichettaGruppo(stringResource(R.string.opz_gruppo_turni))
                RigaOpzione(
                    titolo = stringResource(R.string.opz_ordine_casuale),
                    descrizione = stringResource(R.string.opz_desc_ordine_casuale),
                    checked = config.ordineCasuale,
                    onChange = { v -> onCambia { it.copy(ordineCasuale = v) } },
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.opz_giri), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.opz_desc_giri),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        for (n in 1..Regole.MAX_GIRI) {
                            SegmentedButton(
                                selected = config.giriIndizi == n,
                                onClick = { onCambia { it.copy(giriIndizi = n) } },
                                shape = SegmentedButtonDefaults.itemShape(n - 1, Regole.MAX_GIRI),
                            ) { Text(n.toString()) }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                EtichettaGruppo(stringResource(R.string.opz_gruppo_fine))
                RigaOpzione(
                    titolo = stringResource(R.string.opz_promemoria),
                    descrizione = stringResource(R.string.opz_desc_promemoria),
                    checked = config.promemoriaUltimaPossibilita,
                    onChange = { v -> onCambia { it.copy(promemoriaUltimaPossibilita = v) } },
                )
            }
        }
    }
}

@Composable
private fun EtichettaGruppo(testo: String) {
    Text(
        testo,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
    )
}

/** Riga con titolo, descrizione e Switch; tutta la riga e' cliccabile. */
@Composable
fun RigaOpzione(
    titolo: String,
    descrizione: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titolo, style = MaterialTheme.typography.bodyLarge)
            Text(
                descrizione,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}
