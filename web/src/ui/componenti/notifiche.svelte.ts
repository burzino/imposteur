/** Un solo messaggio alla volta; sparisce da solo. */
const DURATA_MS = 3000;

class StatoToast {
  testo = $state<string | null>(null);
  #timer: ReturnType<typeof setTimeout> | null = null;

  mostra(testo: string): void {
    if (this.#timer !== null) clearTimeout(this.#timer);
    this.testo = testo;
    this.#timer = setTimeout(() => {
      this.testo = null;
      this.#timer = null;
    }, DURATA_MS);
  }
}

export const statoToast = new StatoToast();

export function mostraToast(testo: string): void {
  statoToast.mostra(testo);
}
