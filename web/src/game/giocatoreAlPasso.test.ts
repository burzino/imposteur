import { describe, it, expect } from 'vitest';
import { GestorePartite, giocatoreAlPasso, contenutoPer, ordineDiParola } from './partita';
import type { Partita } from './partita';
import { casualeConSeme } from './casuale';
import { categorieDemo, configBase, partitaBase } from './fixture';

const cats = categorieDemo();
const partita = (n: number, primo: number, ordine: number[] | null = null, impostori: number[] = [0]): Partita =>
  partitaBase({
    giocatori: Array.from({ length: n }, (_, i) => `G${i}`),
    impostori: new Set(impostori),
    primoGiocatore: primo,
    ordine,
  });
const range = (n: number) => Array.from({ length: n }, (_, i) => i);
const passi = (p: Partita) => range(p.giocatori.length).map((k) => giocatoreAlPasso(p, k));
const ordinato = (v: readonly number[]) => [...v].sort((a, b) => a - b);

describe('giocatoreAlPasso (contratto v2.4)', () => {
  it('CA-119 con ordine 0 2 1 i passi 0 1 2 danno i giocatori 0 2 1', () => {
    expect(passi(partita(3, 0, [0, 2, 1]))).toEqual([0, 2, 1]);
  });

  it('CA-119 senza ordine e primo 2 su 4 giocatori i passi danno 2 3 0 1', () => {
    expect(passi(partita(4, 2))).toEqual([2, 3, 0, 1]);
  });

  it('CA-121 senza ordine e primo 0 e identita come prima', () => {
    expect(passi(partita(5, 0))).toEqual([0, 1, 2, 3, 4]);
  });

  it('CA-119 ordine casuale 1 0 2 su 3 giocatori', () => {
    expect(passi(partita(3, 1, [1, 0, 2]))).toEqual([1, 0, 2]);
  });

  it('CA-119 il passo 0 e sempre primoGiocatore e i risultati sono una permutazione', () => {
    for (let n = 3; n <= 20; n++) for (let primo = 0; primo < n; primo++) {
      const p = partita(n, primo);
      expect(giocatoreAlPasso(p, 0), `N=${n} primo=${primo}`).toBe(primo);
      expect(ordinato(passi(p)), `N=${n} primo=${primo}`).toEqual(range(n));
    }
    for (let seed = 0; seed < 100; seed++) {
      const n = 3 + (seed % 18);
      const r = new GestorePartite(casualeConSeme(seed)).nuovaPartita(configBase({ n }, { ordineCasuale: true }), cats);
      expect(r.tipo).toBe('Ok');
      if (r.tipo !== 'Ok') return;
      const s = passi(r.partita);
      expect(ordinato(s), `seed=${seed}`).toEqual(range(n));
      expect(s[0], `seed=${seed}`).toBe(r.partita.primoGiocatore);
    }
  });

  it('CA-119 k fuori da 0..N-1 o non intero e RangeError', () => {
    for (const p of [partita(3, 0, [0, 2, 1]), partita(4, 2), partita(20, 5)]) {
      const n = p.giocatori.length;
      for (const k of [-1, n, n + 5, 1.5, NaN, Infinity]) {
        expect(() => giocatoreAlPasso(p, k), `N=${n} k=${k}`).toThrow(RangeError);
      }
    }
  });

  it('CA-119 con 20 giocatori coincide con ordineDiParola', () => {
    const p = partita(20, 13);
    for (let k = 0; k < 20; k++) expect(giocatoreAlPasso(p, k)).toBe(ordineDiParola(p)[k]);
    const ordine = [7, ...range(20).filter((i) => i !== 7).reverse()];
    const q = partita(20, 7, ordine);
    for (let k = 0; k < 20; k++) expect(giocatoreAlPasso(q, k)).toBe(ordineDiParola(q)[k]);
  });

  it('CA-120 con ordine 0 2 1 e impostore il giocatore 2 il passo 1 mostra il ruolo da impostore di G2', () => {
    const p = { ...partita(3, 0, [0, 2, 1], [2]), modalita: 'SENZA_PAROLA' as const, mostraCategoria: true };
    const g = giocatoreAlPasso(p, 1);
    expect(g).toBe(2);
    expect(p.giocatori[g]).toBe('G2');
    expect(contenutoPer(p, g).tipo).toBe('Impostore');
    // la riga 1 della lista originale (G1) e un civile: non va confusa con il giocatore al passo 1
    expect(contenutoPer(p, 1).tipo).toBe('ParolaSegreta');
  });

  it("CA-122 l'elenco in ordine di parola mappa gli indici sui nomi e il tocco usa l'indice giocatore", () => {
    const p = partita(3, 0, [0, 2, 1], [2]);
    const elenco = ordineDiParola(p).map((g) => [g, p.giocatori[g]]);
    expect(elenco).toEqual([[0, 'G0'], [2, 'G2'], [1, 'G1']]);
    expect(contenutoPer(p, elenco[1][0] as number).tipo).toBe('Impostore');
  });
});
