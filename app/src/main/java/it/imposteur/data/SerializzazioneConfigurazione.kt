package it.imposteur.data

import it.imposteur.game.Categoria
import it.imposteur.game.Configurazione
import it.imposteur.game.Modalita
import it.imposteur.game.Regole
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class ConfigurazioneDto(
    val numeroGiocatori: Int = 4,
    val nomi: List<String> = emptyList(),
    val numeroImpostori: Int = 1,
    val modalita: String = Modalita.SENZA_PAROLA.name,
    val mostraCategoria: Boolean = true,
    val categorieSelezionate: List<String> = emptyList(),
    val impostoreNonPrimo: Boolean = false,
    val impostoriSorpresa: Boolean = false,
    val ordineCasuale: Boolean = false,
    val partitaTrappola: Boolean = false,
    val promemoriaUltimaPossibilita: Boolean = false,
    val giriIndizi: Int = 1,
)

object SerializzazioneConfigurazione {
    private val formato = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun aStringa(config: Configurazione): String = formato.encodeToString(
        ConfigurazioneDto(
            config.numeroGiocatori, config.nomi, config.numeroImpostori, config.modalita.name,
            config.mostraCategoria, config.categorieSelezionate.toList(),
            config.impostoreNonPrimo, config.impostoriSorpresa, config.ordineCasuale,
            config.partitaTrappola, config.promemoriaUltimaPossibilita, config.giriIndizi,
        )
    )

    fun daStringa(s: String?, categorie: List<Categoria>): Configurazione {
        val tutte = categorie.map { it.id }.toSet()
        val dto: ConfigurazioneDto? = if (s.isNullOrBlank()) null else try {
            formato.decodeFromString<ConfigurazioneDto>(s)
        } catch (e: Exception) {
            null
        }
        if (dto == null) return Configurazione(categorieSelezionate = tutte)
        val n = dto.numeroGiocatori.coerceIn(Regole.MIN_GIOCATORI, Regole.MAX_GIOCATORI)
        val selezionate = dto.categorieSelezionate.filter { it in tutte }.toSet().ifEmpty { tutte }
        return Configurazione(
            numeroGiocatori = n,
            nomi = dto.nomi.take(n).map { it.take(Regole.MAX_LUNGHEZZA_NOME) },
            numeroImpostori = dto.numeroImpostori.coerceIn(1, Regole.maxImpostori(n)),
            modalita = Modalita.entries.firstOrNull { it.name == dto.modalita } ?: Modalita.SENZA_PAROLA,
            mostraCategoria = dto.mostraCategoria,
            categorieSelezionate = selezionate,
            impostoreNonPrimo = dto.impostoreNonPrimo,
            impostoriSorpresa = dto.impostoriSorpresa,
            ordineCasuale = dto.ordineCasuale,
            partitaTrappola = dto.partitaTrappola,
            promemoriaUltimaPossibilita = dto.promemoriaUltimaPossibilita,
            giriIndizi = dto.giriIndizi.coerceIn(1, Regole.MAX_GIRI),
        )
    }
}
