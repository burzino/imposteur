import { describe, it, expect } from 'vitest';
import { GestorePartite, ordineDiParola } from './partita';
import type { Partita } from './partita';
import { casualeConSeme } from './casuale';
import { categorieDemo, configBase, partitaBase } from './fixture';

const cats = categorieDemo();
const partita = (n: number, primo: number): Partita =>
  partitaBase({ giocatori: Array.from({ length: n }, (_, i) => `G${i}`), impostori: new Set([0]), primoGiocatore: primo });
const range = (n: number) => Array.from({ length: n }, (_, i) => i);
const ordinato = (v: readonly number[]) => [...v].sort((a, b) => a - b);

describe('ordineDiParola', () => {
  it("ORD-01 con primo 0 l'ordine e 0 fino a N-1", () => {
    expect(ordineDiParola(partita(5, 0))).toEqual([0, 1, 2, 3, 4]);
  });

  it("ORD-02 con primo 2 su 4 giocatori l'ordine e 2 3 0 1", () => {
    expect(ordineDiParola(partita(4, 2))).toEqual([2, 3, 0, 1]);
  });

  it("ORD-03 con primo N-1 l'ordine e N-1 0 1 eccetera", () => {
    for (const n of [3, 6, 20]) expect(ordineDiParola(partita(n, n - 1))).toEqual([n - 1, ...range(n - 1)]);
  });

  it('ORD-04 per ogni N e ogni primo e una permutazione che parte dal primo', () => {
    for (let n = 3; n <= 20; n++) for (let primo = 0; primo < n; primo++) {
      const o = ordineDiParola(partita(n, primo));
      expect(ordinato(o), `N=${n} primo=${primo}`).toEqual(range(n));
      expect(o[0], `N=${n} primo=${primo}`).toBe(primo);
    }
  });

  it("ORD-05 in partite da GestorePartite il primo dell'ordine e primoGiocatore", () => {
    for (let seed = 0; seed < 200; seed++) {
      const n = 3 + (seed % 18);
      const r = new GestorePartite(casualeConSeme(seed)).nuovaPartita(configBase({ n }), cats);
      expect(r.tipo).toBe('Ok');
      if (r.tipo !== 'Ok') return;
      const o = ordineDiParola(r.partita);
      expect(o[0], `seed=${seed}`).toBe(r.partita.primoGiocatore);
      expect(ordinato(o), `seed=${seed}`).toEqual(range(n));
    }
  });
});
