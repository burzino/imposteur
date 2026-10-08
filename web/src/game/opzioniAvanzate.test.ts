import { describe, it, expect } from 'vitest';
import { GestorePartite, contenutoPer, eTrappola, ordineDiParola, testoSvelamento } from './partita';
import type { Partita } from './partita';
import type { Configurazione, Modalita } from './modelli';
import { Regole } from './regole';
import { casualeConSeme } from './casuale';
import { configurazioneAStringa, configurazioneDaStringa } from '../data/serializzazioneConfigurazione';
import { sessioneAStringa, sessioneDaStringa } from '../data/serializzazioneSessione';
import type { SessioneSalvata } from './partita';
import { categorieDemo, configBase, voceCane } from './fixture';

// Opzioni avanzate (OPZ-xx, contratto v1.6). Casualita con seme: si verificano proprieta, non sequenze.
const cats = categorieDemo();
const seeds = 500;
const range = (n: number) => Array.from({ length: n }, (_, i) => i);

function cfg(extra: Partial<Configurazione> & { n?: number; k?: number; mod?: Modalita } = {}): Configurazione {
  const { n, k, mod, ...resto } = extra;
  return configBase({ n: n ?? 7, k: k ?? 2, mod: mod ?? 'SENZA_PAROLA' }, resto);
}

function gioca(c: Configurazione, seed: number): Partita {
  const r = new GestorePartite(casualeConSeme(seed)).nuovaPartita(c, cats);
  if (r.tipo !== 'Ok') throw new Error(`seed=${seed}: ${JSON.stringify(r)}`);
  return r.partita;
}
const rotazione = (p: Partita) => range(p.giocatori.length).map((i) => (p.primoGiocatore + i) % p.giocatori.length);

