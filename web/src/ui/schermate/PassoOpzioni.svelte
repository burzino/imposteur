<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { t } from "../testi";
  import Interruttore from "../componenti/Interruttore.svelte";
  import Selettore from "../componenti/Selettore.svelte";
  import PassoIntestazione from "./PassoIntestazione.svelte";
  import { Regole } from "../../game/regole";
  import type { Configurazione } from "../../game/modelli";

  const stato = getStato();
  const config = $derived(stato.config);
  const giri = Array.from({ length: Regole.MAX_GIRI }, (_, i) => ({
    valore: String(i + 1),
    etichetta: String(i + 1),
  }));

  function cambia(f: (c: Configurazione) => Configurazione) {
    stato.impostaOpzione(f);
  }
</script>

<div class="corpo">
  <PassoIntestazione titolo={t.passo2Titolo} sottotitolo={t.passo2Sottotitolo}>
    {#snippet lato()}
      {#if stato.opzioniAttive > 0}<span class="badge">{t.opzAttive(stato.opzioniAttive)}</span>{/if}
    {/snippet}
  </PassoIntestazione>

  <section class="scheda">
    <h3>{t.opzGruppoRuoli}</h3>
    {#if config.modalita === "SENZA_PAROLA"}
      <Interruttore
        etichetta={t.configMostraCategoria}
        descrizione={t.opzDescVedeCategoria}
        valore={config.mostraCategoria}
        onCambia={(v) => cambia((c) => ({ ...c, mostraCategoria: v }))}
      />
    {/if}
    <Interruttore
      etichetta={t.opzNonPrimo}
      descrizione={t.opzDescNonPrimo}
      valore={config.impostoreNonPrimo}
      onCambia={(v) => cambia((c) => ({ ...c, impostoreNonPrimo: v }))}
    />
    <Interruttore
      etichetta={t.opzTrappola}
      descrizione={t.opzDescTrappola}
      valore={config.partitaTrappola}
      onCambia={(v) => cambia((c) => ({ ...c, partitaTrappola: v }))}
    />
  </section>

  <section class="scheda">
    <h3>{t.opzGruppoTurni}</h3>
    <Interruttore
      etichetta={t.opzOrdineCasuale}
      descrizione={t.opzDescOrdineCasuale}
      valore={config.ordineCasuale}
      onCambia={(v) => cambia((c) => ({ ...c, ordineCasuale: v }))}
    />
    <div class="giri">
      <span class="g-titolo">{t.opzGiri}</span>
      <span class="g-desc">{t.opzDescGiri}</span>
      <Selettore
        etichetta={t.opzGiri}
        opzioni={giri}
        valore={String(config.giriIndizi)}
        onCambia={(v) => cambia((c) => ({ ...c, giriIndizi: Number(v) }))}
      />
    </div>
  </section>

  <section class="scheda">
    <h3>{t.opzGruppoFine}</h3>
    <Interruttore
      etichetta={t.opzPromemoria}
      descrizione={t.opzDescPromemoria}
      valore={config.promemoriaUltimaPossibilita}
      onCambia={(v) => cambia((c) => ({ ...c, promemoriaUltimaPossibilita: v }))}
    />
  </section>
</div>

<style>
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-4); padding-bottom: 96px; }
  .scheda {
    display: flex; flex-direction: column; gap: var(--spazio-1);
    padding: var(--spazio-3) 20px;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
    color: var(--colore-su-superficie);
  }
  h3 { margin: 0; font: var(--testo-etichetta); color: var(--colore-primario); }
  .badge {
    padding: 4px 12px; border-radius: var(--raggio-pieno); white-space: nowrap;
    font: var(--testo-etichetta);
    background: var(--colore-contenitore-primario); color: var(--colore-su-contenitore-primario);
  }
  .giri { display: flex; flex-direction: column; gap: var(--spazio-2); padding: var(--spazio-2) 0; }
  .g-titolo { font: var(--testo-titolo); color: var(--colore-su-superficie); }
  .g-desc { font: var(--testo-corpo-piccolo); color: var(--colore-su-superficie-variante); }
</style>
