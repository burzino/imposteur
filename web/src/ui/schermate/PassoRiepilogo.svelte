<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { vaiAPasso } from "../rotte";
  import { t } from "../testi";
  import Pulsante from "../componenti/Pulsante.svelte";
  import PassoIntestazione from "./PassoIntestazione.svelte";
  import type { OpzioneRiepilogo } from "../../game/passi";

  const stato = getStato();
  const r = $derived(stato.riepilogo);

  function testoOpzione(o: OpzioneRiepilogo): string {
    switch (o) {
      case "NON_PARLA_PER_PRIMO": return t.opzBreveNonPrimo;
      case "TRAPPOLA": return t.opzBreveTrappola;
      case "ORDINE_CASUALE": return t.opzBreveOrdineCasuale;
      case "PROMEMORIA": return t.opzBrevePromemoria;
      case "SENZA_CATEGORIA": return t.riepSenzaCategoria;
      case "GIRI": return t.opzBreveGiri(r.giriIndizi);
    }
  }

  const impostori = $derived(
    r.finoA
      ? t.riepFinoA(r.numeroImpostori)
      : t.riepImpostori(r.numeroImpostori),
  );
  const opzioni = $derived(r.opzioni.length === 0 ? t.riepNessunaOpzione : r.opzioni.map(testoOpzione).join(", "));
</script>

<div class="corpo">
  <PassoIntestazione titolo={t.passo4Titolo} sottotitolo={t.passo4Sottotitolo} />

  <ul class="righe">
    <li class="riga">
      <div class="testi">
        <span class="etichetta">{t.passoGiocatori}</span>
        <span class="valore">{t.riepGiocatoriImpostori(t.riepGiocatori(r.numeroGiocatori), impostori)}</span>
        <span class="secondaria">{r.nomi.join(", ")}</span>
      </div>
      <div class="modifica"><Pulsante variante="testo" onClick={() => vaiAPasso(1)}>{t.passoModifica}</Pulsante></div>
    </li>
    <li class="riga">
      <div class="testi">
        <span class="etichetta">{t.configModalita}</span>
        <span class="valore">{r.modalita === "PAROLA_AFFINE" ? t.modalitaAffine : t.modalitaSenzaParola}</span>
      </div>
      <div class="modifica"><Pulsante variante="testo" onClick={() => vaiAPasso(1)}>{t.passoModifica}</Pulsante></div>
    </li>
    <li class="riga">
      <div class="testi">
        <span class="etichetta">
          {t.opzTitolo}
          {#if stato.opzioniAttive > 0}<span class="badge">{t.opzAttive(stato.opzioniAttive)}</span>{/if}
        </span>
        <span class="valore">{opzioni}</span>
      </div>
      <div class="modifica"><Pulsante variante="testo" onClick={() => vaiAPasso(2)}>{t.passoModifica}</Pulsante></div>
    </li>
    <li class="riga">
      <div class="testi">
        <span class="etichetta">{t.configCategorie}</span>
        <span class="valore">{t.riepCategorie(r.numeroCategorie)}</span>
        <span class="secondaria">{t.configParoleRimanenti(stato.paroleRimanenti, stato.paroleTotali)}</span>
      </div>
      <div class="modifica"><Pulsante variante="testo" onClick={() => vaiAPasso(3)}>{t.passoModifica}</Pulsante></div>
    </li>
  </ul>
</div>

<style>
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-4); padding-bottom: 96px; }
  .righe { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--spazio-2); }
  .riga {
    display: flex; align-items: center; justify-content: space-between; gap: var(--spazio-3);
    padding: var(--spazio-3) var(--spazio-4);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
    color: var(--colore-su-superficie);
  }
  .testi { display: flex; flex-direction: column; min-width: 0; }
  .etichetta { display: flex; align-items: center; gap: var(--spazio-2); font: var(--testo-etichetta); color: var(--colore-primario); }
  .valore { font: var(--testo-titolo); overflow-wrap: anywhere; }
  .secondaria { font: var(--testo-corpo-piccolo); color: var(--colore-su-superficie-variante); overflow-wrap: anywhere; }
  .badge {
    padding: 2px 10px; border-radius: var(--raggio-pieno); white-space: nowrap;
    font: var(--testo-didascalia);
    background: var(--colore-contenitore-primario); color: var(--colore-su-contenitore-primario);
  }
  .modifica { flex: none; }
  .modifica :global(.pulsante) { width: auto; }
</style>
