import { describe, it, expect } from 'vitest';
import { FormatoSegnalazioni, segnalazione } from './segnalazioni';
import type { Segnalazione } from './segnalazioni';

// Segnalazioni (SEG-xx, contratto v1.3/v1.4); CA-W15 per il formato di esportazione (una riga JSON per segnalazione).
function unica(s: Segnalazione): Segnalazione {
  const l = FormatoSegnalazioni.leggi(FormatoSegnalazioni.riga(s));
  expect(l).toHaveLength(1);
  return l[0];
}

const coppia = segnalazione({
  tipo: 'coppia', istante: '2026-10-08T21:14:03', categoriaId: 'animali', parola: 'gatto', affine: 'tigre',
  modalita: 'SENZA_PAROLA', motivi: ['TROPPO_SIMILI', 'POCO_CONOSCIUTA'], nota: 'troppo facile',
});
const app = segnalazione({ tipo: 'app', istante: '2026-10-08T21:15:00' });

describe('FormatoSegnalazioni', () => {
  it('SEG-01 round-trip coppia con tutti i campi', () => {
    expect(unica(coppia)).toEqual(coppia);
  });

  it('SEG-02 round-trip app con soli campi minimi', () => {
    const r = unica(app);
    expect(r).toEqual(app);
    expect(r.categoriaId).toBeNull();
    expect(r.parola).toBeNull();
    expect(r.affine).toBeNull();
    expect(r.modalita).toBeNull();
    expect(r.motivi).toEqual([]);
    expect(r.nota).toBe('');
  });

  it('SEG-03 e CA-W15 riga non contiene a capo anche se la nota ne contiene', () => {
    const riga = FormatoSegnalazioni.riga({ ...coppia, nota: 'prima\nseconda\r\nterza\rquarta' });
    expect(riga).not.toContain('\n');
    expect(riga).not.toContain('\r');
    expect(FormatoSegnalazioni.leggi(riga)).toHaveLength(1);
  });

  it('SEG-04 leggi su piu righe mantiene l ordine', () => {
    const c = { ...coppia, istante: '2026-10-09T08:00:00', parola: 'cane' };
    const testo = [coppia, app, c].map((s) => FormatoSegnalazioni.riga(s)).join('\n') + '\n';
    expect(FormatoSegnalazioni.leggi(testo)).toEqual([coppia, app, c]);
  });

  it('SEG-05 leggi salta righe vuote malformate e con motivo sconosciuto', () => {
    const buona1 = FormatoSegnalazioni.riga(coppia);
    const buona2 = FormatoSegnalazioni.riga(app);
    const motivoIgnoto = '{"tipo":"coppia","istante":"2026-10-08T21:14:03","motivi":["MOTIVO_INVENTATO"],"nota":""}';
    const testo = ['', buona1, '   ', "questo non e' json", '{"tipo":', motivoIgnoto, '[1,2,3]', buona2, ''].join('\n');
    expect(FormatoSegnalazioni.leggi(testo)).toEqual([coppia, app]);
  });

  it('SEG-06 leggi di stringa vuota restituisce lista vuota', () => {
    expect(FormatoSegnalazioni.leggi('')).toEqual([]);
  });

  it('SEG-07 campi sconosciuti nel JSON vengono ignorati', () => {
    const json = '{"tipo":"app","istante":"2026-10-08T21:15:00","campoNuovo":42,"altro":{"x":[1,2]},"nota":"ok"}';
    const r = FormatoSegnalazioni.leggi(json);
    expect(r).toEqual([segnalazione({ tipo: 'app', istante: '2026-10-08T21:15:00', nota: 'ok' })]);
  });

  it('SEG-08 accenti e virgolette nella nota sopravvivono al round-trip', () => {
    const nota = 'perché "così" è più città, backslash \\ e \'apici\' ñ 日本';
    expect(unica({ ...coppia, nota }).nota).toBe(nota);
  });

  it('SEG-09 nota oltre 500 caratteri viene troncata a 500', () => {
    const r = unica({ ...coppia, nota: 'a'.repeat(800) });
    expect(r.nota).toHaveLength(500);
    expect(r.nota).toBe('a'.repeat(500));
  });

  it('SEG-10 nota di esattamente 500 caratteri resta intatta', () => {
    const nota = 'b'.repeat(500);
    expect(unica({ ...coppia, nota }).nota).toBe(nota);
  });

  it('SEG-11 nota viene sottoposta a trim', () => {
    expect(unica({ ...coppia, nota: '   ciao mondo \t ' }).nota).toBe('ciao mondo');
  });

  it('SEG-12 nota di soli spazi diventa vuota', () => {
    expect(unica({ ...coppia, nota: '     ' }).nota).toBe('');
  });

  it('SEG-13 round-trip con propostaParola e propostaAffine', () => {
    const s = { ...coppia, propostaParola: 'leone', propostaAffine: 'puma' };
    const r = unica(s);
    expect(r).toEqual(s);
    expect(r.propostaParola).toBe('leone');
    expect(r.propostaAffine).toBe('puma');
  });

  it('SEG-14 riga salvata prima della v1.4 si legge con proposte null', () => {
    const vecchia = '{"tipo":"coppia","istante":"2026-10-08T21:14:03","categoriaId":"animali","parola":"gatto","affine":"tigre","modalita":"SENZA_PAROLA","motivi":["TROPPO_SIMILI"],"nota":"vecchia"}';
    const l = FormatoSegnalazioni.leggi(vecchia);
    expect(l).toHaveLength(1);
    expect(l[0].propostaParola).toBeNull();
    expect(l[0].propostaAffine).toBeNull();
    expect(l[0].parola).toBe('gatto');
    expect(l[0].nota).toBe('vecchia');
  });

  it('SEG-15 normalizza applica trim alle proposte', () => {
    const n = FormatoSegnalazioni.normalizza({ ...coppia, propostaParola: '  leone ', propostaAffine: '\tpuma  ' });
    expect(n.propostaParola).toBe('leone');
    expect(n.propostaAffine).toBe('puma');
  });

  it('SEG-16 normalizza trasforma vuoto e soli spazi in null', () => {
    const n = FormatoSegnalazioni.normalizza({ ...coppia, propostaParola: '', propostaAffine: '    ' });
    expect(n.propostaParola).toBeNull();
    expect(n.propostaAffine).toBeNull();
  });

  it('SEG-17 normalizza tronca le proposte a 40 caratteri', () => {
    const n = FormatoSegnalazioni.normalizza({ ...coppia, propostaParola: 'a'.repeat(60), propostaAffine: 'b'.repeat(40) });
    expect(n.propostaParola).toBe('a'.repeat(40));
    expect(n.propostaAffine).toBe('b'.repeat(40));
  });

  it('SEG-18 riga normalizza anche le proposte', () => {
    const r = unica({ ...coppia, propostaParola: '  x'.padEnd(80, 'x'), propostaAffine: ' ' });
    expect(r.propostaParola).toHaveLength(40);
    expect(r.propostaAffine).toBeNull();
  });

  it('SEG-19 propostaValida false con un solo campo', () => {
    expect(FormatoSegnalazioni.propostaValida({ ...coppia, propostaParola: 'leone' })).toBe(false);
    expect(FormatoSegnalazioni.propostaValida({ ...coppia, propostaAffine: 'puma' })).toBe(false);
    expect(FormatoSegnalazioni.propostaValida(coppia)).toBe(false);
  });

  it('SEG-20 propostaValida false con parole uguali a meno di maiuscole e spazi', () => {
    expect(FormatoSegnalazioni.propostaValida({ ...coppia, propostaParola: 'Pane', propostaAffine: ' pane ' })).toBe(false);
  });

  it('SEG-21 propostaValida true con due parole diverse', () => {
    expect(FormatoSegnalazioni.propostaValida({ ...coppia, propostaParola: 'Pane', propostaAffine: 'Pasta' })).toBe(true);
  });

  it('SEG-22 segnalazione() imposta i default null, [] e vuoto', () => {
    const s = segnalazione({ tipo: 'app', istante: 'x' });
    expect(s).toMatchObject({
      categoriaId: null, parola: null, affine: null, modalita: null, motivi: [], nota: '',
      propostaParola: null, propostaAffine: null,
    });
  });
});
