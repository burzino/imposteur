<script lang="ts">
  import { onMount } from "svelte";
  import { richiediSchermoAcceso, rilasciaSchermoAcceso } from "../browser";
  import Avatar from "../componenti/Avatar.svelte";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import TestoAdattivo from "../componenti/TestoAdattivo.svelte";
  import { getStato } from "../stato.svelte";
  import { indietro, vaiAHome } from "../rotte";
  import { t } from "../testi";
  import DistribuzionePassaggio from "./DistribuzionePassaggio.svelte";
  import DistribuzioneRivelazione from "./DistribuzioneRivelazione.svelte";

  const stato = getStato();
  const partita = $derived(stato.partita);
  const revisione = $derived(stato.revisione);

  onMount(() => {
    void richiediSchermoAcceso();
    return () => rilasciaSchermoAcceso();
  });

  function home() {
    stato.sospendiPartita();
    vaiAHome();
  }
  // Freccia in app: nell'elenco e dopo la rivelazione si esce (indietro() chiude la revisione);
  // dal Passaggio si torna all'elenco.
  function freccia() {
    if (revisione !== null && !revisione.rivelato) stato.tornaAElencoRevisione();
    else indietro();
  }
</script>

{#if partita !== null}
  {#if revisione !== null && revisione.indice >= 0 && revisione.indice < partita.giocatori.length}
    {#if revisione.rivelato}
      {#key revisione.indice}
        <DistribuzioneRivelazione
          titolo={t.rivediTitolo}
          {partita}
          indice={revisione.indice}
          etichettaPulsante={t.rivediNascondi}
          onNascondi={indietro}
          onHome={home}
          onIndietro={freccia}
        />
      {/key}
    {:else}
      {#key revisione.indice}
        <DistribuzionePassaggio
          titolo={t.rivediTitolo}
          {partita}
          indice={revisione.indice}
          mostraFila={false}
          onSono={() => stato.rivelaRevisione()}
          onHome={home}
          onIndietro={freccia}
        />
      {/key}
    {/if}
  {:else}
    <Pagina titolo={t.rivediTitolo} onHome={home} onIndietro={indietro}>
      <div class="intestazione">
        <h2>{t.rivediIntestazione}</h2>
        <p>{t.rivediSottotitolo}</p>
      </div>
      <ul class="griglia">
        {#each partita.giocatori as nome, i (i)}
          <li>
            <button
              type="button"
              class="scheda"
              aria-label={t.rivediCdGiocatore(nome)}
              onclick={() => stato.scegliRevisione(i)}
            >
              <Avatar {nome} indice={i} dimensione={48} />
              <TestoAdattivo testo={nome} classe="rivedi-nome" maxRighe={2} />
            </button>
          </li>
        {/each}
      </ul>
      {#snippet piede()}
        <Pulsante variante="contorno" onClick={indietro}>{t.rivediTornaGioco}</Pulsante>
      {/snippet}
    </Pagina>
  {/if}
{/if}

<style>
  .intestazione h2 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 400;
  }
  .intestazione p {
    margin: var(--spazio-1) 0 0;
    font-size: 1rem;
    color: var(--colore-su-superficie-variante);
  }
  .griglia {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: var(--spazio-3);
    margin: var(--spazio-4) 0;
    padding: 0;
    list-style: none;
  }
  .scheda {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-2);
    box-sizing: border-box;
    width: 100%;
    min-height: 112px;
    padding: var(--spazio-3);
    border: 0;
    border-radius: var(--raggio-m);
    background: var(--colore-superficie-variante);
    color: var(--colore-su-superficie-variante);
    box-shadow: 0 1px 4px rgb(0 0 0 / 0.25);
    font: inherit;
    cursor: pointer;
  }
  .scheda :global(.rivedi-nome) {
    width: 100%;
    text-align: center;
    font-size: 1.125rem;
    font-weight: 500;
  }
  .scheda:focus-visible {
    outline: 3px solid var(--colore-primario);
    outline-offset: 2px;
  }
</style>
