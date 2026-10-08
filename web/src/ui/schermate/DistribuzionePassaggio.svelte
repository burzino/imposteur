<script lang="ts">
  import { onMount } from "svelte";
  import type { Partita } from "../../game/partita";
  import Pagina from "../componenti/Pagina.svelte";
  import TestoAdattivo from "../componenti/TestoAdattivo.svelte";
  import { t } from "../testi";
  import DistribuzioneFilaAvatar from "./DistribuzioneFilaAvatar.svelte";
  import DistribuzioneTieniPremuto from "./DistribuzioneTieniPremuto.svelte";

  // Montato con {#key indice}: il ritardo anti doppio tocco riparte a ogni giocatore.
  let {
    titolo,
    partita,
    indice,
    mostraFila = true,
    onSono,
    onHome,
    onIndietro,
  }: {
    titolo: string;
    partita: Partita;
    indice: number;
    mostraFila?: boolean;
    onSono: () => void;
    onHome: () => void;
    onIndietro: () => void;
  } = $props();

  const RITARDO_MS = 600;
  let abilitato = $state(false);
  const nome = $derived(partita.giocatori[indice] ?? "");
  const iniziale = $derived((Array.from(nome.trim())[0] ?? "").toUpperCase());

  onMount(() => {
    const id = setTimeout(() => (abilitato = true), RITARDO_MS);
    return () => clearTimeout(id);
  });
</script>

<Pagina {titolo} {onHome} {onIndietro}>
  <div class="scena">
    {#if mostraFila}
      <DistribuzioneFilaAvatar giocatori={partita.giocatori} corrente={indice} />
      <p class="indicatore">{t.distribuzioneIndicatore(indice + 1, partita.giocatori.length)}</p>
    {/if}
    <div class="cerchio" aria-hidden="true">{iniziale}</div>
    <p class="passa">{t.distribuzionePassaA}</p>
    <TestoAdattivo testo={nome} classe="dist-nome" maxRighe={2} />
    <p class="avviso">{t.distribuzioneNonGuardare}</p>
  </div>
  {#snippet piede()}
    <div class="piede">
      <DistribuzioneTieniPremuto {abilitato} onRivela={onSono} />
      <p class="didascalia">{t.distribuzioneSonoPremuto(nome)}</p>
    </div>
  {/snippet}
</Pagina>

<style>
  .scena {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-5);
    min-height: 60vh;
    padding: var(--spazio-4);
    border-radius: var(--raggio-l);
    background: var(--colore-superficie-variante);
    color: var(--colore-su-superficie-variante);
    text-align: center;
  }
  .indicatore,
  .passa {
    margin: 0;
    font-size: 1.125rem;
  }
  .avviso {
    margin: 0;
    font-size: 1rem;
  }
  .cerchio {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 96px;
    height: 96px;
    border-radius: 50%;
    background: var(--colore-contenitore-primario);
    color: var(--colore-su-contenitore-primario);
    font-size: 2.25rem;
    animation: respiro 2400ms ease-in-out infinite;
  }
  .scena :global(.dist-nome) {
    width: 100%;
    color: var(--colore-primario);
    font-size: 2.75rem;
    font-weight: 500;
  }
  .piede {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spazio-2);
    width: 100%;
  }
  .didascalia {
    margin: 0;
    font-size: 0.875rem;
    text-align: center;
    color: var(--colore-su-superficie-variante);
  }
  @keyframes respiro {
    0%,
    100% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.05);
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .cerchio {
      animation: none;
    }
  }
</style>
