import { describe, it, expect } from 'vitest';
import { GestorePartite, chiaveDi, chiaveStringa } from './partita';
import type { ChiaveParola } from './partita';
import type { VoceParola } from './modelli';
import { Regole } from './regole';
import { casualeConSeme } from './casuale';
import { categorieDemo, configBase } from './fixture';

// Contatore delle parole da giocare (CA-61).
const categorie = categorieDemo();
const poolA = Regole.pool(categorie, new Set(['animali']), 'SENZA_PAROLA');
const poolB = Regole.pool(categorie, new Set(['cibo']), 'SENZA_PAROLA');
const poolTutto = Regole.pool(categorie, new Set(['animali', 'cibo']), 'SENZA_PAROLA');

const gestore = (seed: number, usate?: Iterable<ChiaveParola>, ultima?: ChiaveParola | null) =>
  new GestorePartite(casualeConSeme(seed), usate, ultima);

function gioca(g: GestorePartite, c: string) {
  const r = g.nuovaPartita(configBase({ cats: [c] }), categorie);
  expect(r.tipo).toBe('Ok');
}
const insiemeStringhe = (pool: readonly VoceParola[]) => new Set(pool.map((v) => chiaveStringa(chiaveDi(v))));
const ordinate = (v: readonly ChiaveParola[]) => v.map(chiaveStringa).sort();
const inPool = (v: readonly ChiaveParola[], pool: readonly VoceParola[]) =>
  v.filter((k) => insiemeStringhe(pool).has(chiaveStringa(k)));

describe('Contatore parole', () => {
  it('CA-61 a pool nuovo rimanenti vale pool.length', () => {
    const g = gestore(1);
    expect(g.rimanenti(poolTutto)).toBe(poolTutto.length);
    expect(g.rimanenti(poolA)).toBe(poolA.length);
    expect(g.rimanenti(poolB)).toBe(poolB.length);
  });

  it('CA-61 rimanenti cala di 1 a ogni partita', () => {
    const g = gestore(2);
    let atteso = poolA.length;
    for (let i = 0; i < poolA.length; i++) {
      gioca(g, 'animali');
      atteso -= 1;
      expect(g.rimanenti(poolA)).toBe(atteso);
    }
  });

  it('CA-61 a pool esaurito rimanenti vale 0', () => {
    const g = gestore(3);
    for (let i = 0; i < poolA.length; i++) gioca(g, 'animali');
    expect(g.rimanenti(poolA)).toBe(0);
    expect(g.rimanenti(poolB)).toBe(poolB.length);
  });

  it('CA-61 azzeraUsate sul pool A non tocca le usate del pool B', () => {
    const g = gestore(4);
    for (let i = 0; i < poolA.length; i++) gioca(g, 'animali');
    for (let i = 0; i < 2; i++) gioca(g, 'cibo');
    const usateB = ordinate(inPool(g.usate, poolB));
    expect(usateB).toHaveLength(2);

    g.azzeraUsate(poolA);

    expect(ordinate(g.usate)).toEqual(usateB);
    expect(g.rimanenti(poolA)).toBe(poolA.length);
    expect(g.rimanenti(poolB)).toBe(0);
  });

  it('CA-61 ultima diventa null solo se appartiene al pool azzerato', () => {
    const g = gestore(5);
    gioca(g, 'animali');
    gioca(g, 'cibo');
    const ultimaB = g.ultima;
    expect(ultimaB).not.toBeNull();
    expect(insiemeStringhe(poolB).has(chiaveStringa(ultimaB!))).toBe(true);

    g.azzeraUsate(poolA);
    expect(g.ultima).toEqual(ultimaB);

    g.azzeraUsate(poolB);
    expect(g.ultima).toBeNull();
  });

  it('CA-61 azzeraUsate del pool contenente ultima la mette a null', () => {
    const g = gestore(6);
    gioca(g, 'cibo');
    gioca(g, 'animali');
    g.azzeraUsate(poolA);
    expect(g.ultima).toBeNull();
  });

  it('CA-61 dopo azzeraUsate rimanenti torna a pool.length', () => {
    const g = gestore(7);
    gioca(g, 'animali');
    gioca(g, 'cibo');
    gioca(g, 'animali');
    g.azzeraUsate(poolTutto);
    expect(g.rimanenti(poolTutto)).toBe(poolTutto.length);
    expect(g.usate).toEqual([]);
    expect(g.ultima).toBeNull();
  });

  it('CA-61 round-trip con usate e ultima esposte non cambia rimanenti', () => {
    const g = gestore(8);
    gioca(g, 'animali');
    gioca(g, 'cibo');
    gioca(g, 'animali');

    const copia = gestore(99, g.usate, g.ultima);
    expect(ordinate(copia.usate)).toEqual(ordinate(g.usate));
    expect(copia.ultima).toEqual(g.ultima);
    expect(copia.rimanenti(poolTutto)).toBe(g.rimanenti(poolTutto));
    expect(copia.rimanenti(poolA)).toBe(g.rimanenti(poolA));

    g.azzeraUsate(poolA);
    const copia2 = gestore(98, g.usate, g.ultima);
    expect(ordinate(copia2.usate)).toEqual(ordinate(g.usate));
    expect(copia2.ultima).toEqual(g.ultima);
    expect(copia2.rimanenti(poolTutto)).toBe(g.rimanenti(poolTutto));
  });
});
