package it.imposteur.game

import org.junit.Assert.*
import org.junit.Test

class PassiTest {
    private val cats = categorieDemo()
    private val catSenzaAffini = listOf(cat("solo", "Alfa" to null, "Beta" to null))

    private val erroriGiocatori = { e: ErroreConfigurazione ->
        e is ErroreConfigurazione.TroppoPochiGiocatori || e is ErroreConfigurazione.TroppiGiocatori ||
            e is ErroreConfigurazione.NomeDuplicato || e is ErroreConfigurazione.NomeTroppoLungo
    }

    // ---- enum ----

    @Test fun `CA-99 ordinale piu uno e il numero del passo`() {
        assertEquals(
            listOf(
                PassoConfigurazione.GIOCATORI, PassoConfigurazione.MODALITA, PassoConfigurazione.OPZIONI,
                PassoConfigurazione.CATEGORIE, PassoConfigurazione.RIEPILOGO,
            ),
            PassoConfigurazione.entries.toList(),
        )
        assertEquals(listOf(1, 2, 3, 4, 5), PassoConfigurazione.entries.map { it.ordinal + 1 })
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

    @Test fun `CA-101 impostori oltre il massimo danno MODALITA e GIOCATORI null`() {
        val c = configBase(n = 5, k = 3)
        assertEquals(ErroreConfigurazione.TroppiImpostori, Passi.errorePasso(PassoConfigurazione.MODALITA, c, cats))
        assertNull(Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats))
        assertNull(Passi.errorePasso(PassoConfigurazione.CATEGORIE, c, cats))
    }

