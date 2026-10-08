package it.imposteur.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
enum class MotivoSegnalazione { TROPPO_SIMILI, TROPPO_DIVERSE, POCO_CONOSCIUTA, CATEGORIA_SBAGLIATA }

@Serializable
data class Segnalazione(
    val tipo: String,
    val istante: String,
    val categoriaId: String? = null,
    val parola: String? = null,
    val affine: String? = null,
    val modalita: String? = null,
    val motivi: List<MotivoSegnalazione> = emptyList(),
    val nota: String = "",
)

object FormatoSegnalazioni {
    private const val MAX_NOTA = 500
    private val formato = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun pulisciNota(nota: String): String = nota.trim().take(MAX_NOTA).trim()

    fun riga(s: Segnalazione): String =
        formato.encodeToString(Segnalazione.serializer(), s.copy(nota = pulisciNota(s.nota)))

    fun leggi(testo: String): List<Segnalazione> =
        testo.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { r ->
                try {
                    formato.decodeFromString(Segnalazione.serializer(), r)
                        .let { it.copy(nota = pulisciNota(it.nota)) }
                } catch (e: Exception) {
                    null
                }
            }
            .toList()
}
