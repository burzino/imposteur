import type { Categoria, Parola } from "../game/modelli";
import {
  analizza,
  campo,
  campoNullable,
  comeIntero,
  comeLista,
  comeOggetto,
  comeStringa,
  richiesto,
} from "./jsonStretto";

export type RisultatoCaricamento =
  | { readonly tipo: "Ok"; readonly categorie: readonly Categoria[] }
  | { readonly tipo: "Errore"; readonly messaggio: string };

interface ParolaDto {
  parola: string | null;
  affine: string | null;
}
interface CategoriaDto {
  id: string;
  nome: string;
  parole: ParolaDto[];
}

/** CA-17 */
export function parseParole(json: string): RisultatoCaricamento {
  let versione: number;
  let categorieDto: CategoriaDto[];
  try {
    const radice = comeOggetto(analizza(json));
    versione = richiesto(radice, "versione", comeIntero);
    categorieDto = campo(
      radice,
      "categorie",
      (v) =>
        comeLista(v, (c): CategoriaDto => {
          const o = comeOggetto(c);
          return {
            id: richiesto(o, "id", comeStringa),
            nome: richiesto(o, "nome", comeStringa),
            parole: campo(
              o,
              "parole",
              (pv) =>
                comeLista(pv, (p): ParolaDto => {
                  const po = comeOggetto(p);
                  return {
                    parola: campoNullable(po, "parola", comeStringa, null),
                    affine: campoNullable(po, "affine", comeStringa, null),
                  };
                }),
              [],
            ),
          };
        }),
      [],
    );
  } catch (e) {
    return { tipo: "Errore", messaggio: `JSON non valido: ${e instanceof Error ? e.message : String(e)}` };
  }
  if (versione !== 1) return { tipo: "Errore", messaggio: `Versione non supportata: ${versione}` };
  const ids = categorieDto.map((c) => c.id);
  if (ids.length !== new Set(ids).size) return { tipo: "Errore", messaggio: "Id categoria duplicato" };

  const categorie: Categoria[] = [];
  for (const c of categorieDto) {
    const viste = new Set<string>();
    const parole: Parola[] = [];
    for (const p of c.parole) {
      const testo = (p.parola ?? "").trim();
      if (testo.length === 0) return { tipo: "Errore", messaggio: `Parola vuota nella categoria ${c.id}` };
      if (viste.has(testo.toLowerCase())) continue;
      viste.add(testo.toLowerCase());
      const a = p.affine !== null ? p.affine.trim() : null;
      const affine = a !== null && a.length > 0 && a.toLowerCase() !== testo.toLowerCase() ? a : null;
      parole.push({ parola: testo, affine });
    }
    categorie.push({ id: c.id, nome: c.nome, parole });
  }
  return { tipo: "Ok", categorie };
}
