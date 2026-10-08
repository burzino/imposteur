import { describe, it, expect } from 'vitest';
import { parseParole } from './parserParole';
import type { Categoria } from '../game/modelli';

function ok(json: string): readonly Categoria[] {
  const r = parseParole(json);
  if (r.tipo !== 'Ok') throw new Error(`atteso Ok: ${JSON.stringify(r)}`);
  return r.categorie;
}
const errore = (json: string) => expect(parseParole(json).tipo).toBe('Errore');

describe('parseParole', () => {
  it('CA-17 formato valido', () => {
    const c = ok('{"versione":1,"categorie":[{"id":"animali","nome":"Animali","parole":[{"parola":"Cane","affine":"Lupo"}]}]}');
    expect(c).toHaveLength(1);
    expect(c[0].id).toBe('animali');
    expect(c[0].nome).toBe('Animali');
    expect(c[0].parole).toEqual([{ parola: 'Cane', affine: 'Lupo' }]);
  });

  it('CA-17 versione diversa da 1 rifiutata', () => {
    errore('{"versione":2,"categorie":[]}');
    errore('{"versione":0,"categorie":[]}');
  });

  it('CA-17 e CA-W05 json malformato o illeggibile rifiutato (CA-34)', () => {
    errore('{non json');
    errore('');
    errore('null');
    errore('[]');
  });

  it('CA-17 parola vuota rifiutata', () => {
    errore('{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[{"parola":"","affine":"x"}]}]}');
  });

  it('CA-17 affine assente o vuoto tollerato come null', () => {
    const c = ok('{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[{"parola":"Uno"},{"parola":"Due","affine":""}]}]}');
    expect(c[0].parole).toEqual([{ parola: 'Uno', affine: null }, { parola: 'Due', affine: null }]);
  });

  it('CA-17 nessuna coppia con affine uguale alla parola', () => {
    const c = ok('{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[{"parola":"Uno","affine":"uno"},{"parola":"Due","affine":"Tre"}]}]}');
    const ps = c[0].parole;
    expect(ps.every((p) => p.affine === null || p.affine.toLowerCase() !== p.parola.toLowerCase())).toBe(true);
    expect(ps).toContainEqual({ parola: 'Due', affine: 'Tre' });
  });

  it('CA-17 categoria senza parole mantenuta', () => {
    const c = ok('{"versione":1,"categorie":[{"id":"a","nome":"A","parole":[]}]}');
    expect(c).toHaveLength(1);
    expect(c[0].parole).toEqual([]);
  });

  it('CA-W05 chiavi sconosciute ignorate', () => {
    const c = ok('{"versione":1,"extra":true,"categorie":[{"id":"a","nome":"A","x":1,"parole":[{"parola":"Uno","affine":"Due","y":[]}]}]}');
    expect(c[0].parole).toEqual([{ parola: 'Uno', affine: 'Due' }]);
  });
});
