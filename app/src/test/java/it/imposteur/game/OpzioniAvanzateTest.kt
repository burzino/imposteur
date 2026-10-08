package it.imposteur.game

import it.imposteur.data.SerializzazioneConfigurazione
import it.imposteur.data.SerializzazioneSessione
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Opzioni avanzate (contratto v1.6). Id OPZ-xx. */
class OpzioniAvanzateTest {
    private val cats = categorieDemo()
    private val seeds = 500

    private fun cfg(
        n: Int = 7, k: Int = 2, mod: Modalita = Modalita.SENZA_PAROLA,
        nonPrimo: Boolean = false, sorpresa: Boolean = false, casuale: Boolean = false,
        trappola: Boolean = false, promemoria: Boolean = false, giri: Int = 1,
    ) = Configurazione(
        numeroGiocatori = n, nomi = emptyList(), numeroImpostori = k, modalita = mod,
        mostraCategoria = true, categorieSelezionate = setOf("animali", "cibo"),
        impostoreNonPrimo = nonPrimo, impostoriSorpresa = sorpresa, ordineCasuale = casuale,
        partitaTrappola = trappola, promemoriaUltimaPossibilita = promemoria, giriIndizi = giri,
    )

    private fun gioca(c: Configurazione, seed: Int): Partita {
        val r = GestorePartite(Random(seed)).nuovaPartita(c, cats)
        assertTrue("seed=$seed: $r", r is RisultatoNuovaPartita.Ok)
        return (r as RisultatoNuovaPartita.Ok).partita
    }

    private fun rotazione(p: Partita) = List(p.giocatori.size) { (p.primoGiocatore + it) % p.giocatori.size }

    // ---------- Default ----------

    @Test fun `OPZ-01 default K uguale a numeroImpostori ordine a giro e mai trappola su 500 seed`() {
        for (seed in 0 until seeds) {
            val p = gioca(cfg(), seed)
            assertEquals("seed=$seed", 2, p.impostori.size)
            assertEquals("seed=$seed", rotazione(p), p.ordineDiParola())
            assertFalse("seed=$seed", p.trappola)
            assertEquals(1, p.giriIndizi)
            assertFalse(p.promemoriaUltimaPossibilita)
        }
    }

    // ---------- impostoreNonPrimo ----------

    @Test fun `OPZ-02 impostoreNonPrimo il primo non e mai impostore e varia tra i civili`() {
        val primi = mutableSetOf<Int>()
        for (seed in 0 until seeds) {
            val p = gioca(cfg(nonPrimo = true), seed)
            assertEquals(2, p.impostori.size)
            assertFalse("seed=$seed", p.primoGiocatore in p.impostori)
            primi += p.primoGiocatore
        }
        assertTrue("il primo deve variare: $primi", primi.size > 1)
    }

    @Test fun `OPZ-02 impostoreNonPrimo con K massimo e K 1 su piu configurazioni`() {
        for (n in 3..9) for (k in 1..Regole.maxImpostori(n)) for (seed in 0 until 60) {
            val p = gioca(cfg(n = n, k = k, nonPrimo = true), seed)
            assertFalse("n=$n k=$k seed=$seed", p.primoGiocatore in p.impostori)
        }
    }

    // ---------- impostoriSorpresa ----------

    @Test fun `OPZ-03 impostoriSorpresa K in 1 fino a max e compaiono tutti i valori`() {
        val max = 3
        val visti = mutableSetOf<Int>()
        for (seed in 0 until seeds) {
            val p = gioca(cfg(n = 9, k = max, sorpresa = true), seed)
            assertTrue("seed=$seed K=${p.impostori.size}", p.impostori.size in 1..max)
            visti += p.impostori.size
        }
        assertEquals(setOf(1, 2, 3), visti)
    }

    @Test fun `OPZ-03 impostoriSorpresa con max 1 da sempre K 1`() {
        for (seed in 0 until seeds) assertEquals(1, gioca(cfg(n = 5, k = 1, sorpresa = true), seed).impostori.size)
    }

    // ---------- ordineCasuale ----------

    @Test fun `OPZ-04 ordineCasuale e una permutazione che inizia con il primo e a volte non e la rotazione`() {
        var diverso = false
        for (seed in 0 until seeds) {
            val p = gioca(cfg(casuale = true), seed)
            val o = p.ordineDiParola()
            assertEquals("seed=$seed", (0 until 7).toSet(), o.toSet())
            assertEquals("seed=$seed", 7, o.size)
            assertEquals("seed=$seed", p.primoGiocatore, o[0])
            if (o != rotazione(p)) diverso = true
        }
        assertTrue("almeno una volta diverso dalla rotazione", diverso)
    }

