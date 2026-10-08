package it.imposteur.data

import it.imposteur.game.Categoria
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FileParoleRealeTest {
    private val file = File("src/main/assets/parole.json")

    private fun carica(): List<Categoria> {
        assertTrue("file mancante: ${file.absolutePath}", file.exists())
        val r = ParserParole.parse(file.readText(Charsets.UTF_8))
        assertTrue("parse fallito: $r", r is RisultatoCaricamento.Ok)
        return (r as RisultatoCaricamento.Ok).categorie
    }

    @Test fun `CA-17 file reale JSON valido, categorie non vuote e id univoci`() {
        val c = carica()
        assertTrue(c.isNotEmpty())
        c.forEach { assertTrue("categoria vuota: ${it.id}", it.parole.isNotEmpty()) }
        assertEquals("id categoria univoci", c.size, c.map { it.id }.toSet().size)
    }

    @Test fun `CA-17 file reale senza duplicati e con affine diversa dalla parola`() {
        for (cat in carica()) {
            val chiavi = cat.parole.map { it.parola.trim().lowercase() }
            assertEquals("duplicati in ${cat.id}", chiavi.size, chiavi.toSet().size)
            cat.parole.forEach {
                assertTrue(it.parola.isNotBlank())
                assertFalse("affine == parola: ${it.parola}", it.affine != null && it.affine.trim().equals(it.parola.trim(), true))
            }
        }
    }

    @Test fun `CA-17 file reale il parser non scarta parole`() {
        val nel = Regex("\"parola\"\\s*:").findAll(file.readText(Charsets.UTF_8)).count()
        assertEquals("parole nel sorgente vs parsate", nel, carica().sumOf { it.parole.size })
    }
}
