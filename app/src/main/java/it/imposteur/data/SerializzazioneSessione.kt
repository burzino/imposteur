package it.imposteur.data

import it.imposteur.game.Categoria
import it.imposteur.game.ChiaveParola
import it.imposteur.game.Modalita
import it.imposteur.game.Partita
import it.imposteur.game.SessioneSalvata
import it.imposteur.game.StatoDistribuzione
import it.imposteur.game.VoceParola
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
internal data class ChiaveDto(val categoriaId: String = "", val parola: String = "")

@Serializable
internal data class PartitaDto(
    val giocatori: List<String>,
    val impostori: List<Int>,
    val categoriaId: String,
    val parola: String,
    val affine: String?,
    val modalita: String,
    val mostraCategoria: Boolean,
    val primoGiocatore: Int,
)

@Serializable
internal data class StatoDto(val tipo: String, val indice: Int)

@Serializable
internal data class SessioneDto(
    val partita: JsonElement? = null,
    val stato: JsonElement? = null,
    val usate: List<ChiaveDto> = emptyList(),
    val ultima: ChiaveDto? = null,
)

object SerializzazioneSessione {
    private val formato = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val vuota = SessioneSalvata(null, null, emptySet(), null)

    private fun norm(s: String) = s.trim().lowercase()

    fun aStringa(s: SessioneSalvata): String {
        val p = s.partita
        val dto = SessioneDto(
            partita = p?.let {
                formato.encodeToJsonElement(
                    PartitaDto(
                        it.giocatori, it.impostori.sorted(), it.voce.categoriaId, it.voce.parola,
                        it.voce.affine, it.modalita.name, it.mostraCategoria, it.primoGiocatore,
                    ),
                )
            },
            stato = if (p == null) null else when (val st = s.stato) {
                is StatoDistribuzione.Passaggio -> formato.encodeToJsonElement(StatoDto("PASSAGGIO", st.indice))
                is StatoDistribuzione.Rivelazione -> formato.encodeToJsonElement(StatoDto("RIVELAZIONE", st.indice))
                StatoDistribuzione.Gioco -> formato.encodeToJsonElement(StatoDto("GIOCO", 0))
                null -> null
            },
            usate = s.usate.map { ChiaveDto(it.categoriaId, it.parola) },
            ultima = s.ultima?.let { ChiaveDto(it.categoriaId, it.parola) },
        )
        return formato.encodeToString(dto)
    }

    fun daStringa(s: String?, categorie: List<Categoria>): SessioneSalvata {
        if (s.isNullOrBlank()) return vuota
        val dto = try {
            formato.decodeFromString<SessioneDto>(s)
        } catch (e: Exception) {
            return vuota
        }
        val esistenti = categorie.flatMap { c -> c.parole.map { ChiaveParola(c.id, norm(it.parola)) } }.toSet()
        fun chiave(k: ChiaveDto) = ChiaveParola(k.categoriaId, norm(k.parola))
        val usate = dto.usate.map(::chiave).filter { it in esistenti }.toSet()
        val ultima = dto.ultima?.let(::chiave)?.takeIf { it in esistenti }
        val (partita, stato) = ricostruisci(dto, categorie) ?: (null to null)
        return SessioneSalvata(partita, stato, usate, ultima)
    }

    private fun ricostruisci(dto: SessioneDto, categorie: List<Categoria>): Pair<Partita, StatoDistribuzione>? {
        val p = try {
            formato.decodeFromJsonElement<PartitaDto>(dto.partita ?: return null)
        } catch (e: Exception) {
            return null
        }
        val st = try {
            formato.decodeFromJsonElement<StatoDto>(dto.stato ?: return null)
        } catch (e: Exception) {
            return null
        }
        val n = p.giocatori.size
        if (n == 0 || p.impostori.isEmpty()) return null
        if (p.impostori.toSet().size != p.impostori.size) return null
        if (p.impostori.any { it !in 0 until n } || p.primoGiocatore !in 0 until n) return null
        val categoria = categorie.firstOrNull { it.id == p.categoriaId } ?: return null
        val parola = categoria.parole.firstOrNull { norm(it.parola) == norm(p.parola) } ?: return null
        val stato = when (st.tipo) {
            "PASSAGGIO", "RIVELAZIONE" -> {
                if (st.indice !in 0 until n) return null
                StatoDistribuzione.Passaggio(st.indice)
            }
            "GIOCO" -> StatoDistribuzione.Gioco
            else -> return null
        }
        val modalita = Modalita.entries.firstOrNull { it.name == p.modalita } ?: Modalita.SENZA_PAROLA
        if (modalita == Modalita.PAROLA_AFFINE) {
            if (p.affine.isNullOrBlank()) return null
            val attuale = parola.affine ?: return null
            if (norm(attuale) != norm(p.affine)) return null
        }
        val partita = Partita(
            giocatori = p.giocatori,
            impostori = p.impostori.toSet(),
            voce = VoceParola(categoria.id, categoria.nome, parola.parola, p.affine),
            modalita = modalita,
            mostraCategoria = p.mostraCategoria,
            primoGiocatore = p.primoGiocatore,
        )
        return partita to stato
    }
}
