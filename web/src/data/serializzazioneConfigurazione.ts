import type { Categoria, Configurazione, Modalita } from "../game/modelli";
import { configurazioneDefault } from "../game/modelli";
import { MAX_GIRI, MAX_GIOCATORI, MAX_LUNGHEZZA_NOME, MIN_GIOCATORI, maxImpostori } from "../game/regole";
import {
  analizza,
  campo,
  comeBooleano,
  comeIntero,
  comeLista,
  comeOggetto,
  comeStringa,
} from "./jsonStretto";

function limita(v: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, v));
}

export function configurazioneAStringa(config: Configurazione): string {
  return JSON.stringify({
    numeroGiocatori: config.numeroGiocatori,
    nomi: [...config.nomi],
    numeroImpostori: config.numeroImpostori,
    modalita: config.modalita,
    mostraCategoria: config.mostraCategoria,
    categorieSelezionate: [...config.categorieSelezionate],
    impostoreNonPrimo: config.impostoreNonPrimo,
    impostoriSorpresa: config.impostoriSorpresa,
    ordineCasuale: config.ordineCasuale,
    partitaTrappola: config.partitaTrappola,
    promemoriaUltimaPossibilita: config.promemoriaUltimaPossibilita,
    giriIndizi: config.giriIndizi,
  });
}

/** null/vuota/malformata -> default con tutte le categorie selezionate; normalizza ai limiti. */
export function configurazioneDaStringa(s: string | null, categorie: readonly Categoria[]): Configurazione {
  const tutte = new Set(categorie.map((c) => c.id));
  const predefinita = (): Configurazione => configurazioneDefault({ categorieSelezionate: tutte });
  if (s === null || s.trim().length === 0) return predefinita();

  let dto;
  try {
    const o = comeOggetto(analizza(s));
    dto = {
      numeroGiocatori: campo(o, "numeroGiocatori", comeIntero, 4),
      nomi: campo(o, "nomi", (v) => comeLista(v, comeStringa), [] as string[]),
      numeroImpostori: campo(o, "numeroImpostori", comeIntero, 1),
      modalita: campo(o, "modalita", comeStringa, "SENZA_PAROLA"),
      mostraCategoria: campo(o, "mostraCategoria", comeBooleano, true),
      categorieSelezionate: campo(o, "categorieSelezionate", (v) => comeLista(v, comeStringa), [] as string[]),
      impostoreNonPrimo: campo(o, "impostoreNonPrimo", comeBooleano, false),
      impostoriSorpresa: campo(o, "impostoriSorpresa", comeBooleano, false),
      ordineCasuale: campo(o, "ordineCasuale", comeBooleano, false),
      partitaTrappola: campo(o, "partitaTrappola", comeBooleano, false),
      promemoriaUltimaPossibilita: campo(o, "promemoriaUltimaPossibilita", comeBooleano, false),
      giriIndizi: campo(o, "giriIndizi", comeIntero, 1),
    };
  } catch {
    return predefinita();
  }

  const n = limita(dto.numeroGiocatori, MIN_GIOCATORI, MAX_GIOCATORI);
  const valide = new Set(dto.categorieSelezionate.filter((id) => tutte.has(id)));
  const modalita: Modalita = dto.modalita === "PAROLA_AFFINE" ? "PAROLA_AFFINE" : "SENZA_PAROLA";
  return {
    numeroGiocatori: n,
    nomi: dto.nomi.slice(0, n).map((x) => x.slice(0, MAX_LUNGHEZZA_NOME)),
    numeroImpostori: limita(dto.numeroImpostori, 1, maxImpostori(n)),
    modalita,
    mostraCategoria: dto.mostraCategoria,
    categorieSelezionate: valide.size > 0 ? valide : tutte,
    impostoreNonPrimo: dto.impostoreNonPrimo,
    impostoriSorpresa: dto.impostoriSorpresa,
    ordineCasuale: dto.ordineCasuale,
    partitaTrappola: dto.partitaTrappola,
    promemoriaUltimaPossibilita: dto.promemoriaUltimaPossibilita,
    giriIndizi: limita(dto.giriIndizi, 1, MAX_GIRI),
  };
}
