import type { Categoria, Configurazione, ErroreConfigurazione, Modalita } from './modelli';
import { Regole } from './regole';

export type PassoConfigurazione = 'GIOCATORI' | 'OPZIONI' | 'CATEGORIE' | 'RIEPILOGO';

export type OpzioneRiepilogo =
  | 'NON_PARLA_PER_PRIMO'
  | 'TRAPPOLA'
  | 'ORDINE_CASUALE'
  | 'PROMEMORIA'
  | 'SENZA_CATEGORIA'
  | 'GIRI';

export interface RiepilogoConfigurazione {
  readonly numeroGiocatori: number;
  readonly nomi: readonly string[];
  readonly numeroImpostori: number;
  readonly finoA: boolean;
  readonly modalita: Modalita;
  readonly opzioni: readonly OpzioneRiepilogo[];
  readonly giriIndizi: number;
  readonly numeroCategorie: number;
}

const ORDINE: readonly PassoConfigurazione[] = ['GIOCATORI', 'OPZIONI', 'CATEGORIE', 'RIEPILOGO'];

const TIPI_GIOCATORI: readonly ErroreConfigurazione['tipo'][] = [
  'TroppoPochiGiocatori',
  'TroppiGiocatori',
  'TroppoPochiImpostori',
  'TroppiImpostori',
  'NomeDuplicato',
  'NomeTroppoLungo',
];
const TIPI_CATEGORIE: readonly ErroreConfigurazione['tipo'][] = ['NessunaCategoria', 'PoolVuoto'];

export function passoDiPassoConfigurazione(p: PassoConfigurazione): 1 | 2 | 3 | 4 {
  return (ORDINE.indexOf(p) + 1) as 1 | 2 | 3 | 4;
}

function errorePasso(
  passo: PassoConfigurazione,
  config: Configurazione,
  categorie: readonly Categoria[],
): ErroreConfigurazione | null {
  const tipi = passo === 'GIOCATORI' ? TIPI_GIOCATORI : passo === 'CATEGORIE' ? TIPI_CATEGORIE : null;
  if (tipi === null) return null;
  return Regole.valida(config, categorie).find((e) => tipi.includes(e.tipo)) ?? null;
}

function primoPassoNonValido(
  config: Configurazione,
  categorie: readonly Categoria[],
): PassoConfigurazione | null {
  for (const p of ORDINE) if (errorePasso(p, config, categorie) !== null) return p;
  return null;
}

function riepilogo(config: Configurazione, categorie: readonly Categoria[]): RiepilogoConfigurazione {
  const n = config.numeroGiocatori;
  const numeroImpostori = Math.min(
    Math.max(config.numeroImpostori, 1),
    Math.max(1, Regole.maxImpostori(n)),
  );
  const giriIndizi = Math.min(Math.max(config.giriIndizi, 1), Regole.MAX_GIRI);
  const opzioni: OpzioneRiepilogo[] = [];
  if (config.impostoreNonPrimo) opzioni.push('NON_PARLA_PER_PRIMO');
  if (config.partitaTrappola) opzioni.push('TRAPPOLA');
  if (config.ordineCasuale) opzioni.push('ORDINE_CASUALE');
  if (config.promemoriaUltimaPossibilita) opzioni.push('PROMEMORIA');
  if (!config.mostraCategoria && config.modalita === 'SENZA_PAROLA') opzioni.push('SENZA_CATEGORIA');
  if (giriIndizi > 1) opzioni.push('GIRI');
  const ids = new Set(categorie.map((c) => c.id));
  return {
    numeroGiocatori: n,
    nomi: Regole.nomiEffettivi(config),
    numeroImpostori,
    finoA: config.impostoriSorpresa && numeroImpostori > 1,
    modalita: config.modalita,
    opzioni,
    giriIndizi,
    numeroCategorie: [...config.categorieSelezionate].filter((id) => ids.has(id)).length,
  };
}

function contaOpzioniAttive(config: Configurazione): number {
  return (
    (config.impostoreNonPrimo ? 1 : 0) +
    (config.partitaTrappola ? 1 : 0) +
    (config.ordineCasuale ? 1 : 0) +
    (config.promemoriaUltimaPossibilita ? 1 : 0) +
    (config.giriIndizi > 1 ? 1 : 0)
  );
}

export const Passi = { errorePasso, primoPassoNonValido, riepilogo, contaOpzioniAttive } as const;
