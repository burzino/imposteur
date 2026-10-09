package it.imposteur.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spaziature e misure di design.md 2.4. */
object Spazio {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 24.dp
    val s6 = 32.dp
    val s7 = 48.dp

    /** 16 dp, 24 dp da 600 dp di larghezza (design.md 2.4). */
    val margineSchermata: Dp
        @Composable get() = if (LocalConfiguration.current.screenWidthDp >= 600) s5 else s4
    val altezzaTocco = 48.dp
    val altezzaPulsante = 56.dp
    val altezzaPulsanteGrande = 72.dp
    val larghezzaMax = 480.dp
    val avatarRiga = 40.dp
    val avatarGrande = 96.dp
    val passoAltezza = 8.dp
    val passoSpazio = 4.dp
    const val PASSO_CORRENTE_PESO = 2.4f
}
