<script lang="ts">
  import { onDestroy } from "svelte";
  import { t } from "../testi";
  import { getStato } from "../stato.svelte";
  import { soloTocco } from "../../game/durataPressione";

  // Pulsante a pressione lunga (durata dall'aspetto; 0 = rivelazione al rilascio) con Pointer Events: rilascio, uscita e cancel annullano.
  // Tastiera e tecnologie assistive: click con detail 0 = rivelazione immediata.
  let { abilitato, onRivela }: { abilitato: boolean; onRivela: () => void } = $props();

  const stato = getStato();
  const durata = $derived(stato.aspetto.durataPressioneMs);
  const tocco = $derived(soloTocco(durata));
  let premuto = $state(false);
  let timer: ReturnType<typeof setTimeout> | null = null;
  let fatto = false;

  let inizioTocco = false;

  function annulla() {
    inizioTocco = false;
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
    if (tocco) {
      inizioTocco = true;
      return;
    }
    premuto = true;
    timer = setTimeout(() => {
      timer = null;
      fatto = true;
      onRivela();
    }, durata);
  }

  function su() {
    if (tocco && inizioTocco && abilitato && !fatto) {
      inizioTocco = false;
      fatto = true;
      onRivela();
      return;
    }
    annulla();
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
  style:--durata-barra="{durata}ms"
  aria-label={t.distribuzioneScopriCd}
  onpointerdown={giu}
  onpointerup={su}
  onpointercancel={annulla}
  onpointerleave={annulla}
  oncontextmenu={(e) => e.preventDefault()}
  ondragstart={(e) => e.preventDefault()}
  onclick={clic}
>
  <!-- Due livelli di testo identici: sotto su binario, sopra su riempimento, ritagliato alla stessa larghezza -->
  {#if tocco}
    <span class="etichetta">{t.distribuzioneTocca}</span>
  {:else}
    <span class="riempimento riempimento-info" class:premuto aria-hidden="true"></span>
    <span class="etichetta">{t.distribuzioneTieniPremuto}</span>
    <span class="etichetta sopra riempimento-info" class:premuto aria-hidden="true">{t.distribuzioneTieniPremuto}</span>
  {/if}
</button>

<style>
  .tieni {
    position: relative;
    overflow: hidden;
    display: block;
    width: 100%;
    min-height: var(--altezza-pulsante-grande);
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xxl);
    background: var(--colore-contenitore-primario);
    color: var(--colore-su-contenitore-primario);
    font: var(--testo-titolo-sezione);
    cursor: pointer;
    touch-action: none;
    user-select: none;
    -webkit-user-select: none;
    -webkit-touch-callout: none;
    -webkit-tap-highlight-color: transparent;
  }
  .spento {
    opacity: 0.38;
  }
  .riempimento {
    position: absolute;
    inset: 0;
    background: var(--colore-primario);
    clip-path: inset(0 100% 0 0);
    transition: clip-path var(--durata-rilascio-barra) ease-out;
    pointer-events: none;
  }
  .etichetta {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: calc(var(--altezza-pulsante-grande) - 2 * var(--spessore-contorno));
    padding: 0 var(--spazio-5);
    text-align: center;
    pointer-events: none;
  }
  .etichetta.sopra {
    position: absolute;
    inset: 0;
    color: var(--colore-su-primario);
    clip-path: inset(0 100% 0 0);
    transition: clip-path var(--durata-rilascio-barra) ease-out;
  }
  /* il riempimento avanza nella durata scelta (--durata-barra), lineare, in sincronia con il timer di rivelazione */
  .riempimento.premuto,
  .etichetta.sopra.premuto {
    clip-path: inset(0 0 0 0);
    transition: clip-path var(--durata-barra) linear;
  }
</style>
