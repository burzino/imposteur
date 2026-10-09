<script lang="ts">
  import type { Snippet } from "svelte";

  /**
   * Carta che si gira (design 3): alla comparsa ruota da 180 a 0 gradi attorno all'asse Y con la molla
   * lenta. Dorso (schema del Passaggio, punto interrogativo) e fronte hanno lo stesso colore e le stesse
   * misure per ogni ruolo. Con movimento ridotto: dissolvenza di 100 ms.
   */
  let { fronte, dorso }: { fronte: Snippet; dorso?: Snippet } = $props();
</script>

<div class="prospettiva">
  <div class="carta">
    <div class="faccia dorso" aria-hidden="true">
      {#if dorso}
        {@render dorso()}
      {:else}
        <span class="domanda">?</span>
      {/if}
    </div>
    <div class="faccia fronte">
      {@render fronte()}
    </div>
  </div>
</div>

<style>
  .prospettiva {
    width: 100%;
    perspective: 1200px;
  }
  .carta {
    position: relative;
    min-height: 280px;
    transform-style: preserve-3d;
    animation: gira var(--molla-spaziale-lenta) both;
  }
  .faccia {
    width: 100%;
    min-height: 280px;
    padding: var(--spazio-6);
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xxl);
    background: var(--colore-contenitore-superficie-alto);
    color: var(--colore-su-superficie);
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
    transform: rotateY(180deg);
  }
  .domanda {
    font: var(--testo-display);
    font-size: 6rem;
    line-height: 1;
    color: var(--colore-primario);
  }
  .fronte {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--spazio-4);
    text-align: center;
  }
  @keyframes gira {
    from {
      transform: rotateY(180deg);
    }
    to {
      transform: rotateY(0deg);
    }
  }
  @keyframes dissolvi {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .carta {
      animation: dissolvi 100ms linear both !important;
      animation-duration: 100ms !important;
    }
  }
</style>
