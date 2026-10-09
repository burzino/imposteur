package it.imposteur.ui.schermate

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import it.imposteur.R
import it.imposteur.game.ContenutoRuolo
import it.imposteur.game.Modalita
import it.imposteur.game.Partita
import it.imposteur.game.StatoDistribuzione
import it.imposteur.ui.UiState
import it.imposteur.ui.componenti.Avatar
import it.imposteur.ui.componenti.CartaGirevole
import it.imposteur.ui.componenti.DialogoConferma
import it.imposteur.ui.componenti.FilaAvatar
import it.imposteur.ui.componenti.PulsantePieno
import it.imposteur.game.DurataPressione
import it.imposteur.ui.componenti.PulsantePressioneLunga
import it.imposteur.ui.componenti.ScaffoldConBarra
import it.imposteur.ui.theme.DURATA_PRONTI_MS
import it.imposteur.ui.theme.DURATA_VELOCE_MS
import it.imposteur.ui.theme.Forme
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello
import it.imposteur.ui.theme.mollaEffetti
import it.imposteur.ui.theme.mollaSpaziale
import it.imposteur.ui.theme.mollaSpazialeLenta
import it.imposteur.ui.theme.rilevaRiduciAnimazioni
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SchermataDistribuzione(
    stato: UiState,
    durataPressioneMs: Int,
    onSono: (StatoDistribuzione) -> Unit,
    onNascondiEPassa: (StatoDistribuzione) -> Unit,
    onInterrompiRivelazione: () -> Unit,
    onFineDistribuzione: () -> Unit,
    onInterrompiPartita: () -> Unit,
    onHome: () -> Unit,
) {
    var chiediInterruzione by rememberSaveable { mutableStateOf(false) }
    BackHandler { chiediInterruzione = true }

    // Schermo sempre acceso durante la distribuzione.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Niente screenshot né anteprima in Recents mentre un ruolo può essere visibile.
    SchermoProtetto()

    // Il ruolo non resta visibile se l'app va in background (include rotazione).
    val lifecycleOwner = LocalLifecycleOwner.current
    val interrompi by rememberUpdatedState(onInterrompiRivelazione)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) interrompi()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val partita = stato.partita
    val fase = stato.distribuzione

    // "Tutti pronti!": la partita e' gia' nello stato Gioco; la navigazione parte dopo l'overlay (o al tocco).
    var navigato by rememberSaveable { mutableStateOf(false) }
    val fine by rememberUpdatedState(onFineDistribuzione)
    val termina = {
        if (!navigato) {
            navigato = true
            fine()
        }
    }
    LaunchedEffect(fase) {
        if (fase is StatoDistribuzione.Gioco) {
            delay(DURATA_PRONTI_MS)
            termina()
        }
    }

    if (partita != null) {
        ScaffoldConBarra(titolo = stringResource(R.string.distribuzione_titolo), onHome = onHome) {
            when (fase) {
                is StatoDistribuzione.Passaggio -> Passaggio(partita, fase.indice, durataPressioneMs) { onSono(fase) }
                is StatoDistribuzione.Rivelazione -> Rivelazione(partita, fase.indice) { onNascondiEPassa(fase) }
                StatoDistribuzione.Gioco -> Unit
            }
        }
    }

    if (fase is StatoDistribuzione.Gioco) TuttiPronti(onTocco = { termina() })

    if (chiediInterruzione) {
        DialogoConferma(
            titolo = stringResource(R.string.interrompere_partita),
            messaggio = stringResource(R.string.interrompere_messaggio),
            etichettaSi = stringResource(R.string.interrompere_conferma),
            etichettaNo = stringResource(R.string.interrompere_continua),
            onSi = {
                chiediInterruzione = false
                onInterrompiPartita()
            },
            onNo = { chiediInterruzione = false },
        )
    }
}

/** Attiva FLAG_SECURE finché il composable è in composizione. */
@Composable
internal fun SchermoProtetto() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.trovaActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

private tailrec fun Context.trovaActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.trovaActivity()
    else -> null
}

/** Struttura fissa: contenuto al centro, azione ancorata in basso (zona del pollice). */
@Composable
internal fun Contenitore(
    azione: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spazio.s4)
            .padding(bottom = Spazio.s4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spazio.s5, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
        Spacer(Modifier.height(Spazio.s4))
        azione()
    }
}

