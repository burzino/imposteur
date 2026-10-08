import { interoTra, mescolato, reale, type Casuale } from "./casuale";
import {
  TestiGioco,
  type Categoria,
  type Configurazione,
  type ErroreConfigurazione,
  type Modalita,
  type VoceParola,
} from "./modelli";
import { MAX_GIRI, PROBABILITA_TRAPPOLA, nomiEffettivi, pool, valida } from "./regole";

export type ContenutoRuolo =
  | { readonly tipo: "ParolaSegreta"; readonly testo: string }
  | { readonly tipo: "Impostore"; readonly categoria: string | null };

export interface Partita {
  readonly giocatori: readonly string[];
  readonly impostori: ReadonlySet<number>; // vuoto = partita trappola
  readonly voce: VoceParola;
  readonly modalita: Modalita;
  readonly mostraCategoria: boolean;
  readonly primoGiocatore: number;
  /** Ordine personalizzato (ordine casuale); null = rotazione da primoGiocatore. */
  readonly ordine: readonly number[] | null;
  readonly giriIndizi: number;
  readonly promemoriaUltimaPossibilita: boolean;
}

export function eTrappola(p: Partita): boolean {
  return p.impostori.size === 0;
}

/** CA-09, CA-10 */
export function contenutoPer(p: Partita, indice: number): ContenutoRuolo {
  const impostore = p.impostori.has(indice);
  if (eTrappola(p) || !impostore) return { tipo: "ParolaSegreta", testo: p.voce.parola };
  if (p.modalita === "PAROLA_AFFINE") {
    return { tipo: "ParolaSegreta", testo: p.voce.affine ?? p.voce.parola };
  }
  return { tipo: "Impostore", categoria: p.mostraCategoria ? p.voce.categoriaNome : null };
}

export function ordineDiParola(p: Partita): number[] {
  if (p.ordine !== null) return [...p.ordine];
  const n = p.giocatori.length;
  return Array.from({ length: n }, (_, i) => (p.primoGiocatore + i) % n);
}

/** CA-21 */
export function testoSvelamento(p: Partita): string {
  const nomi = [...p.impostori].sort((a, b) => a - b).map((i) => p.giocatori[i]);
  const righe: string[] = [];
  if (nomi.length === 0) righe.push(TestiGioco.NESSUN_IMPOSTORE);
  else if (nomi.length === 1) righe.push(TestiGioco.impostoriSingolare(nomi[0]));
  else righe.push(TestiGioco.impostoriPlurale(nomi));
  righe.push(TestiGioco.laParolaEra(p.voce.parola));
  if (p.modalita === "PAROLA_AFFINE" && p.voce.affine !== null) {
    righe.push(TestiGioco.laParolaAffineEra(p.voce.affine));
  }
  righe.push(TestiGioco.categoria(p.voce.categoriaNome));
  return righe.join("\n");
}

/** Parola identificata per categoria; `parola` e' normalizzata (trim + minuscole). */
export interface ChiaveParola {
  readonly categoriaId: string;
  readonly parola: string;
}

export function chiaveDi(v: VoceParola): ChiaveParola {
  return { categoriaId: v.categoriaId, parola: v.parola.trim().toLowerCase() };
}

export function chiaveStringa(k: ChiaveParola): string {
  return JSON.stringify([k.categoriaId, k.parola]);
}

export type StatoDistribuzione =
  | { readonly tipo: "Passaggio"; readonly indice: number }
  | { readonly tipo: "Rivelazione"; readonly indice: number }
  | { readonly tipo: "Gioco" };

export interface SessioneSalvata {
  readonly partita: Partita | null;
  readonly stato: StatoDistribuzione | null; // null se partita null
  readonly usate: readonly ChiaveParola[]; // senza duplicati
  readonly ultima: ChiaveParola | null;
}

export type RisultatoNuovaPartita =
  | { readonly tipo: "Ok"; readonly partita: Partita }
  | { readonly tipo: "Errore"; readonly errori: readonly ErroreConfigurazione[] };

function limita(v: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, v));
}

/** Vive per tutta la sessione: tiene l'insieme delle parole usate. */
export class GestorePartite {
  private readonly casuale: Casuale;
  private readonly usateInterne = new Map<string, ChiaveParola>();
  private ultimaInterna: ChiaveParola | null;

