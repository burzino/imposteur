package it.imposteur.data

import it.imposteur.game.Categoria
import it.imposteur.game.Parola
import org.junit.Assert.*
import org.junit.Test

class ParserParoleTest {
    private fun ok(json: String): List<Categoria> {
        val r = ParserParole.parse(json)
        assertTrue("atteso Ok: $r", r is RisultatoCaricamento.Ok)
        return (r as RisultatoCaricamento.Ok).categorie
    }

    private fun errore(json: String) =
        assertTrue(ParserParole.parse(json) is RisultatoCaricamento.Errore)

    @Test fun `CA-17 formato valido`() {
        val c = ok("""{"versione":1,"categorie":[{"id":"animali","nome":"Animali","parole":[{"parola":"Cane","affine":"Lupo"}]}]}""")
        assertEquals(1, c.size)
        assertEquals("animali", c[0].id); assertEquals("Animali", c[0].nome)
        assertEquals(listOf(Parola("Cane", "Lupo")), c[0].parole)
    }

    @Test fun `CA-17 versione diversa da 1 rifiutata`() {
        errore("""{"versione":2,"categorie":[]}""")
        errore("""{"versione":0,"categorie":[]}""")
    }

    @Test fun `CA-17 json malformato rifiutato`() {
        errore("{non json")
        errore("")
    }

    @Test fun `CA-17 parola vuota rifiutata`() {
        errore("""{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[{"parola":"","affine":"x"}]}]}""")
    }

    @Test fun `CA-17 affine assente o vuoto tollerato come null`() {
        val c = ok("""{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[{"parola":"Uno"},{"parola":"Due","affine":""}]}]}""")
        assertEquals(listOf(Parola("Uno", null), Parola("Due", null)), c[0].parole)
    }

    @Test fun `CA-17 nessuna coppia con affine uguale alla parola`() {
        val c = ok("""{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[{"parola":"Uno","affine":"uno"},{"parola":"Due","affine":"Tre"}]}]}""")
        val ps = c[0].parole
        assertTrue(ps.none { it.affine != null && it.affine.equals(it.parola, ignoreCase = true) })
        assertTrue(Parola("Due", "Tre") in ps)
    }

    @Test fun `CA-17 categoria senza parole mantenuta`() {
        val c = ok("""{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[]}]}""")
        assertEquals(1, c.size); assertTrue(c[0].parole.isEmpty())
    }
}
