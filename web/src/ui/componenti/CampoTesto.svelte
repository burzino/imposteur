<script lang="ts">
  interface Props {
    valore: string;
    onCambia: (v: string) => void;
    etichetta: string;
    segnaposto?: string;
    maxLunghezza?: number;
    errore?: string;
    multilinea?: boolean;
    righe?: number;
  }
  let {
    valore,
    onCambia,
    etichetta,
    segnaposto,
    maxLunghezza,
    errore,
    multilinea = false,
    righe = 3,
  }: Props = $props();

  const id = $props.id();
</script>

<div class="campo" class:con-errore={!!errore}>
  <label for="{id}-c">{etichetta}</label>
  {#if multilinea}
    <textarea
      id="{id}-c"
      rows={righe}
      value={valore}
      placeholder={segnaposto}
      maxlength={maxLunghezza}
      aria-invalid={errore ? "true" : undefined}
      aria-describedby={errore ? `${id}-e` : undefined}
      oninput={(e) => onCambia(e.currentTarget.value)}
    ></textarea>
  {:else}
    <input
      id="{id}-c"
      type="text"
      value={valore}
      placeholder={segnaposto}
      maxlength={maxLunghezza}
      autocomplete="off"
      aria-invalid={errore ? "true" : undefined}
      aria-describedby={errore ? `${id}-e` : undefined}
      oninput={(e) => onCambia(e.currentTarget.value)}
    />
  {/if}
  {#if errore}
    <p id="{id}-e" class="errore" role="alert">{errore}</p>
  {/if}
</div>

<style>
  .campo {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-1);
  }
  label {
    font: var(--testo-didascalia);
    color: var(--colore-su-superficie-variante);
  }
  /* campo riempito: contenitore alto, angoli superiori s, indicatore inferiore da 2 px */
  input,
  textarea {
    width: 100%;
    min-height: var(--altezza-tocco);
    padding: var(--spazio-3) var(--spazio-4);
    border: 0;
    border-bottom: 2px solid var(--colore-su-superficie-variante);
    border-radius: var(--raggio-s) var(--raggio-s) 0 0;
    background: var(--colore-contenitore-superficie-alto);
    color: var(--colore-su-superficie);
    font-size: max(16px, 1rem); /* niente zoom automatico su iOS */
    line-height: 1.4;
    transition: border-color var(--molla-effetti);
  }
  textarea {
    resize: vertical;
  }
  input:focus,
  textarea:focus {
    outline: none;
    border-bottom-color: var(--colore-primario);
    box-shadow: 0 1px 0 0 var(--colore-primario);
  }
  :global([data-tema="alto-contrasto"]) input,
  :global([data-tema="alto-contrasto"]) textarea {
    border: 2px solid var(--colore-contorno);
    border-radius: var(--raggio-s);
  }
  .con-errore input,
  .con-errore textarea {
    border-bottom-color: var(--colore-errore);
    box-shadow: none;
  }
  .con-errore label {
    color: var(--colore-errore);
  }
  .errore {
    font: var(--testo-corpo-piccolo);
    color: var(--colore-errore);
  }
</style>