@Composable
internal fun Passaggio(
    partita: Partita,
    indice: Int,
    durataPressioneMs: Int,
    mostraAvanzamento: Boolean = true,
    onSono: () -> Unit,
) {
    val nome = partita.giocatori[indice]
    // Anti doppio tocco: il pulsante si attiva solo dopo un ritardo da quando compare questo giocatore.
    var abilitato by remember(indice) { mutableStateOf(false) }
    LaunchedEffect(indice) {
        delay(RITARDO_NASCONDI_MS)
        abilitato = true
    }
    val soloTocco = DurataPressione.soloTocco(durataPressioneMs)
    val ridotto = rilevaRiduciAnimazioni()
    Contenitore(
        azione = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PulsantePressioneLunga(
                    etichetta = stringResource(if (soloTocco) R.string.distribuzione_tocca else R.string.distribuzione_tieni_premuto),
                    descrizioneAzione = stringResource(R.string.distribuzione_scopri_cd),
                    chiave = indice,
                    abilitato = abilitato,
                    durataMs = durataPressioneMs,
                    onCompletato = onSono,
                )
                Spacer(Modifier.height(Spazio.s2))
                Text(
                    stringResource(if (soloTocco) R.string.distribuzione_sono_tocca else R.string.distribuzione_sono_premuto, nome),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
    ) {
        // In Rivedi (CA-54) né fila di avatar né "Giocatore k di N"
        if (mostraAvanzamento) {
            FilaAvatar(partita.giocatori, indice)
            Text(
                stringResource(R.string.distribuzione_indicatore, indice + 1, partita.giocatori.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val respiro by if (ridotto) {
            remember { mutableFloatStateOf(1f) }
        } else {
            rememberInfiniteTransition(label = "respiro").animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                label = "scala",
            )
        }
        Text(
            stringResource(R.string.distribuzione_passa_a),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TestoAdattivo(
            testo = nome,
            stile = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            maxRighe = 2,
        )
        Avatar(
            nome = nome,
            indice = indice,
            misura = Spazio.avatarGrande,
            modifier = Modifier.graphicsLayer { scaleX = respiro; scaleY = respiro },
        )
        Surface(
            shape = Forme.Piena,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            border = bordoLivello(MaterialTheme.colorScheme.tertiary),
        ) {
            Text(
                stringResource(R.string.distribuzione_non_guardare),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spazio.s4, vertical = Spazio.s2),
            )
        }
    }
}

/** Ritardo anti doppio tocco prima di abilitare i pulsanti "Tieni premuto" e "Nascondi e passa". */
private const val RITARDO_NASCONDI_MS = 600L

@Composable
internal fun Rivelazione(
    partita: Partita,
    indice: Int,
    testoPulsante: Int? = null,
    mostraFila: Boolean = true,
    onNascondiEPassa: () -> Unit,
) {
    val contenuto = partita.contenutoPer(indice)
    val ultimo = indice == partita.giocatori.size - 1
    val etichettaPulsante = testoPulsante
        ?: if (ultimo) R.string.distribuzione_nascondi_ultimo else R.string.distribuzione_nascondi
    val haptic = LocalHapticFeedback.current
    val colori = MaterialTheme.colorScheme
    val ridotto = rilevaRiduciAnimazioni()

    // Flip 3D della carta (dissolvenza con "Riduci animazioni"); la vibrazione e' una sola, identica
    // per tutti, alla comparsa del fronte.
    val rotazione = remember(indice) { Animatable(if (ridotto) 0f else 180f) }
    val opacita = remember(indice) { Animatable(if (ridotto) 0f else 1f) }
    LaunchedEffect(indice) {
        if (ridotto) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            opacita.animateTo(1f, tween(DURATA_VELOCE_MS))
        } else {
            coroutineScope {
                launch {
                    snapshotFlow { rotazione.value }.first { it <= 90f }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                rotazione.animateTo(0f, mollaSpazialeLenta<Float>())
            }
        }
    }
    // Il pulsante si abilita dopo un breve ritardo, per evitare il doppio tocco.
    var abilitato by remember(indice) { mutableStateOf(false) }
    LaunchedEffect(indice) {
        delay(RITARDO_NASCONDI_MS)
        abilitato = true
    }

    Contenitore(
        azione = {
            PulsantePieno(
                testo = stringResource(etichettaPulsante),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNascondiEPassa()
                },
                abilitato = abilitato,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        if (mostraFila) FilaAvatar(partita.giocatori, indice)
        Text(
            stringResource(R.string.distribuzione_indicatore, indice + 1, partita.giocatori.size),
            style = MaterialTheme.typography.labelMedium,
            color = colori.onSurfaceVariant,
        )
        CartaGirevole(
            rotazione = rotazione.value,
            alpha = opacita.value,
            retro = {
                Text(
                    "?",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colori.primary,
                )
            },
            fronte = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spazio.s4, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (contenuto) {
                        is ContenutoRuolo.ParolaSegreta -> {
                            val etichetta = if (partita.modalita == Modalita.PAROLA_AFFINE) {
                                R.string.ruolo_la_tua_parola_e
                            } else {
                                R.string.ruolo_la_parola_e
                            }
                            Text(
                                stringResource(etichetta),
                                style = MaterialTheme.typography.titleLarge,
                                color = colori.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            TestoAdattivo(
                                testo = contenuto.testo,
                                stile = MaterialTheme.typography.displaySmall,
                                color = colori.primary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        is ContenutoRuolo.Impostore -> {
                            Icon(
                                painterResource(R.drawable.ic_launcher_monochrome),
                                contentDescription = null,
                                tint = colori.primary,
                                modifier = Modifier.size(120.dp),
                            )
                            TestoAdattivo(
                                testo = stringResource(R.string.ruolo_sei_impostore),
                                stile = MaterialTheme.typography.displaySmall,
                                color = colori.primary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            contenuto.categoria?.let {
                                Text(
                                    stringResource(R.string.ruolo_categoria, it),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colori.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

/** Overlay a tutto schermo mostrato prima del Gioco; un tocco lo salta. */
@Composable
private fun TuttiPronti(onTocco: () -> Unit) {
    val ridotto = rilevaRiduciAnimazioni()
    var visibile by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visibile = true }
    val scala by animateFloatAsState(
        targetValue = if (visibile) 1f else 0.8f,
        animationSpec = mollaSpaziale(),
        label = "scalaTuttiPronti",
    )
    val opacita by animateFloatAsState(
        targetValue = if (visibile) 1f else 0f,
        animationSpec = mollaEffetti(),
        label = "opacitaTuttiPronti",
    )
    val colori = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colori.tertiaryContainer)
            .clickable(onClick = onTocco)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(Spazio.s5)
                .graphicsLayer {
                    alpha = opacita
                    val s = if (ridotto) 1f else scala
                    scaleX = s
                    scaleY = s
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spazio.s2),
        ) {
            Text(
                stringResource(R.string.distribuzione_tutti_pronti),
                style = MaterialTheme.typography.displayMedium,
                color = colori.onTertiaryContainer,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.distribuzione_bluff),
                style = MaterialTheme.typography.titleMedium,
                color = colori.onTertiaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}
