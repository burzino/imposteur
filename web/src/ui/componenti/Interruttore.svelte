<script lang="ts">
  interface Props {
    valore: boolean;
    onCambia: (v: boolean) => void;
    etichetta: string;
    descrizione?: string;
    disabilitato?: boolean;
  }
  let { valore, onCambia, etichetta, descrizione, disabilitato = false }: Props = $props();

  const id = $props.id();
</script>

<button
  type="button"
  role="switch"
  class="riga"
  aria-checked={valore}
  aria-labelledby="{id}-e"
  aria-describedby={descrizione ? `${id}-d` : undefined}
  disabled={disabilitato}
  onclick={() => onCambia(!valore)}
>
  <span class="testi">
    <span id="{id}-e" class="etichetta">{etichetta}</span>
    {#if descrizione}
      <span id="{id}-d" class="descrizione">{descrizione}</span>
    {/if}
  </span>
  <span class="binario" class:acceso={valore} aria-hidden="true">
    <span class="pallino"></span>
  </span>
</button>

<style>
  .riga {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--spazio-4);
    width: 100%;
    min-height: var(--altezza-tocco);
    padding: var(--spazio-2) 0;
    border: 0;
    background: transparent;
    color: var(--colore-su-superficie);
    text-align: left;
    cursor: pointer;
  }
  .riga:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }
  .testi {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }
  .etichetta {
    font-size: 1rem;
  }
  .descrizione {
    font-size: 0.875rem;
    color: var(--colore-su-superficie-variante);
  }
  .binario {
    position: relative;
    flex: none;
    width: 52px;
    height: 32px;
    border: 2px solid var(--colore-contorno);
    border-radius: 16px;
    background: var(--colore-superficie-variante);
    transition: background 120ms;
  }
  .binario.acceso {
    background: var(--colore-primario);
    border-color: var(--colore-primario);
  }
  .pallino {
    position: absolute;
    top: 50%;
    left: 4px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: var(--colore-contorno);
    transform: translateY(-50%);
    transition:
      left 120ms,
      width 120ms,
      height 120ms,
      background 120ms;
  }
  .acceso .pallino {
    left: 18px;
    width: 24px;
    height: 24px;
    margin-left: -2px;
    background: var(--colore-su-primario);
  }
</style>
