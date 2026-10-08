<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { vai, vaiAHome } from "../rotte";
  import { richiestaInterruzione } from "../rotte";
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

<Pagina titolo={t.giocoTitolo} onIndietro={() => (richiestaInterruzione.aperta = true)} onHome={home}>
  {#if partita}
    <div class="corpo">
      <TestoAdattivo testo={t.giocoInizia(partita.giocatori[partita.primoGiocatore])} classe="gioco-primo" />
      <p class="istruzioni">{t.giocoIstruzioni}</p>
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
    </div>
  {/if}

  {#snippet piede()}
    <div class="piede">
      <Pulsante variante="testo" onClick={rivedi}>{t.giocoRivedi}</Pulsante>
      <Pulsante variante="tonale" onClick={() => (chiediRivela = true)}>{t.giocoRivela}</Pulsante>
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
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-2); padding: var(--spazio-4); }
  .corpo :global(.gioco-primo) { font-size: 1.75rem; font-weight: 600; text-align: center; }
  .istruzioni { margin: 0; text-align: center; font-size: 0.875rem; color: var(--colore-su-superficie-variante); }
  h2 { margin: var(--spazio-2) 0 0; font-size: 1rem; font-weight: 600; }
  .elenco { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 6px; }
  .giro {
    position: sticky; top: 0; z-index: 1; padding: var(--spazio-1) 0;
    background: var(--colore-sfondo); color: var(--colore-primario); font-size: 0.875rem; font-weight: 600;
  }
  .voce {
    display: flex; align-items: center; gap: var(--spazio-3);
    padding: var(--spazio-2) var(--spazio-3); border-radius: var(--raggio-m);
    background: var(--colore-superficie-variante); color: var(--colore-su-superficie-variante);
  }
  .voce.primo { background: var(--colore-contenitore-primario); color: var(--colore-su-contenitore-primario); font-weight: 700; }
  .numero {
    flex: none; width: 32px; height: 32px; border-radius: 50%;
    display: grid; place-items: center; font-size: 0.875rem; font-weight: 600;
    background: var(--colore-su-superficie-variante); color: var(--colore-superficie-variante);
  }
  .primo .numero { background: var(--colore-su-contenitore-primario); color: var(--colore-contenitore-primario); }
  .nome { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .piede { display: flex; flex-direction: column; gap: var(--spazio-2); padding: var(--spazio-4); }
</style>
