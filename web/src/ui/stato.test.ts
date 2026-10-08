import { afterEach, describe, expect, it, vi } from "vitest";
import { CHIAVI_ARCHIVIO, creaArchivio, storageInMemoria, type ArchivioStorage } from "../data/archivio";
import { segnalazione } from "../data/segnalazioni";
import { casualeConSeme } from "../game/casuale";
import { categorieDemo } from "../game/fixture";
import type { Categoria } from "../game/modelli";
import { contenutoPer, type StatoDistribuzione } from "../game/partita";
import { StatoApp } from "./stato.svelte";
import { temaScuroAttivo } from "./tema";

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

function crea(storage: ArchivioStorage = storageInMemoria(), ritardo = 0): { s: StatoApp; storage: ArchivioStorage } {
  const s = new StatoApp({
    archivio: creaArchivio(storage),
    casuale: casualeConSeme(7),
    caricaCategorie: async () => ({ tipo: "Ok", categorie: categorieDemo() }),
    ritardoSalvataggioMs: ritardo,
  });
  return { s, storage };
}

async function pronto(storage?: ArchivioStorage): Promise<{ s: StatoApp; storage: ArchivioStorage }> {
  const r = crea(storage);
  await r.s.pronto;
  return r;
}

describe("StatoApp: caricamento", () => {
  it("CA-22 parte in caricamento e poi espone le categorie", async () => {
    const { s } = crea();
    expect(s.caricamento).toBe(true);
    expect(s.puoIniziare).toBe(false);
    await s.pronto;
    expect(s.caricamento).toBe(false);
    expect(s.categorie.length).toBe(2);
    expect(s.partita).toBeNull();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
  });

  it("CA-34 errore di caricamento (anche con eccezione)", async () => {
    const s = new StatoApp({
      archivio: creaArchivio(storageInMemoria()),
      casuale: casualeConSeme(1),
      caricaCategorie: async () => {
        throw new Error("rete");
      },
    });
    await s.pronto;
    expect(s.erroreCaricamento).toBe(true);
    expect(s.caricamento).toBe(false);
    expect(s.puoIniziare).toBe(false);
    expect(s.iniziaPartita()).toBe(false);
  });

  it("CA-22 le modifiche di configurazione durante il caricamento sono ignorate", () => {
    const { s } = crea();
    s.impostaNumeroGiocatori(9);
    expect(s.config.numeroGiocatori).toBe(4);
  });
});

describe("StatoApp: configurazione", () => {
  it("CA-23 CA-63 corregge ai limiti e aggiorna il contatore", async () => {
    const { s } = await pronto();
    s.impostaNumeroGiocatori(99);
    expect(s.config.numeroGiocatori).toBe(20);
    s.impostaNumeroGiocatori(1);
    expect(s.config.numeroGiocatori).toBe(3);
    s.impostaNumeroImpostori(50);
    expect(s.config.numeroImpostori).toBeLessThan(3);
    s.selezionaTutte(true);
    expect(s.paroleTotali).toBe(5);
    expect(s.paroleRimanenti).toBe(5);
    s.impostaCategoria("cibo", false);
    expect(s.paroleTotali).toBe(3);
    expect(s.config.categorieSelezionate.has("animali")).toBe(true);
  });

  it("CA-05 tronca i nomi alla lunghezza massima", async () => {
    const { s } = await pronto();
    s.impostaNome(2, "x".repeat(40));
    expect(s.config.nomi[2]?.length).toBe(20);
    expect(s.config.nomi[0]).toBe("");
  });

  it("CA-03 CA-25 senza categorie non si puo' iniziare", async () => {
    const { s } = await pronto();
    s.selezionaTutte(false);
    expect(s.errori.length).toBeGreaterThan(0);
    expect(s.iniziaPartita()).toBe(false);
  });

  it("CA-26 CA-83 salvataggio con debounce e salvaOra immediato", async () => {
    vi.useFakeTimers();
    try {
      const storage = storageInMemoria();
      const { s } = crea(storage, 500);
      await s.pronto;
      s.impostaNome(0, "Anna");
      const letta = (): string | null => storage.getItem("impostore.configurazione");
      expect(letta()).toBeNull();
      vi.advanceTimersByTime(499);
      expect(letta()).toBeNull();
      vi.advanceTimersByTime(2);
      expect(letta()).toContain("Anna");
      s.impostaNome(0, "Bea");
      s.salvaOra();
      expect(letta()).toContain("Bea");
    } finally {
      vi.useRealTimers();
    }
  });
});

describe("StatoApp: partita", () => {
  async function inPartita(): Promise<{ s: StatoApp; storage: ArchivioStorage }> {
    const r = await pronto();
    r.s.selezionaTutte(true);
    r.s.impostaNumeroGiocatori(3);
    expect(r.s.iniziaPartita()).toBe(true);
    return r;
  }

  it("CA-18 distribuzione completa e ignora i tocchi doppi", async () => {
    const { s } = await inPartita();
    const p0 = s.distribuzione;
    s.avanza(p0);
    expect(s.distribuzione).toEqual({ tipo: "Rivelazione", indice: 0 });
    s.avanza(p0); // doppio tocco su uno stato vecchio
    expect(s.distribuzione).toEqual({ tipo: "Rivelazione", indice: 0 });
    for (let i = 0; i < 5; i++) s.avanza(s.distribuzione);
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
  });

  it("CA-W06 CA-29 pagina nascosta: Rivelazione(k) torna a Passaggio(k)", async () => {
    const { s } = await inPartita();
    s.avanza(s.distribuzione);
    s.nascondiRuolo();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
  });

  it("CA-W07 CA-39 CA-45 CA-46 sospendi e riprendi, anche dopo un ricaricamento (Rivelazione diventa Passaggio)", async () => {
    const { s, storage } = await inPartita();
    s.avanza(s.distribuzione);
    s.avanza(s.distribuzione);
    s.avanza(s.distribuzione); // Rivelazione(1)
    s.sospendiPartita();
    expect(s.ripristinabile).not.toBeNull();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 1 });

    const { s: s2 } = await pronto(storage);
    expect(s2.partita).toBeNull();
    expect(s2.ripristinabile).not.toBeNull();
    expect(s2.riprendiPartita()).toBe(false);
    expect(s2.distribuzione).toEqual({ tipo: "Passaggio", indice: 1 });
    expect(s2.partita?.giocatori.length).toBe(3);
    expect(s2.ripristinabile).toBeNull();
  });

  it("CA-47 riprendere dal Gioco restituisce true", async () => {
    const { s, storage } = await inPartita();
    for (let i = 0; i < 6; i++) s.avanza(s.distribuzione);
    s.sospendiPartita();
    const { s: s2 } = await pronto(storage);
    expect(s2.riprendiPartita()).toBe(true);
    expect(s2.distribuzione).toEqual({ tipo: "Gioco" });
  });

  it("CA-48 CA-50 entraInRivela cancella la partita salvata ma conserva le parole usate", async () => {
    const { s, storage } = await inPartita();
    s.entraInRivela();
    const { s: s2 } = await pronto(storage);
    expect(s2.ripristinabile).toBeNull();
    s2.selezionaTutte(true);
    expect(s2.paroleRimanenti).toBe(4);
    s2.azzeraParole();
    expect(s2.paroleRimanenti).toBe(5);
  });

  it("CA-48 terminaPartita azzera partita e revisione", async () => {
    const { s } = await inPartita();
    s.scegliRevisione(0);
    s.terminaPartita();
    expect(s.partita).toBeNull();
    expect(s.revisione).toBeNull();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
  });
});

