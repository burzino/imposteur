<script lang="ts">
  /**
   * Indicatore dei passi a pillole (design 3). Passi fatti: pieni e toccabili; corrente: pieno e largo
   * --passo-corrente-peso volte gli altri; da fare: solo contorno. L'etichetta visibile e il testo
   * per l'accessibilita arrivano gia composti dal chiamante (nessun testo qui dentro).
   */
  interface Props {
    /** Nomi dei passi, nell'ordine (uno per segmento). */
    nomi: readonly string[];
    /** Passo corrente, da 1 a nomi.length. */
    corrente: number;
    /** Testo visibile sotto i segmenti, es. "Passo 2 di 4 · Opzioni". */
    etichetta: string;
    /** Valore per aria-valuetext, es. "Passo 2 di 4: Opzioni". */
    descrizione: string;
    /** Chiamato con il numero (1-based) di un passo gia fatto. */
    onVai: (passo: number) => void;
  }
  let { nomi, corrente, etichetta, descrizione, onVai }: Props = $props();
</script>

<div class="passi">
  <span
    class="solo-lettori"
    role="progressbar"
    aria-valuemin="1"
    aria-valuemax={nomi.length}
    aria-valuenow={corrente}
    aria-valuetext={descrizione}
  ></span>
  <div class="segmenti">
    {#each nomi as nome, i (i)}
      {@const k = i + 1}
      <span class="passo" style:--peso={k === corrente ? "var(--passo-corrente-peso)" : 1}>
        {#if k < corrente}
          <button type="button" class="tocco fatto" aria-label={nome} onclick={() => onVai(k)}>
            <span class="barra piena"></span>
          </button>
        {:else}
          <span class="tocco">
            <span class="barra" class:piena={k === corrente}></span>
          </span>
        {/if}
      </span>
    {/each}
  </div>
  <p class="etichetta">{etichetta}</p>
</div>

<style>
  .passi {
    position: relative;
    padding: 0 var(--spazio-4);
  }
  .segmenti {
    display: flex;
    gap: var(--passo-spazio);
  }
  .passo {
    flex: var(--peso) 1 0;
    min-width: 0;
    display: flex;
    transition: flex-grow var(--molla-spaziale);
  }
  .tocco {
    flex: 1;
    display: flex;
    align-items: center;
    height: var(--altezza-tocco);
    padding: 0;
    border: 0;
    background: transparent;
  }
  .fatto {
    cursor: pointer;
  }
  .barra {
    display: block;
    width: 100%;
    height: var(--passo-altezza);
    border: 2px solid var(--colore-contorno);
    border-radius: var(--raggio-pieno);
    background: transparent;
    transition:
      background-color var(--molla-effetti),
      border-color var(--molla-effetti);
  }
  .barra.piena {
    border-color: var(--colore-primario);
    background: var(--colore-primario);
  }
  .fatto:hover .barra {
    filter: brightness(1.15);
  }
  .etichetta {
    margin-top: var(--spazio-1);
    font: var(--testo-didascalia);
    color: var(--colore-su-superficie-variante);
  }
</style>
