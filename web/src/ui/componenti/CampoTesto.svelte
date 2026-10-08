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
    font-size: 0.875rem;
    color: var(--colore-su-superficie-variante);
  }
  input,
  textarea {
    width: 100%;
    min-height: var(--altezza-tocco);
    padding: var(--spazio-3) var(--spazio-4);
    border: 1px solid var(--colore-contorno);
    border-radius: var(--raggio-s);
    background: var(--colore-superficie);
    color: var(--colore-su-superficie);
    font-size: max(16px, 1rem); /* niente zoom automatico su iOS */
    line-height: 1.4;
  }
  textarea {
    resize: vertical;
  }
  input:focus,
  textarea:focus {
    outline: 2px solid var(--colore-primario);
    outline-offset: 0;
    border-color: var(--colore-primario);
  }
  .con-errore input,
  .con-errore textarea {
    border-color: var(--colore-errore);
  }
  .con-errore label {
    color: var(--colore-errore);
  }
  .errore {
    font-size: 0.875rem;
    color: var(--colore-errore);
  }
</style>
