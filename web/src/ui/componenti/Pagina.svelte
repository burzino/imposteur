<script lang="ts">
  import type { Snippet } from "svelte";
  import BarraApp from "./BarraApp.svelte";

  interface Props {
    titolo: string;
    onHome?: () => void;
    onIndietro?: () => void;
    children: Snippet;
    piede?: Snippet;
    azione?: Snippet;
    /** Fascia fissa sotto la barra (es. indicatore dei passi). */
    testata?: Snippet;
  }
  let { titolo, onHome, onIndietro, children, piede, azione, testata }: Props = $props();

  let scorso = $state(false);
</script>

<div class="pagina">
  <BarraApp {titolo} {onHome} {onIndietro} sollevata={scorso} {azione} />
  {#if testata}
    <div class="testata">
      <div class="colonna">
        {@render testata()}
      </div>
    </div>
  {/if}
  <main class="scorrevole" onscroll={(e) => (scorso = e.currentTarget.scrollTop > 0)}>
    <div class="colonna">
      {@render children()}
    </div>
  </main>
  {#if piede}
    <footer class="piede">
      <div class="colonna">
        {@render piede()}
      </div>
    </footer>
  {/if}
</div>

<style>
  .pagina {
    display: flex;
    flex-direction: column;
    height: 100vh;
    height: 100dvh;
    background: var(--colore-sfondo);
    color: var(--colore-su-sfondo);
  }
  .testata {
    flex: none;
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
  }
  .testata .colonna {
    padding-top: 0;
    padding-bottom: 0;
  }
  .scorrevole {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
  }
  .colonna {
    width: 100%;
    max-width: var(--larghezza-max);
    margin: 0 auto;
    padding: var(--spazio-2) var(--margine-schermata) var(--spazio-4);
  }
  /* Barra azioni: livello 2, raggio superiore 28, sopra gli inset di sistema */
  .piede {
    flex: none;
    background: var(--colore-contenitore-superficie);
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-bottom: 0;
    border-radius: var(--raggio-xl) var(--raggio-xl) 0 0;
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
    padding-bottom: env(safe-area-inset-bottom);
  }
  .piede .colonna {
    padding-top: var(--spazio-4);
    padding-bottom: var(--spazio-4);
  }
  /* senza piede, il contenuto scorrevole rispetta l'area sicura in basso */
  .scorrevole:last-child {
    padding-bottom: env(safe-area-inset-bottom);
  }
  @media (min-width: 600px) {
    .colonna {
      padding-left: var(--spazio-5);
      padding-right: var(--spazio-5);
    }
  }
</style>
