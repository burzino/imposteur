package it.imposteur.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DurataPressioneTest {

    @Test
    fun `CA-106 costanti`() {
        assertEquals(0, DurataPressione.MIN_MS)
        assertEquals(1000, DurataPressione.MAX_MS)
        assertEquals(50, DurataPressione.PASSO_MS)
        assertEquals(150, DurataPressione.PREDEFINITA_MS)
    }

    @Test
    fun `CA-106 normalizza casi limite del contratto`() {
        val attesi = mapOf(
            0 to 0, 1000 to 1000, 150 to 150, 125 to 150, 124 to 100,
            25 to 50, 24 to 0, -30 to 0, 1049 to 1000, 5000 to 1000,
            Int.MIN_VALUE to 0, Int.MAX_VALUE to 1000,
        )
        for ((ingresso, atteso) in attesi) {
            assertEquals("normalizza($ingresso)", atteso, DurataPressione.normalizza(ingresso))
        }
    }

    @Test
    fun `CA-106 normalizza negativi e sopra 1000`() {
        assertEquals(0, DurataPressione.normalizza(-1))
        assertEquals(0, DurataPressione.normalizza(-5000))
        assertEquals(1000, DurataPressione.normalizza(1001))
        assertEquals(1000, DurataPressione.normalizza(100000))
    }

    @Test
    fun `CA-106 normalizza il risultato e sempre multiplo di 50 in 0 1000 e idempotente`() {
        val campioni = (-200..1300) + listOf(Int.MIN_VALUE, Int.MAX_VALUE)
        for (v in campioni) {
            val r = DurataPressione.normalizza(v)
            assertTrue("fuori intervallo: $v -> $r", r in 0..1000)
            assertEquals("non multiplo di 50: $v -> $r", 0, r % 50)
            assertEquals("non idempotente: $v", r, DurataPressione.normalizza(r))
        }
    }

    @Test
    fun `CA-106 normalizza valori gia validi restano invariati`() {
        for (v in 0..1000 step 50) assertEquals(v, DurataPressione.normalizza(v))
    }

    @Test
    fun `CA-106 daTesto null vuoto e non numerico danno predefinita`() {
        assertEquals(150, DurataPressione.daTesto(null))
        assertEquals(150, DurataPressione.daTesto(""))
        assertEquals(150, DurataPressione.daTesto("   "))
        assertEquals(150, DurataPressione.daTesto("abc"))
        assertEquals(150, DurataPressione.daTesto("12abc"))
    }

    @Test
    fun `CA-106 daTesto NaN e Infinity danno predefinita`() {
        assertEquals(150, DurataPressione.daTesto("NaN"))
        assertEquals(150, DurataPressione.daTesto("Infinity"))
        assertEquals(150, DurataPressione.daTesto("-Infinity"))
    }

    @Test
    fun `CA-106 daTesto numeri validi`() {
        assertEquals(200, DurataPressione.daTesto("200"))
        assertEquals(0, DurataPressione.daTesto("0"))
        assertEquals(1000, DurataPressione.daTesto("1000"))
        assertEquals(150, DurataPressione.daTesto("125"))
        assertEquals(100, DurataPressione.daTesto("124"))
    }

    @Test
    fun `CA-106 daTesto spazi ai lati ignorati`() {
        assertEquals(200, DurataPressione.daTesto(" 200 "))
        assertEquals(200, DurataPressione.daTesto("\t200\n"))
    }

    @Test
    fun `CA-106 daTesto decimali ed esponenziale`() {
        assertEquals(150, DurataPressione.daTesto("149.6"))
        assertEquals(1000, DurataPressione.daTesto("1e9"))
    }

    @Test
    fun `CA-106 daTesto negativi e fuori intervallo vengono normalizzati`() {
        assertEquals(0, DurataPressione.daTesto("-30"))
        assertEquals(1000, DurataPressione.daTesto("5000"))
    }

    @Test
    fun `CA-106 daTesto non lancia eccezioni su testi anomali`() {
        for (s in listOf("--5", "1.2.3", "0x10", ",", "9".repeat(400), "\u0000")) {
            val r = DurataPressione.daTesto(s)
            assertTrue("fuori intervallo per '$s'", r in 0..1000)
            assertEquals(0, r % 50)
        }
    }

    @Test
    fun `CA-107 soloTocco vero solo a zero`() {
        assertTrue(DurataPressione.soloTocco(0))
        assertFalse(DurataPressione.soloTocco(50))
        assertFalse(DurataPressione.soloTocco(150))
        assertFalse(DurataPressione.soloTocco(1000))
    }

    @Test
    fun `CA-108 la durata predefinita al primo avvio non e solo tocco`() {
        assertFalse(DurataPressione.soloTocco(DurataPressione.PREDEFINITA_MS))
    }
}
