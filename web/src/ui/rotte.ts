import { createSubscriber } from "svelte/reactivity";
import type { StatoApp } from "./stato.svelte";

export type Rotta =
  | "home"
  | "regole"
  | "impostazioni"
  | "configurazione"
  | "distribuzione"
  | "gioco"
  | "rivedi"
  | "rivela";

const ROTTE: readonly Rotta[] = [
  "home",
  "regole",
  "impostazioni",
  "configurazione",
  "distribuzione",
  "gioco",
  "rivedi",
  "rivela",
];

/** Rotte che hanno senso solo con una partita in corso (D1 Kotlin). */
const RICHIEDONO_PARTITA: readonly Rotta[] = ["distribuzione", "gioco", "rivedi", "rivela"];

/** Sottoinsieme di `window` usato dalle rotte; nei test se ne passa uno finto. */
export interface AmbienteRotte {
  location: { hash: string };
  history: {
    readonly state: unknown;
    pushState(stato: unknown, titolo: string, url: string): void;
    replaceState(stato: unknown, titolo: string, url: string): void;
    go(delta: number): void;
  };
  addEventListener(tipo: "popstate", f: () => void): void;
  removeEventListener(tipo: "popstate", f: () => void): void;
}

// ---- Segnali reattivi (rune non utilizzabili in un .ts: si usa createSubscriber) ----------------

function segnale<T>(iniziale: T): { leggi(): T; scrivi(v: T): void } {
  let valore = iniziale;
  const ascoltatori = new Set<() => void>();
  const iscrivi = createSubscriber((aggiorna) => {
    ascoltatori.add(aggiorna);
    return () => {
      ascoltatori.delete(aggiorna);
    };
  });
  return {
    leggi() {
      iscrivi();
      return valore;
    },
    scrivi(v) {
      if (Object.is(v, valore)) return;
      valore = v;
      for (const a of [...ascoltatori]) a();
    },
  };
}

const sRotta = segnale<Rotta>("home");
const sInterruzione = segnale(false);

export const rottaCorrente: { readonly valore: Rotta } = {
  get valore() {
    return sRotta.leggi();
  },
};

export const richiestaInterruzione: { aperta: boolean } = {
  get aperta() {
    return sInterruzione.leggi();
  },
  set aperta(v: boolean) {
    sInterruzione.scrivi(v);
  },
};

// ---- Stato interno ------------------------------------------------------------------------------

let ambiente: AmbienteRotte | null = null;
let statoApp: StatoApp | null = null;
/** Posizione nella history (history.state.i) e copia delle rotte visitate, per posizione. */
let indice = 0;
let pila: Rotta[] = [];
/** Un go() lanciato da noi e' in corso: il prossimo popstate non e' un "indietro" dell'utente. */
let inAttesa: { dopo?: () => void } | null = null;
/** Un "indietro" in app (indietro()) e' in corso: il prossimo popstate e' atteso. */
let indietroInApp = false;

