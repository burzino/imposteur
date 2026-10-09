package it.imposteur.game

/** I cinque passi della Configurazione; ordinale + 1 = numero del passo (1..5). */
enum class PassoConfigurazione { GIOCATORI, MODALITA, OPZIONI, CATEGORIE, RIEPILOGO }

/** Opzioni avanzate non predefinite elencate nel riepilogo, nell'ordine di specifiche 4.2.1. */
enum class OpzioneRiepilogo { NON_PARLA_PER_PRIMO, TRAPPOLA, ORDINE_CASUALE, PROMEMORIA, SENZA_CATEGORIA, GIRI }

/** Riepilogo come dati: nessuna stringa localizzata, la UI sceglie i testi. */
data class RiepilogoConfigurazione(
    val numeroGiocatori: Int,
    val nomi: List<String>,
    val numeroImpostori: Int,
    val finoA: Boolean,
    val modalita: Modalita,
    val opzioni: List<OpzioneRiepilogo>,
    val giriIndizi: Int,
    val numeroCategorie: Int,
)

object Passi {
    private fun appartiene(passo: PassoConfigurazione, errore: ErroreConfigurazione): Boolean = when (passo) {
        PassoConfigurazione.GIOCATORI ->
            errore is ErroreConfigurazione.TroppoPochiGiocatori ||
                errore is ErroreConfigurazione.TroppiGiocatori ||
                errore is ErroreConfigurazione.NomeDuplicato ||
                errore is ErroreConfigurazione.NomeTroppoLungo
        PassoConfigurazione.MODALITA ->
            errore is ErroreConfigurazione.TroppoPochiImpostori || errore is ErroreConfigurazione.TroppiImpostori
        PassoConfigurazione.CATEGORIE ->
            errore is ErroreConfigurazione.NessunaCategoria || errore is ErroreConfigurazione.PoolVuoto
        PassoConfigurazione.OPZIONI, PassoConfigurazione.RIEPILOGO -> false
    }

    /** Primo errore di [Regole.valida] che appartiene al passo, o null se il passo e' valido. */
    fun errorePasso(
        passo: PassoConfigurazione,
        config: Configurazione,
        categorie: List<Categoria>,
    ): ErroreConfigurazione? = Regole.valida(config, categorie).firstOrNull { appartiene(passo, it) }

    /** Primo passo (GIOCATORI, MODALITA, CATEGORIE) con un errore; null se la configurazione e' valida. */
    fun primoPassoNonValido(config: Configurazione, categorie: List<Categoria>): PassoConfigurazione? =
        PassoConfigurazione.entries.firstOrNull { errorePasso(it, config, categorie) != null }

    /** Aggiunge un giocatore in fondo; invariata se si e' gia' al massimo. */
    fun aggiungiGiocatore(config: Configurazione): Configurazione {
        val n = config.numeroGiocatori
        if (n >= Regole.MAX_GIOCATORI) return config
        val nomi = List(n) { config.nomi.getOrNull(it) ?: "" } + ""
        return config.copy(numeroGiocatori = n + 1, nomi = nomi)
    }

    /** Rimuove il giocatore in posizione [indice]; invariata al minimo o con indice fuori intervallo. */
    fun rimuoviGiocatore(config: Configurazione, indice: Int): Configurazione {
        val n = config.numeroGiocatori
        if (n <= Regole.MIN_GIOCATORI || indice !in 0 until n) return config
        val nomi = List(n) { config.nomi.getOrNull(it) ?: "" }.toMutableList().apply { removeAt(indice) }
        return config.copy(
            numeroGiocatori = n - 1,
            nomi = nomi,
            numeroImpostori = minOf(config.numeroImpostori, Regole.maxImpostori(n - 1)),
        )
    }

    fun puoAggiungereGiocatore(config: Configurazione): Boolean = config.numeroGiocatori < Regole.MAX_GIOCATORI
    fun puoRimuovereGiocatore(config: Configurazione): Boolean = config.numeroGiocatori > Regole.MIN_GIOCATORI

    fun riepilogo(config: Configurazione, categorie: List<Categoria>): RiepilogoConfigurazione {
        val massimo = maxOf(1, Regole.maxImpostori(config.numeroGiocatori))
        val impostori = config.numeroImpostori.coerceIn(1, massimo)
        val giri = config.giriIndizi.coerceIn(1, Regole.MAX_GIRI)
        val opzioni = buildList {
            if (config.impostoreNonPrimo) add(OpzioneRiepilogo.NON_PARLA_PER_PRIMO)
            if (config.partitaTrappola) add(OpzioneRiepilogo.TRAPPOLA)
            if (config.ordineCasuale) add(OpzioneRiepilogo.ORDINE_CASUALE)
            if (config.promemoriaUltimaPossibilita) add(OpzioneRiepilogo.PROMEMORIA)
            if (!config.mostraCategoria && config.modalita == Modalita.SENZA_PAROLA) {
                add(OpzioneRiepilogo.SENZA_CATEGORIA)
            }
            if (giri > 1) add(OpzioneRiepilogo.GIRI)
        }
        val ids = categorie.map { it.id }.toSet()
        return RiepilogoConfigurazione(
            numeroGiocatori = config.numeroGiocatori,
            nomi = Regole.nomiEffettivi(config),
            numeroImpostori = impostori,
            finoA = config.impostoriSorpresa && impostori > 1,
            modalita = config.modalita,
            opzioni = opzioni,
            giriIndizi = giri,
            numeroCategorie = config.categorieSelezionate.count { it in ids },
        )
    }

    /** Badge "N attive" (CA-81): non conta mostraCategoria ne' impostoriSorpresa. */
    fun contaOpzioniAttive(config: Configurazione): Int =
        listOf(
            config.impostoreNonPrimo,
            config.partitaTrappola,
            config.ordineCasuale,
            config.promemoriaUltimaPossibilita,
            config.giriIndizi > 1,
        ).count { it }
}
