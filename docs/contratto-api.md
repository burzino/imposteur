# Contratto delle API (v1)

È il riferimento comune per chi implementa la logica (`game`, `data`), per chi scrive i test e per chi scrive la UI. Le firme sono vincolanti: chi ha bisogno di cambiarle lo segnala nel resoconto, senza cambiarle da solo.

Package radice: `it.imposteur`.

## `it.imposteur.game` (Kotlin puro, nessun import `android.*`)

```kotlin
enum class Modalita { SENZA_PAROLA, PAROLA_AFFINE }

data class Parola(val parola: String, val affine: String?)          // affine null se assente o vuota
data class Categoria(val id: String, val nome: String, val parole: List<Parola>)

data class Configurazione(
    val numeroGiocatori: Int = 4,
    val nomi: List<String> = emptyList(),      // nomi inseriti, lunghezza <= numeroGiocatori; "" = default
    val numeroImpostori: Int = 1,
    val modalita: Modalita = Modalita.SENZA_PAROLA,
    val mostraCategoria: Boolean = true,
    val categorieSelezionate: Set<String> = emptySet(),
)

object Regole {
    const val MIN_GIOCATORI = 3
    const val MAX_GIOCATORI = 20
    const val MAX_LUNGHEZZA_NOME = 20
    fun maxImpostori(numeroGiocatori: Int): Int                         // floor((N-1)/2), CA-01
    fun nomiEffettivi(config: Configurazione): List<String>             // trim + default "Giocatore n", size == N, CA-05
    fun valida(config: Configurazione, categorie: List<Categoria>): List<ErroreConfigurazione>  // vuota = valida
    fun conNumeroGiocatori(config: Configurazione, n: Int): Configurazione                     // CA-06
    fun pool(categorie: List<Categoria>, selezionate: Set<String>, modalita: Modalita): List<VoceParola> // CA-11
}

sealed interface ErroreConfigurazione {
    data object TroppoPochiGiocatori : ErroreConfigurazione
    data object TroppiGiocatori : ErroreConfigurazione
    data object TroppoPochiImpostori : ErroreConfigurazione
    data object TroppiImpostori : ErroreConfigurazione
    data object NessunaCategoria : ErroreConfigurazione
    data object PoolVuoto : ErroreConfigurazione                       // CA-16
    data class NomeDuplicato(val indici: List<Int>) : ErroreConfigurazione   // indici 0-based coinvolti
    data class NomeTroppoLungo(val indice: Int) : ErroreConfigurazione
}

data class VoceParola(val categoriaId: String, val categoriaNome: String, val parola: String, val affine: String?)

/** Ciò che un giocatore vede. In PAROLA_AFFINE civile e impostore sono entrambi Parola (CA-10). */
sealed interface ContenutoRuolo {
    data class ParolaSegreta(val testo: String) : ContenutoRuolo
    data class Impostore(val categoria: String?) : ContenutoRuolo      // categoria null se mostraCategoria = false
}

data class Partita(
    val giocatori: List<String>,               // nomi effettivi
    val impostori: Set<Int>,                   // indici 0-based
    val voce: VoceParola,
    val modalita: Modalita,
    val mostraCategoria: Boolean,
    val primoGiocatore: Int,
) {
    fun contenutoPer(indice: Int): ContenutoRuolo                      // CA-09, CA-10
    fun testoSvelamento(): String                                      // CA-21, testi esatti dalle specifiche §4.5
}

/** Vive per tutta la sessione: tiene l'insieme delle parole usate (CA-13, CA-14). */
class GestorePartite(private val random: kotlin.random.Random) {
    fun nuovaPartita(config: Configurazione, categorie: List<Categoria>): RisultatoNuovaPartita  // CA-07, 08, 12, 15, 20
}
sealed interface RisultatoNuovaPartita {
    data class Ok(val partita: Partita) : RisultatoNuovaPartita
    data class Errore(val errori: List<ErroreConfigurazione>) : RisultatoNuovaPartita
}

/** Macchina a stati della distribuzione (CA-18). Immutabile: ogni transizione restituisce un nuovo stato. */
sealed interface StatoDistribuzione {
    data class Passaggio(val indice: Int) : StatoDistribuzione
    data class Rivelazione(val indice: Int) : StatoDistribuzione
    data object Gioco : StatoDistribuzione
}
object Distribuzione {
    fun iniziale(): StatoDistribuzione                                 // Passaggio(0)
    fun avanza(stato: StatoDistribuzione, numeroGiocatori: Int): StatoDistribuzione  // Passaggio(k)->Rivelazione(k)->Passaggio(k+1)|Gioco; Gioco->Gioco
    fun interrompiRivelazione(stato: StatoDistribuzione): StatoDistribuzione        // Rivelazione(k)->Passaggio(k) (background/rotazione, spec §4.3); altri invariati
}
```

`interrompiRivelazione` riporta a `Passaggio(k)` lo stesso giocatore che stava guardando il proprio ruolo, non un giocatore precedente, quindi non viola CA-18.

## `it.imposteur.data`

```kotlin
object ParserParole {
    fun parse(json: String): RisultatoCaricamento                     // CA-17, kotlinx.serialization, nessun import android.*
}
sealed interface RisultatoCaricamento {
    data class Ok(val categorie: List<Categoria>) : RisultatoCaricamento
    data class Errore(val messaggio: String) : RisultatoCaricamento
}

object SerializzazioneConfigurazione {                                // Kotlin puro, CA-19
    fun aStringa(config: Configurazione): String                      // JSON
    fun daStringa(s: String?, categorie: List<Categoria>): Configurazione  // null/malformata -> default; normalizza ai limiti
}

class RepositoryParole(private val context: android.content.Context) {
    suspend fun carica(): RisultatoCaricamento                        // legge assets/parole.json su Dispatchers.IO
}
class RepositoryConfigurazione(private val context: android.content.Context) {
    suspend fun leggi(categorie: List<Categoria>): Configurazione     // DataStore Preferences, chiave stringa con il JSON
    suspend fun salva(config: Configurazione)
}
```

