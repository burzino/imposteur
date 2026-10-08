<script lang="ts">
  interface Props {
    testo: string;
    classe?: string;
    maxRighe?: number;
  }
  let { testo, classe, maxRighe = 2 }: Props = $props();

  const PASSO = 0.9;
  const MINIMO = 0.4;
  const INTERLINEA = 1.2;

  let elemento: HTMLElement | undefined = $state();

  /** Riduce del 10% a passo (fino al 40%) finche' il testo sta in larghezza e righe, senza spezzare parole. */
  function adatta(el: HTMLElement): void {
    el.style.fontSize = "";
    const base = parseFloat(getComputedStyle(el).fontSize);
    if (!Number.isFinite(base) || base <= 0) return;
    let fattore = 1;
    for (;;) {
      el.style.fontSize = `${base * fattore}px`;
      const troppoLargo = el.scrollWidth > el.clientWidth + 1;
      const troppoAlto = el.scrollHeight > el.clientHeight + 1;
      if ((!troppoLargo && !troppoAlto) || fattore <= MINIMO + 1e-9) break;
      fattore = Math.max(MINIMO, fattore * PASSO);
    }
  }

  $effect(() => {
    void testo;
    void classe;
    void maxRighe;
    const el = elemento;
    if (!el) return;
    adatta(el);
    if (typeof ResizeObserver === "undefined") return;
    let larghezza = el.clientWidth;
    const osservatore = new ResizeObserver(() => {
      if (el.clientWidth !== larghezza) {
        larghezza = el.clientWidth;
        adatta(el);
      }
    });
    osservatore.observe(el);
    return () => osservatore.disconnect();
  });
</script>

<span
  bind:this={elemento}
  class="adattivo {classe ?? ''}"
  style:max-height="{INTERLINEA * maxRighe}em"
>{testo}</span>

<style>
  .adattivo {
    display: block;
    width: 100%;
    overflow: hidden;
    line-height: 1.2;
    text-align: center;
    overflow-wrap: normal;
    word-break: keep-all;
    hyphens: none;
  }
</style>
