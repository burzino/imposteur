import type { Categoria, Modalita } from "../game/modelli";
import {
  chiaveStringa,
  type ChiaveParola,
  type Partita,
  type SessioneSalvata,
  type StatoDistribuzione,
} from "../game/partita";
import { MAX_GIRI } from "../game/regole";
import {
  analizza,
  campo,
  campoNullable,
  comeBooleano,
  comeIntero,
  comeLista,
  comeOggetto,
  comeStringa,
  richiesto,
  richiestoNullable,
  type Oggetto,
} from "./jsonStretto";

const vuota = (): SessioneSalvata => ({ partita: null, stato: null, usate: [], ultima: null });

function norm(s: string): string {
  return s.trim().toLowerCase();
}

function limita(v: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, v));
}

function chiaveJson(k: ChiaveParola): Oggetto {
  return { categoriaId: k.categoriaId, parola: k.parola };
}

export function sessioneAStringa(s: SessioneSalvata): string {
  const p = s.partita;
  const partita =
    p === null
      ? null
      : {
          giocatori: [...p.giocatori],
          impostori: [...p.impostori].sort((a, b) => a - b),
          categoriaId: p.voce.categoriaId,
          parola: p.voce.parola,
          affine: p.voce.affine,
          modalita: p.modalita,
          mostraCategoria: p.mostraCategoria,
          primoGiocatore: p.primoGiocatore,
          ordine: p.ordine === null ? null : [...p.ordine],
          giriIndizi: p.giriIndizi,
          promemoria: p.promemoriaUltimaPossibilita,
        };
  let stato: { tipo: string; indice: number } | null = null;
  if (p !== null && s.stato !== null) {
    switch (s.stato.tipo) {
      case "Passaggio":
        stato = { tipo: "PASSAGGIO", indice: s.stato.indice };
        break;
      case "Rivelazione":
        stato = { tipo: "RIVELAZIONE", indice: s.stato.indice };
        break;
      case "Gioco":
        stato = { tipo: "GIOCO", indice: 0 };
        break;
    }
  }
  return JSON.stringify({
    partita,
    stato,
    usate: s.usate.map(chiaveJson),
    ultima: s.ultima === null ? null : chiaveJson(s.ultima),
  });
}

function leggiChiave(v: unknown): ChiaveParola {
  const o = comeOggetto(v);
  return {
    categoriaId: campo(o, "categoriaId", comeStringa, ""),
    parola: campo(o, "parola", comeStringa, ""),
  };
}

/** null/malformata -> sessione vuota. Rivelazione(k) -> Passaggio(k). */
export function sessioneDaStringa(s: string | null, categorie: readonly Categoria[]): SessioneSalvata {
  if (s === null || s.trim().length === 0) return vuota();
  let radice: Oggetto;
  let usateGrezze: ChiaveParola[];
  let ultimaGrezza: ChiaveParola | null;
  try {
    radice = comeOggetto(analizza(s));
    usateGrezze = campo(radice, "usate", (v) => comeLista(v, leggiChiave), []);
    ultimaGrezza = campoNullable(radice, "ultima", leggiChiave, null);
    // "partita" e "stato" sono JsonElement? in Kotlin: qualunque valore e' ammesso a questo livello.
  } catch {
    return vuota();
  }
  const esistenti = new Set<string>();
  for (const c of categorie) for (const p of c.parole) esistenti.add(chiaveStringa({ categoriaId: c.id, parola: norm(p.parola) }));
  const normalizza = (k: ChiaveParola): ChiaveParola => ({ categoriaId: k.categoriaId, parola: norm(k.parola) });

  const viste = new Set<string>();
  const usate: ChiaveParola[] = [];
  for (const g of usateGrezze) {
    const k = normalizza(g);
    const ks = chiaveStringa(k);
    if (esistenti.has(ks) && !viste.has(ks)) {
      viste.add(ks);
      usate.push(k);
    }
  }
  let ultima: ChiaveParola | null = null;
  if (ultimaGrezza !== null) {
    const k = normalizza(ultimaGrezza);
    if (esistenti.has(chiaveStringa(k))) ultima = k;
  }
  const ricostruita = ricostruisci(radice, categorie);
  return {
    partita: ricostruita?.partita ?? null,
    stato: ricostruita?.stato ?? null,
    usate,
    ultima,
  };
}