describe("StatoApp: revisione", () => {
  it("CA-W06 CA-52 CA-55 CA-57 scegli, rivela, nascondi con pagina nascosta, chiudi", async () => {
    const { s } = await pronto();
    s.selezionaTutte(true);
    s.iniziaPartita();
    s.scegliRevisione(99);
    expect(s.revisione).toBeNull();
    s.scegliRevisione(1);
    s.scegliRevisione(2); // ignorata: ce n'e' gia' una
    expect(s.revisione).toEqual({ indice: 1, rivelato: false });
    s.rivelaRevisione();
    expect(s.revisione).toEqual({ indice: 1, rivelato: true });
    s.nascondiRuolo();
    expect(s.revisione).toEqual({ indice: 1, rivelato: false });
    s.tornaAElencoRevisione();
    expect(s.revisione).toBeNull();
    s.scegliRevisione(0);
    s.chiudiRevisione();
    expect(s.revisione).toBeNull();
  });
});

describe("StatoApp: aspetto e segnalazioni", () => {
  it("CA-W14 CA-W16 l'aspetto si salva e si rilegge; localStorage che lancia non rompe nulla", async () => {
    const { s, storage } = await pronto();
    s.impostaAspetto({ tema: "SCURO", coloriDinamici: true });
    expect(s.aspetto.tema).toBe("SCURO");
    const s2 = crea(storage).s;
    expect(s2.aspetto.tema).toBe("SCURO");

    const rotto: ArchivioStorage = {
      getItem: () => {
        throw new Error("bloccato");
      },
      setItem: () => {
        throw new Error("bloccato");
      },
      removeItem: () => {
        throw new Error("bloccato");
      },
    };
    const { s: s3 } = await pronto(rotto);
    s3.selezionaTutte(true);
    expect(s3.iniziaPartita()).toBe(true);
  });

  it("CA-W15 salva, conta, esporta e cancella le segnalazioni", async () => {
    const { s } = await pronto();
    expect(s.segnalazioniSalvate).toBe(0);
    expect(s.leggiSegnalazioniJsonl()).toBe("");
    s.salvaSegnalazione({
      tipo: "app",
      istante: "2026-01-01T10:00:00",
      categoriaId: null,
      parola: null,
      affine: null,
      modalita: null,
      motivi: [],
      nota: "ciao",
      propostaParola: null,
      propostaAffine: null,
    });
    expect(s.segnalazioniSalvate).toBe(1);
    const jsonl = s.leggiSegnalazioniJsonl();
    expect(jsonl.endsWith("\n")).toBe(true);
    expect(jsonl.split("\n").filter(Boolean).length).toBe(1);
    s.cancellaSegnalazioni();
    expect(s.segnalazioniSalvate).toBe(0);
  });
});

// ======================================================================================
// Integrazioni del tester: copertura di CA-xx (specifiche.md) e CA-Wxx (specifiche-web.md W10)
// ======================================================================================

const SESSIONE = CHIAVI_ARCHIVIO.sessione;
const chiaveVoce = (v: { categoriaId: string; parola: string }): string => `${v.categoriaId}/${v.parola}`;

function creaCon(storage: ArchivioStorage, categorie: Categoria[] = categorieDemo(), seme = 7): StatoApp {
  return new StatoApp({
    archivio: creaArchivio(storage),
    casuale: casualeConSeme(seme),
    caricaCategorie: async () => ({ tipo: "Ok", categorie }),
    ritardoSalvataggioMs: 0,
  });
}

/** Stato pronto con tutte le categorie, `n` giocatori e `k` impostori. */
async function configurato(
  n = 4,
  k = 1,
  storage: ArchivioStorage = storageInMemoria(),
): Promise<{ s: StatoApp; storage: ArchivioStorage }> {
  const r = await pronto(storage);
  r.s.selezionaTutte(true);
  r.s.impostaNumeroGiocatori(n);
  r.s.impostaNumeroImpostori(k);
  return r;
}

function sessioneSalvata(s: StatoApp, storage: ArchivioStorage) {
  return creaArchivio(storage).leggiSessione(s.categorie);
}

function inGioco(s: StatoApp): void {
  const n = s.partita?.giocatori.length ?? 0;
  for (let i = 0; i < 2 * n; i++) s.avanza(s.distribuzione);
}

