package it.imposteur.game

/** I quattro passi della Configurazione; ordinale + 1 = numero del passo (1..4). */
enum class PassoConfigurazione { GIOCATORI, OPZIONI, CATEGORIE, RIEPILOGO }

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
                errore is ErroreConfigurazione.TroppoPochiImpostori ||
                errore is ErroreConfigurazione.TroppiImpostori ||
                errore is ErroreConfigurazione.NomeDuplicato ||
                errore is ErroreConfigurazione.NomeTroppoLungo
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

    /** Primo passo (GIOCATORI, OPZIONI, CATEGORIE) con un errore; null se la configurazione e' valida. */
    fun primoPassoNonValido(config: Configurazione, categorie: List<Categoria>): PassoConfigurazione? =
        PassoConfigurazione.entries.firstOrNull { errorePasso(it, config, categorie) != null }

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
