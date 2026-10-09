<script lang="ts">
  import type { Snippet } from "svelte";
  import { onMount } from "svelte";
  import { t } from "../testi";

  interface Props {
    titolo: string;
    onHome?: () => void;
    onIndietro?: () => void;
    /** True quando il contenuto e' scorso sotto la barra: sfondo tonale (nessuna ombra). */
    sollevata?: boolean;
    /** Azione facoltativa a destra (es. "Inizia"). */
    azione?: Snippet;
  }
  let { titolo, onHome, onIndietro, sollevata = false, azione }: Props = $props();

  let intestazione: HTMLHeadingElement | undefined = $state();

  // Alla comparsa di una schermata il titolo prende il focus (lettori di schermo, tastiera).
  onMount(() => {
    intestazione?.focus({ preventScroll: true });
  });
</script>

<header class="barra" class:sollevata>
  <div class="interno">
    {#if onIndietro}
      <button type="button" class="icona" aria-label={t.indietro} onclick={() => onIndietro?.()}>
        <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" focusable="false">
          <path fill="currentColor" d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z" />
        </svg>
      </button>
    {/if}
    <h1 bind:this={intestazione} tabindex="-1" class:senza-freccia={!onIndietro}>{titolo}</h1>
    {#if azione}{@render azione()}{/if}
    {#if onHome}
      <button type="button" class="icona" aria-label={t.tornaHome} onclick={() => onHome?.()}>
        <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" focusable="false">
          <path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />
        </svg>
      </button>
    {/if}
  </div>
</header>

<style>
  .barra {
    flex: none;
    background: var(--colore-sfondo);
    color: var(--colore-su-sfondo);
    padding-top: env(safe-area-inset-top);
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
    transition: background-color var(--molla-effetti);
  }
  .barra.sollevata {
    background: var(--colore-contenitore-superficie);
    border-bottom: var(--spessore-contorno) solid var(--colore-bordo-livello);
  }
  .interno {
    display: flex;
    align-items: center;
    gap: var(--spazio-1);
    max-width: var(--larghezza-max);
    min-height: 64px;
    margin: 0 auto;
    padding: 0 var(--spazio-2);
  }
  h1 {
    flex: 1;
    min-width: 0;
    font: var(--testo-titolo-schermata);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    outline: none;
  }
  h1.senza-freccia {
    padding-left: var(--spazio-3);
  }
  .icona {
    flex: none;
    display: grid;
    place-items: center;
    width: var(--altezza-tocco);
    height: var(--altezza-tocco);
    padding: 0;
    border: 0;
    border-radius: 50%;
    background: transparent;
    color: var(--colore-su-sfondo);
    cursor: pointer;
  }
  .icona:hover {
    background: color-mix(in srgb, currentColor 10%, transparent);
  }
  .icona:active {
    background: color-mix(in srgb, currentColor 18%, transparent);
  }
</style>
