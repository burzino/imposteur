import type { Tema } from "../data/aspetto";

const VALORE_ATTRIBUTO: Record<Tema, string> = {
  SISTEMA: "sistema",
  CHIARO: "chiaro",
  SCURO: "scuro",
  ALTO_CONTRASTO: "alto-contrasto",
};

function preferisceScuro(): boolean {
  try {
    return typeof matchMedia === "function" && matchMedia("(prefers-color-scheme: dark)").matches;
  } catch {
    return false;
  }
}

/** True se, con questo tema, l'interfaccia e' scura (equivale a usaTemaScuro di Theme.kt). */
export function temaScuroAttivo(t: Tema): boolean {
  switch (t) {
    case "SISTEMA":
      return preferisceScuro();
    case "CHIARO":
      return false;
    case "SCURO":
    case "ALTO_CONTRASTO":
      return true;
  }
}

let ascoltoSistema = false;
let temaCorrente: Tema = "SISTEMA";

function aggiornaMeta(): void {
  const meta = document.querySelector('meta[name="theme-color"]');
  const colore = getComputedStyle(document.documentElement).getPropertyValue("--colore-sfondo").trim();
  if (meta && colore) meta.setAttribute("content", colore);
}

/** Imposta `data-tema` su <html> e allinea `<meta name="theme-color">` allo sfondo risolto. No-op senza DOM. */
export function applicaTema(t: Tema): void {
  try {
    if (typeof document === "undefined") return;
    temaCorrente = t;
    document.documentElement.setAttribute("data-tema", VALORE_ATTRIBUTO[t]);
    aggiornaMeta();
    if (!ascoltoSistema && typeof matchMedia === "function") {
      ascoltoSistema = true;
      matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
        if (temaCorrente === "SISTEMA") aggiornaMeta();
      });
    }
  } catch {
    // ambiente senza DOM completo: ignorato
  }
}
