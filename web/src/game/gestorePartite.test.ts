import { describe, it, expect } from 'vitest';
import { GestorePartite } from './partita';
import type { Partita, RisultatoNuovaPartita } from './partita';
import { casualeConSeme } from './casuale';
import { cat, categorieDemo, configBase } from './fixture';

const cats = categorieDemo();

function ok(r: RisultatoNuovaPartita): Partita {
  if (r.tipo !== 'Ok') throw new Error(`atteso Ok, ottenuto ${JSON.stringify(r)}`);
  return r.partita;
}
const gestore = (seed: number) => new GestorePartite(casualeConSeme(seed));
const range = (n: number) => Array.from({ length: n }, (_, i) => i);

describe('GestorePartite', () => {
  it('CA-07 esattamente K impostori distinti in range', () => {
    for (let seed = 0; seed < 100; seed++) {
      for (const [n, k] of [[3, 1], [5, 2], [7, 3], [20, 9]]) {
        const p = ok(gestore(seed).nuovaPartita(configBase({ n, k }), cats));
        expect(p.impostori.size).toBe(k);
        expect([...p.impostori].every((i) => Number.isInteger(i) && i >= 0 && i < n)).toBe(true);
        expect(p.giocatori).toHaveLength(n);
        expect(range(n).filter((i) => !p.impostori.has(i))).toHaveLength(n - k);
      }
    }
  });

  it('CA-08 stesso seme stessa partita', () => {
    const c = configBase({ n: 8, k: 3 });
    const a = ok(gestore(42).nuovaPartita(c, cats));
    const b = ok(gestore(42).nuovaPartita(c, cats));
    expect(a).toEqual(b);
  });

  it('CA-08 ogni giocatore puo essere impostore', () => {
    const visti = new Set<number>();
    for (let seed = 0; seed < 500; seed++) {
      for (const i of ok(gestore(seed).nuovaPartita(configBase({ n: 6, k: 2 }), cats)).impostori) visti.add(i);
    }
    expect(visti).toEqual(new Set(range(6)));
  });

  it('CA-12 parola appartiene alle categorie selezionate', () => {
    const cs = [cat('a', ['Uno', 'X1'], ['Due', 'X2']), cat('b', ['Tre', 'X3'])];
    for (let seed = 0; seed < 100; seed++) {
      const p = ok(gestore(seed).nuovaPartita(configBase({ cats: ['a'] }), cs));
      expect(p.voce.categoriaId).toBe('a');
      expect(['Uno', 'Due']).toContain(p.voce.parola);
    }
  });

  it('CA-12 modalita affine voce con affine valido', () => {
    const cs = [cat('a', ['Uno', null], ['Due', 'Tre'], ['Quattro', 'quattro'])];
    const p = ok(gestore(1).nuovaPartita(configBase({ mod: 'PAROLA_AFFINE', cats: ['a'] }), cs));
    expect(p.voce.parola).toBe('Due');
    expect(p.voce.affine).toBe('Tre');
  });

  it('CA-13 prime M partite usano M parole diverse', () => {
    for (let seed = 0; seed < 30; seed++) {
      const g = gestore(seed);
      const usate = range(5).map(() => ok(g.nuovaPartita(configBase(), cats)).voce.parola);
      expect(new Set(usate).size, `seed ${seed}: ${usate}`).toBe(5);
    }
  });

  it('CA-14 esaurito il pool non ripete l ultima parola', () => {
    for (let seed = 0; seed < 50; seed++) {
      const g = gestore(seed);
      const parole = range(15).map(() => ok(g.nuovaPartita(configBase(), cats)).voce.parola);
      for (let i = 1; i < parole.length; i++) expect(parole[i], `seed ${seed}: ${parole}`).not.toBe(parole[i - 1]);
    }
  });

  it('CA-14 pool di una sola parola ripete senza errori', () => {
    const cs = [cat('a', ['Solo', 'Uno'])];
    const g = gestore(3);
    for (let i = 0; i < 4; i++) expect(ok(g.nuovaPartita(configBase({ cats: ['a'] }), cs)).voce.parola).toBe('Solo');
  });

  it('CA-14 le usate non si azzerano cambiando categorie', () => {
    const g = gestore(9);
    const cs = [cat('a', ['A1', 'x'], ['A2', 'y']), cat('b', ['B1', 'z'], ['B2', 'w'])];
    const prime = range(2).map(() => ok(g.nuovaPartita(configBase({ cats: ['a'] }), cs)).voce.parola);
    expect(new Set(prime).size).toBe(2);
    const poi = range(2).map(() => ok(g.nuovaPartita(configBase({ cats: ['a', 'b'] }), cs)).voce.parola);
    expect(poi.every((p) => p.startsWith('B'))).toBe(true);
  });

  it('CA-15 primo giocatore in range e tutti compaiono', () => {
    const visti = new Set<number>();
    for (let seed = 0; seed < 300; seed++) {
      const p = ok(gestore(seed).nuovaPartita(configBase({ n: 7, k: 3 }), cats));
      expect(p.primoGiocatore).toBeGreaterThanOrEqual(0);
      expect(p.primoGiocatore).toBeLessThan(7);
      visti.add(p.primoGiocatore);
    }
    expect(visti).toEqual(new Set(range(7)));
  });

  it('CA-16 pool vuoto restituisce Errore senza eccezioni', () => {
    const cs = [cat('a', ['Uno', null])];
    const r = gestore(0).nuovaPartita(configBase({ mod: 'PAROLA_AFFINE', cats: ['a'] }), cs);
    expect(r.tipo).toBe('Errore');
    if (r.tipo === 'Errore') expect(r.errori.map((e) => e.tipo)).toContain('PoolVuoto');
  });

  it('configurazione non valida restituisce Errore (CA-02)', () => {
    const r = gestore(0).nuovaPartita(configBase({ n: 5, k: 4 }), cats);
    expect(r.tipo).toBe('Errore');
    if (r.tipo === 'Errore') expect(r.errori.map((e) => e.tipo)).toContain('TroppiImpostori');
  });

  it('CA-20 nuova partita mantiene giocatori e impostazioni e rinnova il resto', () => {
    const nomi = ['Anna', 'Bruno', 'Carla', 'Dino', 'Elio', 'Fabio'];
    const c = configBase({ n: 6, k: 2, nomi, mostra: false });
    const g = gestore(5);
    const partite = range(5).map(() => ok(g.nuovaPartita(c, cats)));
    for (const p of partite) {
      expect(p.giocatori).toEqual(nomi);
      expect(p.impostori.size).toBe(2);
      expect(p.mostraCategoria).toBe(false);
      expect(p.modalita).toBe('SENZA_PAROLA');
    }
    expect(new Set(partite.map((p) => p.voce.parola)).size).toBe(5);
    expect(new Set(partite.map((p) => [...p.impostori].sort().join(','))).size).toBeGreaterThan(1);
    expect(new Set(partite.map((p) => p.primoGiocatore)).size).toBeGreaterThan(1);
  });
});
