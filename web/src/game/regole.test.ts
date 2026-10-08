import { describe, it, expect } from 'vitest';
import { Regole } from './regole';
import { cat, categorieDemo, configBase } from './fixture';

const cats = categorieDemo();
const tipi = (e: readonly { tipo: string }[]) => e.map((x) => x.tipo);

describe('Regole', () => {
  it('CA-01 maxImpostori', () => {
    const atteso: [number, number][] = [[3, 1], [4, 1], [5, 2], [6, 2], [7, 3], [20, 9]];
    for (const [n, k] of atteso) expect(Regole.maxImpostori(n), `N=${n}`).toBe(k);
  });

  it('CA-02 troppo pochi e troppi giocatori', () => {
    expect(tipi(Regole.valida(configBase({ n: 2 }), cats))).toContain('TroppoPochiGiocatori');
    expect(tipi(Regole.valida(configBase({ n: 21 }), cats))).toContain('TroppiGiocatori');
    expect(Regole.valida(configBase({ n: 3 }), cats)).toEqual([]);
    expect(Regole.valida(configBase({ n: 20, k: 9 }), cats)).toEqual([]);
  });

  it('CA-02 impostori fuori limite con errori distinti', () => {
    expect(tipi(Regole.valida(configBase({ k: 0 }), cats))).toContain('TroppoPochiImpostori');
    const tanti = tipi(Regole.valida(configBase({ n: 5, k: 3 }), cats));
    expect(tanti).toContain('TroppiImpostori');
    expect(tanti).not.toContain('TroppoPochiImpostori');
    expect(Regole.valida(configBase({ n: 5, k: 2 }), cats)).toEqual([]);
  });

  it('CA-03 nessuna categoria', () => {
    expect(tipi(Regole.valida(configBase({ cats: [] }), cats))).toContain('NessunaCategoria');
  });

  it('CA-04 nomi duplicati case-insensitive dopo trim', () => {
    const c = configBase({ n: 4, nomi: ['Anna', ' anna ', 'Bruno', 'Carla'] });
    const e = Regole.valida(c, cats).filter((x) => x.tipo === 'NomeDuplicato');
    expect(e).toHaveLength(1);
    const d = e[0] as { tipo: 'NomeDuplicato'; indici: readonly number[] };
    expect(new Set(d.indici)).toEqual(new Set([0, 1]));
  });

  it('CA-04 duplicato con nome di default', () => {
    const c = configBase({ n: 4, nomi: ['Giocatore 2', '', 'Bruno', 'Carla'] });
    const e = Regole.valida(c, cats).filter((x) => x.tipo === 'NomeDuplicato');
    expect(e).toHaveLength(1);
    const d = e[0] as { tipo: 'NomeDuplicato'; indici: readonly number[] };
    expect(new Set(d.indici)).toEqual(new Set([0, 1]));
  });

  it('CA-04 nessun duplicato con nomi distinti', () => {
    const c = configBase({ n: 3, nomi: ['A', 'B', ''] });
    expect(tipi(Regole.valida(c, cats))).not.toContain('NomeDuplicato');
  });

  it('CA-05 nomi effettivi default e trim', () => {
    const c = configBase({ n: 4, nomi: ['', '   ', '  Luca  '] });
    expect(Regole.nomiEffettivi(c)).toEqual(['Giocatore 1', 'Giocatore 2', 'Luca', 'Giocatore 4']);
  });

  it('CA-05 nomi effettivi senza nomi inseriti ha lunghezza N', () => {
    expect(Regole.nomiEffettivi(configBase({ n: 6 }))).toHaveLength(6);
  });

  it('CA-05 nome oltre 20 caratteri rifiutato', () => {
    const lungo = 'x'.repeat(21);
    const e = Regole.valida(configBase({ n: 3, nomi: ['A', lungo, 'C'] }), cats);
    expect(e).toContainEqual({ tipo: 'NomeTroppoLungo', indice: 1 });
    const ok = Regole.valida(configBase({ n: 3, nomi: ['A', 'x'.repeat(20), 'C'] }), cats);
    expect(tipi(ok)).not.toContain('NomeTroppoLungo');
  });

  it('CA-05 la lunghezza si valuta dopo trim', () => {
    const nome = '  ' + 'x'.repeat(20) + '  ';
    const e = Regole.valida(configBase({ n: 3, nomi: [nome, 'B', 'C'] }), cats);
    expect(tipi(e)).not.toContain('NomeTroppoLungo');
  });

  it('CA-06 riducendo N gli impostori scendono al massimo', () => {
    const c = Regole.conNumeroGiocatori(configBase({ n: 9, k: 4 }), 5);
    expect(c.numeroGiocatori).toBe(5);
    expect(c.numeroImpostori).toBe(2);
    expect(Regole.conNumeroGiocatori(configBase({ n: 9, k: 4 }), 3).numeroImpostori).toBe(1);
  });

  it('CA-06 aumentando N gli impostori restano', () => {
    const c = Regole.conNumeroGiocatori(configBase({ n: 5, k: 2 }), 10);
    expect(c.numeroGiocatori).toBe(10);
    expect(c.numeroImpostori).toBe(2);
  });

  it('CA-06 conNumeroGiocatori non muta la configurazione di partenza', () => {
    const c = configBase({ n: 9, k: 4 });
    Regole.conNumeroGiocatori(c, 3);
    expect(c.numeroGiocatori).toBe(9);
    expect(c.numeroImpostori).toBe(4);
  });

  it('CA-11 pool in modalita affine esclude parole senza affine valido', () => {
    const cs = [cat('a', ['Uno', 'Due'], ['Tre', null], ['Quattro', ''], ['Cinque', 'cinque'], ['Sei', 'Sette'])];
    const pool = Regole.pool(cs, new Set(['a']), 'PAROLA_AFFINE');
    expect(new Set(pool.map((v) => v.parola))).toEqual(new Set(['Uno', 'Sei']));
    expect(pool.every((v) => v.affine !== null && v.affine !== '')).toBe(true);
  });

  it('CA-11 pool senza parola include tutte e solo le categorie selezionate', () => {
    const cs = [cat('a', ['Uno', null], ['Due', 'Tre']), cat('b', ['Altro', 'X'])];
    const pool = Regole.pool(cs, new Set(['a']), 'SENZA_PAROLA');
    expect(new Set(pool.map((v) => v.parola))).toEqual(new Set(['Uno', 'Due']));
    expect(pool.every((v) => v.categoriaId === 'a' && v.categoriaNome === 'A')).toBe(true);
  });

  it('CA-16 pool vuoto segnalato dalla validazione', () => {
    const cs = [cat('a', ['Uno', null]), cat('b', ['Due', 'Tre'])];
    const e = Regole.valida(configBase({ mod: 'PAROLA_AFFINE', cats: ['a'] }), cs);
    expect(tipi(e)).toContain('PoolVuoto');
    expect(tipi(Regole.valida(configBase({ mod: 'SENZA_PAROLA', cats: ['a'] }), cs))).not.toContain('PoolVuoto');
  });

  it('costanti delle regole (CA-01, CA-02, CA-05)', () => {
    expect(Regole.MIN_GIOCATORI).toBe(3);
    expect(Regole.MAX_GIOCATORI).toBe(20);
    expect(Regole.MAX_LUNGHEZZA_NOME).toBe(20);
  });
});
