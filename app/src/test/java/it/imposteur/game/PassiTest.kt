package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test

class PassiTest {
    private val cats = categorieDemo()
    private val catSenzaAffini = listOf(cat("solo", "Alfa" to null, "Beta" to null))

    private val erroriGiocatori = { e: ErroreConfigurazione ->
        e is ErroreConfigurazione.TroppoPochiGiocatori || e is ErroreConfigurazione.TroppiGiocatori ||
            e is ErroreConfigurazione.TroppoPochiImpostori || e is ErroreConfigurazione.TroppiImpostori ||
            e is ErroreConfigurazione.NomeDuplicato || e is ErroreConfigurazione.NomeTroppoLungo
    }

    // ---- enum ----

    @Test fun `CA-99 ordinale piu uno e il numero del passo`() {
        assertEquals(
            listOf(
                PassoConfigurazione.GIOCATORI, PassoConfigurazione.OPZIONI,
                PassoConfigurazione.CATEGORIE, PassoConfigurazione.RIEPILOGO,
            ),
            PassoConfigurazione.entries.toList(),
        )
        assertEquals(listOf(1, 2, 3, 4), PassoConfigurazione.entries.map { it.ordinal + 1 })
    }

    // ---- errorePasso ----

    @Test fun `CA-25 CA-101 nome duplicato blocca GIOCATORI e non CATEGORIE`() {
        val c = configBase(n = 4, nomi = listOf("Anna", " anna ", "Bruno", "Carla"))
        val e = Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats)
        assertTrue(e is ErroreConfigurazione.NomeDuplicato)
        assertNull(Passi.errorePasso(PassoConfigurazione.CATEGORIE, c, cats))
    }

    @Test fun `CA-25 nome duplicato anche con Giocatore n di default`() {
        val c = configBase(n = 4, nomi = listOf("Giocatore 2", "", "Bruno", "Carla"))
        assertTrue(Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats) is ErroreConfigurazione.NomeDuplicato)
        assertNull(Passi.errorePasso(PassoConfigurazione.CATEGORIE, c, cats))
    }

    @Test fun `CA-25 CA-101 categorie vuote danno NessunaCategoria in CATEGORIE e null in GIOCATORI`() {
        val c = configBase(cats = emptySet())
        assertEquals(ErroreConfigurazione.NessunaCategoria, Passi.errorePasso(PassoConfigurazione.CATEGORIE, c, cats))
        assertNull(Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats))
    }

    @Test fun `CA-101 selezione non vuota ma pool vuoto in PAROLA_AFFINE senza affini`() {
        val c = configBase(mod = Modalita.PAROLA_AFFINE, cats = setOf("solo"))
        assertEquals(ErroreConfigurazione.PoolVuoto, Passi.errorePasso(PassoConfigurazione.CATEGORIE, c, catSenzaAffini))
        assertNull(Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, catSenzaAffini))
    }

    @Test fun `CA-101 OPZIONI non blocca mai nemmeno con trappola e sorpresa`() {
        val combinazioni = listOf(
            configBase(),
            configBase(cats = emptySet()),
            configBase(n = 2, k = 0),
            configBase().copy(partitaTrappola = true, impostoriSorpresa = true),
            configBase(n = 5, k = 2).copy(
                partitaTrappola = true, impostoriSorpresa = true, impostoreNonPrimo = true,
                ordineCasuale = true, promemoriaUltimaPossibilita = true, giriIndizi = 3,
            ),
        )
        for (c in combinazioni) assertNull(Passi.errorePasso(PassoConfigurazione.OPZIONI, c, cats))
    }

    @Test fun `CA-101 RIEPILOGO e sempre null`() {
        val c = configBase(n = 2, k = 0, cats = emptySet(), nomi = listOf("A", "a"))
        assertNull(Passi.errorePasso(PassoConfigurazione.RIEPILOGO, c, cats))
        assertNull(Passi.errorePasso(PassoConfigurazione.RIEPILOGO, configBase(), cats))
    }

    @Test fun `CA-25 piu errori nello stesso passo restituisce il primo di Regole valida`() {
        val c = configBase(n = 2, k = 0, nomi = listOf("A", "a"))
        val attesoG = Regole.valida(c, cats).first(erroriGiocatori)
        assertEquals(attesoG, Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats))

        val c2 = configBase(cats = emptySet())
        val attesoC = Regole.valida(c2, cats).first {
            it is ErroreConfigurazione.NessunaCategoria || it is ErroreConfigurazione.PoolVuoto
        }
        assertEquals(attesoC, Passi.errorePasso(PassoConfigurazione.CATEGORIE, c2, cats))
    }

    @Test fun `CA-25 tipi non del passo non compaiono mai`() {
        val tutte = cats + catSenzaAffini
        val configs = listOf(
            configBase(n = 2, k = 0, cats = emptySet()),
            configBase(n = 21, k = 1, cats = emptySet(), nomi = listOf("A", "a")),
            configBase(mod = Modalita.PAROLA_AFFINE, cats = setOf("solo"), nomi = listOf("A", "a")),
        )
        for (c in configs) {
            val g = Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, tutte)
            if (g != null) assertTrue("GIOCATORI: $g", erroriGiocatori(g))
            val k = Passi.errorePasso(PassoConfigurazione.CATEGORIE, c, tutte)
            if (k != null) assertTrue(
                "CATEGORIE: $k",
                k is ErroreConfigurazione.NessunaCategoria || k is ErroreConfigurazione.PoolVuoto,
            )
        }
        // NessunaCategoria mai in GIOCATORI
        val e = Passi.errorePasso(PassoConfigurazione.GIOCATORI, configBase(n = 2, cats = emptySet()), cats)
        assertEquals(ErroreConfigurazione.TroppoPochiGiocatori, e)
    }

    // ---- primoPassoNonValido ----

    @Test fun `CA-102 CA-101 nome duplicato e categorie vuote danno GIOCATORI`() {
        val c = configBase(n = 4, cats = emptySet(), nomi = listOf("Anna", "anna", "Bruno", "Carla"))
        assertEquals(PassoConfigurazione.GIOCATORI, Passi.primoPassoNonValido(c, cats))
    }

    @Test fun `CA-102 solo categorie vuote danno CATEGORIE`() {
        assertEquals(PassoConfigurazione.CATEGORIE, Passi.primoPassoNonValido(configBase(cats = emptySet()), cats))
    }

    @Test fun `CA-25 configurazione valida danno null come Regole valida vuota`() {
        val c = configBase()
        assertTrue(Regole.valida(c, cats).isEmpty())
        assertNull(Passi.primoPassoNonValido(c, cats))
    }

    @Test fun `CA-102 non restituisce mai OPZIONI ne RIEPILOGO`() {
        val tutte = cats + catSenzaAffini
        val configs = listOf(
            configBase(), configBase(n = 2), configBase(cats = emptySet()),
            configBase(n = 4, nomi = listOf("A", "a")),
            configBase(mod = Modalita.PAROLA_AFFINE, cats = setOf("solo")),
            configBase().copy(partitaTrappola = true, impostoriSorpresa = true),
        )
        for (c in configs) {
            val p = Passi.primoPassoNonValido(c, tutte)
            assertTrue("$p", p == null || p == PassoConfigurazione.GIOCATORI || p == PassoConfigurazione.CATEGORIE)
        }
    }

    // ---- riepilogo ----

    @Test fun `CA-104 sorpresa attiva con massimo 1 non da finoA`() {
        for (n in listOf(3, 4)) {
            val r = Passi.riepilogo(configBase(n = n, k = 1).copy(impostoriSorpresa = true), cats)
            assertFalse("N=$n", r.finoA)
            assertEquals(1, r.numeroImpostori)
        }
    }

    @Test fun `CA-92 sorpresa attiva con 5 giocatori e 2 impostori da finoA`() {
        val r = Passi.riepilogo(configBase(n = 5, k = 2).copy(impostoriSorpresa = true), cats)
        assertTrue(r.finoA)
        assertEquals(2, r.numeroImpostori)
        assertEquals(5, r.numeroGiocatori)
    }

    @Test fun `CA-92 senza sorpresa finoA e falso e il numero e quello configurato`() {
        val r = Passi.riepilogo(configBase(n = 5, k = 2), cats)
        assertFalse(r.finoA)
        assertEquals(2, r.numeroImpostori)
        assertFalse(Passi.riepilogo(configBase(n = 5, k = 1).copy(impostoriSorpresa = true), cats).finoA)
    }

    @Test fun `CA-92 numero impostori limitato a 1 e al massimo`() {
        assertEquals(2, Passi.riepilogo(configBase(n = 5, k = 9), cats).numeroImpostori)
        assertEquals(1, Passi.riepilogo(configBase(n = 5, k = 0), cats).numeroImpostori)
    }

    @Test fun `CA-103 nomi vuoti diventano Giocatore n`() {
        val c = configBase(n = 4, nomi = listOf("", "  Luca "))
        val r = Passi.riepilogo(c, cats)
        assertEquals(listOf("Giocatore 1", "Luca", "Giocatore 3", "Giocatore 4"), r.nomi)
        assertEquals(Regole.nomiEffettivi(c), r.nomi)
    }

    @Test fun `CA-93 PAROLA_AFFINE con mostraCategoria falso non ha SENZA_CATEGORIA`() {
        val r = Passi.riepilogo(configBase(mod = Modalita.PAROLA_AFFINE, mostra = false), cats)
        assertFalse(OpzioneRiepilogo.SENZA_CATEGORIA in r.opzioni)
        assertEquals(Modalita.PAROLA_AFFINE, r.modalita)
    }

    @Test fun `CA-93 SENZA_PAROLA con mostraCategoria falso ha SENZA_CATEGORIA`() {
        val r = Passi.riepilogo(configBase(mod = Modalita.SENZA_PAROLA, mostra = false), cats)
        assertEquals(listOf(OpzioneRiepilogo.SENZA_CATEGORIA), r.opzioni)
    }

    @Test fun `CA-89 tutte le opzioni spente danno opzioni vuota e giriIndizi 1`() {
        val r = Passi.riepilogo(configBase(), cats)
        assertTrue(r.opzioni.isEmpty())
        assertEquals(1, r.giriIndizi)
    }

    @Test fun `CA-93 ordine casuale e 2 giri danno ORDINE_CASUALE e GIRI`() {
        val r = Passi.riepilogo(configBase().copy(ordineCasuale = true, giriIndizi = 2), cats)
        assertEquals(listOf(OpzioneRiepilogo.ORDINE_CASUALE, OpzioneRiepilogo.GIRI), r.opzioni)
        assertEquals(2, r.giriIndizi)
    }

    @Test fun `CA-93 tutte le opzioni attive nell ordine di 4-2-1 senza duplicati`() {
        val c = configBase(mostra = false).copy(
            impostoreNonPrimo = true, partitaTrappola = true, ordineCasuale = true,
            promemoriaUltimaPossibilita = true, giriIndizi = 3, impostoriSorpresa = true,
        )
        val r = Passi.riepilogo(c, cats)
        assertEquals(
            listOf(
                OpzioneRiepilogo.NON_PARLA_PER_PRIMO, OpzioneRiepilogo.TRAPPOLA, OpzioneRiepilogo.ORDINE_CASUALE,
                OpzioneRiepilogo.PROMEMORIA, OpzioneRiepilogo.SENZA_CATEGORIA, OpzioneRiepilogo.GIRI,
            ),
            r.opzioni,
        )
        assertEquals(r.opzioni.distinct(), r.opzioni)
        assertEquals(3, r.giriIndizi)
    }

    @Test fun `CA-93 numero di impostori e sorpresa non entrano in opzioni`() {
        val r = Passi.riepilogo(configBase(n = 6, k = 2).copy(impostoriSorpresa = true), cats)
        assertTrue(r.opzioni.isEmpty())
    }

    @Test fun `CA-93 ogni interruttore da solo produce la sua opzione`() {
        assertEquals(
            listOf(OpzioneRiepilogo.NON_PARLA_PER_PRIMO),
            Passi.riepilogo(configBase().copy(impostoreNonPrimo = true), cats).opzioni,
        )
        assertEquals(
            listOf(OpzioneRiepilogo.TRAPPOLA),
            Passi.riepilogo(configBase().copy(partitaTrappola = true), cats).opzioni,
        )
        assertEquals(
            listOf(OpzioneRiepilogo.PROMEMORIA),
            Passi.riepilogo(configBase().copy(promemoriaUltimaPossibilita = true), cats).opzioni,
        )
    }

    @Test fun `CA-103 numeroCategorie conta solo gli id esistenti`() {
        val c = configBase(cats = setOf("animali", "cibo", "inesistente", "fantasma"))
        assertEquals(2, Passi.riepilogo(c, cats).numeroCategorie)
        assertEquals(0, Passi.riepilogo(configBase(cats = setOf("zzz")), cats).numeroCategorie)
        assertEquals(0, Passi.riepilogo(configBase(cats = emptySet()), cats).numeroCategorie)
    }

    @Test fun `CA-94 il riepilogo riflette sempre la config corrente`() {
        val a = configBase(n = 4)
        val b = a.copy(numeroGiocatori = 6, ordineCasuale = true)
        assertEquals(4, Passi.riepilogo(a, cats).numeroGiocatori)
        assertEquals(6, Passi.riepilogo(b, cats).numeroGiocatori)
        assertEquals(4, Passi.riepilogo(a, cats).numeroGiocatori)
        assertTrue(Passi.riepilogo(a, cats).opzioni.isEmpty())
        assertEquals(Passi.riepilogo(b, cats), Passi.riepilogo(b.copy(), cats))
    }

    @Test fun `CA-94 giriIndizi fuori range e limitato a 1-3`() {
        assertEquals(1, Passi.riepilogo(configBase().copy(giriIndizi = 0), cats).giriIndizi)
        assertEquals(3, Passi.riepilogo(configBase().copy(giriIndizi = 9), cats).giriIndizi)
    }

    // ---- contaOpzioniAttive ----

    @Test fun `CA-89 tutte spente danno 0`() {
        assertEquals(0, Passi.contaOpzioniAttive(configBase()))
    }

    @Test fun `CA-81 solo mostraCategoria falso non e contato`() {
        assertEquals(0, Passi.contaOpzioniAttive(configBase(mostra = false)))
        assertEquals(0, Passi.contaOpzioniAttive(configBase(mod = Modalita.SENZA_PAROLA, mostra = false)))
    }

    @Test fun `CA-94 solo impostoriSorpresa non e contato`() {
        assertEquals(0, Passi.contaOpzioniAttive(configBase().copy(impostoriSorpresa = true)))
        assertEquals(0, Passi.contaOpzioniAttive(configBase(n = 6, k = 3)))
    }

    @Test fun `CA-81 giriIndizi 2 o 3 contano 1`() {
        assertEquals(1, Passi.contaOpzioniAttive(configBase().copy(giriIndizi = 2)))
        assertEquals(1, Passi.contaOpzioniAttive(configBase().copy(giriIndizi = 3)))
        assertEquals(0, Passi.contaOpzioniAttive(configBase().copy(giriIndizi = 1)))
    }

    @Test fun `CA-81 tutte le cinque contate attive danno 5`() {
        val c = configBase().copy(
            impostoreNonPrimo = true, partitaTrappola = true, ordineCasuale = true,
            promemoriaUltimaPossibilita = true, giriIndizi = 2,
        )
        assertEquals(5, Passi.contaOpzioniAttive(c))
        assertEquals(5, Passi.contaOpzioniAttive(c.copy(mostraCategoria = false, impostoriSorpresa = true)))
    }

    @Test fun `CA-81 ogni interruttore conta 1`() {
        assertEquals(1, Passi.contaOpzioniAttive(configBase().copy(impostoreNonPrimo = true)))
        assertEquals(1, Passi.contaOpzioniAttive(configBase().copy(partitaTrappola = true)))
        assertEquals(1, Passi.contaOpzioniAttive(configBase().copy(ordineCasuale = true)))
        assertEquals(1, Passi.contaOpzioniAttive(configBase().copy(promemoriaUltimaPossibilita = true)))
    }
}
