<script lang="ts">
  import { t } from "../testi";

  interface Props {
    valore: number;
    min: number;
    max: number;
    onCambia: (n: number) => void;
    etichetta: string;
  }
  let { valore, min, max, onCambia, etichetta }: Props = $props();

  const id = $props.id();
</script>

<div class="contatore" role="group" aria-labelledby="{id}-e">
  <span id="{id}-e" class="etichetta">{etichetta}</span>
  <div class="controlli">
    <button
      type="button"
      class="tasto"
      aria-label="{t.configDiminuisci}: {etichetta}"
      disabled={valore <= min}
      onclick={() => onCambia(Math.max(min, valore - 1))}
    >
      <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" focusable="false">
        <path fill="currentColor" d="M5 11h14v2H5z" />
      </svg>
    </button>
    <span class="valore" aria-live="polite">{valore}</span>
    <button
      type="button"
      class="tasto"
      aria-label="{t.configAumenta}: {etichetta}"
      disabled={valore >= max}
      onclick={() => onCambia(Math.min(max, valore + 1))}
    >
      <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" focusable="false">
        <path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6z" />
      </svg>
    </button>
  </div>
</div>

<style>
  .contatore {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--spazio-3);
    min-height: var(--altezza-tocco);
  }
  .etichetta {
    min-width: 0;
  }
  .controlli {
    flex: none;
    display: flex;
    align-items: center;
    gap: var(--spazio-1);
  }
  .tasto {
    display: grid;
    place-items: center;
    width: var(--altezza-tocco);
    height: var(--altezza-tocco);
    padding: 0;
    border: 1px solid var(--colore-contorno);
    border-radius: 50%;
    background: transparent;
    color: var(--colore-primario);
    cursor: pointer;
  }
  .tasto:disabled {
    cursor: not-allowed;
    opacity: 0.4;
  }
  .valore {
    min-width: 2.5ch;
    text-align: center;
    font-size: 1.25rem;
    font-variant-numeric: tabular-nums;
  }
</style>
