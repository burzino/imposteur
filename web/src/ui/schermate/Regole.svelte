<script lang="ts">
  import { indietro, vaiAHome } from "../rotte";
  import { t } from "../testi";
  import { eIosSafariNonInstallato } from "../browser";
  import Pagina from "../componenti/Pagina.svelte";

  const sezioni = [
    { titolo: t.regoleModalitaSenzaParolaTitolo, testo: t.regoleModalitaSenzaParola },
    { titolo: t.regoleModalitaAffineTitolo, testo: t.regoleModalitaAffine },
    { titolo: t.regoleFineTitolo, testo: t.regoleFine },
  ];
  const mostraIos = eIosSafariNonInstallato();
</script>

<Pagina titolo={t.comeSiGioca} onIndietro={indietro} onHome={vaiAHome}>
  <div class="regole">
    {#each sezioni as s}
      <h2>{s.titolo}</h2>
      <p>{s.testo}</p>
    {/each}
    {#if mostraIos}
      <p class="ios">{t.installaIos}</p>
    {/if}
  </div>
</Pagina>

<style>
  .regole {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
    padding: var(--spazio-4);
  }
  h2 {
    margin: 0;
    font-size: 1.375rem;
    font-weight: 400;
    color: var(--colore-primario);
  }
  p {
    margin: 0;
    font-size: 1rem;
    line-height: 1.5;
    white-space: pre-line;
  }
  .ios {
    margin-top: var(--spazio-2);
    color: var(--colore-su-superficie-variante);
  }
</style>
