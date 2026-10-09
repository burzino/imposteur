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
    <span class="valore" aria-live="polite">
      {#key valore}<span class="num">{valore}</span>{/key}
    </span>
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
    padding: var(--spazio-2) var(--spazio-3) var(--spazio-2) 20px;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
  }
  .etichetta {
    min-width: 0;
    font: var(--testo-titolo);
  }
  .controlli {
    flex: none;
    display: flex;
    align-items: center;
    gap: 0;
  }
  .tasto {
    display: grid;
    place-items: center;
    width: var(--altezza-tocco);
    height: var(--altezza-tocco);
    padding: 0;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: 50%;
    background: var(--colore-contenitore-secondario);
    color: var(--colore-su-contenitore-secondario);
    cursor: pointer;
    transition: transform var(--durata-veloce) ease-out;
  }
  .tasto:not(:disabled):active {
    transform: scale(0.92);
  }
  .tasto:disabled {
    cursor: not-allowed;
    background: color-mix(in srgb, var(--colore-su-superficie) 12%, transparent);
    color: color-mix(in srgb, var(--colore-su-superficie) 38%, transparent);
  }
  .valore {
    min-width: 40px;
    text-align: center;
    font: var(--testo-titolo-sezione);
    font-variant-numeric: tabular-nums;
  }
  .num {
    display: inline-block;
    animation: rimbalzo var(--molla-spaziale) both;
  }
  @keyframes rimbalzo {
    from {
      transform: scale(1.12);
    }
    to {
      transform: scale(1);
    }
  }
</style>
