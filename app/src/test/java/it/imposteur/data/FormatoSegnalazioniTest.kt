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
}
