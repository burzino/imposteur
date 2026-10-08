<script lang="ts">
  import { onMount } from "svelte";
  import { richiediSchermoAcceso, rilasciaSchermoAcceso } from "../browser";
  import { getStato } from "../stato.svelte";
  import { richiestaInterruzione, vai, vaiAHome } from "../rotte";
  import { t } from "../testi";
  import DistribuzionePassaggio from "./DistribuzionePassaggio.svelte";
  import DistribuzioneRivelazione from "./DistribuzioneRivelazione.svelte";
  import DistribuzioneTuttiPronti from "./DistribuzioneTuttiPronti.svelte";

  const stato = getStato();
  const DURATA_PRONTI_MS = 1200;

  const partita = $derived(stato.partita);
  const fase = $derived(stato.distribuzione);
  let navigato = false;

  onMount(() => {
    void richiediSchermoAcceso();
    return () => rilasciaSchermoAcceso();
  });

  // "Tutti pronti!": la partita e' gia' in Gioco; si naviga dopo l'overlay o al tocco.
  function termina() {
    if (navigato) return;
    navigato = true;
    vai("gioco", { sostituisci: true });
  }

  $effect(() => {
    if (fase.tipo !== "Gioco") return;
    const id = setTimeout(termina, DURATA_PRONTI_MS);
    return () => clearTimeout(id);
  });

  function home() {
    stato.sospendiPartita();
    vaiAHome();
  }
  function chiediInterruzione() {
    richiestaInterruzione.aperta = true;
  }
</script>

{#if partita !== null}
  {#if fase.tipo === "Passaggio"}
    {#key fase.indice}
      <DistribuzionePassaggio
        titolo={t.distribuzioneTitolo}
        {partita}
        indice={fase.indice}
        onSono={() => stato.avanza(fase)}
        onHome={home}
        onIndietro={chiediInterruzione}
      />
    {/key}
  {:else if fase.tipo === "Rivelazione"}
    {#key fase.indice}
      <DistribuzioneRivelazione
        titolo={t.distribuzioneTitolo}
        {partita}
        indice={fase.indice}
        onNascondi={() => stato.avanza(fase)}
        onHome={home}
        onIndietro={chiediInterruzione}
      />
    {/key}
  {/if}
  {#if fase.tipo === "Gioco"}
    <DistribuzioneTuttiPronti onTocco={termina} />
  {/if}
{/if}
