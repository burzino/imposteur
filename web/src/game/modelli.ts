export type Modalita = "SENZA_PAROLA" | "PAROLA_AFFINE";

export interface Parola {
  readonly parola: string;
  readonly affine: string | null;
}
export interface Categoria {
  readonly id: string;
  readonly nome: string;
  readonly parole: readonly Parola[];
}

export interface Configurazione {
  readonly numeroGiocatori: number;
  readonly nomi: readonly string[];
  readonly numeroImpostori: number;
  readonly modalita: Modalita;
  readonly mostraCategoria: boolean;
  readonly categorieSelezionate: ReadonlySet<string>;
  readonly impostoreNonPrimo: boolean;
  readonly impostoriSorpresa: boolean;
  readonly ordineCasuale: boolean;
  readonly partitaTrappola: boolean;
  readonly promemoriaUltimaPossibilita: boolean;
  readonly giriIndizi: number;
}

/** Default del Kotlin: 4 giocatori, nomi [], 1 impostore, SENZA_PAROLA, mostraCategoria true, categorie vuote, opzioni false, giriIndizi 1. */
export function configurazioneDefault(sovrascritture: Partial<Configurazione> = {}): Configurazione {
  return {
    numeroGiocatori: 4,
    nomi: [],
    numeroImpostori: 1,
    modalita: "SENZA_PAROLA",
    mostraCategoria: true,
    categorieSelezionate: new Set<string>(),
    impostoreNonPrimo: false,
    impostoriSorpresa: false,
    ordineCasuale: false,
    partitaTrappola: false,
    promemoriaUltimaPossibilita: false,
    giriIndizi: 1,
    ...sovrascritture,
  };
}

export interface VoceParola {
  readonly categoriaId: string;
  readonly categoriaNome: string;
  readonly parola: string;
  readonly affine: string | null;
}

export type ErroreConfigurazione =
  | { readonly tipo: "TroppoPochiGiocatori" }
  | { readonly tipo: "TroppiGiocatori" }
  | { readonly tipo: "TroppoPochiImpostori" }
  | { readonly tipo: "TroppiImpostori" }
  | { readonly tipo: "NessunaCategoria" }
  | { readonly tipo: "PoolVuoto" }
  | { readonly tipo: "NomeDuplicato"; readonly indici: readonly number[] }
  | { readonly tipo: "NomeTroppoLungo"; readonly indice: number };

/** Testi generati dalla logica (specifiche 4.3 e 4.5). */
export const TestiGioco = {
  SEI_IMPOSTORE: "Sei l'impostore",
  LA_PAROLA_E: "La parola è:",
  LA_TUA_PAROLA_E: "La tua parola è:",
  NESSUN_IMPOSTORE: "Nessun impostore: era una partita trappola!",
  PROMEMORIA_ULTIMA_POSSIBILITA:
    "L'impostore scoperto può provare a indovinare la parola: se ci riesce, vince lui!",
  categoria: (nome: string): string => `Categoria: ${nome}`,
  impostoriSingolare: (nome: string): string => `L'impostore era: ${nome}`,
  impostoriPlurale: (nomi: readonly string[]): string => `Gli impostori erano: ${nomi.join(", ")}`,
  laParolaEra: (parola: string): string => `La parola era: ${parola}`,
  laParolaAffineEra: (affine: string): string => `La parola affine era: ${affine}`,
} as const;
