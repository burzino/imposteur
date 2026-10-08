package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test

class RegoleTest {
    private val cats = categorieDemo()

    @Test fun `CA-01 maxImpostori`() {
        val atteso = mapOf(3 to 1, 4 to 1, 5 to 2, 6 to 2, 7 to 3, 20 to 9)
        for ((n, k) in atteso) assertEquals("N=$n", k, Regole.maxImpostori(n))
    }

    @Test fun `CA-02 troppo pochi e troppi giocatori`() {
        assertTrue(ErroreConfigurazione.TroppoPochiGiocatori in Regole.valida(configBase(n = 2), cats))
        assertTrue(ErroreConfigurazione.TroppiGiocatori in Regole.valida(configBase(n = 21), cats))
        assertTrue(Regole.valida(configBase(n = 3), cats).isEmpty())
        assertTrue(Regole.valida(configBase(n = 20, k = 9), cats).isEmpty())
    }

    @Test fun `CA-02 impostori fuori limite con errori distinti`() {
        assertTrue(ErroreConfigurazione.TroppoPochiImpostori in Regole.valida(configBase(k = 0), cats))
        val tanti = Regole.valida(configBase(n = 5, k = 3), cats)
        assertTrue(ErroreConfigurazione.TroppiImpostori in tanti)
        assertFalse(ErroreConfigurazione.TroppoPochiImpostori in tanti)
        assertTrue(Regole.valida(configBase(n = 5, k = 2), cats).isEmpty())
        assertNotEquals(ErroreConfigurazione.TroppoPochiGiocatori, ErroreConfigurazione.TroppiGiocatori)
    }

    @Test fun `CA-03 nessuna categoria`() {
        assertTrue(ErroreConfigurazione.NessunaCategoria in Regole.valida(configBase(cats = emptySet()), cats))
    }

    @Test fun `CA-04 nomi duplicati case-insensitive dopo trim`() {
        val c = configBase(n = 4, nomi = listOf("Anna", " anna ", "Bruno", "Carla"))
        val e = Regole.valida(c, cats).filterIsInstance<ErroreConfigurazione.NomeDuplicato>()
        assertEquals(1, e.size)
        assertEquals(setOf(0, 1), e[0].indici.toSet())
    }

    @Test fun `CA-04 duplicato con nome di default`() {
        val c = configBase(n = 4, nomi = listOf("Giocatore 2", "", "Bruno", "Carla"))
        val e = Regole.valida(c, cats).filterIsInstance<ErroreConfigurazione.NomeDuplicato>()
        assertEquals(1, e.size)
        assertEquals(setOf(0, 1), e[0].indici.toSet())
    }

    @Test fun `CA-04 nessun duplicato con nomi distinti`() {
        val c = configBase(n = 3, nomi = listOf("A", "B", ""))
        assertTrue(Regole.valida(c, cats).none { it is ErroreConfigurazione.NomeDuplicato })
    }

    @Test fun `CA-05 nomi effettivi default e trim`() {
        val c = configBase(n = 4, nomi = listOf("", "   ", "  Luca  "))
        assertEquals(listOf("Giocatore 1", "Giocatore 2", "Luca", "Giocatore 4"), Regole.nomiEffettivi(c))
    }

    @Test fun `CA-05 nomi effettivi senza nomi inseriti ha size N`() {
        assertEquals(6, Regole.nomiEffettivi(configBase(n = 6)).size)
    }

    @Test fun `CA-05 nome oltre 20 caratteri rifiutato`() {
        val lungo = "x".repeat(21)
        val e = Regole.valida(configBase(n = 3, nomi = listOf("A", lungo, "C")), cats)
        assertTrue(ErroreConfigurazione.NomeTroppoLungo(1) in e)
        val ok = Regole.valida(configBase(n = 3, nomi = listOf("A", "x".repeat(20), "C")), cats)
        assertTrue(ok.none { it is ErroreConfigurazione.NomeTroppoLungo })
    }

    @Test fun `CA-05 la lunghezza si valuta dopo trim`() {
        val nome = "  " + "x".repeat(20) + "  "
        val e = Regole.valida(configBase(n = 3, nomi = listOf(nome, "B", "C")), cats)
        assertTrue(e.none { it is ErroreConfigurazione.NomeTroppoLungo })
    }

    @Test fun `CA-06 riducendo N gli impostori scendono al massimo`() {
        val c = Regole.conNumeroGiocatori(configBase(n = 9, k = 4), 5)
        assertEquals(5, c.numeroGiocatori)
        assertEquals(2, c.numeroImpostori)
        assertEquals(1, Regole.conNumeroGiocatori(configBase(n = 9, k = 4), 3).numeroImpostori)
    }

    @Test fun `CA-06 aumentando N gli impostori restano`() {
        val c = Regole.conNumeroGiocatori(configBase(n = 5, k = 2), 10)
        assertEquals(10, c.numeroGiocatori)
        assertEquals(2, c.numeroImpostori)
    }

    @Test fun `CA-11 pool in modalita affine esclude parole senza affine valido`() {
        val cs = listOf(cat("a", "Uno" to "Due", "Tre" to null, "Quattro" to "", "Cinque" to "cinque", "Sei" to "Sette"))
        val pool = Regole.pool(cs, setOf("a"), Modalita.PAROLA_AFFINE)
        assertEquals(setOf("Uno", "Sei"), pool.map { it.parola }.toSet())
        assertTrue(pool.all { !it.affine.isNullOrEmpty() })
    }

    @Test fun `CA-11 pool senza parola include tutte e solo le categorie selezionate`() {
        val cs = listOf(cat("a", "Uno" to null, "Due" to "Tre"), cat("b", "Altro" to "X"))
        val pool = Regole.pool(cs, setOf("a"), Modalita.SENZA_PAROLA)
        assertEquals(setOf("Uno", "Due"), pool.map { it.parola }.toSet())
        assertTrue(pool.all { it.categoriaId == "a" && it.categoriaNome == "A" })
    }

    @Test fun `CA-16 pool vuoto segnalato dalla validazione`() {
        val cs = listOf(cat("a", "Uno" to null), cat("b", "Due" to "Tre"))
        val e = Regole.valida(configBase(mod = Modalita.PAROLA_AFFINE, cats = setOf("a")), cs)
        assertTrue(ErroreConfigurazione.PoolVuoto in e)
        assertTrue(ErroreConfigurazione.PoolVuoto !in Regole.valida(configBase(mod = Modalita.SENZA_PAROLA, cats = setOf("a")), cs))
    }

    @Test fun `costanti delle regole`() {
        assertEquals(3, Regole.MIN_GIOCATORI); assertEquals(20, Regole.MAX_GIOCATORI)
        assertEquals(20, Regole.MAX_LUNGHEZZA_NOME)
    }
}
