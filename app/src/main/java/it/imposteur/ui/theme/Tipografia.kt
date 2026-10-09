package it.imposteur.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import it.imposteur.R

/** Roboto Flex variabile incorporato, un'istanza per peso. */
@OptIn(ExperimentalTextApi::class)
val RobotoFlex = FontFamily(
    listOf(400, 500, 600, 700, 800).map { peso ->
        Font(
            R.font.roboto_flex,
            weight = FontWeight(peso),
            variationSettings = FontVariation.Settings(FontVariation.weight(peso)),
        )
    },
)

private fun TextStyle.stile(dimensione: Int, interlinea: Int, peso: Int) = copy(
    fontFamily = RobotoFlex,
    fontSize = dimensione.sp,
    lineHeight = interlinea.sp,
    fontWeight = FontWeight(peso),
    letterSpacing = TextUnit.Unspecified,
)

private fun TextStyle.famiglia() = copy(fontFamily = RobotoFlex)

/** Tabella di design.md 2.2; gli stili non elencati mantengono le misure Material con il font incorporato. */
internal fun tipografiaImpostore(): Typography {
    val b = Typography()
    return Typography(
        displayLarge = b.displayLarge.famiglia(),
        displayMedium = b.displayMedium.stile(44, 52, 800),
        displaySmall = b.displaySmall.stile(36, 44, 800),
        headlineLarge = b.headlineLarge.famiglia(),
        headlineMedium = b.headlineMedium.stile(28, 36, 700),
        headlineSmall = b.headlineSmall.famiglia(),
        titleLarge = b.titleLarge.stile(22, 28, 700),
        titleMedium = b.titleMedium.stile(16, 24, 600),
        titleSmall = b.titleSmall.famiglia(),
        bodyLarge = b.bodyLarge.stile(16, 24, 400),
        bodyMedium = b.bodyMedium.stile(14, 20, 400),
        bodySmall = b.bodySmall.famiglia(),
        labelLarge = b.labelLarge.stile(14, 20, 600),
        labelMedium = b.labelMedium.stile(12, 16, 500),
        labelSmall = b.labelSmall.famiglia(),
    )
}
