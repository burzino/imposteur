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

## Distribuzione nell'ordine di parola (v1.9 Kotlin, v2.4 TS)

La distribuzione dei ruoli segue `ordineDiParola()` (specifiche 4.3, 5.6): `StatoDistribuzione.Passaggio(k)` / `Rivelazione(k)` hanno `k` = posizione nella sequenza (0..N-1), non indice del giocatore. `Distribuzione.iniziale/avanza/interrompiRivelazione` NON cambiano (lavorano su posizioni, CA-18, CA-121). Il formato salvato della sessione non cambia (`indice` = posizione).

```kotlin
// game, in Partita
/** Indice del giocatore (in `giocatori`) alla posizione k della sequenza: ordineDiParola()[k].
 *  k fuori da 0..giocatori.size-1 -> IllegalArgumentException. */
fun giocatoreAlPasso(k: Int): Int
```
```ts
// game/partita.ts
export function giocatoreAlPasso(p: Partita, k: number): number;   // ordineDiParola(p)[k]; k intero fuori 0..N-1 (o non intero) -> RangeError
```

Uso nella UI (Android e web): in Distribuzione `val g = partita.giocatoreAlPasso(k)`; nome = `giocatori[g]`, ruolo = `contenutoPer(partita, g)` (l'argomento resta l'indice del giocatore, non la posizione), iniziale e colore dell'avatar del giocatore `g` (il colore segue il giocatore, non la posizione); "Giocatore k+1 di N"; fila di avatar = `ordineDiParola()` mappata su `giocatori`; l'ultimo passo (`k == N-1`) mostra "Nascondi e inizia". Rivedi la parola: elenco = `ordineDiParola()` mappata su `giocatori`, il tocco passa l'indice giocatore `g` (non la riga).

| Funzione | CA verificati | Casi |
|---|---|---|
| `giocatoreAlPasso` | CA-119, 120, 121 | `ordine` = [0,2,1], k = 0,1,2 -> 0,2,1; `ordine` null, N = 4, `primoGiocatore` = 2 -> k 0..3 = 2,3,0,1; `primoGiocatore` = 0 senza ordine -> identità (comportamento di prima); N = 3 con `ordine` casuale [1,0,2] -> 1,0,2; k = 0 è sempre `primoGiocatore`; l'insieme dei risultati su 0..N-1 è una permutazione (ogni giocatore una volta); k = -1 e k = N -> errore (IllegalArgumentException / RangeError); N = 20 stesso risultato di `ordineDiParola()[k]` |
| `contenutoPer` con `giocatoreAlPasso` | CA-120 | per `ordine` = [0,2,1] e impostore il giocatore 2: il Passaggio/Rivelazione k = 1 mostra il contenuto da impostore con il nome del giocatore 2 |

## Opzioni avanzate (v1.6)

Tutte disattivate per default, così la partita base resta identica a prima.

```kotlin
// game, nuovi campi di Configurazione (con default: le configurazioni salvate prima restano leggibili)
data class Configurazione(
    ...esistenti...,
    val impostoreNonPrimo: Boolean = false,       // 1. chi parla per primo è sempre un civile
    val impostoriSorpresa: Boolean = false,       // 2. K effettivo uniforme in 1..numeroImpostori (numeroImpostori diventa il massimo)
    val ordineCasuale: Boolean = false,           // 3. ordine di parola mescolato invece che a giro
    val partitaTrappola: Boolean = false,         // 4. con probabilità Regole.PROBABILITA_TRAPPOLA nessuno è impostore
    val promemoriaUltimaPossibilita: Boolean = false, // 5. solo testo nella schermata finale
    val giriIndizi: Int = 1,                      // 6. 1..3; solo visualizzazione dell'ordine ripetuto per giro
)
object Regole { const val PROBABILITA_TRAPPOLA = 0.10; const val MAX_GIRI = 3 }

// game, Partita: nuovi campi
data class Partita(
    ...esistenti...,                              // impostori può ora essere VUOTO (partita trappola)
    val ordine: List<Int>? = null,                // null = rotazione da primoGiocatore (calcolata); valorizzato solo con ordine casuale
    val giriIndizi: Int = 1,
    val promemoriaUltimaPossibilita: Boolean = false,
) {
    fun ordineDiParola(): List<Int> = ordine ?: rotazione da primoGiocatore
    val trappola: Boolean get() = impostori.isEmpty()
}
```

Algoritmo di `GestorePartite.nuovaPartita`, nell'ordine e sempre con il `Random` iniettato:
1. Trappola: se `partitaTrappola` e `random.nextDouble() < PROBABILITA_TRAPPOLA`, allora K = 0.
2. Altrimenti K = `impostoriSorpresa ? random.nextInt(1, numeroImpostori + 1) : numeroImpostori`.
3. Gli impostori sono K indici distinti, scelti in modo uniforme.
4. Primo giocatore: se `impostoreNonPrimo` e K > 0, uniforme tra i civili; altrimenti uniforme tra tutti.
5. Ordine: se `ordineCasuale`, [primo] + una permutazione casuale degli altri; altrimenti la rotazione da primo.
6. `giriIndizi` (limitato a 1..3) e `promemoriaUltimaPossibilita` vengono copiati dalla configurazione nella partita.

Con K = 0 ogni giocatore riceve la parola dei civili, in entrambe le modalità.

