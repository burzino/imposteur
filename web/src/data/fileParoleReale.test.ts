import { describe, it, expect } from 'vitest';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { parseParole } from './parserParole';
import type { Categoria } from '../game/modelli';

// I test girano con cwd = web/ (npm test da web/): il file reale sta in ../app/src/main/assets/parole.json.
const percorso = resolve(process.cwd(), '..', 'app', 'src', 'main', 'assets', 'parole.json');

function sorgente(): string {
  expect(existsSync(percorso), `file mancante: ${percorso}`).toBe(true);
  return readFileSync(percorso, 'utf-8');
}

function carica(): readonly Categoria[] {
  const r = parseParole(sorgente());
  if (r.tipo !== 'Ok') throw new Error(`parse fallito: ${JSON.stringify(r)}`);
  return r.categorie;
}

describe('parole.json reale', () => {
  it('CA-17 e CA-W05 file reale: JSON valido, categorie non vuote e id univoci', () => {
    expect(() => JSON.parse(sorgente())).not.toThrow();
    const c = carica();
    expect(c.length).toBeGreaterThan(0);
    for (const k of c) expect(k.parole.length, `categoria vuota: ${k.id}`).toBeGreaterThan(0);
    expect(new Set(c.map((k) => k.id)).size, 'id categoria univoci').toBe(c.length);
  });

  it('CA-17 file reale: nessuna parola duplicata e affine diversa dalla parola', () => {
    for (const k of carica()) {
      const chiavi = k.parole.map((p) => p.parola.trim().toLowerCase());
      expect(new Set(chiavi).size, `duplicati in ${k.id}`).toBe(chiavi.length);
      for (const p of k.parole) {
        expect(p.parola.trim().length).toBeGreaterThan(0);
        if (p.affine !== null) {
          expect(p.affine.trim().toLowerCase(), `affine == parola: ${p.parola}`).not.toBe(p.parola.trim().toLowerCase());
        }
      }
    }
  });

  it('CA-17 file reale: il parser non scarta parole', () => {
    const nel = (sorgente().match(/"parola"\s*:/g) ?? []).length;
    const parsate = carica().reduce((acc, k) => acc + k.parole.length, 0);
    expect(parsate, 'parole nel sorgente vs parsate').toBe(nel);
  });
});
