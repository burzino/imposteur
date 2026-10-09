package it.imposteur.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Molle di design.md 2.6, uguali a MotionScheme.expressive() (smorzamento, rigidezza). */
fun <T> mollaSpaziale(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = 700f)

fun <T> mollaSpazialeLenta(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = 300f)

fun <T> mollaEffetti(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 1600f)

const val DURATA_VELOCE_MS = 100
const val DURATA_BARRA_MS = 300
const val DURATA_RILASCIO_BARRA_MS = 150
const val DURATA_PRONTI_MS = 1200L

/** True se "Riduci animazioni" e' attivo (scala delle animazioni a 0): niente rimbalzo ne' rotazione 3D. */
@Composable
fun rilevaRiduciAnimazioni(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
