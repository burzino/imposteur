<script lang="ts">
  import Avatar from "../componenti/Avatar.svelte";

  // Decorativa: l'informazione e nel testo "Giocatore n di N". Solo l'iniziale, nessun ruolo.
  // `ordine` = indici dei giocatori nell'ordine di parola; `corrente` = posizione nella sequenza.
  let {
    giocatori,
    ordine,
    corrente,
  }: { giocatori: readonly string[]; ordine: readonly number[]; corrente: number } = $props();

  let contenitore: HTMLElement | undefined = $state();
  const scorre = $derived(ordine.length > 6);

  $effect(() => {
    const i = corrente;
    const c = contenitore;
    if (!c || !scorre) return;
    const el = c.children[i] as HTMLElement | undefined;
    if (!el) return;
    const ridotto =
      typeof matchMedia === "function" && matchMedia("(prefers-reduced-motion: reduce)").matches;
    c.scrollTo({
      left: el.offsetLeft - (c.clientWidth / 2 - el.offsetWidth / 2),
      behavior: ridotto ? "auto" : "smooth",
    });
  });
</script>

<div class="fila" class:scorre bind:this={contenitore} aria-hidden="true">
  {#each ordine as g, i (i)}
    <Avatar
      nome={giocatori[g] ?? ""}
      indice={g}
      stato={i < corrente ? "fatto" : i === corrente ? "corrente" : "attesa"}
      dimensione={40}
    />
  {/each}
</div>

<style>
  /* il padding verticale lascia posto all'anello e alla scala 1,15 dell'avatar corrente */
  .fila {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: safe center;
    gap: var(--spazio-3);
    width: 100%;
    padding-block: 10px;
  }
  .fila.scorre {
    justify-content: safe center;
    overflow-x: auto;
    padding-inline: var(--spazio-4);
    scrollbar-width: none;
  }
  .fila.scorre::-webkit-scrollbar {
    display: none;
  }
  .fila :global(> *) {
    flex: none;
  }
</style>
