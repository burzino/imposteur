import { describe, it, expect } from 'vitest';
import {
  DURATA_PRESSIONE,
  durataPressioneDaTesto,
  normalizzaDurataPressione,
  soloTocco,
} from './durataPressione';

// Porting di DurataPressioneTest.kt (CA-106, CA-107, CA-108, CA-W20).
describe('DurataPressione', () => {
  it('CA-106 costanti', () => {
    expect(DURATA_PRESSIONE).toEqual({ min: 0, max: 1000, passo: 50, predefinita: 150 });
  });

  it('CA-106 normalizza casi limite del contratto', () => {
    const attesi: Array<[number, number]> = [
      [0, 0], [1000, 1000], [150, 150], [125, 150], [124, 100],
      [25, 50], [24, 0], [-30, 0], [1049, 1000], [5000, 1000],
      [Number.MIN_SAFE_INTEGER, 0], [Number.MAX_SAFE_INTEGER, 1000],
      [2147483647, 1000], [-2147483648, 0],
    ];
    for (const [ingresso, atteso] of attesi) {
      expect(normalizzaDurataPressione(ingresso), `normalizza(${ingresso})`).toBe(atteso);
    }
  });

  it('CA-106 normalizza negativi e sopra 1000', () => {
    expect(normalizzaDurataPressione(-1)).toBe(0);
    expect(normalizzaDurataPressione(-5000)).toBe(0);
    expect(normalizzaDurataPressione(1001)).toBe(1000);
    expect(normalizzaDurataPressione(100000)).toBe(1000);
  });

  it('CA-106 normalizza decimali', () => {
    expect(normalizzaDurataPressione(149.6)).toBe(150);
    expect(normalizzaDurataPressione(124.9)).toBe(100);
  });

  it('CA-106 normalizza il risultato e sempre multiplo di 50 in 0..1000 e idempotente', () => {
    for (let v = -200; v <= 1300; v++) {
      const r = normalizzaDurataPressione(v);
      expect(r >= 0 && r <= 1000, `fuori intervallo ${v} -> ${r}`).toBe(true);
      expect(r % 50, `non multiplo di 50: ${v} -> ${r}`).toBe(0);
      expect(normalizzaDurataPressione(r)).toBe(r);
    }
  });

  it('CA-106 normalizza valori gia validi restano invariati', () => {
    for (let v = 0; v <= 1000; v += 50) expect(normalizzaDurataPressione(v)).toBe(v);
  });

  it('CA-106 normalizza NaN e infiniti danno 150', () => {
    expect(normalizzaDurataPressione(NaN)).toBe(150);
    expect(normalizzaDurataPressione(Infinity)).toBe(150);
    expect(normalizzaDurataPressione(-Infinity)).toBe(150);
  });

  it('CA-106 normalizza tipi non numerici danno 150', () => {
    for (const v of ['200', '', 'abc', null, undefined, true, false, {}, [], [200], { valueOf: () => 200 }, () => 200]) {
      expect(normalizzaDurataPressione(v), String(typeof v)).toBe(150);
    }
  });

  it('CA-106 daTesto null vuoto e non numerico danno 150', () => {
    expect(durataPressioneDaTesto(null)).toBe(150);
    expect(durataPressioneDaTesto('')).toBe(150);
    expect(durataPressioneDaTesto('   ')).toBe(150);
    expect(durataPressioneDaTesto('abc')).toBe(150);
    expect(durataPressioneDaTesto('12abc')).toBe(150);
  });

  it('CA-106 daTesto NaN e Infinity danno 150', () => {
    expect(durataPressioneDaTesto('NaN')).toBe(150);
    expect(durataPressioneDaTesto('Infinity')).toBe(150);
    expect(durataPressioneDaTesto('-Infinity')).toBe(150);
  });

  it('CA-106 daTesto numeri validi', () => {
    expect(durataPressioneDaTesto('200')).toBe(200);
    expect(durataPressioneDaTesto('0')).toBe(0);
    expect(durataPressioneDaTesto('1000')).toBe(1000);
    expect(durataPressioneDaTesto('125')).toBe(150);
    expect(durataPressioneDaTesto('124')).toBe(100);
  });

  it('CA-106 daTesto spazi ai lati ignorati', () => {
    expect(durataPressioneDaTesto(' 200 ')).toBe(200);
    expect(durataPressioneDaTesto('\t200\n')).toBe(200);
  });

  it('CA-106 daTesto decimali ed esponenziale', () => {
    expect(durataPressioneDaTesto('149.6')).toBe(150);
    expect(durataPressioneDaTesto('1e9')).toBe(1000);
  });

  it('CA-106 daTesto negativi e fuori intervallo vengono normalizzati', () => {
    expect(durataPressioneDaTesto('-30')).toBe(0);
    expect(durataPressioneDaTesto('5000')).toBe(1000);
  });

  it('CA-106 daTesto non lancia su testi anomali', () => {
    for (const s of ['--5', '1.2.3', ',', '9'.repeat(400), '\u0000']) {
      const r = durataPressioneDaTesto(s);
      expect(r >= 0 && r <= 1000).toBe(true);
      expect(r % 50).toBe(0);
    }
  });

  it('CA-107 soloTocco vero solo a zero', () => {
    expect(soloTocco(0)).toBe(true);
    expect(soloTocco(50)).toBe(false);
    expect(soloTocco(150)).toBe(false);
    expect(soloTocco(1000)).toBe(false);
  });

  it('CA-108 la durata predefinita al primo avvio non e solo tocco', () => {
    expect(soloTocco(DURATA_PRESSIONE.predefinita)).toBe(false);
  });
});
