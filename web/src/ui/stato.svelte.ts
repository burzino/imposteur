import { getContext, hasContext } from "svelte";
import { creaArchivio, type Archivio, type ArchivioStorage } from "../data/archivio";
import type { Aspetto } from "../data/aspetto";
import { parseParole, type RisultatoCaricamento } from "../data/parserParole";
import { FormatoSegnalazioni, type Segnalazione } from "../data/segnalazioni";
import { casualeDiSistema, type Casuale } from "../game/casuale";
import {
  configurazioneDefault,
  type Categoria,
  type Configurazione,
  type ErroreConfigurazione,
  type Modalita,
  type VoceParola,
} from "../game/modelli";
import {
  Distribuzione,
  GestorePartite,
  type Partita,
  type SessioneSalvata,
  type StatoDistribuzione,
} from "../game/partita";
import { Regole } from "../game/regole";
import { applicaTema } from "./tema";

export interface Revisione {
  readonly indice: number;
  readonly rivelato: boolean;
}

export interface Dipendenze {
  archivio: Archivio;
  casuale: Casuale;
  caricaCategorie: () => Promise<RisultatoCaricamento>;
  /** Debounce del salvataggio della configurazione (default 500; 0 = salvataggio immediato, per i test). */
  ritardoSalvataggioMs?: number;
}

interface Ui {
  readonly caricamento: boolean;
  readonly erroreCaricamento: boolean;
  readonly categorie: readonly Categoria[];
  readonly config: Configurazione;
  readonly partita: Partita | null;
  readonly distribuzione: StatoDistribuzione;
  readonly ripristinabile: SessioneSalvata | null;
  readonly paroleRimanenti: number;
  readonly paroleTotali: number;
}

function stessoStato(a: StatoDistribuzione, b: StatoDistribuzione): boolean {
  if (a.tipo !== b.tipo) return false;
  return a.tipo === "Gioco" || (b.tipo !== "Gioco" && a.indice === b.indice);
}

/** Porting di ImpostoreViewModel: unica fonte di stato della UI. Gli oggetti sono immutabili. */
export class StatoApp {
  readonly pronto: Promise<void>;

  #dip: Dipendenze;
  #gestore: GestorePartite;
  /** True se la partita in UI fa parte della sessione salvata. */
  #partitaAttiva = false;
  #timerSalvataggio: ReturnType<typeof setTimeout> | null = null;

