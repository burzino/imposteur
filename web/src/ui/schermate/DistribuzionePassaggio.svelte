<script lang="ts">
  import { onMount } from "svelte";
  import type { Partita } from "../../game/partita";
  import Avatar from "../componenti/Avatar.svelte";
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

  onMount(() => {
    const id = setTimeout(() => (abilitato = true), RITARDO_MS);
    return () => clearTimeout(id);
  });
</script>

<Pagina {titolo} {onHome} {onIndietro} piedeNudo>
  <div class="scena">
    {#if mostraFila}
      <div class="testata">
        <DistribuzioneFilaAvatar giocatori={partita.giocatori} corrente={indice} />
        <p class="indicatore">{t.distribuzioneIndicatore(indice + 1, partita.giocatori.length)}</p>
      </div>
    {/if}
    <div class="centro">
      <p class="passa">{t.distribuzionePassaA}</p>
      <TestoAdattivo testo={nome} classe="dist-nome" maxRighe={2} />
      <span class="respiro">
        <Avatar {nome} {indice} stato="pieno" dimensione={96} />
      </span>
      <p class="avviso">{t.distribuzioneNonGuardare}</p>
    </div>
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
    gap: var(--spazio-4);
    min-height: 60vh;
    color: var(--colore-su-sfondo);
    text-align: center;
  }
  .testata {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
  }
  .indicatore {
    font: var(--testo-didascalia);
    color: var(--colore-su-superficie-variante);
  }
  .centro {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-4);
    width: 100%;
  }
  .passa {
    font: var(--testo-corpo);
  }
  .centro :global(.dist-nome) {
    width: 100%;
    color: var(--colore-primario);
    font-size: 2.25rem;
    font-weight: 800;
  }
  .respiro {
    display: inline-flex;
    animation: respiro 2400ms ease-in-out infinite;
  }
  /* avviso: stessa pillola per tutti, nessun colore dipende dal ruolo */
  .avviso {
    padding: var(--spazio-2) var(--spazio-4);
    border: var(--spessore-contorno) solid var(--colore-su-contenitore-terziario);
    border-radius: var(--raggio-pieno);
    background: var(--colore-contenitore-terziario);
    color: var(--colore-su-contenitore-terziario);
    font: var(--testo-corpo-piccolo);
  }
  .piede {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spazio-2);
    width: 100%;
  }
  .didascalia {
    font: var(--testo-corpo-piccolo);
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
    .respiro {
      animation: none;
    }
  }
</style>
