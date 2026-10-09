package it.imposteur.ui.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import it.imposteur.data.Aspetto
import it.imposteur.data.Tema

private const val SCALA_ALTO_CONTRASTO = 1.15f

private fun TextUnit.scala(f: Float): TextUnit = if (this == TextUnit.Unspecified) this else this * f

private fun TextStyle.scala(f: Float): TextStyle =
    copy(fontSize = fontSize.scala(f), lineHeight = lineHeight.scala(f))

private fun Typography.scala(f: Float) = Typography(
    displayLarge = displayLarge.scala(f), displayMedium = displayMedium.scala(f), displaySmall = displaySmall.scala(f),
    headlineLarge = headlineLarge.scala(f), headlineMedium = headlineMedium.scala(f), headlineSmall = headlineSmall.scala(f),
    titleLarge = titleLarge.scala(f), titleMedium = titleMedium.scala(f), titleSmall = titleSmall.scala(f),
    bodyLarge = bodyLarge.scala(f), bodyMedium = bodyMedium.scala(f), bodySmall = bodySmall.scala(f),
    labelLarge = labelLarge.scala(f), labelMedium = labelMedium.scala(f), labelSmall = labelSmall.scala(f),
)

/** True se il tema attivo e' Alto contrasto. */
val LocalAltoContrasto = staticCompositionLocalOf { false }

/** Contorno dei livelli di superficie: 2 dp in Alto contrasto (design.md 2.5), altrimenti nessuno. */
@Composable
fun bordoLivello(colore: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface): BorderStroke? =
    if (LocalAltoContrasto.current) BorderStroke(2.dp, colore) else null

/** True se, con questo aspetto, l'interfaccia e' scura. */
@Composable
fun usaTemaScuro(aspetto: Aspetto): Boolean = when (aspetto.tema) {
    Tema.SISTEMA -> isSystemInDarkTheme()
    Tema.CHIARO -> false
    Tema.SCURO, Tema.ALTO_CONTRASTO -> true
}

/**
 * Le API Expressive (MaterialExpressiveTheme, MotionScheme.expressive()) sono interne in material3 1.4.0:
 * si usa MaterialTheme e il movimento e' in [mollaSpaziale] & co. (stessi parametri, design.md 2.6).
 */
@Composable
fun ImpostoreTheme(aspetto: Aspetto, content: @Composable () -> Unit) {
    val scuro = usaTemaScuro(aspetto)
    val altoContrasto = aspetto.tema == Tema.ALTO_CONTRASTO
    val dinamici = aspetto.coloriDinamici && !altoContrasto && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme: ColorScheme = when {
        altoContrasto -> ColoriAltoContrasto
        dinamici -> {
            val context = LocalContext.current
            if (scuro) androidx.compose.material3.dynamicDarkColorScheme(context)
            else androidx.compose.material3.dynamicLightColorScheme(context)
        }
        scuro -> ColoriScuro
        else -> ColoriChiaro
    }
    val tipografia = tipografiaImpostore().let { if (altoContrasto) it.scala(SCALA_ALTO_CONTRASTO) else it }
    CompositionLocalProvider(LocalAltoContrasto provides altoContrasto) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = FormeImpostore,
            typography = tipografia,
            content = content,
        )
    }
}
