import { afterEach, describe, expect, it, vi } from "vitest";
import {
  avviaRotte,
  indietro,
  richiestaInterruzione,
  rottaCorrente,
  vai,
  vaiAConfigurazione,
  vaiAHome,
  type AmbienteRotte,
} from "./rotte";
import type { StatoApp } from "./stato.svelte";

interface Voce {
  hash: string;
  stato: unknown;
}

function creaEnv(hashIniziale = ""): { env: AmbienteRotte; voci: Voce[]; posizione: () => number } {
  const voci: Voce[] = [{ hash: hashIniziale, stato: null }];
  let i = 0;
  const ascoltatori = new Set<() => void>();
  const env: AmbienteRotte = {
    location: {
      get hash() {
        return voci[i].hash;
      },
    },
    history: {
      get state() {
        return voci[i].stato;
      },
      pushState(stato, _titolo, url) {
        voci.splice(i + 1);
        voci.push({ hash: url, stato });
        i += 1;
      },
      replaceState(stato, _titolo, url) {
        voci[i] = { hash: url, stato };
      },
      go(delta) {
        i = Math.max(0, Math.min(voci.length - 1, i + delta));
        for (const f of [...ascoltatori]) f();
      },
    },
    addEventListener: (_tipo, f) => {
      ascoltatori.add(f);
    },
    removeEventListener: (_tipo, f) => {
      ascoltatori.delete(f);
    },
  };
  return { env, voci, posizione: () => i };
}

function statoFinto(conPartita: boolean): StatoApp & { chiudiRevisione: ReturnType<typeof vi.fn>; salvaOra: ReturnType<typeof vi.fn> } {
  return {
    pronto: Promise.resolve(),
    caricamento: false,
    partita: conPartita ? {} : null,
    chiudiRevisione: vi.fn(),
    salvaOra: vi.fn(),
  } as unknown as StatoApp & { chiudiRevisione: ReturnType<typeof vi.fn>; salvaOra: ReturnType<typeof vi.fn> };
}

let rimuovi: (() => void) | null = null;
afterEach(() => {
  rimuovi?.();
  rimuovi = null;
  richiestaInterruzione.aperta = false;
});

function avvia(hash: string, conPartita = true): ReturnType<typeof creaEnv> & { s: ReturnType<typeof statoFinto> } {
  const e = creaEnv(hash);
  const s = statoFinto(conPartita);
  rimuovi = avviaRotte(s, e.env);
  return { ...e, s };
}

describe("rotte", () => {
  it("hash vuoto o sconosciuto = home", () => {
    avvia("");
    expect(rottaCorrente.valore).toBe("home");
    rimuovi?.();
    avvia("#/boh");
    expect(rottaCorrente.valore).toBe("home");
  });

  it("rotta di partita senza partita -> home (voce sostituita)", async () => {
    const { voci } = avvia("#/gioco", false);
    await Promise.resolve();
    await Promise.resolve();
    expect(rottaCorrente.valore).toBe("home");
    expect(voci[0].hash).toBe("#/home");
    expect(voci.length).toBe(1);
  });

  it("vai aggiunge una voce, la stessa rotta e' un no-op, sostituisci non aggiunge", () => {
    const { voci } = avvia("#/home");
    vai("configurazione");
    vai("configurazione");
    expect(voci.length).toBe(2);
    vai("distribuzione", { sostituisci: true });
    expect(voci.length).toBe(2);
    expect(rottaCorrente.valore).toBe("distribuzione");
    expect(voci[1].hash).toBe("#/distribuzione");
  });

  it("indietro del browser in distribuzione/gioco: resta li' e chiede conferma", () => {
    const { env, voci } = avvia("#/home");
    vai("configurazione");
    vai("distribuzione");
    env.history.go(-1);
    expect(rottaCorrente.valore).toBe("distribuzione");
    expect(richiestaInterruzione.aperta).toBe(true);
    expect(voci[voci.length - 1].hash).toBe("#/distribuzione");
    expect(env.location.hash).toBe("#/distribuzione");
  });

  it("la freccia in app in gioco apre il dialogo e non naviga", () => {
    avvia("#/home");
    vai("configurazione");
    vai("gioco");
    indietro();
    expect(richiestaInterruzione.aperta).toBe(true);
    expect(rottaCorrente.valore).toBe("gioco");
  });

  it("vaiAConfigurazione: Home sotto, Configurazione sopra", () => {
    const { voci, env } = avvia("#/home");
    vai("configurazione");
    vai("distribuzione");
    vai("gioco", { sostituisci: true });
    vaiAConfigurazione();
    expect(rottaCorrente.valore).toBe("configurazione");
    expect(richiestaInterruzione.aperta).toBe(false);
    expect(voci.map((v) => v.hash)).toEqual(["#/home", "#/configurazione"]);
    expect(env.location.hash).toBe("#/configurazione");
  });

  it("vaiAHome: indietro non riporta alla schermata lasciata", () => {
    const { voci, posizione } = avvia("#/home");
    vai("regole");
    vaiAHome();
    expect(rottaCorrente.valore).toBe("home");
    expect(posizione()).toBe(0);
    expect(voci[0].hash).toBe("#/home");
  });

  it("indietro da rivedi chiude la revisione; da configurazione salva", () => {
    const { s } = avvia("#/home");
    vai("configurazione");
    indietro();
    expect(s.salvaOra).toHaveBeenCalledTimes(1);
    expect(rottaCorrente.valore).toBe("home");
    vai("rivedi");
    indietro();
    expect(s.chiudiRevisione).toHaveBeenCalledTimes(1);
    expect(rottaCorrente.valore).toBe("home");
  });

  it("indietro senza history sotto porta in home", () => {
    const { voci } = avvia("#/regole");
    indietro();
    expect(rottaCorrente.valore).toBe("home");
    expect(voci[0].hash).toBe("#/home");
  });
});
