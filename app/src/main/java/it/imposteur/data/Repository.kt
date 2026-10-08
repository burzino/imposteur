package it.imposteur.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import it.imposteur.game.Categoria
import it.imposteur.game.Configurazione
import it.imposteur.game.SessioneSalvata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal val Context.dataStore by preferencesDataStore(name = "impostore_config")

class RepositoryParole(private val context: Context) {
    suspend fun carica(): RisultatoCaricamento = withContext(Dispatchers.IO) {
        try {
            val testo = context.assets.open("parole.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            ParserParole.parse(testo)
        } catch (e: Exception) {
            RisultatoCaricamento.Errore("Impossibile leggere le parole: ${e.message}")
        }
    }
}

class RepositorySessione(private val context: Context) {
    private val chiave = stringPreferencesKey("sessione")

    suspend fun leggi(categorie: List<Categoria>): SessioneSalvata {
        val s = try {
            context.dataStore.data.first()[chiave]
        } catch (e: Exception) {
            null
        }
        return SerializzazioneSessione.daStringa(s, categorie)
    }

    suspend fun salva(s: SessioneSalvata) {
        context.dataStore.edit { it[chiave] = SerializzazioneSessione.aStringa(s) }
    }
}

class RepositoryConfigurazione(private val context: Context) {
    private val chiave = stringPreferencesKey("configurazione")

    suspend fun leggi(categorie: List<Categoria>): Configurazione {
        val s = try {
            context.dataStore.data.first()[chiave]
        } catch (e: Exception) {
            null
        }
        return SerializzazioneConfigurazione.daStringa(s, categorie)
    }

    suspend fun salva(config: Configurazione) {
        context.dataStore.edit { it[chiave] = SerializzazioneConfigurazione.aStringa(config) }
    }
}
