import { describe, it, expect } from 'vitest';
import { Distribuzione } from './partita';
import type { StatoDistribuzione } from './partita';

describe('Distribuzione', () => {
  it('CA-18 stato iniziale e Passaggio 0', () => {
    expect(Distribuzione.iniziale()).toEqual({ tipo: 'Passaggio', indice: 0 });
  });

  it('CA-18 percorso completo con N giocatori', () => {
    for (const n of [3, 4, 20]) {
      let s = Distribuzione.iniziale();
      for (let k = 0; k < n; k++) {
        expect(s).toEqual({ tipo: 'Passaggio', indice: k });
        s = Distribuzione.avanza(s, n);
        expect(s).toEqual({ tipo: 'Rivelazione', indice: k });
        s = Distribuzione.avanza(s, n);
      }
      expect(s).toEqual({ tipo: 'Gioco' });
    }
  });

  it('CA-18 Gioco resta Gioco', () => {
    expect(Distribuzione.avanza({ tipo: 'Gioco' }, 4)).toEqual({ tipo: 'Gioco' });
  });

  it('CA-18 indici mai decrescenti e nessuna doppia rivelazione', () => {
    let s: StatoDistribuzione = Distribuzione.iniziale();
    const rivelati: number[] = [];
    let ultimo = -1;
    for (let r = 0; r < 20; r++) {
      const cur: StatoDistribuzione = s;
      const i = cur.tipo === 'Gioco' ? ultimo : cur.indice;
      expect(i).toBeGreaterThanOrEqual(ultimo);
      ultimo = i;
      if (cur.tipo === 'Rivelazione') rivelati.push(cur.indice);
      s = Distribuzione.avanza(cur, 5);
    }
    expect(rivelati).toEqual([0, 1, 2, 3, 4]);
  });

  it('CA-18 interrompi riporta a Passaggio dello stesso giocatore', () => {
    expect(Distribuzione.interrompiRivelazione({ tipo: 'Rivelazione', indice: 2 })).toEqual({ tipo: 'Passaggio', indice: 2 });
  });

  it('CA-18 interrompi lascia invariati gli altri stati', () => {
    expect(Distribuzione.interrompiRivelazione({ tipo: 'Passaggio', indice: 1 })).toEqual({ tipo: 'Passaggio', indice: 1 });
    expect(Distribuzione.interrompiRivelazione({ tipo: 'Gioco' })).toEqual({ tipo: 'Gioco' });
  });

  it('CA-18 interrompi poi avanza non salta il giocatore', () => {
    const s = Distribuzione.interrompiRivelazione({ tipo: 'Rivelazione', indice: 1 });
    expect(Distribuzione.avanza(s, 4)).toEqual({ tipo: 'Rivelazione', indice: 1 });
  });
});
