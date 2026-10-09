<script lang="ts">
  import type { Snippet } from "svelte";

  interface Props {
    variante?: "pieno" | "tonale" | "contorno" | "testo";
    disabilitato?: boolean;
    onClick: () => void;
    children: Snippet;
    ariaLabel?: string;
  }
  let { variante = "pieno", disabilitato = false, onClick, children, ariaLabel }: Props = $props();
</script>

<button
  type="button"
  class="pulsante {variante}"
  disabled={disabilitato}
  aria-label={ariaLabel}
  onclick={() => onClick()}
>
  {@render children()}
</button>

<style>
  /* Pillola a riposo; premuta passa a --raggio-m (morph) e rimpicciolisce a 0,97. */
  .pulsante {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-2);
    width: 100%;
    min-height: var(--altezza-pulsante);
    padding: var(--spazio-2) var(--spazio-5);
    border: 1px solid transparent;
    border-radius: var(--raggio-pieno);
    font: var(--testo-titolo);
    text-align: center;
    cursor: pointer;
    user-select: none;
    -webkit-user-select: none;
    transition:
      border-radius var(--durata-veloce) ease-out,
      transform var(--durata-veloce) ease-out,
      background-color var(--molla-effetti),
      filter var(--molla-effetti);
  }
  .pieno {
    background: var(--colore-primario);
    color: var(--colore-su-primario);
  }
  .tonale {
    background: var(--colore-contenitore-secondario);
    color: var(--colore-su-contenitore-secondario);
    border-color: var(--colore-bordo-livello);
  }
  .contorno {
    background: transparent;
    color: var(--colore-primario);
    border-color: var(--colore-contorno);
  }
  .testo {
    width: auto;
    min-height: var(--altezza-tocco);
    background: transparent;
    color: var(--colore-primario);
    padding-left: var(--spazio-4);
    padding-right: var(--spazio-4);
  }
  .pulsante:not(:disabled):hover {
    filter: brightness(1.08);
  }
  .testo:not(:disabled):hover,
  .contorno:not(:disabled):hover {
    filter: none;
    background: color-mix(in srgb, var(--colore-primario) 10%, transparent);
  }
  .pulsante:not(:disabled):active {
    border-radius: var(--raggio-m);
    transform: scale(0.97);
    filter: brightness(0.92);
  }
  .pulsante:disabled {
    cursor: not-allowed;
    background: color-mix(in srgb, var(--colore-su-superficie) 12%, transparent);
    color: color-mix(in srgb, var(--colore-su-superficie) 38%, transparent);
    border-color: transparent;
  }
  .contorno:disabled {
    border-color: color-mix(in srgb, var(--colore-su-superficie) 15%, transparent);
    background: transparent;
  }
  .testo:disabled {
    background: transparent;
  }
</style>
