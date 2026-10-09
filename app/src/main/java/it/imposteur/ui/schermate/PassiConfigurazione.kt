package it.imposteur.ui.schermate

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.game.ErroreConfigurazione
import it.imposteur.game.Modalita
import it.imposteur.game.OpzioneRiepilogo
import it.imposteur.game.Passi
import it.imposteur.game.Regole
import it.imposteur.ui.ImpostoreViewModel
import it.imposteur.ui.UiState
import it.imposteur.ui.componenti.Avatar
import it.imposteur.ui.componenti.CartaSelezionabile
import it.imposteur.ui.componenti.GruppoCarte
import it.imposteur.ui.componenti.PulsanteContorno
import it.imposteur.ui.componenti.PulsanteTesto
import it.imposteur.ui.componenti.PulsanteTonale
import it.imposteur.ui.componenti.RigaInterruttore
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello
import it.imposteur.ui.theme.mollaEffetti
import it.imposteur.ui.theme.rilevaRiduciAnimazioni

/** Contenuto del passo 1: un campo nome per giocatore (avatar, campo, "x") e "+ Aggiungi giocatore". */
@Composable
fun PassoGiocatori(stato: UiState, viewModel: ImpostoreViewModel) {
    val config = stato.config
    val indiciDuplicati = stato.errori.filterIsInstance<ErroreConfigurazione.NomeDuplicato>()
        .flatMap { it.indici }.toSet()
    // Indice del campo che deve ricevere il fuoco dopo "+ Aggiungi giocatore"; la rimozione non sposta il fuoco.
    var daFocalizzare by remember { mutableStateOf<Int?>(null) }
    val focus = LocalFocusManager.current
    val numero = config.numeroGiocatori
    val puoRimuovere = Passi.puoRimuovereGiocatore(config)
    val puoAggiungere = Passi.puoAggiungereGiocatore(config)
    Column(verticalArrangement = Arrangement.spacedBy(Spazio.s2)) {
        for (i in 0 until numero) {
            key(i) {
                RigaGiocatore(
                    posizione = i,
                    ultima = i == numero - 1,
                    nome = config.nomi.getOrElse(i) { "" },
                    duplicato = i in indiciDuplicati,
                    puoRimuovere = puoRimuovere,
                    daFocalizzare = daFocalizzare == i,
                    onFocalizzato = { if (daFocalizzare == i) daFocalizzare = null },
                    onNome = { viewModel.impostaNome(i, it) },
                    onRimuovi = { viewModel.rimuoviGiocatore(i) },
                    onAvanti = { focus.moveFocus(FocusDirection.Down) },
                    onFatto = { focus.clearFocus() },
                )
            }
        }
        PulsanteTesto(
            testo = stringResource(if (puoAggiungere) R.string.giocatori_aggiungi else R.string.giocatori_massimo),
            onClick = {
                if (puoAggiungere) {
                    daFocalizzare = numero
                    viewModel.aggiungiGiocatore()
                }
            },
            abilitato = puoAggiungere,
        )
    }
}

