<script lang="ts">
  import { fly, fade } from "svelte/transition";
  import { getStato } from "../stato.svelte";
  import { vai, vaiAPasso, indietro, passoCorrente } from "../rotte";
  import { t } from "../testi";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import IndicatorePassi from "../componenti/IndicatorePassi.svelte";
  import PassoGiocatori from "./PassoGiocatori.svelte";
  import PassoOpzioni from "./PassoOpzioni.svelte";
  import PassoCategorie from "./PassoCategorie.svelte";
  import PassoRiepilogo from "./PassoRiepilogo.svelte";
  import { Passi, passoDiPassoConfigurazione, type PassoConfigurazione } from "../../game/passi";
  import type { ErroreConfigurazione } from "../../game/modelli";

  const stato = getStato();
  const PASSI: readonly PassoConfigurazione[] = ["GIOCATORI", "OPZIONI", "CATEGORIE", "RIEPILOGO"];
  const NOMI = [t.passoGiocatori, t.passoOpzioni, t.configCategorie, t.passoRiepilogo];

  const passo = $derived(passoCorrente.valore);
  const errore = $derived(Passi.errorePasso(PASSI[passo - 1], stato.config, stato.categorie));

  // Direzione dello scorrimento: verso destra avanzando, sinistra tornando.
  let ultimo = passoCorrente.valore;
  let direzione = 1;
  $effect.pre(() => {
    direzione = passo >= ultimo ? 1 : -1;
    ultimo = passo;
  });
  const riduci = typeof matchMedia === "function" && matchMedia("(prefers-reduced-motion: reduce)").matches;
  function ingresso(nodo: Element) {
    return riduci ? fade(nodo, { duration: 100 }) : fly(nodo, { x: 48 * direzione, duration: 300, opacity: 0 });
  }

  function testoErrore(e: ErroreConfigurazione): string {
    switch (e.tipo) {
      case "NessunaCategoria": return t.configNessunaCategoria;
      case "PoolVuoto": return t.configPoolVuoto;
      case "NomeDuplicato": return t.configNomeDuplicato;
      default: return t.configNonValida;
    }
  }

  /** "Inizia": configurazione valida -> Distribuzione; altrimenti primo passo non valido, con il suo errore. */
  function inizia() {
    const p = Passi.primoPassoNonValido(stato.config, stato.categorie);
    if (p === null) {
      if (stato.iniziaPartita()) vai("distribuzione");
      return;
    }
    vaiAPasso(passoDiPassoConfigurazione(p), { sostituisci: true });
  }

  function avanti() {
    if (errore === null && passo < 4) vaiAPasso((passo + 1) as 2 | 3 | 4);
  }
</script>

{#key passo}
  <Pagina titolo={t.configTitolo} onIndietro={indietro}>
    {#snippet azione()}
      {#if passo < 4}
        <button type="button" class="inizia-barra" onclick={inizia}>{t.configInizia}</button>
      {/if}
    {/snippet}
    {#snippet testata()}
      <div class="indicatore">
        <IndicatorePassi
          nomi={NOMI}
          corrente={passo}
          etichetta={t.passoEtichetta(passo, 4, NOMI[passo - 1])}
          descrizione={t.passoEtichettaCd(passo, 4, NOMI[passo - 1])}
          onVai={(k) => vaiAPasso(k as 1 | 2 | 3 | 4)}
        />
      </div>
    {/snippet}

    <div class="passo" in:ingresso>
      {#if passo === 1}
        <PassoGiocatori />
      {:else if passo === 2}
        <PassoOpzioni />
      {:else if passo === 3}
        <PassoCategorie />
      {:else}
        <PassoRiepilogo />
      {/if}
    </div>

    {#snippet piede()}
      <div class="piede">
        {#if errore !== null}
          <p class="errore" role="alert">{testoErrore(errore)}</p>
        {/if}
        <div class="pulsanti">
          {#if passo > 1}
            <Pulsante variante="contorno" onClick={indietro}>{t.indietro}</Pulsante>
          {/if}
          {#if passo < 4}
            <Pulsante variante="pieno" disabilitato={errore !== null} onClick={avanti}>{t.passoAvanti}</Pulsante>
          {:else}
            <Pulsante variante="pieno" onClick={inizia}>{t.configInizia}</Pulsante>
          {/if}
        </div>
      </div>
    {/snippet}
  </Pagina>
{/key}

<style>
  .indicatore { margin: 0 calc(-1 * var(--margine-schermata)); }
  .piede { display: flex; flex-direction: column; gap: var(--spazio-2); }
  .pulsanti { display: flex; gap: var(--spazio-2); }
  .pulsanti :global(.pulsante) { flex: 2; }
  .pulsanti :global(.pulsante.contorno) { flex: 1; }
  .errore { margin: 0; text-align: center; color: var(--colore-errore); font: var(--testo-corpo-piccolo); }
  .inizia-barra {
    flex: none; min-height: var(--altezza-tocco); padding: 0 var(--spazio-4);
    border: 0; border-radius: var(--raggio-pieno); background: transparent;
    color: var(--colore-primario); font: var(--testo-titolo); cursor: pointer;
  }
  .inizia-barra:hover { background: color-mix(in srgb, currentColor 10%, transparent); }
  .inizia-barra:focus-visible { outline: 3px solid var(--colore-primario); outline-offset: 2px; }
</style>
