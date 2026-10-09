<script lang="ts">
  /** Carte selezionabili (radio) in colonna: titolo, riga di spiegazione, indicatore a sinistra. */
  interface Props {
    opzioni: { valore: string; titolo: string; descrizione?: string }[];
    valore: string;
    onCambia: (v: string) => void;
    etichetta?: string;
  }
  let { opzioni, valore, onCambia, etichetta }: Props = $props();

  const id = $props.id();
</script>

<div class="gruppo" role="radiogroup" aria-label={etichetta}>
  {#each opzioni as o (o.valore)}
    <label class="carta" class:scelta={o.valore === valore}>
      <input
        class="solo-lettori"
        type="radio"
        name={id}
        value={o.valore}
        checked={o.valore === valore}
        onchange={() => onCambia(o.valore)}
      />
      <span class="indicatore" aria-hidden="true"><span class="pallino"></span></span>
      <span class="testi">
        <span class="titolo">{o.titolo}</span>
        {#if o.descrizione}<span class="descrizione">{o.descrizione}</span>{/if}
      </span>
    </label>
  {/each}
</div>

<style>
  .gruppo {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
  }
  .carta {
    display: flex;
    align-items: center;
    gap: var(--spazio-4);
    min-height: 72px;
    padding: var(--spazio-3) var(--spazio-4);
    border: 2px solid transparent;
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
    color: var(--colore-su-superficie);
    cursor: pointer;
    transition:
      background-color var(--molla-effetti),
      border-color var(--molla-effetti);
  }
  :global([data-tema="alto-contrasto"]) .carta {
    border-color: var(--colore-contorno);
  }
  .carta.scelta {
    background: var(--colore-contenitore-primario);
    color: var(--colore-su-contenitore-primario);
    border-color: var(--colore-primario);
  }
  .indicatore {
    flex: none;
    display: grid;
    place-items: center;
    width: 24px;
    height: 24px;
    border: 2px solid var(--colore-contorno);
    border-radius: 50%;
  }
  .scelta .indicatore {
    border-color: var(--colore-su-contenitore-primario);
  }
  .pallino {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: var(--colore-su-contenitore-primario);
    transform: scale(0);
    transition: transform var(--molla-spaziale);
  }
  .scelta .pallino {
    transform: scale(1);
  }
  .testi {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }
  .titolo {
    font: var(--testo-titolo);
  }
  .descrizione {
    font: var(--testo-corpo-piccolo);
    color: var(--colore-su-superficie-variante);
  }
  .scelta .descrizione {
    color: inherit;
  }
  .carta:has(input:focus-visible) {
    outline: 3px solid var(--colore-primario);
    outline-offset: 2px;
  }
</style>
