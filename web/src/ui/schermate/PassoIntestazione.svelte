<script lang="ts">
  import type { Snippet } from "svelte";

  interface Props {
    titolo: string;
    sottotitolo: string;
    /** Elemento a destra del titolo (es. badge). */
    lato?: Snippet;
  }
  let { titolo, sottotitolo, lato }: Props = $props();
</script>

<header class="intestazione">
  <div class="riga">
    <h2>{titolo}</h2>
    {#if lato}{@render lato()}{/if}
  </div>
  <p>{sottotitolo}</p>
</header>

<style>
  .intestazione { display: flex; flex-direction: column; gap: var(--spazio-1); }
  .riga { display: flex; align-items: center; justify-content: space-between; gap: var(--spazio-3); }
  h2 { margin: 0; font: var(--testo-titolo-sezione); color: var(--colore-su-sfondo); }
  p { margin: 0; font: var(--testo-corpo); color: var(--colore-su-superficie-variante); }
</style>
