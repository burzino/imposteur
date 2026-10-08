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
export function reale(c: Casuale): number;                              // [0, 1), costruito da chiamate a intero()
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
export interface Aspetto { readonly tema: Tema; readonly coloriDinamici: boolean }
export const aspettoDefault: Aspetto;                                // { tema: "SISTEMA", coloriDinamici: true }
export function aspettoAStringa(a: Aspetto): string;                 // {"tema":"...","coloriDinamici":bool}
export function aspettoDaStringa(s: string | null): Aspetto;         // null/malformata/valori sconosciuti -> default per campo
```

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
