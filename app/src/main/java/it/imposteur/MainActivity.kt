package it.imposteur

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.imposteur.ui.ImpostoreViewModel
import it.imposteur.ui.navigazione.ImpostoreNavHost
import it.imposteur.ui.theme.ImpostoreTheme
import it.imposteur.ui.theme.usaTemaScuro

class MainActivity : ComponentActivity() {
    private val viewModel: ImpostoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val aspetto by viewModel.aspetto.collectAsStateWithLifecycle()
            val scuro = usaTemaScuro(aspetto)
            // Barre di sistema coerenti con il tema scelto (icone chiare su scuro e viceversa).
            DisposableEffect(scuro) {
                enableEdgeToEdge(
                    statusBarStyle = if (scuro) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (scuro) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                )
                onDispose {}
            }
            ImpostoreTheme(aspetto) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ImpostoreNavHost(viewModel)
                }
            }
        }
    }
}