describe("StatoApp: configurazione iniziale e vincoli (CA-02, CA-04, CA-05, CA-06, CA-22, CA-23)", () => {
  it("CA-22 al primo avvio: 4 giocatori, 1 impostore, senza parola, categoria visibile, tutte le categorie", async () => {
    const { s } = await pronto();
    expect(s.config.numeroGiocatori).toBe(4);
    expect(s.config.numeroImpostori).toBe(1);
    expect(s.config.modalita).toBe("SENZA_PAROLA");
    expect(s.config.mostraCategoria).toBe(true);
    expect([...s.config.categorieSelezionate].sort()).toEqual(["animali", "cibo"]);
    expect(s.puoIniziare).toBe(true);
    expect(s.errori).toEqual([]);
  });

  it("CA-06 CA-23 riducendo i giocatori gli impostori scendono al massimo; aumentando restano", async () => {
    const { s } = await pronto();
    s.impostaNumeroGiocatori(7);
    s.impostaNumeroImpostori(99);
    expect(s.config.numeroImpostori).toBe(3);
    s.impostaNumeroGiocatori(4);
    expect(s.config.numeroImpostori).toBe(1);
    s.impostaNumeroGiocatori(10);
    expect(s.config.numeroImpostori).toBe(1);
    s.impostaNumeroImpostori(0);
    expect(s.config.numeroImpostori).toBe(1);
  });

  it("CA-02 la validazione distingue giocatori e impostori fuori limite", async () => {
    const { s } = await pronto();
    const tipi = (): string[] => s.errori.map((e) => e.tipo);
    s.impostaOpzione((c) => ({ ...c, numeroImpostori: 2 })); // N=4: massimo 1
    expect(tipi()).toContain("TroppiImpostori");
    s.impostaOpzione((c) => ({ ...c, numeroImpostori: 0 }));
    expect(tipi()).toContain("TroppoPochiImpostori");
    s.impostaOpzione((c) => ({ ...c, numeroImpostori: 1, numeroGiocatori: 2 }));
    expect(tipi()).toContain("TroppoPochiGiocatori");
    s.impostaOpzione((c) => ({ ...c, numeroGiocatori: 21 }));
    expect(tipi()).toContain("TroppiGiocatori");
    expect(s.puoIniziare).toBe(false);
  });

  it("CA-02 configurazione non valida: iniziaPartita false, nessuna partita e nessuna sessione salvata", async () => {
    const { s, storage } = await pronto();
    s.impostaOpzione((c) => ({ ...c, numeroImpostori: 2 })); // TroppiImpostori con N=4
    expect(s.puoIniziare).toBe(false);
    expect(s.iniziaPartita()).toBe(false);
    expect(s.partita).toBeNull();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
    expect(storage.getItem(SESSIONE)).toBeNull();
  });

  it("CA-03 CA-25 nessuna categoria: errore NessunaCategoria e nessuna partita", async () => {
    const { s } = await pronto();
    s.selezionaTutte(false);
    expect(s.errori.map((e) => e.tipo)).toEqual(["NessunaCategoria"]);
    expect(s.puoIniziare).toBe(false);
    expect(s.iniziaPartita()).toBe(false);
    expect(s.partita).toBeNull();
    s.impostaCategoria("cibo", true);
    expect(s.puoIniziare).toBe(true);
  });

  it("CA-04 CA-25 nome duplicato (maiuscole e spazi ignorati): errore e Inizia bloccato", async () => {
    const { s } = await pronto();
    s.impostaNome(0, "Anna");
    s.impostaNome(1, "  ANNA ");
    expect(s.errori).toContainEqual({ tipo: "NomeDuplicato", indici: [0, 1] });
    expect(s.puoIniziare).toBe(false);
    expect(s.iniziaPartita()).toBe(false);
    expect(s.partita).toBeNull();
    s.impostaNome(1, "Bea");
    expect(s.puoIniziare).toBe(true);
  });

  it("CA-04 un nome uguale al nome di default di un altro giocatore e' duplicato", async () => {
    const { s } = await pronto();
    s.impostaNome(0, "giocatore 2"); // il giocatore 2 si chiama "Giocatore 2"
    expect(s.errori).toContainEqual({ tipo: "NomeDuplicato", indici: [0, 1] });
    expect(s.iniziaPartita()).toBe(false);
  });

  it("CA-05 nome troppo lungo (forzato con impostaOpzione) e' rifiutato", async () => {
    const { s } = await pronto();
    s.impostaOpzione((c) => ({ ...c, nomi: ["x".repeat(21)] }));
    expect(s.errori).toContainEqual({ tipo: "NomeTroppoLungo", indice: 0 });
    expect(s.iniziaPartita()).toBe(false);
  });

  it("CA-05 nomi vuoti o spaziati: 'Giocatore n' e trim nella partita", async () => {
    const { s } = await configurato(3, 1);
    s.impostaNome(0, "  Anna  ");
    s.impostaNome(1, "");
    s.impostaNome(2, "   ");
    expect(s.iniziaPartita()).toBe(true);
    expect(s.partita?.giocatori).toEqual(["Anna", "Giocatore 2", "Giocatore 3"]);
  });
});

describe("StatoApp: avvio partita da configurazione valida (CA-07, CA-10, CA-12, CA-13, CA-20)", () => {
  it("CA-07 CA-12 la partita ha K impostori distinti in [0,N), parola di una categoria scelta, Passaggio 0", async () => {
    for (const [n, k] of [[3, 1], [5, 2], [7, 3], [20, 9]] as const) {
      const { s, storage } = await configurato(n, k);
      s.impostaCategoria("cibo", false);
      expect(s.iniziaPartita()).toBe(true);
      const p = s.partita;
      expect(p).not.toBeNull();
      expect(p?.giocatori.length).toBe(n);
      expect(p?.impostori.size).toBe(k);
      for (const i of p?.impostori ?? []) {
        expect(Number.isInteger(i)).toBe(true);
        expect(i).toBeGreaterThanOrEqual(0);
        expect(i).toBeLessThan(n);
      }
      expect(p?.voce.categoriaId).toBe("animali");
      expect(p?.primoGiocatore).toBeGreaterThanOrEqual(0);
      expect(p?.primoGiocatore).toBeLessThan(n);
      expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
      expect(s.ripristinabile).toBeNull();
      expect(storage.getItem(SESSIONE)).not.toBeNull();
    }
  });

  it("CA-13 CA-61 iniziare consuma una parola: il contatore scende di uno", async () => {
    const { s } = await configurato(4, 1);
    expect(s.paroleRimanenti).toBe(5);
    expect(s.iniziaPartita()).toBe(true);
    expect(s.paroleRimanenti).toBe(4);
    expect(s.paroleTotali).toBe(5);
  });

  it("CA-10 modalita' Parola affine: civili e impostori ricevono lo stesso tipo di contenuto", async () => {
    const { s } = await configurato(5, 2);
    s.impostaModalita("PAROLA_AFFINE");
    expect(s.iniziaPartita()).toBe(true);
    const p = s.partita;
    if (p === null) throw new Error("partita attesa");
    for (let i = 0; i < p.giocatori.length; i++) {
      const c = contenutoPer(p, i);
      expect(c.tipo).toBe("ParolaSegreta");
      const testo = c.tipo === "ParolaSegreta" ? c.testo : "";
      expect(testo).toBe(p.impostori.has(i) ? p.voce.affine : p.voce.parola);
    }
  });

  it("CA-20 nuova partita con le stesse impostazioni: stessi giocatori e opzioni, parola nuova", async () => {
    const { s } = await configurato(5, 2);
    s.impostaNome(0, "Anna");
    s.impostaNome(1, "Bea");
    expect(s.iniziaPartita()).toBe(true);
    const prima = s.partita!;
    s.terminaPartita();
    expect(s.config.nomi[0]).toBe("Anna");
    expect(s.iniziaPartita()).toBe(true);
    const dopo = s.partita!;
    expect(dopo.giocatori).toEqual(prima.giocatori);
    expect(dopo.impostori.size).toBe(2);
    expect(dopo.modalita).toBe(prima.modalita);
    expect(chiaveVoce(dopo.voce)).not.toBe(chiaveVoce(prima.voce));
  });

  it("CA-26 iniziaPartita salva subito la configurazione anche con debounce", async () => {
    vi.useFakeTimers();
    const storage = storageInMemoria();
    const { s } = crea(storage, 500);
    await s.pronto;
    s.impostaNome(0, "Zoe");
    expect(storage.getItem(CHIAVI_ARCHIVIO.configurazione)).toBeNull();
    expect(s.iniziaPartita()).toBe(true);
    expect(storage.getItem(CHIAVI_ARCHIVIO.configurazione)).toContain("Zoe");
  });
});

