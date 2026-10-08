import { analizza, comeOggetto } from "./jsonStretto";

export type Tema = "SISTEMA" | "CHIARO" | "SCURO" | "ALTO_CONTRASTO";
export interface Aspetto {
  readonly tema: Tema;
  readonly coloriDinamici: boolean;
}

const TEMI: readonly Tema[] = ["SISTEMA", "CHIARO", "SCURO", "ALTO_CONTRASTO"];

export const aspettoDefault: Aspetto = { tema: "SISTEMA", coloriDinamici: true };

export function aspettoAStringa(a: Aspetto): string {
  return JSON.stringify({ tema: a.tema, coloriDinamici: a.coloriDinamici });
}

/** null/malformata/valori sconosciuti -> default per campo. */
export function aspettoDaStringa(s: string | null): Aspetto {
  if (s === null) return aspettoDefault;
  try {
    const o = comeOggetto(analizza(s));
    const tema = TEMI.find((t) => t === o.tema) ?? aspettoDefault.tema;
    const coloriDinamici = typeof o.coloriDinamici === "boolean" ? o.coloriDinamici : aspettoDefault.coloriDinamici;
    return { tema, coloriDinamici };
  } catch {
    return aspettoDefault;
  }
}
