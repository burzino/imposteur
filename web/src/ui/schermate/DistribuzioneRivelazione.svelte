<script lang="ts">
  import { onMount } from "svelte";
  import { contenutoPer, type Partita } from "../../game/partita";
  import CartaGirevole from "../componenti/CartaGirevole.svelte";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import TestoAdattivo from "../componenti/TestoAdattivo.svelte";
  import { vibra } from "../browser";
  import { t } from "../testi";
  import DistribuzioneFilaAvatar from "./DistribuzioneFilaAvatar.svelte";

  // Montato con {#key indice}. Layout, colori e animazione identici per ogni ruolo e modalita'.
  let {
    titolo,
    partita,
    indice,
    etichettaPulsante,
    mostraFila = true,
    onNascondi,
    onHome,
    onIndietro,
  }: {
    titolo: string;
    partita: Partita;
    indice: number;
    etichettaPulsante?: string;
    mostraFila?: boolean;
    onNascondi: () => void;
    onHome: () => void;
    onIndietro: () => void;
  } = $props();

  const RITARDO_MS = 600;
  const META_FLIP_MS = 90; // meta rotazione della carta (molla lenta)
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
    <div class="testata">
      {#if mostraFila}
        <DistribuzioneFilaAvatar giocatori={partita.giocatori} corrente={indice} />
      {/if}
      <p class="indicatore">{t.distribuzioneIndicatore(indice + 1, partita.giocatori.length)}</p>
    </div>
    <div class="centro">
      <CartaGirevole>
        {#snippet fronte()}
          {#if contenuto.tipo === "ParolaSegreta"}
            <p class="intro">
              {partita.modalita === "PAROLA_AFFINE" ? t.ruoloLaTuaParolaE : t.ruoloLaParolaE}
            </p>
            <TestoAdattivo testo={contenuto.testo} classe="dist-parola" />
          {:else}
            {@render icona(96)}
            <TestoAdattivo testo={t.ruoloSeiImpostore} classe="dist-impostore" />
            {#if contenuto.categoria !== null}
              <p class="categoria">{t.ruoloCategoria(contenuto.categoria)}</p>
            {/if}
          {/if}
        {/snippet}
      </CartaGirevole>
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
    gap: var(--spazio-4);
    min-height: 60vh;
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
    align-items: center;
    width: 100%;
  }
  .intro {
    font: var(--testo-titolo-sezione);
  }
  .categoria {
    font: var(--testo-titolo-sezione);
  }
  .scena :global(.dist-parola) {
    width: 100%;
    font-size: 2.25rem;
    font-weight: 800;
  }
  .scena :global(.dist-impostore) {
    width: 100%;
    font-size: 2.25rem;
    font-weight: 800;
  }
  .piede {
    width: 100%;
  }
</style>
