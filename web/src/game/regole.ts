import type { Categoria, Configurazione, ErroreConfigurazione, Modalita, VoceParola } from "./modelli";

export const MIN_GIOCATORI = 3;
export const MAX_GIOCATORI = 20;
export const MAX_LUNGHEZZA_NOME = 20;
export const PROBABILITA_TRAPPOLA = 0.1;
export const MAX_GIRI = 3;

export function maxImpostori(numeroGiocatori: number): number {
  return Math.max(0, Math.trunc((numeroGiocatori - 1) / 2));
}

export function nomiEffettivi(config: Configurazione): string[] {
  const n = Math.max(0, config.numeroGiocatori);
  const r: string[] = [];
  for (let i = 0; i < n; i++) {
    const nome = (config.nomi[i] ?? "").trim();
    r.push(nome.length === 0 ? `Giocatore ${i + 1}` : nome);
  }
  return r;
}

export function pool(
  categorie: readonly Categoria[],
  selezionate: ReadonlySet<string>,
  modalita: Modalita,
): VoceParola[] {
  const r: VoceParola[] = [];
  for (const c of categorie) {
    if (!selezionate.has(c.id)) continue;
    for (const p of c.parole) {
      const affine = p.affine !== null && p.affine.trim().length > 0 ? p.affine : null;
      if (
        modalita === "PAROLA_AFFINE" &&
        (affine === null || affine.trim().toLowerCase() === p.parola.trim().toLowerCase())
      ) {
        continue;
      }
      r.push({ categoriaId: c.id, categoriaNome: c.nome, parola: p.parola, affine });
    }
  }
  return r;
}

export function valida(config: Configurazione, categorie: readonly Categoria[]): ErroreConfigurazione[] {
  const errori: ErroreConfigurazione[] = [];
  const n = config.numeroGiocatori;
  const nOk = n >= MIN_GIOCATORI && n <= MAX_GIOCATORI;
  if (n < MIN_GIOCATORI) errori.push({ tipo: "TroppoPochiGiocatori" });
  if (n > MAX_GIOCATORI) errori.push({ tipo: "TroppiGiocatori" });
  if (nOk) {
    if (config.numeroImpostori < 1) errori.push({ tipo: "TroppoPochiImpostori" });
    if (config.numeroImpostori > maxImpostori(n)) errori.push({ tipo: "TroppiImpostori" });
  }
  const ids = new Set(categorie.map((c) => c.id));
  const almenoUna = [...config.categorieSelezionate].some((id) => ids.has(id));
  if (!almenoUna) {
    errori.push({ tipo: "NessunaCategoria" });
  } else if (pool(categorie, config.categorieSelezionate, config.modalita).length === 0) {
    errori.push({ tipo: "PoolVuoto" });
  }
  const nomi = nomiEffettivi(config);
  nomi.forEach((nome, i) => {
    if (nome.length > MAX_LUNGHEZZA_NOME) errori.push({ tipo: "NomeTroppoLungo", indice: i });
  });
  // groupBy sul nome minuscolo: ordine dei gruppi = prima occorrenza (gia' ordinato per primo indice)
  const gruppi = new Map<string, number[]>();
  nomi.forEach((nome, i) => {
    const k = nome.toLowerCase();
    const g = gruppi.get(k);
    if (g) g.push(i);
    else gruppi.set(k, [i]);
  });
  const duplicati = [...gruppi.values()].filter((g) => g.length > 1).sort((a, b) => a[0] - b[0]);
  for (const indici of duplicati) errori.push({ tipo: "NomeDuplicato", indici });
  return errori;
}

/** CA-06 */
export function conNumeroGiocatori(config: Configurazione, n: number): Configurazione {
  return {
    ...config,
    numeroGiocatori: n,
    nomi: config.nomi.slice(0, Math.max(0, n)),
    numeroImpostori: Math.min(config.numeroImpostori, Math.max(1, maxImpostori(n))),
  };
}

export const Regole = {
  MIN_GIOCATORI,
  MAX_GIOCATORI,
  MAX_LUNGHEZZA_NOME,
  PROBABILITA_TRAPPOLA,
  MAX_GIRI,
  maxImpostori,
  nomiEffettivi,
  valida,
  conNumeroGiocatori,
  pool,
} as const;
