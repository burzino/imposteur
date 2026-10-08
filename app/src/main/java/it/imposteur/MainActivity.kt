package it.imposteur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import it.imposteur.ui.ImpostoreViewModel
import it.imposteur.ui.navigazione.ImpostoreNavHost
import it.imposteur.ui.theme.ImpostoreTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ImpostoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpostoreTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ImpostoreNavHost(viewModel)
                }
            }
        }
    }
}
