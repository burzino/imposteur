<script lang="ts">
  import Avatar from "../componenti/Avatar.svelte";

  // Decorativa: l'informazione e' nel testo "Giocatore n di N". Solo l'iniziale, nessun ruolo.
  let { giocatori, corrente }: { giocatori: readonly string[]; corrente: number } = $props();

  let contenitore: HTMLElement | undefined = $state();
  const scorre = $derived(giocatori.length > 8);

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
  {#each giocatori as nome, i (i)}
    <Avatar
      {nome}
      indice={i}
      stato={i < corrente ? "fatto" : i === corrente ? "corrente" : "attesa"}
      dimensione={i === corrente ? 36 : 30}
    />
  {/each}
</div>

<style>
  .fila {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-2);
    width: 100%;
    min-height: 40px;
  }
  .fila.scorre {
    justify-content: flex-start;
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
