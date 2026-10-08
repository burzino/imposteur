package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Verifica Partita.ordineDiParola() (contratto API, sezione "Ordine di parola (v1.5)"). */
class OrdineDiParolaTest {
    private val cats = categorieDemo()

    private fun base(): Partita {
        val r = GestorePartite(Random(1)).nuovaPartita(configBase(n = 3), cats)
        assertTrue("atteso Ok, ottenuto $r", r is RisultatoNuovaPartita.Ok)
        return (r as RisultatoNuovaPartita.Ok).partita
    }

    private fun partita(n: Int, primo: Int): Partita =
        base().copy(giocatori = List(n) { "G$it" }, impostori = setOf(0), primoGiocatore = primo)

    @Test fun `ORD-01 con primo 0 l'ordine e 0 fino a N-1`() {
        assertEquals(listOf(0, 1, 2, 3, 4), partita(5, 0).ordineDiParola())
    }

    @Test fun `ORD-02 con primo 2 su 4 giocatori l'ordine e 2 3 0 1`() {
        assertEquals(listOf(2, 3, 0, 1), partita(4, 2).ordineDiParola())
    }

    @Test fun `ORD-03 con primo N-1 l'ordine e N-1 0 1 eccetera`() {
        for (n in listOf(3, 6, 20)) {
            assertEquals(listOf(n - 1) + (0 until n - 1).toList(), partita(n, n - 1).ordineDiParola())
        }
    }

    @Test fun `ORD-04 per ogni N e ogni primo e una permutazione che parte dal primo`() {
        for (n in 3..20) for (primo in 0 until n) {
            val o = partita(n, primo).ordineDiParola()
            assertEquals("N=$n primo=$primo", (0 until n).toList(), o.sorted())
            assertEquals("N=$n primo=$primo", primo, o.first())
        }
    }

    @Test fun `ORD-05 in partite da GestorePartite il primo dell'ordine e primoGiocatore`() {
        for (seed in 0 until 200) {
            val n = 3 + seed % 18
            val r = GestorePartite(Random(seed)).nuovaPartita(configBase(n = n), cats)
            assertTrue(r is RisultatoNuovaPartita.Ok)
            val p = (r as RisultatoNuovaPartita.Ok).partita
            val o = p.ordineDiParola()
            assertEquals("seed=$seed", p.primoGiocatore, o.first())
            assertEquals("seed=$seed", (0 until n).toList(), o.sorted())
        }
    }
}
