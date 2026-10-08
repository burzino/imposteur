package it.imposteur.data

import it.imposteur.game.Categoria
import it.imposteur.game.Parola
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed interface RisultatoCaricamento {
    data class Ok(val categorie: List<Categoria>) : RisultatoCaricamento
    data class Errore(val messaggio: String) : RisultatoCaricamento
}

@Serializable
internal data class ParolaDto(val parola: String? = null, val affine: String? = null)

@Serializable
internal data class CategoriaDto(val id: String, val nome: String, val parole: List<ParolaDto> = emptyList())

@Serializable
internal data class FileParoleDto(val versione: Int, val categorie: List<CategoriaDto> = emptyList())

object ParserParole {
    private val formato = Json { ignoreUnknownKeys = true }

    fun parse(json: String): RisultatoCaricamento {
        val dto = try {
            formato.decodeFromString<FileParoleDto>(json)
        } catch (e: Exception) {
            return RisultatoCaricamento.Errore("JSON non valido: ${e.message}")
        }
        if (dto.versione != 1) return RisultatoCaricamento.Errore("Versione non supportata: ${dto.versione}")
        val ids = dto.categorie.map { it.id }
        if (ids.size != ids.toSet().size) return RisultatoCaricamento.Errore("Id categoria duplicato")
        val categorie = dto.categorie.map { c ->
            val viste = mutableSetOf<String>()
            val parole = mutableListOf<Parola>()
            for (p in c.parole) {
                val testo = p.parola?.trim().orEmpty()
                if (testo.isEmpty()) return RisultatoCaricamento.Errore("Parola vuota nella categoria ${c.id}")
                if (!viste.add(testo.lowercase())) continue
                val affine = p.affine?.trim()?.takeIf { it.isNotEmpty() && !it.equals(testo, ignoreCase = true) }
                parole += Parola(testo, affine)
            }
            Categoria(c.id, c.nome, parole)
        }
        return RisultatoCaricamento.Ok(categorie)
    }
}