/**
 * Riga giocatore: avatar 40 dp (area 48 x 48, non interattiva; con nome vuoto mostra il numero del giocatore),
 * campo nome, "x" che rimuove (lo spazio da 48 dp resta riservato anche quando e' assente).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RigaGiocatore(
    posizione: Int,
    ultima: Boolean,
    nome: String,
    duplicato: Boolean,
    puoRimuovere: Boolean,
    daFocalizzare: Boolean,
    onFocalizzato: () -> Unit,
    onNome: (String) -> Unit,
    onRimuovi: () -> Unit,
    onAvanti: () -> Unit,
    onFatto: () -> Unit,
) {
    val colori = MaterialTheme.colorScheme
    val etichetta = stringResource(R.string.config_giocatore_n, posizione + 1)
    val richiedente = remember { FocusRequester() }
    if (daFocalizzare) {
        LaunchedEffect(Unit) {
            richiedente.requestFocus()
            onFocalizzato()
        }
    }
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spazio.s2),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
        Box(Modifier.size(Spazio.altezzaTocco).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) {
            Avatar(
                nome = nome,
                indice = posizione,
                iniziale = if (nome.isBlank()) (posizione + 1).toString() else null,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        TextField(
            value = nome,
            onValueChange = onNome,
            label = { Text(etichetta) },
            placeholder = { Text(etichetta) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = if (ultima) ImeAction.Done else ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { onAvanti() }, onDone = { onFatto() }),
            singleLine = true,
            isError = duplicato,
            supportingText = if (duplicato) {
                { Text(stringResource(R.string.config_nome_duplicato)) }
            } else null,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colori.surfaceContainerHigh,
                unfocusedContainerColor = colori.surfaceContainerHigh,
                errorContainerColor = colori.surfaceContainerHigh,
            ),
            modifier = Modifier.weight(1f).focusRequester(richiedente),
        )
        Box(Modifier.size(Spazio.altezzaTocco).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) {
            if (puoRimuovere) {
                val descrizione = stringResource(R.string.giocatori_rimuovi, posizione + 1)
                IconButton(onClick = onRimuovi, modifier = Modifier.size(Spazio.altezzaTocco)) {
                    Icon(Icons.Filled.Clear, contentDescription = descrizione, tint = colori.onSurfaceVariant)
                }
            }
        }
    }
}

/** Contenuto del passo 2: modalita', numero di impostori e "Impostori a sorpresa". */
@Composable
fun PassoModalita(stato: UiState, viewModel: ImpostoreViewModel) {
    val config = stato.config
    Column(verticalArrangement = Arrangement.spacedBy(Spazio.s3)) {
        TitoloSezione(stringResource(R.string.config_modalita))
        GruppoCarte {
            CartaSelezionabile(
                selezionata = config.modalita == Modalita.SENZA_PAROLA,
                onClick = { viewModel.impostaModalita(Modalita.SENZA_PAROLA) },
                titolo = stringResource(R.string.modalita_senza_parola),
                descrizione = stringResource(R.string.modalita_senza_parola_desc),
            )
            CartaSelezionabile(
                selezionata = config.modalita == Modalita.PAROLA_AFFINE,
                onClick = { viewModel.impostaModalita(Modalita.PAROLA_AFFINE) },
                titolo = stringResource(R.string.modalita_affine),
                descrizione = stringResource(R.string.modalita_affine_desc),
            )
        }
        Contatore(
            etichetta = stringResource(
                if (config.impostoriSorpresa) R.string.config_numero_impostori_max else R.string.config_numero_impostori,
            ),
            valore = config.numeroImpostori,
            min = 1,
            max = Regole.maxImpostori(config.numeroGiocatori),
            onCambia = viewModel::impostaNumeroImpostori,
        )
        RigaInterruttore(
            etichetta = stringResource(R.string.opz_sorpresa),
            descrizione = stringResource(R.string.opz_desc_sorpresa),
            attivo = config.impostoriSorpresa,
            onCambia = { v -> viewModel.impostaOpzione { it.copy(impostoriSorpresa = v) } },
        )
    }
}

/** Contenuto del passo 3: le tre schede di gruppo. */
@Composable
fun PassoOpzioni(stato: UiState, viewModel: ImpostoreViewModel) {
    OpzioniAvanzate(config = stato.config, onCambia = viewModel::impostaOpzione)
}

/** Contenuto del passo 4: categorie e parole ancora da giocare. */
@Composable
fun PassoCategorie(stato: UiState, viewModel: ImpostoreViewModel, onChiediAzzera: () -> Unit) {
    val config = stato.config
    Column(verticalArrangement = Arrangement.spacedBy(Spazio.s3)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spazio.s2), modifier = Modifier.fillMaxWidth()) {
            PulsanteTonale(
                testo = stringResource(R.string.config_seleziona_tutte),
                onClick = { viewModel.selezionaTutte(true) },
                modifier = Modifier.weight(1f),
            )
            PulsanteContorno(
                testo = stringResource(R.string.config_deseleziona_tutte),
                onClick = { viewModel.selezionaTutte(false) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.config_parole_rimanenti, stato.paroleRimanenti, stato.paroleTotali),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            PulsanteTesto(
                testo = stringResource(R.string.config_azzera),
                onClick = onChiediAzzera,
                abilitato = stato.paroleRimanenti < stato.paroleTotali,
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge,
            border = bordoLivello(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                stato.categorie.forEachIndexed { i, categoria ->
                    if (i > 0) HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.background)
                    val selezionata = categoria.id in config.categorieSelezionate
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = Spazio.altezzaTocco)
                            .toggleable(
                                value = selezionata,
                                role = Role.Checkbox,
                                onValueChange = { viewModel.impostaCategoria(categoria.id, it) },
                            )
                            .padding(horizontal = Spazio.s4, vertical = Spazio.s1),
                    ) {
                        Checkbox(checked = selezionata, onCheckedChange = null)
                        Text(
                            categoria.nome,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = Spazio.s3),
                        )
                    }
                }
            }
        }
    }
}

