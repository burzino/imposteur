package it.imposteur.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

enum class Tema { SISTEMA, CHIARO, SCURO, ALTO_CONTRASTO }

data class Aspetto(val tema: Tema = Tema.SISTEMA, val coloriDinamici: Boolean = true)

class RepositoryAspetto(private val context: Context) {
    private val chiaveTema = stringPreferencesKey("aspetto_tema")
    private val chiaveDinamici = booleanPreferencesKey("aspetto_colori_dinamici")

    val aspetto: Flow<Aspetto> = context.dataStore.data
        .map { p ->
            Aspetto(
                tema = Tema.entries.firstOrNull { it.name == p[chiaveTema] } ?: Tema.SISTEMA,
                coloriDinamici = p[chiaveDinamici] ?: true,
            )
        }
        .catch { emit(Aspetto()) }

    suspend fun salva(a: Aspetto) {
        context.dataStore.edit {
            it[chiaveTema] = a.tema.name
            it[chiaveDinamici] = a.coloriDinamici
        }
    }
}
