import { describe, it, expect } from 'vitest';
import { GestorePartite, chiaveStringa } from './partita';
import type { ChiaveParola } from './partita';
import type { Configurazione, VoceParola } from './modelli';
import { casualeConSeme } from './casuale';
import { categorieDemo, configBase } from './fixture';

const cats = categorieDemo();
const tutte: ChiaveParola[] = cats.flatMap((c) => c.parole.map((p) => ({ categoriaId: c.id, parola: p.parola.toLowerCase() })));
const gestore = (seed: number, usate?: Iterable<ChiaveParola>, ultima?: ChiaveParola | null) =>
  new GestorePartite(casualeConSeme(seed), usate, ultima);

function voce(g: GestorePartite, c: Configurazione = configBase()): VoceParola {
  const r = g.nuovaPartita(c, cats);
  if (r.tipo !== 'Ok') throw new Error(`atteso Ok: ${JSON.stringify(r)}`);
  return r.partita.voce;
}
const chiave = (v: VoceParola): ChiaveParola => ({ categoriaId: v.categoriaId, parola: v.parola.trim().toLowerCase() });
const stringhe = (v: readonly ChiaveParola[]) => v.map(chiaveStringa).sort();

describe('GestorePartite: stato di sessione', () => {
  it('CA-42 stato iniziale vuoto di default', () => {
    const g = gestore(1);
    expect(g.usate).toEqual([]);
    expect(g.ultima).toBeNull();
  });

  it('CA-42 usate e ultima esposte dopo ogni estrazione', () => {
    const g = gestore(3);
    const v = voce(g);
    expect(g.usate).toEqual([chiave(v)]);
    expect(g.ultima).toEqual(chiave(v));
    const v2 = voce(g);
    expect(g.usate).toHaveLength(2);
    expect(g.ultima).toEqual(chiave(v2));
  });

  it('CA-42 usateIniziali escluse dall estrazione successiva', () => {
    for (let seed = 0; seed < 50; seed++) {
      const g = gestore(seed, tutte.slice(1));
      expect(chiave(voce(g))).toEqual(tutte[0]);
    }
  });

  it('CA-42 dopo salvataggio e ripristino non si ripetono parole fino a esaurimento', () => {
    for (let seed = 0; seed < 30; seed++) {
      const g1 = gestore(seed);
      const viste = new Set<string>();
      viste.add(chiaveStringa(chiave(voce(g1))));
      viste.add(chiaveStringa(chiave(voce(g1))));
      const g2 = gestore(seed + 1000, g1.usate, g1.ultima);
      for (let i = 0; i < tutte.length - 2; i++) {
        const k = chiaveStringa(chiave(voce(g2)));
        expect(viste.has(k), 'ripetuta').toBe(false);
        viste.add(k);
      }
      expect(viste.size).toBe(tutte.length);
    }
  });

  it('CA-42 esaurimento del pool azzera le usate ed evita l ultima', () => {
    for (let seed = 0; seed < 50; seed++) {
      const ultima = tutte[0];
      const g = gestore(seed, tutte, ultima);
      const v = chiave(voce(g));
      expect(v).not.toEqual(ultima);
      expect(g.usate).toEqual([v]);
    }
  });

  it('CA-42 usate iniziali fuori dal pool non impediscono l estrazione', () => {
    const g = gestore(5, [{ categoriaId: 'fantasy', parola: 'elfo' }]);
    const v = voce(g);
    expect(cats.some((c) => c.id === v.categoriaId)).toBe(true);
  });

  it('CA-42 usate e una copia', () => {
    const g = gestore(2);
    const prima = g.usate;
    voce(g);
    expect(prima).toEqual([]);
    expect(g.usate).toHaveLength(1);
  });

  it('CA-42 il contenitore iniziale passato non viene alterato', () => {
    const iniz: ChiaveParola[] = [{ categoriaId: 'animali', parola: 'cane' }];
    const g = gestore(2, iniz);
    voce(g);
    expect(iniz).toHaveLength(1);
  });

  it('CA-42 usate senza duplicati (contratto)', () => {
    const g = gestore(4, [tutte[0], tutte[0], tutte[1]]);
    expect(new Set(stringhe(g.usate)).size).toBe(g.usate.length);
  });
});
