<script lang="ts">
  /** Selettore a segmenti connessi (design 3): pillola esterna, raggio interno xs, spunta sul selezionato. */
  interface Props {
    opzioni: { valore: string; etichetta: string }[];
    valore: string;
    onCambia: (v: string) => void;
    etichetta?: string;
    /** Segmenti per riga (default: tutti su una riga). Con 4 opzioni e 2 colonne: due righe. */
    colonne?: number;
  }
  let { opzioni, valore, onCambia, etichetta, colonne }: Props = $props();

  const id = $props.id();
  const col = $derived(Math.max(1, Math.min(colonne ?? opzioni.length, opzioni.length)));
  const ultimaRiga = $derived(Math.floor((opzioni.length - 1) / col));

  /** Angoli esterni pieni, interni xs. */
  function raggio(i: number): string {
    const riga = Math.floor(i / col);
    const c = i % col;
    const ultimaDellaRiga = c === col - 1 || i === opzioni.length - 1;
    const p = "var(--raggio-pieno)";
    const x = "var(--raggio-xs)";
    const tl = riga === 0 && c === 0 ? p : x;
    const tr = riga === 0 && ultimaDellaRiga ? p : x;
    const br = riga === ultimaRiga && i === opzioni.length - 1 ? p : x;
    const bl = riga === ultimaRiga && c === 0 ? p : x;
    return `${tl} ${tr} ${br} ${bl}`;
  }
</script>

<div class="selettore" role="radiogroup" aria-label={etichetta} style:--colonne={col}>
  {#each opzioni as o, i (o.valore)}
    <label class="segmento" class:scelto={o.valore === valore} style:border-radius={raggio(i)}>
      <input
        class="solo-lettori"
        type="radio"
        name={id}
        value={o.valore}
        checked={o.valore === valore}
        onchange={() => onCambia(o.valore)}
      />
      {#if o.valore === valore}
        <svg class="spunta" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" focusable="false">
          <path fill="currentColor" d="m9.55 17.65-4.4-4.4 1.4-1.4 3 3 7.9-7.9 1.4 1.4z" />
        </svg>
      {/if}
      <span>{o.etichetta}</span>
    </label>
  {/each}
</div>

<style>
  .selettore {
    display: grid;
    grid-template-columns: repeat(var(--colonne), minmax(0, 1fr));
    gap: 2px;
  }
  .segmento {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-2);
    min-height: var(--altezza-tocco);
    padding: var(--spazio-2) var(--spazio-3);
    border: 1px solid var(--colore-contorno);
    text-align: center;
    font: var(--testo-etichetta);
    cursor: pointer;
    background: transparent;
    color: var(--colore-su-superficie);
    transition: background-color var(--molla-effetti);
  }
  .segmento:hover {
    background: color-mix(in srgb, var(--colore-su-superficie) 8%, transparent);
  }
  .segmento.scelto {
    background: var(--colore-contenitore-secondario);
    color: var(--colore-su-contenitore-secondario);
    border-color: var(--colore-contenitore-secondario);
  }
  :global([data-tema="alto-contrasto"]) .segmento.scelto {
    border-color: var(--colore-contorno);
  }
  .spunta {
    flex: none;
  }
  .segmento:has(input:focus-visible) {
    outline: 3px solid var(--colore-primario);
    outline-offset: 2px;
  }
</style>
