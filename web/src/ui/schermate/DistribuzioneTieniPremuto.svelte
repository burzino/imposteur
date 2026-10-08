<script lang="ts">
  import { onDestroy } from "svelte";
  import { t } from "../testi";

  // Pulsante a pressione lunga (300 ms) con Pointer Events: rilascio, uscita e cancel annullano.
  // Tastiera e tecnologie assistive: click con detail 0 = rivelazione immediata.
  let { abilitato, onRivela }: { abilitato: boolean; onRivela: () => void } = $props();

  const DURATA_PRESSIONE_MS = 300;
  let premuto = $state(false);
  let timer: ReturnType<typeof setTimeout> | null = null;
  let fatto = false;

  function annulla() {
    if (timer !== null) {
      clearTimeout(timer);
      timer = null;
    }
    if (!fatto) premuto = false;
  }

  function giu(e: PointerEvent) {
    if (!abilitato || fatto || premuto) return;
    if (e.pointerType === "mouse" && e.button !== 0) return;
    try {
      // Il tocco cattura il puntatore in modo implicito: lo rilascio per ricevere pointerleave.
      (e.currentTarget as HTMLElement).releasePointerCapture(e.pointerId);
    } catch {
      /* ignorato */
    }
    premuto = true;
    timer = setTimeout(() => {
      timer = null;
      fatto = true;
      onRivela();
    }, DURATA_PRESSIONE_MS);
  }

  function clic(e: MouseEvent) {
    if (e.detail === 0 && abilitato && !fatto) {
      fatto = true;
      onRivela();
    }
  }

  onDestroy(annulla);
</script>

<button
  type="button"
  class="tieni"
  class:spento={!abilitato}
  aria-disabled={!abilitato}
  aria-label={t.distribuzioneScopriCd}
  onpointerdown={giu}
  onpointerup={annulla}
  onpointercancel={annulla}
  onpointerleave={annulla}
  oncontextmenu={(e) => e.preventDefault()}
  ondragstart={(e) => e.preventDefault()}
  onclick={clic}
>
  <span class="riempimento" class:premuto aria-hidden="true"></span>
  <span class="etichetta">{t.distribuzioneTieniPremuto}</span>
</button>

<style>
  .tieni {
    position: relative;
    overflow: hidden;
    width: 100%;
    min-height: 64px;
    border: 0;
    border-radius: 32px;
    background: var(--colore-primario);
    color: var(--colore-su-primario);
    font: inherit;
    font-size: 1.25rem;
    font-weight: 500;
    cursor: pointer;
    touch-action: none;
    user-select: none;
    -webkit-user-select: none;
    -webkit-touch-callout: none;
    -webkit-tap-highlight-color: transparent;
  }
  .spento {
    opacity: 0.5;
  }
  .riempimento {
    position: absolute;
    inset: 0;
    background: var(--colore-su-primario);
    opacity: 0.3;
    transform: scaleX(0);
    transform-origin: left center;
    transition: none;
    pointer-events: none;
  }
  .riempimento.premuto {
    transform: scaleX(1);
    transition: transform 300ms linear;
  }
  .etichetta {
    position: relative;
    pointer-events: none;
  }
</style>
