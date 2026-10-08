<script lang="ts">
  import { t } from "../testi";

  interface Props {
    indice: number;
    valore: string;
    duplicato: boolean;
    ultimo: boolean;
    onCambia: (v: string) => void;
    onProssimo: () => void;
  }
  let { indice, valore, duplicato, ultimo, onCambia, onProssimo }: Props = $props();
  let campo: HTMLInputElement | undefined = $state();
  const id = $derived(`nome-${indice}`);

  function tasto(e: KeyboardEvent) {
    if (e.key !== "Enter") return;
    e.preventDefault();
    if (ultimo) campo?.blur();
    else onProssimo();
  }
</script>

<div class="blocco">
  <div class="campo" class:errore={duplicato}>
    <label for={id}>{t.configGiocatoreN(indice + 1)}</label>
    <input
      {id}
      bind:this={campo}
      type="text"
      value={valore}
      placeholder={t.configGiocatoreN(indice + 1)}
      autocapitalize="words"
      autocomplete="off"
      enterkeyhint={ultimo ? "done" : "next"}
      aria-invalid={duplicato}
      oninput={(e) => onCambia(e.currentTarget.value)}
      onkeydown={tasto}
    />
    {#if valore.length > 0}
      <button type="button" class="cancella" aria-label={t.configCancellaNome} onclick={() => onCambia("")}>×</button>
    {/if}
  </div>
  {#if duplicato}<p class="msg" role="alert">{t.configNomeDuplicato}</p>{/if}
</div>

<style>
  .blocco { display: flex; flex-direction: column; gap: var(--spazio-1); }
  .campo {
    position: relative;
    border: 1px solid var(--colore-contorno);
    border-radius: var(--raggio-m);
    background: var(--colore-sfondo);
  }
  .campo:focus-within { border-color: var(--colore-primario); outline: 1px solid var(--colore-primario); }
  .campo.errore, .campo.errore:focus-within { border-color: var(--colore-errore); outline-color: var(--colore-errore); }
  label {
    position: absolute; top: 4px; left: var(--spazio-3);
    font-size: 0.75rem; color: var(--colore-su-superficie-variante);
  }
  .errore label { color: var(--colore-errore); }
  input {
    box-sizing: border-box; width: 100%; min-height: 56px;
    padding: 22px 52px 6px var(--spazio-3);
    border: 0; background: transparent; color: var(--colore-su-sfondo);
    font: inherit; font-size: 1rem; /* >= 16px: niente zoom su iOS */
    outline: none;
  }
  .cancella {
    position: absolute; top: 4px; right: 0;
    width: var(--altezza-tocco); height: var(--altezza-tocco);
    border: 0; background: transparent; color: var(--colore-su-superficie-variante);
    font-size: 1.5rem; cursor: pointer;
  }
  .msg { margin: 0 var(--spazio-3); font-size: 0.75rem; color: var(--colore-errore); }
</style>
