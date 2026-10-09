import { describe, it, expect } from 'vitest';
import { Passi, passoDiPassoConfigurazione, type PassoConfigurazione } from './passi';
import { Regole } from './regole';
import { cat, categorieDemo, configBase } from './fixture';
import type { ErroreConfigurazione } from './modelli';

const cats = categorieDemo();
const catSenzaAffini = [cat('solo', ['Alfa', null], ['Beta', null])];
const tutte = [...cats, ...catSenzaAffini];

const TIPI_GIOCATORI: readonly string[] = [
  'TroppoPochiGiocatori',
  'TroppiGiocatori',
  'TroppoPochiImpostori',
  'TroppiImpostori',
  'NomeDuplicato',
  'NomeTroppoLungo',
];
const TIPI_CATEGORIE: readonly string[] = ['NessunaCategoria', 'PoolVuoto'];
const erroreGiocatori = (e: ErroreConfigurazione): boolean => TIPI_GIOCATORI.includes(e.tipo);
const erroreCategorie = (e: ErroreConfigurazione): boolean => TIPI_CATEGORIE.includes(e.tipo);

describe('Passi', () => {
  // ---- passoDiPassoConfigurazione ----

  it('CA-99 passoDiPassoConfigurazione: ordinale piu uno', () => {
    const attesi: [PassoConfigurazione, 1 | 2 | 3 | 4][] = [
      ['GIOCATORI', 1],
      ['OPZIONI', 2],
      ['CATEGORIE', 3],
      ['RIEPILOGO', 4],
    ];
    for (const [p, n] of attesi) expect(passoDiPassoConfigurazione(p), p).toBe(n);
  });

  // ---- errorePasso ----

  it('CA-25 CA-101 nome duplicato blocca GIOCATORI e non CATEGORIE', () => {
    const c = configBase({ n: 4, nomi: ['Anna', ' anna ', 'Bruno', 'Carla'] });
    expect(Passi.errorePasso('GIOCATORI', c, cats)?.tipo).toBe('NomeDuplicato');
    expect(Passi.errorePasso('CATEGORIE', c, cats)).toBeNull();
  });

  it('CA-25 nome duplicato anche con Giocatore n di default', () => {
    const c = configBase({ n: 4, nomi: ['Giocatore 2', '', 'Bruno', 'Carla'] });
    expect(Passi.errorePasso('GIOCATORI', c, cats)?.tipo).toBe('NomeDuplicato');
    expect(Passi.errorePasso('CATEGORIE', c, cats)).toBeNull();
  });

  it('CA-25 CA-101 categorie vuote danno NessunaCategoria in CATEGORIE e null in GIOCATORI', () => {
    const c = configBase({ cats: [] });
    expect(Passi.errorePasso('CATEGORIE', c, cats)).toEqual({ tipo: 'NessunaCategoria' });
    expect(Passi.errorePasso('GIOCATORI', c, cats)).toBeNull();
  });

  it('CA-101 selezione non vuota ma pool vuoto in PAROLA_AFFINE senza affini', () => {
    const c = configBase({ mod: 'PAROLA_AFFINE', cats: ['solo'] });
    expect(Passi.errorePasso('CATEGORIE', c, catSenzaAffini)).toEqual({ tipo: 'PoolVuoto' });
    expect(Passi.errorePasso('GIOCATORI', c, catSenzaAffini)).toBeNull();
  });

  it('CA-101 OPZIONI non blocca mai nemmeno con trappola e sorpresa', () => {
    const combinazioni = [
      configBase(),
      configBase({ cats: [] }),
      configBase({ n: 2, k: 0 }),
      configBase({}, { partitaTrappola: true, impostoriSorpresa: true }),
      configBase(
        { n: 5, k: 2 },
        {
          partitaTrappola: true,
          impostoriSorpresa: true,
          impostoreNonPrimo: true,
          ordineCasuale: true,
          promemoriaUltimaPossibilita: true,
          giriIndizi: 3,
        },
      ),
    ];
    for (const c of combinazioni) expect(Passi.errorePasso('OPZIONI', c, cats)).toBeNull();
  });

  it('CA-101 RIEPILOGO e sempre null', () => {
    const c = configBase({ n: 2, k: 0, cats: [], nomi: ['A', 'a'] });
    expect(Passi.errorePasso('RIEPILOGO', c, cats)).toBeNull();
    expect(Passi.errorePasso('RIEPILOGO', configBase(), cats)).toBeNull();
  });

  it('CA-25 piu errori nello stesso passo restituisce il primo di Regole.valida', () => {
    const c = configBase({ n: 2, k: 0, nomi: ['A', 'a'] });
    const attesoG = Regole.valida(c, cats).find(erroreGiocatori);
    expect(attesoG).toBeDefined();
    expect(Passi.errorePasso('GIOCATORI', c, cats)).toEqual(attesoG);

    const c2 = configBase({ cats: [] });
    const attesoC = Regole.valida(c2, cats).find(erroreCategorie);
    expect(attesoC).toBeDefined();
    expect(Passi.errorePasso('CATEGORIE', c2, cats)).toEqual(attesoC);
  });

  it('CA-25 tipi non del passo non compaiono mai', () => {
    const configs = [
      configBase({ n: 2, k: 0, cats: [] }),
      configBase({ n: 21, k: 1, cats: [], nomi: ['A', 'a'] }),
      configBase({ mod: 'PAROLA_AFFINE', cats: ['solo'], nomi: ['A', 'a'] }),
    ];
    for (const c of configs) {
      const g = Passi.errorePasso('GIOCATORI', c, tutte);
      if (g !== null) expect(erroreGiocatori(g), `GIOCATORI: ${g.tipo}`).toBe(true);
      const k = Passi.errorePasso('CATEGORIE', c, tutte);
      if (k !== null) expect(erroreCategorie(k), `CATEGORIE: ${k.tipo}`).toBe(true);
    }
    const e = Passi.errorePasso('GIOCATORI', configBase({ n: 2, cats: [] }), cats);
    expect(e).toEqual({ tipo: 'TroppoPochiGiocatori' });
  });

  // ---- primoPassoNonValido ----

  it('CA-102 CA-101 nome duplicato e categorie vuote danno GIOCATORI', () => {
    const c = configBase({ n: 4, cats: [], nomi: ['Anna', 'anna', 'Bruno', 'Carla'] });
    expect(Passi.primoPassoNonValido(c, cats)).toBe('GIOCATORI');
  });

  it('CA-102 solo categorie vuote danno CATEGORIE', () => {
    expect(Passi.primoPassoNonValido(configBase({ cats: [] }), cats)).toBe('CATEGORIE');
  });

  it('CA-25 configurazione valida danno null come Regole.valida vuota', () => {
    const c = configBase();
    expect(Regole.valida(c, cats)).toEqual([]);
    expect(Passi.primoPassoNonValido(c, cats)).toBeNull();
  });

  it('CA-102 non restituisce mai OPZIONI ne RIEPILOGO', () => {
    const configs = [
      configBase(),
      configBase({ n: 2 }),
      configBase({ cats: [] }),
      configBase({ n: 4, nomi: ['A', 'a'] }),
      configBase({ mod: 'PAROLA_AFFINE', cats: ['solo'] }),
      configBase({}, { partitaTrappola: true, impostoriSorpresa: true }),
    ];
    for (const c of configs) {
      const p = Passi.primoPassoNonValido(c, tutte);
      expect([null, 'GIOCATORI', 'CATEGORIE']).toContain(p);
    }
  });

  // ---- riepilogo ----

  it('CA-104 sorpresa attiva con massimo 1 non da finoA', () => {
    for (const n of [3, 4]) {
      const r = Passi.riepilogo(configBase({ n, k: 1 }, { impostoriSorpresa: true }), cats);
      expect(r.finoA, `N=${n}`).toBe(false);
      expect(r.numeroImpostori).toBe(1);
    }
  });

  it('CA-92 sorpresa attiva con 5 giocatori e 2 impostori da finoA', () => {
    const r = Passi.riepilogo(configBase({ n: 5, k: 2 }, { impostoriSorpresa: true }), cats);
    expect(r.finoA).toBe(true);
    expect(r.numeroImpostori).toBe(2);
    expect(r.numeroGiocatori).toBe(5);
  });

  it('CA-92 senza sorpresa finoA e falso e il numero e quello configurato', () => {
    const r = Passi.riepilogo(configBase({ n: 5, k: 2 }), cats);
    expect(r.finoA).toBe(false);
    expect(r.numeroImpostori).toBe(2);
    expect(Passi.riepilogo(configBase({ n: 5, k: 1 }, { impostoriSorpresa: true }), cats).finoA).toBe(false);
  });

  it('CA-92 numero impostori limitato a 1 e al massimo', () => {
    expect(Passi.riepilogo(configBase({ n: 5, k: 9 }), cats).numeroImpostori).toBe(2);
    expect(Passi.riepilogo(configBase({ n: 5, k: 0 }), cats).numeroImpostori).toBe(1);
  });

  it('CA-103 nomi vuoti diventano Giocatore n', () => {
    const c = configBase({ n: 4, nomi: ['', '  Luca '] });
    const r = Passi.riepilogo(c, cats);
    expect(r.nomi).toEqual(['Giocatore 1', 'Luca', 'Giocatore 3', 'Giocatore 4']);
    expect(r.nomi).toEqual(Regole.nomiEffettivi(c));
  });

  it('CA-93 PAROLA_AFFINE con mostraCategoria falso non ha SENZA_CATEGORIA', () => {
    const r = Passi.riepilogo(configBase({ mod: 'PAROLA_AFFINE', mostra: false }), cats);
    expect(r.opzioni).not.toContain('SENZA_CATEGORIA');
    expect(r.modalita).toBe('PAROLA_AFFINE');
  });

  it('CA-93 SENZA_PAROLA con mostraCategoria falso ha SENZA_CATEGORIA', () => {
    const r = Passi.riepilogo(configBase({ mod: 'SENZA_PAROLA', mostra: false }), cats);
    expect(r.opzioni).toEqual(['SENZA_CATEGORIA']);
  });

  it('CA-89 tutte le opzioni spente danno opzioni vuota e giriIndizi 1', () => {
    const r = Passi.riepilogo(configBase(), cats);
    expect(r.opzioni).toEqual([]);
    expect(r.giriIndizi).toBe(1);
  });

  it('CA-93 ordine casuale e 2 giri danno ORDINE_CASUALE e GIRI', () => {
    const r = Passi.riepilogo(configBase({}, { ordineCasuale: true, giriIndizi: 2 }), cats);
    expect(r.opzioni).toEqual(['ORDINE_CASUALE', 'GIRI']);
    expect(r.giriIndizi).toBe(2);
  });

  it('CA-93 tutte le opzioni attive nell ordine di 4.2.1 senza duplicati', () => {
    const c = configBase(
      { mostra: false },
      {
        impostoreNonPrimo: true,
        partitaTrappola: true,
        ordineCasuale: true,
        promemoriaUltimaPossibilita: true,
        giriIndizi: 3,
        impostoriSorpresa: true,
      },
    );
    const r = Passi.riepilogo(c, cats);
    expect(r.opzioni).toEqual([
      'NON_PARLA_PER_PRIMO',
      'TRAPPOLA',
      'ORDINE_CASUALE',
      'PROMEMORIA',
      'SENZA_CATEGORIA',
      'GIRI',
    ]);
    expect(new Set(r.opzioni).size).toBe(r.opzioni.length);
    expect(r.giriIndizi).toBe(3);
  });

  it('CA-93 numero di impostori e sorpresa non entrano in opzioni', () => {
    const r = Passi.riepilogo(configBase({ n: 6, k: 2 }, { impostoriSorpresa: true }), cats);
    expect(r.opzioni).toEqual([]);
  });

  it('CA-93 ogni interruttore da solo produce la sua opzione', () => {
    expect(Passi.riepilogo(configBase({}, { impostoreNonPrimo: true }), cats).opzioni).toEqual(['NON_PARLA_PER_PRIMO']);
    expect(Passi.riepilogo(configBase({}, { partitaTrappola: true }), cats).opzioni).toEqual(['TRAPPOLA']);
    expect(Passi.riepilogo(configBase({}, { promemoriaUltimaPossibilita: true }), cats).opzioni).toEqual(['PROMEMORIA']);
  });

  it('CA-103 numeroCategorie conta solo gli id esistenti', () => {
    const c = configBase({ cats: ['animali', 'cibo', 'inesistente', 'fantasma'] });
    expect(Passi.riepilogo(c, cats).numeroCategorie).toBe(2);
    expect(Passi.riepilogo(configBase({ cats: ['zzz'] }), cats).numeroCategorie).toBe(0);
    expect(Passi.riepilogo(configBase({ cats: [] }), cats).numeroCategorie).toBe(0);
  });

  it('CA-94 il riepilogo riflette sempre la config corrente', () => {
    const a = configBase({ n: 4 });
    const b = configBase({ n: 6 }, { ordineCasuale: true });
    expect(Passi.riepilogo(a, cats).numeroGiocatori).toBe(4);
    expect(Passi.riepilogo(b, cats).numeroGiocatori).toBe(6);
    expect(Passi.riepilogo(a, cats).numeroGiocatori).toBe(4);
    expect(Passi.riepilogo(a, cats).opzioni).toEqual([]);
    expect(Passi.riepilogo(b, cats)).toEqual(Passi.riepilogo({ ...b }, cats));
  });

  it('CA-94 giriIndizi fuori range e limitato a 1-3', () => {
    expect(Passi.riepilogo(configBase({}, { giriIndizi: 0 }), cats).giriIndizi).toBe(1);
    expect(Passi.riepilogo(configBase({}, { giriIndizi: 9 }), cats).giriIndizi).toBe(3);
  });

  // ---- contaOpzioniAttive ----

  it('CA-89 tutte spente danno 0', () => {
    expect(Passi.contaOpzioniAttive(configBase())).toBe(0);
  });

  it('CA-81 solo mostraCategoria falso non e contato', () => {
    expect(Passi.contaOpzioniAttive(configBase({ mostra: false }))).toBe(0);
    expect(Passi.contaOpzioniAttive(configBase({ mod: 'SENZA_PAROLA', mostra: false }))).toBe(0);
  });

  it('CA-94 solo impostoriSorpresa non e contato', () => {
    expect(Passi.contaOpzioniAttive(configBase({}, { impostoriSorpresa: true }))).toBe(0);
    expect(Passi.contaOpzioniAttive(configBase({ n: 6, k: 3 }))).toBe(0);
  });

  it('CA-81 giriIndizi 2 o 3 contano 1', () => {
    expect(Passi.contaOpzioniAttive(configBase({}, { giriIndizi: 2 }))).toBe(1);
    expect(Passi.contaOpzioniAttive(configBase({}, { giriIndizi: 3 }))).toBe(1);
    expect(Passi.contaOpzioniAttive(configBase({}, { giriIndizi: 1 }))).toBe(0);
  });

  it('CA-81 tutte le cinque contate attive danno 5', () => {
    const extra = {
      impostoreNonPrimo: true,
      partitaTrappola: true,
      ordineCasuale: true,
      promemoriaUltimaPossibilita: true,
      giriIndizi: 2,
    };
    expect(Passi.contaOpzioniAttive(configBase({}, extra))).toBe(5);
    expect(Passi.contaOpzioniAttive(configBase({ mostra: false }, { ...extra, impostoriSorpresa: true }))).toBe(5);
  });

  it('CA-81 ogni interruttore conta 1', () => {
    expect(Passi.contaOpzioniAttive(configBase({}, { impostoreNonPrimo: true }))).toBe(1);
    expect(Passi.contaOpzioniAttive(configBase({}, { partitaTrappola: true }))).toBe(1);
    expect(Passi.contaOpzioniAttive(configBase({}, { ordineCasuale: true }))).toBe(1);
    expect(Passi.contaOpzioniAttive(configBase({}, { promemoriaUltimaPossibilita: true }))).toBe(1);
  });
});
