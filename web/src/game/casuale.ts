export interface Casuale {
  /** Intero uniforme con 0 <= x < limite. limite >= 1 (intero). */
  intero(limite: number): number;
}

/** Generatore deterministico (mulberry32), per i test. */
export function casualeConSeme(seme: number): Casuale {
  let stato = seme >>> 0;
  const prossimo = (): number => {
    stato = (stato + 0x6d2b79f5) >>> 0;
    let t = stato;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
  return {
    intero(limite: number): number {
      return Math.floor(prossimo() * limite);
    },
  };
}

/** Generatore di sistema (crypto.getRandomValues) senza bias: rejection sampling su 32 bit. */
export function casualeDiSistema(): Casuale {
  const buffer = new Uint32Array(1);
  const prossimo32 = (): number => {
    globalThis.crypto.getRandomValues(buffer);
    return buffer[0];
  };
  return {
    intero(limite: number): number {
      if (!Number.isInteger(limite) || limite < 1 || limite > 4294967296) {
        throw new RangeError(`limite non valido: ${limite}`);
      }
      // Scarta i valori nella coda incompleta dell'intervallo a 2^32.
      const massimoAccettato = 4294967296 - (4294967296 % limite);
      let v = prossimo32();
      while (v >= massimoAccettato) v = prossimo32();
      return v % limite;
    },
  };
}

/** Equivalente di nextInt(minInclusivo, maxEsclusivo). */
export function interoTra(c: Casuale, minInclusivo: number, maxEsclusivo: number): number {
  return minInclusivo + c.intero(maxEsclusivo - minInclusivo);
}

/**
 * Valore in [0, 1) costruito con UNA sola chiamata a intero(2^30): 30 bit di risoluzione
 * bastano per confrontare con PROBABILITA_TRAPPOLA.
 */
export function reale(c: Casuale): number {
  const LIMITE = 1 << 30;
  return c.intero(LIMITE) / LIMITE;
}

/** Fisher-Yates (stesso schema di Collections.shuffle): nuovo array, l'originale non cambia. */
export function mescolato<T>(c: Casuale, v: readonly T[]): T[] {
  const r = [...v];
  for (let i = r.length; i > 1; i--) {
    const j = c.intero(i);
    const t = r[i - 1];
    r[i - 1] = r[j];
    r[j] = t;
  }
  return r;
}
