<script lang="ts">
  import { getStato } from "../stato.svelte";
  import { vai, vaiAHome, vaiAConfigurazione } from "../rotte";
  import { t } from "../testi";
  import { mostraToast } from "../componenti/notifiche.svelte";
  import Pagina from "../componenti/Pagina.svelte";
  import Pulsante from "../componenti/Pulsante.svelte";
  import TestoAdattivo from "../componenti/TestoAdattivo.svelte";
  import DialogoSegnalazione from "./DialogoSegnalazione.svelte";
  import { segnalazione, type MotivoSegnalazione } from "../../data/segnalazioni";
  import type { Partita } from "../../game/partita";

  const stato = getStato();
  // Fissa la partita svelata: un cambio di partita durante la transizione non la altera.
  let fissata = $state<Partita | null>(stato.partita);
  $effect(() => {
    if (fissata === null && stato.partita !== null) fissata = stato.partita;
  });
  const partita = $derived(fissata);
  const nomiImpostori = $derived(
    partita
      ? [...partita.impostori].sort((a, b) => a - b).map((i) => partita.giocatori[i])
      : [],
  );
  const testoImpostori = $derived(
    nomiImpostori.length === 0
      ? t.rivelaTrappola
      : nomiImpostori.length === 1
        ? t.rivelaImpostore(nomiImpostori[0])
        : t.rivelaImpostori(nomiImpostori.join(", ")),
  );
  const trappola = $derived(partita !== null && partita.impostori.size === 0);
  let segnalando = $state(false);

  function adesso(): string {
    const d = new Date();
    const p = (n: number) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
  }

  function salva(motivi: MotivoSegnalazione[], nota: string, propParola: string | null, propAffine: string | null) {
    if (!partita) return;
    segnalando = false;
    stato.salvaSegnalazione(
      segnalazione({
        tipo: "coppia",
        istante: adesso(),
        categoriaId: partita.voce.categoriaId,
        parola: partita.voce.parola,
        affine: partita.voce.affine,
        modalita: partita.modalita,
        motivi,
        nota,
        propostaParola: propParola,
        propostaAffine: propAffine,
      }),
    );
    mostraToast(t.segnalaSalvata);
  }

  function rigioca() {
    if (stato.iniziaPartita()) vai("distribuzione", { sostituisci: true });
    else vaiAConfigurazione();
  }

  function cambia() {
    stato.terminaPartita();
    vaiAConfigurazione();
  }
</script>

<Pagina titolo={t.rivelaTitolo} onHome={vaiAHome}>
  {#if partita}
    <div class="corpo">
      <section class="hero">
        <TestoAdattivo testo={testoImpostori} classe={trappola ? "rivela-trappola" : "rivela-impostori"} maxRighe={3} />
      </section>
      <section class="carta">
        <p class="grande">{t.rivelaParola(partita.voce.parola)}</p>
        {#if partita.modalita === "PAROLA_AFFINE" && partita.voce.affine !== null}
          <p class="grande">{t.rivelaAffine(partita.voce.affine)}</p>
        {/if}
        <p class="media">{t.rivelaCategoria(partita.voce.categoriaNome)}</p>
      </section>
      {#if partita.promemoriaUltimaPossibilita && !trappola}
        <div class="promemoria" role="note">
          <svg class="info" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true" focusable="false">
            <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z" />
          </svg>
          <span>{t.rivelaPromemoria}</span>
        </div>
      {/if}
    </div>
  {/if}

  {#snippet piede()}
    <div class="piede">
      <Pulsante variante="pieno" onClick={rigioca}>{t.rivelaRigioca}</Pulsante>
      <Pulsante variante="tonale" onClick={cambia}>{t.rivelaCambiaImpostazioni}</Pulsante>
      <Pulsante variante="testo" onClick={() => (segnalando = true)}>{t.segnalaPulsante}</Pulsante>
    </div>
  {/snippet}
</Pagina>

{#if segnalando && partita}
  <DialogoSegnalazione
    parola={partita.voce.parola}
    affine={partita.modalita === "PAROLA_AFFINE" ? partita.voce.affine : null}
    categoria={partita.voce.categoriaNome}
    onAnnulla={() => (segnalando = false)}
    onSalva={salva}
  />
{/if}

<style>
  .corpo {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-3);
  }
  .hero {
    padding: var(--spazio-6) var(--spazio-5);
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xxl);
    background: var(--colore-contenitore-primario);
    color: var(--colore-su-contenitore-primario);
    text-align: center;
  }
  .corpo :global(.rivela-impostori) {
    font-size: 1.75rem;
    font-weight: 700;
  }
  .corpo :global(.rivela-trappola) {
    font-size: 1.375rem;
    font-weight: 700;
  }
  .carta {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
    padding: var(--spazio-5) 20px;
    border: var(--spessore-contorno) solid var(--colore-bordo-livello);
    border-radius: var(--raggio-xl);
    background: var(--colore-contenitore-superficie-basso);
  }
  .grande {
    font: var(--testo-titolo-sezione);
    overflow-wrap: anywhere;
  }
  .media {
    font: var(--testo-titolo);
    color: var(--colore-su-superficie-variante);
  }
  .info { flex: none; }
  .promemoria {
    display: flex;
    align-items: center;
    gap: var(--spazio-3);
    padding: var(--spazio-4);
    border: var(--spessore-contorno) solid var(--colore-su-contenitore-terziario);
    border-radius: var(--raggio-xl);
    text-align: left;
    font: var(--testo-corpo-piccolo);
    background: var(--colore-contenitore-terziario);
    color: var(--colore-su-contenitore-terziario);
  }
  .piede {
    display: flex;
    flex-direction: column;
    gap: var(--spazio-2);
    align-items: center;
  }
</style>
