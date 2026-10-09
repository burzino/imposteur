// Porting di DurataPressione.kt: durata della pressione lunga per scoprire il ruolo.
export const DURATA_PRESSIONE = { min: 0, max: 1000, passo: 50, predefinita: 150 } as const;

/** Clamp a 0..1000, poi multiplo di 50 più vicino (metà strada per eccesso). Non numerico o non finito -> 150. */
export function normalizzaDurataPressione(valore: unknown): number {
  if (typeof valore !== "number" || !Number.isFinite(valore)) return DURATA_PRESSIONE.predefinita;
  const limitato = Math.min(DURATA_PRESSIONE.max, Math.max(DURATA_PRESSIONE.min, valore));
  return Math.floor(limitato / DURATA_PRESSIONE.passo + 0.5) * DURATA_PRESSIONE.passo;
}

/** Testo -> valore valido; null, vuoto o non numerico -> 150. */
export function durataPressioneDaTesto(s: string | null): number {
  if (typeof s !== "string") return DURATA_PRESSIONE.predefinita;
  const p = s.trim();
  if (p === "") return DURATA_PRESSIONE.predefinita;
  return normalizzaDurataPressione(Number(p));
}

export function soloTocco(ms: number): boolean {
  return ms === 0;
}
