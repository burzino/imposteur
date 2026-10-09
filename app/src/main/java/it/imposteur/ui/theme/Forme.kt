package it.imposteur.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** --raggio-xs..xl di design.md 2.3. */
internal val FormeImpostore = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object Forme {
    /** --raggio-xxl: carta che si gira, pulsante a pressione lunga. */
    val XXL = RoundedCornerShape(36.dp)

    /** --raggio-pieno. */
    val Piena = CircleShape
}
