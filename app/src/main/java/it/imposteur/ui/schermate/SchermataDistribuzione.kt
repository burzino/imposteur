package it.imposteur.ui.schermate

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import it.imposteur.R
import it.imposteur.game.ContenutoRuolo
import it.imposteur.game.Modalita
import it.imposteur.game.Partita
import it.imposteur.game.StatoDistribuzione
import it.imposteur.ui.UiState

@Composable
fun SchermataDistribuzione(
    stato: UiState,
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
        ScaffoldConHome(titolo = R.string.distribuzione_titolo, onHome = onHome) {
            when (fase) {
                is StatoDistribuzione.Passaggio -> Passaggio(partita, fase.indice) { onSono(fase) }
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

/** Struttura fissa: contenuto al centro, pulsante ancorato in basso (zona del pollice). */
@Composable
internal fun Contenitore(
    sfondo: Brush,
    azione: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sfondo)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.navigationBarsPadding()) { azione() }
    }
}

private fun iniziale(nome: String) = nome.trim().take(1).uppercase()

/** Fila di avatar: gia' visti (pieni con spunta), corrente (anello), successivi (attenuati). */
@Composable
private fun FilaAvatar(giocatori: List<String>, corrente: Int) {
    @Composable
    fun Avatar(i: Int) {
        val colori = MaterialTheme.colorScheme
        val dimensione = if (i == corrente) 36.dp else 30.dp
        val base = Modifier.size(dimensione).clip(CircleShape)
        when {
            i < corrente -> Box(base.background(colori.primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = colori.onPrimary, modifier = Modifier.size(18.dp))
            }
            i == corrente -> Box(
                base
                    .background(colori.primaryContainer)
                    .border(3.dp, colori.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(iniziale(giocatori[i]), style = MaterialTheme.typography.titleSmall, color = colori.onPrimaryContainer)
            }
            else -> Box(
                base.background(colori.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    iniziale(giocatori[i]),
                    style = MaterialTheme.typography.labelLarge,
                    color = colori.onPrimaryContainer.copy(alpha = 0.6f),
                )
            }
        }
    }

    // Decorativa: l'informazione e' nel testo "Giocatore n di N".
    val modificatore = Modifier.fillMaxWidth().clearAndSetSemantics { }
    if (giocatori.size <= 8) {
        Row(
            modifier = modificatore,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) { giocatori.indices.forEach { Avatar(it) } }
    } else {
        val stato = rememberLazyListState()
        val densita = LocalDensity.current
        LaunchedEffect(corrente) {
            val larghezza = stato.layoutInfo.viewportSize.width
            val offset = -(larghezza / 2 - with(densita) { 18.dp.roundToPx() })
            stato.animateScrollToItem(corrente, offset)
        }
        LazyRow(
            state = stato,
            modifier = modificatore,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) { itemsIndexed(giocatori) { i, _ -> Avatar(i) } }
    }
}

@Composable
internal fun Passaggio(
    partita: Partita,
    indice: Int,
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
    Contenitore(
        sfondo = SolidColor(MaterialTheme.colorScheme.surfaceContainerHighest),
        azione = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PulsanteTieniPremuto(indice = indice, abilitato = abilitato, onRivela = onSono)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.distribuzione_sono_premuto, nome),
                    style = MaterialTheme.typography.bodySmall,
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val respiro by rememberInfiniteTransition(label = "respiro").animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
            label = "scala",
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer { scaleX = respiro; scaleY = respiro }
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                iniziale(nome),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            stringResource(R.string.distribuzione_passa_a),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TestoAdattivo(
            testo = nome,
            stile = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            maxRighe = 2,
        )
        Text(
            stringResource(R.string.distribuzione_non_guardare),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Pulsante a pressione prolungata: la barra si riempie in [DURATA_PRESSIONE_MS], poi rivela. */
@Composable
private fun PulsanteTieniPremuto(indice: Int, abilitato: Boolean, onRivela: () -> Unit) {
    val progresso = remember(indice) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val rivela by rememberUpdatedState(onRivela)
    val abilitatoAttuale by rememberUpdatedState(abilitato)
    val colori = MaterialTheme.colorScheme
    val etichetta = stringResource(R.string.distribuzione_tieni_premuto)
    val azioneCd = stringResource(R.string.distribuzione_scopri_cd)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer { alpha = if (abilitato) 1f else 0.5f }
            .clip(RoundedCornerShape(32.dp))
            .background(colori.primary)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick(label = azioneCd) {
                    if (abilitatoAttuale) {
                        rivela()
                        true
                    } else {
                        false
                    }
                }
            }
            .pointerInput(indice) {
                detectTapGestures(
                    onPress = {
                        if (!abilitatoAttuale) return@detectTapGestures
                        val lavoro = scope.launch {
                            progresso.animateTo(1f, tween(DURATA_PRESSIONE_MS, easing = LinearEasing))
                            rivela()
                        }
                        tryAwaitRelease()
                        lavoro.cancel()
                        if (progresso.value < 1f) scope.launch { progresso.snapTo(0f) }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(progresso.value)
                .fillMaxSize()
                .background(colori.onPrimary.copy(alpha = 0.3f)),
        )
        Text(etichetta, style = MaterialTheme.typography.titleLarge, color = colori.onPrimary)
    }
}

/** Ritardo anti doppio tocco prima di abilitare i pulsanti "Tieni premuto" e "Nascondi e passa". */
private const val RITARDO_NASCONDI_MS = 600L
private const val DURATA_PRESSIONE_MS = 300
private const val DURATA_PRONTI_MS = 1200L

@Composable
internal fun Rivelazione(
    partita: Partita,
    indice: Int,
    testoPulsante: Int? = null,
    onNascondiEPassa: () -> Unit,
) {
    val contenuto = partita.contenutoPer(indice)
    val ultimo = indice == partita.giocatori.size - 1
    val etichettaPulsante = testoPulsante
        ?: if (ultimo) R.string.distribuzione_nascondi_ultimo else R.string.distribuzione_nascondi
    val haptic = LocalHapticFeedback.current
    val colori = MaterialTheme.colorScheme

    // Flip 3D della carta; la vibrazione e' una sola, identica per tutti, alla comparsa del fronte.
    val rotazione = remember(indice) { Animatable(180f) }
    LaunchedEffect(indice) {
        rotazione.animateTo(90f, tween(225, easing = LinearEasing))
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        rotazione.animateTo(0f, tween(225, easing = LinearOutSlowInEasing))
    }
    // Il pulsante si abilita dopo un breve ritardo, per evitare il doppio tocco.
    var abilitato by remember(indice) { mutableStateOf(false) }
    LaunchedEffect(indice) {
        delay(RITARDO_NASCONDI_MS)
        abilitato = true
    }
    val densita = LocalDensity.current.density

    Contenitore(
        sfondo = Brush.verticalGradient(listOf(colori.secondaryContainer, colori.surface)),
        azione = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNascondiEPassa()
                },
                enabled = abilitato,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colori.secondary,
                    contentColor = colori.onSecondary,
                ),
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Text(stringResource(etichettaPulsante), style = MaterialTheme.typography.titleLarge)
            }
        },
    ) {
        Text(
            stringResource(R.string.distribuzione_indicatore, indice + 1, partita.giocatori.size),
            style = MaterialTheme.typography.titleMedium,
            color = colori.onSurfaceVariant,
        )
        val dorso = rotazione.value > 90f
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (dorso) colori.primary else colori.tertiaryContainer,
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(min = 300.dp)
                .graphicsLayer {
                    rotationY = rotazione.value
                    cameraDistance = 12f * densita
                },
        ) {
            if (dorso) {
                // Dorso: la rotazione lo specchia, lo raddrizziamo.
                Box(
                    Modifier.fillMaxWidth().heightIn(min = 300.dp).graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_launcher_monochrome),
                        contentDescription = null,
                        tint = colori.onPrimary,
                        modifier = Modifier.size(160.dp),
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
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
                                style = MaterialTheme.typography.titleMedium,
                                color = colori.onTertiaryContainer,
                                textAlign = TextAlign.Center,
                            )
                            TestoAdattivo(
                                testo = contenuto.testo,
                                stile = MaterialTheme.typography.displayLarge,
                                color = colori.onTertiaryContainer,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        is ContenutoRuolo.Impostore -> {
                            Icon(
                                painterResource(R.drawable.ic_launcher_monochrome),
                                contentDescription = null,
                                tint = colori.onTertiaryContainer,
                                modifier = Modifier.size(120.dp),
                            )
                            TestoAdattivo(
                                testo = stringResource(R.string.ruolo_sei_impostore),
                                stile = MaterialTheme.typography.displaySmall,
                                color = colori.onTertiaryContainer,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            contenuto.categoria?.let {
                                Text(
                                    stringResource(R.string.ruolo_categoria, it),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colori.onTertiaryContainer,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Overlay a tutto schermo mostrato prima del Gioco; un tocco lo salta. */
@Composable
private fun TuttiPronti(onTocco: () -> Unit) {
    var visibile by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visibile = true }
    val progresso by animateFloatAsState(
        targetValue = if (visibile) 1f else 0f,
        animationSpec = tween(400),
        label = "tuttiPronti",
    )
    val colori = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colori.primaryContainer)
            .clickable(onClick = onTocco),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .graphicsLayer {
                    alpha = progresso
                    scaleX = 0.8f + 0.2f * progresso
                    scaleY = 0.8f + 0.2f * progresso
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.distribuzione_tutti_pronti),
                style = MaterialTheme.typography.displaySmall,
                color = colori.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.distribuzione_bluff),
                style = MaterialTheme.typography.titleMedium,
                color = colori.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}
