<script lang="ts">
  import { onMount } from "svelte";
  import { t } from "../testi";
  import Pulsante from "../componenti/Pulsante.svelte";
  import { FormatoSegnalazioni, segnalazione, type MotivoSegnalazione } from "../../data/segnalazioni";

  interface Props {
    parola: string;
    affine: string | null;
    categoria: string;
    onAnnulla: () => void;
    onSalva: (motivi: MotivoSegnalazione[], nota: string, propostaParola: string | null, propostaAffine: string | null) => void;
  }
  let { parola, affine, categoria, onAnnulla, onSalva }: Props = $props();

  const MAX_COMMENTO = 500;
  const MAX_PROPOSTA = 40;
  const MOTIVI: { motivo: MotivoSegnalazione; testo: string }[] = [
    { motivo: "TROPPO_SIMILI", testo: t.segnalaMotivoSimili },
    { motivo: "TROPPO_DIVERSE", testo: t.segnalaMotivoDiverse },
    { motivo: "POCO_CONOSCIUTA", testo: t.segnalaMotivoPocoConosciuta },
    { motivo: "CATEGORIA_SBAGLIATA", testo: t.segnalaMotivoCategoria },
  ];

  let motivi = $state<MotivoSegnalazione[]>([]);
  let nota = $state("");
  let propParola = $state("");
  let propAffine = $state("");
  let dialogo: HTMLDialogElement | undefined = $state();
  let alto = $state("100dvh");
  let sopra = $state("0px");

  const proposta = $derived(
    FormatoSegnalazioni.normalizza(
      segnalazione({ tipo: "coppia", istante: "", propostaParola: propParola, propostaAffine: propAffine }),
    ),
  );
  const propostaValida = $derived(FormatoSegnalazioni.propostaValida(proposta));
  const propostaMezza = $derived((proposta.propostaParola === null) !== (proposta.propostaAffine === null));
  const propostaUguale = $derived(
    proposta.propostaParola !== null && proposta.propostaAffine !== null && !propostaValida,
  );
  const abilitato = $derived(
    (motivi.length > 0 || nota.trim() !== "" || propostaValida) && !propostaMezza && !propostaUguale,
  );

  function alterna(m: MotivoSegnalazione, v: boolean) {
    motivi = v ? [...motivi, m] : motivi.filter((x) => x !== m);
  }

  function salva() {
    if (!abilitato) return;
    const ordinati = MOTIVI.map((x) => x.motivo).filter((m) => motivi.includes(m));
    onSalva(ordinati, nota.trim(), proposta.propostaParola, proposta.propostaAffine);
  }

  onMount(() => {
    dialogo?.showModal();
    // Con la tastiera aperta il dialogo segue l'area visibile.
    const vv = typeof window !== "undefined" ? window.visualViewport : null;
    const aggiorna = () => {
      if (!vv) return;
      alto = `${vv.height}px`;
      sopra = `${vv.offsetTop}px`;
    };
    aggiorna();
    vv?.addEventListener("resize", aggiorna);
    vv?.addEventListener("scroll", aggiorna);
    return () => {
      vv?.removeEventListener("resize", aggiorna);
      vv?.removeEventListener("scroll", aggiorna);
      if (dialogo?.open) dialogo.close();
    };
  });
</script>

<dialog
  bind:this={dialogo}
  class="sfondo"
  style:height={alto}
  style:top={sopra}
  aria-labelledby="segnala-titolo"
  oncancel={(e) => {
    e.preventDefault();
    onAnnulla();
  }}
  onclick={(e) => {
    if (e.target === e.currentTarget) onAnnulla();
  }}
