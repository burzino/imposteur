import { describe, it, expect } from 'vitest';
import { CHIAVI_ARCHIVIO, creaArchivio, storageInMemoria } from './archivio';
import type { ArchivioStorage } from './archivio';
import { configurazioneDaStringa } from './serializzazioneConfigurazione';
import { segnalazione } from './segnalazioni';
import { aspettoDefault } from './aspetto';
import { configurazioneDefault } from '../game/modelli';
import type { Partita, SessioneSalvata } from '../game/partita';
import { categorieDemo, partitaBase } from '../game/fixture';

const cats = categorieDemo();
const vuota: SessioneSalvata = { partita: null, stato: null, usate: [], ultima: null };

/** Storage in cui ogni accesso lancia (modalita privata, accessor bloccato). */
function storageCheLancia(): ArchivioStorage {
  const boom = (): never => { throw new Error('storage bloccato'); };
  return { getItem: boom, setItem: boom, removeItem: boom };
}

const sessione = (p: Partita = partitaBase()): SessioneSalvata => ({
  partita: p, stato: { tipo: 'Passaggio', indice: 1 }, usate: [{ categoriaId: 'animali', parola: 'cane' }],
  ultima: { categoriaId: 'animali', parola: 'cane' },
});

describe('Archivio', () => {
  it('CA-W02 salva e carica la configurazione', () => {
    const a = creaArchivio(storageInMemoria());
    const c = configurazioneDefault({ numeroGiocatori: 7, numeroImpostori: 3, modalita: 'PAROLA_AFFINE', categorieSelezionate: new Set(['cibo']) });
    a.salvaConfigurazione(c);
    expect(a.leggiConfigurazione(cats)).toEqual(c);
  });

  it('CA-W02 senza dati salvati la configurazione e quella di default', () => {
    const a = creaArchivio(storageInMemoria());
    expect(a.leggiConfigurazione(cats)).toEqual(configurazioneDaStringa(null, cats));
  });

  it('CA-W02 salva e carica la sessione', () => {
    const a = creaArchivio(storageInMemoria());
    const s = sessione();
    a.salvaSessione(s);
    const r = a.leggiSessione(cats);
    expect(r.partita).toEqual(s.partita);
    expect(r.stato).toEqual(s.stato);
    expect(r.usate).toEqual(s.usate);
    expect(r.ultima).toEqual(s.ultima);
  });

  it('CA-W03 sessione assente o con JSON non valido e vuota senza eccezioni', () => {
    const st = storageInMemoria();
    const a = creaArchivio(st);
    expect(a.leggiSessione(cats)).toEqual(vuota);
    st.setItem(CHIAVI_ARCHIVIO.sessione, '{non json');
    expect(a.leggiSessione(cats)).toEqual(vuota);
  });

  it('CA-W03 sessione incoerente con parole.json scartata (partita null)', () => {
    const a = creaArchivio(storageInMemoria());
    a.salvaSessione(sessione(partitaBase({ voce: { categoriaId: 'sparita', categoriaNome: 'S', parola: 'X', affine: null } })));
    expect(a.leggiSessione(cats).partita).toBeNull();
  });

  it('CA-W02 chiavi distinte: scrivere la configurazione non tocca la sessione', () => {
    const st = storageInMemoria();
    const a = creaArchivio(st);
    a.salvaSessione(sessione());
    const prima = st.getItem(CHIAVI_ARCHIVIO.sessione);
    a.salvaConfigurazione(configurazioneDefault());
    expect(st.getItem(CHIAVI_ARCHIVIO.sessione)).toBe(prima);
    expect(st.getItem(CHIAVI_ARCHIVIO.configurazione)).not.toBeNull();
    const chiavi = Object.values(CHIAVI_ARCHIVIO);
    expect(new Set(chiavi).size).toBe(chiavi.length);
  });

  it('CA-W14 salva e carica l aspetto; default se assente', () => {
    const a = creaArchivio(storageInMemoria());
    expect(a.leggiAspetto()).toEqual(aspettoDefault);
    a.salvaAspetto({ tema: 'ALTO_CONTRASTO', coloriDinamici: false });
    expect(a.leggiAspetto()).toEqual({ tema: 'ALTO_CONTRASTO', coloriDinamici: false });
  });

  it('CA-W15 segnalazioni: aggiungi, conta, leggi in ordine, cancella', () => {
    const a = creaArchivio(storageInMemoria());
    expect(a.contaSegnalazioni()).toBe(0);
    expect(a.leggiSegnalazioni()).toEqual([]);
    const s1 = segnalazione({ tipo: 'app', istante: '2026-10-08T10:00:00', nota: 'uno' });
    const s2 = segnalazione({ tipo: 'coppia', istante: '2026-10-08T11:00:00', parola: 'gatto', affine: 'tigre', motivi: ['TROPPO_SIMILI'] });
    a.aggiungiSegnalazione(s1);
    a.aggiungiSegnalazione(s2);
    expect(a.contaSegnalazioni()).toBe(2);
    expect(a.leggiSegnalazioni()).toEqual([s1, s2]);
    a.cancellaSegnalazioni();
    expect(a.contaSegnalazioni()).toBe(0);
    expect(a.leggiSegnalazioni()).toEqual([]);
  });

  it('CA-W15 il testo salvato e JSONL: una riga per segnalazione terminata da a capo', () => {
    const st = storageInMemoria();
    const a = creaArchivio(st);
    a.aggiungiSegnalazione(segnalazione({ tipo: 'app', istante: 'a', nota: 'x\ny' }));
    a.aggiungiSegnalazione(segnalazione({ tipo: 'app', istante: 'b' }));
    const testo = st.getItem(CHIAVI_ARCHIVIO.segnalazioni)!;
    expect(testo.endsWith('\n')).toBe(true);
    const righe = testo.split('\n').filter((r) => r.length > 0);
    expect(righe).toHaveLength(2);
    for (const r of righe) expect(() => JSON.parse(r)).not.toThrow();
  });

  it('CA-W15 contaSegnalazioni ignora righe malformate', () => {
    const st = storageInMemoria();
    const a = creaArchivio(st);
    a.aggiungiSegnalazione(segnalazione({ tipo: 'app', istante: 'a' }));
    st.setItem(CHIAVI_ARCHIVIO.segnalazioni, (st.getItem(CHIAVI_ARCHIVIO.segnalazioni) ?? '') + 'spazzatura\n');
    expect(a.contaSegnalazioni()).toBe(1);
  });

  it('CA-W04 e CA-W16 storage che lancia: le scritture non propagano eccezioni', () => {
    const a = creaArchivio(storageCheLancia());
    expect(() => a.salvaConfigurazione(configurazioneDefault())).not.toThrow();
    expect(() => a.salvaSessione(sessione())).not.toThrow();
    expect(() => a.salvaAspetto({ tema: 'SCURO', coloriDinamici: true })).not.toThrow();
    expect(() => a.aggiungiSegnalazione(segnalazione({ tipo: 'app', istante: 'a' }))).not.toThrow();
    expect(() => a.cancellaSegnalazioni()).not.toThrow();
  });

  it('CA-W04 e CA-W16 storage che lancia: le letture danno i valori di default', () => {
    const a = creaArchivio(storageCheLancia());
    expect(a.leggiConfigurazione(cats)).toEqual(configurazioneDaStringa(null, cats));
    expect(a.leggiSessione(cats)).toEqual(vuota);
    expect(a.leggiAspetto()).toEqual(aspettoDefault);
    expect(a.leggiSegnalazioni()).toEqual([]);
    expect(a.contaSegnalazioni()).toBe(0);
  });

  it('CA-W04 solo setItem che lancia (quota piena): letture comunque valide', () => {
    const st = storageInMemoria();
    const a = creaArchivio({
      getItem: (k) => st.getItem(k),
      setItem: () => { throw new Error('QuotaExceededError'); },
      removeItem: (k) => st.removeItem(k),
    });
    expect(() => a.salvaConfigurazione(configurazioneDefault())).not.toThrow();
    expect(a.leggiConfigurazione(cats)).toEqual(configurazioneDaStringa(null, cats));
  });
});
