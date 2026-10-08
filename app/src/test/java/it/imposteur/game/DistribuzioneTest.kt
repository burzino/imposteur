package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test

class DistribuzioneTest {
    @Test fun `CA-18 stato iniziale e Passaggio 0`() {
        assertEquals(StatoDistribuzione.Passaggio(0), Distribuzione.iniziale())
    }

    @Test fun `CA-18 percorso completo con N giocatori`() {
        for (n in listOf(3, 4, 20)) {
            var s = Distribuzione.iniziale()
            for (k in 0 until n) {
                assertEquals(StatoDistribuzione.Passaggio(k), s)
                s = Distribuzione.avanza(s, n)
                assertEquals(StatoDistribuzione.Rivelazione(k), s)
                s = Distribuzione.avanza(s, n)
            }
            assertEquals(StatoDistribuzione.Gioco, s)
        }
    }

    @Test fun `CA-18 Gioco resta Gioco`() {
        assertEquals(StatoDistribuzione.Gioco, Distribuzione.avanza(StatoDistribuzione.Gioco, 4))
    }

    @Test fun `CA-18 indici mai decrescenti e nessuna doppia rivelazione`() {
        var s: StatoDistribuzione = Distribuzione.iniziale()
        val rivelati = mutableListOf<Int>()
        var ultimo = -1
        repeat(20) {
            val cur = s
            val i = when (cur) {
                is StatoDistribuzione.Passaggio -> cur.indice
                is StatoDistribuzione.Rivelazione -> cur.indice
                StatoDistribuzione.Gioco -> ultimo
            }
            assertTrue(i >= ultimo); ultimo = i
            if (cur is StatoDistribuzione.Rivelazione) rivelati += cur.indice
            s = Distribuzione.avanza(cur, 5)
        }
        assertEquals(listOf(0, 1, 2, 3, 4), rivelati)
    }

    @Test fun `CA-18 interrompi riporta a Passaggio dello stesso giocatore`() {
        assertEquals(StatoDistribuzione.Passaggio(2), Distribuzione.interrompiRivelazione(StatoDistribuzione.Rivelazione(2)))
    }

    @Test fun `CA-18 interrompi lascia invariati gli altri stati`() {
        assertEquals(StatoDistribuzione.Passaggio(1), Distribuzione.interrompiRivelazione(StatoDistribuzione.Passaggio(1)))
        assertEquals(StatoDistribuzione.Gioco, Distribuzione.interrompiRivelazione(StatoDistribuzione.Gioco))
    }

    @Test fun `CA-18 interrompi poi avanza non salta il giocatore`() {
        val s = Distribuzione.interrompiRivelazione(StatoDistribuzione.Rivelazione(1))
        assertEquals(StatoDistribuzione.Rivelazione(1), Distribuzione.avanza(s, 4))
    }
}