describe("StatoApp: distribuzione (CA-18, CA-28, CA-31)", () => {
  it("CA-18 CA-31 sequenza completa Passaggio k, Rivelazione k, ..., Gioco, mai all'indietro", async () => {
    const { s } = await configurato(3, 1);
    s.iniziaPartita();
    const visti: StatoDistribuzione[] = [s.distribuzione];
    for (let i = 0; i < 6; i++) {
      s.avanza(s.distribuzione);
      visti.push(s.distribuzione);
    }
    expect(visti).toEqual([
      { tipo: "Passaggio", indice: 0 },
      { tipo: "Rivelazione", indice: 0 },
      { tipo: "Passaggio", indice: 1 },
      { tipo: "Rivelazione", indice: 1 },
      { tipo: "Passaggio", indice: 2 },
      { tipo: "Rivelazione", indice: 2 },
      { tipo: "Gioco" },
    ]);
    s.avanza({ tipo: "Gioco" });
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
  });

  it("CA-18 avanza con uno stato diverso dal corrente e' ignorato (mai salti o ritorni)", async () => {
    const { s } = await configurato(3, 1);
    s.iniziaPartita();
    s.avanza({ tipo: "Rivelazione", indice: 0 }); // salto
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
    s.avanza({ tipo: "Passaggio", indice: 1 });
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
    s.avanza(s.distribuzione);
    s.avanza({ tipo: "Passaggio", indice: 0 }); // vecchio stato
    expect(s.distribuzione).toEqual({ tipo: "Rivelazione", indice: 0 });
  });

  it("CA-18 senza partita avanza non fa nulla", async () => {
    const { s, storage } = await pronto();
    s.avanza({ tipo: "Passaggio", indice: 0 });
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
    expect(storage.getItem(SESSIONE)).toBeNull();
  });
});

describe("StatoApp: nascondiRuolo (CA-W06, CA-29, CA-39, CA-57, CA-60)", () => {
  it("CA-29 CA-39 nascondiRuolo in Rivelazione(k) -> Passaggio(k) per ogni k, e lo salva", async () => {
    const { s, storage } = await configurato(4, 1);
    s.iniziaPartita();
    for (let k = 0; k < 4; k++) {
      s.avanza(s.distribuzione); // Rivelazione(k)
      expect(s.distribuzione).toEqual({ tipo: "Rivelazione", indice: k });
      s.nascondiRuolo();
      expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: k });
      expect(sessioneSalvata(s, storage).stato).toEqual({ tipo: "Passaggio", indice: k });
      s.avanza(s.distribuzione);
      s.avanza(s.distribuzione); // Rivelazione(k) -> Passaggio(k+1) o Gioco
    }
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
  });

  it("CA-39 nascondiRuolo lascia invariati Passaggio e Gioco", async () => {
    const { s } = await configurato(3, 1);
    s.iniziaPartita();
    s.nascondiRuolo();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
    inGioco(s);
    s.nascondiRuolo();
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
  });

  it("CA-29 nascondiRuolo senza partita non fa nulla e non scrive sessioni", async () => {
    const { s, storage } = await pronto();
    s.nascondiRuolo();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
    expect(s.partita).toBeNull();
    expect(storage.getItem(SESSIONE)).toBeNull();
  });

  it("CA-26 nascondiRuolo salva subito la configurazione in attesa del debounce", async () => {
    vi.useFakeTimers();
    const storage = storageInMemoria();
    const { s } = crea(storage, 500);
    await s.pronto;
    s.impostaNome(0, "Carla");
    expect(storage.getItem(CHIAVI_ARCHIVIO.configurazione)).toBeNull();
    s.nascondiRuolo();
    expect(storage.getItem(CHIAVI_ARCHIVIO.configurazione)).toContain("Carla");
  });

  it("CA-W06 CA-57 in Rivedi nascondiRuolo imposta solo rivelato=false, senza toccare Gioco e partita", async () => {
    const { s } = await configurato(3, 1);
    s.iniziaPartita();
    inGioco(s);
    const partita = s.partita;
    s.scegliRevisione(2);
    s.rivelaRevisione();
    expect(s.revisione).toEqual({ indice: 2, rivelato: true });
    s.nascondiRuolo();
    expect(s.revisione).toEqual({ indice: 2, rivelato: false });
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
    expect(s.partita).toBe(partita);
    s.nascondiRuolo(); // idempotente
    expect(s.revisione).toEqual({ indice: 2, rivelato: false });
  });
});

