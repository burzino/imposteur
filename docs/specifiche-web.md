# Impostore - Specifiche PWA (versione web)

Questo documento elenca SOLO le differenze rispetto a `docs/specifiche.md` (nel seguito "Android"). Per tutto ciò che non è qui citato valgono le sezioni Android: stesse schermate, stessi testi (identici a `app/src/main/res/values/strings.xml`), stesse regole di gioco, stesso formato di `parole.json`, stessi CA-xx. Codice in `web/` (Svelte 5 + TypeScript + Vite + vite-plugin-pwa); la logica in `web/src/game/` è il porting in TS puro di `game/`.

## W1. Perimetro

- Tutte le funzioni Android: due modalità, opzioni avanzate (4.2.1), salvataggio di partita e parole usate (6), Rivedi la parola (4.4.1), ordine di parola, contatore con "Azzera", temi, segnalazioni (4.6), restyling della distribuzione (4.3), tasto Home.
- Aggiunti: installazione come app (W5), funzionamento offline, hosting statico (W6).
- Le parole si leggono da `app/src/main/assets/parole.json`, senza copie (incluse nel build e nella cache offline).

## W2. Persistenza (sostituisce il § 6 per lo storage, non le regole)

- Storage: `localStorage`, con 4 chiavi distinte: configurazione, sessione (partita in corso + parole usate), aspetto/tema, segnalazioni. Nomi esatti nel contratto API.
- Tutte le regole di § 6 restano (correzione ai limiti, ignorare id non più presenti, cancellazione della partita in Rivela, Rivelazione k → Passaggio k al ripristino, stato di Rivedi non salvato).
- Sessione illeggibile o incoerente con `parole.json` (JSON non valido, indici fuori limite, categoria/parola/affine inesistenti): scartata in silenzio, senza messaggi né "Riprendi partita".
- `localStorage` non disponibile o che lancia eccezioni (navigazione privata, dati bloccati) o quota piena: l'app funziona senza salvataggio, in silenzio; nessun errore a schermo.
- I dati restano nel browser e nel dominio dell'utente: sono per-dispositivo e per-browser; cancellare i dati del sito li elimina. Nessuna rete, nessun account.

## W3. Protezione del ruolo (sostituisce FLAG_SECURE e ON_STOP in 4.3, 4.4.1)

- FLAG_SECURE e "schermo sempre acceso" non esistono sul web. Screenshot e registrazione non sono bloccabili: rischio accettato (fair play, come in 4.4.1).
- Schermo acceso: dove disponibile si usa la Screen Wake Lock API durante Distribuzione e Rivedi (decisione presa: approvato); se manca, si ignora in silenzio.
- Evento `visibilitychange` con pagina nascosta (cambio scheda, blocco schermo, app in background): Rivelazione(k) diventa Passaggio(k); in Rivedi si torna alla schermata di Gioco.
- Ricaricamento della pagina (F5, ripristino della scheda) e riapertura: come il ripristino Android, Rivelazione(k) riparte come Passaggio(k).
- Il rotare lo schermo NON ricrea lo stato: nessun effetto, il ruolo resta visibile (differenza da CA-29, CA-57; decisione presa: accettata).
- Ritardo di "Nascondi e passa" / "Nascondi e inizia": 600 ms dalla comparsa del ruolo, come in 4.3 (decisione presa: confermato).
- Tasto/gesto indietro del browser (history/popstate) durante la Distribuzione: non porta mai a un ruolo precedente; mostra il dialogo "Interrompere la partita?" ("La partita andrà persa.", "Interrompi" / "Continua a giocare"). In Gioco: stesso dialogo. Le altre schermate usano la history normale (la freccia indietro in app equivale al tasto indietro Android).
- Pressione lunga (4.3): vale per mouse, tocco e penna (pointer events); il rilascio, l'uscita del puntatore dal pulsante o `pointercancel` la annullano. Il menu contestuale del browser e la selezione del testo sono disattivati sul pulsante. Tastiera: il pulsante è raggiungibile con Tab; Invio/Spazio attivano la rivelazione senza pressione lunga (equivalente dell'azione di accessibilità).

## W4. Feedback tattile

- `navigator.vibrate`, se presente: una vibrazione leggera identica per tutti alla comparsa del ruolo e al passaggio (4.3). Se assente (iOS Safari, molti desktop) non si fa nulla, in silenzio: nessun messaggio, nessuna sostituzione sonora o visiva (non deve distinguere i ruoli).

## W5. Installazione e offline

