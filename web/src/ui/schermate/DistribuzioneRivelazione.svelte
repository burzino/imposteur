<script lang="ts">
  import { onMount } from "svelte";
  import { contenutoPer, type Partita } from "../../game/partita";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import TestoAdattivo from "../componenti/TestoAdattivo.svelte";
  import { vibra } from "../browser";
  import { t } from "../testi";

  // Montato con {#key indice}. Layout, colori e animazione identici per ogni ruolo e modalita'.
  let {
    titolo,
    partita,
    indice,
    etichettaPulsante,
    onNascondi,
    onHome,
    onIndietro,
  }: {
    titolo: string;
    partita: Partita;
    indice: number;
    etichettaPulsante?: string;
    onNascondi: () => void;
    onHome: () => void;
    onIndietro: () => void;
  } = $props();

  const RITARDO_MS = 600;
  const META_FLIP_MS = 225;
  let abilitato = $state(false);
  const contenuto = $derived(contenutoPer(partita, indice));
  const ultimo = $derived(indice === partita.giocatori.length - 1);
  const etichetta = $derived(
    etichettaPulsante ?? (ultimo ? t.distribuzioneNascondiUltimo : t.distribuzioneNascondi),
  );

  onMount(() => {
    // Una sola vibrazione, identica per tutti, alla comparsa del fronte.
    const v = setTimeout(() => vibra(), META_FLIP_MS);
    const a = setTimeout(() => (abilitato = true), RITARDO_MS);
    return () => {
      clearTimeout(v);
      clearTimeout(a);
    };
  });

  function nascondi() {
    vibra();
    onNascondi();
  }
</script>

{#snippet icona(dim: number)}
  <svg width={dim} height={dim} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
    <path
      d="M12 2C8 2 5 5 5 9v5.5L3.5 20 8 18.5 12 22l4-3.5 4.5 1.5L19 14.5V9c0-4-3-7-7-7Zm-3 9a1.6 1.6 0 1 1 0 3.2A1.6 1.6 0 0 1 9 11Zm6 0a1.6 1.6 0 1 1 0 3.2A1.6 1.6 0 0 1 15 11Z"
    />
  </svg>
{/snippet}

<Pagina {titolo} {onHome} {onIndietro}>
  <div class="scena">
    <p class="indicatore">{t.distribuzioneIndicatore(indice + 1, partita.giocatori.length)}</p>
    <div class="prospettiva">
      <div class="carta">
        <div class="faccia dorso" aria-hidden="true">{@render icona(160)}</div>
        <div class="faccia fronte">
          {#if contenuto.tipo === "ParolaSegreta"}
            <p class="intro">
              {partita.modalita === "PAROLA_AFFINE" ? t.ruoloLaTuaParolaE : t.ruoloLaParolaE}
            </p>
            <TestoAdattivo testo={contenuto.testo} classe="dist-parola" />
          {:else}
            {@render icona(120)}
            <TestoAdattivo testo={t.ruoloSeiImpostore} classe="dist-impostore" />
            {#if contenuto.categoria !== null}
              <p class="categoria">{t.ruoloCategoria(contenuto.categoria)}</p>
            {/if}
          {/if}
        </div>
      </div>
    </div>
  </div>
  {#snippet piede()}
    <div class="piede">
      <Pulsante variante="pieno" disabilitato={!abilitato} onClick={nascondi}>{etichetta}</Pulsante>
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
    background: linear-gradient(
      to bottom,
      var(--colore-contenitore-secondario),
      var(--colore-superficie)
    );
    color: var(--colore-su-superficie);
  }
  .indicatore {
    margin: 0;
    font-size: 1.125rem;
  }
  .prospettiva {
    width: 80%;
    perspective: 1200px;
  }
  .carta {
    position: relative;
    min-height: 300px;
    transform-style: preserve-3d;
    animation: gira 450ms ease-in-out both;
  }
  .faccia {
    box-sizing: border-box;
    width: 100%;
    min-height: 300px;
    border-radius: 28px;
    box-shadow: 0 2px 8px rgb(0 0 0 / 0.25);
    backface-visibility: hidden;
    -webkit-backface-visibility: hidden;
    user-select: none;
    -webkit-user-select: none;
  }
  .dorso {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--colore-primario);
    color: var(--colore-su-primario);
    transform: rotateY(180deg);
  }
  .fronte {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-4);
    padding: 20px;
    background: var(--colore-terziario);
    color: var(--colore-su-terziario);
    text-align: center;
  }
  .intro,
  .categoria {
    margin: 0;
    font-size: 1.125rem;
  }
  .categoria {
    font-size: 1.375rem;
  }
  .fronte :global(.dist-parola) {
    width: 100%;
    font-size: 3.25rem;
    font-weight: 500;
  }
  .fronte :global(.dist-impostore) {
    width: 100%;
    font-size: 2.25rem;
    font-weight: 500;
  }
  .piede {
    width: 100%;
  }
  .piede :global(button) {
    width: 100%;
    min-height: 64px;
    font-size: 1.25rem;
  }
  @keyframes gira {
    from {
      transform: rotateY(180deg);
    }
    to {
      transform: rotateY(0deg);
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .carta {
      animation: none;
    }
  }
</style>
