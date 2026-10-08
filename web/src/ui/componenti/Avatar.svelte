<script lang="ts">
  interface Props {
    nome: string;
    indice: number;
    stato?: "attesa" | "corrente" | "fatto";
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
  <span class="iniziale" aria-hidden="true">{iniziale}</span>
  {#if stato === "fatto"}
    <svg class="spunta" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <circle cx="12" cy="12" r="12" fill="var(--colore-sfondo)" />
      <path fill="var(--colore-primario)" d="m9.55 17.65-4.4-4.4 1.4-1.4 3 3 7.9-7.9 1.4 1.4z" />
    </svg>
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
    border-radius: 50%;
    color: var(--colore-su-avatar);
    font-size: calc(var(--d) * 0.45);
    font-weight: 600;
    line-height: 1;
    user-select: none;
    -webkit-user-select: none;
  }
  .attesa {
    opacity: 0.75;
  }
  .corrente {
    box-shadow:
      0 0 0 2px var(--colore-sfondo),
      0 0 0 5px var(--colore-primario);
  }
  .spunta {
    position: absolute;
    right: -2px;
    bottom: -2px;
    width: 40%;
    height: 40%;
  }
</style>
