import type { StatoApp } from "./stato.svelte";

// ---- Schermo acceso (Screen Wake Lock) -----------------------------------------------------

let richieste = 0;
let sentinella: WakeLockSentinel | null = null;
let inCorso: Promise<void> | null = null;

async function acquisisci(): Promise<void> {
  try {
    if (sentinella !== null || typeof navigator === "undefined" || !navigator.wakeLock) return;
    const s = await navigator.wakeLock.request("screen");
    if (richieste <= 0) {
      // rilasciato mentre si attendeva la risposta
      await s.release().catch(() => undefined);
      return;
    }
    sentinella = s;
    s.addEventListener("release", () => {
      if (sentinella === s) sentinella = null;
    });
  } catch {
    // rifiutato o non disponibile: si ignora
  }
}

/** Chiede lo schermo acceso. Le richieste annidate si contano; no-op se l'API manca o rifiuta. */
export function richiediSchermoAcceso(): Promise<void> {
  richieste += 1;
  if (inCorso === null) {
    inCorso = acquisisci().finally(() => {
      inCorso = null;
    });
  }
  return inCorso;
}

/** Rilascia una richiesta; allo zero libera il wake lock. Idempotente. */
export function rilasciaSchermoAcceso(): void {
  if (richieste > 0) richieste -= 1;
  if (richieste === 0 && sentinella !== null) {
    const s = sentinella;
    sentinella = null;
    try {
      void s.release().catch(() => undefined);
    } catch {
      // ignorato
    }
  }
}

// ---- Vibrazione ----------------------------------------------------------------------------

export function vibra(ms = 30): void {
  try {
    if (typeof navigator !== "undefined" && typeof navigator.vibrate === "function") navigator.vibrate(ms);
  } catch {
    // ignorato
  }
}

// ---- Protezione del ruolo ------------------------------------------------------------------

/**
 * Pagina nascosta -> `nascondiRuolo()`; di nuovo visibile -> ri-richiede il wake lock se serviva;
 * `pagehide` -> `salvaOra()`. Restituisce la funzione di rimozione.
 */
export function avviaProtezioneRuolo(s: StatoApp): () => void {
  if (typeof document === "undefined" || typeof window === "undefined") return () => undefined;
  const alCambioVisibilita = (): void => {
    try {
      if (document.visibilityState === "hidden") {
        s.nascondiRuolo();
      } else if (richieste > 0) {
        void acquisisci();
      }
    } catch {
      // ignorato
    }
  };
  const allaChiusura = (): void => {
    try {
      s.salvaOra();
    } catch {
      // ignorato
    }
  };
  document.addEventListener("visibilitychange", alCambioVisibilita);
  window.addEventListener("pagehide", allaChiusura);
  return () => {
    document.removeEventListener("visibilitychange", alCambioVisibilita);
    window.removeEventListener("pagehide", allaChiusura);
  };
}

// ---- Esportazione segnalazioni -------------------------------------------------------------

const NOME_FILE = "segnalazioni.jsonl";
const TIPO_FILE = "application/x-ndjson";

function creaFile(jsonl: string): File {
  return new File([new Blob([jsonl], { type: TIPO_FILE })], NOME_FILE, { type: TIPO_FILE });
}

export function condivisioneFileDisponibile(): boolean {
  try {
    if (typeof navigator === "undefined" || typeof navigator.canShare !== "function" || typeof navigator.share !== "function") {
      return false;
    }
    return navigator.canShare({ files: [creaFile("")] });
  } catch {
    return false;
  }
}

export async function esportaSegnalazioni(
  jsonl: string,
  modo: "scarica" | "condividi",
): Promise<"scaricato" | "condiviso" | "annullato"> {
  try {
    if (modo === "condividi") {
      try {
        await navigator.share({ files: [creaFile(jsonl)] });
        return "condiviso";
      } catch (e) {
        if (e instanceof DOMException && e.name === "AbortError") return "annullato";
        throw e;
      }
    }
    const url = URL.createObjectURL(new Blob([jsonl], { type: TIPO_FILE }));
    try {
      const a = document.createElement("a");
      a.href = url;
      a.download = NOME_FILE;
      a.style.display = "none";
      document.body.appendChild(a);
      a.click();
      a.remove();
    } finally {
      setTimeout(() => URL.revokeObjectURL(url), 10_000);
    }
    return "scaricato";
  } catch {
    return "annullato";
  }
}

// ---- Ambiente ------------------------------------------------------------------------------

export function eIosSafariNonInstallato(): boolean {
  try {
    if (typeof navigator === "undefined") return false;
    const ua = navigator.userAgent ?? "";
    const iPadOs = navigator.platform === "MacIntel" && (navigator.maxTouchPoints ?? 0) > 1;
    const ios = /iPad|iPhone|iPod/.test(ua) || iPadOs;
    if (!ios) return false;
    if (/CriOS|FxiOS|EdgiOS|OPiOS/.test(ua)) return false;
    const standalone =
      (navigator as Navigator & { standalone?: boolean }).standalone === true ||
      (typeof matchMedia === "function" && matchMedia("(display-mode: standalone)").matches);
    return !standalone;
  } catch {
    return false;
  }
}
