import { describe, it, expect } from 'vitest';
import { sessioneAStringa, sessioneDaStringa } from './serializzazioneSessione';
import type { ChiaveParola, Partita, SessioneSalvata, StatoDistribuzione } from '../game/partita';
import { chiaveStringa } from '../game/partita';
import type { Modalita } from '../game/modelli';
import { categorieDemo } from '../game/fixture';

const cats = categorieDemo();
const vuota: SessioneSalvata = { partita: null, stato: null, usate: [], ultima: null };

interface OpzPartita {
  modalita?: Modalita; impostori?: number[]; categoria?: string; parola?: string;
  affine?: string | null; nome?: string; primo?: number; mostra?: boolean;
}
function partita(o: OpzPartita = {}): Partita {
  return {
    giocatori: ['Anna', 'Bruno', 'Carla', 'Dino', 'Eva'],
    impostori: new Set(o.impostori ?? [1, 3]),
    voce: {
      categoriaId: o.categoria ?? 'animali', categoriaNome: o.nome ?? 'Animali',
      parola: o.parola ?? 'Cane', affine: o.affine === undefined ? 'Lupo' : o.affine,
    },
    modalita: o.modalita ?? 'SENZA_PAROLA',
    mostraCategoria: o.mostra ?? true,
    primoGiocatore: o.primo ?? 2,
    ordine: null,
    giriIndizi: 1,
    promemoriaUltimaPossibilita: false,
  };
}
function sessione(
  p: Partita | null = partita(), st: StatoDistribuzione | null = { tipo: 'Gioco' },
  usate: ChiaveParola[] = [], ultima: ChiaveParola | null = null,
): SessioneSalvata {
  return { partita: p, stato: st, usate, ultima };
}
const rt = (s: SessioneSalvata) => sessioneDaStringa(sessioneAStringa(s), cats);
const k = (categoriaId: string, parola: string): ChiaveParola => ({ categoriaId, parola });
const ordinate = (v: readonly ChiaveParola[]) => v.map(chiaveStringa).sort();
/** Applica una modifica al JSON prodotto da sessioneAStringa e rilegge. */
function modifica(s: SessioneSalvata, f: (obj: any) => void): SessioneSalvata {
  const obj = JSON.parse(sessioneAStringa(s));
  f(obj);
  return sessioneDaStringa(JSON.stringify(obj), cats);
}