describe("StatoApp: salvataggio a ogni cambio di stato e ripristino all'avvio (CA-37, CA-45..CA-48, CA-83)", () => {
  it("CA-37 CA-45 ogni cambio di stato aggiorna la sessione salvata", async () => {
    const { s, storage } = await configurato(3, 1);
    s.iniziaPartita();
    expect(sessioneSalvata(s, storage).stato).toEqual({ tipo: "Passaggio", indice: 0 });
    expect(sessioneSalvata(s, storage).partita?.giocatori.length).toBe(3);
    const attesi: StatoDistribuzione[] = [
      { tipo: "Passaggio", indice: 0 }, // Rivelazione(0) si rilegge normalizzata (CA-39)
      { tipo: "Passaggio", indice: 1 },
      { tipo: "Passaggio", indice: 1 },
      { tipo: "Passaggio", indice: 2 },
      { tipo: "Passaggio", indice: 2 },
      { tipo: "Gioco" },
    ];
    for (const atteso of attesi) {
      s.avanza(s.distribuzione);
      expect(sessioneSalvata(s, storage).stato).toEqual(atteso);
    }
  });

  it("CA-45 chiusura (senza sospendi) in Passaggio k: al riavvio stessa partita offerta in Passaggio k", async () => {
    const { s, storage } = await configurato(5, 2);
    s.iniziaPartita();
    for (let i = 0; i < 4; i++) s.avanza(s.distribuzione); // Passaggio(2)
    const originale = s.partita;
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.partita).toBeNull(); // non attiva finche' non si riprende
    expect(s2.ripristinabile?.stato).toEqual({ tipo: "Passaggio", indice: 2 });
    expect(s2.ripristinabile?.partita?.giocatori).toEqual(originale?.giocatori);
    expect([...(s2.ripristinabile?.partita?.impostori ?? [])].sort()).toEqual([...(originale?.impostori ?? [])].sort());
    expect(s2.ripristinabile?.partita?.voce).toEqual(originale?.voce);
    expect(s2.ripristinabile?.partita?.primoGiocatore).toBe(originale?.primoGiocatore);
    expect(s2.riprendiPartita()).toBe(false);
    expect(s2.distribuzione).toEqual({ tipo: "Passaggio", indice: 2 });
  });

  it("CA-W07 CA-46 chiusura con il ruolo visibile: al riavvio Passaggio k, mai Rivelazione k", async () => {
    const { s, storage } = await configurato(3, 1);
    s.iniziaPartita();
    for (let i = 0; i < 3; i++) s.avanza(s.distribuzione); // Rivelazione(1), nessun nascondiRuolo
    expect(s.distribuzione).toEqual({ tipo: "Rivelazione", indice: 1 });
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.ripristinabile?.stato).toEqual({ tipo: "Passaggio", indice: 1 });
    expect(s2.riprendiPartita()).toBe(false);
    expect(s2.distribuzione).toEqual({ tipo: "Passaggio", indice: 1 });
  });

  it("CA-47 chiusura in Gioco: riprendiPartita porta al Gioco con la stessa partita", async () => {
    const { s, storage } = await configurato(4, 1);
    s.iniziaPartita();
    inGioco(s);
    const primo = s.partita?.primoGiocatore;
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.riprendiPartita()).toBe(true);
    expect(s2.distribuzione).toEqual({ tipo: "Gioco" });
    expect(s2.partita?.primoGiocatore).toBe(primo);
    expect(s2.ripristinabile).toBeNull();
  });

  it("CA-W07 CA-59 chiusura durante Rivedi: al riavvio si torna al Gioco, nessuno stato di revisione", async () => {
    const { s, storage } = await configurato(3, 1);
    s.iniziaPartita();
    inGioco(s);
    s.scegliRevisione(1);
    s.rivelaRevisione();
    expect(sessioneSalvata(s, storage).stato).toEqual({ tipo: "Gioco" });
    expect(storage.getItem(SESSIONE)).not.toMatch(/revisione|rivelato/i);
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.revisione).toBeNull();
    expect(s2.riprendiPartita()).toBe(true);
    expect(s2.revisione).toBeNull();
  });

  it("CA-60 la revisione non modifica la distribuzione ne' la sessione salvata", async () => {
    const { s, storage } = await configurato(3, 1);
    s.iniziaPartita();
    inGioco(s);
    const prima = storage.getItem(SESSIONE);
    s.scegliRevisione(0);
    s.rivelaRevisione();
    s.nascondiRuolo();
    s.tornaAElencoRevisione();
    s.scegliRevisione(1);
    s.chiudiRevisione();
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
    expect(storage.getItem(SESSIONE)).toBe(prima);
  });

  it("CA-48 terminaPartita (Interrompi) cancella la partita salvata: al riavvio niente Riprendi", async () => {
    const { s, storage } = await configurato(3, 1);
    s.iniziaPartita();
    s.avanza(s.distribuzione);
    s.terminaPartita();
    expect(s.ripristinabile).toBeNull();
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.ripristinabile).toBeNull();
    expect(s2.riprendiPartita()).toBe(false);
  });

  it("CA-48 entraInRivela cancella la partita salvata", async () => {
    const { s, storage } = await configurato(3, 1);
    s.iniziaPartita();
    inGioco(s);
    s.entraInRivela();
    expect(sessioneSalvata(s, storage).partita).toBeNull();
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.ripristinabile).toBeNull();
  });

  it("CA-43 senza partita salvata niente ripresa; riprendiPartita senza ripristinabile e' false", async () => {
    const { s } = await pronto();
    expect(s.ripristinabile).toBeNull();
    expect(s.riprendiPartita()).toBe(false);
    expect(s.partita).toBeNull();
  });

  it("CA-44 una nuova partita sostituisce quella ripristinabile", async () => {
    const { s, storage } = await configurato(4, 1);
    s.iniziaPartita();
    s.sospendiPartita();
    expect(s.ripristinabile).not.toBeNull();
    expect(s.iniziaPartita()).toBe(true);
    expect(s.ripristinabile).toBeNull();
    expect(s.distribuzione).toEqual({ tipo: "Passaggio", indice: 0 });
    expect(sessioneSalvata(s, storage).stato).toEqual({ tipo: "Passaggio", indice: 0 });
  });

  it("CA-45 sospendiPartita senza partita non fa nulla", async () => {
    const { s, storage } = await pronto();
    s.sospendiPartita();
    expect(s.ripristinabile).toBeNull();
    expect(storage.getItem(SESSIONE)).toBeNull();
  });

  it("CA-26 CA-83 la configurazione (anche opzioni avanzate) si ritrova al riavvio", async () => {
    const { s, storage } = await pronto();
    s.impostaNumeroGiocatori(8);
    s.impostaNumeroImpostori(3);
    s.impostaNome(0, "Anna");
    s.impostaModalita("PAROLA_AFFINE");
    s.impostaMostraCategoria(false);
    s.impostaCategoria("cibo", false);
    s.impostaOpzione((c) => ({ ...c, giriIndizi: 3, ordineCasuale: true, partitaTrappola: true }));
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.config.numeroGiocatori).toBe(8);
    expect(s2.config.numeroImpostori).toBe(3);
    expect(s2.config.nomi[0]).toBe("Anna");
    expect(s2.config.modalita).toBe("PAROLA_AFFINE");
    expect(s2.config.mostraCategoria).toBe(false);
    expect([...s2.config.categorieSelezionate]).toEqual(["animali"]);
    expect(s2.config.giriIndizi).toBe(3);
    expect(s2.config.ordineCasuale).toBe(true);
    expect(s2.config.partitaTrappola).toBe(true);
  });
});

