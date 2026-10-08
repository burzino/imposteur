<script lang="ts">
  import { statoToast } from "./notifiche.svelte";
</script>

<div class="zona" role="status" aria-live="polite">
  {#if statoToast.testo}
    <div class="toast">{statoToast.testo}</div>
  {/if}
</div>

<style>
  .zona {
    position: fixed;
    left: 0;
    right: 0;
    bottom: calc(var(--spazio-5) + env(safe-area-inset-bottom));
    display: flex;
    justify-content: center;
    padding: 0 var(--spazio-4);
    pointer-events: none;
    z-index: 1000;
  }
  .toast {
    max-width: var(--larghezza-max);
    padding: var(--spazio-3) var(--spazio-4);
    border-radius: var(--raggio-s);
    background: var(--colore-su-superficie);
    color: var(--colore-superficie);
    box-shadow: 0 2px 8px rgb(0 0 0 / 0.3);
  }
</style>
