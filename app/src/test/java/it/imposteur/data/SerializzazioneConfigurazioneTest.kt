package it.imposteur.data

import it.imposteur.game.*
import org.junit.Assert.*
import org.junit.Test

class SerializzazioneConfigurazioneTest {
    private val cats = listOf(
        Categoria("a", "A", listOf(Parola("x", "y"))),
        Categoria("b", "B", listOf(Parola("z", null))),
        Categoria("c", "C", listOf(Parola("w", "v"))),
    )

    private fun rt(c: Configurazione) =
        SerializzazioneConfigurazione.daStringa(SerializzazioneConfigurazione.aStringa(c), cats)

    @Test fun `CA-19 round-trip identico`() {
        val c = Configurazione(7, listOf("Anna", "", "Carlo"), 3, Modalita.PAROLA_AFFINE, false, setOf("a", "c"))
        assertEquals(c, rt(c))
    }

    @Test fun `CA-19 round-trip con tutte le categorie`() {
        val c = Configurazione(categorieSelezionate = setOf("a", "b", "c"))
        assertEquals(c, rt(c))
    }

    @Test fun `CA-19 null o malformata danno default valido`() {
        for (s in listOf(null, "", "{rotto", "[]")) {
            val c = SerializzazioneConfigurazione.daStringa(s, cats)
            assertEquals(4, c.numeroGiocatori)
            assertEquals(1, c.numeroImpostori)
            assertEquals(Modalita.SENZA_PAROLA, c.modalita)
            assertTrue(c.categorieSelezionate.isNotEmpty())
            assertTrue(c.categorieSelezionate.all { id -> cats.any { it.id == id } })
        }
    }

    @Test fun `CA-19 id sconosciuti scartati`() {
        val s = SerializzazioneConfigurazione.aStringa(Configurazione(categorieSelezionate = setOf("a", "zzz")))
        assertEquals(setOf("a"), SerializzazioneConfigurazione.daStringa(s, cats).categorieSelezionate)
    }

    @Test fun `CA-19 nessun id valido seleziona tutte`() {
        val s = SerializzazioneConfigurazione.aStringa(Configurazione(categorieSelezionate = setOf("zzz")))
        assertEquals(setOf("a", "b", "c"), SerializzazioneConfigurazione.daStringa(s, cats).categorieSelezionate)
    }

    @Test fun `CA-19 valori fuori limite corretti`() {
        val alto = rt(Configurazione(numeroGiocatori = 50, numeroImpostori = 40, categorieSelezionate = setOf("a")))
        assertEquals(20, alto.numeroGiocatori); assertEquals(9, alto.numeroImpostori)
        val basso = rt(Configurazione(numeroGiocatori = 1, numeroImpostori = 0, categorieSelezionate = setOf("a")))
        assertEquals(3, basso.numeroGiocatori); assertEquals(1, basso.numeroImpostori)
        val troppi = rt(Configurazione(numeroGiocatori = 5, numeroImpostori = 4, categorieSelezionate = setOf("a")))
        assertEquals(2, troppi.numeroImpostori)
    }

    @Test fun `CA-19 nomi in eccesso rispetto a N non superano N`() {
        val c = rt(Configurazione(numeroGiocatori = 3, nomi = listOf("a", "b", "c", "d", "e"), categorieSelezionate = setOf("a")))
        assertTrue(c.nomi.size <= c.numeroGiocatori)
    }
}
