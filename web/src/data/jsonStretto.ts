/**
 * Lettori JSON stretti (interni a data/): replicano il comportamento di kotlinx.serialization
 * non lenient. Un campo di tipo errato lancia ErroreFormato; un campo assente prende il default
 * (o lancia se obbligatorio); le chiavi sconosciute si ignorano.
 */
export class ErroreFormato extends Error {}

export type Oggetto = Record<string, unknown>;

export function comeOggetto(v: unknown): Oggetto {
  if (typeof v !== "object" || v === null || Array.isArray(v)) throw new ErroreFormato("oggetto atteso");
  return v as Oggetto;
}

function presente(o: Oggetto, k: string): boolean {
  return Object.prototype.hasOwnProperty.call(o, k);
}

export function comeStringa(v: unknown): string {
  if (typeof v !== "string") throw new ErroreFormato("stringa attesa");
  return v;
}

export function comeIntero(v: unknown): number {
  if (typeof v !== "number" || !Number.isInteger(v)) throw new ErroreFormato("intero atteso");
  return v;
}

export function comeBooleano(v: unknown): boolean {
  if (typeof v !== "boolean") throw new ErroreFormato("booleano atteso");
  return v;
}

export function comeLista<T>(v: unknown, elemento: (x: unknown) => T): T[] {
  if (!Array.isArray(v)) throw new ErroreFormato("lista attesa");
  return v.map(elemento);
}

/** Campo opzionale con default (assente -> default; presente ma null o di tipo errato -> errore). */
export function campo<T>(o: Oggetto, k: string, lettore: (x: unknown) => T, predefinito: T): T {
  return presente(o, k) ? lettore(o[k]) : predefinito;
}

/** Campo obbligatorio. */
export function richiesto<T>(o: Oggetto, k: string, lettore: (x: unknown) => T): T {
  if (!presente(o, k)) throw new ErroreFormato(`campo mancante: ${k}`);
  return lettore(o[k]);
}

/** Campo nullable con default (assente -> predefinito; null -> null). */
export function campoNullable<T>(o: Oggetto, k: string, lettore: (x: unknown) => T, predefinito: T | null): T | null {
  if (!presente(o, k)) return predefinito;
  return o[k] === null ? null : lettore(o[k]);
}

/** Campo nullable obbligatorio (Kotlin: `T?` senza default). */
export function richiestoNullable<T>(o: Oggetto, k: string, lettore: (x: unknown) => T): T | null {
  if (!presente(o, k)) throw new ErroreFormato(`campo mancante: ${k}`);
  return o[k] === null ? null : lettore(o[k]);
}

export function analizza(s: string): unknown {
  try {
    return JSON.parse(s);
  } catch (e) {
    throw new ErroreFormato(e instanceof Error ? e.message : String(e));
  }
}