## Salvataggio della sessione (v1.1)

Le firme esistenti restano invariate. Si aggiunge:

```kotlin
// game
data class ChiaveParola(val categoriaId: String, val parola: String)   // parola normalizzata trim+minuscole

class GestorePartite(
    private val random: kotlin.random.Random,
    usateIniziali: Set<ChiaveParola> = emptySet(),
    ultimaIniziale: ChiaveParola? = null,
) {
    val usate: Set<ChiaveParola>          // copia di sola lettura
    val ultima: ChiaveParola?
    fun nuovaPartita(...)                 // invariata
}

data class SessioneSalvata(
    val partita: Partita?,                // null = nessuna partita in corso (ma parole usate da conservare)
    val stato: StatoDistribuzione?,       // null se partita null
    val usate: Set<ChiaveParola>,
    val ultima: ChiaveParola?,
)

// data (Kotlin puro)
object SerializzazioneSessione {
    fun aStringa(s: SessioneSalvata): String
    /** null/malformata -> SessioneSalvata(null, null, emptySet(), null).
     *  Partita con categoria/parola non più presente in `categorie` -> partita e stato scartati (usate conservate, filtrate sulle chiavi esistenti).
     *  Rivelazione(k) -> Passaggio(k). Stato con indice fuori range -> partita scartata. */
    fun daStringa(s: String?, categorie: List<Categoria>): SessioneSalvata
}

// data (Android)
class RepositorySessione(private val context: android.content.Context) {
    suspend fun leggi(categorie: List<Categoria>): SessioneSalvata
    suspend fun salva(s: SessioneSalvata)       // stesso DataStore della configurazione, chiave distinta
}
```

## Contatore delle parole da giocare (v1.2)

```kotlin
// game, in GestorePartite
/** Numero di voci di `pool` la cui chiave non è in `usate`. */
fun rimanenti(pool: List<VoceParola>): Int
/** Toglie da `usate` le chiavi di `pool`; se `ultima` appartiene a `pool` la mette a null. Le altre usate restano. */
fun azzeraUsate(pool: List<VoceParola>)
```

La UI calcola il pool con `Regole.pool(categorie, config.categorieSelezionate, config.modalita)` e mostra "Parole ancora da giocare: <rimanenti> / <pool.size>". Dopo `azzeraUsate` la sessione va salvata.

Caso limite: a pool esaurito `rimanenti` vale 0. La partita successiva azzera da sola le usate del pool (regola §5.2.3), quindi la UI mostra 0 senza errori.

## Ordine di parola (v1.5)

```kotlin
// game, in Partita
/** Indici dei giocatori nell'ordine in cui parlano: da primoGiocatore in poi, seguendo la lista e ripartendo dall'inizio.
 *  Size == giocatori.size, contiene ogni indice una volta, il primo elemento è primoGiocatore. */
fun ordineDiParola(): List<Int>
```

## Segnalazioni (v1.3)

```kotlin
// data/Segnalazioni.kt (Kotlin puro)
enum class MotivoSegnalazione { TROPPO_SIMILI, TROPPO_DIVERSE, POCO_CONOSCIUTA, CATEGORIA_SBAGLIATA }
data class Segnalazione(
    val tipo: String,                     // "coppia" | "app"
    val istante: String,                  // ISO-8601 locale, es. "2026-10-08T21:14:03"
    val categoriaId: String? = null,      // solo tipo "coppia"
    val parola: String? = null,
    val affine: String? = null,
    val modalita: String? = null,         // Modalita.name
    val motivi: List<MotivoSegnalazione> = emptyList(),
    val nota: String = "",                // trim, max 500 caratteri
    val propostaParola: String? = null,   // v1.4: coppia proposta, trim, max 40 caratteri, "" -> null
    val propostaAffine: String? = null,   // v1.4: idem; valida solo se entrambe presenti e diverse (senza distinzione maiuscole/minuscole)
)
object FormatoSegnalazioni {
    fun riga(s: Segnalazione): String                 // una riga JSON, senza a capo
    fun leggi(testo: String): List<Segnalazione>      // JSONL; le righe malformate vengono saltate
}

// data/RepositorySegnalazioni.kt (Android)
class RepositorySegnalazioni(context: Context) {
    // File: context.getExternalFilesDir(null) ?: context.filesDir, nome "segnalazioni.jsonl"
    suspend fun aggiungi(s: Segnalazione)             // append + a capo, Dispatchers.IO
    suspend fun conta(): Int
    suspend fun cancellaTutte()
}
```

Una segnalazione di tipo "coppia" senza motivi, nota o coppia proposta valida non si salva: il pulsante Salva resta disattivato. Una coppia proposta con un solo campo compilato, o con due parole uguali, blocca il salvataggio e mostra un avviso.

v1.4: `FormatoSegnalazioni.leggi` deve leggere anche le righe salvate prima della v1.4 (campi di proposta assenti, quindi null). `FormatoSegnalazioni.normalizza(s)` applica trim e limiti a nota e proposte; `riga()` la usa. Aggiungi anche `fun propostaValida(s: Segnalazione): Boolean`.

## `it.imposteur.ui` (UI: la scrive solo l'agente dedicato)

`ImpostoreViewModel` (AndroidViewModel) espone `StateFlow<UiState>` e possiede un unico `GestorePartite(Random.Default)`. Navigazione Compose con le rotte: home, regole, configurazione, distribuzione, gioco, rivela.