function daHash(hash: string): Rotta {
  const nome = hash.replace(/^#\/?/, "").split(/[/?]/)[0];
  return ROTTE.find((r) => r === nome) ?? "home";
}

function aHash(r: Rotta): string {
  return `#/${r}`;
}

function indiceDa(stato: unknown): number | null {
  if (typeof stato === "object" && stato !== null && "i" in stato) {
    const i = (stato as { i: unknown }).i;
    if (typeof i === "number" && Number.isInteger(i) && i >= 0) return i;
  }
  return null;
}

function imposta(r: Rotta): void {
  sRotta.scrivi(r);
}

export function vai(rotta: Rotta, opz?: { sostituisci?: boolean }): void {
  if (rotta === sRotta.leggi() || inAttesa !== null) return;
  if (ambiente !== null) {
    if (opz?.sostituisci) {
      pila[indice] = rotta;
      ambiente.history.replaceState({ i: indice }, "", aHash(rotta));
    } else {
      indice += 1;
      pila = [...pila.slice(0, indice), rotta];
      ambiente.history.pushState({ i: indice }, "", aHash(rotta));
    }
  }
  imposta(rotta);
}

/** Torna alla voce piu' recente della rotta `r` (popUpTo) e poi esegue `dopo`. false se non c'e'. */
function tornaA(r: Rotta, dopo: () => void): boolean {
  if (ambiente === null) return false;
  let j = -1;
  for (let k = Math.min(indice, pila.length - 1); k >= 0; k--) {
    if (pila[k] === r) {
      j = k;
      break;
    }
  }
  if (j < 0) return false;
  if (j === indice) {
    dopo();
    return true;
  }
  inAttesa = { dopo };
  ambiente.history.go(j - indice);
  return true;
}

export function vaiAHome(): void {
  if (inAttesa !== null) return;
  // popUpTo(Home, inclusive) + Home: la voce di Home resta, tutto quello sopra viene dimenticato.
  if (!tornaA("home", () => imposta("home"))) vai("home", { sostituisci: true });
}

export function vaiAConfigurazione(): void {
  if (inAttesa !== null) return;
  if (sRotta.leggi() === "configurazione") return;
  // popUpTo(Home): Home sotto, Configurazione sopra.
  const dopo = (): void => {
    imposta("home");
    vai("configurazione");
  };
  if (!tornaA("home", dopo)) vai("configurazione");
}

export function indietro(): void {
  const r = sRotta.leggi();
  if (r === "distribuzione" || r === "gioco") {
    richiestaInterruzione.aperta = true;
    return;
  }
  if (ambiente === null) return;
  if (indice <= 0) {
    // niente history sotto di noi (apertura diretta): si va in Home senza uscire dal sito
    if (r !== "home") vai("home", { sostituisci: true });
    return;
  }
  indietroInApp = true;
  ambiente.history.go(-1);
}

// ---- Avvio --------------------------------------------------------------------------------------

function allaNavigazione(): void {
  const env = ambiente;
  const s = statoApp;
  if (env === null) return;
  const precedente = sRotta.leggi();
  const atteso = inAttesa;
  const dallApp = indietroInApp;
  inAttesa = null;
  indietroInApp = false;

  const nuova = daHash(env.location.hash);
  const i = indiceDa(env.history.state);
  if (i !== null) {
    indice = i;
  } else {
    // voce senza indice (hash digitato a mano): le si assegna una posizione
    indice += 1;
    env.history.replaceState({ i: indice }, "", aHash(nuova));
  }

  if (atteso === null && !dallApp && (precedente === "distribuzione" || precedente === "gioco")) {
    // Back/forward del browser: mai a un ruolo precedente. Si ripristina la voce e si chiede conferma.
    indice += 1;
    pila = [...pila.slice(0, indice), precedente];
    env.history.pushState({ i: indice }, "", aHash(precedente));
    richiestaInterruzione.aperta = true;
    return;
  }

  pila[indice] = nuova;
  if (atteso === null) {
    if (precedente === "rivedi") s?.chiudiRevisione();
    else if (precedente === "configurazione") s?.salvaOra();
  }
  if (s !== null && !s.caricamento && s.partita === null && RICHIEDONO_PARTITA.includes(nuova)) {
    pila[indice] = "home";
    env.history.replaceState({ i: indice }, "", aHash("home"));
    imposta("home");
  } else {
    imposta(nuova);
  }
  atteso?.dopo?.();
}

/**
 * Avvia le rotte (hash `#/nome`) e restituisce la funzione di rimozione.
 * Dopo `s.pronto`, una rotta che richiede una partita senza partita porta in Home (sostituendo la voce).
 */
export function avviaRotte(s: StatoApp, env?: AmbienteRotte): () => void {
  const e = env ?? (typeof window !== "undefined" ? (window as unknown as AmbienteRotte) : null);
  if (e === null) return () => undefined;
  ambiente = e;
  statoApp = s;
  inAttesa = null;
  indietroInApp = false;

  const r = daHash(e.location.hash);
  const i = indiceDa(e.history.state);
  indice = i ?? 0;
  pila = [];
  pila[indice] = r;
  if (i === null || e.location.hash !== aHash(r)) e.history.replaceState({ i: indice }, "", aHash(r));
  sRotta.scrivi(r);

  let attiva = true;
  void s.pronto.then(() => {
    if (!attiva) return;
    if (s.partita === null && RICHIEDONO_PARTITA.includes(sRotta.leggi())) {
      vai("home", { sostituisci: true });
    }
  });

  e.addEventListener("popstate", allaNavigazione);
  return () => {
    attiva = false;
    e.removeEventListener("popstate", allaNavigazione);
    if (ambiente === e) {
      ambiente = null;
      statoApp = null;
    }
  };
}
