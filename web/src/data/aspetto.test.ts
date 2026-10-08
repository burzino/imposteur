import { describe, it, expect } from 'vitest';
import { aspettoAStringa, aspettoDaStringa, aspettoDefault } from './aspetto';
import type { Aspetto } from './aspetto';

// Tema persistente (CA-W14).
describe('Aspetto', () => {
  it('CA-W14 default', () => {
    expect(aspettoDefault).toEqual({ tema: 'SISTEMA', coloriDinamici: true });
  });

  it('CA-W14 round-trip per ogni tema', () => {
    for (const tema of ['SISTEMA', 'CHIARO', 'SCURO', 'ALTO_CONTRASTO'] as const) {
      for (const coloriDinamici of [true, false]) {
        const a: Aspetto = { tema, coloriDinamici };
        expect(aspettoDaStringa(aspettoAStringa(a))).toEqual(a);
      }
    }
  });

  it('CA-W14 null malformata o valori sconosciuti danno il default per campo', () => {
    for (const s of [null, '', '{rotto', '[]']) expect(aspettoDaStringa(s)).toEqual(aspettoDefault);
    expect(aspettoDaStringa('{"tema":"ARCOBALENO","coloriDinamici":false}')).toEqual({ tema: 'SISTEMA', coloriDinamici: false });
    expect(aspettoDaStringa('{"tema":"SCURO"}')).toEqual({ tema: 'SCURO', coloriDinamici: true });
  });

  it('CA-W14 formato JSON {"tema","coloriDinamici"}', () => {
    expect(JSON.parse(aspettoAStringa({ tema: 'CHIARO', coloriDinamici: false }))).toEqual({ tema: 'CHIARO', coloriDinamici: false });
  });
});