    // ---------- partitaTrappola ----------

    @Test fun `OPZ-05 trappola frequenza di K 0 tra 5 e 15 percento su 2000 seed`() {
        var zero = 0
        val tot = 2000
        for (seed in 0 until tot) {
            val p = gioca(cfg(trappola = true), seed)
            if (p.impostori.isEmpty()) zero++ else assertEquals(2, p.impostori.size)
        }
        val f = zero.toDouble() / tot
        assertTrue("frequenza=$f", f in 0.05..0.15)
    }

    @Test fun `OPZ-05 con K 0 tutti ricevono la parola in entrambe le modalita e trappola e true`() {
        for (mod in Modalita.entries) {
            val p = trappolaDi(mod)
            assertTrue(p.trappola)
            assertTrue(p.impostori.isEmpty())
            for (i in p.giocatori.indices) {
                val c = p.contenutoPer(i)
                assertTrue("mod=$mod i=$i $c", c is ContenutoRuolo.ParolaSegreta)
                assertEquals(p.voce.parola, (c as ContenutoRuolo.ParolaSegreta).testo)
            }
        }
    }

    @Test fun `OPZ-05 lo svelamento della trappola contiene il testo della trappola e la parola`() {
        for (mod in Modalita.entries) {
            val p = trappolaDi(mod)
            val t = p.testoSvelamento()
            assertTrue(t, t.contains("Nessun impostore: era una partita trappola!"))
            assertTrue(t, t.contains(p.voce.parola))
            if (mod == Modalita.PAROLA_AFFINE && p.voce.affine != null) assertTrue(t, t.contains(p.voce.affine!!))
            assertTrue(t, t.contains(p.voce.categoriaNome))
        }
    }

    @Test fun `OPZ-05 senza trappola lo svelamento non contiene il testo della trappola`() {
        val t = gioca(cfg(), 1).testoSvelamento()
        assertFalse(t, t.contains("trappola"))
    }

    private fun trappolaDi(mod: Modalita): Partita {
        for (seed in 0 until 5000) {
            val p = gioca(cfg(mod = mod, trappola = true), seed)
            if (p.impostori.isEmpty()) return p
        }
        fail("nessuna partita trappola trovata"); throw IllegalStateException()
    }

    // ---------- Combinate ----------

    @Test fun `OPZ-06 trappola piu impostoreNonPrimo con K 0 non va in errore`() {
        var zero = 0
        for (seed in 0 until 2000) {
            val p = gioca(cfg(trappola = true, nonPrimo = true, casuale = true, sorpresa = true), seed)
            assertTrue(p.primoGiocatore in p.giocatori.indices)
            if (p.impostori.isEmpty()) zero++ else assertFalse(p.primoGiocatore in p.impostori)
        }
        assertTrue("almeno una trappola", zero > 0)
    }

    // ---------- Promemoria e giri ----------

    @Test fun `OPZ-07 promemoria e giri vengono copiati nella partita`() {
        val p = gioca(cfg(promemoria = true, giri = 3), 7)
        assertTrue(p.promemoriaUltimaPossibilita)
        assertEquals(3, p.giriIndizi)
        val q = gioca(cfg(promemoria = false, giri = 2), 7)
        assertFalse(q.promemoriaUltimaPossibilita)
        assertEquals(2, q.giriIndizi)
    }

    @Test fun `OPZ-07 giri 0 o 5 vengono limitati a 1 fino a 3 dalla serializzazione`() {
        fun giri(g: Int) = SerializzazioneConfigurazione.daStringa(
            SerializzazioneConfigurazione.aStringa(cfg(giri = g)), cats,
        ).giriIndizi
        assertEquals(1, giri(0))
        assertEquals(3, giri(5))
        assertEquals(1, giri(-2))
        for (g in 1..3) assertEquals(g, giri(g))
    }

    // ---------- Serializzazione configurazione ----------

    @Test fun `OPZ-08 round-trip configurazione con tutte le opzioni`() {
        val c = cfg(
            n = 8, k = 3, mod = Modalita.PAROLA_AFFINE, nonPrimo = true, sorpresa = true,
            casuale = true, trappola = true, promemoria = true, giri = 2,
        )
        assertEquals(c, SerializzazioneConfigurazione.daStringa(SerializzazioneConfigurazione.aStringa(c), cats))
    }