`TestiGioco`: con K = 0 lo svelamento è "Nessun impostore: era una partita trappola!", seguito dalla parola (e dall'affine in modalità affine) e dalla categoria. Il promemoria, mostrato solo se attivo e K > 0, è "L'impostore scoperto può provare a indovinare la parola: se ci riesce, vince lui!".

Serializzazione:
- Configurazione: i campi mancanti prendono il default; `giriIndizi` viene limitato a 1..3.
- Sessione: `ordine`, `giriIndizi` e `promemoria` vengono salvati. Se `ordine` manca (salvataggi vecchi), si usa la rotazione. Un `ordine` che non è una permutazione di 0..N-1 o non inizia con `primoGiocatore` fa scartare la partita.
- Impostori vuoti: ora sono validi, perché indicano una partita trappola.

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

## Durata della pressione (v1.8 Kotlin, v2.3 TS)

Decisione utente 2026-10-09: la durata della pressione lunga di "Tieni premuto per scoprire" (Distribuzione e Rivedi) si sceglie nelle Impostazioni. Specifiche 4.7, 4.3, 6; CA-106…CA-112, CA-W20, CA-W21.

Logica pura (nessun import `android.*`), `it.imposteur.game`:

```kotlin
object DurataPressione {
    const val MIN_MS = 0
    const val MAX_MS = 1000
    const val PASSO_MS = 50
    const val PREDEFINITA_MS = 150
    /** Riporta al valore valido più vicino: clamp a 0..1000, poi multiplo di 50 più vicino (metà strada per eccesso: 125 -> 150). Mai eccezioni. CA-106 */
    fun normalizza(valore: Int): Int
    /** Testo -> valore valido. null, vuoto, non numerico, NaN, infinito -> PREDEFINITA_MS; decimali ("149.6") e notazione esponenziale ("1e9") accettati e poi normalizzati; spazi ai lati ignorati. CA-106 */
    fun daTesto(s: String?): Int
    /** true se ms == 0: nessuna barra, rivelazione al rilascio, testi "Tocca per scoprire". CA-107 */
    fun soloTocco(ms: Int): Boolean
}
```

Aspetto Android (`it.imposteur.data`, `Aspetto.kt`, stesso DataStore di tema e configurazione, chiavi distinte):

```kotlin
data class Aspetto(
    val tema: Tema = Tema.SISTEMA,
    val coloriDinamici: Boolean = true,
    val durataPressioneMs: Int = DurataPressione.PREDEFINITA_MS,   // sempre già normalizzata
)
class RepositoryAspetto(private val context: Context) {
    // chiavi: "aspetto_tema" (string), "aspetto_colori_dinamici" (boolean), NUOVA "aspetto_durata_pressione_ms" (stringPreferencesKey, testo decimale)
    val aspetto: Flow<Aspetto>              // la durata è letta con DurataPressione.daTesto(p[chiave]): assente/illeggibile -> 150; una chiave illeggibile non azzera tema né colori
    suspend fun salva(a: Aspetto)           // scrive DurataPressione.normalizza(a.durataPressioneMs) come testo; subito a ogni cambio
}
// ImpostoreViewModel:
fun impostaDurataPressione(ms: Int)        // = impostaAspetto(aspetto.value.copy(durataPressioneMs = DurataPressione.normalizza(ms)))
```

Il tema continua a leggersi e scriversi con le stesse chiavi di prima; i salvataggi precedenti (senza la nuova chiave) danno 150.

TypeScript, `web/src/game/durataPressione.ts`:

```ts
export const DURATA_PRESSIONE: { readonly min: 0; readonly max: 1000; readonly passo: 50; readonly predefinita: 150 };
/** Stesso esito di DurataPressione.normalizza. number finito: clamp + multiplo di 50 più vicino (metà strada per eccesso); NaN, ±Infinity, string, null, undefined, boolean, oggetti -> 150. */
export function normalizzaDurataPressione(valore: unknown): number;
/** Stesso esito di DurataPressione.daTesto: string|null -> Number(trim) finito -> normalizza; altrimenti 150. */
export function durataPressioneDaTesto(s: string | null): number;
export function soloTocco(ms: number): boolean;           // ms === 0
```

Casi limite comuni (JUnit e Vitest identici): 0→0; 1000→1000; 150→150; 125→150; 124→100; 25→50; 24→0; -30→0; 1049→1000; 5000→1000; decimale 149.6→150; "200"→200; " 200 "→200; ""/"abc"/"NaN"/"Infinity"/null→150; "1e9"→1000. Il risultato è sempre multiplo di 50 in 0..1000.

UI: la durata si legge da `aspetto.durataPressioneMs` (Android: `viewModel.aspetto`; web: `stato.aspetto`); le schermate Distribuzione e Rivedi non leggono la chiave direttamente. Durata della barra = durata; con `soloTocco` la barra non viene disegnata. Il cursore delle Impostazioni chiama `impostaDurataPressione` / `impostaAspetto({ ...stato.aspetto, durataPressioneMs })`, che normalizza e salva.

## `it.imposteur.ui` (UI: la scrive solo l'agente dedicato)

`ImpostoreViewModel` (AndroidViewModel) espone `StateFlow<UiState>` e possiede un unico `GestorePartite(Random.Default)`. Navigazione Compose con le rotte: home, regole, configurazione, distribuzione, gioco, rivela.

## PWA: `web/src/game` e `web/src/data` (TypeScript, v2.0)

Porting 1:1 di `game/` e `data/` Kotlin. Le firme Kotlin e le specifiche restano la fonte di verità: dove qui non è detto altro, il comportamento è identico (stessi algoritmi, stesso ordine di chiamata a `Casuale`, stessi testi). Regole comuni:

- TS puro, `strict`, nessun DOM né Svelte in `game/` e `data/` (solo `archivio.ts` riceve lo storage per iniezione).
- Mappatura: data class → `interface` con campi `readonly`; sealed → unione discriminata con campo `tipo`; enum → unione di stringhe letterali (stessi nomi del Kotlin, es. `"SENZA_PAROLA"`); `Set<T>` → `ReadonlySet<T>`; `List<T>` → `readonly T[]`; `T?` → `T | null` (mai `undefined` nei modelli). Le `copy` Kotlin diventano oggetti nuovi (`{ ...c, campo }`): nessuna mutazione dei modelli.
- Niente eccezioni di validazione: dove Kotlin restituisce un risultato, TS restituisce un risultato.
- Il JSON salvato ha gli stessi nomi di campo e le stesse regole di normalizzazione del Kotlin (§6 delle specifiche), così un salvataggio Android e uno web sono interscambiabili.

### `web/src/game/casuale.ts`

```ts
export interface Casuale {
  /** Intero uniforme con 0 <= x < limite. limite >= 1 (intero). */
  intero(limite: number): number;
}
export function casualeConSeme(seme: number): Casuale;  // deterministica (mulberry32), per i test
export function casualeDiSistema(): Casuale;            // crypto.getRandomValues, senza bias (rejection sampling)

// helper puri sopra Casuale (equivalenti di nextInt(a, b), nextDouble(), shuffled)
export function interoTra(c: Casuale, minInclusivo: number, maxEsclusivo: number): number;
export function reale(c: Casuale): number;                              // [0, 1): UNA chiamata a intero(2^30), diviso 2^30 (risolto: 30 bit bastano per PROBABILITA_TRAPPOLA)
export function mescolato<T>(c: Casuale, v: readonly T[]): T[];         // Fisher-Yates, nuovo array
```

`GestorePartite` usa `reale(c) < PROBABILITA_TRAPPOLA` dove Kotlin usa `nextDouble()`. Gli esiti non sono identici bit a bit a quelli Kotlin (generatori diversi): i test web verificano le proprietà, non sequenze.

### `web/src/game/modelli.ts`

```ts
export type Modalita = "SENZA_PAROLA" | "PAROLA_AFFINE";

export interface Parola { readonly parola: string; readonly affine: string | null }
export interface Categoria { readonly id: string; readonly nome: string; readonly parole: readonly Parola[] }

export interface Configurazione {
  readonly numeroGiocatori: number;
  readonly nomi: readonly string[];
  readonly numeroImpostori: number;
  readonly modalita: Modalita;
  readonly mostraCategoria: boolean;
  readonly categorieSelezionate: ReadonlySet<string>;
  readonly impostoreNonPrimo: boolean;
  readonly impostoriSorpresa: boolean;
  readonly ordineCasuale: boolean;
  readonly partitaTrappola: boolean;
  readonly promemoriaUltimaPossibilita: boolean;
  readonly giriIndizi: number;
}
/** Default del Kotlin: 4 giocatori, nomi [], 1 impostore, SENZA_PAROLA, mostraCategoria true, categorie vuote, opzioni false, giriIndizi 1. */
export function configurazioneDefault(sovrascritture?: Partial<Configurazione>): Configurazione;

export interface VoceParola {
  readonly categoriaId: string; readonly categoriaNome: string;
  readonly parola: string; readonly affine: string | null;
}

export type ErroreConfigurazione =
  | { readonly tipo: "TroppoPochiGiocatori" }
  | { readonly tipo: "TroppiGiocatori" }
  | { readonly tipo: "TroppoPochiImpostori" }
  | { readonly tipo: "TroppiImpostori" }
  | { readonly tipo: "NessunaCategoria" }
  | { readonly tipo: "PoolVuoto" }
  | { readonly tipo: "NomeDuplicato"; readonly indici: readonly number[] }   // 0-based
  | { readonly tipo: "NomeTroppoLungo"; readonly indice: number };

export const TestiGioco: {
  readonly SEI_IMPOSTORE: string; readonly LA_PAROLA_E: string; readonly LA_TUA_PAROLA_E: string;
  readonly NESSUN_IMPOSTORE: string; readonly PROMEMORIA_ULTIMA_POSSIBILITA: string;
  categoria(nome: string): string;
  impostoriSingolare(nome: string): string;
  impostoriPlurale(nomi: readonly string[]): string;
  laParolaEra(parola: string): string;
  laParolaAffineEra(affine: string): string;
};   // stringhe identiche al Kotlin
```

### `web/src/game/regole.ts`

```ts
export const Regole: {
  readonly MIN_GIOCATORI: 3;
  readonly MAX_GIOCATORI: 20;
  readonly MAX_LUNGHEZZA_NOME: 20;
  readonly PROBABILITA_TRAPPOLA: 0.10;
  readonly MAX_GIRI: 3;
  maxImpostori(numeroGiocatori: number): number;                                  // max(0, floor((N-1)/2))
  nomiEffettivi(config: Configurazione): string[];                                // trim + "Giocatore n", lunghezza N
  valida(config: Configurazione, categorie: readonly Categoria[]): ErroreConfigurazione[];  // vuota = valida, stesso ordine del Kotlin
  conNumeroGiocatori(config: Configurazione, n: number): Configurazione;         // CA-06
  pool(categorie: readonly Categoria[], selezionate: ReadonlySet<string>, modalita: Modalita): VoceParola[];  // CA-11
};
```

Le costanti sono anche esportate singolarmente (`MIN_GIOCATORI`, ...). I confronti "senza maiuscole" del Kotlin (`lowercase()`, `equals(ignoreCase)`) diventano `toLowerCase()`; la lunghezza dei nomi si misura in unità UTF-16, come `String.length` in Kotlin.

### `web/src/game/partita.ts`

```ts
export type ContenutoRuolo =
  | { readonly tipo: "ParolaSegreta"; readonly testo: string }
  | { readonly tipo: "Impostore"; readonly categoria: string | null };

export interface Partita {
  readonly giocatori: readonly string[];
  readonly impostori: ReadonlySet<number>;      // vuoto = partita trappola
  readonly voce: VoceParola;
  readonly modalita: Modalita;
  readonly mostraCategoria: boolean;
  readonly primoGiocatore: number;
  readonly ordine: readonly number[] | null;    // null = rotazione da primoGiocatore
  readonly giriIndizi: number;
  readonly promemoriaUltimaPossibilita: boolean;
}
export function eTrappola(p: Partita): boolean;                       // impostori.size === 0
export function contenutoPer(p: Partita, indice: number): ContenutoRuolo;   // CA-09, CA-10
export function ordineDiParola(p: Partita): number[];
export function giocatoreAlPasso(p: Partita, k: number): number;       // v2.4, vedi "Distribuzione nell'ordine di parola"
export function testoSvelamento(p: Partita): string;                  // CA-21

export interface ChiaveParola { readonly categoriaId: string; readonly parola: string }  // parola = trim + minuscole
export function chiaveDi(v: VoceParola): ChiaveParola;
export function chiaveStringa(k: ChiaveParola): string;               // JSON.stringify([categoriaId, parola])

export interface SessioneSalvata {
  readonly partita: Partita | null;
  readonly stato: StatoDistribuzione | null;   // null se partita null
  readonly usate: readonly ChiaveParola[];     // senza duplicati (vedi nota)
  readonly ultima: ChiaveParola | null;
}

export type RisultatoNuovaPartita =
  | { readonly tipo: "Ok"; readonly partita: Partita }
  | { readonly tipo: "Errore"; readonly errori: readonly ErroreConfigurazione[] };

export class GestorePartite {
  constructor(casuale: Casuale, usateIniziali?: Iterable<ChiaveParola>, ultimaIniziale?: ChiaveParola | null);
  get usate(): readonly ChiaveParola[];         // copia
  get ultima(): ChiaveParola | null;
  rimanenti(pool: readonly VoceParola[]): number;
  azzeraUsate(pool: readonly VoceParola[]): void;
  nuovaPartita(config: Configurazione, categorie: readonly Categoria[]): RisultatoNuovaPartita;  // stesso algoritmo e ordine di chiamate a Casuale di v1.6
}

export type StatoDistribuzione =
  | { readonly tipo: "Passaggio"; readonly indice: number }
  | { readonly tipo: "Rivelazione"; readonly indice: number }
  | { readonly tipo: "Gioco" };

export const Distribuzione: {
  iniziale(): StatoDistribuzione;
  avanza(stato: StatoDistribuzione, numeroGiocatori: number): StatoDistribuzione;
  interrompiRivelazione(stato: StatoDistribuzione): StatoDistribuzione;
};
```

Uguaglianza delle chiavi: in JS un `Set` di oggetti confronta per riferimento, quindi `usate` è un array senza duplicati (unicità garantita con `chiaveStringa`, usata anche per i `Set<string>` interni del `GestorePartite`). I test confrontano con `chiaveStringa` o con `toEqual` su array ordinati.

### `web/src/data/parserParole.ts`

```ts
export type RisultatoCaricamento =
  | { readonly tipo: "Ok"; readonly categorie: readonly Categoria[] }
  | { readonly tipo: "Errore"; readonly messaggio: string };

export function parseParole(json: string): RisultatoCaricamento;   // CA-17; ignora chiavi sconosciute; versione deve essere 1; stesse regole e messaggi di ParserParole.kt
```

Il file è `app/src/main/assets/parole.json` (nessuna copia); il caricamento (fetch/import) sta fuori da `data/`, nel livello UI.

### `web/src/data/serializzazioneConfigurazione.ts`

```ts
export function configurazioneAStringa(config: Configurazione): string;
/** null/vuota/malformata -> default con tutte le categorie selezionate; normalizza ai limiti come il Kotlin. */
export function configurazioneDaStringa(s: string | null, categorie: readonly Categoria[]): Configurazione;
```

Formato JSON (campi identici al Kotlin, default se mancanti): `numeroGiocatori`, `nomi` (array), `numeroImpostori`, `modalita` (stringa enum), `mostraCategoria`, `categorieSelezionate` (array di id), `impostoreNonPrimo`, `impostoriSorpresa`, `ordineCasuale`, `partitaTrappola`, `promemoriaUltimaPossibilita`, `giriIndizi`. Un campo di tipo errato rende malformato l'intero JSON (come in Kotlin) e dà la configurazione di default.

### `web/src/data/serializzazioneSessione.ts`

```ts
export function sessioneAStringa(s: SessioneSalvata): string;
/** null/malformata -> { partita: null, stato: null, usate: [], ultima: null }. Stesse regole di scarto e di filtro del Kotlin (v1.1, v1.6); Rivelazione(k) -> Passaggio(k). */
export function sessioneDaStringa(s: string | null, categorie: readonly Categoria[]): SessioneSalvata;
```

Formato JSON: `{ "partita": {...}|null, "stato": {...}|null, "usate": [{"categoriaId","parola"}], "ultima": {"categoriaId","parola"}|null }`.
- `partita`: `giocatori`, `impostori` (array ordinato), `categoriaId`, `parola`, `affine` (stringa o null, sempre presente), `modalita`, `mostraCategoria`, `primoGiocatore`, `ordine` (array o null), `giriIndizi`, `promemoria` (= `promemoriaUltimaPossibilita`).
- `stato`: `{ "tipo": "PASSAGGIO" | "RIVELAZIONE" | "GIOCO", "indice": number }` (per GIOCO `indice` vale 0). La conversione verso `StatoDistribuzione` (PascalCase in memoria, maiuscolo nel JSON) sta solo in questo modulo.

### `web/src/data/segnalazioni.ts`

```ts
export type MotivoSegnalazione = "TROPPO_SIMILI" | "TROPPO_DIVERSE" | "POCO_CONOSCIUTA" | "CATEGORIA_SBAGLIATA";

export interface Segnalazione {
  readonly tipo: "coppia" | "app";
  readonly istante: string;                 // ISO-8601 locale
  readonly categoriaId: string | null;
  readonly parola: string | null;
  readonly affine: string | null;
  readonly modalita: Modalita | null;
  readonly motivi: readonly MotivoSegnalazione[];
  readonly nota: string;
  readonly propostaParola: string | null;
  readonly propostaAffine: string | null;
}
export function segnalazione(base: Pick<Segnalazione, "tipo" | "istante"> & Partial<Segnalazione>): Segnalazione;  // default null / [] / ""

export const FormatoSegnalazioni: {
  normalizza(s: Segnalazione): Segnalazione;        // trim e limiti: nota 500, proposte 40, "" -> null
  propostaValida(s: Segnalazione): boolean;
  riga(s: Segnalazione): string;                    // una riga JSON senza a capo; i null sono scritti come null, come in Kotlin
  leggi(testo: string): Segnalazione[];             // JSONL; righe malformate saltate; campi assenti -> default
};
```

Sul web non c'è un file: `archivio.ts` conserva il testo JSONL e l'esportazione (download/condivisione) sta nella UI.

### `web/src/data/aspetto.ts`

```ts
export type Tema = "SISTEMA" | "CHIARO" | "SCURO" | "ALTO_CONTRASTO";
export interface Aspetto { readonly tema: Tema; readonly coloriDinamici: boolean; readonly durataPressioneMs: number }
export const aspettoDefault: Aspetto;                                // { tema: "SISTEMA", coloriDinamici: true, durataPressioneMs: 150 }
export function aspettoAStringa(a: Aspetto): string;                 // {"tema":"...","coloriDinamici":bool,"durataPressioneMs":n}; scrive il valore normalizzato
export function aspettoDaStringa(s: string | null): Aspetto;         // null/malformata/valori sconosciuti -> default per campo; durataPressioneMs via normalizzaDurataPressione
```

Durata della pressione, funzione pura in `web/src/game/durataPressione.ts` (porting di `DurataPressione.kt`, v2.3; vedi "Durata della pressione" sotto).

Su web `coloriDinamici` è conservato ma la UI può ignorarlo (nessun Material You).

### `web/src/data/archivio.ts` (sostituisce `Repository*.kt`)

```ts
/** Sottoinsieme di Storage: in produzione `window.localStorage`, nei test una Map in memoria. */
export interface ArchivioStorage {
  getItem(chiave: string): string | null;
  setItem(chiave: string, valore: string): void;
  removeItem(chiave: string): void;
}
export function storageInMemoria(): ArchivioStorage;   // per i test

export const CHIAVI_ARCHIVIO: {
  readonly configurazione: "impostore.configurazione";
  readonly sessione: "impostore.sessione";
  readonly aspetto: "impostore.aspetto";
  readonly segnalazioni: "impostore.segnalazioni";     // testo JSONL
};

export interface Archivio {
  leggiConfigurazione(categorie: readonly Categoria[]): Configurazione;
  salvaConfigurazione(config: Configurazione): void;
  leggiSessione(categorie: readonly Categoria[]): SessioneSalvata;
  salvaSessione(s: SessioneSalvata): void;
  leggiAspetto(): Aspetto;
  salvaAspetto(a: Aspetto): void;
  aggiungiSegnalazione(s: Segnalazione): void;       // append di riga + "\n"
  leggiSegnalazioni(): Segnalazione[];
  contaSegnalazioni(): number;
  cancellaSegnalazioni(): void;
}
export function creaArchivio(storage: ArchivioStorage): Archivio;
```

Gli errori di storage (accessor che lancia, quota piena, modalità privata) sono assorbiti: le letture danno il valore di default, le scritture non lanciano (come i `catch` dei repository Kotlin). Il caricamento delle parole non fa parte di `Archivio`.

## PWA: UI (`web/src/ui`, v2.1)

Quattro sviluppatori in parallelo; ognuno tocca solo i file che possiede. Svelte 5 (rune), TypeScript, nessuna libreria UI. Le schermate importano solo da `ui/stato.svelte.ts`, `ui/rotte.ts`, `ui/testi.ts`, `ui/componenti/*`, `ui/browser.ts`, `game/`, `data/`. Un cambio a questo contratto si fa prima del codice.

### 1. File e proprietari

```
web/src/App.svelte                       (A)  monta tema, rotta corrente, dialogo di interruzione
web/src/main.ts                          (A)  importa tema.css, avvia stato, rotte, protezione ruolo
web/src/ui/stato.svelte.ts  stato.test.ts (A)  Vitest senza DOM
web/src/ui/rotte.ts  rotte.test.ts       (A)
web/src/ui/testi.ts  testi.test.ts       (A)
web/src/ui/browser.ts                    (A)
web/src/ui/tema.css  tema.ts             (A)
web/src/ui/componenti/*.svelte|*.ts      (A)  quelli del punto 5
web/src/ui/schermate/Home.svelte                  (B)
web/src/ui/schermate/Regole.svelte                (B)
web/src/ui/schermate/Impostazioni.svelte          (B)
web/src/ui/schermate/Configurazione.svelte        (C)
web/src/ui/schermate/OpzioniAvanzate.svelte       (C)
web/src/ui/schermate/Gioco.svelte                 (C)
web/src/ui/schermate/Rivela.svelte                (C)
web/src/ui/schermate/DialogoSegnalazione.svelte   (C)
web/src/ui/schermate/Distribuzione.svelte         (D)
web/src/ui/schermate/Rivedi.svelte                (D)
web/src/ui/schermate/<Nome>*.svelte      sotto-componenti privati: li possiede chi possiede la schermata (prefisso = nome schermata)
```
Ogni schermata è un componente senza props: legge `stato` e chiama `vai`. `App.svelte` (A) sceglie il componente con `rottaCorrente.valore`. Un componente che serve a due schermate di sviluppatori diversi si chiede ad A. I test di schermata (se servono) stanno accanto al file e usano uno stato iniettato (punto 2).

### 2. Stato (`ui/stato.svelte.ts`, porting di `ImpostoreViewModel`)

```ts
export interface Revisione { readonly indice: number; readonly rivelato: boolean }

export interface Dipendenze {
  archivio: Archivio;                       // data/archivio.ts
  casuale: Casuale;                         // game/casuale.ts
  caricaCategorie: () => Promise<RisultatoCaricamento>;   // default: fetch(`${import.meta.env.BASE_URL}parole.json`) + parseParole
  ritardoSalvataggioMs?: number;            // default 500 (debounce della configurazione); 0 nei test
}

export class StatoApp {
  constructor(dip: Dipendenze);
  readonly pronto: Promise<void>;           // si risolve a caricamento finito (per i test)

  // Campi leggibili ($state / $derived; non si assegnano dall'esterno)
  readonly caricamento: boolean;            // true finché parole.json non è caricato
  readonly erroreCaricamento: boolean;
  readonly categorie: readonly Categoria[];
  readonly config: Configurazione;
  readonly partita: Partita | null;
  readonly distribuzione: StatoDistribuzione;      // Distribuzione.iniziale() se non c'è partita
  readonly ripristinabile: SessioneSalvata | null; // offerta da "Riprendi partita"
  readonly paroleRimanenti: number;
  readonly paroleTotali: number;
  readonly revisione: Revisione | null;            // locale, mai salvata
  readonly aspetto: Aspetto;
  readonly segnalazioniSalvate: number;
  readonly errori: readonly ErroreConfigurazione[];  // [] se categorie vuote, altrimenti Regole.valida
  readonly puoIniziare: boolean;                     // !caricamento && !erroreCaricamento && errori.length === 0

  // Aspetto e segnalazioni
  impostaAspetto(a: Aspetto): void;                // salva e chiama applicaTema
  salvaSegnalazione(s: Segnalazione): void;
  cancellaSegnalazioni(): void;
  leggiSegnalazioniJsonl(): string;                // contenuto del file, ogni riga terminata da "\n"

  // Configurazione (ignorate se caricamento; salvataggio con debounce)
  impostaNumeroGiocatori(n: number): void;
  impostaNumeroImpostori(n: number): void;
  impostaNome(indice: number, testo: string): void;
  impostaModalita(m: Modalita): void;
  impostaOpzione(trasforma: (c: Configurazione) => Configurazione): void;
  impostaMostraCategoria(v: boolean): void;
  impostaCategoria(id: string, selezionata: boolean): void;
  selezionaTutte(tutte: boolean): void;
  azzeraParole(): void;
  salvaOra(): void;                                // flush immediato (Inizia, indietro, pagina nascosta)

  // Partita
  iniziaPartita(): boolean;
  avanza(da: StatoDistribuzione): void;            // ignora se distribuzione !== da (confronto per tipo e indice)
  interrompiRivelazione(): void;
  scegliRevisione(indice: number): void;
  rivelaRevisione(): void;
  interrompiRevisione(): void;
  tornaAElencoRevisione(): void;
  chiudiRevisione(): void;
  entraInRivela(): void;
  terminaPartita(): void;
  sospendiPartita(): void;
  riprendiPartita(): boolean;                      // true se si va in Gioco
  nascondiRuolo(): void;                           // pagina nascosta: interrompiRivelazione() + interrompiRevisione() + salvaOra()
}

export function creaStatoPredefinito(): StatoApp;  // archivio su localStorage, casualeDiSistema()
export const stato: StatoApp;                      // singleton usato da App e schermate
```
Semantica identica al ViewModel Kotlin (stesse condizioni, stessi ritorni, `persisti()` a ogni cambio di sessione, `partitaAttiva`, `GestorePartite` creato al caricamento con `usate/ultima` della sessione). Differenze: nessuna coroutine (debounce con `setTimeout`), `aspetto.coloriDinamici` resta nel dato ma la UI lo ignora, `aspetto` si legge dall'archivio subito (sincrono). Gli oggetti di stato sono immutabili: le azioni sostituiscono, non mutano. In Rivedi, `visibilitychange` porta al passaggio (`interrompiRevisione`, W3); le schermate non ascoltano `visibilitychange` da sé. Per i test della UI, `stato` va costruito con `new StatoApp(...)` e passato via contesto Svelte `setContext("stato", s)`; le schermate lo leggono con `getStato()` esportato da `stato.svelte.ts` (default: il singleton).

### 3. Rotte (`ui/rotte.ts`, porting di `ImpostoreNavHost`)

```ts
export type Rotta = "home" | "regole" | "impostazioni" | "configurazione" | "distribuzione" | "gioco" | "rivedi" | "rivela";
export const rottaCorrente: { readonly valore: Rotta };       // $state, derivata dall'hash
export const richiestaInterruzione: { aperta: boolean };      // $state: dialogo "Interrompere la partita?" (lo mostra App.svelte)
export function vai(rotta: Rotta, opz?: { sostituisci?: boolean }): void;   // push in history, o replace
export function vaiAHome(): void;                  // dopo, "indietro" non riporta alla schermata lasciata
export function vaiAConfigurazione(): void;        // Home sotto, Configurazione sopra (popUpTo Home)
export function indietro(): void;                  // freccia in app: equivale al tasto indietro del browser
export function avviaRotte(s: StatoApp, env?: AmbienteRotte): () => void;   // hashchange/popstate; restituisce la rimozione. `env` finto per i test (location, history)
```
Hash `#/home` ecc.; vuoto o sconosciuto = `home`. Regole:
- Avvio: dopo `s.pronto`, se la rotta è distribuzione/gioco/rivedi/rivela e `s.partita === null` -> `vai("home", {sostituisci:true})` (D1 Kotlin). Dopo un ricaricamento la partita non è attiva: si finisce in Home, dove "Riprendi partita" la offre.
- `vai(r)` verso la rotta già corrente è un no-op (ignora i doppi tocchi).
- Tasto indietro del browser (popstate) in `distribuzione` o `gioco` verso una voce non provocata da `vai`: l'app ripristina la voce e imposta `richiestaInterruzione.aperta = true`; mai si torna a un ruolo precedente. "Interrompi" -> `terminaPartita()` + `vaiAConfigurazione()`; "Continua a giocare" chiude il dialogo. Il pulsante indietro in app di Distribuzione e Gioco apre lo stesso dialogo (`richiestaInterruzione.aperta = true`), non ne duplica uno.
- `rivedi`: indietro -> `chiudiRevisione()` poi history normale. `configurazione`: indietro -> `salvaOra()` poi Home. `rivela`, `regole`, `impostazioni`: history normale. `home`: indietro esce dal sito (nessuna azione, W9).
- Azioni (equivalgono alle lambda del NavHost, le scrivono le schermate): Home "Nuova partita" -> `vai("configurazione")`; "Riprendi" -> `const g = riprendiPartita(); vai(g ? "gioco" : "distribuzione")` solo se `ripristinabile`; Configurazione "Inizia" -> `if (iniziaPartita()) vai("distribuzione")`; fine Distribuzione -> `vai("gioco", {sostituisci:true})`; Gioco "Rivela" -> `entraInRivela()`, `vai("rivela", {sostituisci:true})`; Gioco "Rivedi" -> `chiudiRevisione()`, `vai("rivedi")`; Home-in-barra in Distribuzione/Gioco/Rivedi -> `sospendiPartita()` + `vaiAHome()`; in Configurazione -> `salvaOra()` + `vaiAHome()`; in Regole/Impostazioni/Rivela -> `vaiAHome()`; Rivela "Nuova partita" -> `iniziaPartita() ? vai("distribuzione", {sostituisci:true}) : vaiAConfigurazione()`; Rivela "Cambia impostazioni" e "Interrompi" in Gioco -> `terminaPartita()` + `vaiAConfigurazione()`.

### 4. Testi (`ui/testi.ts`)

`export const t = { ... }` con TUTTE le chiavi di `strings.xml`, stesso testo (apostrofi senza backslash, `\n` reali).
- Chiave: nome Android in camelCase (`config_numero_giocatori` -> `configNumeroGiocatori`, `app_name` -> `appName`, `torna_home` -> `tornaHome`); i numeri restano attaccati (`opz_x2` -> `opzX2`).
- Stringa senza parametri: proprietà `string`. Con segnaposto (`%1$d`, `%2$s`): funzione con parametri posizionali nello stesso ordine (`configGiocatoreN: (n: number) => string`).
- `<plurals>`: funzione `(n: number) => string` (`opzAttive(n)`), con `Intl.PluralRules("it")`; `<string-array>`: `readonly string[]`.
- Testi della durata della pressione (v2.3; anche in `strings.xml`): `distribuzioneTocca` ("Tocca per scoprire"), `distribuzioneSonoTocca(nome)` ("Sono <nome> — tocca"), `impostazioniPressioneTitolo` ("Pressione per scoprire"), `impostazioniPressioneEtichetta` ("Durata della pressione"), `impostazioniPressioneDescrizione` ("Quanto tempo tenere premuto per scoprire il ruolo. Con 0 basta un tocco."), `impostazioniPressioneValore(ms)` ("<ms> ms"), `impostazioniPressioneA11y(ms)` ("<ms> millisecondi"), `impostazioniPressioneA11yZero` ("0 millisecondi, basta un tocco").
- Testi nuovi del web, aggiunti da A: `esportaSegnalazioni` ("Esporta segnalazioni"), `condividiSegnalazioni` ("Condividi"), `segnalazioniSalvateN(n)` ("1 segnalazione salvata" / "n segnalazioni salvate"), `installaIos` ("Per installare l'app su iPhone: tocca Condividi, poi Aggiungi a Home.").
- `testi.test.ts` (A) legge `app/src/main/res/values/strings.xml` e verifica che ogni `<string>`, `<plurals>` e `<string-array>` abbia la chiave corrispondente.
- Un testo mancante si chiede ad A; nessuna stringa italiana letterale nelle schermate.

### 5. Componenti comuni (`ui/componenti/`, A)

Props con `$props()`, eventi come props-funzione `onXxx`, contenuto come `Snippet`.
- `BarraApp.svelte`: `{ titolo: string; onHome?: () => void; onIndietro?: () => void }`. Freccia indietro (`t.indietro`), titolo, tasto Home (`t.tornaHome`), come `ScaffoldConHome`. Senza `onHome` niente tasto.
- `Pagina.svelte`: `{ titolo: string; onHome?; onIndietro?; children: Snippet; piede?: Snippet }`. `BarraApp` + colonna centrata (max `--larghezza-max`), area scorrevole, `piede` fisso in basso.
- `Pulsante.svelte`: `{ variante?: "pieno" | "tonale" | "contorno" | "testo"; disabilitato?: boolean; onClick: () => void; children: Snippet; ariaLabel?: string }`. Altezza minima `--altezza-tocco`.
- `DialogoConferma.svelte`: `{ aperto: boolean; titolo: string; messaggio?: string; etichettaSi: string; etichettaNo: string; onSi: () => void; onNo: () => void }`. `<dialog>` modale; Esc e sfondo = `onNo`; focus iniziale su No; mai etichette Sì/No.
- `CampoTesto.svelte`: `{ valore: string; onCambia: (v: string) => void; etichetta: string; segnaposto?: string; maxLunghezza?: number; errore?: string; multilinea?: boolean; righe?: number }`. Valore controllato, font >= 16 px (niente zoom su iOS).
- `Interruttore.svelte`: `{ valore: boolean; onCambia: (v: boolean) => void; etichetta: string; descrizione?: string; disabilitato?: boolean }`.
- `Selettore.svelte` (segmenti/radio): `{ opzioni: { valore: string; etichetta: string }[]; valore: string; onCambia: (v: string) => void; etichetta?: string }`.
- `Cursore.svelte` (nuovo, v2.3): `{ valore: number; min: number; max: number; passo: number; onCambia: (v: number) => void; etichetta: string; testoValore: string; testoAccessibile: string; descrizione?: string }`. Un `<input type="range">` nativo (frecce, Home, End; `aria-valuetext={testoAccessibile}`), etichetta associata, `onCambia` a ogni `input`.
- `Contatore.svelte` (-/+): `{ valore: number; min: number; max: number; onCambia: (n: number) => void; etichetta: string }`.
- `Fisarmonica.svelte`: `{ titolo: string; aperta: boolean; onCambia: (a: boolean) => void; children: Snippet }`.
- `TestoAdattivo.svelte`: `{ testo: string; classe?: string; maxRighe?: number /* 2 */ }`: riduce del 10% a passo fino al 40% finché sta in larghezza e righe e non spezza parole.
- `Avatar.svelte`: `{ nome: string; indice: number; stato?: "attesa" | "corrente" | "fatto"; dimensione?: number }`; colore da `--colore-avatar-{indice % 8}`.
- `Toast.svelte` (montato da `App.svelte`) e `mostraToast(testo: string)` esportata da `componenti/notifiche.svelte.ts` (nome diverso da `Toast.svelte` non solo per le maiuscole: su Windows gli import si confondono).

### 6. Tema (`ui/tema.css`, `ui/tema.ts`; porting di `Theme.kt`)

`<html data-tema="sistema|chiaro|scuro|alto-contrasto">`. `tema.ts` esporta `applicaTema(t: Tema): void` (imposta `data-tema`, aggiorna `<meta name="theme-color">` con `--colore-sfondo` risolto) e `temaScuroAttivo(t: Tema): boolean`. `StatoApp` lo chiama all'avvio e in `impostaAspetto`; un `<script>` in `index.html` (A) lo imposta prima del primo paint leggendo l'archivio. Selettori: `:root, :root[data-tema="chiaro"]` (chiaro); `:root[data-tema="scuro"]`; `:root[data-tema="alto-contrasto"]`; per `sistema`, le variabili scure dentro `@media (prefers-color-scheme: dark) { :root[data-tema="sistema"] { ... } }`. `color-scheme` coerente.

Variabili `--colore-...`: `primario`, `su-primario`, `contenitore-primario`, `su-contenitore-primario`, `secondario`, `su-secondario`, `contenitore-secondario`, `su-contenitore-secondario`, `terziario`, `su-terziario`, `sfondo`, `su-sfondo`, `superficie`, `su-superficie`, `superficie-variante`, `su-superficie-variante`, `contorno`, `contorno-variante`, `errore`, `su-errore`, `contenitore-errore`, `su-contenitore-errore`, `avatar-0` .. `avatar-7`.
Valori: chiaro e scuro come `LightColors`/`DarkColors` di Theme.kt (primario #3F2B96 / #CBBEFF, contenitore #E6DEFF / #4A3AA8, secondario #625B71 / #CCC2DC, terziario #7D5260 / #EFB8C8; gli altri dalla palette Material 3 di base); alto contrasto come `AltoContrastoColors` (sfondo e superfici #000, testo #FFF, primario/secondario/terziario/contenitore primario #FFD600 con "su" nero, contorni #FFF, errore #FF8A80). `--scala-testo`: 1 (1.15 in alto contrasto), applicata a `html { font-size: calc(100% * var(--scala-testo)) }`; dimensioni in `rem`.
Altre variabili: `--spazio-1..6` (4, 8, 12, 16, 24, 32 px), `--raggio-s|m|l`, `--larghezza-max: 480px`, `--altezza-tocco: 48px`. Colori dinamici assenti sul web: B non mostra l'opzione. Le schermate non usano colori letterali, solo variabili.

### 7. Servizi del browser (`ui/browser.ts`, A)

```ts
export function richiediSchermoAcceso(): Promise<void>;   // navigator.wakeLock.request("screen"); no-op se manca o rifiuta
export function rilasciaSchermoAcceso(): void;            // idempotente
export function vibra(ms?: number): void;                 // default 30; navigator.vibrate se esiste, altrimenti nulla; mai eccezioni
export function avviaProtezioneRuolo(s: StatoApp): () => void;   // visibilitychange hidden -> s.nascondiRuolo(); visible -> ri-richiede il wake lock se qualcuno lo aveva chiesto; pagehide -> s.salvaOra(). Restituisce la rimozione
export function esportaSegnalazioni(jsonl: string, modo: "scarica" | "condividi"): Promise<"scaricato" | "condiviso" | "annullato">;
export function condivisioneFileDisponibile(): boolean;   // navigator.canShare({ files: [file di prova] })
export function eIosSafariNonInstallato(): boolean;       // iOS Safari e non standalone (display-mode / navigator.standalone)
```
- Wake lock: D chiama `richiediSchermoAcceso()` all'ingresso in Distribuzione e Rivedi e `rilascia...` all'uscita (onMount/onDestroy). Il contatore interno gestisce richieste annidate.
- `vibra`: D la chiama alla comparsa del ruolo e al passaggio, sempre con la stessa durata (non deve distinguere i ruoli).
- `esportaSegnalazioni`: `Blob([jsonl], { type: "application/x-ndjson" })`, file `segnalazioni.jsonl`; "scarica" con `<a download>` + `URL.createObjectURL` (revoca dopo); "condividi" con `navigator.share({ files })`, `AbortError` = "annullato". Non cancella le segnalazioni.
- B mostra in Impostazioni "Esporta segnalazioni" (e "Condividi" se `condivisioneFileDisponibile()`) solo se `stato.segnalazioniSalvate > 0`, con il contatore e il pulsante di cancellazione già presente su Android. B mostra `t.installaIos` come ultima riga di "Come si gioca" (Regole) solo se `eIosSafariNonInstallato()`. C salva la segnalazione da Rivela con `stato.salvaSegnalazione`.
- Ogni funzione è in `try/catch` e degrada senza `window`/`navigator` (test in Node).

## Configurazione in 5 passi (v1.9 Kotlin, v2.4 TS; prima 4 passi, v1.7 / v2.2)

Solo funzioni pure, senza stato di navigazione (il passo corrente non è salvato, specifiche 4.2). Stesse firme e stessa semantica in `game/` (Kotlin, file `Passi.kt`) e in `web/src/game/passi.ts` (TS, unioni di stringhe letterali con gli stessi nomi, `T?` come `T | null`). Nessuna modifica a `Configurazione` né al formato salvato (`numeroGiocatori` = numero di campi del passo 1).

```kotlin
enum class PassoConfigurazione { GIOCATORI, MODALITA, OPZIONI, CATEGORIE, RIEPILOGO }    // ordinale + 1 = numero del passo (1..5)

object Passi {
    /** Errore del passo, o null se valido. Riusa Regole.valida; ritorna il PRIMO errore di Regole.valida appartenente al passo.
     *  GIOCATORI: TroppoPochiGiocatori, TroppiGiocatori, NomeDuplicato, NomeTroppoLungo.
     *  MODALITA: TroppoPochiImpostori, TroppiImpostori (raggiungibili solo con dati incoerenti: la UI li impedisce).
     *  OPZIONI e RIEPILOGO: sempre null (nessuna validazione aggiuntiva, 4.2.1).
     *  CATEGORIE: NessunaCategoria, PoolVuoto. */
    fun errorePasso(passo: PassoConfigurazione, config: Configurazione, categorie: List<Categoria>): ErroreConfigurazione?

    /** Primo passo, in ordine GIOCATORI, MODALITA, OPZIONI, CATEGORIE, con errorePasso != null; null se la configurazione è valida
     *  (equivale a Regole.valida(...).isEmpty()). Usato da "Inizia" in barra (4.2). */
    fun primoPassoNonValido(config: Configurazione, categorie: List<Categoria>): PassoConfigurazione?

    /** Aggiunge un giocatore in fondo (passo 1, "+ Aggiungi giocatore").
     *  Se config.numeroGiocatori >= Regole.MAX_GIOCATORI restituisce config invariata.
     *  Altrimenti: nomi = primi numeroGiocatori nomi (eventuali nomi oltre N scartati) completati con "" fino a N, più "" in coda;
     *  numeroGiocatori + 1; numeroImpostori invariato (il massimo cresce o resta). */
    fun aggiungiGiocatore(config: Configurazione): Configurazione

    /** Rimuove il giocatore alla posizione `indice` (0-based; "x" del campo).
     *  Se numeroGiocatori <= Regole.MIN_GIOCATORI o indice fuori da 0..numeroGiocatori-1 restituisce config invariata.
     *  Altrimenti: nomi completati con "" fino a N, tolto l'elemento `indice` (i successivi scalano, i vuoti restano vuoti e
     *  quindi seguono la nuova posizione, Regole.nomiEffettivi); numeroGiocatori - 1;
     *  numeroImpostori = min(numeroImpostori, Regole.maxImpostori(N - 1)). */
    fun rimuoviGiocatore(config: Configurazione, indice: Int): Configurazione

    /** Per la UI: numeroGiocatori < MAX (abilita "+ Aggiungi giocatore") / numeroGiocatori > MIN (mostra la "x"). */
    fun puoAggiungereGiocatore(config: Configurazione): Boolean
    fun puoRimuovereGiocatore(config: Configurazione): Boolean

    /** Riepilogo come dati: nessuna stringa localizzata, la UI sceglie i testi. */
    fun riepilogo(config: Configurazione, categorie: List<Categoria>): RiepilogoConfigurazione

    /** Badge "N attive" (CA-81). */
    fun contaOpzioniAttive(config: Configurazione): Int
}

enum class OpzioneRiepilogo { NON_PARLA_PER_PRIMO, TRAPPOLA, ORDINE_CASUALE, PROMEMORIA, SENZA_CATEGORIA, GIRI }   // questo è l'ordine di 4.2.1

data class RiepilogoConfigurazione(
    val numeroGiocatori: Int,
    val nomi: List<String>,              // Regole.nomiEffettivi(config)
    val numeroImpostori: Int,            // config.numeroImpostori limitato a 1..max(1, maxImpostori(N)); con fino a = true è il massimo; mostrato nella riga "Modalità" (passo 2)
    val finoA: Boolean,                  // impostoriSorpresa && numeroImpostori > 1 (CA-92, CA-104)
    val modalita: Modalita,
    val opzioni: List<OpzioneRiepilogo>, // solo non predefinite, nell'ordine dell'enum, senza duplicati; vuota = "Nessuna opzione attiva" (CA-89)
    val giriIndizi: Int,                 // 1..3 (limitato); "N giri" nel testo solo se GIRI in opzioni
    val numeroCategorie: Int,            // categorieSelezionate che esistono in `categorie`
)
```

Regole di `riepilogo.opzioni`: NON_PARLA_PER_PRIMO se `impostoreNonPrimo`; TRAPPOLA se `partitaTrappola`; ORDINE_CASUALE se `ordineCasuale`; PROMEMORIA se `promemoriaUltimaPossibilita`; SENZA_CATEGORIA se `!mostraCategoria` e `modalita == SENZA_PAROLA` (mai in PAROLA_AFFINE); GIRI se `giriIndizi` > 1. Né il numero di impostori né `impostoriSorpresa` entrano in `opzioni` (CA-93). `contaOpzioniAttive` = interruttori attivi tra `impostoreNonPrimo`, `partitaTrappola`, `ordineCasuale`, `promemoriaUltimaPossibilita`, più 1 se `giriIndizi` > 1; NON conta `mostraCategoria` né `impostoriSorpresa`, né SENZA_CATEGORIA (CA-81). Il badge del passo 3 e quello della riga del passo 5 usano la stessa funzione; con 0 non compare.

Il conteggio "Parole ancora da giocare: X / Y" non è nel riepilogo: dipende dalla sessione, la UI lo prende da `GestorePartite.rimanenti(Regole.pool(...))` (v1.2) come nel passo 3.

| Funzione | CA verificati | Casi limite |
|---|---|---|
| `errorePasso` | CA-25, 100, 101 | nome duplicato (anche con "Giocatore n" di default) -> GIOCATORI e CATEGORIE null; impostori oltre il massimo (dati incoerenti) -> MODALITA `TroppiImpostori`, GIOCATORI null; categorie vuote -> CATEGORIE `NessunaCategoria`; selezione non vuota ma pool vuoto (PAROLA_AFFINE senza affini) -> `PoolVuoto`; OPZIONI null con qualunque combinazione (trappola + sorpresa incluse); RIEPILOGO null; più errori nello stesso passo -> il primo di `Regole.valida`; tipi non del passo non compaiono mai (es. `NessunaCategoria` in GIOCATORI) |
| `primoPassoNonValido` | CA-102, 101, 25 | nome duplicato e categorie vuote -> GIOCATORI; impostori incoerenti e categorie vuote -> MODALITA; solo categorie vuote -> CATEGORIE; configurazione valida -> null; non restituisce mai OPZIONI né RIEPILOGO |
| `aggiungiGiocatore` | CA-113, 114, 118 | N = 4, nomi [] -> N = 5, nomi ["","","","",""], impostori invariati; N = 4, nomi ["A","B"] -> nomi ["A","B","","",""]; N = 3, nomi ["A","B","C","X"] (oltre N) -> N = 4, nomi ["A","B","C",""]; N = 19 -> N = 20; N = 20 -> config identica (stesso oggetto o uguale); 5 giocatori e 2 impostori -> 6 giocatori e 2 impostori; non modifica gli altri campi (modalità, opzioni, categorie); il risultato non è mai modificato in loco (funzione pura); nomiEffettivi del risultato ha la nuova lunghezza |
| `rimuoviGiocatore` | CA-115, 116, 117, 118 | ["Anna","","Carla",""], indice 1 -> N = 3, ["Anna","Carla",""] (nomiEffettivi "Anna","Carla","Giocatore 3"); ["","Bea","Cia","Dino"], indice 0 -> ["Bea","Cia","Dino"]; ultimo indice -> toglie l'ultimo; nomi più corti di N (N = 5, nomi ["A"]), indice 3 -> N = 4, nomi ["A","",""]; N = 3 -> invariata; indice -1 e indice N -> invariata; N = 7 e 3 impostori, rimozione -> N = 6 e 2; N = 5 e 2 -> N = 4 e 1; N = 6 e 1 -> N = 5 e 1; N = 20 e 9 impostori -> N = 19 e 9; sorpresa e trappola invariati; due nomi duplicati "Ann" e "ann", rimuovendo uno -> `errorePasso(GIOCATORI)` null |
| `puoAggiungere/RimuovereGiocatore` | CA-114, 115 | N = 19 -> aggiungere true; N = 20 -> aggiungere false; N = 3 -> rimuovere false; N = 4 -> rimuovere true |
| `riepilogo` | CA-92, 93, 94, 103, 104, 89 | sorpresa attiva con massimo 1 (N = 3 o 4) -> `finoA` false, "1 impostore"; sorpresa attiva con 5 giocatori e 2 -> `finoA` true; nomi vuoti -> "Giocatore n"; PAROLA_AFFINE con `mostraCategoria` false -> niente SENZA_CATEGORIA; tutte le opzioni spente -> `opzioni` vuota, `giriIndizi` 1; esempio "Ordine casuale, 2 giri" -> [ORDINE_CASUALE, GIRI] e `giriIndizi` 2; selezione con id inesistenti non contati; riflette sempre la config corrente (funzione pura, nessuna cache) |
| `contaOpzioniAttive` | CA-81, 89, 94 | tutte spente -> 0; solo `mostraCategoria` false -> 0; solo `impostoriSorpresa` -> 0; `giriIndizi` 2 o 3 -> 1; tutte le cinque contate attive -> 5 |

TS: `Passi` è un oggetto/modulo con le stesse funzioni (`aggiungiGiocatore(config)`, `rimuoviGiocatore(config, indice)`, `puoAggiungereGiocatore`, `puoRimuovereGiocatore`) e `PassoConfigurazione = "GIOCATORI" | "MODALITA" | "OPZIONI" | "CATEGORIE" | "RIEPILOGO"`. `StatoApp` (web) e `ImpostoreViewModel` (Android) espongono `aggiungiGiocatore()` e `rimuoviGiocatore(indice)` che applicano la funzione pura e salvano; il fuoco sul nuovo campo è compito della UI. `Regole.conNumeroGiocatori` resta (CA-06) ma il passo 1 non lo usa più.

### UI web: rotte dei passi (v2.4; v2.2 con 4 passi)

Aggiornamento di §3 "Rotte": `Rotta` diventa `... | "configurazione" | ...` invariato, ma la rotta `configurazione` ha un sotto-percorso. Hash `#/configurazione/1` .. `#/configurazione/5`; `#/configurazione` senza numero equivale a `#/configurazione/1`; numero fuori 1..5 o non intero -> `1`. Il passo corrente è letto dall'hash (non salvato, specifiche 4.2).

```ts
// ui/rotte.ts (aggiunte)
export const passoCorrente: { readonly valore: 1 | 2 | 3 | 4 | 5 };   // $state, derivato dall'hash; 1 fuori da "configurazione"
export function vaiAPasso(passo: 1 | 2 | 3 | 4 | 5, opz?: { sostituisci?: boolean }): void;   // Avanti/Modifica = push; Indietro dal riepilogo dopo "Modifica" = history normale
export function passoDiPassoConfigurazione(p: PassoConfigurazione): 1 | 2 | 3 | 4 | 5;        // GIOCATORI -> 1, MODALITA -> 2, OPZIONI -> 3, CATEGORIE -> 4, RIEPILOGO -> 5 (in game/passi.ts)
```
`vaiAConfigurazione()` apre `#/configurazione/1` (CA-99, 105). "Inizia" in barra: `const p = primoPassoNonValido(...)`; se `p === null` -> `iniziaPartita()` e `vai("distribuzione")`; altrimenti `vaiAPasso(passoDiPassoConfigurazione(p), { sostituisci: true })` e mostra l'errore del passo (resta nel passo corrente se coincide) (CA-102). Indietro del browser/freccia/tasto di sistema dai passi 2..5 = history normale, senza validare; dal passo 1 `salvaOra()` poi Home (CA-100). `StatoApp` espone `readonly riepilogo: RiepilogoConfigurazione` (`$derived` da `Passi.riepilogo(config, categorie)`) e `readonly opzioniAttive: number` (`contaOpzioniAttive(config)`); oltre a `aggiungiGiocatore()` / `rimuoviGiocatore(indice)` nessun'altra azione nuova (le `imposta*` esistenti bastano, `impostoriSorpresa` passa da `impostaOpzione`; `impostaNome(indice, testo)` resta).

## Foto dei giocatori (v1.10 Kotlin, v2.5 TS)

Decisione utente 2026-10-09. Specifiche 4.8, 4.2, 4.3, 4.7, 6; CA-123…CA-140. Nessuna modifica a `Configurazione`, `Partita`, `SessioneSalvata` né ai loro JSON: le foto stanno in un archivio separato, cercate per NOME al momento di disegnare un avatar. Un salvataggio v1.9 / v2.4 si legge invariato.

### Logica pura (Kotlin `it.imposteur.game`, file `FotoGiocatori.kt`; TS `web/src/game/fotoGiocatori.ts`)

Nessun import `android.*`, nessun accesso a file o orologio: l'istante (`ora`, millisecondi, `Long`; in TS `number` intero) è un parametro. Le funzioni non lanciano mai eccezioni.

```kotlin
const val MAX_FOTO = 50            // TS: MAX_FOTO
const val LATO_FOTO_PX = 256       // TS: LATO_FOTO_PX
const val QUALITA_JPEG = 85        // TS: QUALITA_JPEG (0..100; il web passa QUALITA_JPEG / 100 a toBlob)

object ChiaveFoto {
    /** Chiave di archivio del nome: trim + lowercase() (Kotlin) / toLowerCase() (TS), spazi interni mantenuti.
     *  null se il risultato è vuoto o se è "giocatore" seguito da sole cifre, con o senza spazi in mezzo (nome riservato al default). CA-123 */
    fun daNome(nome: String): String?
}

/** Una foto archiviata. `file` è un nome opaco scelto dal chiamante (Android: "<uuid>.jpg"; web: uguale alla chiave). */
data class VoceFoto(val chiave: String, val file: String, val ultimoUso: Long)

/** Invariante: nessuna chiave ripetuta; `voci` ordinate per chiave crescente (confronto per unità UTF-16: `compareTo` / `<` di JS). */
data class IndiceFoto(val voci: List<VoceFoto> = emptyList()) {
    val dimensione: Int
    /** Voce del nome (via ChiaveFoto.daNome), null se nome non valido o assente. CA-124 */
    fun trova(nome: String): VoceFoto?
    /** Aggiunge o sostituisce la voce del nome con (file, ora); poi elimina le eccedenti oltre MAX_FOTO, mai la voce appena registrata.
     *  daEliminare = file della voce sostituita (se il file era diverso) + file delle eccedenti. Nome non valido -> indice invariato, daEliminare vuoto. CA-125, CA-126 */
    fun registra(nome: String, file: String, ora: Long): EsitoIndice
    /** Per ogni nome con voce (chiave valida, ripetuti ammessi) imposta ultimoUso = ora; ignora gli altri; non aggiunge né toglie voci. CA-127 */
    fun segnaUso(nomi: List<String>, ora: Long): IndiceFoto
    /** Toglie la voce del nome; il suo file è in daEliminare. Nome senza voce -> invariato. CA-127, CA-136 */
    fun rimuovi(nome: String): EsitoIndice
    /** Indice vuoto; tutti i file in daEliminare (ordine dell'indice). CA-127, CA-135 */
    fun svuota(): EsitoIndice
    /** Voci da eliminare per rispettare `massimo`: le meno recenti per ultimoUso, a parità la chiave minore; vuoto se dimensione <= massimo (massimo < 0 trattato come 0). CA-126 */
    fun eccedenti(massimo: Int = MAX_FOTO): List<VoceFoto>
}
data class EsitoIndice(val indice: IndiceFoto, val daEliminare: List<String>)   // TS: interface con campi readonly

object RitaglioFoto {
    data class Riquadro(val x: Int, val y: Int, val lato: Int)
    /** Quadrato centrato più grande: lato = min(w, h); x = (w - lato) / 2, y = (h - lato) / 2 (divisione intera). w o h <= 0 -> null. CA-131 */
    fun quadratoCentrato(larghezza: Int, altezza: Int): Riquadro?
    /** Lato del risultato: min(LATO_FOTO_PX, lato del quadrato) (nessun ingrandimento). 0 se non valido. CA-131 */
    fun latoRisultato(larghezza: Int, altezza: Int): Int
}
```
TS: `ChiaveFoto.daNome` → `chiaveFoto(nome: string): string | null`; `IndiceFoto` è `interface { readonly voci: readonly VoceFoto[] }` con le funzioni libere `trovaFoto(indice, nome)`, `registraFoto(indice, nome, file, ora)`, `segnaUsoFoto(indice, nomi, ora)`, `rimuoviFoto(indice, nome)`, `svuotaFoto(indice)`, `fotoEccedenti(indice, massimo = MAX_FOTO)`, `quadratoCentrato(w, h): Riquadro | null`, `latoRisultato(w, h): number`, costante `indiceFotoVuoto`. Nei CA la forma `f(indice, …)` vale per entrambe (in Kotlin `indice.f(…)`).

Tabella di casi (JUnit e Vitest identici):

| Funzione | CA | Casi |
|---|---|---|
| `daNome` | 123 | "Marco" -> "marco"; "  MARCO " -> "marco"; "Ma rco" -> "ma rco"; "" e "   " -> null; "Giocatore 3" -> null; "giocatore12" -> null; "GIOCATORE  7" -> null; "Giocatore" -> "giocatore"; "Giocatore 3a" -> "giocatore 3a"; "Élodie" -> "élodie" |
| `trova` | 124 | indice con "marco": "Marco", " MARCO", "marco" -> la voce; "Marcos", "", "Giocatore 2" -> null; indice vuoto -> null; dopo `registra("Marco")`, `trova("Marcello")` -> null |
| `registra` | 125 | su indice vuoto, ("Marco","a.jpg",100) -> 1 voce ("marco","a.jpg",100), daEliminare vuoto; poi ("MARCO","b.jpg",200) -> 1 voce ("marco","b.jpg",200), daEliminare ["a.jpg"]; di nuovo con file "b.jpg" -> daEliminare vuoto; ("", ...) e ("Giocatore 2", ...) -> indice invariato; nomi "Zoe" poi "Anna" -> voci ordinate ["anna","zoe"] |
| `registra` con limite, `eccedenti` | 126 | 50 voci con ultimoUso 1..50 (chiavi k01..k50), nuova "nuova" ora 60 -> 50 voci, "k01" assente, daEliminare = [file di k01]; 3 voci ultimoUso 10, 20, 30 e massimo 2 -> `eccedenti(2)` = [la voce 10]; due voci con ultimoUso 5 (chiavi "b","a"), massimo 1 -> eccedenti = ["a"]; registra con ora minore di tutte le altre a 50 voci -> la nuova resta, esce la meno recente tra le altre; 50 voci -> `eccedenti()` vuoto; 52 voci -> le 2 meno recenti |
| `segnaUso` | 127, 134 | nomi ["Marco","Sconosciuto","","Giocatore 1","MARCO"] con ora 500 -> solo "marco" ha ultimoUso 500; dimensione invariata; lista vuota -> indice uguale |
| `rimuovi`, `svuota` | 127, 136 | rimuovi("Marco") -> voce tolta e il suo file in daEliminare, le altre invariate; rimuovi("Nessuno") -> invariato, daEliminare vuoto; svuota con 3 voci -> indice vuoto, 3 file |
| `quadratoCentrato`, `latoRisultato` | 131 | (1000,2000) -> (0,500,1000), lato 256; (2000,1000) -> (500,0,1000), lato 256; (100,100) -> (0,0,100), lato 100; (257,256) -> (0,0,256), lato 256; (301,300) -> (0,0,300), lato 256; (0,10) e (10,-1) -> null, lato 0 |

### Formato dell'indice (Kotlin `it.imposteur.data`, `FormatoIndiceFoto.kt`; TS `web/src/data/formatoIndiceFoto.ts`; puro)

```kotlin
object FormatoIndiceFoto {
    fun scrivi(indice: IndiceFoto): String
    /** Testo vuoto/null, non JSON o radice di tipo errato -> IndiceFoto(). Altrimenti normalizza (CA-128):
     *  scarta le voci con chiave diversa da ChiaveFoto.daNome(chiave) (non normalizzata, vuota, riservata), `file` vuoto o con "/", "\" o "..", `ultimoUso` non intero;
     *  due voci con la stessa chiave -> resta quella con ultimoUso maggiore (a parità, l'ultima nel testo); poi tiene le MAX_FOTO più recenti; ordina per chiave. Campi sconosciuti ignorati. */
    fun leggi(testo: String?): IndiceFoto
}
```
TS: `scriviIndiceFoto(indice): string`, `leggiIndiceFoto(testo: string | null): IndiceFoto`. JSON: `{"versione":1,"foto":[{"chiave":"marco","file":"3f2a.jpg","ultimoUso":1760000000000}]}`. `versione` mancante o diversa da 1 non è un errore (si legge `foto` se è un array). Casi: scrivi→leggi identico; "" / "{" / "[]" / `{"foto":5}` -> vuoto; voce con chiave "Marco" (non normalizzata) scartata e le altre restano; `file` "../x.jpg" scartata; `ultimoUso` "x" scartata; due "marco" con 10 e 20 -> resta 20; 52 voci -> le 50 più recenti.

### Android: archivio e sistema (`it.imposteur.data`)

```kotlin
/** Archivio delle foto su file. Le scritture sono serializzate (Mutex interno); le funzioni sospese girano su Dispatchers.IO e non lanciano: un errore di I/O = nessuna modifica visibile (salva -> false). */
interface RepositoryFoto {
    /** Indice corrente (aggiornato a ogni scrittura). Al primo accesso legge `indice.json` con FormatoIndiceFoto.leggi e cancella da `avatar/` i file orfani (CA-138). */
    val indice: StateFlow<IndiceFoto>
    /** File JPEG della foto del nome; null se non c'è la voce o il file manca (CA-133). */
    fun fileDi(nome: String): File?
    /** Scrive `jpeg` in un nuovo file `<uuid>.jpg`, poi indice.registra(nome, file, ora), poi cancella i file in daEliminare e salva l'indice (scrittura atomica: file temporaneo + rinomina). false se nome non valido o I/O fallito. CA-125, 126, 137 */
    suspend fun salva(nome: String, jpeg: ByteArray): Boolean
    suspend fun rimuovi(nome: String)                       // CA-136
    suspend fun segnaUso(nomi: List<String>)                // CA-127, 134; non scrive se nessun nome ha foto
    suspend fun eliminaTutte()                              // CA-135
}
class RepositoryFotoFile(directory: File, orologio: () -> Long = System::currentTimeMillis) : RepositoryFoto
// directory = File(context.filesDir, "avatar"); indice in File(directory, "indice.json")
```
Elaborazione immagine (UI/piattaforma, non in `game/`; usa `RitaglioFoto`): `suspend fun elaboraFoto(context: Context, uri: Uri): ByteArray?` in `ui/` (`ElaboraFoto.kt`): decodifica con campionamento (`inSampleSize`) per restare intorno a 2048 px, applica l'orientamento EXIF, ritaglia con `quadratoCentrato`, scala a `latoRisultato`, comprime JPEG a `QUALITA_JPEG`; null se non decodificabile (CA-131, 137).

Sistema Android (nessun permesso):
- Galleria: `ActivityResultContracts.PickVisualMedia` con `PickVisualMediaRequest(PickVisualMedia.ImageOnly)`; nessun permesso `READ_MEDIA_*` / `READ_EXTERNAL_STORAGE`.
- Fotocamera: `ActivityResultContracts.TakePicture` (ACTION_IMAGE_CAPTURE) con uri di `FileProvider` su un file temporaneo in `cacheDir/foto-temp/` (cancellato dopo l'elaborazione o all'annullamento). Manifest: `<provider>` FileProvider con authority `${applicationId}.fileprovider`, `grantUriPermissions="true"`, `res/xml/percorsi_file.xml` con `<cache-path name="foto_temp" path="foto-temp/"/>`; `<queries>` con l'intent `android.media.action.IMAGE_CAPTURE` (per sapere se esiste un'app fotocamera: "Scatta una foto" nascosta altrimenti). NON dichiarare `android.permission.CAMERA` (se dichiarato, andrebbe chiesto a runtime).
- Backup: escludere `avatar/` in `data_extraction_rules.xml` e in `fullBackupContent` (`<exclude domain="file" path="avatar/"/>`) (CA-139).

`ImpostoreViewModel` (aggiunte):
```kotlin
val foto: StateFlow<IndiceFoto>                      // = repositoryFoto.indice
val fotoDisponibile: Boolean                          // sempre true su Android
fun fileFoto(nome: String): File?                     // = repositoryFoto.fileDi(nome)
fun salvaFoto(nome: String, jpeg: ByteArray)          // se salva() dà false emette l'evento "Impossibile usare questa foto"
fun rimuoviFoto(nome: String)
fun eliminaTutteLeFoto()
// iniziaPartita() / rigioca(): dopo aver creato la partita chiama repositoryFoto.segnaUso(partita.giocatori) (nomi effettivi) (CA-134)
```
`nome` è il nome del campo al momento del tocco sull'avatar, conservato (`rememberSaveable`) mentre l'app esterna è aperta (CA-129, 130). Componente comune `AvatarGiocatore(nome, colore, dimensione, foto: File?)`: se `foto` è decodificabile mostra l'immagine ritagliata a cerchio, altrimenti l'iniziale. Nessun parametro di ruolo: la resa non può dipenderne (CA-133).

### PWA: archivio IndexedDB (`web/src/data/archivioFoto.ts`)

```ts
export interface ArchivioFoto {
  /** Indice corrente (file = chiave). */
  leggiIndice(): Promise<IndiceFoto>;
  /** Immagine del nome; null se assente o illeggibile. */
  leggiFoto(nome: string): Promise<Blob | null>;
  /** `jpeg` è già elaborato (type "image/jpeg"). Registra con registraFoto(…, ora), elimina le eccedenti nella stessa transazione. true se salvato; mai eccezioni (errore -> false). */
  salvaFoto(nome: string, jpeg: Blob): Promise<boolean>;
  rimuoviFoto(nome: string): Promise<void>;
  segnaUso(nomi: readonly string[]): Promise<void>;
  eliminaTutte(): Promise<void>;
}
/** null se `indexedDB` non esiste o l'apertura fallisce (modalità privata, blocco): la funzione è allora nascosta (CA-140). */
export function creaArchivioFotoIdb(orologio?: () => number): Promise<ArchivioFoto | null>;
/** Per i test: stessa semantica, in memoria. */
export function creaArchivioFotoInMemoria(orologio?: () => number): ArchivioFoto;
```
IndexedDB: database `impostore-foto` v1, object store `foto` con `keyPath: "chiave"`, record `{ chiave, blob, ultimoUso }` (l'indice si ricava dai record: nessun altro store). Ogni scrittura è una sola transazione `readwrite`. Record non validi (chiave non normalizzata, `blob` non Blob) sono scartati e cancellati. `FormatoIndiceFoto` non serve sul web.

`StatoApp` (aggiunte): `readonly fotoDisponibile: boolean`; `readonly foto: IndiceFoto` ($state); `urlFoto(nome: string): string | null` (object URL in cache, `URL.revokeObjectURL` quando la voce cambia o sparisce); `salvaFoto(nome: string, file: Blob): Promise<void>` (elabora con `elaboraFoto` e salva; errore -> evento "Impossibile usare questa foto"); `rimuoviFoto(nome)`; `eliminaTutteLeFoto()`; `iniziaPartita()` chiama `segnaUso`. `ui/browser.ts` (aggiunta): `elaboraFoto(file: Blob): Promise<Blob | null>` con `createImageBitmap(file, { imageOrientation: "from-image" })`, `quadratoCentrato` / `latoRisultato` su canvas, `canvas.toBlob(cb, "image/jpeg", QUALITA_JPEG / 100)`; null se non decodificabile. Campi file nascosti: galleria `<input type="file" accept="image/*">`; fotocamera `<input type="file" accept="image/*" capture="user">`.

### Dove si usa l'avatar (UI)

`AvatarGiocatore` (Android) / `.avatar` (web) disegnano la foto del nome al posto dell'iniziale in: `RigaGiocatore` (passo 1, unico punto toccabile: 48 x 48 dp, nome accessibile "Foto di <nome>", menu "Scegli dalla galleria" / "Scatta una foto" / "Rimuovi foto"), fila e cerchio grande di Distribuzione, elenco e Passaggio di Rivedi, avatar piccoli di Rivela e ogni altro uso. Il gruppo "Foto dei giocatori" delle Impostazioni (visibile se `foto.dimensione >= 1` e `fotoDisponibile`) mostra "Foto salvate: N" e "Elimina tutte le foto" con la conferma di 4.8. Gli stili restano a `docs/design.md`, da aggiornare (3 "Riga giocatore" dice che l'area dell'avatar non è interattiva).

| Verifica | CA | Dove |
|---|---|---|
| `ChiaveFoto`, `IndiceFoto`, `RitaglioFoto`, `FormatoIndiceFoto` | 123-128, 131 (geometria) | JUnit (`FotoGiocatoriTest`) e Vitest (`fotoGiocatori.test.ts`), tabelle sopra |
| `RepositoryFotoFile` su cartella temporanea e orologio finto (salva, sostituisci, limite 50 con file, rimuovi, eliminaTutte, orfani) | 125, 126, 134-138 | JUnit con `TemporaryFolder` |
| `creaArchivioFotoInMemoria` (stessa semantica) | 125, 126, 134-136, 140 | Vitest |
| UI: passo 1, menu, impostazioni, indistinguibilità, errori, manifest | 129-133, 135, 137, 139, 140 | strumentati/manuali |
