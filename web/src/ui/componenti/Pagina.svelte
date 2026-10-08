<script lang="ts">
  import type { Snippet } from "svelte";
  import BarraApp from "./BarraApp.svelte";

  interface Props {
    titolo: string;
    onHome?: () => void;
    onIndietro?: () => void;
    children: Snippet;
    piede?: Snippet;
  }
  let { titolo, onHome, onIndietro, children, piede }: Props = $props();
</script>

<div class="pagina">
  <BarraApp {titolo} {onHome} {onIndietro} />
  <main class="scorrevole">
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
    padding: var(--spazio-2) var(--spazio-4) var(--spazio-4);
  }
  .piede {
    flex: none;
    background: var(--colore-sfondo);
    border-top: 1px solid var(--colore-contorno-variante);
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
    padding-bottom: env(safe-area-inset-bottom);
  }
  .piede .colonna {
    padding-top: var(--spazio-3);
    padding-bottom: var(--spazio-3);
  }
  /* senza piede, il contenuto scorrevole rispetta l'area sicura in basso */
  .scorrevole:last-child {
    padding-bottom: env(safe-area-inset-bottom);
  }
</style>
