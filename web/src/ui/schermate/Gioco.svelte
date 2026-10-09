<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { vai, vaiAHome } from "../rotte";
  import { t } from "../testi";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import DialogoConferma from "../componenti/DialogoConferma.svelte";
  import TestoAdattivo from "../componenti/TestoAdattivo.svelte";
  import { ordineDiParola } from "../../game/partita";

  const stato = getStato();
  const partita = $derived(stato.partita);
  const ordine = $derived(partita ? ordineDiParola(partita) : []);
  const giri = $derived(
    partita ? Array.from({ length: Math.min(3, Math.max(1, partita.giriIndizi)) }, (_, i) => i + 1) : [],
  );
  let chiediRivela = $state(false);

  function rivela() {
    chiediRivela = false;
    stato.entraInRivela();
    vai("rivela", { sostituisci: true });
  }

  function rivedi() {
    stato.chiudiRevisione();
    vai("rivedi");
  }

  function home() {
    stato.sospendiPartita();
    vaiAHome();
  }
</script>

<Pagina titolo={t.giocoTitolo} onHome={home}>
  {#if partita}
    <div class="corpo">
      <section class="hero">
        <TestoAdattivo testo={t.giocoInizia(partita.giocatori[partita.primoGiocatore])} classe="gioco-primo" maxRighe={3} />
      </section>
      <p class="istruzioni">{t.giocoIstruzioni}</p>
      <section class="carta">
        <h2>{t.giocoOrdineTitolo}</h2>
        <ol class="elenco">
          {#each giri as giro (giro)}
            {#if partita.giriIndizi > 1}
              <li class="giro" aria-hidden="false">{t.giocoGiroN(giro)}</li>
            {/if}
            {#each ordine as indice, posizione (posizione)}
              {@const primo = giro === 1 && posizione === 0}
              <li class="voce" class:primo>
                <span class="numero" aria-hidden="true">{posizione + 1}</span>
                <span class="nome">{partita.giocatori[indice]}</span>
              </li>
            {/each}
          {/each}
        </ol>
      </section>
    </div>
  {/if}

  {#snippet piede()}
    <div class="piede">
      <Pulsante variante="testo" onClick={rivedi}>{t.giocoRivedi}</Pulsante>
      <Pulsante variante="pieno" onClick={() => (chiediRivela = true)}>{t.giocoRivela}</Pulsante>
    </div>
  {/snippet}
</Pagina>

<DialogoConferma
  aperto={chiediRivela}
  titolo={t.giocoConfermaRivela}
  messaggio={t.giocoConfermaMessaggio}
  etichettaSi={t.giocoConfermaSi}
  etichettaNo={t.giocoConfermaNo}
  onSi={rivela}
  onNo={() => (chiediRivela = false)}
/>

<style>
  .corpo {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-3);
  }
  /* carta in evidenza */
  .hero {
    padding: var(--spazio-6) var(--spazio-5);
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-primario);
    color: var(--colore-su-contenitore-primario);
  }
  .corpo :global(.gioco-primo) {
    font-size: 2.25rem;
    font-weight: 800;
  }
  .istruzioni {
    text-align: center;
    font: var(--testo-corpo-piccolo);
    color: var(--colore-su-superficie-variante);
  }
  .carta {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-3);
    padding: var(--spazio-4) var(--spazio-4) var(--spazio-5);
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
  }
  h2 {
    padding: 0 var(--spazio-1);
    font: var(--testo-titolo-sezione);
  }
  .elenco {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
  }
  .giro {
    padding: var(--spazio-1) var(--spazio-1) 0;
    color: var(--colore-primario);
    font: var(--testo-etichetta);
  }
  .voce {
    display: flex;
    align-items: center;
    gap: var(--spazio-3);
    min-height: var(--altezza-tocco);
    padding: var(--spazio-1) var(--spazio-2);
    border-radius: var(--raggio-m);
    font: var(--testo-corpo);
  }
  .voce.primo {
    background: var(--colore-contenitore-primario);
    color: var(--colore-su-contenitore-primario);
    font-weight: 700;
  }
  .numero {
    flex: none;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: grid;
    place-items: center;
    font: var(--testo-etichetta);
    background: var(--colore-contenitore-secondario);
    color: var(--colore-su-contenitore-secondario);
  }
  .nome {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .piede {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
    align-items: center;
  }
</style>
