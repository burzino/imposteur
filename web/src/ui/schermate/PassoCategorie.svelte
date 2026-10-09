<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { t } from "../testi";
  import Pulsante from "../componenti/Pulsante.svelte";
  import DialogoConferma from "../componenti/DialogoConferma.svelte";
  import PassoIntestazione from "./PassoIntestazione.svelte";

  const stato = getStato();
  const config = $derived(stato.config);
  let chiediAzzera = $state(false);
</script>

<div class="corpo">
  <PassoIntestazione titolo={t.passo3Titolo} sottotitolo={t.passo3Sottotitolo} />

  <div class="azioni">
    <Pulsante variante="contorno" onClick={() => stato.selezionaTutte(true)}>{t.configSelezionaTutte}</Pulsante>
    <Pulsante variante="contorno" onClick={() => stato.selezionaTutte(false)}>{t.configDeselezionaTutte}</Pulsante>
  </div>
  <div class="rimanenti">
    <span>{t.configParoleRimanenti(stato.paroleRimanenti, stato.paroleTotali)}</span>
    <div class="azzera">
      <Pulsante
        variante="testo"
        disabilitato={stato.paroleRimanenti >= stato.paroleTotali}
        onClick={() => (chiediAzzera = true)}>{t.configAzzera}</Pulsante
      >
    </div>
  </div>
  <div class="elenco">
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
  </div>
</div>

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
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-3); padding-bottom: 96px; }
  .azioni { display: flex; flex-wrap: wrap; gap: var(--spazio-2); }
  .azioni :global(.pulsante) { width: auto; flex: 1; }
  .rimanenti {
    display: flex; align-items: center; justify-content: space-between; gap: var(--spazio-2);
    font: var(--testo-corpo-piccolo); color: var(--colore-su-superficie-variante);
  }
  .azzera :global(.pulsante) { width: auto; }
  .elenco { display: flex; flex-direction: column; }
  .riga {
    display: flex; align-items: center; gap: var(--spazio-3);
    min-height: var(--altezza-tocco); cursor: pointer; font: var(--testo-corpo);
  }
  .riga input { width: 22px; height: 22px; accent-color: var(--colore-primario); flex: none; }
</style>