>
  <div class="scheda">
    <h2 id="segnala-titolo">{t.segnalaTitolo}</h2>
    <div class="scorri">
      <p class="coppia">{affine !== null ? t.segnalaSottotitoloCoppia(parola, affine) : parola}</p>
      <p class="cat">{t.segnalaCategoria(categoria)}</p>
      {#each MOTIVI as { motivo, testo } (motivo)}
        <label class="riga">
          <input type="checkbox" checked={motivi.includes(motivo)} onchange={(e) => alterna(motivo, e.currentTarget.checked)} />
          <span>{testo}</span>
        </label>
      {/each}

      <label class="campo">
        <span class="et">{t.segnalaCommento}</span>
        <textarea
          rows="3"
          value={nota}
          oninput={(e) => (nota = e.currentTarget.value.slice(0, MAX_COMMENTO))}
        ></textarea>
        <span class="conta">{t.segnalaContatore(nota.length, MAX_COMMENTO)}</span>
      </label>

      <p class="sezione">{t.segnalaProponi}</p>
      <label class="campo">
        <span class="et">{t.segnalaProponiParola}</span>
        <input
          type="text"
          value={propParola}
          autocapitalize="sentences"
          enterkeyhint="next"
          oninput={(e) => (propParola = e.currentTarget.value.slice(0, MAX_PROPOSTA))}
        />
      </label>
      <label class="campo">
        <span class="et">{t.segnalaProponiAffine}</span>
        <input
          type="text"
          value={propAffine}
          autocapitalize="sentences"
          enterkeyhint="done"
          oninput={(e) => (propAffine = e.currentTarget.value.slice(0, MAX_PROPOSTA))}
        />
      </label>
      {#if propostaMezza || propostaUguale}
        <p class="errore" role="alert">{propostaMezza ? t.segnalaProponiManca : t.segnalaProponiUguali}</p>
      {/if}
    </div>
    <div class="azioni">
      <Pulsante variante="testo" onClick={onAnnulla}>{t.annulla}</Pulsante>
      <Pulsante variante="testo" disabilitato={!abilitato} onClick={salva}>{t.segnalaSalva}</Pulsante>
    </div>
  </div>
</dialog>

<style>
  /* Il <dialog> occupa l'area visibile (visualViewport) e fa da sfondo. */
  .sfondo {
    position: fixed; left: 0; right: 0; margin: 0; padding: var(--spazio-4); box-sizing: border-box;
    width: 100%; max-width: none; max-height: none; border: 0;
    background: rgba(0, 0, 0, 0.5);
    align-items: center; justify-content: center;
    overflow: hidden;
  }
  .sfondo[open] { display: flex; }
  .sfondo::backdrop { background: transparent; }
  .scheda {
    display: flex; flex-direction: column; box-sizing: border-box;
    width: 100%; max-width: 560px; max-height: 100%;
    padding: var(--spazio-5, 24px); border-radius: var(--raggio-l);
    background: var(--colore-superficie); color: var(--colore-su-superficie);
  }
  h2 { margin: 0 0 var(--spazio-4); font-size: 1.5rem; font-weight: 400; }
  .scorri { flex: 1; min-height: 0; overflow-y: auto; overscroll-behavior: contain; display: flex; flex-direction: column; gap: var(--spazio-2); }
  .coppia { margin: 0; font-size: 1rem; font-weight: 600; overflow-wrap: anywhere; }
  .cat { margin: 0; font-size: 0.875rem; }
  .riga { display: flex; align-items: center; gap: var(--spazio-3); min-height: var(--altezza-tocco); cursor: pointer; }
  .riga input { width: 22px; height: 22px; accent-color: var(--colore-primario); flex: none; }
  .sezione { margin: var(--spazio-2) 0 0; font-size: 0.875rem; font-weight: 600; }
  .campo { display: flex; flex-direction: column; gap: 2px; }
  .et { font-size: 0.75rem; color: var(--colore-su-superficie-variante); }
  textarea, input[type="text"] {
    box-sizing: border-box; width: 100%; padding: var(--spazio-3);
    border: 1px solid var(--colore-contorno); border-radius: var(--raggio-m);
    background: transparent; color: inherit; font: inherit; font-size: 1rem; /* niente zoom su iOS */
  }
  input[type="text"] { min-height: var(--altezza-tocco); }
  textarea { resize: vertical; }
  textarea:focus, input[type="text"]:focus { outline: 2px solid var(--colore-primario); outline-offset: 0; }
  .conta { align-self: flex-end; font-size: 0.75rem; color: var(--colore-su-superficie-variante); }
  .errore { margin: 0; font-size: 0.75rem; color: var(--colore-errore); }
  .azioni { display: flex; justify-content: flex-end; gap: var(--spazio-2); padding-top: var(--spazio-4); }
</style>
