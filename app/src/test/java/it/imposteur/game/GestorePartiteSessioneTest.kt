package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GestorePartiteSessioneTest {
    private val cats = categorieDemo()
    private val tutte = cats.flatMap { c -> c.parole.map { ChiaveParola(c.id, it.parola.lowercase()) } }

    private fun voce(g: GestorePartite, c: Configurazione = configBase()): VoceParola {
        val r = g.nuovaPartita(c, cats)
        assertTrue("atteso Ok: $r", r is RisultatoNuovaPartita.Ok)
        return (r as RisultatoNuovaPartita.Ok).partita.voce
    }

    private fun chiave(v: VoceParola) = ChiaveParola(v.categoriaId, v.parola.trim().lowercase())

    @Test fun `CA-42 stato iniziale vuoto di default`() {
        val g = GestorePartite(Random(1))
        assertTrue(g.usate.isEmpty())
        assertNull(g.ultima)
    }

    @Test fun `CA-42 usate e ultima esposte dopo ogni estrazione`() {
        val g = GestorePartite(Random(3))
        val v = voce(g)
        assertEquals(setOf(chiave(v)), g.usate)
        assertEquals(chiave(v), g.ultima)
        val v2 = voce(g)
        assertEquals(2, g.usate.size)
        assertEquals(chiave(v2), g.ultima)
    }

    @Test fun `CA-42 usateIniziali escluse dall estrazione successiva`() {
        for (seed in 0 until 50) {
            val g = GestorePartite(Random(seed), tutte.drop(1).toSet())
            assertEquals(tutte.first(), chiave(voce(g)))
        }
    }

    @Test fun `CA-42 dopo salvataggio e ripristino non si ripetono parole fino a esaurimento`() {
        for (seed in 0 until 30) {
            val g1 = GestorePartite(Random(seed))
            val viste = mutableSetOf<ChiaveParola>()
            viste += chiave(voce(g1)); viste += chiave(voce(g1))
            val g2 = GestorePartite(Random(seed + 1000), g1.usate, g1.ultima)
            repeat(tutte.size - 2) { assertTrue("ripetuta", viste.add(chiave(voce(g2)))) }
            assertEquals(tutte.size, viste.size)
        }
    }

    @Test fun `CA-42 esaurimento del pool azzera le usate ed evita l ultima`() {
        for (seed in 0 until 50) {
            val ultima = tutte.first()
            val g = GestorePartite(Random(seed), tutte.toSet(), ultima)
            val v = chiave(voce(g))
            assertNotEquals(ultima, v)
            assertEquals(setOf(v), g.usate)
        }
    }

    @Test fun `CA-42 usate iniziali fuori dal pool non impediscono l estrazione`() {
        val g = GestorePartite(Random(5), setOf(ChiaveParola("fantasy", "elfo")))
        val v = voce(g)
        assertTrue(cats.any { it.id == v.categoriaId })
    }

    @Test fun `CA-42 usate e una copia di sola lettura`() {
        val g = GestorePartite(Random(2))
        val prima = g.usate
        voce(g)
        assertTrue(prima.isEmpty())
        assertEquals(1, g.usate.size)
    }

    @Test fun `CA-42 il set iniziale passato non viene alterato`() {
        val iniz = mutableSetOf(ChiaveParola("animali", "cane"))
        val g = GestorePartite(Random(2), iniz)
        voce(g)
        assertEquals(1, iniz.size)
    }
}
