import { describe, it, expect } from 'vitest';
import { configurazioneAStringa, configurazioneDaStringa } from './serializzazioneConfigurazione';
import { configurazioneDefault } from '../game/modelli';
import type { Categoria, Configurazione } from '../game/modelli';

const cats: Categoria[] = [
  { id: 'a', nome: 'A', parole: [{ parola: 'x', affine: 'y' }] },
  { id: 'b', nome: 'B', parole: [{ parola: 'z', affine: null }] },
  { id: 'c', nome: 'C', parole: [{ parola: 'w', affine: 'v' }] },
];
const rt = (c: Configurazione) => configurazioneDaStringa(configurazioneAStringa(c), cats);

describe('Serializzazione configurazione', () => {
  it('CA-19 e CA-W02 round-trip identico', () => {
    const c = configurazioneDefault({
      numeroGiocatori: 7, nomi: ['Anna', '', 'Carlo'], numeroImpostori: 3, modalita: 'PAROLA_AFFINE',
      mostraCategoria: false, categorieSelezionate: new Set(['a', 'c']),
    });
    expect(rt(c)).toEqual(c);
  });

  it('CA-19 round-trip con tutte le categorie', () => {
    const c = configurazioneDefault({ categorieSelezionate: new Set(['a', 'b', 'c']) });
    expect(rt(c)).toEqual(c);
  });

  it('CA-19 e CA-W02 null o malformata danno default valido', () => {
    for (const s of [null, '', '{rotto', '[]']) {
      const c = configurazioneDaStringa(s, cats);
      expect(c.numeroGiocatori).toBe(4);
      expect(c.numeroImpostori).toBe(1);
      expect(c.modalita).toBe('SENZA_PAROLA');
      expect(c.categorieSelezionate.size).toBeGreaterThan(0);
      expect([...c.categorieSelezionate].every((id) => cats.some((k) => k.id === id))).toBe(true);
    }
  });

  it('CA-W02 campo di tipo errato rende malformato l\'intero JSON: default', () => {
    const c = configurazioneDaStringa('{"numeroGiocatori":"sei","numeroImpostori":2}', cats);
    expect(c.numeroGiocatori).toBe(4);
    expect(c.numeroImpostori).toBe(1);
  });

  it('CA-19 id sconosciuti scartati', () => {
    const s = configurazioneAStringa(configurazioneDefault({ categorieSelezionate: new Set(['a', 'zzz']) }));
    expect(configurazioneDaStringa(s, cats).categorieSelezionate).toEqual(new Set(['a']));
  });

  it('CA-19 nessun id valido seleziona tutte', () => {
    const s = configurazioneAStringa(configurazioneDefault({ categorieSelezionate: new Set(['zzz']) }));
    expect(configurazioneDaStringa(s, cats).categorieSelezionate).toEqual(new Set(['a', 'b', 'c']));
  });

  it('CA-19 e CA-W02 valori fuori limite corretti', () => {
    const alto = rt(configurazioneDefault({ numeroGiocatori: 50, numeroImpostori: 40, categorieSelezionate: new Set(['a']) }));
    expect(alto.numeroGiocatori).toBe(20);
    expect(alto.numeroImpostori).toBe(9);
    const basso = rt(configurazioneDefault({ numeroGiocatori: 1, numeroImpostori: 0, categorieSelezionate: new Set(['a']) }));
    expect(basso.numeroGiocatori).toBe(3);
    expect(basso.numeroImpostori).toBe(1);
    const troppi = rt(configurazioneDefault({ numeroGiocatori: 5, numeroImpostori: 4, categorieSelezionate: new Set(['a']) }));
    expect(troppi.numeroImpostori).toBe(2);
  });

  it('CA-19 nomi in eccesso rispetto a N non superano N', () => {
    const c = rt(configurazioneDefault({ numeroGiocatori: 3, nomi: ['a', 'b', 'c', 'd', 'e'], categorieSelezionate: new Set(['a']) }));
    expect(c.nomi.length).toBeLessThanOrEqual(c.numeroGiocatori);
  });

  it('CA-W02 la stringa e JSON con i nomi di campo del contratto', () => {
    const o = JSON.parse(configurazioneAStringa(configurazioneDefault({ categorieSelezionate: new Set(['a']) })));
    for (const campo of [
      'numeroGiocatori', 'nomi', 'numeroImpostori', 'modalita', 'mostraCategoria', 'categorieSelezionate',
      'impostoreNonPrimo', 'impostoriSorpresa', 'ordineCasuale', 'partitaTrappola', 'promemoriaUltimaPossibilita', 'giriIndizi',
    ]) expect(o, campo).toHaveProperty(campo);
    expect(Array.isArray(o.categorieSelezionate)).toBe(true);
  });
});