describe("StatoApp: sessione incoerente scartata in silenzio (CA-40, CA-41, CA-49, CA-W03)", () => {
  async function conSessioneValida(modalita: "SENZA_PAROLA" | "PAROLA_AFFINE" = "SENZA_PAROLA") {
    const r = await configurato(4, 1);
    r.s.impostaModalita(modalita);
    r.s.iniziaPartita();
    r.s.avanza(r.s.distribuzione);
    r.s.sospendiPartita();
    const grezza = r.storage.getItem(SESSIONE);
    if (grezza === null) throw new Error("sessione attesa");
    return { ...r, grezza, voce: r.s.partita?.voce };
  }

  async function ripristinaCon(storage: ArchivioStorage, categorie?: Categoria[]): Promise<StatoApp> {
    const s2 = creaCon(storage, categorie);
    await s2.pronto;
    expect(s2.erroreCaricamento).toBe(false);
    expect(s2.caricamento).toBe(false);
    expect(s2.partita).toBeNull();
    return s2;
  }

  it("la sessione valida e' ripristinabile con le stesse categorie (controllo di sanita')", async () => {
    const { storage } = await conSessioneValida();
    expect((await ripristinaCon(storage)).ripristinabile).not.toBeNull();
  });

  it("CA-40 CA-49 JSON malformato o non oggetto: nessuna ripresa e nessuna eccezione", async () => {
    for (const rotto of ["{non json", "[]", "42", "null", ""]) {
      const storage = storageInMemoria();
      storage.setItem(SESSIONE, rotto);
      const s2 = await ripristinaCon(storage);
      expect(s2.ripristinabile).toBeNull();
      expect(s2.riprendiPartita()).toBe(false);
    }
  });

  it("CA-40 impostori duplicati o fuori limite, stato fuori limite o sconosciuto, campo mancante: scartata", async () => {
    const { grezza } = await conSessioneValida();
    const modifiche: Array<(o: any) => void> = [
      (o) => (o.partita.impostori = [0, 0]),
      (o) => (o.partita.impostori = [99]),
      (o) => (o.partita.impostori = [-1]),
      (o) => (o.stato.indice = 99),
      (o) => (o.stato.tipo = "BOH"),
      (o) => delete o.partita.giocatori,
    ];
    for (const m of modifiche) {
      const o = JSON.parse(grezza);
      m(o);
      const storage = storageInMemoria();
      storage.setItem(SESSIONE, JSON.stringify(o));
      const s2 = await ripristinaCon(storage);
      expect(s2.ripristinabile).toBeNull();
    }
  });

  it("CA-41 CA-49 categoria rimossa da parole.json: sessione scartata", async () => {
    const { storage, voce } = await conSessioneValida();
    const senza = categorieDemo().filter((c) => c.id !== voce?.categoriaId);
    expect(senza.length).toBe(1);
    expect((await ripristinaCon(storage, senza)).ripristinabile).toBeNull();
  });

  it("CA-41 CA-49 parola rimossa da parole.json: sessione scartata", async () => {
    const { storage, voce } = await conSessioneValida();
    const senza = categorieDemo().map((c) => ({ ...c, parole: c.parole.filter((p) => p.parola !== voce?.parola) }));
    expect((await ripristinaCon(storage, senza)).ripristinabile).toBeNull();
  });

  it("CA-41 affine cambiato in modalita' Parola affine: sessione scartata", async () => {
    const { storage, voce } = await conSessioneValida("PAROLA_AFFINE");
    const cambiato = categorieDemo().map((c) => ({
      ...c,
      parole: c.parole.map((p) => (p.parola === voce?.parola ? { ...p, affine: "Altro" } : p)),
    }));
    expect((await ripristinaCon(storage, cambiato)).ripristinabile).toBeNull();
  });

  it("CA-W03 configurazione illeggibile: valori di default senza eccezioni", async () => {
    const storage = storageInMemoria();
    storage.setItem(CHIAVI_ARCHIVIO.configurazione, "%%%");
    const s2 = await ripristinaCon(storage);
    expect(s2.config.numeroGiocatori).toBe(4);
    expect(s2.puoIniziare).toBe(true);
  });
});

describe("StatoApp: errore di caricamento (CA-34, CA-W05)", () => {
  it("CA-34 risultato Errore del parser: erroreCaricamento, niente partita, configurazione non scritta", async () => {
    const storage = storageInMemoria();
    const s = new StatoApp({
      archivio: creaArchivio(storage),
      casuale: casualeConSeme(1),
      caricaCategorie: async () => ({ tipo: "Errore", messaggio: "versione non supportata" }),
      ritardoSalvataggioMs: 0,
    });
    await s.pronto;
    expect(s.erroreCaricamento).toBe(true);
    expect(s.caricamento).toBe(false);
    expect(s.puoIniziare).toBe(false);
    expect(s.categorie).toEqual([]);
    expect(s.iniziaPartita()).toBe(false);
    s.salvaOra();
    expect(storage.getItem(CHIAVI_ARCHIVIO.configurazione)).toBeNull();
  });

  it("CA-34 caricaCategorie che lancia in modo sincrono: pronto si risolve e lo stato e' d'errore", async () => {
    const s = new StatoApp({
      archivio: creaArchivio(storageInMemoria()),
      casuale: casualeConSeme(1),
      caricaCategorie: () => {
        throw new Error("sincrono");
      },
    });
    await expect(s.pronto).resolves.toBeUndefined();
    expect(s.erroreCaricamento).toBe(true);
    expect(s.ripristinabile).toBeNull();
  });

  it("CA-34 caricaCategorie respinta: nessuna partita", async () => {
    const s = new StatoApp({
      archivio: creaArchivio(storageInMemoria()),
      casuale: casualeConSeme(1),
      caricaCategorie: () => Promise.reject(new Error("rete")),
    });
    await s.pronto;
    expect(s.erroreCaricamento).toBe(true);
    expect(s.iniziaPartita()).toBe(false);
    expect(s.partita).toBeNull();
  });
});

describe("StatoApp: Rivedi la parola (CA-51..CA-60)", () => {
  async function gioco(n = 4) {
    const r = await configurato(n, 1);
    r.s.iniziaPartita();
    inGioco(r.s);
    return r;
  }

  it("CA-52 CA-54 scegliRevisione accetta solo indici interi validi e una sola scelta alla volta", async () => {
    const { s } = await gioco(4);
    for (const i of [-1, 4, 99, 1.5, Number.NaN]) {
      s.scegliRevisione(i);
      expect(s.revisione).toBeNull();
    }
    s.scegliRevisione(3);
    expect(s.revisione).toEqual({ indice: 3, rivelato: false });
    s.scegliRevisione(0);
    expect(s.revisione).toEqual({ indice: 3, rivelato: false });
  });

  it("CA-52 senza partita nessuna revisione", async () => {
    const { s } = await pronto();
    s.scegliRevisione(0);
    expect(s.revisione).toBeNull();
    s.rivelaRevisione();
    expect(s.revisione).toBeNull();
  });

  it("CA-54 CA-55 rivela solo dopo la scelta; tocchi doppi innocui; tornaAElenco nasconde", async () => {
    const { s } = await gioco(3);
    s.rivelaRevisione();
    expect(s.revisione).toBeNull();
    s.scegliRevisione(1);
    s.rivelaRevisione();
    s.rivelaRevisione();
    expect(s.revisione).toEqual({ indice: 1, rivelato: true });
    s.tornaAElencoRevisione();
    expect(s.revisione).toBeNull();
  });

  it("CA-56 si puo' rivedere piu' volte senza cambiare partita e distribuzione", async () => {
    const { s } = await gioco(4);
    const p = s.partita;
    for (const i of [0, 0, 3, 1, 3]) {
      s.scegliRevisione(i);
      s.rivelaRevisione();
      expect(s.revisione).toEqual({ indice: i, rivelato: true });
      s.nascondiRuolo();
      s.tornaAElencoRevisione();
    }
    expect(s.partita).toBe(p);
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
  });

  it("CA-57 interrompiRevisione senza ruolo visibile non cambia nulla", async () => {
    const { s } = await gioco(3);
    s.interrompiRevisione();
    expect(s.revisione).toBeNull();
    s.scegliRevisione(2);
    s.interrompiRevisione();
    expect(s.revisione).toEqual({ indice: 2, rivelato: false });
  });

  it("CA-59 sospendiPartita e terminaPartita azzerano la revisione", async () => {
    const { s } = await gioco(3);
    s.scegliRevisione(0);
    s.rivelaRevisione();
    s.sospendiPartita();
    expect(s.revisione).toBeNull();
    s.riprendiPartita();
    s.scegliRevisione(1);
    s.terminaPartita();
    expect(s.revisione).toBeNull();
  });

  it("CA-53 chiudiRevisione riporta al Gioco con la stessa partita", async () => {
    const { s } = await gioco(3);
    const p = s.partita;
    s.scegliRevisione(1);
    s.chiudiRevisione();
    expect(s.revisione).toBeNull();
    expect(s.partita).toBe(p);
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
  });
});

