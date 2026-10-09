<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { vaiAPasso } from "../rotte";
  import { t } from "../testi";
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
    <li>
      <button type="button" class="riga" onclick={() => vaiAPasso(1)}>
        <span class="testi">
          <span class="etichetta">{t.passoGiocatori}</span>
          <span class="valore">{t.riepGiocatoriImpostori(t.riepGiocatori(r.numeroGiocatori), impostori)}</span>
          <span class="secondaria">{r.nomi.join(", ")}</span>
        </span>
        <span class="modifica">{t.passoModifica}</span>
      </button>
    </li>
    <li>
      <button type="button" class="riga" onclick={() => vaiAPasso(1)}>
        <span class="testi">
          <span class="etichetta">{t.configModalita}</span>
          <span class="valore">{r.modalita === "PAROLA_AFFINE" ? t.modalitaAffine : t.modalitaSenzaParola}</span>
        </span>
        <span class="modifica">{t.passoModifica}</span>
      </button>
    </li>
    <li>
      <button type="button" class="riga" onclick={() => vaiAPasso(2)}>
        <span class="testi">
          <span class="etichetta">{t.opzTitolo}</span>
          <span class="valore">{opzioni}</span>
        </span>
        {#if stato.opzioniAttive > 0}<span class="badge">{t.opzAttive(stato.opzioniAttive)}</span>{/if}
        <span class="modifica">{t.passoModifica}</span>
      </button>
    </li>
    <li>
      <button type="button" class="riga" onclick={() => vaiAPasso(3)}>
        <span class="testi">
          <span class="etichetta">{t.configCategorie}</span>
          <span class="valore">{t.riepCategorie(r.numeroCategorie)}</span>
          <span class="secondaria">{t.configParoleRimanenti(stato.paroleRimanenti, stato.paroleTotali)}</span>
        </span>
        <span class="modifica">{t.passoModifica}</span>
      </button>
    </li>
  </ul>
</div>

<style>
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-4); padding-bottom: 96px; }
  .righe { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--spazio-2); }
  .riga {
    display: flex; align-items: center; gap: var(--spazio-3);
    width: 100%; min-height: 72px; padding: var(--spazio-2) var(--spazio-2) var(--spazio-2) 20px;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
    color: var(--colore-su-superficie);
    font: inherit; text-align: left; cursor: pointer;
  }
  .riga:hover { background: color-mix(in srgb, var(--colore-primario) 8%, var(--colore-contenitore-superficie-basso)); }
  .riga:focus-visible { outline: 3px solid var(--colore-primario); outline-offset: 2px; }
  .testi { flex: 1; display: flex; flex-direction: column; min-width: 0; }
  .etichetta { font: var(--testo-didascalia); color: var(--colore-su-superficie-variante); }
  .valore { font: var(--testo-titolo); overflow-wrap: anywhere; }
  .secondaria {
    font: var(--testo-corpo-piccolo); color: var(--colore-su-superficie-variante); overflow-wrap: anywhere;
    display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  }
  .badge {
    flex: none; padding: 2px 10px; border-radius: var(--raggio-pieno); white-space: nowrap;
    font: var(--testo-didascalia);
    background: var(--colore-contenitore-primario); color: var(--colore-su-contenitore-primario);
  }
  .modifica { flex: none; padding: 0 var(--spazio-3); font: var(--testo-titolo); color: var(--colore-primario); }
</style>
