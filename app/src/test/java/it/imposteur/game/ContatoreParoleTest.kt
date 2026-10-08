package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Contatore delle parole da giocare (contratto v1.2). Nome "CA-61" scelto dal committente. */
class ContatoreParoleTest {

    private val categorie = categorieDemo()
    private val poolA = Regole.pool(categorie, setOf("animali"), Modalita.SENZA_PAROLA)
    private val poolB = Regole.pool(categorie, setOf("cibo"), Modalita.SENZA_PAROLA)
    private val poolTutto = Regole.pool(categorie, setOf("animali", "cibo"), Modalita.SENZA_PAROLA)

    private fun gioca(g: GestorePartite, cat: String) {
        val r = g.nuovaPartita(configBase(cats = setOf(cat)), categorie)
        assertTrue("partita attesa Ok, ottenuto $r", r is RisultatoNuovaPartita.Ok)
    }

    private fun chiavi(pool: List<VoceParola>) =
        pool.map { ChiaveParola(it.categoriaId, it.parola.trim().lowercase()) }.toSet()

    @Test
    fun `CA-61 a pool nuovo rimanenti vale pool size`() {
        val g = GestorePartite(Random(1))
        assertEquals(poolTutto.size, g.rimanenti(poolTutto))
        assertEquals(poolA.size, g.rimanenti(poolA))
        assertEquals(poolB.size, g.rimanenti(poolB))
    }

    @Test
    fun `CA-61 rimanenti cala di 1 a ogni partita`() {
        val g = GestorePartite(Random(2))
        var atteso = poolA.size
        repeat(poolA.size) {
            gioca(g, "animali")
            atteso -= 1
            assertEquals(atteso, g.rimanenti(poolA))
        }
    }

    @Test
    fun `CA-61 a pool esaurito rimanenti vale 0`() {
        val g = GestorePartite(Random(3))
        repeat(poolA.size) { gioca(g, "animali") }
        assertEquals(0, g.rimanenti(poolA))
        assertEquals(poolB.size, g.rimanenti(poolB))
    }

    @Test
    fun `CA-61 azzeraUsate sul pool A non tocca le usate del pool B`() {
        val g = GestorePartite(Random(4))
        repeat(poolA.size) { gioca(g, "animali") }
        repeat(2) { gioca(g, "cibo") }
        val usateB = g.usate.filter { it in chiavi(poolB) }.toSet()
        assertEquals(2, usateB.size)

        g.azzeraUsate(poolA)

        assertEquals(usateB, g.usate)
        assertEquals(poolA.size, g.rimanenti(poolA))
        assertEquals(0, g.rimanenti(poolB))
    }

    @Test
    fun `CA-61 ultima diventa null solo se appartiene al pool azzerato`() {
        val g = GestorePartite(Random(5))
        gioca(g, "animali")
        gioca(g, "cibo") // ultima in B
        val ultimaB = g.ultima
        assertTrue(ultimaB != null && ultimaB in chiavi(poolB))

        g.azzeraUsate(poolA)
        assertEquals("azzerare A non tocca ultima in B", ultimaB, g.ultima)

        g.azzeraUsate(poolB)
        assertNull(g.ultima)
    }

    @Test
    fun `CA-61 azzeraUsate del pool contenente ultima la mette a null`() {
        val g = GestorePartite(Random(6))
        gioca(g, "cibo")
        gioca(g, "animali") // ultima in A
        g.azzeraUsate(poolA)
        assertNull(g.ultima)
    }

    @Test
    fun `CA-61 dopo azzeraUsate rimanenti torna a pool size`() {
        val g = GestorePartite(Random(7))
        gioca(g, "animali")
        gioca(g, "cibo")
        gioca(g, "animali")
        g.azzeraUsate(poolTutto)
        assertEquals(poolTutto.size, g.rimanenti(poolTutto))
        assertTrue(g.usate.isEmpty())
        assertNull(g.ultima)
    }

    @Test
    fun `CA-61 round-trip con usate e ultima esposte non cambia rimanenti`() {
        val g = GestorePartite(Random(8))
        gioca(g, "animali")
        gioca(g, "cibo")
        gioca(g, "animali")

        val copia = GestorePartite(Random(99), g.usate, g.ultima)
        assertEquals(g.usate, copia.usate)
        assertEquals(g.ultima, copia.ultima)
        assertEquals(g.rimanenti(poolTutto), copia.rimanenti(poolTutto))
        assertEquals(g.rimanenti(poolA), copia.rimanenti(poolA))

        g.azzeraUsate(poolA)
        val copia2 = GestorePartite(Random(98), g.usate, g.ultima)
        assertEquals(g.usate, copia2.usate)
        assertEquals(g.ultima, copia2.ultima)
        assertEquals(g.rimanenti(poolTutto), copia2.rimanenti(poolTutto))
    }
}
