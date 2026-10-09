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
  'NomeDuplicato',
  'NomeTroppoLungo',
];
const TIPI_MODALITA: readonly string[] = ['TroppoPochiImpostori', 'TroppiImpostori'];
const TIPI_CATEGORIE: readonly string[] = ['NessunaCategoria', 'PoolVuoto'];
const erroreGiocatori = (e: ErroreConfigurazione): boolean => TIPI_GIOCATORI.includes(e.tipo);
const erroreCategorie = (e: ErroreConfigurazione): boolean => TIPI_CATEGORIE.includes(e.tipo);

describe('Passi', () => {
  // ---- passoDiPassoConfigurazione ----

  it('CA-99 passoDiPassoConfigurazione: ordinale piu uno', () => {
    const attesi: [PassoConfigurazione, 1 | 2 | 3 | 4 | 5][] = [
      ['GIOCATORI', 1],
      ['MODALITA', 2],
      ['OPZIONI', 3],
      ['CATEGORIE', 4],
      ['RIEPILOGO', 5],
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

  it('CA-101 impostori oltre il massimo danno MODALITA e GIOCATORI null', () => {
    const c = configBase({ n: 5, k: 3 });
    expect(Passi.errorePasso('MODALITA', c, cats)).toEqual({ tipo: 'TroppiImpostori' });
    expect(Passi.errorePasso('GIOCATORI', c, cats)).toBeNull();
    expect(Passi.errorePasso('CATEGORIE', c, cats)).toBeNull();
  });

  it('CA-101 zero impostori danno TroppoPochiImpostori in MODALITA', () => {
    const c = configBase({ n: 5, k: 0 });
    expect(Passi.errorePasso('MODALITA', c, cats)).toEqual({ tipo: 'TroppoPochiImpostori' });
    expect(Passi.errorePasso('GIOCATORI', c, cats)).toBeNull();
  });

  it('CA-101 MODALITA e null con impostori coerenti anche con nomi duplicati e categorie vuote', () => {
    expect(Passi.errorePasso('MODALITA', configBase(), cats)).toBeNull();
    expect(Passi.errorePasso('MODALITA', configBase({ n: 4, nomi: ['A', 'a'], cats: [] }), cats)).toBeNull();
  });

  it('CA-25 MODALITA restituisce solo errori sugli impostori', () => {
    const configs = [
      configBase({ n: 2, k: 0, cats: [] }),
      configBase({ n: 21, k: 1 }),
      configBase({ n: 5, k: 9 }),
      configBase({ n: 4, nomi: ['A', 'a'] }),
      configBase({ n: 5, k: 0 }),
    ];
    for (const c of configs) {
      const m = Passi.errorePasso('MODALITA', c, cats);
      if (m !== null) expect(TIPI_MODALITA, `MODALITA: ${m.tipo}`).toContain(m.tipo);
    }
  });

  // ---- primoPassoNonValido ----

  it('CA-102 impostori incoerenti e categorie vuote danno MODALITA', () => {
    expect(Passi.primoPassoNonValido(configBase({ n: 5, k: 3, cats: [] }), cats)).toBe('MODALITA');
  });

  it('CA-102 nome duplicato e impostori incoerenti danno GIOCATORI', () => {
    expect(Passi.primoPassoNonValido(configBase({ n: 4, k: 3, nomi: ['A', 'a'] }), cats)).toBe('GIOCATORI');
  });

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

  // ---- aggiungiGiocatore / rimuoviGiocatore (v2.4) ----

  it('CA-113 aggiungi con nomi vuoti porta N a 5 e nomi completati', () => {
    const r = Passi.aggiungiGiocatore(configBase({ n: 4, nomi: [] }));
    expect(r.numeroGiocatori).toBe(5);
    expect(r.nomi).toEqual(['', '', '', '', '']);
    expect(r.numeroImpostori).toBe(1);
    expect(Regole.nomiEffettivi(r)).toHaveLength(5);
  });

  it('CA-113 aggiungi completa i nomi corti con vuoti', () => {
    const r = Passi.aggiungiGiocatore(configBase({ n: 4, nomi: ['A', 'B'] }));
    expect(r.nomi).toEqual(['A', 'B', '', '', '']);
    expect(r.numeroGiocatori).toBe(5);
  });

  it('CA-113 aggiungi scarta i nomi oltre N', () => {
    const r = Passi.aggiungiGiocatore(configBase({ n: 3, nomi: ['A', 'B', 'C', 'X'] }));
    expect(r.numeroGiocatori).toBe(4);
    expect(r.nomi).toEqual(['A', 'B', 'C', '']);
  });

  it('CA-113 CA-117 aggiungere non modifica gli impostori ne gli altri campi', () => {
    const c = configBase(
      { n: 5, k: 2, mod: 'PAROLA_AFFINE', cats: ['cibo'] },
      { partitaTrappola: true, impostoriSorpresa: true, giriIndizi: 2 },
    );
    const r = Passi.aggiungiGiocatore(c);
    expect(r.numeroGiocatori).toBe(6);
    expect(r.numeroImpostori).toBe(2);
    expect(r).toEqual({ ...c, numeroGiocatori: 6, nomi: ['', '', '', '', '', ''] });
  });

  it('CA-114 aggiungi con 19 porta a 20 e con 20 lascia invariata', () => {
    expect(Passi.aggiungiGiocatore(configBase({ n: 19 })).numeroGiocatori).toBe(20);
    const c = configBase({ n: 20, nomi: ['A'] });
    expect(Passi.aggiungiGiocatore(c)).toEqual(c);
  });

  it("CA-118 aggiungi e rimuovi sono pure e non modificano l'originale", () => {
    const c = configBase({ n: 4, nomi: ['A', 'B'] });
    Passi.aggiungiGiocatore(c);
    Passi.rimuoviGiocatore(c, 0);
    expect(c.numeroGiocatori).toBe(4);
    expect(c.nomi).toEqual(['A', 'B']);
  });

  it('CA-116 rimuovi indice 1 fa scalare i successivi e il vuoto segue la posizione', () => {
    const r = Passi.rimuoviGiocatore(configBase({ n: 4, nomi: ['Anna', '', 'Carla', ''] }), 1);
    expect(r.numeroGiocatori).toBe(3);
    expect(r.nomi).toEqual(['Anna', 'Carla', '']);
    expect(Regole.nomiEffettivi(r)).toEqual(['Anna', 'Carla', 'Giocatore 3']);
  });

  it('CA-116 rimuovi indice 0 e ultimo indice', () => {
    const r0 = Passi.rimuoviGiocatore(configBase({ n: 4, nomi: ['', 'Bea', 'Cia', 'Dino'] }), 0);
    expect(r0.nomi).toEqual(['Bea', 'Cia', 'Dino']);
    const rU = Passi.rimuoviGiocatore(configBase({ n: 4, nomi: ['A', 'B', 'C', 'D'] }), 3);
    expect(rU.nomi).toEqual(['A', 'B', 'C']);
  });

  it('CA-116 rimuovi con nomi piu corti di N', () => {
    const r = Passi.rimuoviGiocatore(configBase({ n: 5, nomi: ['A'] }), 3);
    expect(r.numeroGiocatori).toBe(4);
    // il contratto dice nomi ["A","",""]: si verifica il comportamento osservabile, non la lunghezza di `nomi`
    expect(Regole.nomiEffettivi(r)).toEqual(['A', 'Giocatore 2', 'Giocatore 3', 'Giocatore 4']);
  });

  it('CA-115 rimuovi con 3 giocatori o indice fuori range lascia invariata', () => {
    const c3 = configBase({ n: 3, k: 1, nomi: ['A', 'B', 'C'] });
    expect(Passi.rimuoviGiocatore(c3, 0)).toEqual(c3);
    const c = configBase({ n: 5, nomi: ['A', 'B', 'C', 'D', 'E'] });
    expect(Passi.rimuoviGiocatore(c, -1)).toEqual(c);
    expect(Passi.rimuoviGiocatore(c, 5)).toEqual(c);
  });

  it('CA-117 rimuovere riduce gli impostori al nuovo massimo', () => {
    const dopo = (n: number, k: number) => {
      const r = Passi.rimuoviGiocatore(configBase({ n, k }), 0);
      return [r.numeroGiocatori, r.numeroImpostori];
    };
    expect(dopo(7, 3)).toEqual([6, 2]);
    expect(dopo(5, 2)).toEqual([4, 1]);
    expect(dopo(6, 1)).toEqual([5, 1]);
    expect(dopo(20, 9)).toEqual([19, 9]);
  });

  it('CA-117 rimuovere non tocca sorpresa trappola e modalita', () => {
    const c = configBase({ n: 6, k: 2, mod: 'PAROLA_AFFINE' }, { impostoriSorpresa: true, partitaTrappola: true });
    const r = Passi.rimuoviGiocatore(c, 2);
    expect(r.impostoriSorpresa).toBe(true);
    expect(r.partitaTrappola).toBe(true);
    expect(r.modalita).toBe('PAROLA_AFFINE');
    expect(r.categorieSelezionate).toEqual(c.categorieSelezionate);
  });

  it("CA-118 rimuovere uno di due duplicati elimina l'errore del passo GIOCATORI", () => {
    const c = configBase({ n: 4, nomi: ['Ann', 'ann', 'Bea', 'Cia'] });
    expect(Passi.errorePasso('GIOCATORI', c, cats)).not.toBeNull();
    expect(Passi.errorePasso('GIOCATORI', Passi.rimuoviGiocatore(c, 1), cats)).toBeNull();
  });

  it('CA-118 aggiungere crea un duplicato quando un nome coincide con il default della nuova posizione', () => {
    const c = Passi.aggiungiGiocatore(configBase({ n: 3, nomi: ['Giocatore 4', 'B', 'C'] }));
    expect(Passi.errorePasso('GIOCATORI', c, cats)?.tipo).toBe('NomeDuplicato');
  });

  it('CA-114 CA-115 puoAggiungere e puoRimuovere ai bordi', () => {
    expect(Passi.puoAggiungereGiocatore(configBase({ n: 19 }))).toBe(true);
    expect(Passi.puoAggiungereGiocatore(configBase({ n: 20 }))).toBe(false);
    expect(Passi.puoRimuovereGiocatore(configBase({ n: 3 }))).toBe(false);
    expect(Passi.puoRimuovereGiocatore(configBase({ n: 4 }))).toBe(true);
    expect(Passi.puoAggiungereGiocatore(configBase({ n: 3 }))).toBe(true);
    expect(Passi.puoRimuovereGiocatore(configBase({ n: 20 }))).toBe(true);
  });

  it('CA-113 CA-115 da 3 si aggiunge fino a 20 e si rimuove fino a 3', () => {
    let c = configBase({ n: 3, k: 1 });
    while (Passi.puoAggiungereGiocatore(c)) c = Passi.aggiungiGiocatore(c);
    expect(c.numeroGiocatori).toBe(20);
    while (Passi.puoRimuovereGiocatore(c)) c = Passi.rimuoviGiocatore(c, 0);
    expect(c.numeroGiocatori).toBe(3);
    expect(c.numeroImpostori).toBe(1);
  });

  it('CA-104 riepilogo dopo rimozione riflette il nuovo massimo di impostori', () => {
    const c = configBase({ n: 5, k: 2 }, { impostoriSorpresa: true });
    expect(Passi.riepilogo(c, cats).finoA).toBe(true);
    const r = Passi.riepilogo(Passi.rimuoviGiocatore(c, 0), cats);
    expect(r.numeroGiocatori).toBe(4);
    expect(r.numeroImpostori).toBe(1);
    expect(r.finoA).toBe(false);
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
