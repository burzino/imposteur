import type { Categoria, Configurazione } from "../game/modelli";
import type { SessioneSalvata } from "../game/partita";
import { aspettoAStringa, aspettoDaStringa, aspettoDefault, type Aspetto } from "./aspetto";
import { configurazioneAStringa, configurazioneDaStringa } from "./serializzazioneConfigurazione";
import { sessioneAStringa, sessioneDaStringa } from "./serializzazioneSessione";
import { FormatoSegnalazioni, type Segnalazione } from "./segnalazioni";

/** Sottoinsieme di Storage: in produzione `window.localStorage`, nei test una Map in memoria. */
export interface ArchivioStorage {
  getItem(chiave: string): string | null;
  setItem(chiave: string, valore: string): void;
  removeItem(chiave: string): void;
}

export function storageInMemoria(): ArchivioStorage {
  const m = new Map<string, string>();
  return {
    getItem: (k) => m.get(k) ?? null,
    setItem: (k, v) => {
      m.set(k, v);
    },
    removeItem: (k) => {
      m.delete(k);
    },
  };
}

export const CHIAVI_ARCHIVIO = {
  configurazione: "impostore.configurazione",
  sessione: "impostore.sessione",
  aspetto: "impostore.aspetto",
  segnalazioni: "impostore.segnalazioni", // testo JSONL
} as const;

export interface Archivio {
  leggiConfigurazione(categorie: readonly Categoria[]): Configurazione;
  salvaConfigurazione(config: Configurazione): void;
  leggiSessione(categorie: readonly Categoria[]): SessioneSalvata;
  salvaSessione(s: SessioneSalvata): void;
  leggiAspetto(): Aspetto;
  salvaAspetto(a: Aspetto): void;
  aggiungiSegnalazione(s: Segnalazione): void; // append di riga + "\n"
  leggiSegnalazioni(): Segnalazione[];
  contaSegnalazioni(): number;
  cancellaSegnalazioni(): void;
}

export function creaArchivio(storage: ArchivioStorage): Archivio {
  // Gli errori di storage sono assorbiti: letture -> null (quindi default), scritture -> ignorate.
  const leggi = (chiave: string): string | null => {
    try {
      return storage.getItem(chiave);
    } catch {
      return null;
    }
  };
  const scrivi = (chiave: string, valore: string): void => {
    try {
      storage.setItem(chiave, valore);
    } catch {
      // quota piena, modalita' privata, ecc.
    }
  };
  const rimuovi = (chiave: string): void => {
    try {
      storage.removeItem(chiave);
    } catch {
      // ignorato
    }
  };
  const segnalazioni = (): Segnalazione[] => FormatoSegnalazioni.leggi(leggi(CHIAVI_ARCHIVIO.segnalazioni) ?? "");

  return {
    leggiConfigurazione: (categorie) => configurazioneDaStringa(leggi(CHIAVI_ARCHIVIO.configurazione), categorie),
    salvaConfigurazione: (config) => scrivi(CHIAVI_ARCHIVIO.configurazione, configurazioneAStringa(config)),
    leggiSessione: (categorie) => sessioneDaStringa(leggi(CHIAVI_ARCHIVIO.sessione), categorie),
    salvaSessione: (s) => scrivi(CHIAVI_ARCHIVIO.sessione, sessioneAStringa(s)),
    leggiAspetto: () => {
      const s = leggi(CHIAVI_ARCHIVIO.aspetto);
      return s === null ? aspettoDefault : aspettoDaStringa(s);
    },
    salvaAspetto: (a) => scrivi(CHIAVI_ARCHIVIO.aspetto, aspettoAStringa(a)),
    aggiungiSegnalazione: (s) => {
      const precedente = leggi(CHIAVI_ARCHIVIO.segnalazioni) ?? "";
      scrivi(CHIAVI_ARCHIVIO.segnalazioni, precedente + FormatoSegnalazioni.riga(s) + "\n");
    },
    leggiSegnalazioni: segnalazioni,
    contaSegnalazioni: () => segnalazioni().length,
    cancellaSegnalazioni: () => rimuovi(CHIAVI_ARCHIVIO.segnalazioni),
  };
}
