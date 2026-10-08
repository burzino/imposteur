import { describe, it, expect } from 'vitest';
import { contenutoPer, testoSvelamento } from './partita';
import type { Modalita } from './modelli';
import { partitaBase } from './fixture';

const partita = (modalita: Modalita, impostori: number[] = [1], mostraCategoria = true) =>
  partitaBase({ modalita, impostori: new Set(impostori), mostraCategoria });

describe('ContenutoRuolo', () => {
  it('CA-09 civile vede la parola', () => {
    expect(contenutoPer(partita('SENZA_PAROLA'), 0)).toEqual({ tipo: 'ParolaSegreta', testo: 'Cane' });
  });

  it('CA-09 impostore con categoria', () => {
    expect(contenutoPer(partita('SENZA_PAROLA'), 1)).toEqual({ tipo: 'Impostore', categoria: 'Animali' });
  });

  it('CA-09 impostore senza categoria se disattiva', () => {
    expect(contenutoPer(partita('SENZA_PAROLA', [1], false), 1)).toEqual({ tipo: 'Impostore', categoria: null });
  });

  it('CA-09 impostore non contiene parola ne affine', () => {
    const s = JSON.stringify(contenutoPer(partita('SENZA_PAROLA'), 1));
    expect(s).not.toContain('Cane');
    expect(s).not.toContain('Lupo');
  });

  it('CA-10 affine civile parola impostore affine stessa struttura', () => {
    const p = partita('PAROLA_AFFINE');
    const civ = contenutoPer(p, 0);
    const imp = contenutoPer(p, 1);
    expect(civ).toEqual({ tipo: 'ParolaSegreta', testo: 'Cane' });
    expect(imp).toEqual({ tipo: 'ParolaSegreta', testo: 'Lupo' });
    expect(civ.tipo).toBe(imp.tipo);
  });

  it('CA-10 affine nessun contenuto e di tipo Impostore', () => {
    const p = partita('PAROLA_AFFINE', [0, 2]);
    for (let i = 0; i < 4; i++) expect(contenutoPer(p, i).tipo).toBe('ParolaSegreta');
  });

  const svela = (m: Modalita, imp: number[]) => testoSvelamento(partita(m, imp));

  it('CA-21 singolare con un impostore', () => {
    const t = svela('SENZA_PAROLA', [1]);
    expect(t).toContain("L'impostore era: Bruno");
    expect(t).not.toContain('Gli impostori erano');
  });

  it('CA-21 plurale con piu impostori nell ordine dei giocatori', () => {
    const t = svela('SENZA_PAROLA', [2, 0]);
    expect(t).toContain('Gli impostori erano: Anna, Carla');
    expect(t).not.toContain("L'impostore era");
  });

  it('CA-21 contiene parola e categoria', () => {
    const t = svela('SENZA_PAROLA', [1]);
    expect(t).toContain('La parola era: Cane');
    expect(t).toContain('Categoria: Animali');
  });

  it('CA-21 affine solo in modalita affine', () => {
    expect(svela('SENZA_PAROLA', [1])).not.toContain('Lupo');
    expect(svela('PAROLA_AFFINE', [1])).toContain('La parola affine era: Lupo');
  });
});
