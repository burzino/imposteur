<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { vai, vaiAHome, indietro } from "../rotte";
  import { t } from "../testi";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import Contatore from "../componenti/Contatore.svelte";
  import DialogoConferma from "../componenti/DialogoConferma.svelte";
  import ConfigurazioneNome from "./ConfigurazioneNome.svelte";
  import OpzioniAvanzate from "./OpzioniAvanzate.svelte";
  import { Regole } from "../../game/regole";
  import type { ErroreConfigurazione } from "../../game/modelli";

  const stato = getStato();
  const config = $derived(stato.config);
  const errori = $derived(stato.errori);
  const duplicati = $derived(
    new Set<number>(errori.flatMap((e) => (e.tipo === "NomeDuplicato" ? [...e.indici] : []))),
  );
  const maxImpostori = $derived(Regole.maxImpostori(config.numeroGiocatori));
  const nessunaCategoria = $derived(errori.some((e) => e.tipo === "NessunaCategoria"));
  const poolVuoto = $derived(errori.some((e) => e.tipo === "PoolVuoto"));
  let chiediAzzera = $state(false);
  let elenco: HTMLElement | undefined = $state();

  function testoErrore(e: ErroreConfigurazione): string {
    switch (e.tipo) {
      case "NessunaCategoria": return t.configNessunaCategoria;
      case "PoolVuoto": return t.configPoolVuoto;
      case "NomeDuplicato": return t.configNomeDuplicato;
      default: return t.configNonValida;
    }
  }

  function prossimoCampo(i: number) {
    elenco?.querySelector<HTMLInputElement>(`#nome-${i + 1}`)?.focus();
  }

  function inizia() {
    if (stato.iniziaPartita()) vai("distribuzione");
  }

  function home() {
    stato.salvaOra();
    vaiAHome();
  }
</script>

<Pagina titolo={t.configTitolo} onIndietro={indietro} onHome={home}>
  <div class="corpo">
    <Contatore
      etichetta={t.configNumeroGiocatori}
      valore={config.numeroGiocatori}
      min={Regole.MIN_GIOCATORI}
      max={Regole.MAX_GIOCATORI}
      onCambia={(n) => stato.impostaNumeroGiocatori(n)}
    />

    <h2>{t.configNomi}</h2>
    <div class="nomi" bind:this={elenco}>
      {#each Array.from({ length: config.numeroGiocatori }, (_, i) => i) as i (i)}
        <ConfigurazioneNome
          indice={i}
          valore={config.nomi[i] ?? ""}
          duplicato={duplicati.has(i)}
          ultimo={i === config.numeroGiocatori - 1}
          onCambia={(v) => stato.impostaNome(i, v)}
          onProssimo={() => prossimoCampo(i)}
        />
      {/each}
    </div>

    <hr />
    <Contatore
      etichetta={config.impostoriSorpresa ? t.configNumeroImpostoriMax : t.configNumeroImpostori}
      valore={config.numeroImpostori}
      min={1}
      max={maxImpostori}
      onCambia={(n) => stato.impostaNumeroImpostori(n)}
    />

    <hr />
    <h2>{t.configModalita}</h2>
    <div role="radiogroup" aria-label={t.configModalita} class="radio">
      <label class="riga">
        <input
          type="radio"
          name="modalita"
          checked={config.modalita === "SENZA_PAROLA"}
          onchange={() => stato.impostaModalita("SENZA_PAROLA")}
        />
        <span>{t.modalitaSenzaParola}</span>
      </label>
      <label class="riga">
        <input
          type="radio"
          name="modalita"
          checked={config.modalita === "PAROLA_AFFINE"}
          onchange={() => stato.impostaModalita("PAROLA_AFFINE")}
        />
        <span>{t.modalitaAffine}</span>
      </label>
    </div>

    <hr />
    <h2>{t.configCategorie}</h2>
    <div class="azioni">
      <Pulsante variante="contorno" onClick={() => stato.selezionaTutte(true)}>{t.configSelezionaTutte}</Pulsante>
      <Pulsante variante="contorno" onClick={() => stato.selezionaTutte(false)}>{t.configDeselezionaTutte}</Pulsante>
    </div>
    <div class="rimanenti">
      <span>{t.configParoleRimanenti(stato.paroleRimanenti, stato.paroleTotali)}</span>
      <Pulsante
        variante="testo"
        disabilitato={stato.paroleRimanenti >= stato.paroleTotali}
        onClick={() => (chiediAzzera = true)}>{t.configAzzera}</Pulsante
      >
    </div>
    {#each stato.categorie as categoria (categoria.id)}
      <label class="riga">
        <input
          type="checkbox"
          checked={config.categorieSelezionate.has(categoria.id)}
          onchange={(e) => stato.impostaCategoria(categoria.id, e.currentTarget.checked)}
        />
        <span>{categoria.nome}</span>
      </label>
    {/each}
    {#if nessunaCategoria}
      <p class="errore">{t.configNessunaCategoria}</p>
    {:else if poolVuoto}
      <p class="errore">{t.configPoolVuoto}</p>
    {/if}

    <OpzioniAvanzate {config} onCambia={(f) => stato.impostaOpzione(f)} />
  </div>

  {#snippet piede()}
    <div class="piede">
      {#if !stato.puoIniziare && errori.length > 0}
        <p class="errore" role="alert">{testoErrore(errori[0])}</p>
      {/if}
      <Pulsante variante="pieno" disabilitato={!stato.puoIniziare} onClick={inizia}>{t.configInizia}</Pulsante>
    </div>
  {/snippet}
</Pagina>

<DialogoConferma
  aperto={chiediAzzera}
  titolo={t.configAzzeraConferma}
  etichettaSi={t.configAzzera}
  etichettaNo={t.annulla}
  onSi={() => {
    chiediAzzera = false;
    stato.azzeraParole();
  }}
  onNo={() => (chiediAzzera = false)}
/>

<style>
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-3); padding: var(--spazio-2) var(--spazio-4); }
  h2 { margin: 0; font-size: 1rem; font-weight: 600; color: var(--colore-primario); }
  hr { width: 100%; border: 0; border-top: 1px solid var(--colore-contorno-variante); margin: 0; }
  .nomi { display: flex; flex-direction: column; gap: var(--spazio-3); }
  .radio { display: flex; flex-direction: column; }
  .riga {
    display: flex; align-items: center; gap: var(--spazio-3);
    min-height: var(--altezza-tocco); cursor: pointer;
  }
  .riga input { width: 22px; height: 22px; accent-color: var(--colore-primario); flex: none; }
  .azioni { display: flex; flex-wrap: wrap; gap: var(--spazio-2); }
  .rimanenti { display: flex; align-items: center; justify-content: space-between; gap: var(--spazio-2); font-size: 0.875rem; }
  .errore { margin: 0; color: var(--colore-errore); font-size: 0.875rem; }
  .piede { display: flex; flex-direction: column; gap: var(--spazio-2); padding: var(--spazio-4); }
</style>
