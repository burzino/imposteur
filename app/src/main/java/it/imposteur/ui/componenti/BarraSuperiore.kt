package it.imposteur.ui.componenti

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.imposteur.R
import it.imposteur.ui.theme.Spazio
import it.imposteur.ui.theme.bordoLivello

/** Tasto "Home" uniforme, da mettere nelle azioni della barra superiore. */
@Composable
fun AzioneHome(onHome: () -> Unit) {
    IconButton(onClick = onHome) {
        Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.torna_home))
    }
}

/**
 * Barra superiore: titolo a sinistra (headlineMedium), freccia indietro opzionale, azione Home opzionale.
 * Sfondo `background`; scorrendo passa a `surfaceContainer` (tonale, senza ombra).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperiore(
    titolo: String,
    modifier: Modifier = Modifier,
    onIndietro: (() -> Unit)? = null,
    onHome: (() -> Unit)? = null,
    azioni: @Composable RowScope.() -> Unit = {},
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior? = null,
) {
    val sollevata = (scrollBehavior?.state?.overlappedFraction ?: 0f) > 0.01f
    val sfondo by animateColorAsState(
        if (sollevata) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.background,
        label = "sfondoBarra",
    )
    // Lo sfondo copre tutta la larghezza; titolo e azioni stanno nella colonna da 480 dp, centrata.
    Box(modifier = modifier.fillMaxWidth().background(sfondo), contentAlignment = Alignment.TopCenter) {
    TopAppBar(
        title = {
            Text(titolo, style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        modifier = Modifier.widthIn(max = Spazio.larghezzaMax).fillMaxWidth(),
        navigationIcon = {
            if (onIndietro != null) {
                IconButton(onClick = onIndietro) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.indietro))
                }
            }
        },
        actions = {
            azioni()
            if (onHome != null) AzioneHome(onHome)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        scrollBehavior = scrollBehavior,
    )
    }
}

/**
 * Scaffold edge-to-edge con [BarraSuperiore]: lo sfondo passa sotto le barre di sistema, il contenuto
 * riceve gli inset come [PaddingValues]. Il contenuto e' limitato a 480 dp e centrato.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldConBarra(
    titolo: String,
    modifier: Modifier = Modifier,
    onIndietro: (() -> Unit)? = null,
    onHome: (() -> Unit)? = null,
    azioni: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scroll.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = snackbarHost,
        topBar = { BarraSuperiore(titolo, onIndietro = onIndietro, onHome = onHome, azioni = azioni, scrollBehavior = scroll) },
        bottomBar = bottomBar,
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(Modifier.widthIn(max = Spazio.larghezzaMax).fillMaxSize()) { content(PaddingValues()) }
        }
    }
}

/**
 * Barra azioni fissa in basso: surfaceContainer, raggio superiore 28, padding 16, sopra gli inset di
 * sistema e la tastiera.
 */
@Composable
fun BarraAzioni(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = bordoLivello(),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = Spazio.larghezzaMax).fillMaxWidth().padding(Spazio.s4),
                verticalArrangement = Arrangement.spacedBy(Spazio.s2),
                content = content,
            )
        }
    }
}