describe("StatoApp: parole usate, nuova partita e azzeramento (CA-11, CA-13, CA-14, CA-16, CA-42, CA-50, CA-61, CA-63)", () => {
  function giocaEScarta(s: StatoApp): string {
    expect(s.iniziaPartita()).toBe(true);
    const chiave = chiaveVoce(s.partita!.voce);
    s.terminaPartita();
    return chiave;
  }

  it("CA-13 le prime M partite usano M parole tutte diverse e il contatore scende a ogni partita", async () => {
    const { s } = await configurato(4, 1);
    const viste = new Set<string>();
    for (let i = 0; i < 5; i++) {
      expect(s.paroleRimanenti).toBe(5 - i);
      viste.add(giocaEScarta(s));
    }
    expect(viste.size).toBe(5);
    expect(s.paroleRimanenti).toBe(0);
  });

  it("CA-14 esaurito il pool la partita successiva riparte dal pool completo senza ripetere l'ultima", async () => {
    const { s } = await configurato(4, 1);
    let ultima = "";
    for (let i = 0; i < 5; i++) ultima = giocaEScarta(s);
    expect(s.iniziaPartita()).toBe(true);
    expect(chiaveVoce(s.partita!.voce)).not.toBe(ultima);
    expect(s.paroleRimanenti).toBeGreaterThan(0);
    expect(s.paroleRimanenti).toBeLessThan(5);
  });

  it("CA-14 pool di una sola parola: si ripete senza errori", async () => {
    const una: Categoria[] = [{ id: "solo", nome: "Solo", parole: [{ parola: "Unica", affine: "Altra" }] }];
    const s = creaCon(storageInMemoria(), una);
    await s.pronto;
    for (let i = 0; i < 3; i++) {
      expect(s.iniziaPartita()).toBe(true);
      expect(s.partita?.voce.parola).toBe("Unica");
      s.terminaPartita();
    }
  });

  it("CA-42 CA-50 le parole usate sopravvivono al riavvio: la partita successiva non le ripete", async () => {
    const { s, storage } = await configurato(4, 1);
    const usate = new Set([giocaEScarta(s), giocaEScarta(s)]);
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.ripristinabile).toBeNull();
    expect(s2.paroleRimanenti).toBe(3);
    for (let i = 0; i < 3; i++) {
      expect(s2.iniziaPartita()).toBe(true);
      const c = chiaveVoce(s2.partita!.voce);
      expect(usate.has(c)).toBe(false);
      usate.add(c);
      s2.terminaPartita();
    }
    expect(usate.size).toBe(5);
  });

  it("CA-61 CA-63 azzeraParole rimette in gioco solo le parole delle categorie scelte", async () => {
    const { s, storage } = await configurato(4, 1);
    expect(s.iniziaPartita()).toBe(true);
    const giocata = s.partita!.voce.categoriaId;
    const altra = giocata === "animali" ? "cibo" : "animali";
    s.terminaPartita();
    expect(s.paroleRimanenti).toBe(4);
    s.selezionaTutte(false);
    s.impostaCategoria(altra, true);
    s.azzeraParole(); // la categoria della parola giocata non e' scelta: resta usata
    s.selezionaTutte(true);
    expect(s.paroleRimanenti).toBe(4);
    s.azzeraParole();
    expect(s.paroleRimanenti).toBe(5);
    expect(s.paroleTotali).toBe(5);
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.paroleRimanenti).toBe(5); // azzeramento persistito
  });

  it("CA-61 il contatore segue categorie e modalita' (X <= Y)", async () => {
    const { s } = await configurato(4, 1);
    s.impostaCategoria("cibo", false);
    expect(s.paroleTotali).toBe(3);
    expect(s.paroleRimanenti).toBeLessThanOrEqual(s.paroleTotali);
    s.impostaModalita("PAROLA_AFFINE");
    expect(s.paroleTotali).toBe(3);
    s.selezionaTutte(true);
    expect(s.paroleTotali).toBe(5);
  });

  it("CA-11 in Parola affine il pool esclude le parole senza affine, vuote o uguali alla parola", async () => {
    const cat: Categoria[] = [
      {
        id: "x",
        nome: "X",
        parole: [
          { parola: "Uno", affine: "Due" },
          { parola: "Tre", affine: null },
          { parola: "Quattro", affine: "" },
          { parola: "Cinque", affine: "Cinque" },
        ],
      },
    ];
    const s = creaCon(storageInMemoria(), cat);
    await s.pronto;
    s.impostaModalita("PAROLA_AFFINE");
    expect(s.paroleTotali).toBe(1);
    s.impostaModalita("SENZA_PAROLA");
    expect(s.paroleTotali).toBe(4);
  });

  it("CA-16 pool vuoto in Parola affine: errore esplicito e nessuna partita", async () => {
    const cat: Categoria[] = [{ id: "x", nome: "X", parole: [{ parola: "Uno", affine: null }] }];
    const s = creaCon(storageInMemoria(), cat);
    await s.pronto;
    s.impostaModalita("PAROLA_AFFINE");
    expect(s.errori.map((e) => e.tipo)).toContain("PoolVuoto");
    expect(s.iniziaPartita()).toBe(false);
    expect(s.partita).toBeNull();
  });

  it("CA-61 azzeraParole e' ignorato durante il caricamento", () => {
    const { s } = crea();
    expect(() => s.azzeraParole()).not.toThrow();
    expect(s.paroleRimanenti).toBe(0);
  });
});

