package it.imposteur.data

import it.imposteur.game.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class SerializzazioneSessioneTest {
    private val cats = categorieDemo()
    private val vuota = SessioneSalvata(null, null, emptySet(), null)

    private fun partita(
        modalita: Modalita = Modalita.SENZA_PAROLA,
        impostori: Set<Int> = setOf(1, 3),
        categoria: String = "animali", parola: String = "Cane", affine: String? = "Lupo",
        nome: String = "Animali", primo: Int = 2, mostra: Boolean = true,
    ) = Partita(
        giocatori = listOf("Anna", "Bruno", "Carla", "Dino", "Eva"),
        impostori = impostori,
        voce = VoceParola(categoria, nome, parola, affine),
        modalita = modalita, mostraCategoria = mostra, primoGiocatore = primo,
    )

    private fun sessione(
        p: Partita? = partita(), st: StatoDistribuzione? = StatoDistribuzione.Gioco,
        usate: Set<ChiaveParola> = emptySet(), ultima: ChiaveParola? = null,
    ) = SessioneSalvata(p, st, usate, ultima)

    private fun rt(s: SessioneSalvata) = SerializzazioneSessione.daStringa(SerializzazioneSessione.aStringa(s), cats)

    // Nome del campo-oggetto "partita" (il piu grande) e dello stato (il piu piccolo) nel JSON, senza assumere i nomi.
    private fun oggetti(s: SessioneSalvata): Pair<JsonObject, List<Map.Entry<String, JsonElement>>> {
        val obj = Json.parseToJsonElement(SerializzazioneSessione.aStringa(s)).jsonObject
        return obj to obj.entries.filter { it.value is JsonObject }.sortedBy { (it.value as JsonObject).size }
    }

    @Test fun `CA-37 round-trip Passaggio k per ogni k`() {
        for (k in 0 until 5) {
            val s = sessione(st = StatoDistribuzione.Passaggio(k))
            assertEquals(s, rt(s))
        }
    }

    @Test fun `CA-37 round-trip Gioco`() {
        val s = sessione(st = StatoDistribuzione.Gioco)
        assertEquals(s, rt(s))
    }

    @Test fun `CA-37 e CA-39 Rivelazione k diventa Passaggio k`() {
        for (k in 0 until 5) {
            val s = sessione(st = StatoDistribuzione.Rivelazione(k))
            assertEquals(s.copy(stato = StatoDistribuzione.Passaggio(k)), rt(s))
        }
    }

    @Test fun `CA-37 round-trip modalita parola affine e opzioni`() {
        val s = sessione(
            partita(Modalita.PAROLA_AFFINE, setOf(0), "cibo", "Pizza", "Focaccia", "Cibo", 4, false),
            StatoDistribuzione.Passaggio(3),
        )
        assertEquals(s, rt(s))
        val s2 = sessione(partita(mostra = false, primo = 0), StatoDistribuzione.Gioco)
        assertEquals(s2, rt(s2))
    }

    @Test fun `CA-37 round-trip affine null in modalita senza parola`() {
        val s = sessione(partita(affine = null))
        assertEquals(s, rt(s))
    }

    @Test fun `CA-39 Passaggio e Gioco restano invariati`() {
        assertEquals(StatoDistribuzione.Passaggio(2), rt(sessione(st = StatoDistribuzione.Passaggio(2))).stato)
        assertEquals(StatoDistribuzione.Gioco, rt(sessione(st = StatoDistribuzione.Gioco)).stato)
    }

    @Test fun `CA-38 round-trip insieme usate vuoto`() {
        assertEquals(vuota, rt(vuota))
    }

    @Test fun `CA-38 round-trip insieme usate con ultima e senza partita`() {
        val u = setOf(ChiaveParola("animali", "cane"), ChiaveParola("cibo", "pizza"))
        val s = SessioneSalvata(null, null, u, ChiaveParola("cibo", "pizza"))
        assertEquals(s, rt(s))
    }

    @Test fun `CA-38 round-trip usate insieme a partita`() {
        val u = setOf(ChiaveParola("animali", "cane"), ChiaveParola("animali", "gatto"))
        val s = sessione(usate = u, ultima = ChiaveParola("animali", "cane"))
        assertEquals(s, rt(s))
    }

    @Test fun `CA-40 null vuoto e JSON malformato danno sessione vuota senza eccezioni`() {
        for (x in listOf<String?>(null, "", "   ", "{", "non json", "[1,2", "{\"a\":", "null", "[]", "42", "{}")) {
            assertEquals("input=$x", vuota, SerializzazioneSessione.daStringa(x, cats))
        }
    }

    @Test fun `CA-40 JSON troncato non lancia eccezioni`() {
        val j = SerializzazioneSessione.aStringa(sessione())
        for (n in 0 until j.length - 1 step 3) {
            assertEquals("n=$n", vuota, SerializzazioneSessione.daStringa(j.substring(0, n), cats))
        }
    }

    @Test fun `CA-40 indici impostori fuori range scartano la partita`() {
        for (imp in listOf(setOf(5), setOf(-1), setOf(0, 99), setOf(7, 1))) {
            val r = rt(sessione(partita(impostori = imp)))
            assertNull("imp=$imp", r.partita)
            assertNull(r.stato)
        }
    }

    @Test fun `CA-40 primo giocatore fuori range scarta la partita`() {
        assertNull(rt(sessione(partita(primo = 5))).partita)
        assertNull(rt(sessione(partita(primo = -1))).partita)
    }

    @Test fun `CA-40 k fuori intervallo scarta la partita`() {
        for (st in listOf(
            StatoDistribuzione.Passaggio(5), StatoDistribuzione.Passaggio(-1),
            StatoDistribuzione.Rivelazione(5), StatoDistribuzione.Rivelazione(99),
        )) {
            val r = rt(sessione(st = st))
            assertNull("st=$st", r.partita)
            assertNull("st=$st", r.stato)
        }
    }

    @Test fun `CA-40 impostori duplicati nel JSON scartano la partita`() {
        val (obj, ogg) = oggetti(sessione(partita(impostori = setOf(1, 3))))
        val (kP, p) = ogg.last()
        val po = p as JsonObject
        val kI = po.entries.first { e ->
            val a = e.value as? JsonArray
            a != null && a.size == 2 && a.all { (it as? JsonPrimitive)?.intOrNull != null }
        }.key
        val dup = JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(1)))
        val mod = JsonObject(obj + (kP to JsonObject(po + (kI to dup))))
        assertNull(SerializzazioneSessione.daStringa(mod.toString(), cats).partita)
    }

    @Test fun `CA-40 campo mancante nella partita la scarta`() {
        val (obj, ogg) = oggetti(sessione())
        val (kP, p) = ogg.last()
        for (campo in (p as JsonObject).keys) {
            val mod = JsonObject(obj + (kP to JsonObject(p - campo)))
            val r = SerializzazioneSessione.daStringa(mod.toString(), cats)
            assertNull("campo mancante '$campo' dovrebbe scartare la partita", r.partita)
        }
    }

    @Test fun `CA-40 stato sconosciuto scarta la partita`() {
        val (obj, ogg) = oggetti(sessione(st = StatoDistribuzione.Passaggio(1)))
        val (kS, st) = ogg.first()
        val rotto = JsonObject((st as JsonObject).mapValues { (_, v) ->
            if (v is JsonPrimitive && v.isString) JsonPrimitive("XYZ_SCONOSCIUTO") else v
        })
        val mod = JsonObject(obj + (kS to rotto))
        assertNull(SerializzazioneSessione.daStringa(mod.toString(), cats).partita)
    }

    @Test fun `CA-41 categoria non piu presente scarta partita e stato`() {
        val r = rt(sessione(partita(categoria = "sparita", nome = "Sparita")))
        assertNull(r.partita); assertNull(r.stato)
    }

    @Test fun `CA-41 parola non piu presente scarta la partita`() {
        val r = rt(sessione(partita(parola = "Ornitorinco")))
        assertNull(r.partita); assertNull(r.stato)
    }

    @Test fun `CA-41 affine non piu presente in modalita affine scarta la partita`() {
        assertNull(rt(sessione(partita(Modalita.PAROLA_AFFINE, affine = "Inesistente"))).partita)
    }

    @Test fun `CA-41 dati coerenti col file sono validi`() {
        assertNotNull(rt(sessione(partita(Modalita.PAROLA_AFFINE))).partita)
        assertNotNull(rt(sessione()).partita)
    }

    @Test fun `CA-41 CA-42 scarto partita conserva le usate filtrate sulle chiavi esistenti`() {
        val u = setOf(ChiaveParola("animali", "cane"), ChiaveParola("cibo", "pizza"), ChiaveParola("sparita", "x"))
        val r = rt(sessione(partita(parola = "Ornitorinco"), usate = u))
        assertNull(r.partita)
        assertEquals(setOf(ChiaveParola("animali", "cane"), ChiaveParola("cibo", "pizza")), r.usate)
    }

    @Test fun `CA-42 usate non piu presenti nel file sono ignorate`() {
        val u = setOf(ChiaveParola("animali", "cane"), ChiaveParola("animali", "drago"), ChiaveParola("fantasy", "elfo"))
        val r = rt(SessioneSalvata(null, null, u, ChiaveParola("animali", "cane")))
        assertEquals(setOf(ChiaveParola("animali", "cane")), r.usate)
    }

    @Test fun `CA-42 ultima non piu presente nel file e ignorata`() {
        assertNull(rt(SessioneSalvata(null, null, emptySet(), ChiaveParola("fantasy", "elfo"))).ultima)
    }
}
