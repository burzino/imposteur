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
      <section class="carta">
        <h2>{s.titolo}</h2>
        <p>{s.testo}</p>
      </section>
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
    gap: var(--spazio-3);
  }
  .carta {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
    padding: var(--spazio-5) 20px;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
  }
  h2 {
    font: var(--testo-titolo-sezione);
  }
  p {
    font: var(--testo-corpo);
    white-space: pre-line;
  }
  .ios {
    padding: 0 var(--spazio-2);
    font: var(--testo-corpo-piccolo);
    color: var(--colore-su-superficie-variante);
  }
</style>
