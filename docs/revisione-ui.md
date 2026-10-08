# Revisione UI (parte non testata)

File letti: ImpostoreViewModel.kt, ImpostoreNavHost.kt, MainActivity.kt, strings.xml, schermate/*.kt (Home, Configurazione, Distribuzione, Gioco, Rivela, Regole, Componenti), AndroidManifest (solo attributi activity). Per game/data solo firme e Partita.kt/Repository.kt/SerializzazioneSessione.kt (conversione Rivelazione->Passaggio, riga ~106).

Verificati senza difetti: CA-22, 23, 24, 25 (nome duplicato e nessuna categoria), 27, 28 (back stack: Distribuzione/Gioco intercettano il back con BackHandler, la Distribuzione viene tolta con popUpTo al passaggio a Gioco), 29 (ON_STOP -> interrompiRivelazione, stato nel ViewModel), 30, 31, 32, 33, 34, 35, 43, 44, 45, 46, 47, 48, 49 (lato UI), 50 (lato UI: usate persistite in persisti()). Orientamento bloccato in portrait nel manifest, nessun configChanges: la rotazione non e' un caso reale, ma altri cambi di configurazione (tema, font) ricreano l'activity e passano da ON_STOP.

Conteggio: alta 1, media 3, bassa 5.

## Difetti

### D1 (alta) Schermata vuota dopo process death con back stack ripristinata
- Rif.: CA-45, CA-46, CA-47; ImpostoreNavHost.kt:38-117, ImpostoreViewModel.kt:35, 58-74.
- Scenario: partita in Distribuzione, Gioco o Rivela -> il sistema termina il processo in background -> l'utente riapre l'app dalla recente. La navigazione ripristina la rotta salvata (distribuzione/gioco/rivela) ma il ViewModel e' nuovo: `partita == null`. SchermataDistribuzione e SchermataGioco non disegnano nulla (Distribuzione: `if (partita != null)`, Gioco: `if (partita != null)`, Rivela: `return`). Ottenuto: schermo vuoto, uscita possibile solo col tasto indietro (dialogo o Config). La "Riprendi partita" in Home non e' raggiungibile e la partita salvata viene poi cancellata se l'utente conferma "Sì". Atteso: Home con "Riprendi partita" (la sessione e' salvata).
- Correzione: nel NavHost, se `!stato.caricamento && stato.partita == null` su rotte DISTRIBUZIONE/GIOCO/RIVELA fare `navigate(HOME){popUpTo(0)}` (LaunchedEffect); in alternativa avviare sempre da Home azzerando la back stack salvata.

### D2 (media) Doppio tocco su "Sono"/"Nascondi e passa" salta un giocatore o rivela il ruolo al giocatore sbagliato
- Rif.: CA-27, CA-28; ImpostoreNavHost.kt:71-72, ImpostoreViewModel.kt:141-145, SchermataDistribuzione.kt:123, 173.
- `avanza()` non e' idempotente: avanza dallo stato corrente, non da quello atteso. Scenario: il giocatore k tocca due volte velocemente "Nascondi e passa": il primo tocco porta a Passaggio k+1; se il secondo cade dove nella schermata successiva c'e' "Sono <k+1>" (posizione dipende dall'altezza del testo), il ruolo di k+1 compare sul telefono ancora in mano a k. Un doppio tocco su "Sono" porta a Rivelazione e poi, se il layout coincide, a Passaggio k+1 saltando la visione del ruolo di k.
- Correzione: `avanza(daStato: StatoDistribuzione)` che ignora la chiamata se lo stato attuale e' diverso da quello della schermata che l'ha emessa (passare `fase` dal composable); oppure debounce ~400 ms.

### D3 (media) Testo del messaggio di pool vuoto diverso dalle specifiche
- Rif.: CA-36, §5.2.6; strings.xml:43 (`config_pool_vuoto`).
- Ottenuto: "Nessuna parola disponibile con queste impostazioni". Atteso: "Le categorie scelte non contengono parole utilizzabili".
- Correzione: allineare la stringa (o la specifica, se si preferisce il testo attuale).

### D4 (media) Flash della soluzione della nuova partita durante "Nuova partita" da Rivela
- Rif.: ImpostoreNavHost.kt:103-111, ImpostoreViewModel.kt:124-139.
- `iniziaPartita()` sostituisce `stato.partita` subito; la schermata Rivela e' ancora composta durante l'animazione di navigazione (popUpTo(RIVELA) inclusive) e mostra impostori e parola della NUOVA partita. Se qualcuno guarda lo schermo, vede la soluzione prima della distribuzione.
- Correzione: far leggere a SchermataRivela una copia della partita tenuta con `remember` alla prima composizione (non `stato.partita` vivo), oppure navigare prima e creare la partita dopo (ma con rischio di Distribuzione senza partita: usare la copia e' piu' semplice). Stesso accorgimento per Gioco/Rivela dopo `terminaPartita` (schermata che si svuota durante l'uscita: solo estetico).

### D5 (bassa) "Riprendi partita" con doppio tocco apre una rotta in piu'
- Rif.: ImpostoreNavHost.kt:43-46.
- Il secondo clic trova `ripristinabile == null` -> `riprendiPartita()` restituisce false -> `navigate(DISTRIBUZIONE)` anche se lo stato e' Gioco. Con stato Gioco si crea la back stack Home, Gioco, Distribuzione che poi rinaviga a Gioco: non incoerente nei dati ma doppia schermata. Con Passaggio: due Distribuzione impilate; il back resta intercettato dal dialogo, quindi nessun ruolo precedente raggiungibile.
- Correzione: `launchSingleTop = true` su entrambe le navigate e non navigare se `riprendiPartita()` e' gia' stato consumato.

### D6 (bassa) Doppio tocco su "Inizia" consuma due parole
- Rif.: ImpostoreNavHost.kt:61-65, ImpostoreViewModel.kt:124-139. Due chiamate a `iniziaPartita()`: seconda partita sostituisce la prima e la parola della prima resta in `usate` (CA-50: nessun danno funzionale, pool si esaurisce prima).
- Correzione: ignorare `iniziaPartita()` se `partitaAttiva && distribuzione == iniziale && partita != null` appena creata, oppure debounce nel click.

### D7 (bassa) Ultime modifiche alla configurazione perse se il processo muore entro 500 ms
- Rif.: CA-26; ImpostoreViewModel.kt:76-84. Il salvataggio e' ritardato di 500 ms in viewModelScope e non viene forzato quando l'app va in background (solo su Indietro/Inizia).
- Correzione: chiamare `salvaOra()` anche su ON_STOP dell'activity (o nel NavHost con un LifecycleEventObserver).

### D8 (bassa) Dialoghi di conferma persi dopo ricreazione dell'activity
- Rif.: SchermataDistribuzione.kt:49, SchermataGioco.kt:31-32, SchermataHome.kt:33. `remember` invece di `rememberSaveable`: un cambio di configurazione chiude "Interrompere la partita?", "Rivelare i ruoli?" e la conferma "Esiste una partita in corso". Nessuna perdita di dati di gioco; nessun ruolo esposto (ON_STOP e' gia' passato).
- Correzione: `rememberSaveable`.

### D9 (bassa) Salvataggi in viewModelScope non garantiti all'uscita
- Rif.: ImpostoreViewModel.kt:176-186. `persisti()` lancia `repoSessione.salva` in viewModelScope; l'ordine delle scritture e' mantenuto (Main.immediate + coda DataStore), quindi nessuna race di sovrascrittura tra stati. Resta il rischio che, dopo ON_STOP, il processo venga ucciso prima che la scrittura di `Passaggio k` (da interrompiRivelazione) sia terminata: la sessione salvata resta `Rivelazione k`, ma il deserializzatore la converte comunque in Passaggio k, quindi il ruolo non e' mai visibile (CA-46 rispettata). Riportato solo per completezza.

## Da verificare sul dispositivo
1. Miniatura di Recents / app switcher: la interrompiRivelazione scatta su ON_STOP, non su ON_PAUSE; con la navigazione a gesti la miniatura puo' essere scattata mentre il ruolo e' ancora visibile. Valutare `FLAG_SECURE` sulla Distribuzione o interruzione anche su ON_PAUSE (attenzione: ON_PAUSE scatta anche con dialoghi di sistema).
2. Doppio tocco (D2) sull'effettivo layout: verificare se i due pulsanti cadono sulla stessa posizione con nomi corti.
3. Process death reale (`adb shell am kill it.imposteur` dopo background) in Distribuzione, Gioco e Rivela (D1).
4. Back predittivo (Android 14+): verificare che BackHandler intercetti il gesto sulla Distribuzione senza mostrare l'anteprima della schermata precedente.
