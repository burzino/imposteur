<script lang="ts">
  import type { Snippet } from "svelte";

  interface Props {
    titolo: string;
    aperta: boolean;
    onCambia: (a: boolean) => void;
    children: Snippet;
  }
  let { titolo, aperta, onCambia, children }: Props = $props();

  const id = $props.id();
</script>

<section class="fisarmonica">
  <h3>
    <button
      type="button"
      class="testata"
      aria-expanded={aperta}
      aria-controls="{id}-p"
      onclick={() => onCambia(!aperta)}
    >
      <span>{titolo}</span>
      <svg class="freccia" class:su={aperta} viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" focusable="false">
        <path fill="currentColor" d="M7.41 8.59 12 13.17l4.59-4.58L18 10l-6 6-6-6z" />
      </svg>
    </button>
  </h3>
  <div id="{id}-p" class="pannello" hidden={!aperta}>
    {#if aperta}
      {@render children()}
    {/if}
  </div>
</section>

<style>
  .fisarmonica {
    border: 1px solid var(--colore-contorno-variante);
    border-radius: var(--raggio-m);
    background: var(--colore-superficie);
    color: var(--colore-su-superficie);
  }
  h3 {
    font-size: 1rem;
    font-weight: 500;
  }
  .testata {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--spazio-3);
    width: 100%;
    min-height: var(--altezza-tocco);
    padding: var(--spazio-2) var(--spazio-4);
    border: 0;
    border-radius: var(--raggio-m);
    background: transparent;
    text-align: left;
    font-weight: 500;
    cursor: pointer;
  }
  .freccia {
    flex: none;
    transition: transform 150ms;
  }
  .freccia.su {
    transform: rotate(180deg);
  }
  .pannello {
    padding: 0 var(--spazio-4) var(--spazio-4);
  }
  .pannello[hidden] {
    display: none;
  }
</style>