    @Test fun `OPZ-08 configurazione senza i campi nuovi usa i default`() {
        val vecchio = """{"numeroGiocatori":6,"nomi":[],"numeroImpostori":2,"modalita":"SENZA_PAROLA","mostraCategoria":true,"categorieSelezionate":["animali"]}"""
        val c = SerializzazioneConfigurazione.daStringa(vecchio, cats)
        assertEquals(6, c.numeroGiocatori)
        assertEquals(2, c.numeroImpostori)
        assertFalse(c.impostoreNonPrimo)
        assertFalse(c.impostoriSorpresa)
        assertFalse(c.ordineCasuale)
        assertFalse(c.partitaTrappola)
        assertFalse(c.promemoriaUltimaPossibilita)
        assertEquals(1, c.giriIndizi)
    }

    // ---------- Serializzazione sessione ----------

    private fun partitaS(
        ordine: List<Int>? = null, impostori: Set<Int> = setOf(1, 3), primo: Int = 2,
        giri: Int = 1, promemoria: Boolean = false,
    ): Partita {
        val g = listOf("Anna", "Bruno", "Carla", "Dino", "Eva")
        val v = VoceParola("animali", "Animali", "Cane", "Lupo")
        return if (ordine == null) {
            Partita(g, impostori, v, Modalita.SENZA_PAROLA, true, primo, giriIndizi = giri, promemoriaUltimaPossibilita = promemoria)
        } else {
            Partita(g, impostori, v, Modalita.SENZA_PAROLA, true, primo, ordine, giri, promemoria)
        }
    }

    private fun sess(p: Partita) = SessioneSalvata(p, StatoDistribuzione.Gioco, emptySet(), null)
    private fun rt(s: SessioneSalvata) = SerializzazioneSessione.daStringa(SerializzazioneSessione.aStringa(s), cats)

    /** Chiave e valore del campo "ordine" nel JSON, individuato come l'unico array di 5 interi dentro la partita. */
    private fun campoOrdine(s: SessioneSalvata): Triple<JsonObject, String, String> {
        val obj = Json.parseToJsonElement(SerializzazioneSessione.aStringa(s)).jsonObject
        val (kP, p) = obj.entries.filter { it.value is JsonObject }.maxByOrNull { (it.value as JsonObject).size }!!
        val kO = (p as JsonObject).entries.single { e ->
            val a = e.value as? JsonArray
            a != null && a.size == 5 && a.all { (it as? JsonPrimitive)?.intOrNull != null }
        }.key
        return Triple(obj, kP, kO)
    }

    private fun conOrdine(s: SessioneSalvata, nuovo: JsonElement?): SessioneSalvata {
        val (obj, kP, kO) = campoOrdine(s)
        val po = obj[kP] as JsonObject
        val mod = JsonObject(obj + (kP to JsonObject(if (nuovo == null) po - kO else po + (kO to nuovo))))
        return SerializzazioneSessione.daStringa(mod.toString(), cats)
    }

    private fun arr(vararg v: Int) = JsonArray(v.map { JsonPrimitive(it) })

    @Test fun `OPZ-09 round-trip sessione con ordine casuale giri e promemoria`() {
        val s = sess(partitaS(ordine = listOf(2, 4, 0, 3, 1), giri = 3, promemoria = true))
        assertEquals(s, rt(s))
    }

    @Test fun `OPZ-09 round-trip sessione con trappola`() {
        val s = sess(partitaS(impostori = emptySet(), ordine = listOf(2, 0, 4, 1, 3)))
        val r = rt(s)
        assertEquals(s, r)
        assertTrue(r.partita!!.trappola)
    }

    @Test fun `OPZ-09 sessione con impostori vuoti e valida`() {
        val r = rt(sess(partitaS(impostori = emptySet())))
        assertNotNull(r.partita)
        assertEquals(StatoDistribuzione.Gioco, r.stato)
    }

    @Test fun `OPZ-09 sessione senza ordine da la rotazione`() {
        val s = sess(partitaS(ordine = listOf(2, 4, 0, 3, 1)))
        val r = conOrdine(s, null)
        assertNotNull(r.partita)
        assertEquals(listOf(2, 3, 4, 0, 1), r.partita!!.ordineDiParola())
    }

    @Test fun `OPZ-09 ordine non valido scarta la partita`() {
        val s = sess(partitaS(ordine = listOf(2, 4, 0, 3, 1)))
        val invalidi = listOf(
            arr(0, 1, 2, 3, 4),   // non inizia con il primo (2)
            arr(2, 2, 0, 1, 3),   // duplicato
            arr(2, 0, 1, 3, 9),   // fuori range
            arr(2, 0, 1, 3, -1),  // negativo
            arr(2, 0, 1, 3),      // lunghezza errata
            arr(2, 0, 1, 3, 4, 1), // troppo lungo
        )
        for (o in invalidi) {
            val r = conOrdine(s, o)
            assertNull("ordine=$o", r.partita)
            assertNull("ordine=$o", r.stato)
        }
    }
}