- Manifest: nome "Impostore", `display: standalone`, `lang: it`, colori di tema/sfondo coerenti con l'aspetto scuro/chiaro, `start_url` e `scope` relativi al base path (W6).
- Icone: 192, 512 e maskable 512 PNG, più `apple-touch-icon` 180.
- Service worker (vite-plugin-pwa): precache di tutti gli asset, `parole.json` compreso; dopo il primo caricamento l'app funziona senza rete. Gli aggiornamenti si applicano al caricamento successivo alla pubblicazione (nessun dialogo); una partita in corso non viene mai interrotta dall'aggiornamento.
- Android/Chrome: invito all'installazione tramite il meccanismo del browser; l'app non mostra pulsanti propri di installazione.
- iOS Safari: nessun invito automatico. Nella schermata "Come si gioca" una nota finale "Per installare l'app su iPhone: tocca Condividi, poi Aggiungi a Home." compare solo se il browser è iOS Safari non installato (decisione presa: nota approvata). Limiti noti: `navigator.vibrate` assente (W4); i dati di un sito non aggiunto a Home possono essere cancellati da Safari dopo giorni di inutilizzo.
- Cancellare i dati del sito cancella partita, configurazione e parole usate.

## W6. Hosting e base path

- Sito statico su GitHub Pages: base `/imposteur/`; poi dominio `imposteur.burzi.eu` con base `/`.
- Il base path è un parametro di build (es. variabile d'ambiente), usato per asset, manifest, service worker, `start_url` e routing; nessun percorso assoluto fisso nel codice. Il routing non deve richiedere configurazione del server: rotte in hash (decisione presa), senza fallback `404.html`; un ricaricamento in qualsiasi schermata non dà errore 404.
- Il `parole.json` si carica con URL relativo al base path.

## W7. Aspetto e temi

- Temi: "Sistema", "Chiaro", "Scuro", "Alto contrasto" (stesse etichette Android), salvati in W2. "Sistema" segue `prefers-color-scheme`.
- I colori dinamici (Material You) non esistono sul web: l'opzione è assente. Decisione presa: nessuna sostituzione; la palette fissa dell'app (stessa degli altri temi) è l'unica.
- Token di colore, tipografia, forme e layout: `docs/design.md` (fonte unica, comune ad Android). Font Roboto Flex incorporato nella PWA (file locale, precache del service worker, nessuna richiesta di rete per i font), come su Android (CA-98).
- Stessi testi, layout adattato a viewport verticale da telefono; su schermi larghi il contenuto resta in una colonna centrata (max 480 px).

## W8. Segnalazioni (sostituisce il file locale di 4.6)

- Le segnalazioni (stessi motivi, note, proposte e limiti di 4.6) si accumulano in `localStorage` (W2), una riga JSON per segnalazione, stesso formato di `segnalazioni.jsonl`.
- Esportazione: pulsante "Esporta segnalazioni" nelle impostazioni (decisione presa), visibile solo se esiste almeno una segnalazione; scarica il file `segnalazioni.jsonl` (Blob + download). Se `navigator.share` con file è disponibile, affianca la condivisione; altrimenti solo il download. Esportare non cancella le segnalazioni.
- Gli altri testi nuovi dell'esportazione: da definire nel contratto API (da aggiungere a `strings.xml`-equivalente).

## W9. Altre differenze

- Tasto indietro "esci dall'app" di Home (4.1): sul web non c'è azione; il tasto indietro del browser esce dal sito.
- Configurazione a passi (4.2): ogni passo è una voce di cronologia (`#/configurazione/1` … `#/configurazione/4`), quindi il tasto indietro del browser torna al passo precedente, dal passo 1 alla Home; "Modifica" del riepilogo e le pillole dell'indicatore aggiungono una voce.
- Home, Gioco, Rivela, Rivedi: nessun'altra differenza.
- "Parole usate": per browser e dispositivo, non condivise tra dispositivi (§ 2 invariato per il resto).

## W10. Criteri di accettazione web

Vitest = test automatici sulla logica; Browser = collaudo manuale/automatizzato in browser. I CA Android 1-21 (logica) valgono tali e quali sul porting TS (Vitest, stessi casi).

- **CA-W01** (Vitest) Tutti i test di logica Android (CA-01…CA-21, CA-61 e i CA su opzioni avanzate/ordine di parola/parole usate) hanno un equivalente Vitest con lo stesso esito, a parità di `Random` iniettato.
- **CA-W02** (Vitest) La serializzazione di configurazione e sessione in TS legge e scrive JSON equivalente a quello Android; campi mancanti → default; valori fuori limite → corretti (§ 6).
- **CA-W03** (Vitest) Una sessione con JSON non valido, indici fuori limite o categoria/parola inesistente è scartata senza eccezioni; "Riprendi partita" non compare (CA-49).
- **CA-W04** (Vitest) Con storage che lancia eccezioni la logica di salvataggio non propaga l'errore e l'app continua con i valori in memoria.
- **CA-W05** (Vitest) Il parser di `parole.json` accetta il file reale e rifiuta file illeggibili (CA-34), con le stesse regole di § 7.
- **CA-W06** (Browser) Con `visibilitychange` (pagina nascosta) mentre il ruolo è visibile, al ritorno compare "Passa il telefono a <nome>" dello stesso giocatore, senza ruolo (CA-29 sostituito). Lo stesso in Rivedi: si torna al Gioco (CA-57).
- **CA-W07** (Browser) Ricaricando la pagina con il ruolo visibile, "Riprendi partita" o la ripresa automatica mostra Passaggio k, non Rivelazione k (CA-46).
- **CA-W08** (Browser) Il tasto indietro del browser in Distribuzione e Gioco mostra "Interrompere la partita?" e non porta mai a un ruolo precedente (CA-35); "Continua a giocare" resta, "Interrompi" cancella la partita e va alla Configurazione.
- **CA-W09** (Browser) La pressione lunga (300 ms) su "Tieni premuto per scoprire" rivela il ruolo con mouse e tocco; rilascio anticipato, tocco breve o `pointercancel` non rivelano e azzerano la barra (CA-27, CA-67).
- **CA-W10** (Browser) Senza `navigator.vibrate` nessun errore in console e nessun messaggio; con `navigator.vibrate` la chiamata è identica per tutti i ruoli e le modalità (CA-65).
- **CA-W11** (Browser) Dopo il primo caricamento, in modalità aereo l'app si apre, si gioca una partita completa e le parole sono disponibili.
- **CA-W12** (Browser) Il manifest è valido (Lighthouse "installabile"), con icone 192/512/maskable; l'app installata si apre in `standalone` al base path corretto.
- **CA-W13** (Vitest + Browser) Con base `/imposteur/` e con base `/` la build carica asset, `parole.json`, manifest e service worker; ricaricare qualsiasi schermata non dà 404.
- **CA-W14** (Browser) Il tema scelto ("Sistema", "Chiaro", "Scuro", "Alto contrasto") persiste dopo il ricaricamento; l'opzione colori dinamici non compare.
- **CA-W15** (Browser) Dopo una segnalazione, "Esporta segnalazioni" (nelle impostazioni) scarica `segnalazioni.jsonl` con una riga JSON valida per segnalazione, nel formato di 4.6; con `navigator.share` disponibile compare anche la condivisione; senza segnalazioni il pulsante non è visibile.
- **CA-W16** (Browser) Con `localStorage` bloccato (navigazione privata) l'app si avvia e si gioca una partita; nessun errore a schermo.
- **CA-W17** (Browser) Il rotare lo schermo durante un ruolo visibile lascia il ruolo visibile senza perdere lo stato (differenza accettata da CA-29).
- **CA-W18** (Browser) Tutti i testi dell'interfaccia coincidono con `strings.xml` (CA-36), compresi i testi del restyling (CA-66…CA-68).
- **CA-W19** (Browser) I CA di interfaccia Android non citati sopra (CA-22…CA-26, CA-43…CA-55, CA-60, CA-62…CA-65, CA-80…CA-96, CA-99…CA-105) sono verificati sul web con le sole sostituzioni di W3 e W4.

## W11. Fuori perimetro (web)

Notifiche push, sincronizzazione tra dispositivi, account, backend, Wake Lock garantito su tutti i browser, blocco degli screenshot, colori dinamici, pubblicazione su store (PWA solo da browser/Home).

## W12. Punti da confermare

Nessun punto aperto. Decisioni prese (riportate nelle sezioni indicate):

1. Colori dinamici assenti, nessuna sostituzione (W7).
2. Nota iOS in "Come si gioca" approvata (W5).
3. Wake Lock approvato in Distribuzione e Rivedi (W3).
4. Rotte in hash (W6).
5. Rotazione che non azzera il ruolo: accettata (W3, CA-W17).
6. "Esporta segnalazioni" nelle impostazioni, visibile solo se ci sono segnalazioni (W8, CA-W15).
7. Ritardo di "Nascondi e passa" di 600 ms confermato (W3).
