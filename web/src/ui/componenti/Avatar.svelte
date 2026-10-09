<script lang="ts">
  interface Props {
    nome: string;
    indice: number;
    /** attesa = opacita 38%; corrente = anello + scala 1,15; fatto = spunta; pieno = nessun decoro */
    stato?: "attesa" | "corrente" | "fatto" | "pieno";
    /** 40 (riga) o 96 (grande, senza anello) */
    dimensione?: number;
  }
  let { nome, indice, stato = "attesa", dimensione = 40 }: Props = $props();

  const iniziale = $derived.by(() => {
    const prima = Array.from(nome.trim())[0];
    return prima ? prima.toLocaleUpperCase("it") : String(indice + 1);
  });
  const colore = $derived(((indice % 8) + 8) % 8);
</script>

<span
  class="avatar {stato}"
  role="img"
  aria-label={nome}
  style:--d="{dimensione}px"
  style:background="var(--colore-avatar-{colore})"
>
  {#if stato === "fatto"}
    <svg class="spunta" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path fill="#fff" d="m9.55 17.65-4.4-4.4 1.4-1.4 3 3 7.9-7.9 1.4 1.4z" />
    </svg>
  {:else}
    <span class="iniziale" aria-hidden="true">{iniziale}</span>
  {/if}
</span>

<style>
  .avatar {
    position: relative;
    flex: none;
    display: inline-grid;
    place-items: center;
    width: var(--d);
    height: var(--d);
    border-radius: var(--raggio-pieno);
    color: var(--colore-su-avatar);
    font-size: calc(var(--d) * 0.42);
    font-weight: 700;
    line-height: 1;
    user-select: none;
    -webkit-user-select: none;
    transition:
      transform var(--molla-spaziale),
      opacity var(--molla-effetti),
      box-shadow var(--molla-effetti);
  }
  .attesa {
    opacity: 0.38;
  }
  /* anello da 3 px, staccato di 3 px dall'avatar */
  .corrente {
    transform: scale(1.15);
    box-shadow:
      0 0 0 3px var(--colore-sfondo),
      0 0 0 6px var(--colore-primario);
  }
  .spunta {
    width: 16px;
    height: 16px;
  }
</style>
