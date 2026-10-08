package it.imposteur.data

import org.junit.Assert.*
import org.junit.Test

/** Test di FormatoSegnalazioni / Segnalazione (contratto "Segnalazioni v1.3"). */
class FormatoSegnalazioniTest {

    private fun unica(s: Segnalazione): Segnalazione {
        val l = FormatoSegnalazioni.leggi(FormatoSegnalazioni.riga(s))
        assertEquals(1, l.size)
        return l[0]
    }

    private val coppia = Segnalazione(
        tipo = "coppia",
        istante = "2026-10-08T21:14:03",
        categoriaId = "animali",
        parola = "gatto",
        affine = "tigre",
        modalita = "CLASSICA",
        motivi = listOf(MotivoSegnalazione.TROPPO_SIMILI, MotivoSegnalazione.POCO_CONOSCIUTA),
        nota = "troppo facile",
    )

    private val app = Segnalazione(tipo = "app", istante = "2026-10-08T21:15:00")

    @Test
    fun `SEG-01 round-trip coppia con tutti i campi`() {
        assertEquals(coppia, unica(coppia))
    }

    @Test
    fun `SEG-02 round-trip app con soli campi minimi`() {
        val r = unica(app)
        assertEquals(app, r)
        assertNull(r.categoriaId)
        assertNull(r.parola)
        assertNull(r.affine)
        assertNull(r.modalita)
        assertTrue(r.motivi.isEmpty())
        assertEquals("", r.nota)
    }

    @Test
    fun `SEG-03 riga non contiene a capo anche se la nota ne contiene`() {
        val s = coppia.copy(nota = "prima\nseconda\r\nterza\rquarta")
        val riga = FormatoSegnalazioni.riga(s)
        assertFalse(riga.contains('\n'))
        assertFalse(riga.contains('\r'))
        assertEquals(1, FormatoSegnalazioni.leggi(riga).size)
    }

    @Test
    fun `SEG-04 leggi su piu righe mantiene l'ordine`() {
        val a = coppia
        val b = app
        val c = coppia.copy(istante = "2026-10-09T08:00:00", parola = "cane")
        val testo = listOf(a, b, c).joinToString("\n") { FormatoSegnalazioni.riga(it) } + "\n"
        assertEquals(listOf(a, b, c), FormatoSegnalazioni.leggi(testo))
    }

    @Test
    fun `SEG-05 leggi salta righe vuote malformate e con motivo sconosciuto`() {
        val buona1 = FormatoSegnalazioni.riga(coppia)
        val buona2 = FormatoSegnalazioni.riga(app)
        val motivoIgnoto = """{"tipo":"coppia","istante":"2026-10-08T21:14:03","motivi":["MOTIVO_INVENTATO"],"nota":""}"""
        val testo = listOf(
            "", buona1, "   ", "questo non e' json", "{\"tipo\":", motivoIgnoto, "[1,2,3]", buona2, ""
        ).joinToString("\n")
        val r = FormatoSegnalazioni.leggi(testo)
        assertEquals(listOf(coppia, app), r)
    }

    @Test
    fun `SEG-06 leggi di stringa vuota restituisce lista vuota`() {
        assertTrue(FormatoSegnalazioni.leggi("").isEmpty())
    }

    @Test
    fun `SEG-07 campi sconosciuti nel JSON vengono ignorati`() {
        val json = """{"tipo":"app","istante":"2026-10-08T21:15:00","campoNuovo":42,"altro":{"x":[1,2]},"nota":"ok"}"""
        val r = FormatoSegnalazioni.leggi(json)
        assertEquals(1, r.size)
        assertEquals(Segnalazione(tipo = "app", istante = "2026-10-08T21:15:00", nota = "ok"), r[0])
    }

    @Test
    fun `SEG-08 accenti e virgolette nella nota sopravvivono al round-trip`() {
        val nota = "perché \"così\" è più città, backslash \\ e 'apici' ñ 日本"
        assertEquals(nota, unica(coppia.copy(nota = nota)).nota)
    }

