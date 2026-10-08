package it.imposteur.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import it.imposteur.data.Aspetto
import it.imposteur.data.Tema

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F2B96),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6DEFF),
    onPrimaryContainer = Color(0xFF1B0F5C),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCBBEFF),
    onPrimary = Color(0xFF2A1A7A),
    primaryContainer = Color(0xFF4A3AA8),
    onPrimaryContainer = Color(0xFFE6DEFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
)

private val Giallo = Color(0xFFFFD600)
private val RossoChiaro = Color(0xFFFF8A80)

private val AltoContrastoColors = darkColorScheme(
    primary = Giallo,
    onPrimary = Color.Black,
    primaryContainer = Giallo,
    onPrimaryContainer = Color.Black,
    secondary = Giallo,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A1A1A),
    onSecondaryContainer = Color.White,
    tertiary = Giallo,
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color.Black,
    onSurfaceVariant = Color.White,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Color.Black,
    surfaceContainerHigh = Color.Black,
    surfaceContainerHighest = Color.Black,
    surfaceBright = Color.Black,
    surfaceDim = Color.Black,
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    outline = Color.White,
    outlineVariant = Color.White,
    error = RossoChiaro,
    onError = Color.Black,
    errorContainer = Color.Black,
    onErrorContainer = RossoChiaro,
)

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

/** True se, con questo aspetto, l'interfaccia e' scura. */
@Composable
fun usaTemaScuro(aspetto: Aspetto): Boolean = when (aspetto.tema) {
    Tema.SISTEMA -> isSystemInDarkTheme()
    Tema.CHIARO -> false
    Tema.SCURO, Tema.ALTO_CONTRASTO -> true
}

@Composable
fun ImpostoreTheme(aspetto: Aspetto, content: @Composable () -> Unit) {
    val scuro = usaTemaScuro(aspetto)
    val altoContrasto = aspetto.tema == Tema.ALTO_CONTRASTO
    val dinamici = aspetto.coloriDinamici && !altoContrasto && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme: ColorScheme = when {
        altoContrasto -> AltoContrastoColors
        dinamici -> {
            val context = LocalContext.current
            if (scuro) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        scuro -> DarkColors
        else -> LightColors
    }
    val tipografia = if (altoContrasto) Typography().scala(SCALA_ALTO_CONTRASTO) else Typography()
    MaterialTheme(colorScheme = colorScheme, typography = tipografia, content = content)
}
