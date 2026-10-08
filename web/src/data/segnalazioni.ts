import type { Modalita } from "../game/modelli";
import {
  analizza,
  campo,
  campoNullable,
  comeLista,
  comeOggetto,
  comeStringa,
  richiesto,
} from "./jsonStretto";

export type MotivoSegnalazione = "TROPPO_SIMILI" | "TROPPO_DIVERSE" | "POCO_CONOSCIUTA" | "CATEGORIA_SBAGLIATA";

const MOTIVI: readonly MotivoSegnalazione[] = ["TROPPO_SIMILI", "TROPPO_DIVERSE", "POCO_CONOSCIUTA", "CATEGORIA_SBAGLIATA"];

export interface Segnalazione {
  readonly tipo: "coppia" | "app";
  readonly istante: string; // ISO-8601 locale
  readonly categoriaId: string | null;
  readonly parola: string | null;
  readonly affine: string | null;
  readonly modalita: Modalita | null;
  readonly motivi: readonly MotivoSegnalazione[];
  readonly nota: string;
  readonly propostaParola: string | null;
  readonly propostaAffine: string | null;
}

/** Default: null / [] / "". */
export function segnalazione(
  base: Pick<Segnalazione, "tipo" | "istante"> & Partial<Segnalazione>,
): Segnalazione {
  return {
    categoriaId: null,
    parola: null,
    affine: null,
    modalita: null,
    motivi: [],
    nota: "",
    propostaParola: null,
    propostaAffine: null,
    ...base,
  };
}

const MAX_NOTA = 500;
const MAX_PROPOSTA = 40;

function pulisciProposta(p: string | null): string | null {
  if (p === null) return null;
  const r = p.trim().slice(0, MAX_PROPOSTA).trim();
  return r.length === 0 ? null : r;
}

function normalizza(s: Segnalazione): Segnalazione {
  return {
    ...s,
    nota: s.nota.trim().slice(0, MAX_NOTA).trim(),
    propostaParola: pulisciProposta(s.propostaParola),
    propostaAffine: pulisciProposta(s.propostaAffine),
  };
}

function propostaValida(s: Segnalazione): boolean {
  const p = pulisciProposta(s.propostaParola);
  if (p === null) return false;
  const a = pulisciProposta(s.propostaAffine);
  if (a === null) return false;
  return p.toLowerCase() !== a.toLowerCase();
}

function riga(s: Segnalazione): string {
  const n = normalizza(s);
  // Stesso ordine dei campi del Kotlin; i null restano null.
  return JSON.stringify({
    tipo: n.tipo,
    istante: n.istante,
    categoriaId: n.categoriaId,
    parola: n.parola,
    affine: n.affine,
    modalita: n.modalita,
    motivi: [...n.motivi],
    nota: n.nota,
    propostaParola: n.propostaParola,
    propostaAffine: n.propostaAffine,
  });
}

function leggiMotivo(v: unknown): MotivoSegnalazione {
  const s = comeStringa(v);
  const m = MOTIVI.find((x) => x === s);
  if (m === undefined) throw new Error("motivo sconosciuto");
  return m;
}

function leggiRiga(r: string): Segnalazione | null {
  try {
    const o = comeOggetto(analizza(r));
    return normalizza({
      tipo: richiesto(o, "tipo", comeStringa) as Segnalazione["tipo"],
      istante: richiesto(o, "istante", comeStringa),
      categoriaId: campoNullable(o, "categoriaId", comeStringa, null),
      parola: campoNullable(o, "parola", comeStringa, null),
      affine: campoNullable(o, "affine", comeStringa, null),
      modalita: campoNullable(o, "modalita", comeStringa, null) as Modalita | null,
      motivi: campo(o, "motivi", (v) => comeLista(v, leggiMotivo), [] as MotivoSegnalazione[]),
      nota: campo(o, "nota", comeStringa, ""),
      propostaParola: campoNullable(o, "propostaParola", comeStringa, null),
      propostaAffine: campoNullable(o, "propostaAffine", comeStringa, null),
    });
  } catch {
    return null;
  }
}

function leggi(testo: string): Segnalazione[] {
  const r: Segnalazione[] = [];
  for (const linea of testo.split(/\r?\n/)) {
    const t = linea.trim();
    if (t.length === 0) continue;
    const s = leggiRiga(t);
    if (s !== null) r.push(s);
  }
  return r;
}

export const FormatoSegnalazioni = { normalizza, propostaValida, riga, leggi } as const;
