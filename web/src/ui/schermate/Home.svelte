<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { vai } from "../rotte";
  import { t } from "../testi";
  import Pulsante from "../componenti/Pulsante.svelte";
  import DialogoConferma from "../componenti/DialogoConferma.svelte";

  const stato = getStato();
  let chiediNuova = $state(false);

  function nuova() {
    vai("configurazione");
  }
  function riprendi() {
    const g = stato.riprendiPartita();
    vai(g ? "gioco" : "distribuzione");
  }
  function toccaNuova() {
    if (stato.ripristinabile !== null) chiediNuova = true;
    else nuova();
  }
</script>

<div class="home">
  <div class="contenuto">
    <h1>{t.homeTitolo}</h1>
    {#if stato.erroreCaricamento}
      <p class="errore">{t.erroreCaricamentoParole}</p>
    {/if}
    {#if stato.ripristinabile !== null}
      <Pulsante variante="pieno" onClick={riprendi}>{t.riprendiPartita}</Pulsante>
    {/if}
    <Pulsante variante="pieno" disabilitato={stato.caricamento || stato.erroreCaricamento} onClick={toccaNuova}>
      {t.nuovaPartita}
    </Pulsante>
    <Pulsante variante="contorno" onClick={() => vai("regole")}>{t.comeSiGioca}</Pulsante>
  </div>
  <!-- Dopo il contenuto, così sta sopra e riceve i tocchi -->
  <button class="impostazioni" type="button" aria-label={t.impostazioniApri} onclick={() => vai("impostazioni")}>
    <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" fill="currentColor">
      <path d="M19.14 12.94c.04-.3.06-.61.06-.94s-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.49.49 0 0 0-.59-.22l-2.39.96a7.03 7.03 0 0 0-1.62-.94l-.36-2.54a.48.48 0 0 0-.48-.41h-3.84a.48.48 0 0 0-.48.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96a.48.48 0 0 0-.59.22L2.74 8.87a.48.48 0 0 0 .12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.48-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.49.49 0 0 0-.12-.61l-2.01-1.58ZM12 15.6A3.6 3.6 0 1 1 12 8.4a3.6 3.6 0 0 1 0 7.2Z" />
    </svg>
  </button>
</div>

<DialogoConferma
  aperto={chiediNuova}
  titolo={t.partitaInCorsoConferma}
  etichettaSi={t.nuovaPartita}
  etichettaNo={t.annulla}
  onSi={() => {
    chiediNuova = false;
    nuova();
  }}
  onNo={() => (chiediNuova = false)}
/>

<style>
  .home {
    position: relative;
    min-height: 100dvh;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: var(--spazio-5);
    box-sizing: border-box;
    background: var(--colore-sfondo);
    color: var(--colore-su-sfondo);
  }
  .contenuto {
    width: 100%;
    max-width: var(--larghezza-max);
    display: flex;
    flex-direction: column;
    align-items: stretch;
    gap: var(--spazio-4);
    text-align: center;
  }
  h1 {
    margin: 0;
    font-size: 2.8rem;
    font-weight: 400;
    color: var(--colore-primario);
  }
  .errore {
    margin: 0;
    color: var(--colore-errore);
    font-size: 1rem;
  }
  .impostazioni {
    position: absolute;
    top: var(--spazio-2);
    right: var(--spazio-2);
    width: var(--altezza-tocco);
    height: var(--altezza-tocco);
    display: grid;
    place-items: center;
    border: none;
    border-radius: 50%;
    background: transparent;
    color: var(--colore-su-sfondo);
    cursor: pointer;
  }
  .impostazioni:focus-visible {
    outline: 2px solid var(--colore-primario);
  }
</style>