describe('Opzioni avanzate', () => {
  it('OPZ-01 default: K uguale a numeroImpostori, ordine a giro, mai trappola su 500 seed', () => {
    for (let seed = 0; seed < seeds; seed++) {
      const p = gioca(cfg(), seed);
      expect(p.impostori.size, `seed=${seed}`).toBe(2);
      expect(ordineDiParola(p), `seed=${seed}`).toEqual(rotazione(p));
      expect(eTrappola(p), `seed=${seed}`).toBe(false);
      expect(p.giriIndizi).toBe(1);
      expect(p.promemoriaUltimaPossibilita).toBe(false);
    }
  });

  it('OPZ-02 impostoreNonPrimo: il primo non e mai impostore e varia tra i civili', () => {
    const primi = new Set<number>();
    for (let seed = 0; seed < seeds; seed++) {
      const p = gioca(cfg({ impostoreNonPrimo: true }), seed);
      expect(p.impostori.size).toBe(2);
      expect(p.impostori.has(p.primoGiocatore), `seed=${seed}`).toBe(false);
      primi.add(p.primoGiocatore);
    }
    expect(primi.size).toBeGreaterThan(1);
  });

  it('OPZ-02 impostoreNonPrimo con K da 1 a massimo su piu configurazioni', () => {
    for (let n = 3; n <= 9; n++) for (let k = 1; k <= Regole.maxImpostori(n); k++) for (let seed = 0; seed < 60; seed++) {
      const p = gioca(cfg({ n, k, impostoreNonPrimo: true }), seed);
      expect(p.impostori.has(p.primoGiocatore), `n=${n} k=${k} seed=${seed}`).toBe(false);
    }
  });

  it('OPZ-03 impostoriSorpresa: K tra 1 e max e compaiono tutti i valori', () => {
    const max = 3;
    const visti = new Set<number>();
    for (let seed = 0; seed < seeds; seed++) {
      const p = gioca(cfg({ n: 9, k: max, impostoriSorpresa: true }), seed);
      expect(p.impostori.size).toBeGreaterThanOrEqual(1);
      expect(p.impostori.size).toBeLessThanOrEqual(max);
      visti.add(p.impostori.size);
    }
    expect(visti).toEqual(new Set([1, 2, 3]));
  });

  it('OPZ-03 impostoriSorpresa con max 1 da sempre K 1', () => {
    for (let seed = 0; seed < seeds; seed++) {
      expect(gioca(cfg({ n: 5, k: 1, impostoriSorpresa: true }), seed).impostori.size).toBe(1);
    }
  });

  it('OPZ-04 ordineCasuale: permutazione che inizia con il primo e a volte non e la rotazione', () => {
    let diverso = false;
    for (let seed = 0; seed < seeds; seed++) {
      const p = gioca(cfg({ ordineCasuale: true }), seed);
      const o = ordineDiParola(p);
      expect(new Set(o), `seed=${seed}`).toEqual(new Set(range(7)));
      expect(o).toHaveLength(7);
      expect(o[0], `seed=${seed}`).toBe(p.primoGiocatore);
      if (JSON.stringify(o) !== JSON.stringify(rotazione(p))) diverso = true;
    }
    expect(diverso).toBe(true);
  });

  it('OPZ-05 trappola: frequenza di K 0 tra 5 e 15 percento su 2000 seed', () => {
    let zero = 0;
    const tot = 2000;
    for (let seed = 0; seed < tot; seed++) {
      const p = gioca(cfg({ partitaTrappola: true }), seed);
      if (p.impostori.size === 0) zero++;
      else expect(p.impostori.size).toBe(2);
    }
    const f = zero / tot;
    expect(f).toBeGreaterThanOrEqual(0.05);
    expect(f).toBeLessThanOrEqual(0.15);
  });

  function trappolaDi(mod: Modalita): Partita {
    for (let seed = 0; seed < 5000; seed++) {
      const p = gioca(cfg({ mod, partitaTrappola: true }), seed);
      if (p.impostori.size === 0) return p;
    }
    throw new Error('nessuna partita trappola trovata');
  }

  it('OPZ-05 con K 0 tutti ricevono la parola in entrambe le modalita e eTrappola e true', () => {
    for (const mod of ['SENZA_PAROLA', 'PAROLA_AFFINE'] as const) {
      const p = trappolaDi(mod);
      expect(eTrappola(p)).toBe(true);
      expect(p.impostori.size).toBe(0);
      for (let i = 0; i < p.giocatori.length; i++) {
        const c = contenutoPer(p, i);
        expect(c.tipo, `mod=${mod} i=${i}`).toBe('ParolaSegreta');
        if (c.tipo === 'ParolaSegreta') expect(c.testo).toBe(p.voce.parola);
      }
    }
  });

  it('OPZ-05 lo svelamento della trappola contiene il testo della trappola e la parola', () => {
    for (const mod of ['SENZA_PAROLA', 'PAROLA_AFFINE'] as const) {
      const p = trappolaDi(mod);
      const t = testoSvelamento(p);
      expect(t).toContain('Nessun impostore: era una partita trappola!');
      expect(t).toContain(p.voce.parola);
      if (mod === 'PAROLA_AFFINE' && p.voce.affine !== null) expect(t).toContain(p.voce.affine);
      expect(t).toContain(p.voce.categoriaNome);
    }
  });

  it('OPZ-05 senza trappola lo svelamento non contiene il testo della trappola', () => {
    expect(testoSvelamento(gioca(cfg(), 1))).not.toContain('trappola');
  });

  it('OPZ-06 trappola piu impostoreNonPrimo con K 0 non va in errore', () => {
    let zero = 0;
    for (let seed = 0; seed < 2000; seed++) {
      const p = gioca(cfg({ partitaTrappola: true, impostoreNonPrimo: true, ordineCasuale: true, impostoriSorpresa: true }), seed);
      expect(p.primoGiocatore).toBeGreaterThanOrEqual(0);
      expect(p.primoGiocatore).toBeLessThan(p.giocatori.length);
      if (p.impostori.size === 0) zero++;
      else expect(p.impostori.has(p.primoGiocatore)).toBe(false);
    }
    expect(zero).toBeGreaterThan(0);
  });

  it('OPZ-07 promemoria e giri vengono copiati nella partita', () => {
    const p = gioca(cfg({ promemoriaUltimaPossibilita: true, giriIndizi: 3 }), 7);
    expect(p.promemoriaUltimaPossibilita).toBe(true);
    expect(p.giriIndizi).toBe(3);
    const q = gioca(cfg({ promemoriaUltimaPossibilita: false, giriIndizi: 2 }), 7);
    expect(q.promemoriaUltimaPossibilita).toBe(false);
    expect(q.giriIndizi).toBe(2);
  });

  it('OPZ-07 giri 0 o 5 vengono limitati a 1..3 dalla serializzazione', () => {
    const giri = (g: number) => configurazioneDaStringa(configurazioneAStringa(cfg({ giriIndizi: g })), cats).giriIndizi;
    expect(giri(0)).toBe(1);
    expect(giri(5)).toBe(3);
    expect(giri(-2)).toBe(1);
    for (let g = 1; g <= 3; g++) expect(giri(g)).toBe(g);
  });

  it('OPZ-08 round-trip configurazione con tutte le opzioni', () => {
    const c = cfg({
      n: 8, k: 3, mod: 'PAROLA_AFFINE', impostoreNonPrimo: true, impostoriSorpresa: true,
      ordineCasuale: true, partitaTrappola: true, promemoriaUltimaPossibilita: true, giriIndizi: 2,
    });
    expect(configurazioneDaStringa(configurazioneAStringa(c), cats)).toEqual(c);
  });

  it('OPZ-08 configurazione senza i campi nuovi usa i default', () => {
    const vecchio = '{"numeroGiocatori":6,"nomi":[],"numeroImpostori":2,"modalita":"SENZA_PAROLA","mostraCategoria":true,"categorieSelezionate":["animali"]}';
    const c = configurazioneDaStringa(vecchio, cats);
    expect(c.numeroGiocatori).toBe(6);
    expect(c.numeroImpostori).toBe(2);
    expect(c.impostoreNonPrimo).toBe(false);
    expect(c.impostoriSorpresa).toBe(false);
    expect(c.ordineCasuale).toBe(false);
    expect(c.partitaTrappola).toBe(false);
    expect(c.promemoriaUltimaPossibilita).toBe(false);
    expect(c.giriIndizi).toBe(1);
  });

  // ---- Serializzazione sessione ----
  const partitaS = (over: Partial<Partita> = {}): Partita => ({
    giocatori: ['Anna', 'Bruno', 'Carla', 'Dino', 'Eva'],
    impostori: new Set([1, 3]),
    voce: voceCane,
    modalita: 'SENZA_PAROLA',
    mostraCategoria: true,
    primoGiocatore: 2,
    ordine: null,
    giriIndizi: 1,
    promemoriaUltimaPossibilita: false,
    ...over,
  });
  const sess = (p: Partita): SessioneSalvata => ({ partita: p, stato: { tipo: 'Gioco' }, usate: [], ultima: null });
  const rt = (s: SessioneSalvata) => sessioneDaStringa(sessioneAStringa(s), cats);

  /** Riscrive il campo "ordine" della partita nel JSON (undefined = campo rimosso). */
  function conOrdine(s: SessioneSalvata, nuovo: unknown): SessioneSalvata {
    const obj = JSON.parse(sessioneAStringa(s));
    if (nuovo === undefined) delete obj.partita.ordine;
    else obj.partita.ordine = nuovo;
    return sessioneDaStringa(JSON.stringify(obj), cats);
  }

  it('OPZ-09 round-trip sessione con ordine casuale, giri e promemoria', () => {
    const s = sess(partitaS({ ordine: [2, 4, 0, 3, 1], giriIndizi: 3, promemoriaUltimaPossibilita: true }));
    expect(rt(s)).toEqual(s);
  });

  it('OPZ-09 round-trip sessione con trappola', () => {
    const s = sess(partitaS({ impostori: new Set(), ordine: [2, 0, 4, 1, 3] }));
    const r = rt(s);
    expect(r).toEqual(s);
    expect(eTrappola(r.partita!)).toBe(true);
  });

  it('OPZ-09 sessione con impostori vuoti e valida', () => {
    const r = rt(sess(partitaS({ impostori: new Set() })));
    expect(r.partita).not.toBeNull();
    expect(r.stato).toEqual({ tipo: 'Gioco' });
  });

  it('OPZ-09 sessione senza ordine da la rotazione', () => {
    const r = conOrdine(sess(partitaS({ ordine: [2, 4, 0, 3, 1] })), undefined);
    expect(r.partita).not.toBeNull();
    expect(ordineDiParola(r.partita!)).toEqual([2, 3, 4, 0, 1]);
  });

  it('OPZ-09 ordine non valido scarta la partita', () => {
    const s = sess(partitaS({ ordine: [2, 4, 0, 3, 1] }));
    const invalidi = [
      [0, 1, 2, 3, 4],       // non inizia con il primo (2)
      [2, 2, 0, 1, 3],       // duplicato
      [2, 0, 1, 3, 9],       // fuori range
      [2, 0, 1, 3, -1],      // negativo
      [2, 0, 1, 3],          // lunghezza errata
      [2, 0, 1, 3, 4, 1],    // troppo lungo
    ];
    for (const o of invalidi) {
      const r = conOrdine(s, o);
      expect(r.partita, `ordine=${o}`).toBeNull();
      expect(r.stato, `ordine=${o}`).toBeNull();
    }
  });
});
