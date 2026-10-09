<script lang="ts">
  interface Props {
    aperto: boolean;
    titolo: string;
    messaggio?: string;
    etichettaSi: string;
    etichettaNo: string;
    onSi: () => void;
    onNo: () => void;
  }
  let { aperto, titolo, messaggio, etichettaSi, etichettaNo, onSi, onNo }: Props = $props();

  let dialogo: HTMLDialogElement | undefined = $state();
  let pulsanteNo: HTMLButtonElement | undefined = $state();
  const id = $props.id();

  $effect(() => {
    const d = dialogo;
    if (!d) return;
    if (aperto && !d.open) {
      d.showModal();
      pulsanteNo?.focus();
    } else if (!aperto && d.open) {
      d.close();
    }
  });

  $effect(() => {
    const d = dialogo;
    return () => {
      if (d?.open) d.close();
    };
  });

  function allaChiusuraRichiesta(e: Event): void {
    // Esc: la chiusura la decide il genitore tramite `aperto`
    e.preventDefault();
    onNo();
  }

  function alClick(e: MouseEvent): void {
    // click sullo sfondo (::backdrop): il bersaglio e' il <dialog> stesso
    if (e.target === dialogo) onNo();
  }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
<dialog
  bind:this={dialogo}
  class="dialogo"
  aria-labelledby="{id}-titolo"
  aria-describedby={messaggio ? `${id}-messaggio` : undefined}
  oncancel={allaChiusuraRichiesta}
  onclick={alClick}
>
  <div class="contenuto">
    <h2 id="{id}-titolo">{titolo}</h2>
    {#if messaggio}
      <p id="{id}-messaggio">{messaggio}</p>
    {/if}
    <div class="azioni">
      <button bind:this={pulsanteNo} type="button" class="azione" onclick={() => onNo()}>{etichettaNo}</button>
      <button type="button" class="azione" onclick={() => onSi()}>{etichettaSi}</button>
    </div>
  </div>
</dialog>

<style>
  .dialogo {
    width: min(calc(100vw - 2 * var(--spazio-5)), 400px);
    max-height: calc(100dvh - 2 * var(--spazio-5));
    padding: 0;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-alto);
    color: var(--colore-su-superficie);
    overflow: auto;
  }
  .dialogo[open] {
    animation: entra var(--molla-spaziale) both;
  }
  .dialogo::backdrop {
    background: var(--colore-scrim);
  }
  .contenuto {
    padding: var(--spazio-5);
  }
  h2 {
    font: var(--testo-titolo-sezione);
  }
  p {
    margin-top: var(--spazio-3);
    font: var(--testo-corpo);
    color: var(--colore-su-superficie-variante);
  }
  .azioni {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: var(--spazio-2);
    margin-top: var(--spazio-5);
  }
  .azione {
    min-height: var(--altezza-tocco);
    padding: 0 var(--spazio-4);
    border: 0;
    border-radius: var(--raggio-pieno);
    background: transparent;
    color: var(--colore-primario);
    font: var(--testo-titolo);
    cursor: pointer;
  }
  .azione:hover {
    background: color-mix(in srgb, var(--colore-primario) 10%, transparent);
  }
  @keyframes entra {
    from {
      opacity: 0;
      transform: scale(0.9);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }
</style>
