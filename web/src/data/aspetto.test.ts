import { describe, it, expect } from 'vitest';
import { aspettoAStringa, aspettoDaStringa, aspettoDefault } from './aspetto';
import type { Aspetto } from './aspetto';

// Tema persistente (CA-W14).
describe('Aspetto', () => {
  it('CA-W14 default', () => {
    expect(aspettoDefault).toEqual({ tema: 'SISTEMA', coloriDinamici: true, durataPressioneMs: 150 });
  });

  it('CA-W14 round-trip per ogni tema', () => {
    for (const tema of ['SISTEMA', 'CHIARO', 'SCURO', 'ALTO_CONTRASTO'] as const) {
      for (const coloriDinamici of [true, false]) {
        const a: Aspetto = { tema, coloriDinamici, durataPressioneMs: 150 };
        expect(aspettoDaStringa(aspettoAStringa(a))).toEqual(a);
      }
    }
  });

  it('CA-W14 null malformata o valori sconosciuti danno il default per campo', () => {
    for (const s of [null, '', '{rotto', '[]']) expect(aspettoDaStringa(s)).toEqual(aspettoDefault);
    expect(aspettoDaStringa('{"tema":"ARCOBALENO","coloriDinamici":false}')).toEqual({ tema: 'SISTEMA', coloriDinamici: false, durataPressioneMs: 150 });
    expect(aspettoDaStringa('{"tema":"SCURO"}')).toEqual({ tema: 'SCURO', coloriDinamici: true, durataPressioneMs: 150 });
  });

  it('CA-W14 formato JSON {"tema","coloriDinamici"}', () => {
    expect(JSON.parse(aspettoAStringa({ tema: 'CHIARO', coloriDinamici: false, durataPressioneMs: 150 }))).toMatchObject({ tema: 'CHIARO', coloriDinamici: false });
  });

  // Durata della pressione (CA-W20, CA-110).
  it('CA-W20 CA-110 durataPressioneMs assente vale 150', () => {
    expect(aspettoDaStringa('{"tema":"SCURO","coloriDinamici":false}').durataPressioneMs).toBe(150);
  });

  it('CA-W20 CA-110 durataPressioneMs illeggibile vale 150 senza alterare tema e colori', () => {
    for (const v of ['"abc"', 'null', '{}', '[]', 'true']) {
      expect(aspettoDaStringa(`{"tema":"SCURO","coloriDinamici":false,"durataPressioneMs":${v}}`)).toEqual({
        tema: 'SCURO', coloriDinamici: false, durataPressioneMs: 150,
      });
    }
  });

  it('CA-W20 CA-110 durataPressioneMs fuori intervallo o non multiplo viene normalizzata', () => {
    const leggi = (v: number) =>
      aspettoDaStringa(`{"tema":"CHIARO","coloriDinamici":true,"durataPressioneMs":${v}}`);
    expect(leggi(-30)).toEqual({ tema: 'CHIARO', coloriDinamici: true, durataPressioneMs: 0 });
    expect(leggi(5000).durataPressioneMs).toBe(1000);
    expect(leggi(125).durataPressioneMs).toBe(150);
    expect(leggi(124).durataPressioneMs).toBe(100);
  });

  it('CA-W20 CA-110 durataPressioneMs valida viene conservata, 0 compreso', () => {
    for (const ms of [0, 50, 150, 200, 1000]) {
      const a: Aspetto = { tema: 'ALTO_CONTRASTO', coloriDinamici: false, durataPressioneMs: ms };
      expect(aspettoDaStringa(aspettoAStringa(a))).toEqual(a);
    }
  });

  it('CA-W20 CA-110 aspettoAStringa scrive la durata normalizzata', () => {
    const s = aspettoAStringa({ tema: 'SISTEMA', coloriDinamici: true, durataPressioneMs: 124 });
    expect(aspettoDaStringa(s).durataPressioneMs).toBe(100);
  });

  it('CA-W20 CA-110 JSON malformato o null dà durata 150', () => {
    for (const s of [null, '', '{rotto']) expect(aspettoDaStringa(s).durataPressioneMs).toBe(150);
  });
});
