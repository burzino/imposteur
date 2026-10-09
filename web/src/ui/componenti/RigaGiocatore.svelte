<script lang="ts">
  import { t } from "../testi";
  import Avatar from "./Avatar.svelte";

  interface Props {
    indice: number;
    valore: string;
    duplicato: boolean;
    ultimo: boolean;
    /** False con 3 campi: la "x" non c'e' ma il suo spazio resta riservato. */
    puoRimuovere: boolean;
    onCambia: (v: string) => void;
    onRimuovi: () => void;
    onProssimo: () => void;
  }
  let { indice, valore, duplicato, ultimo, puoRimuovere, onCambia, onRimuovi, onProssimo }: Props = $props();
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
  <div class="riga-giocatore">
    <!-- Area di 48 x 48 riservata (futura foto): per ora non interattiva. Nome vuoto: l'avatar mostra il numero. -->
    <span class="area-avatar" aria-hidden="true">
      <Avatar nome={valore.trim()} {indice} stato="pieno" dimensione={40} />
    </span>
    <div class="campo" class:errore={duplicato}>
      <label for={id}>{t.configGiocatoreN(indice + 1)}</label>
      <input
        {id}
        bind:this={campo}
        type="text"
        value={valore}
        autocapitalize="words"
        autocomplete="off"
        enterkeyhint={ultimo ? "done" : "next"}
        aria-invalid={duplicato}
        oninput={(e) => onCambia(e.currentTarget.value)}
        onkeydown={tasto}
      />
    </div>
    <span class="area-x">
      {#if puoRimuovere}
        <button type="button" class="rimuovi" aria-label={t.giocatoriRimuovi(indice + 1)} onclick={onRimuovi}>
          <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" focusable="false">
            <path
              fill="currentColor"
              d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
            />
          </svg>
        </button>
      {/if}
    </span>
  </div>
  {#if duplicato}<p class="msg" role="alert">{t.configNomeDuplicato}</p>{/if}
</div>

<style>
  .blocco { display: flex; flex-direction: column; gap: var(--spazio-1); }
  .riga-giocatore {
    display: grid; grid-template-columns: 48px 1fr 48px; align-items: center; gap: var(--spazio-2);
    min-height: 56px;
  }
  .area-avatar { display: grid; place-items: center; width: 48px; height: 48px; }
  .area-x { display: grid; place-items: center; width: 48px; height: 48px; }
  .campo {
    position: relative;
    border-bottom: 2px solid var(--colore-su-superficie-variante);
    border-radius: var(--raggio-s) var(--raggio-s) 0 0;
    background: var(--colore-contenitore-superficie-alto);
  }
  .campo:focus-within { border-bottom-color: var(--colore-primario); box-shadow: 0 1px 0 0 var(--colore-primario); }
  .campo.errore, .campo.errore:focus-within { border-bottom-color: var(--colore-errore); box-shadow: none; }
  :global([data-tema="alto-contrasto"]) .campo {
    border: 2px solid var(--colore-contorno); border-radius: var(--raggio-s);
  }
  label {
    position: absolute; top: 4px; left: var(--spazio-4);
    font: var(--testo-didascalia); color: var(--colore-su-superficie-variante);
  }
  .errore label { color: var(--colore-errore); }
  input {
    box-sizing: border-box; width: 100%; min-height: 56px;
    padding: 22px var(--spazio-4) 6px var(--spazio-4);
    border: 0; background: transparent; color: var(--colore-su-superficie);
    font: inherit; font-size: 1rem; /* >= 16px: niente zoom su iOS */
    outline: none;
  }
  .rimuovi {
    display: grid; place-items: center;
    width: 48px; height: 48px; padding: 0;
    border: 0; border-radius: var(--raggio-pieno); background: transparent;
    color: var(--colore-su-superficie-variante); cursor: pointer;
  }
  .rimuovi:hover { background: color-mix(in srgb, currentColor 10%, transparent); }
  .rimuovi:active { background: color-mix(in srgb, currentColor 18%, transparent); }
  .rimuovi:focus-visible { outline: 3px solid var(--colore-primario); outline-offset: 2px; }
  .msg { margin: 0 var(--spazio-3) 0 56px; font-size: 0.75rem; color: var(--colore-errore); }
</style>