function ricostruisci(
  radice: Oggetto,
  categorie: readonly Categoria[],
): { partita: Partita; stato: StatoDistribuzione } | null {
  let p;
  let st;
  try {
    if (!("partita" in radice) || radice.partita === null) return null;
    const o = comeOggetto(radice.partita);
    p = {
      giocatori: richiesto(o, "giocatori", (v) => comeLista(v, comeStringa)),
      impostori: richiesto(o, "impostori", (v) => comeLista(v, comeIntero)),
      categoriaId: richiesto(o, "categoriaId", comeStringa),
      parola: richiesto(o, "parola", comeStringa),
      affine: richiestoNullable(o, "affine", comeStringa),
      modalita: richiesto(o, "modalita", comeStringa),
      mostraCategoria: richiesto(o, "mostraCategoria", comeBooleano),
      primoGiocatore: richiesto(o, "primoGiocatore", comeIntero),
      ordine: campoNullable(o, "ordine", (v) => comeLista(v, comeIntero), null),
      giriIndizi: campo(o, "giriIndizi", comeIntero, 1),
      promemoria: campo(o, "promemoria", comeBooleano, false),
    };
    if (!("stato" in radice) || radice.stato === null) return null;
    const so = comeOggetto(radice.stato);
    st = { tipo: richiesto(so, "tipo", comeStringa), indice: richiesto(so, "indice", comeIntero) };
  } catch {
    return null;
  }

  const n = p.giocatori.length;
  if (n === 0) return null;
  if (new Set(p.impostori).size !== p.impostori.length) return null;
  if (p.impostori.some((i) => i < 0 || i >= n) || p.primoGiocatore < 0 || p.primoGiocatore >= n) return null;
  const ordine = p.ordine;
  if (ordine !== null) {
    if (ordine.length !== n || new Set(ordine).size !== n || ordine.some((i) => i < 0 || i >= n)) return null;
    if (ordine[0] !== p.primoGiocatore) return null;
  }
  const categoria = categorie.find((c) => c.id === p.categoriaId);
  if (!categoria) return null;
  const parola = categoria.parole.find((x) => norm(x.parola) === norm(p.parola));
  if (!parola) return null;

  let stato: StatoDistribuzione;
  switch (st.tipo) {
    case "PASSAGGIO":
    case "RIVELAZIONE":
      if (st.indice < 0 || st.indice >= n) return null;
      stato = { tipo: "Passaggio", indice: st.indice };
      break;
    case "GIOCO":
      stato = { tipo: "Gioco" };
      break;
    default:
      return null;
  }

  const modalita: Modalita = p.modalita === "PAROLA_AFFINE" ? "PAROLA_AFFINE" : "SENZA_PAROLA";
  if (modalita === "PAROLA_AFFINE") {
    if (p.affine === null || p.affine.trim().length === 0) return null;
    if (parola.affine === null) return null;
    if (norm(parola.affine) !== norm(p.affine)) return null;
  }
  return {
    partita: {
      giocatori: p.giocatori,
      impostori: new Set(p.impostori),
      voce: { categoriaId: categoria.id, categoriaNome: categoria.nome, parola: parola.parola, affine: p.affine },
      modalita,
      mostraCategoria: p.mostraCategoria,
      primoGiocatore: p.primoGiocatore,
      ordine,
      giriIndizi: limita(p.giriIndizi, 1, MAX_GIRI),
      promemoriaUltimaPossibilita: p.promemoria,
    },
    stato,
  };
}