/** Contenuto del passo 5: quattro righe di riepilogo, ciascuna con "Modifica". */
@Composable
fun PassoRiepilogo(stato: UiState, onModifica: (Int) -> Unit) {
    val r = stato.riepilogo
    val impostori = if (r.finoA) {
        stringResource(R.string.riep_fino_a, r.numeroImpostori)
    } else {
        pluralStringResource(R.plurals.riep_impostori, r.numeroImpostori, r.numeroImpostori)
    }
    val giocatori = pluralStringResource(R.plurals.riep_giocatori, r.numeroGiocatori, r.numeroGiocatori)
    val opzioni = if (r.opzioni.isEmpty()) {
        stringResource(R.string.riep_nessuna_opzione)
    } else {
        r.opzioni.map { o ->
            when (o) {
                OpzioneRiepilogo.NON_PARLA_PER_PRIMO -> stringResource(R.string.opz_breve_non_primo)
                OpzioneRiepilogo.TRAPPOLA -> stringResource(R.string.opz_breve_trappola)
                OpzioneRiepilogo.ORDINE_CASUALE -> stringResource(R.string.opz_breve_ordine_casuale)
                OpzioneRiepilogo.PROMEMORIA -> stringResource(R.string.opz_breve_promemoria)
                OpzioneRiepilogo.SENZA_CATEGORIA -> stringResource(R.string.riep_senza_categoria)
                OpzioneRiepilogo.GIRI -> stringResource(R.string.opz_breve_giri, r.giriIndizi)
            }
        }.joinToString(", ")
    }
    val attive = Passi.contaOpzioniAttive(stato.config)
    Column(verticalArrangement = Arrangement.spacedBy(Spazio.s3)) {
        RigaRiepilogo(
            etichetta = stringResource(R.string.passo_giocatori),
            valore = giocatori,
            secondaria = r.nomi.joinToString(", "),
            onModifica = { onModifica(1) },
        )
        RigaRiepilogo(
            etichetta = stringResource(R.string.config_modalita),
            valore = stringResource(
                if (r.modalita == Modalita.SENZA_PAROLA) R.string.modalita_senza_parola else R.string.modalita_affine,
            ),
            secondaria = impostori,
            onModifica = { onModifica(2) },
        )
        RigaRiepilogo(
            etichetta = stringResource(R.string.opz_titolo),
            valore = opzioni,
            badge = attive.takeIf { it > 0 },
            onModifica = { onModifica(3) },
        )
        RigaRiepilogo(
            etichetta = stringResource(R.string.config_categorie),
            valore = pluralStringResource(R.plurals.riep_categorie, r.numeroCategorie, r.numeroCategorie),
            secondaria = stringResource(R.string.config_parole_rimanenti, stato.paroleRimanenti, stato.paroleTotali),
            onModifica = { onModifica(4) },
        )
    }
}

/** Riga di riepilogo: tutta toccabile, porta al passo indicato. */
@Composable
private fun RigaRiepilogo(
    etichetta: String,
    valore: String,
    onModifica: () -> Unit,
    secondaria: String? = null,
    badge: Int? = null,
) {
    Surface(
        onClick = onModifica,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        border = bordoLivello(),
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, top = Spazio.s2, bottom = Spazio.s2, end = Spazio.s2),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    etichetta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(valore, style = MaterialTheme.typography.titleMedium)
                if (secondaria != null) {
                    Text(
                        secondaria,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (badge != null) BadgeAttive(badge)
            Box(Modifier.heightIn(min = Spazio.altezzaTocco).padding(horizontal = Spazio.s3), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.passo_modifica),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Badge "N attive" (primaryContainer). */
@Composable
fun BadgeAttive(n: Int) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            pluralStringResource(R.plurals.opz_attive, n, n),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = Spazio.s3, vertical = Spazio.s1),
        )
    }
}

@Composable
private fun TitoloSezione(testo: String) {
    Text(testo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
}

/** Contatore: riga in carta con etichetta a sinistra e "-" valore "+" a destra; il valore rimbalza al cambio. */
@Composable
private fun Contatore(etichetta: String, valore: Int, min: Int, max: Int, onCambia: (Int) -> Unit) {
    val ridotto = rilevaRiduciAnimazioni()
    val scala = remember { Animatable(1f) }
    val primo = remember { booleanArrayOf(true) }
    LaunchedEffect(valore) {
        if (primo[0]) {
            primo[0] = false
        } else if (!ridotto) {
            scala.snapTo(1.12f)
            scala.animateTo(1f, mollaEffetti())
        }
    }
    val menoCd = stringResource(R.string.config_diminuisci)
    val piuCd = stringResource(R.string.config_aumenta)
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        border = bordoLivello(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, end = Spazio.s3, top = Spazio.s2, bottom = Spazio.s2),
        ) {
            Text(etichetta, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            FilledTonalIconButton(
                onClick = { onCambia(valore - 1) },
                enabled = valore > min,
                modifier = Modifier.size(Spazio.altezzaTocco).semantics { contentDescription = menoCd },
            ) { Text("−", style = MaterialTheme.typography.titleLarge) }
            Text(
                valore.toString(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(min = 40.dp)
                    .graphicsLayer { scaleX = scala.value; scaleY = scala.value },
            )
            FilledTonalIconButton(
                onClick = { onCambia(valore + 1) },
                enabled = valore < max,
                modifier = Modifier.size(Spazio.altezzaTocco).semantics { contentDescription = piuCd },
            ) { Text("+", style = MaterialTheme.typography.titleLarge) }
        }
    }
}
