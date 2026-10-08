<script lang="ts">
  import { onMount } from "svelte";
  import DialogoConferma from "./ui/componenti/DialogoConferma.svelte";
  import Toast from "./ui/componenti/Toast.svelte";
  import { avviaProtezioneRuolo } from "./ui/browser";
  import { avviaRotte, richiestaInterruzione, rottaCorrente, vaiAConfigurazione } from "./ui/rotte";
  import Configurazione from "./ui/schermate/Configurazione.svelte";
  import Distribuzione from "./ui/schermate/Distribuzione.svelte";
  import Gioco from "./ui/schermate/Gioco.svelte";
  import Home from "./ui/schermate/Home.svelte";
  import Impostazioni from "./ui/schermate/Impostazioni.svelte";
  import Regole from "./ui/schermate/Regole.svelte";
  import Rivedi from "./ui/schermate/Rivedi.svelte";
  import Rivela from "./ui/schermate/Rivela.svelte";
  import { getStato } from "./ui/stato.svelte";
  import { t } from "./ui/testi";

  const stato = getStato();

  onMount(() => {
    const rimuoviRotte = avviaRotte(stato);
    const rimuoviProtezione = avviaProtezioneRuolo(stato);
    return () => {
      rimuoviRotte();
      rimuoviProtezione();
    };
  });

  function interrompi(): void {
    richiestaInterruzione.aperta = false;
    stato.terminaPartita();
    vaiAConfigurazione();
  }

  const rotta = $derived(rottaCorrente.valore);
</script>

{#if stato.caricamento}
  <div class="attesa" aria-busy="true"></div>
{:else if stato.erroreCaricamento}
  <div class="errore" role="alert">{t.erroreCaricamentoParole}</div>
{:else if rotta === "home"}
  <Home />
{:else if rotta === "regole"}
  <Regole />
{:else if rotta === "impostazioni"}
  <Impostazioni />
{:else if rotta === "configurazione"}
  <Configurazione />
{:else if rotta === "distribuzione"}
  <Distribuzione />
{:else if rotta === "gioco"}
  <Gioco />
{:else if rotta === "rivedi"}
  <Rivedi />
{:else if rotta === "rivela"}
  <Rivela />
{/if}

<DialogoConferma
  aperto={richiestaInterruzione.aperta}
  titolo={t.interromperePartita}
  messaggio={t.interrompereMessaggio}
  etichettaSi={t.interrompereConferma}
  etichettaNo={t.interrompereContinua}
  onSi={interrompi}
  onNo={() => (richiestaInterruzione.aperta = false)}
/>

<Toast />

<style>
  .attesa {
    min-height: 100dvh;
  }
  .errore {
    display: grid;
    place-items: center;
    min-height: 100dvh;
    padding: var(--spazio-4);
    text-align: center;
    color: var(--colore-errore);
  }
</style>