    @Test fun `CA-101 zero impostori danno TroppoPochiImpostori in MODALITA`() {
        val c = configBase(n = 5, k = 0)
        assertEquals(ErroreConfigurazione.TroppoPochiImpostori, Passi.errorePasso(PassoConfigurazione.MODALITA, c, cats))
        assertNull(Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats))
    }

    @Test fun `CA-101 MODALITA e null con impostori coerenti anche con nomi duplicati e categorie vuote`() {
        assertNull(Passi.errorePasso(PassoConfigurazione.MODALITA, configBase(), cats))
        val dup = configBase(n = 4, nomi = listOf("A", "a"), cats = emptySet())
        assertNull(Passi.errorePasso(PassoConfigurazione.MODALITA, dup, cats))
    }

    @Test fun `CA-25 MODALITA restituisce solo errori sugli impostori`() {
        val configs = listOf(
            configBase(n = 2, k = 0, cats = emptySet()), configBase(n = 21, k = 1), configBase(n = 5, k = 9),
            configBase(n = 4, nomi = listOf("A", "a")), configBase(n = 5, k = 0),
        )
        for (c in configs) {
            val m = Passi.errorePasso(PassoConfigurazione.MODALITA, c, cats)
            if (m != null) assertTrue(
                "MODALITA: $m",
                m is ErroreConfigurazione.TroppoPochiImpostori || m is ErroreConfigurazione.TroppiImpostori,
            )
        }
    }

    // ---- primoPassoNonValido ----

    @Test fun `CA-102 impostori incoerenti e categorie vuote danno MODALITA`() {
        val c = configBase(n = 5, k = 3, cats = emptySet())
        assertEquals(PassoConfigurazione.MODALITA, Passi.primoPassoNonValido(c, cats))
    }

    @Test fun `CA-102 nome duplicato e impostori incoerenti danno GIOCATORI`() {
        val c = configBase(n = 4, k = 3, nomi = listOf("A", "a"))
        assertEquals(PassoConfigurazione.GIOCATORI, Passi.primoPassoNonValido(c, cats))
    }

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
            assertTrue("$p", p == null || p == PassoConfigurazione.GIOCATORI || p == PassoConfigurazione.CATEGORIE || p == PassoConfigurazione.MODALITA)
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

    // ---- aggiungiGiocatore / rimuoviGiocatore (v1.9) ----

    @Test fun `CA-113 aggiungi con nomi vuoti porta N a 5 e nomi completati`() {
        val r = Passi.aggiungiGiocatore(configBase(n = 4, nomi = emptyList()))
        assertEquals(5, r.numeroGiocatori)
        assertEquals(listOf("", "", "", "", ""), r.nomi)
        assertEquals(1, r.numeroImpostori)
        assertEquals(5, Regole.nomiEffettivi(r).size)
    }

    @Test fun `CA-113 aggiungi completa i nomi corti con vuoti`() {
        val r = Passi.aggiungiGiocatore(configBase(n = 4, nomi = listOf("A", "B")))
        assertEquals(listOf("A", "B", "", "", ""), r.nomi)
        assertEquals(5, r.numeroGiocatori)
    }

    @Test fun `CA-113 aggiungi scarta i nomi oltre N`() {
        val r = Passi.aggiungiGiocatore(configBase(n = 3, nomi = listOf("A", "B", "C", "X")))
        assertEquals(4, r.numeroGiocatori)
        assertEquals(listOf("A", "B", "C", ""), r.nomi)
    }

    @Test fun `CA-113 CA-117 aggiungere non modifica gli impostori ne gli altri campi`() {
        val c = configBase(n = 5, k = 2, mod = Modalita.PAROLA_AFFINE, cats = setOf("cibo"))
            .copy(partitaTrappola = true, impostoriSorpresa = true, giriIndizi = 2)
        val r = Passi.aggiungiGiocatore(c)
        assertEquals(6, r.numeroGiocatori)
        assertEquals(2, r.numeroImpostori)
        assertEquals(c.copy(numeroGiocatori = 6, nomi = List(6) { "" }), r)
    }

    @Test fun `CA-114 aggiungi con 19 porta a 20 e con 20 lascia invariata`() {
        assertEquals(20, Passi.aggiungiGiocatore(configBase(n = 19)).numeroGiocatori)
        val c = configBase(n = 20, nomi = listOf("A"))
        assertEquals(c, Passi.aggiungiGiocatore(c))
    }

    @Test fun `CA-118 aggiungi e funzione pura e non modifica l'originale`() {
        val c = configBase(n = 4, nomi = listOf("A", "B"))
        Passi.aggiungiGiocatore(c)
        assertEquals(4, c.numeroGiocatori)
        assertEquals(listOf("A", "B"), c.nomi)
    }

    @Test fun `CA-116 rimuovi indice 1 fa scalare i successivi e il vuoto segue la posizione`() {
        val r = Passi.rimuoviGiocatore(configBase(n = 4, nomi = listOf("Anna", "", "Carla", "")), 1)
        assertEquals(3, r.numeroGiocatori)
        assertEquals(listOf("Anna", "Carla", ""), r.nomi)
        assertEquals(listOf("Anna", "Carla", "Giocatore 3"), Regole.nomiEffettivi(r))
    }

    @Test fun `CA-116 rimuovi indice 0 e ultimo indice`() {
        val r0 = Passi.rimuoviGiocatore(configBase(n = 4, nomi = listOf("", "Bea", "Cia", "Dino")), 0)
        assertEquals(listOf("Bea", "Cia", "Dino"), r0.nomi)
        val rU = Passi.rimuoviGiocatore(configBase(n = 4, nomi = listOf("A", "B", "C", "D")), 3)
        assertEquals(listOf("A", "B", "C"), rU.nomi)
    }

    @Test fun `CA-116 rimuovi con nomi piu corti di N`() {
        val r = Passi.rimuoviGiocatore(configBase(n = 5, nomi = listOf("A")), 3)
        assertEquals(4, r.numeroGiocatori)
        // il contratto dice nomi ["A","",""]: si verifica il comportamento osservabile, non la lunghezza di `nomi`
        assertEquals(listOf("A", "Giocatore 2", "Giocatore 3", "Giocatore 4"), Regole.nomiEffettivi(r))
    }

    @Test fun `CA-115 rimuovi con 3 giocatori o indice fuori range lascia invariata`() {
        val c3 = configBase(n = 3, k = 1, nomi = listOf("A", "B", "C"))
        assertEquals(c3, Passi.rimuoviGiocatore(c3, 0))
        val c = configBase(n = 5, nomi = listOf("A", "B", "C", "D", "E"))
        assertEquals(c, Passi.rimuoviGiocatore(c, -1))
        assertEquals(c, Passi.rimuoviGiocatore(c, 5))
    }

    @Test fun `CA-117 rimuovere riduce gli impostori al nuovo massimo`() {
        fun dopo(n: Int, k: Int) =
            Passi.rimuoviGiocatore(configBase(n = n, k = k), 0).let { it.numeroGiocatori to it.numeroImpostori }
        assertEquals(6 to 2, dopo(7, 3))
        assertEquals(4 to 1, dopo(5, 2))
        assertEquals(5 to 1, dopo(6, 1))
        assertEquals(19 to 9, dopo(20, 9))
    }

    @Test fun `CA-117 rimuovere non tocca sorpresa trappola e modalita`() {
        val c = configBase(n = 6, k = 2, mod = Modalita.PAROLA_AFFINE).copy(impostoriSorpresa = true, partitaTrappola = true)
        val r = Passi.rimuoviGiocatore(c, 2)
        assertTrue(r.impostoriSorpresa && r.partitaTrappola)
        assertEquals(Modalita.PAROLA_AFFINE, r.modalita)
        assertEquals(c.categorieSelezionate, r.categorieSelezionate)
    }

    @Test fun `CA-118 rimuovere uno di due duplicati elimina l'errore del passo GIOCATORI`() {
        val c = configBase(n = 4, nomi = listOf("Ann", "ann", "Bea", "Cia"))
        assertNotNull(Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats))
        val r = Passi.rimuoviGiocatore(c, 1)
        assertNull(Passi.errorePasso(PassoConfigurazione.GIOCATORI, r, cats))
    }

    @Test fun `CA-118 aggiungere crea un duplicato quando un nome coincide con il default della nuova posizione`() {
        val c = Passi.aggiungiGiocatore(configBase(n = 3, nomi = listOf("Giocatore 4", "B", "C")))
        assertTrue(Passi.errorePasso(PassoConfigurazione.GIOCATORI, c, cats) is ErroreConfigurazione.NomeDuplicato)
    }

    @Test fun `CA-114 CA-115 puoAggiungere e puoRimuovere ai bordi`() {
        assertTrue(Passi.puoAggiungereGiocatore(configBase(n = 19)))
        assertFalse(Passi.puoAggiungereGiocatore(configBase(n = 20)))
        assertFalse(Passi.puoRimuovereGiocatore(configBase(n = 3)))
        assertTrue(Passi.puoRimuovereGiocatore(configBase(n = 4)))
        assertTrue(Passi.puoAggiungereGiocatore(configBase(n = 3)))
        assertTrue(Passi.puoRimuovereGiocatore(configBase(n = 20)))
    }

    @Test fun `CA-113 CA-115 da 3 si aggiunge fino a 20 e si rimuove fino a 3`() {
        var c = configBase(n = 3, k = 1)
        while (Passi.puoAggiungereGiocatore(c)) c = Passi.aggiungiGiocatore(c)
        assertEquals(20, c.numeroGiocatori)
        while (Passi.puoRimuovereGiocatore(c)) c = Passi.rimuoviGiocatore(c, 0)
        assertEquals(3, c.numeroGiocatori)
        assertEquals(1, c.numeroImpostori)
    }

    @Test fun `CA-104 riepilogo dopo rimozione riflette il nuovo massimo di impostori`() {
        val c = configBase(n = 5, k = 2).copy(impostoriSorpresa = true)
        assertTrue(Passi.riepilogo(c, cats).finoA)
        val r = Passi.riepilogo(Passi.rimuoviGiocatore(c, 0), cats)
        assertEquals(4, r.numeroGiocatori)
        assertEquals(1, r.numeroImpostori)
        assertFalse(r.finoA)
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
