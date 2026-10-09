package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Verifica Partita.giocatoreAlPasso(k) (contratto API v1.9, "Distribuzione nell'ordine di parola"). */
class GiocatoreAlPassoTest {
    private val cats = categorieDemo()

    private fun base(): Partita {
        val r = GestorePartite(Random(1)).nuovaPartita(configBase(n = 3), cats)
        assertTrue("atteso Ok, ottenuto $r", r is RisultatoNuovaPartita.Ok)
        return (r as RisultatoNuovaPartita.Ok).partita
    }

    private fun partita(n: Int, primo: Int, ordine: List<Int>? = null, impostori: Set<Int> = setOf(0)): Partita =
        base().copy(giocatori = List(n) { "G$it" }, impostori = impostori, primoGiocatore = primo, ordine = ordine)

    @Test fun `CA-119 con ordine 0 2 1 i passi 0 1 2 danno i giocatori 0 2 1`() {
        val p = partita(3, primo = 0, ordine = listOf(0, 2, 1))
        assertEquals(listOf(0, 2, 1), (0..2).map { p.giocatoreAlPasso(it) })
    }

    @Test fun `CA-119 senza ordine e primo 2 su 4 giocatori i passi danno 2 3 0 1`() {
        val p = partita(4, primo = 2)
        assertNull(p.ordine)
        assertEquals(listOf(2, 3, 0, 1), (0..3).map { p.giocatoreAlPasso(it) })
    }

    @Test fun `CA-121 senza ordine e primo 0 e identita come prima`() {
        val p = partita(5, primo = 0)
        assertEquals(listOf(0, 1, 2, 3, 4), (0..4).map { p.giocatoreAlPasso(it) })
    }

    @Test fun `CA-119 ordine casuale 1 0 2 su 3 giocatori`() {
        val p = partita(3, primo = 1, ordine = listOf(1, 0, 2))
        assertEquals(listOf(1, 0, 2), (0..2).map { p.giocatoreAlPasso(it) })
    }

    @Test fun `CA-119 il passo 0 e sempre primoGiocatore e i risultati sono una permutazione`() {
        for (n in 3..20) for (primo in 0 until n) {
            val rotazione = partita(n, primo)
            assertEquals("rotazione N=$n primo=$primo", primo, rotazione.giocatoreAlPasso(0))
            assertEquals((0 until n).toList(), (0 until n).map { rotazione.giocatoreAlPasso(it) }.sorted())
        }
        for (seed in 0 until 100) {
            val n = 3 + seed % 18
            val r = GestorePartite(Random(seed)).nuovaPartita(configBase(n = n).copy(ordineCasuale = true), cats)
            assertTrue(r is RisultatoNuovaPartita.Ok)
            val p = (r as RisultatoNuovaPartita.Ok).partita
            val passi = (0 until n).map { p.giocatoreAlPasso(it) }
            assertEquals("seed=$seed", (0 until n).toList(), passi.sorted())
            assertEquals("seed=$seed", p.primoGiocatore, passi.first())
        }
    }

    @Test fun `CA-119 k fuori da 0 fino a N-1 e IllegalArgumentException`() {
        for (p in listOf(partita(3, 0, listOf(0, 2, 1)), partita(4, 2), partita(20, 5))) {
            val n = p.giocatori.size
            for (k in listOf(-1, n, n + 5, Int.MIN_VALUE, Int.MAX_VALUE)) {
                assertThrows("N=$n k=$k", IllegalArgumentException::class.java) { p.giocatoreAlPasso(k) }
            }
        }
    }

    @Test fun `CA-119 con 20 giocatori coincide con ordineDiParola`() {
        val p = partita(20, primo = 13)
        for (k in 0 until 20) assertEquals(p.ordineDiParola()[k], p.giocatoreAlPasso(k))
        val ordine = (0 until 20).toList().shuffled(Random(7))
        val q = partita(20, primo = ordine[0], ordine = ordine)
        for (k in 0 until 20) assertEquals(q.ordineDiParola()[k], q.giocatoreAlPasso(k))
    }

    @Test fun `CA-120 con ordine 0 2 1 e impostore il giocatore 2 il passo 1 mostra il ruolo da impostore di G2`() {
        val p = partita(3, primo = 0, ordine = listOf(0, 2, 1), impostori = setOf(2))
            .copy(modalita = Modalita.SENZA_PAROLA, mostraCategoria = true)
        val g = p.giocatoreAlPasso(1)
        assertEquals(2, g)
        assertEquals("G2", p.giocatori[g])
        assertTrue(p.contenutoPer(g) is ContenutoRuolo.Impostore)
        // la posizione 1 della lista originale (G1) e un civile: non va confusa con il giocatore al passo 1
        assertTrue(p.contenutoPer(1) is ContenutoRuolo.ParolaSegreta)
    }

    @Test fun `CA-122 l'elenco in ordine di parola mappa gli indici sui nomi e il tocco usa l'indice giocatore`() {
        val p = partita(3, primo = 0, ordine = listOf(0, 2, 1), impostori = setOf(2))
        val elenco = p.ordineDiParola().map { it to p.giocatori[it] }
        assertEquals(listOf(0 to "G0", 2 to "G2", 1 to "G1"), elenco)
        // riga 1 dell'elenco = giocatore 2, che e l'impostore
        assertTrue(p.contenutoPer(elenco[1].first) is ContenutoRuolo.Impostore)
    }

    @Test fun `CA-121 la macchina a stati resta sulle posizioni e l'ultimo passo e N-1`() {
        val n = 4
        var s: StatoDistribuzione = Distribuzione.iniziale()
        assertEquals(StatoDistribuzione.Passaggio(0), s)
        s = Distribuzione.avanza(s, n)
        assertEquals(StatoDistribuzione.Rivelazione(0), s)
        s = Distribuzione.avanza(s, n)
        assertEquals(StatoDistribuzione.Passaggio(1), s)
        // Rivelazione k interrotta torna a Passaggio k (posizione, non giocatore)
        assertEquals(StatoDistribuzione.Passaggio(2), Distribuzione.interrompiRivelazione(StatoDistribuzione.Rivelazione(2)))
    }
}
