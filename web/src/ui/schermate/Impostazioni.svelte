<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { indietro, vaiAHome } from "../rotte";
  import { t } from "../testi";
  import { esportaSegnalazioni, condivisioneFileDisponibile } from "../browser";
  import type { Tema } from "../../data/aspetto";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import Selettore from "../componenti/Selettore.svelte";
  import CampoTesto from "../componenti/CampoTesto.svelte";
  import DialogoConferma from "../componenti/DialogoConferma.svelte";
  import { mostraToast } from "../componenti/notifiche.svelte";

  const stato = getStato();
  const MAX = 500;
  const opzioniTema: { valore: Tema; etichetta: string }[] = [
    { valore: "SISTEMA", etichetta: t.temaSistema },
    { valore: "CHIARO", etichetta: t.temaChiaro },
    { valore: "SCURO", etichetta: t.temaScuro },
    { valore: "ALTO_CONTRASTO", etichetta: t.temaAltoContrasto },
  ];
  const puoCondividere = condivisioneFileDisponibile();

  let suggerimento = $state("");
  let chiediCancella = $state(false);

  function due(n: number): string {
    return String(n).padStart(2, "0");
  }
  function istanteLocale(): string {
    const d = new Date();
    return `${d.getFullYear()}-${due(d.getMonth() + 1)}-${due(d.getDate())}T${due(d.getHours())}:${due(d.getMinutes())}:${due(d.getSeconds())}`;
  }
  function invia() {
    stato.salvaSegnalazione({
      tipo: "app",
      istante: istanteLocale(),
      categoriaId: null,
      parola: null,
      affine: null,
      propostaParola: null,
      propostaAffine: null,
      modalita: null,
      motivi: [],
      nota: suggerimento.trim(),
    });
    suggerimento = "";
    mostraToast(t.segnalaSalvata);
  }
  function esporta(modo: "scarica" | "condividi") {
    void esportaSegnalazioni(stato.leggiSegnalazioniJsonl(), modo);
  }
</script>

<Pagina titolo={t.impostazioniTitolo} onIndietro={indietro} onHome={vaiAHome}>
  <div class="impostazioni">
    <h2>{t.temaTitolo}</h2>
    <Selettore
      opzioni={opzioniTema}
      valore={stato.aspetto.tema}
      onCambia={(v) => stato.impostaAspetto({ ...stato.aspetto, tema: v as Tema })}
      etichetta={t.temaTitolo}
    />
    <hr />
    <h2>{t.segnalazioniTitolo}</h2>
    <CampoTesto
      valore={suggerimento}
      onCambia={(v) => (suggerimento = v.slice(0, MAX))}
      etichetta={t.segnalazioniSuggerimenti}
      maxLunghezza={MAX}
      multilinea
      righe={3}
    />
    <p class="contatore">{t.segnalaContatore(suggerimento.length, MAX)}</p>
    <div class="destra">
      <Pulsante variante="pieno" disabilitato={suggerimento.trim() === ""} onClick={invia}>
        {t.segnalazioniInvia}
      </Pulsante>
    </div>
    <div class="riga">
      <span class="salvate">{t.segnalazioniSalvateN(stato.segnalazioniSalvate)}</span>
      <Pulsante variante="contorno" disabilitato={stato.segnalazioniSalvate === 0} onClick={() => (chiediCancella = true)}>
        {t.segnalazioniCancella}
      </Pulsante>
    </div>
    {#if stato.segnalazioniSalvate > 0}
      <div class="riga esporta">
        <Pulsante variante="tonale" onClick={() => esporta("scarica")}>{t.esportaSegnalazioni}</Pulsante>
        {#if puoCondividere}
          <Pulsante variante="tonale" onClick={() => esporta("condividi")}>{t.condividiSegnalazioni}</Pulsante>
        {/if}
      </div>
    {/if}
    <p class="nota">{t.segnalazioniLocale}</p>
  </div>
</Pagina>

<DialogoConferma
  aperto={chiediCancella}
  titolo={t.segnalazioniCancellaConferma}
  etichettaSi={t.segnalazioniCancellaSi}
  etichettaNo={t.annulla}
  onSi={() => {
    chiediCancella = false;
    stato.cancellaSegnalazioni();
  }}
  onNo={() => (chiediCancella = false)}
/>

<style>
  .impostazioni {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
    padding: var(--spazio-4);
  }
  h2 {
    margin: 0;
    font-size: 1.375rem;
    font-weight: 400;
    color: var(--colore-primario);
  }
  hr {
    width: 100%;
    margin: var(--spazio-2) 0;
    border: none;
    border-top: 1px solid var(--colore-contorno-variante);
  }
  .contatore {
    margin: 0;
    text-align: end;
    font-size: 0.75rem;
    color: var(--colore-su-superficie-variante);
  }
  .destra {
    display: flex;
    justify-content: flex-end;
  }
  .riga {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: var(--spazio-2);
  }
  .salvate {
    flex: 1;
    min-width: 8rem;
    font-size: 1rem;
  }
  .esporta {
    justify-content: flex-end;
  }
  .nota {
    margin: 0;
    font-size: 0.75rem;
    color: var(--colore-su-superficie-variante);
  }
</style>
