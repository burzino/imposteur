<script lang="ts">
  import { t } from "../testi";

  // Overlay a tutto schermo prima del Gioco: un tocco (o Invio/Spazio) lo salta.
  let { onTocco }: { onTocco: () => void } = $props();
</script>

<button type="button" class="overlay" onclick={onTocco}>
  <span class="contenuto">
    <span class="titolo">{t.distribuzioneTuttiPronti}</span>
    <span class="sotto">{t.distribuzioneBluff}</span>
  </span>
</button>

<style>
  .overlay {
    position: fixed;
    inset: 0;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    border: 0;
    padding: var(--spazio-5);
    background: var(--colore-contenitore-primario);
    color: var(--colore-su-contenitore-primario);
    font: inherit;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
  }
  .contenuto {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
    text-align: center;
    animation: entra 400ms ease-out both;
  }
  .titolo {
    font-size: 2.25rem;
    font-weight: 500;
  }
  .sotto {
    font-size: 1.125rem;
  }
  @keyframes entra {
    from {
      opacity: 0;
      transform: scale(0.8);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .contenuto {
      animation: none;
    }
  }
</style>
