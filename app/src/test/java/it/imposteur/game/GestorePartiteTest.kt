package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GestorePartiteTest {
    private val cats = categorieDemo()

    private fun ok(r: RisultatoNuovaPartita): Partita {
        assertTrue("atteso Ok, ottenuto $r", r is RisultatoNuovaPartita.Ok)
        return (r as RisultatoNuovaPartita.Ok).partita
    }

    @Test fun `CA-07 esattamente K impostori distinti in range`() {
        for (seed in 0 until 100) for ((n, k) in listOf(3 to 1, 5 to 2, 7 to 3, 20 to 9)) {
            val p = ok(GestorePartite(Random(seed)).nuovaPartita(configBase(n = n, k = k), cats))
            assertEquals(k, p.impostori.size)
            assertTrue(p.impostori.all { it in 0 until n })
            assertEquals(n, p.giocatori.size)
            assertEquals(n - k, (0 until n).count { it !in p.impostori })
        }
    }

    @Test fun `CA-08 riproducibile con stesso seed`() {
        val c = configBase(n = 8, k = 3)
        val a = ok(GestorePartite(Random(42)).nuovaPartita(c, cats))
        val b = ok(GestorePartite(Random(42)).nuovaPartita(c, cats))
        assertEquals(a, b)
    }

    @Test fun `CA-08 ogni giocatore puo essere impostore`() {
        val visti = mutableSetOf<Int>()
        for (seed in 0 until 500) visti += ok(GestorePartite(Random(seed)).nuovaPartita(configBase(n = 6, k = 2), cats)).impostori
        assertEquals((0 until 6).toSet(), visti)
    }

    @Test fun `CA-12 parola appartiene alle categorie selezionate`() {
        val cs = listOf(cat("a", "Uno" to "X1", "Due" to "X2"), cat("b", "Tre" to "X3"))
        for (seed in 0 until 100) {
            val p = ok(GestorePartite(Random(seed)).nuovaPartita(configBase(cats = setOf("a")), cs))
            assertEquals("a", p.voce.categoriaId)
            assertTrue(p.voce.parola in setOf("Uno", "Due"))
        }
    }

    @Test fun `CA-12 modalita affine voce con affine valido`() {
        val cs = listOf(cat("a", "Uno" to null, "Due" to "Tre", "Quattro" to "quattro"))
        val p = ok(GestorePartite(Random(1)).nuovaPartita(configBase(mod = Modalita.PAROLA_AFFINE, cats = setOf("a")), cs))
        assertEquals("Due", p.voce.parola); assertEquals("Tre", p.voce.affine)
    }

    @Test fun `CA-13 prime M partite usano M parole diverse`() {
        for (seed in 0 until 30) {
            val g = GestorePartite(Random(seed))
            val usate = (1..5).map { ok(g.nuovaPartita(configBase(), cats)).voce.parola }
            assertEquals("seed $seed: $usate", 5, usate.toSet().size)
        }
    }

    @Test fun `CA-14 esaurito il pool non ripete l ultima parola`() {
        for (seed in 0 until 50) {
            val g = GestorePartite(Random(seed))
            val parole = (1..15).map { ok(g.nuovaPartita(configBase(), cats)).voce.parola }
            for (i in 1 until parole.size) assertNotEquals("seed $seed: $parole", parole[i - 1], parole[i])
        }
    }

    @Test fun `CA-14 pool di una sola parola ripete senza errori`() {
        val cs = listOf(cat("a", "Solo" to "Uno"))
        val g = GestorePartite(Random(3))
        repeat(4) { assertEquals("Solo", ok(g.nuovaPartita(configBase(cats = setOf("a")), cs)).voce.parola) }
    }

    @Test fun `CA-14 le usate non si azzerano cambiando categorie`() {
        val g = GestorePartite(Random(9))
        val cs = listOf(cat("a", "A1" to "x", "A2" to "y"), cat("b", "B1" to "z", "B2" to "w"))
        val prime = (1..2).map { ok(g.nuovaPartita(configBase(cats = setOf("a")), cs)).voce.parola }
        assertEquals(2, prime.toSet().size)
        val poi = (1..2).map { ok(g.nuovaPartita(configBase(cats = setOf("a", "b")), cs)).voce.parola }
        assertTrue(poi.all { it.startsWith("B") })
    }

    @Test fun `CA-15 primo giocatore in range e tutti compaiono`() {
        val visti = mutableSetOf<Int>()
        for (seed in 0 until 300) {
            val p = ok(GestorePartite(Random(seed)).nuovaPartita(configBase(n = 7, k = 3), cats))
            assertTrue(p.primoGiocatore in 0 until 7)
            visti += p.primoGiocatore
        }
        assertEquals((0 until 7).toSet(), visti)
    }

    @Test fun `CA-16 pool vuoto restituisce Errore senza eccezioni`() {
        val cs = listOf(cat("a", "Uno" to null))
        val r = GestorePartite(Random(0)).nuovaPartita(configBase(mod = Modalita.PAROLA_AFFINE, cats = setOf("a")), cs)
        assertTrue(r is RisultatoNuovaPartita.Errore)
        assertTrue(ErroreConfigurazione.PoolVuoto in (r as RisultatoNuovaPartita.Errore).errori)
    }

    @Test fun `configurazione non valida restituisce Errore`() {
        val r = GestorePartite(Random(0)).nuovaPartita(configBase(n = 5, k = 4), cats)
        assertTrue(r is RisultatoNuovaPartita.Errore)
        assertTrue(ErroreConfigurazione.TroppiImpostori in (r as RisultatoNuovaPartita.Errore).errori)
    }

    @Test fun `CA-20 nuova partita mantiene giocatori e impostazioni e rinnova il resto`() {
        val c = configBase(n = 6, k = 2, nomi = listOf("Anna", "Bruno", "Carla", "Dino", "Elio", "Fabio"), mostra = false)
        val g = GestorePartite(Random(5))
        val partite = (1..5).map { ok(g.nuovaPartita(c, cats)) }
        for (p in partite) {
            assertEquals(listOf("Anna", "Bruno", "Carla", "Dino", "Elio", "Fabio"), p.giocatori)
            assertEquals(2, p.impostori.size)
            assertFalse(p.mostraCategoria)
            assertEquals(Modalita.SENZA_PAROLA, p.modalita)
        }
        assertEquals(5, partite.map { it.voce.parola }.toSet().size)
        assertTrue(partite.map { it.impostori }.toSet().size > 1)
        assertTrue(partite.map { it.primoGiocatore }.toSet().size > 1)
    }
}
