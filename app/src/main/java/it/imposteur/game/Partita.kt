package it.imposteur.game

sealed interface ContenutoRuolo {
    data class ParolaSegreta(val testo: String) : ContenutoRuolo
    data class Impostore(val categoria: String?) : ContenutoRuolo
}

data class Partita(
    val giocatori: List<String>,
    val impostori: Set<Int>,
    val voce: VoceParola,
    val modalita: Modalita,
    val mostraCategoria: Boolean,
    val primoGiocatore: Int,
    /** Ordine personalizzato (ordine casuale); null = rotazione da primoGiocatore, calcolata al momento. */
    val ordine: List<Int>? = null,
    val giriIndizi: Int = 1,
    val promemoriaUltimaPossibilita: Boolean = false,
) {
    val trappola: Boolean get() = impostori.isEmpty()

    fun contenutoPer(indice: Int): ContenutoRuolo {
        val impostore = indice in impostori
        return when {
            trappola || !impostore -> ContenutoRuolo.ParolaSegreta(voce.parola)
            modalita == Modalita.PAROLA_AFFINE -> ContenutoRuolo.ParolaSegreta(voce.affine ?: voce.parola)
            else -> ContenutoRuolo.Impostore(if (mostraCategoria) voce.categoriaNome else null)
        }
    }

    fun ordineDiParola(): List<Int> =
        ordine ?: List(giocatori.size) { (primoGiocatore + it) % giocatori.size }

    /** Indice del giocatore alla posizione k della sequenza di parola. */
    fun giocatoreAlPasso(k: Int): Int {
        require(k in giocatori.indices) { "posizione fuori intervallo: $k" }
        return ordineDiParola()[k]
    }

    fun testoSvelamento(): String {
        val nomi = impostori.sorted().map { giocatori[it] }
        val righe = mutableListOf<String>()
        righe += if (nomi.isEmpty()) TestiGioco.NESSUN_IMPOSTORE
        else if (nomi.size == 1) TestiGioco.impostoriSingolare(nomi[0]) else TestiGioco.impostoriPlurale(nomi)
        righe += TestiGioco.laParolaEra(voce.parola)
        if (modalita == Modalita.PAROLA_AFFINE && voce.affine != null) righe += TestiGioco.laParolaAffineEra(voce.affine)
        righe += TestiGioco.categoria(voce.categoriaNome)
        return righe.joinToString("\n")
    }
}

sealed interface RisultatoNuovaPartita {
    data class Ok(val partita: Partita) : RisultatoNuovaPartita
    data class Errore(val errori: List<ErroreConfigurazione>) : RisultatoNuovaPartita
}

/** Parola identificata per categoria; `parola` e' normalizzata (trim + minuscole). */
data class ChiaveParola(val categoriaId: String, val parola: String)

data class SessioneSalvata(
    val partita: Partita?,
    val stato: StatoDistribuzione?,
    val usate: Set<ChiaveParola>,
    val ultima: ChiaveParola?,
)

/** Vive per tutta la sessione: tiene l'insieme delle parole usate. */
class GestorePartite(
    private val random: kotlin.random.Random,
    usateIniziali: Set<ChiaveParola> = emptySet(),
    ultimaIniziale: ChiaveParola? = null,
) {
    private val usateInterne = usateIniziali.toMutableSet()
    private var ultimaInterna: ChiaveParola? = ultimaIniziale

    val usate: Set<ChiaveParola> get() = usateInterne.toSet()
    val ultima: ChiaveParola? get() = ultimaInterna

    private fun chiave(v: VoceParola) = ChiaveParola(v.categoriaId, v.parola.trim().lowercase())

    /** Numero di voci di `pool` la cui chiave non e' in `usate`. */
    fun rimanenti(pool: List<VoceParola>): Int = pool.count { chiave(it) !in usateInterne }

    /** Toglie dalle usate le chiavi di `pool`; se `ultima` appartiene a `pool` la mette a null. */
    fun azzeraUsate(pool: List<VoceParola>) {
        val chiavi = pool.map { chiave(it) }.toSet()
        usateInterne.removeAll(chiavi)
        if (ultimaInterna in chiavi) ultimaInterna = null
    }

    fun nuovaPartita(config: Configurazione, categorie: List<Categoria>): RisultatoNuovaPartita {
        val errori = Regole.valida(config, categorie)
        if (errori.isNotEmpty()) return RisultatoNuovaPartita.Errore(errori)
        val pool = Regole.pool(categorie, config.categorieSelezionate, config.modalita)
        var candidati = pool.filter { chiave(it) !in usateInterne }
        if (candidati.isEmpty()) {
            usateInterne.removeAll(pool.map { chiave(it) }.toSet())
            candidati = if (pool.size >= 2) pool.filter { chiave(it) != ultimaInterna } else pool
        }
        val voce = candidati[random.nextInt(candidati.size)]
        usateInterne += chiave(voce)
        ultimaInterna = chiave(voce)
        val n = config.numeroGiocatori
        val k = when {
            config.partitaTrappola && random.nextDouble() < Regole.PROBABILITA_TRAPPOLA -> 0
            config.impostoriSorpresa -> random.nextInt(1, config.numeroImpostori + 1)
            else -> config.numeroImpostori
        }
        val impostori = (0 until n).shuffled(random).take(k).toSet()
        val primo = if (config.impostoreNonPrimo && k > 0) {
            val civili = (0 until n).filter { it !in impostori }
            civili[random.nextInt(civili.size)]
        } else random.nextInt(n)
        val ordine = if (config.ordineCasuale) {
            listOf(primo) + (0 until n).filter { it != primo }.shuffled(random)
        } else null
        return RisultatoNuovaPartita.Ok(
            Partita(
                giocatori = Regole.nomiEffettivi(config),
                impostori = impostori,
                voce = voce,
                modalita = config.modalita,
                mostraCategoria = config.mostraCategoria,
                primoGiocatore = primo,
                ordine = ordine,
                giriIndizi = config.giriIndizi.coerceIn(1, Regole.MAX_GIRI),
                promemoriaUltimaPossibilita = config.promemoriaUltimaPossibilita,
            )
        )
    }
}

sealed interface StatoDistribuzione {
    data class Passaggio(val indice: Int) : StatoDistribuzione
    data class Rivelazione(val indice: Int) : StatoDistribuzione
    data object Gioco : StatoDistribuzione
}

object Distribuzione {
    fun iniziale(): StatoDistribuzione = StatoDistribuzione.Passaggio(0)

    fun avanza(stato: StatoDistribuzione, numeroGiocatori: Int): StatoDistribuzione = when (stato) {
        is StatoDistribuzione.Passaggio -> StatoDistribuzione.Rivelazione(stato.indice)
        is StatoDistribuzione.Rivelazione ->
            if (stato.indice + 1 < numeroGiocatori) StatoDistribuzione.Passaggio(stato.indice + 1)
            else StatoDistribuzione.Gioco
        StatoDistribuzione.Gioco -> StatoDistribuzione.Gioco
    }

    fun interrompiRivelazione(stato: StatoDistribuzione): StatoDistribuzione =
        if (stato is StatoDistribuzione.Rivelazione) StatoDistribuzione.Passaggio(stato.indice) else stato
}
