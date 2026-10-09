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
          mostraFila={false}
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
      <ul class="elenco">
        {#each partita.giocatori as nome, i (i)}
          <li>
            <button
              type="button"
              class="riga"
              aria-label={t.rivediCdGiocatore(nome)}
              onclick={() => stato.scegliRevisione(i)}
            >
              <Avatar {nome} indice={i} stato="pieno" dimensione={40} />
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
  .intestazione {
    padding: var(--spazio-2) 0;
  }
  .intestazione h2 {
    font: var(--testo-titolo-sezione);
  }
  .intestazione p {
    margin-top: var(--spazio-1);
    font: var(--testo-corpo-piccolo);
    color: var(--colore-su-superficie-variante);
  }
  .elenco {
    display: flex;
    flex-direction: column;
    margin: 0 0 var(--spazio-4);
    padding: var(--spazio-2);
    list-style: none;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
  }
  .riga {
    display: flex;
    align-items: center;
    gap: var(--spazio-4);
    width: 100%;
    min-height: 64px;
    padding: 0 var(--spazio-3);
    border: 0;
    border-radius: var(--raggio-l);
    background: transparent;
    color: var(--colore-su-superficie);
    font: var(--testo-titolo);
    text-align: left;
    cursor: pointer;
  }
  .riga:hover {
    background: color-mix(in srgb, var(--colore-su-superficie) 8%, transparent);
  }
  .riga :global(.rivedi-nome) {
    flex: 1;
    min-width: 0;
    text-align: left;
  }
</style>
