<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { t } from "../testi";
  import Contatore from "../componenti/Contatore.svelte";
  import Interruttore from "../componenti/Interruttore.svelte";
  import SelettoreModalita from "../componenti/SelettoreModalita.svelte";
  import PassoIntestazione from "./PassoIntestazione.svelte";
  import { Regole } from "../../game/regole";
  import type { Modalita } from "../../game/modelli";

  const stato = getStato();
  const config = $derived(stato.config);
  const maxImpostori = $derived(Regole.maxImpostori(config.numeroGiocatori));

  const modalita = $derived([
    { valore: "SENZA_PAROLA", titolo: t.modalitaSenzaParola, descrizione: t.modalitaSenzaParolaDesc },
    { valore: "PAROLA_AFFINE", titolo: t.modalitaAffine, descrizione: t.modalitaAffineDesc },
  ]);
</script>

<div class="corpo">
  <PassoIntestazione titolo={t.passo2Titolo} sottotitolo={t.passo2Sottotitolo} />

  <SelettoreModalita
    etichetta={t.configModalita}
    opzioni={modalita}
    valore={config.modalita}
    onCambia={(v) => stato.impostaModalita(v as Modalita)}
  />
  <Contatore
    etichetta={config.impostoriSorpresa ? t.configNumeroImpostoriMax : t.configNumeroImpostori}
    valore={config.numeroImpostori}
    min={1}
    max={Math.max(1, maxImpostori)}
    onCambia={(n) => stato.impostaNumeroImpostori(n)}
  />
  <Interruttore
    etichetta={t.opzSorpresa}
    descrizione={t.opzDescSorpresa}
    valore={config.impostoriSorpresa}
    onCambia={(v) => stato.impostaOpzione((c) => ({ ...c, impostoriSorpresa: v }))}
  />
</div>

<style>
  .corpo { display: flex; flex-direction: column; gap: var(--spazio-4); padding-bottom: 96px; }
</style>
