<script lang="ts">
  // Cursore nativo <input type="range"> ridisegnato (design.md): binario 16px, maniglia a barra, tacche.
  let {
    valore,
    onCambia,
    etichetta,
    valoreVisibile,
    testoA11y,
    min = 0,
    max = 1000,
    passo = 50,
    id = "cursore-" + Math.random().toString(36).slice(2, 8),
  }: {
    valore: number;
    onCambia: (v: number) => void;
    etichetta: string;
    valoreVisibile: string;
    testoA11y: string;
    min?: number;
    max?: number;
    passo?: number;
    id?: string;
  } = $props();

  const avanzamento = $derived(((valore - min) / (max - min)) * 100);
  const tacche = $derived(Math.round((max - min) / passo));

  function cambia(e: Event) {
    onCambia(Number((e.currentTarget as HTMLInputElement).value));
  }
</script>

<div class="cursore">
  <div class="testa">
    <label for={id}>{etichetta}</label>
    <output for={id} class="valore" aria-hidden="true">{valoreVisibile}</output>
  </div>
  <div class="campo" style:--avanzamento="{avanzamento}%" style:--tacche={tacche}>
    <input
      {id}
      type="range"
      {min}
      {max}
      step={passo}
      value={valore}
      aria-valuetext={testoA11y}
      oninput={cambia}
      onchange={cambia}
    />
  </div>
</div>

<style>
  .cursore {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
  }
  .testa {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: var(--spazio-3);
  }
  label {
    font: var(--testo-titolo);
    color: var(--colore-su-sfondo);
  }
  .valore {
    min-width: 7ch;
    text-align: end;
    font: var(--testo-titolo-sezione);
    color: var(--colore-primario);
    font-variant-numeric: tabular-nums;
  }
  .campo {
    position: relative;
    display: flex;
    align-items: center;
    min-height: 48px;
  }
  /* tacche: una ogni passo, sopra il binario */
  .campo::after {
    content: "";
    position: absolute;
    inset: 50% 8px auto 8px;
    height: 4px;
    transform: translateY(-50%);
    pointer-events: none;
    background-image: radial-gradient(circle, var(--colore-primario) 1.5px, transparent 2px);
    background-size: calc(100% / var(--tacche)) 4px;
    background-position: 0 0;
    background-repeat: repeat-x;
    opacity: 0.5;
  }
  input {
    position: relative;
    z-index: 1;
    width: 100%;
    height: 48px;
    margin: 0;
    background: transparent;
    appearance: none;
    -webkit-appearance: none;
    cursor: pointer;
    touch-action: pan-y;
  }
  input:focus-visible {
    outline: 3px solid var(--colore-primario);
    outline-offset: 2px;
    border-radius: var(--raggio-xs);
  }
  input::-webkit-slider-runnable-track {
    height: 16px;
    border-radius: var(--raggio-pieno);
    background: linear-gradient(
      to right,
      var(--colore-primario) var(--avanzamento),
      var(--colore-contenitore-secondario) var(--avanzamento)
    );
  }
  input::-moz-range-track {
    height: 16px;
    border-radius: var(--raggio-pieno);
    background: linear-gradient(
      to right,
      var(--colore-primario) var(--avanzamento),
      var(--colore-contenitore-secondario) var(--avanzamento)
    );
  }
  input::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 4px;
    height: 44px;
    margin-top: -14px;
    border: 0;
    border-radius: var(--raggio-pieno);
    background: var(--colore-primario);
  }
  input::-moz-range-thumb {
    width: 4px;
    height: 44px;
    border: 0;
    border-radius: var(--raggio-pieno);
    background: var(--colore-primario);
  }
  input:active::-webkit-slider-thumb {
    width: 2px;
  }
  input:active::-moz-range-thumb {
    width: 2px;
  }
</style>