  constructor(
    casuale: Casuale,
    usateIniziali: Iterable<ChiaveParola> = [],
    ultimaIniziale: ChiaveParola | null = null,
  ) {
    this.casuale = casuale;
    for (const k of usateIniziali) this.usateInterne.set(chiaveStringa(k), k);
    this.ultimaInterna = ultimaIniziale;
  }

  get usate(): readonly ChiaveParola[] {
    return [...this.usateInterne.values()];
  }

  get ultima(): ChiaveParola | null {
    return this.ultimaInterna;
  }

  /** Numero di voci di `pool` la cui chiave non e' in `usate`. */
  rimanenti(voci: readonly VoceParola[]): number {
    return voci.filter((v) => !this.usateInterne.has(chiaveStringa(chiaveDi(v)))).length;
  }

  /** Toglie dalle usate le chiavi di `pool`; se `ultima` appartiene a `pool` la mette a null. */
  azzeraUsate(voci: readonly VoceParola[]): void {
    const chiavi = new Set(voci.map((v) => chiaveStringa(chiaveDi(v))));
    for (const k of chiavi) this.usateInterne.delete(k);
    if (this.ultimaInterna !== null && chiavi.has(chiaveStringa(this.ultimaInterna))) {
      this.ultimaInterna = null;
    }
  }

  nuovaPartita(config: Configurazione, categorie: readonly Categoria[]): RisultatoNuovaPartita {
    const errori = valida(config, categorie);
    if (errori.length > 0) return { tipo: "Errore", errori };
    const c = this.casuale;
    const voci = pool(categorie, config.categorieSelezionate, config.modalita);
    let candidati = voci.filter((v) => !this.usateInterne.has(chiaveStringa(chiaveDi(v))));
    if (candidati.length === 0) {
      for (const v of voci) this.usateInterne.delete(chiaveStringa(chiaveDi(v)));
      const ultima = this.ultimaInterna !== null ? chiaveStringa(this.ultimaInterna) : null;
      candidati = voci.length >= 2 ? voci.filter((v) => chiaveStringa(chiaveDi(v)) !== ultima) : voci;
    }
    const voce = candidati[c.intero(candidati.length)];
    const chiave = chiaveDi(voce);
    this.usateInterne.set(chiaveStringa(chiave), chiave);
    this.ultimaInterna = chiave;

    const n = config.numeroGiocatori;
    let k: number;
    if (config.partitaTrappola && reale(c) < PROBABILITA_TRAPPOLA) k = 0;
    else if (config.impostoriSorpresa) k = interoTra(c, 1, config.numeroImpostori + 1);
    else k = config.numeroImpostori;

    const tutti = Array.from({ length: n }, (_, i) => i);
    const impostori = new Set(mescolato(c, tutti).slice(0, k));
    let primo: number;
    if (config.impostoreNonPrimo && k > 0) {
      const civili = tutti.filter((i) => !impostori.has(i));
      primo = civili[c.intero(civili.length)];
    } else {
      primo = c.intero(n);
    }
    const ordine: number[] | null = config.ordineCasuale
      ? [primo, ...mescolato(c, tutti.filter((i) => i !== primo))]
      : null;

    return {
      tipo: "Ok",
      partita: {
        giocatori: nomiEffettivi(config),
        impostori,
        voce,
        modalita: config.modalita,
        mostraCategoria: config.mostraCategoria,
        primoGiocatore: primo,
        ordine,
        giriIndizi: limita(config.giriIndizi, 1, MAX_GIRI),
        promemoriaUltimaPossibilita: config.promemoriaUltimaPossibilita,
      },
    };
  }
}

export const Distribuzione = {
  iniziale(): StatoDistribuzione {
    return { tipo: "Passaggio", indice: 0 };
  },

  avanza(stato: StatoDistribuzione, numeroGiocatori: number): StatoDistribuzione {
    switch (stato.tipo) {
      case "Passaggio":
        return { tipo: "Rivelazione", indice: stato.indice };
      case "Rivelazione":
        return stato.indice + 1 < numeroGiocatori
          ? { tipo: "Passaggio", indice: stato.indice + 1 }
          : { tipo: "Gioco" };
      case "Gioco":
        return stato;
    }
  },

  interrompiRivelazione(stato: StatoDistribuzione): StatoDistribuzione {
    return stato.tipo === "Rivelazione" ? { tipo: "Passaggio", indice: stato.indice } : stato;
  },
} as const;
