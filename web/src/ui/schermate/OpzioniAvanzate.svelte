<script lang="ts">
  import { t } from "../testi";
  import Interruttore from "../componenti/Interruttore.svelte";
  import Selettore from "../componenti/Selettore.svelte";
  import { Regole } from "../../game/regole";
  import type { Configurazione } from "../../game/modelli";

  interface Props {
    config: Configurazione;
    onCambia: (trasforma: (c: Configurazione) => Configurazione) => void;
  }
  let { config, onCambia }: Props = $props();

  let aperto = $state(false);

  // mostraCategoria (attiva di default) non conta tra le opzioni attive.
  const attive = $derived.by(() => {
    const a: string[] = [];
    if (config.impostoreNonPrimo) a.push(t.opzBreveNonPrimo);
    if (config.impostoriSorpresa) a.push(t.opzBreveSorpresa);
    if (config.partitaTrappola) a.push(t.opzBreveTrappola);
    if (config.ordineCasuale) a.push(t.opzBreveOrdineCasuale);
    if (config.giriIndizi > 1) a.push(t.opzBreveGiri(config.giriIndizi));
    if (config.promemoriaUltimaPossibilita) a.push(t.opzBrevePromemoria);
    return a;
  });
  const giri = Array.from({ length: Regole.MAX_GIRI }, (_, i) => ({
    valore: String(i + 1),
    etichetta: String(i + 1),
  }));
</script>

<section class="card">
  <button
    type="button"
    class="testata"
    aria-expanded={aperto}
    aria-controls="opzioni-pannello"
    onclick={() => (aperto = !aperto)}
  >
    <span class="icona" aria-hidden="true">⚙</span>
    <span class="titoli">
      <span class="titolo">{t.opzTitolo}</span>
      {#if !aperto}
        <span class="riassunto">{attive.length === 0 ? t.opzRegoleClassiche : attive.join(" · ")}</span>
      {/if}
    </span>
    {#if attive.length > 0}
      <span class="badge">{t.opzAttive(attive.length)}</span>
    {/if}
    <span class="freccia" class:su={aperto} role="img" aria-label={aperto ? t.opzComprimi : t.opzEspandi}>⌄</span>
  </button>

  {#if aperto}
    <div id="opzioni-pannello" class="pannello">
      <div class="gruppo">{t.opzGruppoRuoli}</div>
      {#if config.modalita === "SENZA_PAROLA"}
        <Interruttore
          etichetta={t.configMostraCategoria}
          descrizione={t.opzDescVedeCategoria}
          valore={config.mostraCategoria}
          onCambia={(v) => onCambia((c) => ({ ...c, mostraCategoria: v }))}
        />
      {/if}
      <Interruttore
        etichetta={t.opzNonPrimo}
        descrizione={t.opzDescNonPrimo}
        valore={config.impostoreNonPrimo}
        onCambia={(v) => onCambia((c) => ({ ...c, impostoreNonPrimo: v }))}
      />
      <Interruttore
        etichetta={t.opzSorpresa}
        descrizione={t.opzDescSorpresa}
        valore={config.impostoriSorpresa}
        onCambia={(v) => onCambia((c) => ({ ...c, impostoriSorpresa: v }))}
      />
      <Interruttore
        etichetta={t.opzTrappola}
        descrizione={t.opzDescTrappola}
        valore={config.partitaTrappola}
        onCambia={(v) => onCambia((c) => ({ ...c, partitaTrappola: v }))}
      />

      <hr />
      <div class="gruppo">{t.opzGruppoTurni}</div>
      <Interruttore
        etichetta={t.opzOrdineCasuale}
        descrizione={t.opzDescOrdineCasuale}
        valore={config.ordineCasuale}
        onCambia={(v) => onCambia((c) => ({ ...c, ordineCasuale: v }))}
      />
      <div class="giri">
        <span class="g-titolo">{t.opzGiri}</span>
        <span class="g-desc">{t.opzDescGiri}</span>
        <Selettore
          opzioni={giri}
          valore={String(config.giriIndizi)}
          onCambia={(v) => onCambia((c) => ({ ...c, giriIndizi: Number(v) }))}
        />
      </div>

      <hr />
      <div class="gruppo">{t.opzGruppoFine}</div>
      <Interruttore
        etichetta={t.opzPromemoria}
        descrizione={t.opzDescPromemoria}
        valore={config.promemoriaUltimaPossibilita}
        onCambia={(v) => onCambia((c) => ({ ...c, promemoriaUltimaPossibilita: v }))}
      />
    </div>
  {/if}
</section>

<style>
  .card {
    background: var(--colore-superficie-variante);
    color: var(--colore-su-superficie-variante);
    border-radius: var(--raggio-l);
    overflow: hidden;
  }
  .testata {
    display: flex; align-items: center; gap: var(--spazio-3);
    width: 100%; min-height: 56px; padding: var(--spazio-2) var(--spazio-4);
    border: 0; background: transparent; color: inherit; font: inherit; text-align: left; cursor: pointer;
  }
  .icona { color: var(--colore-primario); font-size: 1.25rem; }
  .titoli { flex: 1; min-width: 0; display: flex; flex-direction: column; }
  .titolo { font-size: 1rem; font-weight: 600; color: var(--colore-su-superficie); }
  .riassunto {
    font-size: 0.8125rem;
    display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  }
  .badge {
    padding: 4px 10px; border-radius: 999px; font-size: 0.75rem; white-space: nowrap;
    background: var(--colore-contenitore-primario); color: var(--colore-su-contenitore-primario);
  }
  .freccia { font-size: 1.5rem; line-height: 1; transition: transform 0.2s; }
  .freccia.su { transform: rotate(180deg); }
  .pannello { padding: 0 var(--spazio-4) var(--spazio-2); display: flex; flex-direction: column; }
  .gruppo { padding: var(--spazio-1) 0 2px; font-size: 0.875rem; font-weight: 600; color: var(--colore-primario); }
  hr { width: 100%; border: 0; border-top: 1px solid var(--colore-contorno-variante); margin: var(--spazio-2) 0; }
  .giri { display: flex; flex-direction: column; gap: var(--spazio-2); padding: var(--spazio-2) 0; }
  .g-titolo { font-size: 1rem; color: var(--colore-su-superficie); }
  .g-desc { font-size: 0.8125rem; }
  @media (prefers-reduced-motion: reduce) { .freccia { transition: none; } }
</style>
