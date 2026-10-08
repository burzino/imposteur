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
  .pulsante {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-2);
    width: 100%;
    min-height: var(--altezza-tocco);
    padding: var(--spazio-2) var(--spazio-5);
    border: 1px solid transparent;
    border-radius: 999px;
    font-size: 1rem;
    font-weight: 500;
    line-height: 1.25;
    text-align: center;
    cursor: pointer;
    user-select: none;
    -webkit-user-select: none;
  }
  .pieno {
    background: var(--colore-primario);
    color: var(--colore-su-primario);
  }
  .tonale {
    background: var(--colore-contenitore-secondario);
    color: var(--colore-su-contenitore-secondario);
  }
  .contorno {
    background: transparent;
    color: var(--colore-primario);
    border-color: var(--colore-contorno);
  }
  .testo {
    width: auto;
    background: transparent;
    color: var(--colore-primario);
    padding-left: var(--spazio-3);
    padding-right: var(--spazio-3);
  }
  .pulsante:not(:disabled):hover {
    filter: brightness(1.08);
  }
  .pulsante:not(:disabled):active {
    filter: brightness(0.92);
  }
  .pulsante:disabled {
    cursor: not-allowed;
    background: color-mix(in srgb, var(--colore-su-superficie) 12%, transparent);
    color: color-mix(in srgb, var(--colore-su-superficie) 40%, transparent);
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
