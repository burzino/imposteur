import type { Categoria, Configurazione, Modalita, Parola, VoceParola } from './modelli';
import { configurazioneDefault } from './modelli';
import type { Partita } from './partita';

export function parole(...coppie: [string, string | null][]): Parola[] {
  return coppie.map(([parola, affine]) => ({ parola, affine }));
}

export function cat(id: string, ...coppie: [string, string | null][]): Categoria {
  return { id, nome: id.charAt(0).toUpperCase() + id.slice(1), parole: parole(...coppie) };
}

export function categorieDemo(): Categoria[] {
  return [
    cat('animali', ['Cane', 'Lupo'], ['Gatto', 'Leone'], ['Topo', 'Criceto']),
    cat('cibo', ['Pizza', 'Focaccia'], ['Pasta', 'Riso']),
  ];
}

export interface OpzioniConfigBase {
  n?: number;
  k?: number;
  mod?: Modalita;
  cats?: string[];
  nomi?: string[];
  mostra?: boolean;
}

export function configBase(o: OpzioniConfigBase = {}, extra: Partial<Configurazione> = {}): Configurazione {
  return configurazioneDefault({
    numeroGiocatori: o.n ?? 5,
    nomi: o.nomi ?? [],
    numeroImpostori: o.k ?? 1,
    modalita: o.mod ?? 'SENZA_PAROLA',
    mostraCategoria: o.mostra ?? true,
    categorieSelezionate: new Set(o.cats ?? ['animali', 'cibo']),
    ...extra,
  });
}

export const voceCane: VoceParola = { categoriaId: 'animali', categoriaNome: 'Animali', parola: 'Cane', affine: 'Lupo' };

export function partitaBase(over: Partial<Partita> = {}): Partita {
  return {
    giocatori: ['Anna', 'Bruno', 'Carla', 'Dino'],
    impostori: new Set([1]),
    voce: voceCane,
    modalita: 'SENZA_PAROLA',
    mostraCategoria: true,
    primoGiocatore: 0,
    ordine: null,
    giriIndizi: 1,
    promemoriaUltimaPossibilita: false,
    ...over,
  };
}
