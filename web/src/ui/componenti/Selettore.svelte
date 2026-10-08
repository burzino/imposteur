<script lang="ts">
  interface Props {
    opzioni: { valore: string; etichetta: string }[];
    valore: string;
    onCambia: (v: string) => void;
    etichetta?: string;
  }
  let { opzioni, valore, onCambia, etichetta }: Props = $props();

  const id = $props.id();
</script>

<div class="selettore" role="radiogroup" aria-label={etichetta}>
  {#each opzioni as o (o.valore)}
    <label class="segmento" class:scelto={o.valore === valore}>
      <input
        class="solo-lettori"
        type="radio"
        name={id}
        value={o.valore}
        checked={o.valore === valore}
        onchange={() => onCambia(o.valore)}
      />
      <span>{o.etichetta}</span>
    </label>
  {/each}
</div>

<style>
  .selettore {
    display: flex;
    border: 1px solid var(--colore-contorno);
    border-radius: 999px;
    overflow: hidden;
  }
  .segmento {
    position: relative;
    flex: 1 1 0;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: var(--altezza-tocco);
    padding: var(--spazio-2) var(--spazio-3);
    text-align: center;
    font-size: 0.9375rem;
    line-height: 1.25;
    cursor: pointer;
    background: transparent;
    color: var(--colore-su-superficie);
  }
  .segmento + .segmento {
    border-left: 1px solid var(--colore-contorno);
  }
  .segmento.scelto {
    background: var(--colore-contenitore-secondario);
    color: var(--colore-su-contenitore-secondario);
    font-weight: 600;
  }
  .segmento:has(input:focus-visible) {
    outline: 3px solid var(--colore-primario);
    outline-offset: -3px;
  }
</style>