describe('Serializzazione sessione', () => {
  it('CA-37 round-trip Passaggio k per ogni k', () => {
    for (let i = 0; i < 5; i++) {
      const s = sessione(partita(), { tipo: 'Passaggio', indice: i });
      expect(rt(s)).toEqual(s);
    }
  });

  it('CA-37 round-trip Gioco', () => {
    const s = sessione(partita(), { tipo: 'Gioco' });
    expect(rt(s)).toEqual(s);
  });

  it('CA-37 e CA-39 Rivelazione k diventa Passaggio k', () => {
    for (let i = 0; i < 5; i++) {
      const s = sessione(partita(), { tipo: 'Rivelazione', indice: i });
      expect(rt(s)).toEqual({ ...s, stato: { tipo: 'Passaggio', indice: i } });
    }
  });

  it('CA-37 round-trip modalita parola affine e opzioni', () => {
    const s = sessione(
      partita({ modalita: 'PAROLA_AFFINE', impostori: [0], categoria: 'cibo', parola: 'Pizza', affine: 'Focaccia', nome: 'Cibo', primo: 4, mostra: false }),
      { tipo: 'Passaggio', indice: 3 },
    );
    expect(rt(s)).toEqual(s);
    const s2 = sessione(partita({ mostra: false, primo: 0 }), { tipo: 'Gioco' });
    expect(rt(s2)).toEqual(s2);
  });

  it('CA-37 round-trip affine null in modalita senza parola', () => {
    const s = sessione(partita({ affine: null }));
    expect(rt(s)).toEqual(s);
  });

  it('CA-39 Passaggio e Gioco restano invariati', () => {
    expect(rt(sessione(partita(), { tipo: 'Passaggio', indice: 2 })).stato).toEqual({ tipo: 'Passaggio', indice: 2 });
    expect(rt(sessione(partita(), { tipo: 'Gioco' })).stato).toEqual({ tipo: 'Gioco' });
  });

  it('CA-38 round-trip insieme usate vuoto', () => {
    expect(rt(vuota)).toEqual(vuota);
  });

  it('CA-38 round-trip insieme usate con ultima e senza partita', () => {
    const s = sessione(null, null, [k('animali', 'cane'), k('cibo', 'pizza')], k('cibo', 'pizza'));
    const r = rt(s);
    expect(r.partita).toBeNull();
    expect(r.stato).toBeNull();
    expect(ordinate(r.usate)).toEqual(ordinate(s.usate));
    expect(r.ultima).toEqual(s.ultima);
  });

  it('CA-38 round-trip usate insieme a partita', () => {
    const s = sessione(partita(), { tipo: 'Gioco' }, [k('animali', 'cane'), k('animali', 'gatto')], k('animali', 'cane'));
    const r = rt(s);
    expect(r.partita).toEqual(s.partita);
    expect(ordinate(r.usate)).toEqual(ordinate(s.usate));
    expect(r.ultima).toEqual(s.ultima);
  });

  it('CA-40 e CA-W03 null vuoto e JSON malformato danno sessione vuota senza eccezioni', () => {
    for (const x of [null, '', '   ', '{', 'non json', '[1,2', '{"a":', 'null', '[]', '42', '{}']) {
      expect(sessioneDaStringa(x, cats), `input=${x}`).toEqual(vuota);
    }
  });

  it('CA-40 e CA-W03 JSON troncato non lancia eccezioni', () => {
    const j = sessioneAStringa(sessione());
    for (let n = 0; n < j.length - 1; n += 3) {
      expect(sessioneDaStringa(j.substring(0, n), cats), `n=${n}`).toEqual(vuota);
    }
  });

  it('CA-40 e CA-W03 indici impostori fuori range scartano la partita', () => {
    for (const imp of [[5], [-1], [0, 99], [7, 1]]) {
      const r = rt(sessione(partita({ impostori: imp })));
      expect(r.partita, `imp=${imp}`).toBeNull();
      expect(r.stato).toBeNull();
    }
  });

  it('CA-40 primo giocatore fuori range scarta la partita', () => {
    expect(rt(sessione(partita({ primo: 5 }))).partita).toBeNull();
    expect(rt(sessione(partita({ primo: -1 }))).partita).toBeNull();
  });

  it('CA-40 indice k fuori intervallo scarta la partita', () => {
    const stati: StatoDistribuzione[] = [
      { tipo: 'Passaggio', indice: 5 }, { tipo: 'Passaggio', indice: -1 },
      { tipo: 'Rivelazione', indice: 5 }, { tipo: 'Rivelazione', indice: 99 },
    ];
    for (const st of stati) {
      const r = rt(sessione(partita(), st));
      expect(r.partita, JSON.stringify(st)).toBeNull();
      expect(r.stato, JSON.stringify(st)).toBeNull();
    }
  });

  it('CA-40 impostori duplicati nel JSON scartano la partita', () => {
    const r = modifica(sessione(partita({ impostori: [1, 3] })), (o) => { o.partita.impostori = [1, 1]; });
    expect(r.partita).toBeNull();
  });

  it('CA-40 campo mancante nella partita la scarta', () => {
    const facoltativi = new Set(['ordine', 'giriIndizi', 'promemoria']);
    const campi = Object.keys(JSON.parse(sessioneAStringa(sessione())).partita).filter((c) => !facoltativi.has(c));
    expect(campi.length).toBeGreaterThan(0);
    for (const campo of campi) {
      const r = modifica(sessione(), (o) => { delete o.partita[campo]; });
      expect(r.partita, `campo mancante '${campo}' dovrebbe scartare la partita`).toBeNull();
    }
  });

  it('CA-40 stato sconosciuto scarta la partita', () => {
    const r = modifica(sessione(partita(), { tipo: 'Passaggio', indice: 1 }), (o) => { o.stato.tipo = 'XYZ_SCONOSCIUTO'; });
    expect(r.partita).toBeNull();
  });

  it('CA-W02 formato stato nel JSON: tipo maiuscolo e indice', () => {
    const o = JSON.parse(sessioneAStringa(sessione(partita(), { tipo: 'Passaggio', indice: 2 })));
    expect(o.stato).toEqual({ tipo: 'PASSAGGIO', indice: 2 });
    expect(JSON.parse(sessioneAStringa(sessione())).stato.tipo).toBe('GIOCO');
  });

  it('CA-41 categoria non piu presente scarta partita e stato', () => {
    const r = rt(sessione(partita({ categoria: 'sparita', nome: 'Sparita' })));
    expect(r.partita).toBeNull();
    expect(r.stato).toBeNull();
  });

  it('CA-41 parola non piu presente scarta la partita', () => {
    const r = rt(sessione(partita({ parola: 'Ornitorinco' })));
    expect(r.partita).toBeNull();
    expect(r.stato).toBeNull();
  });

  it('CA-41 affine non piu presente in modalita affine scarta la partita', () => {
    expect(rt(sessione(partita({ modalita: 'PAROLA_AFFINE', affine: 'Inesistente' }))).partita).toBeNull();
  });

  it('CA-41 dati coerenti col file sono validi', () => {
    expect(rt(sessione(partita({ modalita: 'PAROLA_AFFINE' }))).partita).not.toBeNull();
    expect(rt(sessione()).partita).not.toBeNull();
  });

  it('CA-41 e CA-42 scarto partita conserva le usate filtrate sulle chiavi esistenti', () => {
    const u = [k('animali', 'cane'), k('cibo', 'pizza'), k('sparita', 'x')];
    const r = rt(sessione(partita({ parola: 'Ornitorinco' }), { tipo: 'Gioco' }, u));
    expect(r.partita).toBeNull();
    expect(ordinate(r.usate)).toEqual(ordinate([k('animali', 'cane'), k('cibo', 'pizza')]));
  });

  it('CA-42 usate non piu presenti nel file sono ignorate', () => {
    const u = [k('animali', 'cane'), k('animali', 'drago'), k('fantasy', 'elfo')];
    const r = rt(sessione(null, null, u, k('animali', 'cane')));
    expect(r.usate).toEqual([k('animali', 'cane')]);
  });

  it('CA-42 ultima non piu presente nel file e ignorata', () => {
    expect(rt(sessione(null, null, [], k('fantasy', 'elfo'))).ultima).toBeNull();
  });
});
