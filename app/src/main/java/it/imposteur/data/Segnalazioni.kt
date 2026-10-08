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
    val propostaParola: String? = null,
    val propostaAffine: String? = null,
)

object FormatoSegnalazioni {
    private const val MAX_NOTA = 500
    private val formato = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private const val MAX_PROPOSTA = 40

    private fun pulisciProposta(p: String?): String? =
        p?.trim()?.take(MAX_PROPOSTA)?.trim()?.ifEmpty { null }

    fun normalizza(s: Segnalazione): Segnalazione = s.copy(
        nota = s.nota.trim().take(MAX_NOTA).trim(),
        propostaParola = pulisciProposta(s.propostaParola),
        propostaAffine = pulisciProposta(s.propostaAffine),
    )

    fun propostaValida(s: Segnalazione): Boolean {
        val p = pulisciProposta(s.propostaParola) ?: return false
        val a = pulisciProposta(s.propostaAffine) ?: return false
        return !p.equals(a, ignoreCase = true)
    }

    fun riga(s: Segnalazione): String =
        formato.encodeToString(Segnalazione.serializer(), normalizza(s))

    fun leggi(testo: String): List<Segnalazione> =
        testo.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { r ->
                try {
                    formato.decodeFromString(Segnalazione.serializer(), r)
                        .let { normalizza(it) }
                } catch (e: Exception) {
                    null
                }
            }
            .toList()
}
