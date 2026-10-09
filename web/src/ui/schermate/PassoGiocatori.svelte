<script lang="ts">
  import { tick } from "svelte";
  import { getStato } from "../stato.svelte";
  import { t } from "../testi";
  import RigaGiocatore from "../componenti/RigaGiocatore.svelte";
  import PassoIntestazione from "./PassoIntestazione.svelte";
  import { Passi } from "../../game/passi";

  const stato = getStato();
  const config = $derived(stato.config);
  const duplicati = $derived(
    new Set<number>(stato.errori.flatMap((e) => (e.tipo === "NomeDuplicato" ? [...e.indici] : []))),
  );
  const puoAggiungere = $derived(Passi.puoAggiungereGiocatore(config));
  const puoRimuovere = $derived(Passi.puoRimuovereGiocatore(config));
  let elenco: HTMLElement | undefined = $state();

  function campo(i: number): HTMLInputElement | null | undefined {
    return elenco?.querySelector<HTMLInputElement>(`#nome-${i}`);
  }

  async function aggiungi() {
    if (!puoAggiungere) return;
    stato.aggiungiGiocatore();
    await tick();
    // il fuoco porta il campo nuovo in vista sopra la barra azioni e la tastiera
    campo(stato.config.numeroGiocatori - 1)?.focus();
  }
</script>

<div class="corpo">
  <PassoIntestazione titolo={t.passo1Titolo} sottotitolo={t.passo1Sottotitolo} />

  <div class="nomi" bind:this={elenco}>
    {#each Array.from({ length: config.numeroGiocatori }, (_, i) => i) as i (i)}
      <RigaGiocatore
        indice={i}
        valore={config.nomi[i] ?? ""}
        duplicato={duplicati.has(i)}
        ultimo={i === config.numeroGiocatori - 1}
        {puoRimuovere}
        onCambia={(v) => stato.impostaNome(i, v)}
        onRimuovi={() => stato.rimuoviGiocatore(i)}
        onProssimo={() => campo(i + 1)?.focus()}
      />
    {/each}
  </div>

  <button type="button" class="aggiungi" disabled={!puoAggiungere} onclick={aggiungi}>
    {puoAggiungere ? t.giocatoriAggiungi : t.giocatoriMassimo}
  </button>
</div>

<style>
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-4); padding-bottom: 96px; }
  .nomi { display: flex; flex-direction: column; gap: var(--spazio-2); }
  .aggiungi {
    align-self: flex-start; min-height: var(--altezza-tocco); padding: 0 var(--spazio-4);
    border: 0; border-radius: var(--raggio-pieno); background: transparent;
    color: var(--colore-primario); font: var(--testo-titolo); cursor: pointer;
  }
  .aggiungi:hover:enabled { background: color-mix(in srgb, currentColor 10%, transparent); }
  .aggiungi:focus-visible { outline: 3px solid var(--colore-primario); outline-offset: 2px; }
  .aggiungi:disabled { color: color-mix(in srgb, var(--colore-su-superficie) 38%, transparent); cursor: default; }
</style>
