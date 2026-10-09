<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { t } from "../testi";
  import Contatore from "../componenti/Contatore.svelte";
  import Interruttore from "../componenti/Interruttore.svelte";
  import SelettoreModalita from "../componenti/SelettoreModalita.svelte";
  import ConfigurazioneNome from "./ConfigurazioneNome.svelte";
  import PassoIntestazione from "./PassoIntestazione.svelte";
  import { Regole } from "../../game/regole";
  import type { Modalita } from "../../game/modelli";

  const stato = getStato();
  const config = $derived(stato.config);
  const duplicati = $derived(
    new Set<number>(stato.errori.flatMap((e) => (e.tipo === "NomeDuplicato" ? [...e.indici] : []))),
  );
  const maxImpostori = $derived(Regole.maxImpostori(config.numeroGiocatori));
  let elenco: HTMLElement | undefined = $state();

  const modalita = $derived([
    { valore: "SENZA_PAROLA", titolo: t.modalitaSenzaParola, descrizione: t.modalitaSenzaParolaDesc },
    { valore: "PAROLA_AFFINE", titolo: t.modalitaAffine, descrizione: t.modalitaAffineDesc },
  ]);

  function prossimoCampo(i: number) {
    elenco?.querySelector<HTMLInputElement>(`#nome-${i + 1}`)?.focus();
  }
</script>

<div class="corpo">
  <PassoIntestazione titolo={t.passo1Titolo} sottotitolo={t.passo1Sottotitolo} />

  <Contatore
    etichetta={t.configNumeroGiocatori}
    valore={config.numeroGiocatori}
    min={Regole.MIN_GIOCATORI}
    max={Regole.MAX_GIOCATORI}
    onCambia={(n) => stato.impostaNumeroGiocatori(n)}
  />
  <Contatore
    etichetta={config.impostoriSorpresa ? t.configNumeroImpostoriMax : t.configNumeroImpostori}
    valore={config.numeroImpostori}
    min={1}
    max={Math.max(1, maxImpostori)}
    onCambia={(n) => stato.impostaNumeroImpostori(n)}
  />
  <Interruttore
    etichetta={t.opzSorpresa}
    descrizione={t.opzDescSorpresa}
    valore={config.impostoriSorpresa}
    onCambia={(v) => stato.impostaOpzione((c) => ({ ...c, impostoriSorpresa: v }))}
  />

  <section class="sezione">
    <h3>{t.configModalita}</h3>
    <SelettoreModalita
      etichetta={t.configModalita}
      opzioni={modalita}
      valore={config.modalita}
      onCambia={(v) => stato.impostaModalita(v as Modalita)}
    />
  </section>

  <section class="sezione">
    <h3>{t.configNomi}</h3>
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
  </section>
</div>

<style>
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-4); padding-bottom: 96px; }
  .sezione { display: flex; flex-direction: column; gap: var(--spazio-3); }
  h3 { margin: 0; font: var(--testo-etichetta); color: var(--colore-primario); }
  .nomi { display: flex; flex-direction: column; gap: var(--spazio-3); }
</style>
