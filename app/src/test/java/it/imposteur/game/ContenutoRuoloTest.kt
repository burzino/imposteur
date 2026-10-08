package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test

class ContenutoRuoloTest {
    private val voce = VoceParola("animali", "Animali", "Cane", "Lupo")
    private fun partita(mod: Modalita, imp: Set<Int> = setOf(1), mostra: Boolean = true) =
        Partita(listOf("Anna", "Bruno", "Carla", "Dino"), imp, voce, mod, mostra, 0)

    @Test fun `CA-09 civile vede la parola`() {
        assertEquals(ContenutoRuolo.ParolaSegreta("Cane"), partita(Modalita.SENZA_PAROLA).contenutoPer(0))
    }

    @Test fun `CA-09 impostore con categoria`() {
        assertEquals(ContenutoRuolo.Impostore("Animali"), partita(Modalita.SENZA_PAROLA).contenutoPer(1))
    }

    @Test fun `CA-09 impostore senza categoria se disattiva`() {
        assertEquals(ContenutoRuolo.Impostore(null), partita(Modalita.SENZA_PAROLA, mostra = false).contenutoPer(1))
    }

    @Test fun `CA-09 impostore non contiene parola ne affine`() {
        val s = partita(Modalita.SENZA_PAROLA).contenutoPer(1).toString()
        assertFalse(s.contains("Cane")); assertFalse(s.contains("Lupo"))
    }

    @Test fun `CA-10 affine civile parola impostore affine stessa struttura`() {
        val p = partita(Modalita.PAROLA_AFFINE)
        val civ = p.contenutoPer(0); val imp = p.contenutoPer(1)
        assertEquals(ContenutoRuolo.ParolaSegreta("Cane"), civ)
        assertEquals(ContenutoRuolo.ParolaSegreta("Lupo"), imp)
        assertEquals(civ::class, imp::class)
    }

    @Test fun `CA-10 affine nessun contenuto e di tipo Impostore`() {
        val p = partita(Modalita.PAROLA_AFFINE, imp = setOf(0, 2))
        for (i in 0 until 4) assertTrue(p.contenutoPer(i) is ContenutoRuolo.ParolaSegreta)
    }

    private fun svela(mod: Modalita, imp: Set<Int>) = partita(mod, imp).testoSvelamento()

    @Test fun `CA-21 singolare con un impostore`() {
        val t = svela(Modalita.SENZA_PAROLA, setOf(1))
        assertTrue(t, t.contains("L'impostore era: Bruno"))
        assertFalse(t.contains("Gli impostori erano"))
    }

    @Test fun `CA-21 plurale con piu impostori nell ordine dei giocatori`() {
        val t = svela(Modalita.SENZA_PAROLA, setOf(2, 0))
        assertTrue(t, t.contains("Gli impostori erano: Anna, Carla"))
        assertFalse(t.contains("L'impostore era"))
    }

    @Test fun `CA-21 contiene parola e categoria`() {
        val t = svela(Modalita.SENZA_PAROLA, setOf(1))
        assertTrue(t, t.contains("La parola era: Cane"))
        assertTrue(t, t.contains("Categoria: Animali"))
    }

    @Test fun `CA-21 affine solo in modalita affine`() {
        assertFalse(svela(Modalita.SENZA_PAROLA, setOf(1)).contains("Lupo"))
        assertTrue(svela(Modalita.PAROLA_AFFINE, setOf(1)).contains("La parola affine era: Lupo"))
    }
}
