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
    <span class="pollice">
      <svg class="spunta" viewBox="0 0 24 24" width="16" height="16" focusable="false">
        <path fill="currentColor" d="m9.55 17.65-4.4-4.4 1.4-1.4 3 3 7.9-7.9 1.4 1.4z" />
      </svg>
    </span>
  </span>
</button>

<style>
  .riga {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--spazio-4);
    width: 100%;
    min-height: 64px;
    padding: var(--spazio-2) 0;
    border: 0;
    background: transparent;
    color: var(--colore-su-superficie);
    text-align: left;
    cursor: pointer;
  }
  .riga:disabled {
    cursor: not-allowed;
    opacity: 0.38;
  }
  .testi {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }
  .etichetta {
    font: var(--testo-titolo);
  }
  .descrizione {
    font: var(--testo-corpo-piccolo);
    color: var(--colore-su-superficie-variante);
  }
  .binario {
    position: relative;
    flex: none;
    width: 52px;
    height: 32px;
    border: 2px solid var(--colore-contorno);
    border-radius: var(--raggio-pieno);
    background: var(--colore-contenitore-superficie-massimo);
    transition:
      background-color var(--molla-effetti),
      border-color var(--molla-effetti);
  }
  .binario.acceso {
    background: var(--colore-primario);
    border-color: var(--colore-primario);
  }
  .pollice {
    position: absolute;
    top: 50%;
    left: 4px;
    display: grid;
    place-items: center;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: var(--colore-contorno);
    color: transparent;
    transform: translateY(-50%);
    transition:
      left var(--molla-spaziale),
      width var(--molla-spaziale),
      height var(--molla-spaziale),
      background-color var(--molla-effetti),
      color var(--molla-effetti);
  }
  .acceso .pollice {
    left: 18px;
    width: 24px;
    height: 24px;
    margin-left: -2px;
    background: var(--colore-su-primario);
    color: var(--colore-primario);
  }
  .spunta {
    opacity: 0;
    transition: opacity var(--molla-effetti);
  }
  .acceso .spunta {
    opacity: 1;
  }
</style>