describe("StatoApp: segnalazioni nell'archivio (CA-W15, CA-91)", () => {
  it("CA-W15 CA-91 le segnalazioni sopravvivono al riavvio, una riga JSON valida ciascuna", async () => {
    const { s, storage } = await pronto();
    s.salvaSegnalazione(
      segnalazione({
        tipo: "coppia",
        istante: "2026-01-01T10:00:00",
        categoriaId: "animali",
        parola: "Cane",
        affine: "Lupo",
        modalita: "PAROLA_AFFINE",
        nota: "riga uno\nriga due",
        propostaParola: "Gatto",
      }),
    );
    s.salvaSegnalazione(segnalazione({ tipo: "app", istante: "2026-01-02T10:00:00", nota: "bug" }));
    expect(s.segnalazioniSalvate).toBe(2);
    const s2 = creaCon(storage);
    await s2.pronto;
    expect(s2.segnalazioniSalvate).toBe(2);
    const righe = s2.leggiSegnalazioniJsonl().split("\n");
    expect(righe.length).toBe(3);
    expect(righe[2]).toBe("");
    for (const r of righe.slice(0, 2)) expect(() => JSON.parse(r)).not.toThrow();
    expect(JSON.parse(righe[0]!).nota).toBe("riga uno\nriga due");
    expect(JSON.parse(righe[0]!).propostaParola).toBe("Gatto");
  });

  it("CA-W15 cancellaSegnalazioni svuota archivio, contatore e file esportato", async () => {
    const { s, storage } = await pronto();
    s.salvaSegnalazione(segnalazione({ tipo: "app", istante: "2026-01-01T10:00:00", nota: "x" }));
    s.cancellaSegnalazioni();
    expect(s.segnalazioniSalvate).toBe(0);
    expect(s.leggiSegnalazioniJsonl()).toBe("");
    expect(creaCon(storage).segnalazioniSalvate).toBe(0);
  });

  it("CA-W15 il contatore e' leggibile subito, prima del caricamento delle parole", () => {
    const storage = storageInMemoria();
    creaArchivio(storage).aggiungiSegnalazione(
      segnalazione({ tipo: "app", istante: "2026-01-01T10:00:00", nota: "x" }),
    );
    const { s } = crea(storage);
    expect(s.caricamento).toBe(true);
    expect(s.segnalazioniSalvate).toBe(1);
  });
});

describe("StatoApp: storage che lancia (CA-W04, CA-W16)", () => {
  const rotto: ArchivioStorage = {
    getItem: () => {
      throw new Error("bloccato");
    },
    setItem: () => {
      throw new Error("bloccato");
    },
    removeItem: () => {
      throw new Error("bloccato");
    },
  };

  it("CA-W04 CA-W16 una partita completa si gioca con valori solo in memoria", async () => {
    const s = creaCon(rotto);
    await s.pronto;
    expect(s.erroreCaricamento).toBe(false);
    s.impostaNome(0, "Anna");
    s.salvaOra();
    expect(s.iniziaPartita()).toBe(true);
    inGioco(s);
    expect(s.distribuzione).toEqual({ tipo: "Gioco" });
    s.scegliRevisione(0);
    s.nascondiRuolo();
    s.sospendiPartita();
    expect(s.riprendiPartita()).toBe(true);
    s.entraInRivela();
    s.azzeraParole();
    s.salvaSegnalazione(segnalazione({ tipo: "app", istante: "2026-01-01T10:00:00", nota: "x" }));
    s.cancellaSegnalazioni();
    s.terminaPartita();
    expect(s.config.nomi[0]).toBe("Anna");
  });
});

describe("StatoApp: aspetto e tema (CA-W14)", () => {
  function fingiDom() {
    const imposta = vi.fn();
    vi.stubGlobal("document", { documentElement: { setAttribute: imposta }, querySelector: () => null });
    vi.stubGlobal("getComputedStyle", () => ({ getPropertyValue: () => "" }));
    vi.stubGlobal("matchMedia", () => ({ matches: false, addEventListener: () => undefined }));
    return imposta;
  }

  it("CA-W14 impostaAspetto applica data-tema per ognuno dei quattro temi", async () => {
    const imposta = fingiDom();
    const { s } = await pronto();
    const attesi = { SISTEMA: "sistema", CHIARO: "chiaro", SCURO: "scuro", ALTO_CONTRASTO: "alto-contrasto" } as const;
    for (const [tema, attributo] of Object.entries(attesi)) {
      s.impostaAspetto({ tema: tema as keyof typeof attesi, coloriDinamici: false });
      expect(imposta).toHaveBeenLastCalledWith("data-tema", attributo);
      expect(s.aspetto.tema).toBe(tema);
    }
  });

  it("CA-W14 il tema scelto persiste e viene riapplicato all'avvio successivo", async () => {
    const imposta = fingiDom();
    const { s, storage } = await pronto();
    s.impostaAspetto({ tema: "ALTO_CONTRASTO", coloriDinamici: true });
    imposta.mockClear();
    const s2 = creaCon(storage);
    expect(s2.aspetto.tema).toBe("ALTO_CONTRASTO");
    expect(imposta).toHaveBeenCalledWith("data-tema", "alto-contrasto");
  });

  it("CA-W14 aspetto salvato illeggibile: tema Sistema", async () => {
    const storage = storageInMemoria();
    storage.setItem(CHIAVI_ARCHIVIO.aspetto, "{rotto");
    const { s } = await pronto(storage);
    expect(s.aspetto.tema).toBe("SISTEMA");
  });

  it("CA-W14 senza DOM (Node) impostaAspetto non lancia", async () => {
    const { s } = await pronto();
    expect(() => s.impostaAspetto({ tema: "SCURO", coloriDinamici: true })).not.toThrow();
    expect(s.aspetto.tema).toBe("SCURO");
  });

  it("CA-W14 temaScuroAttivo: chiaro no, scuro e alto contrasto si', sistema segue la preferenza", () => {
    expect(temaScuroAttivo("CHIARO")).toBe(false);
    expect(temaScuroAttivo("SCURO")).toBe(true);
    expect(temaScuroAttivo("ALTO_CONTRASTO")).toBe(true);
    expect(temaScuroAttivo("SISTEMA")).toBe(false); // Node: nessun matchMedia
    vi.stubGlobal("matchMedia", () => ({ matches: true }));
    expect(temaScuroAttivo("SISTEMA")).toBe(true);
    vi.stubGlobal("matchMedia", () => ({ matches: false }));
    expect(temaScuroAttivo("SISTEMA")).toBe(false);
  });
});