  #ui = $state.raw<Ui>({
    caricamento: true,
    erroreCaricamento: false,
    categorie: [],
    config: configurazioneDefault(),
    partita: null,
    distribuzione: Distribuzione.iniziale(),
    ripristinabile: null,
    paroleRimanenti: 0,
    paroleTotali: 0,
  });
  #revisione = $state.raw<Revisione | null>(null);
  #aspetto = $state.raw<Aspetto>({ tema: "SISTEMA", coloriDinamici: true });
  #segnalazioniSalvate = $state.raw(0);
  #errori = $derived.by<readonly ErroreConfigurazione[]>(() =>
    this.#ui.categorie.length === 0 ? [] : Regole.valida(this.#ui.config, this.#ui.categorie),
  );
  #puoIniziare = $derived(!this.#ui.caricamento && !this.#ui.erroreCaricamento && this.#errori.length === 0);

  constructor(dip: Dipendenze) {
    this.#dip = dip;
    this.#gestore = new GestorePartite(dip.casuale);
    this.#aspetto = dip.archivio.leggiAspetto();
    this.#segnalazioniSalvate = dip.archivio.contaSegnalazioni();
    applicaTema(this.#aspetto.tema);
    this.pronto = this.#carica();
  }

  // ---- Campi leggibili ----------------------------------------------------------------------

  get caricamento(): boolean {
    return this.#ui.caricamento;
  }
  get erroreCaricamento(): boolean {
    return this.#ui.erroreCaricamento;
  }
  get categorie(): readonly Categoria[] {
    return this.#ui.categorie;
  }
  get config(): Configurazione {
    return this.#ui.config;
  }
  get partita(): Partita | null {
    return this.#ui.partita;
  }
  get distribuzione(): StatoDistribuzione {
    return this.#ui.distribuzione;
  }
  get ripristinabile(): SessioneSalvata | null {
    return this.#ui.ripristinabile;
  }
  get paroleRimanenti(): number {
    return this.#ui.paroleRimanenti;
  }
  get paroleTotali(): number {
    return this.#ui.paroleTotali;
  }
  get revisione(): Revisione | null {
    return this.#revisione;
  }
  get aspetto(): Aspetto {
    return this.#aspetto;
  }
  get segnalazioniSalvate(): number {
    return this.#segnalazioniSalvate;
  }
  get errori(): readonly ErroreConfigurazione[] {
    return this.#errori;
  }
  get puoIniziare(): boolean {
    return this.#puoIniziare;
  }

  // ---- Caricamento --------------------------------------------------------------------------

  async #carica(): Promise<void> {
    let r: RisultatoCaricamento;
    try {
      r = await this.#dip.caricaCategorie();
    } catch {
      r = { tipo: "Errore", messaggio: "caricamento non riuscito" };
    }
    if (r.tipo === "Ok") {
      const config = this.#dip.archivio.leggiConfigurazione(r.categorie);
      const sessione = this.#dip.archivio.leggiSessione(r.categorie);
      this.#gestore = new GestorePartite(this.#dip.casuale, sessione.usate, sessione.ultima);
      const ripristinabile = sessione.partita !== null && sessione.stato !== null ? sessione : null;
      this.#aggiorna({ caricamento: false, categorie: r.categorie, config, ripristinabile });
      this.#aggiornaContatore();
    } else {
      this.#aggiorna({ caricamento: false, erroreCaricamento: true });
    }
  }

  #aggiorna(parziale: Partial<Ui>): void {
    this.#ui = { ...this.#ui, ...parziale };
  }

  // ---- Aspetto e segnalazioni ---------------------------------------------------------------

  impostaAspetto(a: Aspetto): void {
    this.#aspetto = a;
    this.#dip.archivio.salvaAspetto(a);
    applicaTema(a.tema);
  }

  salvaSegnalazione(s: Segnalazione): void {
    this.#dip.archivio.aggiungiSegnalazione(s);
    this.#segnalazioniSalvate = this.#dip.archivio.contaSegnalazioni();
  }

  cancellaSegnalazioni(): void {
    this.#dip.archivio.cancellaSegnalazioni();
    this.#segnalazioniSalvate = this.#dip.archivio.contaSegnalazioni();
  }

  /** Contenuto di segnalazioni.jsonl: ogni riga termina con "\n". */
  leggiSegnalazioniJsonl(): string {
    return this.#dip.archivio
      .leggiSegnalazioni()
      .map((s) => FormatoSegnalazioni.riga(s) + "\n")
      .join("");
  }

  // ---- Configurazione -----------------------------------------------------------------------

  #modificaConfig(f: (c: Configurazione) => Configurazione): void {
    if (this.#ui.caricamento) return;
    this.#aggiorna({ config: f(this.#ui.config) });
    this.#aggiornaContatore();
    this.#annullaSalvataggio();
    const ritardo = this.#dip.ritardoSalvataggioMs ?? 500;
    if (ritardo <= 0) {
      this.#dip.archivio.salvaConfigurazione(this.#ui.config);
    } else {
      this.#timerSalvataggio = setTimeout(() => {
        this.#timerSalvataggio = null;
        this.#dip.archivio.salvaConfigurazione(this.#ui.config);
      }, ritardo);
    }
  }

  #annullaSalvataggio(): void {
    if (this.#timerSalvataggio !== null) {
      clearTimeout(this.#timerSalvataggio);
      this.#timerSalvataggio = null;
    }
  }

  #poolCorrente(): VoceParola[] {
    const u = this.#ui;
    return Regole.pool(u.categorie, u.config.categorieSelezionate, u.config.modalita);
  }

  /** Ricalcola "parole ancora da giocare" (categorie, modalita' o usate cambiate). */
  #aggiornaContatore(): void {
    const pool = this.#poolCorrente();
    this.#aggiorna({ paroleRimanenti: this.#gestore.rimanenti(pool), paroleTotali: pool.length });
  }

  /** Rimette in gioco le parole delle categorie scelte e salva la sessione. */
  azzeraParole(): void {
    if (this.#ui.caricamento) return;
    this.#gestore.azzeraUsate(this.#poolCorrente());
    this.#aggiornaContatore();
    this.#persisti();
  }

  /** Salva subito la configurazione corrente (Inizia, indietro, pagina nascosta). */
  salvaOra(): void {
    if (this.#ui.caricamento || this.#ui.erroreCaricamento) return;
    this.#annullaSalvataggio();
    this.#dip.archivio.salvaConfigurazione(this.#ui.config);
  }

  impostaNumeroGiocatori(n: number): void {
    this.#modificaConfig((c) =>
      Regole.conNumeroGiocatori(c, Math.min(Regole.MAX_GIOCATORI, Math.max(Regole.MIN_GIOCATORI, n))),
    );
  }

  impostaNumeroImpostori(n: number): void {
    this.#modificaConfig((c) => ({
      ...c,
      numeroImpostori: Math.min(Regole.maxImpostori(c.numeroGiocatori), Math.max(1, n)),
    }));
  }

  impostaNome(indice: number, testo: string): void {
    this.#modificaConfig((c) => {
      const nomi = [...c.nomi];
      while (nomi.length <= indice) nomi.push("");
      nomi[indice] = testo.slice(0, Regole.MAX_LUNGHEZZA_NOME);
      return { ...c, nomi };
    });
  }

  impostaModalita(m: Modalita): void {
    this.#modificaConfig((c) => ({ ...c, modalita: m }));
  }

  /** Modifica generica per le opzioni avanzate; si salva come le altre. */
  impostaOpzione(trasforma: (c: Configurazione) => Configurazione): void {
    this.#modificaConfig(trasforma);
  }

  impostaMostraCategoria(v: boolean): void {
    this.#modificaConfig((c) => ({ ...c, mostraCategoria: v }));
  }

  impostaCategoria(id: string, selezionata: boolean): void {
    this.#modificaConfig((c) => {
      const sel = new Set(c.categorieSelezionate);
      if (selezionata) sel.add(id);
      else sel.delete(id);
      return { ...c, categorieSelezionate: sel };
    });
  }

  selezionaTutte(tutte: boolean): void {
    this.#modificaConfig((c) => ({
      ...c,
      categorieSelezionate: tutte ? new Set(this.#ui.categorie.map((x) => x.id)) : new Set<string>(),
    }));
  }

  // ---- Partita ------------------------------------------------------------------------------

  /** Crea una nuova partita con la configurazione corrente. True se riuscita. */
  iniziaPartita(): boolean {
    if (!this.#puoIniziare) return false;
    this.salvaOra();
    const r = this.#gestore.nuovaPartita(this.#ui.config, this.#ui.categorie);
    if (r.tipo !== "Ok") return false;
    this.#partitaAttiva = true;
    this.#aggiorna({ partita: r.partita, distribuzione: Distribuzione.iniziale(), ripristinabile: null });
    this.#aggiornaContatore();
    this.#persisti();
    return true;
  }

  /** Avanza solo se lo stato corrente e' ancora `da` (ignora i tocchi doppi). */
  avanza(da: StatoDistribuzione): void {
    const p = this.#ui.partita;
    if (p === null) return;
    if (!stessoStato(this.#ui.distribuzione, da)) return;
    this.#aggiorna({ distribuzione: Distribuzione.avanza(da, p.giocatori.length) });
    this.#persisti();
  }

  /** Pagina nascosta o schermata ricreata durante la rivelazione. */
  interrompiRivelazione(): void {
    const prima = this.#ui.distribuzione;
    const dopo = Distribuzione.interrompiRivelazione(prima);
    if (stessoStato(prima, dopo)) return;
    this.#aggiorna({ distribuzione: dopo });
    this.#persisti();
  }

  /** Sceglie il giocatore che rivede la parola (mostra "Passa il telefono a ..."). */
  scegliRevisione(indice: number): void {
    const p = this.#ui.partita;
    if (p === null) return;
    if (!Number.isInteger(indice) || indice < 0 || indice >= p.giocatori.length || this.#revisione !== null) return;
    this.#revisione = { indice, rivelato: false };
  }

  /** "Sono <nome>": rivela il ruolo. Ignora i tocchi doppi. */
  rivelaRevisione(): void {
    const r = this.#revisione;
    if (r !== null && !r.rivelato) this.#revisione = { ...r, rivelato: true };
  }

  /** Pagina nascosta: se un ruolo e' visibile torna al passaggio. */
  interrompiRevisione(): void {
    const r = this.#revisione;
    if (r !== null && r.rivelato) this.#revisione = { ...r, rivelato: false };
  }

  /** Dal passaggio torna all'elenco. */
  tornaAElencoRevisione(): void {
    this.#revisione = null;
  }

  /** Esce dalla funzione (nasconde tutto). */
  chiudiRevisione(): void {
    this.#revisione = null;
  }

  /** Ingresso in Rivela: cancella la partita salvata (le parole usate restano). */
  entraInRivela(): void {
    this.#partitaAttiva = false;
    this.#aggiorna({ ripristinabile: null });
    this.#persisti();
  }

  terminaPartita(): void {
    this.#partitaAttiva = false;
    this.#revisione = null;
    this.#aggiorna({ partita: null, distribuzione: Distribuzione.iniziale(), ripristinabile: null });
    this.#persisti();
  }

  /**
   * Esce verso la Home senza interrompere la partita: nasconde i ruoli visibili, rende la partita
   * ripristinabile ("Riprendi partita") e salva la sessione.
   */
  sospendiPartita(): void {
    const u = this.#ui;
    const p = u.partita;
    if (p === null) return;
    this.#revisione = null;
    const dist = Distribuzione.interrompiRivelazione(u.distribuzione);
    this.#partitaAttiva = true;
    this.#aggiorna({
      distribuzione: dist,
      ripristinabile: { partita: p, stato: dist, usate: this.#gestore.usate, ultima: this.#gestore.ultima },
    });
    this.#persisti();
  }

  /** Carica la partita salvata nello stato di gioco. True se va aperta la schermata di Gioco. */
  riprendiPartita(): boolean {
    const r = this.#ui.ripristinabile;
    if (r === null) return false;
    const stato = r.stato;
    if (stato === null || r.partita === null) return false;
    this.#partitaAttiva = true;
    this.#aggiorna({ partita: r.partita, distribuzione: stato, ripristinabile: null });
    return stato.tipo === "Gioco";
  }

  /** Pagina nascosta: nasconde ogni ruolo visibile e salva la configurazione. */
  nascondiRuolo(): void {
    this.interrompiRivelazione();
    this.interrompiRevisione();
    this.salvaOra();
  }

  #persisti(): void {
    const u = this.#ui;
    const attiva = this.#partitaAttiva && u.partita !== null;
    this.#dip.archivio.salvaSessione({
      partita: attiva ? u.partita : null,
      stato: attiva ? u.distribuzione : null,
      usate: this.#gestore.usate,
      ultima: this.#gestore.ultima,
    });
  }
}

// ---- Predefiniti ----------------------------------------------------------------------------

/** localStorage "pigro": nessun accesso a window/localStorage a livello di modulo; gli errori li assorbe l'archivio. */
const storageBrowser: ArchivioStorage = {
  getItem: (k) => globalThis.localStorage.getItem(k),
  setItem: (k, v) => globalThis.localStorage.setItem(k, v),
  removeItem: (k) => globalThis.localStorage.removeItem(k),
};

/** parole.json e' incluso nel bundle (alias $parole): precache offline, nessun URL da risolvere. */
async function caricaParoleIncluse(): Promise<RisultatoCaricamento> {
  const modulo = await import("$parole");
  return parseParole(JSON.stringify(modulo.default));
}

export function creaStatoPredefinito(): StatoApp {
  return new StatoApp({
    archivio: creaArchivio(storageBrowser),
    casuale: casualeDiSistema(),
    caricaCategorie: caricaParoleIncluse,
  });
}

export const stato: StatoApp = creaStatoPredefinito();

/** Stato della schermata: quello iniettato con `setContext("stato", s)` (test), altrimenti il singleton. */
export function getStato(): StatoApp {
  return hasContext("stato") ? getContext<StatoApp>("stato") : stato;
}