    @Test
    fun `SEG-09 nota oltre 500 caratteri viene troncata a 500`() {
        val r = unica(coppia.copy(nota = "a".repeat(800)))
        assertEquals(500, r.nota.length)
        assertEquals("a".repeat(500), r.nota)
    }

    @Test
    fun `SEG-10 nota di esattamente 500 caratteri resta intatta`() {
        val nota = "b".repeat(500)
        assertEquals(nota, unica(coppia.copy(nota = nota)).nota)
    }

    @Test
    fun `SEG-11 nota viene sottoposta a trim`() {
        assertEquals("ciao mondo", unica(coppia.copy(nota = "   ciao mondo \t ")).nota)
    }

    @Test
    fun `SEG-12 nota di soli spazi diventa vuota`() {
        assertEquals("", unica(coppia.copy(nota = "     ")).nota)
    }

    // ---- v1.4: coppia proposta ----

    @Test
    fun `SEG-13 round-trip con propostaParola e propostaAffine`() {
        val s = coppia.copy(propostaParola = "leone", propostaAffine = "puma")
        val r = unica(s)
        assertEquals(s, r)
        assertEquals("leone", r.propostaParola)
        assertEquals("puma", r.propostaAffine)
    }

    @Test
    fun `SEG-14 riga salvata prima della v1_4 si legge con proposte null`() {
        val vecchia = """{"tipo":"coppia","istante":"2026-10-08T21:14:03","categoriaId":"animali","parola":"gatto","affine":"tigre","modalita":"CLASSICA","motivi":["TROPPO_SIMILI"],"nota":"vecchia"}"""
        val l = FormatoSegnalazioni.leggi(vecchia)
        assertEquals(1, l.size)
        assertNull(l[0].propostaParola)
        assertNull(l[0].propostaAffine)
        assertEquals("gatto", l[0].parola)
        assertEquals("vecchia", l[0].nota)
    }

    @Test
    fun `SEG-15 normalizza applica trim alle proposte`() {
        val n = FormatoSegnalazioni.normalizza(coppia.copy(propostaParola = "  leone ", propostaAffine = "\tpuma  "))
        assertEquals("leone", n.propostaParola)
        assertEquals("puma", n.propostaAffine)
    }

    @Test
    fun `SEG-16 normalizza trasforma vuoto e soli spazi in null`() {
        val n = FormatoSegnalazioni.normalizza(coppia.copy(propostaParola = "", propostaAffine = "    "))
        assertNull(n.propostaParola)
        assertNull(n.propostaAffine)
    }

    @Test
    fun `SEG-17 normalizza tronca le proposte a 40 caratteri`() {
        val n = FormatoSegnalazioni.normalizza(
            coppia.copy(propostaParola = "a".repeat(60), propostaAffine = "b".repeat(40))
        )
        assertEquals("a".repeat(40), n.propostaParola)
        assertEquals("b".repeat(40), n.propostaAffine)
    }

    @Test
    fun `SEG-18 riga normalizza anche le proposte`() {
        val r = unica(coppia.copy(propostaParola = "  x".padEnd(80, 'x'), propostaAffine = " "))
        assertEquals(40, r.propostaParola!!.length)
        assertNull(r.propostaAffine)
    }

    @Test
    fun `SEG-19 propostaValida false con un solo campo`() {
        assertFalse(FormatoSegnalazioni.propostaValida(coppia.copy(propostaParola = "leone")))
        assertFalse(FormatoSegnalazioni.propostaValida(coppia.copy(propostaAffine = "puma")))
        assertFalse(FormatoSegnalazioni.propostaValida(coppia))
    }

    @Test
    fun `SEG-20 propostaValida false con parole uguali a meno di maiuscole e spazi`() {
        assertFalse(FormatoSegnalazioni.propostaValida(coppia.copy(propostaParola = "Pane", propostaAffine = " pane ")))
    }

    @Test
    fun `SEG-21 propostaValida true con due parole diverse`() {
        assertTrue(FormatoSegnalazioni.propostaValida(coppia.copy(propostaParola = "Pane", propostaAffine = "Pasta")))
    }
}
