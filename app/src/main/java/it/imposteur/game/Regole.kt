package it.imposteur.game

object Regole {
    const val MIN_GIOCATORI = 3
    const val MAX_GIOCATORI = 20
    const val MAX_LUNGHEZZA_NOME = 20

    fun maxImpostori(numeroGiocatori: Int): Int = maxOf(0, (numeroGiocatori - 1) / 2)

    fun nomiEffettivi(config: Configurazione): List<String> =
        List(maxOf(0, config.numeroGiocatori)) { i ->
            val n = config.nomi.getOrNull(i)?.trim().orEmpty()
            if (n.isEmpty()) "Giocatore ${i + 1}" else n
        }

    fun valida(config: Configurazione, categorie: List<Categoria>): List<ErroreConfigurazione> {
        val errori = mutableListOf<ErroreConfigurazione>()
        val n = config.numeroGiocatori
        val nOk = n in MIN_GIOCATORI..MAX_GIOCATORI
        if (n < MIN_GIOCATORI) errori += ErroreConfigurazione.TroppoPochiGiocatori
        if (n > MAX_GIOCATORI) errori += ErroreConfigurazione.TroppiGiocatori
        if (nOk) {
            if (config.numeroImpostori < 1) errori += ErroreConfigurazione.TroppoPochiImpostori
            if (config.numeroImpostori > maxImpostori(n)) errori += ErroreConfigurazione.TroppiImpostori
        }
        val ids = categorie.map { it.id }.toSet()
        if (config.categorieSelezionate.none { it in ids }) {
            errori += ErroreConfigurazione.NessunaCategoria
        } else if (pool(categorie, config.categorieSelezionate, config.modalita).isEmpty()) {
            errori += ErroreConfigurazione.PoolVuoto
        }
        val nomi = nomiEffettivi(config)
        nomi.forEachIndexed { i, nome ->
            if (nome.length > MAX_LUNGHEZZA_NOME) errori += ErroreConfigurazione.NomeTroppoLungo(i)
        }
        nomi.indices.groupBy { nomi[it].lowercase() }.values
            .filter { it.size > 1 }
            .sortedBy { it.first() }
            .forEach { errori += ErroreConfigurazione.NomeDuplicato(it) }
        return errori
    }

    fun conNumeroGiocatori(config: Configurazione, n: Int): Configurazione = config.copy(
        numeroGiocatori = n,
        nomi = config.nomi.take(n),
        numeroImpostori = minOf(config.numeroImpostori, maxOf(1, maxImpostori(n))),
    )

    fun pool(categorie: List<Categoria>, selezionate: Set<String>, modalita: Modalita): List<VoceParola> =
        categorie.filter { it.id in selezionate }.flatMap { c ->
            c.parole.mapNotNull { p ->
                val affine = p.affine?.takeIf { it.isNotBlank() }
                if (modalita == Modalita.PAROLA_AFFINE &&
                    (affine == null || affine.trim().equals(p.parola.trim(), ignoreCase = true))
                ) null
                else VoceParola(c.id, c.nome, p.parola, affine)
            }
        }
}
