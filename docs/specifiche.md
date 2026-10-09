# Impostore - Specifiche funzionali (versione 1)

## 1. Perimetro

App Android per giocare a "Impostore" con un solo telefono che passa di mano in mano. Nessuna rete, nessun multiplayer, nessun account.

Incluso in v1:
- configurazione della partita (giocatori, nomi, impostori, modalità, categorie);
- distribuzione segreta dei ruoli, un giocatore alla volta;
- schermata di gioco con giocatore che inizia;
- rivelazione finale e nuova partita;
- opzioni avanzate facoltative (4.2.1), ordine di parola (5.6) e segnalazione di coppie (4.6);
- memorizzazione della configurazione tra un avvio e l'altro.

Aspetto grafico: `docs/design.md` è la fonte unica di token e layout, comune ad Android e PWA (font Roboto Flex incorporato in entrambe); vedi 8.3.

Discussione e voto avvengono a voce, fuori dall'app. Vedi sezione 9 per il fuori perimetro.

## 2. Glossario

- **Civile**: giocatore non impostore.
- **Impostore**: giocatore con il ruolo speciale.
- **Parola** (parola segreta): la parola dei civili.
- **Parola affine**: parola vicina per significato alla parola, mostrata all'impostore solo in modalità "Parola affine".
- **Categoria**: gruppo di parole (es. "Animali").
- **Sessione**: tempo di vita del processo dell'app. Le parole usate si ricordano per la sessione, non tra un avvio e l'altro.
- **Partita**: un ciclo Distribuzione - Gioco - Rivela.

## 3. Modalità di gioco

1. **"Impostore senza parola"**: i civili vedono la parola. L'impostore vede "Sei l'impostore" e, se l'opzione è attiva, la categoria. Non vede alcuna parola.
2. **"Parola affine"** (stile Undercover): i civili vedono la parola, gli impostori vedono la parola affine. La schermata è identica per tutti (nessuna etichetta di ruolo, nessuna differenza grafica), quindi l'impostore non sa di esserlo.

## 4. Flusso delle schermate

Home → Configurazione → Distribuzione ruoli → Gioco → Rivela → (Rigioca (stessi giocatori) → Distribuzione ruoli | Modifica giocatori e opzioni → Configurazione).

### 4.1 Home
- Contenuto: titolo "Impostore", pulsanti "Riprendi partita" (solo se esiste una partita salvata valida, vedi 6; posto sopra "Nuova partita"), "Nuova partita" e "Come si gioca".
- "Riprendi partita" ripristina la partita salvata: apre la Distribuzione ruoli allo stato Passaggio del giocatore k salvato, oppure la schermata di Gioco se la partita era in stato Gioco (stesso giocatore iniziale).
- "Nuova partita" apre la Configurazione. Se esiste una partita salvata valida compare prima la conferma "Esiste una partita in corso. Iniziarne una nuova?" con pulsanti "Annulla" (resta in Home, partita salvata intatta) e "Nuova partita" (apre la Configurazione; la partita salvata viene cancellata quando si avvia la nuova partita con "Inizia", vedi 6). "Come si gioca" apre una schermata di testo statico con le regole delle due modalità.
- Tasto indietro: esce dall'app.

### 4.2 Configurazione
La Configurazione è un percorso di 5 passi: 1 "Giocatori", 2 "Modalità", 3 "Opzioni", 4 "Categorie", 5 "Riepilogo". Tutti i campi sono precompilati con l'ultima configurazione salvata (o con i default al primo avvio). Ogni ingresso ("Nuova partita", "Modifica giocatori e opzioni", "Interrompi" da Gioco) apre il passo 1; il passo corrente non è salvato. Layout dei passi: `docs/design.md` 4.4.

Passo di ogni campo: nomi dei giocatori (con "+ Aggiungi giocatore" e rimozione) = passo 1; modalità, numero impostori, "Impostori a sorpresa" = passo 2 (in quest'ordine: la modalità è la scelta principale e gli impostori seguono; impostori e sorpresa restano adiacenti); opzioni avanzate (4.2.1) = passo 3; categorie e "Parole ancora da giocare" = passo 4; il passo 5 riassume tutto.

| Campo | Controllo | Regole | Default |
|---|---|---|---|
| Giocatori | un campo testo per giocatore, etichetta/segnaposto "Giocatore n"; sotto i campi il pulsante testuale "+ Aggiungi giocatore"; ogni campo ha una "x" che rimuove il giocatore. Nessun contatore: il numero di giocatori è il numero di campi | da 3 a 20 campi; nome facoltativo; spazi iniziali/finali rimossi; max 20 caratteri; nome vuoto → "Giocatore n" (n = posizione del campo); nomi duplicati (senza distinzione maiuscole/minuscole) non ammessi | 4 campi vuoti (mostra "Giocatore 1…4") |
| Numero impostori (etichetta "Impostori (massimo)" con "Impostori a sorpresa" attivo, vedi 4.2.1) | stepper "−" / "+" con valore visibile, passo 2 | minimo 1; massimo = floor((giocatori − 1) / 2), cioè i civili sono sempre più degli impostori (3-4 giocatori → 1, 5-6 → 2, 7-8 → 3, …, 20 → 9) | 1 |
| Impostori a sorpresa | interruttore, subito sotto il numero di impostori (passo 2); non fa parte delle Opzioni avanzate | se attivo, K è scelto a caso in 1..massimo a ogni partita (5.5) | spento |
| Modalità | scelta singola: "Impostore senza parola" / "Parola affine"; sotto ciascuna una riga di spiegazione: "I civili conoscono la parola, l'impostore deve bluffare." (sotto "Impostore senza parola"), "L'impostore riceve una parola simile ma diversa." (sotto "Parola affine") | obbligatoria | "Impostore senza parola" |
| L'impostore vede la categoria | interruttore, nel gruppo "Ruoli" delle Opzioni avanzate (4.2.1) | visibile e attivo solo se la modalità è "Impostore senza parola"; nascosto (non applicato) in "Parola affine" | attivo |
| Categorie | lista con caselle di selezione, più "Seleziona tutte" / "Deseleziona tutte" | almeno una selezionata | tutte selezionate |
| Parole ancora da giocare | testo "Parole ancora da giocare: X / Y" e pulsante "Azzera" | vedi sotto | calcolato |
| Opzioni avanzate | passo 3, tre gruppi sempre visibili, vedi 4.2.1 | tutte facoltative | tutte spente |

Regole di validazione:
- All'apertura i campi del passo 1 sono tanti quanti i giocatori della configurazione salvata (4 con la configurazione predefinita), con i nomi salvati.
- "+ Aggiungi giocatore" aggiunge in fondo un campo vuoto (segnaposto "Giocatore n" con n = nuova posizione) e gli dà il fuoco. Con 20 campi il pulsante è disabilitato e il suo testo diventa "Massimo 20 giocatori". Aggiungere non cambia il numero di impostori.
- La "x" di ogni campo rimuove il giocatore (non svuota il testo). È assente quando i campi sono 3. Rimuovendo, i giocatori successivi scalano di una posizione portando con sé il nome; un nome vuoto segue la nuova posizione (es. il quarto campo vuoto, dopo la rimozione del secondo, diventa "Giocatore 3"). Se il numero di impostori supera il nuovo massimo, diventa il nuovo massimo (min(impostori, massimo)). Nessun nome oltre il numero di campi resta memorizzato.
- Il formato salvato della configurazione non cambia (`numeroGiocatori` = numero di campi, `nomi` = i nomi dei campi, "" per i vuoti).
- Se un nome duplicato è presente, il campo mostra l'errore "Nome già usato" e "Avanti" del passo 1 è disabilitato; la validità è ricalcolata a ogni aggiunta o rimozione. Il nome di default "Giocatore n" è considerato per il confronto.
- Se nessuna categoria è selezionata: messaggio "Seleziona almeno una categoria" e "Avanti" del passo 4 disabilitato.
- In modalità "Parola affine" una parola senza affine non può essere estratta (vedi 7).
- La "x" ha nome accessibile "Rimuovi giocatore n" e area di tocco >= 48 dp. Sulla tastiera dei campi nome è presente il tasto Avanti, che porta al campo nome successivo.
- Quando "Avanti" è disabilitato, sopra i pulsanti compare in rosso l'errore del passo corrente con il suo testo dedicato ("Nome già usato", "Seleziona almeno una categoria", "Le categorie scelte non contengono parole utilizzabili"); se l'errore non ha un testo dedicato compare "Controlla la configurazione".
- "Parole ancora da giocare: X / Y": Y = dimensione del pool iniziale (5.2 passo 1) per le categorie e la modalità attualmente scelte; X = parole di quel pool non presenti tra le usate. Si aggiorna al variare di categorie e modalità. Il pulsante "Azzera" è abilitato solo se X < Y e chiede conferma "Rimettere in gioco tutte le parole delle categorie scelte?" con pulsanti "Annulla" / "Azzera". Confermando, dalle usate si tolgono solo le parole delle categorie scelte (le usate di altre categorie restano); "Annulla" non cambia nulla.
- La barra azioni (errore del passo e pulsanti) è fissa in fondo allo schermo, fuori dall'area che scorre: resta visibile in ogni passo con qualsiasi numero di giocatori (fino a 20) e con la tastiera aperta (sale sopra la tastiera). Il contenuto scorrevole ha spazio finale sufficiente perché l'ultimo elemento non resti coperto.
- **Indicatore dei passi**: pillole semplici (una per passo, nessuna barra ondulata) con etichetta "Passo k di 5 · Nome" (nomi: "Giocatori", "Modalità", "Opzioni", "Categorie", "Riepilogo"); le pillole distinguono passi fatti, corrente e da fare.
- **Titoli dei passi**: 1 "Chi gioca?" / "Come vi chiamate. Da 3 a 20 giocatori."; 2 "Come si gioca?" / "Scegli la modalità e quanti impostori."; 3 "Opzioni avanzate" / "Sono tutte facoltative: puoi andare avanti senza toccarle."; 4 "Categorie" / "Da quali argomenti pescare le parole."; 5 "Tutto pronto?" / "Controlla e inizia."
- **Pulsanti**: passo 1 solo "Avanti"; passi 2, 3 e 4 "Indietro" e "Avanti"; passo 5 "Indietro" e "Inizia". "Avanti" è disabilitato solo se il passo corrente ha un errore (passo 1: nome duplicato; passi 2 e 3: mai; passo 4: nessuna categoria o nessuna parola utilizzabile).
- **"Inizia" nella barra superiore**: pulsante testo disponibile da tutti i passi 1-4, compreso il passo 1 (il passo 5 ha già "Inizia" come pulsante). Se la configurazione è valida salva e apre la Distribuzione ruoli; altrimenti porta al primo passo non valido (in ordine 1, 2, 3, 4; in pratica 1 o 4: gli errori di impostori sono raggiungibili solo con dati salvati incoerenti) e mostra il suo errore (se è il passo corrente, resta lì e mostra l'errore).
- **Indietro**: pulsante "Indietro", freccia della barra e tasto di sistema tornano al passo precedente, senza essere mai bloccati dalla validazione; dal passo 1 tornano alla Home salvando la configurazione.
- **Passo 5 "Riepilogo"**: quattro righe, ciascuna con "Modifica" che porta al passo indicato (indietro riporta al riepilogo): "Giocatori" (passo 1; valore "N giocatori"; seconda riga i nomi separati da ", ", i vuoti come "Giocatore n"); "Modalità" (passo 2; nome della modalità, seconda riga il numero di impostori: "1 impostore" / "M impostori"; con "Impostori a sorpresa" attivo e massimo > 1 "fino a M impostori", con massimo 1 resta "1 impostore"); "Opzioni avanzate" (passo 3; riepilogo di 4.2.1 e badge "N attive"); "Categorie" (passo 4; "1 categoria" / "N categorie", seconda riga "Parole ancora da giocare: X / Y"). Il numero di impostori sta in "Modalità" perché si modifica al passo 2.
- La configurazione è salvata a ogni cambio di passo, a ogni uscita e con "Inizia"; vale il formato di 6.

### 4.2.1 Opzioni avanzate
Passo 3 della Configurazione (tra Modalità e Categorie): titolo "Opzioni avanzate" con badge "N attive" (N = opzioni contate attive, vedi sotto; assente se N = 0); i tre gruppi sono sempre visibili (nessuna fisarmonica). Tutte le opzioni sono spente per default e sono salvate con la configurazione (6). "Impostori a sorpresa" non è fra le Opzioni avanzate: sta nel passo 2 (4.2).

- **Riepilogo** (riga "Opzioni avanzate" del passo 5): elenco separato da virgole e spazio (", ") delle sole opzioni avanzate non predefinite, in quest'ordine: i nomi brevi degli interruttori attivi ("Non parla per primo", "Trappola", "Ordine casuale", "Promemoria"), "senza categoria" (se "L'impostore vede la categoria" è spento e la modalità è "Impostore senza parola"), "N giri" (se "Giri di indizi" > 1). Se vuoto: "Nessuna opzione attiva". Esempio: "Ordine casuale, 2 giri". Il numero di impostori e "Impostori a sorpresa" NON sono in questo elenco: stanno nella riga "Modalità" (4.2). Il badge conta solo le opzioni elencate in "Badge" (quindi non "senza categoria"). Il testo "Regole classiche" non esiste più. Il riepilogo riflette sempre lo stato corrente.
- **Gruppo "Ruoli"**:
  - "L'impostore vede la categoria" (interruttore, spostato qui dalla posizione di 4.2; stesse regole: visibile solo in modalità "Impostore senza parola"; default attivo; NON contata nel badge);
  - "L'impostore non parla per primo" (interruttore);
  - "Partita trappola" (interruttore): ogni tanto nessuno è impostore (probabilità 10%).
- **Gruppo "Turni"**:
  - "Ordine casuale" (interruttore);
  - "Giri di indizi" (scelta singola 1 | 2 | 3, default 1; contata nel badge se > 1).
- **Gruppo "Fine partita"**:
  - "Promemoria ultima possibilità" (interruttore).
- **Badge**: N = numero di interruttori attivi tra "L'impostore non parla per primo", "Partita trappola", "Ordine casuale", "Promemoria ultima possibilità", più 1 se "Giri di indizi" > 1.
- **Etichetta del selettore impostori** (passo 2): con "Impostori a sorpresa" attivo diventa "Impostori (massimo)"; altrimenti "Numero impostori". Limiti invariati (min 1, max floor((giocatori − 1) / 2)). Nel riepilogo (passo 5) con "Impostori a sorpresa" attivo: "fino a N impostori" (N > 1), "1 impostore" se il massimo è 1.
- Nessuna regola di validazione aggiuntiva: ogni combinazione è ammessa. "Partita trappola" e "Impostori a sorpresa" sono compatibili.
- In modalità "Parola affine" tutte le opzioni restano disponibili; "L'impostore vede la categoria" resta nascosta (4.2).

### 4.3 Distribuzione ruoli
La distribuzione segue l'ordine di parola (`ordine`, 5.6): il telefono passa di mano nell'ordine in cui i giocatori parleranno, e il primo a vedere il ruolo è `primoGiocatore`. Se si parla 1, 3, 2, il telefono passa a 1, poi 3, poi 2. Nel seguito k è la posizione nella sequenza (1..N) e il giocatore mostrato è `ordine[k]` (nomi, iniziali e colori sono quelli del giocatore, non della posizione). Per ogni posizione si alternano tre stati:

1. **Passaggio**: testo "Passa il telefono a" seguito dal nome del giocatore in grande (equivale a "Passa il telefono a <nome>"), avviso "Gli altri non guardino lo schermo", indicatore "Giocatore k di N", cerchio con l'iniziale del nome e, in basso, il pulsante a pressione lunga "Tieni premuto per scoprire" con sotto la didascalia "Sono <nome> — tieni premuto". Nessun ruolo visibile.
   - Fila di avatar (in alto, decorativa: l'informazione è nel testo "Giocatore k di N"): un cerchio con l'iniziale per ogni giocatore, nell'ordine di parola (lo stesso della distribuzione); già visti = pieni con spunta, corrente = evidenziato con anello, successivi = attenuati. Fino a 8 giocatori stanno tutti sulla riga; oltre 8 la riga scorre e resta centrata sul corrente.
   - Pressione lunga: tenendo premuto il pulsante una barra si riempie nella **durata della pressione** scelta nelle Impostazioni (4.7; predefinita 150 ms, da 0 a 1000 ms); a barra piena il ruolo viene rivelato (stato 2). Rilasciando prima, la barra torna a zero e non succede nulla. Con durata > 0 un semplice tocco non rivela.
   - Durata 0 ms ("solo tocco"): la barra non c'è e il ruolo si rivela **al rilascio** del dito (o del mouse) sul pulsante, non alla pressione. Se il dito esce dal pulsante o il gesto è annullato dal sistema (cancel) prima del rilascio, non succede nulla. Motivo: rivelare alla pressione farebbe comparire il ruolo a ogni contatto involontario (telefono che passa di mano, tasca, dito che sfiora) prima che chi deve guardare sia pronto; al rilascio il giocatore può ancora annullare spostando il dito fuori, e il pulsante si comporta come un normale pulsante. In questa modalità il pulsante legge "Tocca per scoprire" e la didascalia "Sono <nome> — tocca"; tutto il resto (stati, 600 ms, accessibilità) è invariato.
   - Il pulsante è disabilitato (attenuato, ignora la pressione) nei primi 600 ms dopo la comparsa del giocatore (anti doppio tocco). Per l'accessibilità l'azione "Scopri il ruolo" attiva la rivelazione senza pressione lunga.
2. **Rivelazione**: la carta si gira (rotazione 3D) mostrando il contenuto del ruolo (vedi tabella) e il pulsante "Nascondi e passa" ("Nascondi e inizia" per l'ultimo giocatore). Il pulsante si attiva 600 ms dopo la comparsa del ruolo (prima è disabilitato). L'animazione è identica per tutti i ruoli.
3. Il tocco su "Nascondi e passa" nasconde il ruolo e passa allo stato 1 del giocatore successivo. Il tocco su "Nascondi e inizia" (ultimo giocatore) mostra l'overlay "Tutti pronti!": schermata a tutto schermo con il testo "Tutti pronti!" e sotto "Che il bluff abbia inizio"; dopo 1200 ms (o al tocco, che lo salta) si apre la schermata di Gioco. La partita è già nello stato Gioco quando compare l'overlay (salvataggio, ripristino e tasto indietro seguono le regole del Gioco).

Contenuto dello stato 2:

| Giocatore | Modalità "Impostore senza parola" | Modalità "Parola affine" |
|---|---|---|
| Civile | "La parola è:" + parola | "La tua parola è:" + parola |
| Impostore | "Sei l'impostore" + (se opzione attiva) "Categoria: <nome categoria>" | "La tua parola è:" + parola affine |

Regole:
- Ogni ruolo si può rivelare una sola volta in Distribuzione (la schermata di Gioco offre poi "Rivedi la parola", vedi 4.4.1): dopo "Nascondi e passa" non è possibile tornare indietro né rivedere il ruolo di un giocatore già passato. Il tasto indietro di sistema nelle schermate di Distribuzione è disabilitato oppure chiede conferma "Interrompere la partita?" con messaggio "La partita andrà persa." e pulsanti "Interrompi" (cancella la partita salvata e va alla Configurazione) / "Continua a giocare" (resta); non porta mai a un ruolo precedente.
- Se l'app va in background o la schermata viene ricreata (rotazione, ripristino) durante lo stato 2, si torna allo stato 1 dello stesso giocatore (il ruolo non resta mai visibile senza interazione).
- Lo stato corrente (Passaggio k / Rivelazione k) viene salvato a ogni cambio (vedi 6). Dopo la chiusura dell'app e "Riprendi partita", uno stato salvato Rivelazione k riparte come Passaggio k: il ruolo non è mai visibile all'avvio. Il giocatore k può quindi rivedere il proprio ruolo una sola volta ancora, solo dopo la pressione lunga su "Tieni premuto per scoprire".
- Lo schermo resta acceso durante la Distribuzione.
- La schermata di Passaggio non mostra mai informazioni sul ruolo.
- In modalità "Parola affine" lo stato 2 di civili e impostori è identico per layout, etichette e colori.
- Vibrazione (feedback tattile): alla comparsa del ruolo (stato 2) una sola vibrazione leggera, IDENTICA per tutti i giocatori (civili e impostori) in entrambe le modalità: una vibrazione diversa (es. doppia) si sentirebbe e rivelerebbe l'impostore a chi sta vicino (indistinguibilità, CA-10, CA-30). Una vibrazione leggera anche al passaggio al giocatore successivo (ingresso nello stato 1), uguale per tutti.

### 4.4 Schermata di gioco
- Testo "Si gioca!" e "Parla per primo: <nome>" dove <nome> è il giocatore che inizia, scelto a caso uniformemente tra tutti i giocatori (indipendentemente dal ruolo).
- Ordine di parola: sotto "Parla per primo: <nome>" l'elenco numerato dei giocatori nell'ordine in cui parlano (5.6), nomi effettivi, senza ruoli. Con "Giri di indizi" > 1 l'elenco si ripete per ogni giro con le intestazioni "Giro 1", "Giro 2", … "Giro n"; con 1 giro nessuna intestazione. L'ordine è solo visualizzato: nessun avanzamento tra giri.
- Testo di istruzione: "Discutete a voce, poi votate. Quando avete deciso, premete Rivela."
- Pulsante "Rivela" (in basso) → conferma "Rivelare i ruoli?" con messaggio "Verrà mostrato chi era l'impostore." e pulsanti "Rivela" / "Non ancora" → schermata Rivela ("Non ancora" chiude il dialogo e resta in Gioco).
- Il ruolo non è mostrato in questa schermata.
- Pulsante piccolo (secondario, meno evidente di "Rivela") "Rivedi la parola" → schermata Rivedi la parola (4.4.1).
- Tasto indietro: chiede "Interrompere la partita?" con messaggio "La partita andrà persa." e pulsanti "Interrompi" (→ Configurazione) / "Continua a giocare" (resta in Gioco).

### 4.4.1 Rivedi la parola
Funzione raggiungibile solo dalla schermata di Gioco, dopo la Distribuzione. Eccezione esplicita alla regola "una sola rivelazione" di 4.3 (che resta valida per la Distribuzione).

Stati:
1. **Elenco**: intestazione "Rivedi la parola", testo "Tocca il tuo nome" e l'elenco di tutti i giocatori (nomi effettivi) nell'ordine di parola (5.6, lo stesso della Distribuzione e dell'elenco di Gioco), uno per riga, toccabili. Nessun ruolo visibile. Tasto indietro (di sistema e freccia in alto): torna alla schermata di Gioco, senza conferma.
2. **Passaggio**: toccando un nome, stesso Passaggio della Distribuzione (stato 1 di 4.3): testo "Passa il telefono a" + nome in grande, avviso "Gli altri non guardino lo schermo", cerchio con l'iniziale e pulsante a pressione lunga "Tieni premuto per scoprire" con didascalia "Sono <nome> — tieni premuto" (barra che si riempie nella durata della pressione delle Impostazioni, 4.7; con 0 ms nessuna barra, rivelazione al rilascio e testi "Tocca per scoprire" / "Sono <nome> — tocca"; pulsante disabilitato nei primi 600 ms, azione di accessibilità "Scopri il ruolo", come in 4.3). Differenze: nessuna fila di avatar e nessun indicatore "Giocatore k di N". Nessun ruolo visibile. Tasto indietro: torna all'Elenco.
3. **Rivelazione**: dopo la pressione lunga (barra piena), stesso contenuto, layout, etichette e colori dello stato 2 della Distribuzione (tabella di 4.3, secondo ruolo e modalità del giocatore), con pulsante "Nascondi". Il tocco su "Nascondi" nasconde il ruolo e torna alla schermata di Gioco. Tasto indietro: come "Nascondi".

Regole:
- Si può rivedere più volte, per qualunque giocatore, senza limiti. Non cambia ruoli, parola, giocatore iniziale né le parole usate.
- Se l'app va in background o la schermata viene ricreata (rotazione, ripristino) in un qualsiasi stato di Rivedi la parola, si torna alla schermata di Gioco; il ruolo non resta mai visibile senza interazione.
- Schermata protetta (FLAG_SECURE) in tutte le schermate di Rivedi la parola: niente screenshot, registrazione schermo né anteprima in Recents con contenuto visibile.
- Lo stato della revisione (Elenco / Passaggio / Rivelazione, giocatore scelto) non viene salvato nella sessione (vedi 6): dopo la chiusura dell'app "Riprendi partita" porta sempre alla schermata di Gioco.
- Lo schermo resta acceso durante la Rivedi la parola.
- Note (fair play): nulla impedisce a un giocatore di aprire il ruolo di un altro. Il rischio è accettato; il gioco si basa sulla correttezza dei partecipanti.

### 4.5 Rivela
Contenuto:
- Intestazione "Il risultato".
- "L'impostore era: <nome>" (singolare) oppure "Gli impostori erano: <nome1>, <nome2>, …" (plurale), nell'ordine della lista giocatori.
- "La parola era: <parola>".
- Solo in modalità "Parola affine": "La parola affine era: <affine>".
- Categoria: "Categoria: <nome categoria>".
- Pulsanti: "Rigioca (stessi giocatori)" (stessi giocatori e impostazioni: nuova parola, nuovi impostori, nuovo giocatore iniziale → Distribuzione ruoli) e "Modifica giocatori e opzioni" (→ Configurazione, precompilata).
- Tasto indietro: come "Modifica giocatori e opzioni".
- Partita trappola (nessun impostore): al posto della riga sull'impostore/i compare "Nessun impostore: era una partita trappola!", seguita da parola, affine (solo modalità affine) e categoria come sopra.
- Riquadro promemoria: se "Promemoria ultima possibilità" è attivo e c'è almeno un impostore, sotto il risultato compare un riquadro con il testo "L'impostore scoperto può provare a indovinare la parola: se ci riesce, vince lui!". Solo testo, nessun controllo; assente nella partita trappola.
- Entrando in Rivela la partita salvata viene cancellata (la Home non mostra più "Riprendi partita" fino a una nuova partita).

### 4.6 Segnalazioni
Dall'app si può segnalare una coppia (parola, affine) o un problema dell'app; le segnalazioni sono salvate in locale in un file `segnalazioni.jsonl` (una riga JSON per segnalazione), nessun invio in rete.
- Segnalazione di tipo "coppia": motivi (troppo simili, troppo diverse, poco conosciuta, categoria sbagliata), nota facoltativa (max 500 caratteri dopo trim) e, dalla v1.4, **proposta di coppia**: due campi facoltativi, parola e affine (trim, max 40 caratteri; vuoto = assente).
- Proposta valida solo se entrambi i campi sono compilati e diversi (senza distinzione maiuscole/minuscole). Con un solo campo compilato, o con due parole uguali, "Salva" è disattivato e compare un avviso.
- "Salva" è disattivato anche se non c'è alcun motivo, nota o proposta valida.
- Le righe salvate prima della proposta restano leggibili (proposta assente); le righe malformate sono saltate.

### 4.7 Impostazioni
Si apre dall'icona Impostazioni della Home (descrizione "Apri le impostazioni"); barra con titolo "Impostazioni" e freccia indietro. Sono impostazioni del dispositivo (non della partita): si applicano e si salvano subito, senza pulsante di conferma. Gruppi, dall'alto:
1. **Tema**: "Sistema", "Chiaro", "Scuro", "Alto contrasto"; su Android 12+ l'interruttore "Colori del telefono" ("Usa i colori dello sfondo (Android 12+)"), assente sul web.
2. **Pressione per scoprire** (nuovo, subito sotto Tema e sopra Segnalazioni): cursore con etichetta "Durata della pressione", descrizione "Quanto tempo tenere premuto per scoprire il ruolo. Con 0 basta un tocco." e valore corrente visibile accanto all'etichetta, nel formato "<n> ms" (es. "150 ms"; "0 ms" incluso).
   - Intervallo 0-1000 ms, passo 50 ms (21 posizioni), predefinito 150 ms.
   - Vale per Distribuzione (4.3) e Rivedi la parola (4.4.1), su Android e PWA; il cambio ha effetto dal successivo Passaggio mostrato. Il blocco anti doppio tocco di 600 ms non dipende da questa impostazione.
   - Il cursore ha un solo valore per volta: spostarlo aggiorna subito il testo del valore e salva (al rilascio del cursore o a ogni passo da tastiera).
   - Accessibilità: il cursore ha nome "Durata della pressione" e valore letto "<n> millisecondi" ("0 millisecondi, basta un tocco" a 0); su Android azioni di aumento/diminuzione di un passo per il lettore di schermo; sul web è un `<input type="range">` nativo: frecce ±50 (`step=50`), Home 0, End 1000, con `aria-valuetext` "<n> millisecondi".
3. **Segnalazioni** (4.6).
Persistenza e normalizzazione: vedi 6.

## 5. Regole di gioco

### 5.1 Assegnazione ruoli
- Con N giocatori e K impostori (1 ≤ K ≤ floor((N−1)/2)), si scelgono K giocatori distinti a caso, con distribuzione uniforme; tutti gli altri sono civili. (Eccezioni con le opzioni avanzate: vedi 5.5.)
- Gli impostori non vengono comunicati tra loro (ciascuno vede solo la propria schermata).
- La distribuzione dei ruoli segue l'ordine di parola `ordine` (5.6), non la lista dei giocatori: il passo k mostra il giocatore `ordine[k]`; il primo è `primoGiocatore`. Per sessioni salvate senza `ordine` vale la rotazione da `primoGiocatore` (6), quindi anche la distribuzione la segue.

### 5.2 Scelta della parola
1. Si costruisce il pool: tutte le coppie (parola, affine) delle categorie selezionate. In modalità "Parola affine" si escludono le parole con affine assente o vuoto.
2. Si escludono le parole già usate nella sessione (insieme "usate").
3. Se il pool risultante è vuoto, l'insieme "usate" viene azzerato (per il solo pool corrente) e si estrae di nuovo dal pool completo; la nuova parola non coincide con quella dell'ultima partita, se il pool ha almeno 2 parole.
4. Si estrae una parola a caso (uniforme) e la si aggiunge a "usate".
5. L'insieme "usate" è persistente (vedi 6): sopravvive alla chiusura dell'app e si azzera solo per esaurimento del pool (passo 3 sopra) o con "Azzera" in Configurazione (4.2), che toglie dalle usate solo le parole delle categorie scelte. Non si azzera cambiando impostazioni o categorie.
7. Parole rimanenti per un pool (categorie e modalità scelte) = parole del pool non presenti tra le usate; l'azzeramento per pool rimuove dalle usate solo le parole delle categorie scelte (indipendentemente dalla modalità).
6. Se il pool iniziale (passo 1) è vuoto, "Avanti" del passo 4 Categorie è disabilitato con messaggio "Le categorie scelte non contengono parole utilizzabili" ("Inizia" porta a quel passo).

### 5.3 Giocatore che inizia
Uniforme tra tutti gli N giocatori, estratto a ogni partita.

### 5.4 Casualità
La logica di gioco riceve la sorgente casuale come parametro (iniettabile) per consentire test deterministici.

### 5.5 Opzioni avanzate
Tutte disattivate per default: la partita base resta identica a quella senza opzioni. Passi della creazione partita, nell'ordine:
1. **Partita trappola**: se attiva, con probabilità 10% (costante `PROBABILITA_TRAPPOLA` = 0,10) K = 0: nessuno è impostore.
2. Altrimenti K = numero impostori; con **Impostori a sorpresa** K è uniforme in 1..numero impostori (che diventa il massimo). Il massimo resta floor((N−1)/2).
3. Gli impostori sono K indici distinti, scelti in modo uniforme.
4. Primo giocatore: con **L'impostore non parla per primo** e K > 0, uniforme tra i civili; altrimenti uniforme tra tutti (5.3).
5. Ordine di parola (5.6).
6. **Giri di indizi** (1..3) e **Promemoria ultima possibilità** vengono copiati dalla configurazione nella partita; influenzano solo la visualizzazione (4.4, 4.5).

Con K = 0 ogni giocatore riceve la parola dei civili, in entrambe le modalità. L'opzione "L'impostore vede la categoria" non è un'opzione avanzata contata (4.2.1) e non cambia regola.

### 5.6 Ordine di parola
Sequenza degli N indici dei giocatori nell'ordine in cui parlano: il primo è il giocatore che inizia. Senza **Ordine casuale**: da lui in poi seguendo la lista e ripartendo dall'inizio. Con **Ordine casuale**: il primo, poi una permutazione casuale degli altri. Ogni giocatore compare una volta. Con più giri l'ordine si ripete identico in ogni giro. È anche l'ordine di distribuzione dei ruoli (4.3, 5.1) e dell'elenco di Rivedi la parola (4.4.1): il giocatore alla posizione k (0-based) è `ordine[k]`.

## 6. Persistenza

Memorizzata in locale (es. DataStore/SharedPreferences) e ripristinata all'avvio: numero giocatori, nomi inseriti, numero impostori, modalità, opzione "mostra categoria", id delle categorie selezionate.

- Opzioni avanzate (4.2.1): i cinque interruttori e "Giri di indizi" fanno parte della configurazione salvata. Campi mancanti (salvataggi precedenti) = default (spento, 1 giro); "Giri di indizi" fuori da 1..3 è corretto ai limiti.
- Partita in corso: si salvano anche ordine di parola, giri di indizi e promemoria. Se l'ordine manca (salvataggi vecchi) si usa la rotazione da primo giocatore, con 1 giro e promemoria spento. Un ordine che non è una permutazione di 0..N−1 o non inizia con il primo giocatore rende la partita non valida. Impostori vuoti sono validi (partita trappola).
- Al ripristino: un id di categoria non più presente nel file parole viene ignorato; se nessuna categoria valida resta, si selezionano tutte.
- Valori fuori limite (es. impostori oltre il massimo) vengono corretti ai limiti.
- **Impostazioni del dispositivo** (aspetto/tema e durata della pressione): salvate subito a ogni cambio, nello stesso archivio dell'aspetto (Android: stesso DataStore, chiavi distinte; PWA: `localStorage`, chiave dell'aspetto) e ripristinate all'avvio; non fanno parte della configurazione né della sessione. Durata della pressione: assente = 150 ms; valore numerico fuori da 0..1000 → riportato al limite più vicino; non multiplo di 50 → riportato al multiplo di 50 più vicino (a metà strada, per eccesso: 125 → 150); valore illeggibile (non numerico, non finito, tipo errato) → 150. Funzione pura condivisa: contratto API "Durata della pressione".
**Partita in corso.** La sessione di gioco sopravvive alla chiusura dell'app. A ogni cambio di stato si salva:
- giocatori (nomi effettivi, nell'ordine), indici degli impostori, voce (id categoria, parola, affine), modalità, mostraCategoria, indice del giocatore che inizia, stato della distribuzione (Passaggio k / Rivelazione k / Gioco, con k = posizione nell'ordine di parola, 4.3);
- l'insieme delle parole usate (chiave: id categoria + parola normalizzata, vedi 7), aggiornato a ogni estrazione. Non si azzera alla chiusura dell'app: persiste finché il pool non si esaurisce (5.2.3).

Regole:
- Ripristino: uno stato Rivelazione k diventa Passaggio k; il ruolo non è mai visibile all'avvio.
- Lo stato di "Rivedi la parola" (4.4.1) non è salvato: la ripresa porta sempre alla schermata di Gioco.
- La Home mostra "Riprendi partita" solo se esiste una partita salvata valida (vedi 4.1).
- Cancellazione della partita salvata (non delle parole usate): all'ingresso in Rivela, confermando "Interrompere la partita?", avviando una nuova partita (sostituisce la salvata). Il solo "Nuova partita" in Home, finché non si preme "Inizia", non la cancella.
- Validità: la partita salvata è scartata in silenzio (nessun messaggio, nessun "Riprendi partita") se i dati sono illeggibili o incoerenti (es. indici fuori limite, k fuori intervallo), o se categoria o parola (o affine, in modalità "Parola affine") non esistono più in `parole.json`. Anche con `parole.json` illeggibile non si offre "Riprendi partita".
- Le parole usate che non esistono più nel file parole sono ignorate.
- I dati sono salvati in chiaro nella memoria privata dell'app (nessuna cifratura).
- Non vengono memorizzati altrove ruoli o parola corrente: sono ricavabili solo dalla partita salvata.

## 7. Formato dati delle parole

File `app/src/main/assets/parole.json`:

```json
{"versione":1,"categorie":[{"id":"animali","nome":"Animali","parole":[{"parola":"Cane","affine":"Lupo"}]}]}
```

- `versione`: intero, atteso 1. Altro valore → errore di caricamento.
- `categorie[].id`: stringa univoca e stabile (usata nella persistenza); `nome`: etichetta mostrata.
- `parole[].parola`: stringa non vuota; `affine`: stringa, può mancare o essere vuota (la parola è allora usabile solo in modalità "Impostore senza parola").
- La parola affine deve essere diversa dalla parola (confronto senza distinzione maiuscole/minuscole): coppie uguali sono scartate.
- Una categoria senza parole utilizzabili viene mostrata ma, se è l'unica selezionata, vale il messaggio del punto 5.2.6. In modalità "Parola affine" una categoria senza parole con affine è mostrata disabilitata.
- Errore di lettura o JSON non valido: la Home mostra "Impossibile caricare le parole" e "Nuova partita" è disabilitato.
- L'unicità di una parola si valuta su (id categoria, parola) normalizzata (trim, minuscole).

## 8. Criteri di accettazione

### 8.1 Logica di gioco pura (test JUnit, senza dipendenze Android)

- **CA-01** Il limite massimo di impostori per N giocatori è floor((N−1)/2): N=3→1, 4→1, 5→2, 6→2, 7→3, 20→9.
- **CA-02** La validazione della configurazione rifiuta N<3 e N>20, impostori <1 e impostori > floor((N−1)/2), con errore distinto per ciascun caso.
- **CA-03** La validazione rifiuta l'insieme di categorie vuoto.
- **CA-04** La validazione rifiuta nomi duplicati (senza distinzione maiuscole/minuscole, dopo trim), compresi i duplicati con un nome di default.
- **CA-05** Un nome vuoto o di soli spazi diventa "Giocatore n" (n = posizione da 1); un nome con spazi ai bordi viene troncato dagli spazi; un nome oltre 20 caratteri è rifiutato.
- **CA-06** Riducendo N, gli impostori vengono ridotti al nuovo massimo; aumentando N restano invariati.
- **CA-07** L'assegnazione produce esattamente K impostori, tutti distinti, con indici in [0, N); gli altri N−K sono civili.
  Nota: eccezione della partita trappola (5.5): con "Partita trappola" attiva, con probabilità 10% K = 0 (nessun impostore); con "Impostori a sorpresa" K è uniforme in 1..numeroImpostori. Con le opzioni spente CA-07 vale invariato. Vedi CA-70…CA-74.
- **CA-08** Con sorgente casuale fissata (seed) l'assegnazione è riproducibile; con seed diversi si ottengono, su molte estrazioni, tutti i giocatori come impostori almeno una volta (nessun indice escluso a priori).
- **CA-09** In modalità "Impostore senza parola" il contenuto del ruolo civile è la parola; quello dell'impostore è "Sei l'impostore" più la categoria se l'opzione è attiva, senza categoria se disattiva; non contiene mai la parola né l'affine.
- **CA-10** In modalità "Parola affine" il contenuto civile è la parola e quello impostore è l'affine; i due oggetti di visualizzazione hanno la stessa struttura e le stesse etichette (differiscono solo nel testo della parola) e non contengono il flag di ruolo.
- **CA-11** In modalità "Parola affine" il pool esclude le parole senza affine, con affine vuoto o uguale alla parola.
- **CA-12** La parola estratta appartiene sempre a una delle categorie selezionate.
- **CA-13** Partite consecutive nella stessa sessione non ripetono la stessa parola finché il pool contiene parole non ancora usate: con un pool di M parole, le prime M partite usano M parole tutte diverse.
- **CA-14** Esaurito il pool, la partita successiva estrae di nuovo dal pool completo e non ripete l'ultima parola usata se M ≥ 2; con M = 1 ripete l'unica parola senza errori.
- **CA-15** Il giocatore che inizia è un indice in [0, N); su molte estrazioni con seed diversi compaiono tutti gli indici.
- **CA-16** Un pool iniziale vuoto (categorie senza parole utilizzabili nella modalità scelta) produce un errore esplicito, non un'eccezione non gestita.
- **CA-17** Il parser accetta il formato di sezione 7, rifiuta `versione` ≠ 1, JSON malformato e `parola` vuota; tollera `affine` assente o vuoto.
- **CA-18** Lo stato della distribuzione è una macchina a stati: dall'indice k in stato "Passaggio" si può passare solo a "Rivelazione" di k; da "Rivelazione" di k solo a "Passaggio" di k+1 (o a "Gioco" se k è l'ultimo); non esiste una transizione verso indici precedenti né una seconda rivelazione dello stesso k.
  Nota: eccezione esplicita data dalla funzione "Rivedi la parola" (4.4.1, CA-51…CA-60): CA-18 resta valido per la sola Distribuzione; la revisione avviene dopo la Distribuzione, fuori da questa macchina a stati, e non ne modifica lo stato.
- **CA-19** La serializzazione/deserializzazione della configurazione è reversibile (round-trip identico); id di categoria sconosciuti vengono scartati e, se non ne resta nessuno, si selezionano tutte; valori fuori limite vengono corretti ai limiti.
- **CA-20** "Nuova partita" con le stesse impostazioni produce nuovi ruoli, nuova parola e nuovo giocatore iniziale mantenendo giocatori, nomi e impostazioni.
- **CA-21** Il testo di svelamento usa la forma singolare con K=1 ("L'impostore era: …") e plurale con K>1 ("Gli impostori erano: …"), e include l'affine solo in modalità "Parola affine".
  Nota: eccezione della partita trappola (opzione avanzata, 5.5): con "Partita trappola" attiva e K = 0 non vale la forma singolare/plurale ma il testo di CA-77/CA-86.
- **CA-37** La serializzazione/deserializzazione dello stato partita (giocatori, indici impostori, categoria, parola, affine, modalità, mostraCategoria, primo giocatore, stato distribuzione) è reversibile (round-trip identico) per ciascuno stato: Passaggio k, Rivelazione k, Gioco.
- **CA-38** La serializzazione/deserializzazione dell'insieme delle parole usate è reversibile (round-trip identico), compreso l'insieme vuoto.
- **CA-39** Il ripristino normalizza Rivelazione k in Passaggio k (stesso k); Passaggio k e Gioco restano invariati.
- **CA-40** Una partita salvata con dati illeggibili o incoerenti (JSON malformato, campo mancante, indici impostori fuori [0, N) o duplicati, k fuori [0, N), stato sconosciuto) è giudicata non valida senza eccezioni non gestite.
- **CA-41** Una partita salvata la cui categoria o parola (o affine, in modalità "Parola affine") non esiste più nel file parole è giudicata non valida; con dati coerenti col file è valida.
- **CA-61** Le parole rimanenti di un pool sono quelle del pool non presenti tra le usate (X ≤ Y); l'azzeramento per pool rimuove dalle usate solo le parole delle categorie scelte, lasciando intatte le usate delle altre categorie; azzerare con nessuna parola usata non cambia nulla.
- **CA-42** Le parole usate persistono tra un ripristino e l'altro: dopo salvataggio e ripristino, l'estrazione successiva esclude le parole già usate; l'esaurimento del pool le azzera come da 5.2.3. Le parole usate non più presenti nel file sono ignorate.

Opzioni avanzate e ordine di parola (4.2.1, 5.5, 5.6). Ogni caso con `Random` iniettato; "molte partite" = almeno 10.000 estrazioni con seed diversi.

- **CA-70** Con tutte le opzioni spente (`impostoreNonPrimo`, `impostoriSorpresa`, `ordineCasuale`, `partitaTrappola`, `promemoriaUltimaPossibilita` = false, `giriIndizi` = 1) il comportamento è invariato: K = `numeroImpostori`, primo giocatore uniforme tra tutti, ordine = rotazione da primo, nessuna partita trappola, nessun promemoria; CA-07, CA-08, CA-15 e CA-21 valgono senza eccezioni. A parità di seed e di sequenza di chiamate al `Random`, l'esito coincide con quello della versione senza opzioni.
- **CA-71** Frequenza della trappola: con `partitaTrappola` attivo, su molte partite la quota di partite con zero impostori è compresa tra 5% e 15% (probabilità nominale 10%). Con `partitaTrappola` spento le partite senza impostori sono 0.
- **CA-72** Trappola deterministica: con `Random` il cui `nextDouble()` restituisce un valore < 0,10 la partita ha zero impostori; con un valore ≥ 0,10 ha K impostori secondo le altre regole. In una partita trappola `trappola` è vero e ogni giocatore riceve la parola dei civili in entrambe le modalità.
- **CA-73** Impostore mai primo: con `impostoreNonPrimo` attivo e K > 0, su molte partite (con ogni N da 3 a 20) il primo giocatore non è mai un impostore, ed è uniforme tra i civili (ogni civile compare almeno una volta). Con K = 0 (trappola) il primo è uniforme tra tutti i giocatori. Il primo di `ordine` coincide con `primoGiocatore`.
- **CA-74** K a sorpresa: con `impostoriSorpresa` attivo e `numeroImpostori` = M, K è sempre in 1..M (mai 0, mai > M, salvo trappola) e su molte partite compaiono tutti i valori 1..M con frequenza approssimativamente uniforme; con M = 1 K vale sempre 1. Il vincolo K ≤ floor((N−1)/2) resta rispettato.
- **CA-75** Ordine di parola: `ordineDiParola()` restituisce una permutazione di 0..N−1 (size N, ogni indice una sola volta) il cui primo elemento è `primoGiocatore`. Con `ordineCasuale` spento è la rotazione: da `primoGiocatore` in poi, seguendo la lista e ripartendo dall'inizio. Con `ordineCasuale` attivo gli altri N−1 sono in ordine casuale e su molte partite non è sempre la rotazione.
- **CA-76** `giriIndizi` è limitato a 1..3 (valori < 1 → 1, > 3 → 3) e, come `promemoriaUltimaPossibilita`, viene copiato dalla configurazione nella partita; non altera ruoli, parola, primo giocatore né ordine.
- **CA-77** Testi: con K = 0 lo svelamento è "Nessun impostore: era una partita trappola!", seguito dalla parola (e dall'affine in modalità "Parola affine") e dalla categoria; il promemoria "L'impostore scoperto può provare a indovinare la parola: se ci riesce, vince lui!" è prodotto solo se l'opzione è attiva e K > 0.
- **CA-78** Salvataggi vecchi leggibili: una configurazione salvata senza i nuovi campi si legge con i default (tutte le opzioni spente, `giriIndizi` = 1); `giriIndizi` fuori da 1..3 è corretto ai limiti; una sessione salvata senza `ordine`, `giriIndizi` o `promemoria` si legge con la rotazione da `primoGiocatore`, 1 giro, promemoria spento. Il round-trip di configurazione e di sessione (CA-19, CA-37) resta identico anche con le nuove opzioni.
- **CA-79** Una sessione salvata con `ordine` che non è una permutazione di 0..N−1, o che non inizia con `primoGiocatore`, è giudicata non valida (come CA-40). Una sessione con impostori vuoti è valida (partita trappola).
- **CA-91** Segnalazioni: la lettura delle righe salvate prima della proposta di coppia (campi di proposta assenti) riesce con proposta nulla; la proposta è trimmata e limitata a 40 caratteri, la stringa vuota diventa assenza; `propostaValida` è vera solo se parola e affine sono entrambe presenti e diverse (senza distinzione maiuscole/minuscole); le righe malformate sono saltate in lettura.

### 8.2 Interfaccia (test strumentati/manuali)

- **CA-22** Al primo avvio la Configurazione mostra: 4 giocatori, 1 impostore, modalità "Impostore senza parola", opzione categoria attiva, tutte le categorie selezionate, nomi "Giocatore 1…4".
- **CA-23** (modificato) I giocatori sono i campi del passo 1, da 3 a 20 (CA-113..CA-118; nessun contatore "−"/"+" dei giocatori); i pulsanti "−"/"+" degli impostori (passo 2) si fermano a 1 e al massimo consentito; il massimo si aggiorna al variare dei giocatori.
- **CA-24** L'interruttore "L'impostore vede la categoria" è visibile solo con modalità "Impostore senza parola".
- **CA-25** Con nessuna categoria selezionata compare "Seleziona almeno una categoria" e "Avanti" del passo 4 è disabilitato, con sopra in rosso lo stesso errore; con un nome duplicato compare "Nome già usato" e "Avanti" del passo 1 è disabilitato; per un errore senza testo dedicato sopra i pulsanti compare in rosso "Controlla la configurazione".
- **CA-26** Chiudendo e riaprendo l'app, la Configurazione mostra gli stessi valori impostati prima della chiusura.
- **CA-27** (modificato) La schermata "Passa il telefono a <nome>" non contiene né parola né testo di ruolo; con durata > 0 il ruolo compare solo dopo la pressione lunga della durata scelta nelle Impostazioni (predefinita 150 ms) su "Tieni premuto per scoprire" (didascalia "Sono <nome> — tieni premuto"); un tocco breve o un rilascio anticipato non rivelano nulla e la barra torna a zero. Con durata 0 vale CA-107.
- **CA-28** Dopo "Nascondi e passa" il ruolo non è più visibile e non esiste alcun controllo né gesto (incluso il tasto indietro) per rivedere il ruolo di un giocatore precedente.
- **CA-29** Mettere l'app in background o ruotare lo schermo mentre un ruolo è visibile riporta alla schermata "Passa il telefono a <nome>" dello stesso giocatore.
- **CA-30** In modalità "Parola affine" le schermate di rivelazione di un civile e di un impostore sono indistinguibili in layout, etichette e colori (differiscono solo per la parola).
- **CA-31** Dopo l'ultimo giocatore compare la schermata di Gioco con "Parla per primo: <nome>"; il ruolo di nessuno è mostrato; "Rivela" chiede conferma ("Rivelare i ruoli?", "Verrà mostrato chi era l'impostore.", "Rivela" / "Non ancora") prima di procedere.
- **CA-32** La schermata Rivela mostra impostore/i, parola, categoria e (solo in modalità affine) la parola affine, con i testi di sezione 4.5.
- **CA-33** "Rigioca (stessi giocatori)" nella schermata Rivela porta a "Passa il telefono a <primo giocatore>" con la stessa lista di giocatori; "Modifica giocatori e opzioni" porta alla Configurazione precompilata.
- **CA-34** Con `parole.json` illeggibile la Home mostra "Impossibile caricare le parole" e "Nuova partita" è disabilitato.
- **CA-35** Il tasto indietro in Distribuzione e Gioco chiede "Interrompere la partita?" ("La partita andrà persa.", "Interrompi" / "Continua a giocare") e non porta mai a un ruolo precedente.
- **CA-36** Tutti i testi dell'interfaccia sono in italiano e coincidono con quelli citati in queste specifiche.
- **CA-43** Senza partita salvata la Home non mostra "Riprendi partita" e "Nuova partita" apre direttamente la Configurazione. Con una partita salvata valida "Riprendi partita" compare sopra "Nuova partita".
- **CA-44** Con una partita salvata, "Nuova partita" mostra "Esiste una partita in corso. Iniziarne una nuova?"; "Annulla" resta in Home con la partita intatta; "Nuova partita" apre la Configurazione e, dopo "Inizia", la partita salvata è sostituita dalla nuova.
- **CA-45** Chiudendo l'app (processo terminato) in Passaggio k, "Riprendi partita" mostra "Passa il telefono a <nome k>" con gli stessi ruoli, parola e giocatore iniziale di prima.
- **CA-46** Chiudendo l'app mentre il ruolo del giocatore k è visibile, "Riprendi partita" mostra "Passa il telefono a <nome k>" e nessun ruolo; il ruolo compare solo dopo la pressione lunga su "Tieni premuto per scoprire".
- **CA-47** Chiudendo l'app nella schermata di Gioco, "Riprendi partita" riporta alla schermata di Gioco con lo stesso "Parla per primo: <nome>".
- **CA-48** La partita salvata è cancellata (la Home non mostra "Riprendi partita") dopo: ingresso in Rivela, conferma "Interrompere la partita?" con "Interrompi".
- **CA-49** Con la partita salvata incoerente col file parole (categoria o parola rimossa) o dati illeggibili, la Home non mostra "Riprendi partita" e non compare alcun messaggio di errore.
- **CA-50** Le parole usate sopravvivono alla chiusura dell'app: dopo riavvio, una nuova partita non estrae parole già usate finché il pool non è esaurito.
- **CA-51** La schermata di Gioco mostra il pulsante "Rivedi la parola", visivamente secondario rispetto a "Rivela"; non compare in Distribuzione né in Rivela.
- **CA-52** Il tocco su "Rivedi la parola" apre la schermata "Rivedi la parola" con tutti i giocatori, con i nomi effettivi e nell'ordine della partita; nessun ruolo è visibile.
- **CA-53** Dall'Elenco, il tasto indietro (di sistema o freccia) torna alla schermata di Gioco senza conferma e con lo stesso "Parla per primo: <nome>".
- **CA-54** Toccando un nome compare "Passa il telefono a <nome>" con il pulsante a pressione lunga "Tieni premuto per scoprire" (didascalia "Sono <nome> — tieni premuto"), senza fila di avatar, senza indicatore "Giocatore k di N", senza parola né testo di ruolo; un tocco breve non rivela; il tasto indietro torna all'Elenco.
- **CA-55** (modificato) Dopo la pressione lunga (durata delle Impostazioni, 4.7; con 0 ms, il rilascio) la rivelazione è identica per layout, etichette e colori alla rivelazione della Distribuzione (CA-30) e mostra il contenuto corretto per ruolo e modalità; "Nascondi" (o tasto indietro) torna alla schermata di Gioco con il ruolo non più visibile.
- **CA-56** Si può rivedere la parola più volte, anche dello stesso giocatore o di giocatori diversi, senza limiti; ruoli, parola e giocatore iniziale restano invariati.
- **CA-57** Mettere l'app in background o ruotare lo schermo in qualsiasi schermata di Rivedi la parola riporta alla schermata di Gioco, con nessun ruolo visibile.
- **CA-58** Le schermate di Rivedi la parola hanno FLAG_SECURE: lo screenshot è bloccato e l'anteprima in Recents non mostra il contenuto.
- **CA-59** Chiudendo l'app (processo terminato) durante Rivedi la parola, "Riprendi partita" porta alla schermata di Gioco con lo stesso "Parla per primo: <nome>"; la partita salvata non contiene alcuno stato di revisione.
- **CA-60** "Rivedi la parola" non modifica la macchina a stati della Distribuzione (CA-18 resta valido) né la partita salvata; CA-28 e CA-35 restano validi per la sola Distribuzione.
- **CA-62** Ogni campo nome ha un pulsante "x" che lo svuota (il nome torna "Giocatore n"); la tastiera dei campi nome mostra il tasto Avanti, che passa al campo successivo.
- **CA-63** Nella Configurazione compare "Parole ancora da giocare: X / Y" coerente con categorie e modalità scelte e si aggiorna al loro variare; "Azzera" chiede "Rimettere in gioco tutte le parole delle categorie scelte?" con "Annulla" / "Azzera"; "Annulla" non cambia X, "Azzera" porta X a Y per le categorie scelte lasciando intatte le usate delle altre (logica: CA-61).
- **CA-64** In Distribuzione l'ultimo giocatore vede "Nascondi e inizia" al posto di "Nascondi e passa"; il pulsante è disabilitato nei primi 600 ms dopo la comparsa del ruolo e poi si attiva; la schermata di Passaggio mostra "Gli altri non guardino lo schermo" e, nei primi 600 ms dal suo ingresso, il pulsante "Tieni premuto per scoprire" ignora la pressione.
- **CA-66** La schermata di Passaggio mostra la fila di avatar (uno per giocatore, nell'ordine della lista): i giocatori già passati con spunta, il corrente evidenziato, i successivi attenuati; con più di 8 giocatori la fila scorre restando centrata sul corrente. La fila non contiene ruoli né altre informazioni oltre all'iniziale del nome.
- **CA-67** Dopo la pressione lunga la rivelazione avviene con la carta che si gira; l'animazione è identica per tutti i giocatori e ruoli (CA-30, CA-65 restano validi).
- **CA-68** Dopo "Nascondi e inizia" compare l'overlay "Tutti pronti!" con "Che il bluff abbia inizio"; si chiude dopo 1200 ms o al tocco e porta alla schermata di Gioco (CA-31); chiudendo l'app durante l'overlay, "Riprendi partita" porta alla schermata di Gioco.
- **CA-65** Il feedback tattile della rivelazione è identico per civili e impostori, in entrambe le modalità (una sola vibrazione leggera); anche quello del passaggio è identico per tutti.

Opzioni avanzate, ordine di parola, segnalazioni (4.2, 4.4, 4.5, 4.6):

- **CA-80** (modificato) Nella Configurazione il passo 3 "Opzioni avanzate" mostra sempre visibili i gruppi "Ruoli", "Turni", "Fine partita" con i controlli di 4.2.1; non esiste alcuna card chiusa/aperta.
- **CA-81** (modificato) Il badge "N attive" sta nel titolo del passo 3 e nella riga "Opzioni avanzate" del passo 5; N = opzioni contate attive (4.2.1: "L'impostore non parla per primo", "Partita trappola", "Ordine casuale", "Promemoria ultima possibilità", "Giri di indizi" > 1); con N = 0 il badge non compare. "L'impostore vede la categoria" e "Impostori a sorpresa" non sono contati.
- **CA-82** (modificato) Nel passo 2 l'interruttore "Impostori a sorpresa" sta subito sotto il numero di impostori; con "Impostori a sorpresa" attivo l'etichetta del selettore è "Impostori (massimo)"; spento torna "Numero impostori". Il limite massimo del selettore (CA-23) non cambia. L'interruttore non compare nel passo 3.
- **CA-83** Il selettore "Giri di indizi" offre 1, 2, 3 (default 1); riaprendo l'app le opzioni avanzate hanno gli stessi valori impostati prima della chiusura (CA-26 esteso).
- **CA-84** Con "Giri di indizi" > 1 la schermata di Gioco mostra l'ordine di parola ripetuto con le intestazioni "Giro 1", "Giro 2", …, "Giro n"; con 1 giro mostra un solo elenco senza intestazione "Giro".
- **CA-85** La schermata di Gioco mostra l'ordine di parola nella sequenza in cui parlano i giocatori, il primo coincide con "Parla per primo: <nome>"; nessun ruolo è visibile.
- **CA-86** In una partita trappola la schermata Rivela mostra "Nessun impostore: era una partita trappola!", poi "La parola era: <parola>" (e "La parola affine era: <affine>" in modalità "Parola affine") e "Categoria: <nome categoria>"; non compare "L'impostore era" né "Gli impostori erano".
- **CA-87** Con "Promemoria ultima possibilità" attivo e almeno un impostore, la schermata Rivela mostra il riquadro con "L'impostore scoperto può provare a indovinare la parola: se ci riesce, vince lui!"; il riquadro non compare se l'opzione è spenta né in una partita trappola.
- **CA-88** In una partita trappola le schermate di rivelazione in Distribuzione e in "Rivedi la parola" mostrano a tutti il contenuto civile (4.3), identico per layout, etichette e colori; con la modalità "Impostore senza parola" nessun giocatore vede "Sei l'impostore".
- **CA-89** (modificato) Con tutte le opzioni avanzate spente: nessun badge, riga "Opzioni avanzate" del passo 5 = "Nessuna opzione attiva" (non più "1 impostore"), nessuna intestazione "Giro", nessun riquadro del promemoria.
- **CA-90** La schermata di segnalazione di una coppia offre i campi facoltativi di proposta (parola e affine, max 40 caratteri); "Salva" resta disattivato se non c'è alcun motivo, nota o coppia proposta valida; con un solo campo compilato o con le due parole uguali (senza distinzione maiuscole/minuscole) "Salva" è disattivato e compare un avviso.

Rinnovo grafico: riepilogo, spiegazioni delle modalità, pulsante "Inizia" fisso (4.2, 4.2.1):

- **CA-92** (modificato) Il numero di impostori è nella seconda riga della riga "Modalità" del passo 5: "1 impostore" con 1, "N impostori" con N > 1, "fino a N impostori" con "Impostori a sorpresa" attivo e N > 1 (con massimo 1: "1 impostore"); es. "Impostore senza parola" / "fino a 2 impostori"; la riga "Giocatori" dice solo "5 giocatori". Il testo "Regole classiche" non compare mai.
- **CA-93** (modificato) La riga "Opzioni avanzate" del passo 5 elenca, separate da ", " e nell'ordine di 4.2.1, solo le opzioni avanzate non predefinite, senza il numero di impostori né "Impostori a sorpresa". Esempio: "Ordine casuale" attivo e "Giri di indizi" = 2 danno "Ordine casuale, 2 giri"; con "L'impostore vede la categoria" spento (modalità "Impostore senza parola") compare "senza categoria"; in modalità "Parola affine" non compare mai.
- **CA-94** (modificato) Il riepilogo è nel passo 5 e riflette sempre lo stato corrente (anche dopo "Modifica" e ritorno). Il badge "N attive" resta definito da CA-81: cambiare solo il numero di impostori o "Impostori a sorpresa" non lo modifica.
- **CA-95** Nella scelta della modalità compare sotto "Impostore senza parola" la riga "I civili conoscono la parola, l'impostore deve bluffare." e sotto "Parola affine" la riga "L'impostore riceve una parola simile ma diversa."; entrambe sono sempre visibili, indipendentemente dalla modalità selezionata.
- **CA-96** (modificato) In ogni passo la barra azioni è fissa e visibile senza scorrere con 20 giocatori e con la tastiera aperta (in quest'ultimo caso sopra la tastiera); l'errore del passo è sopra i pulsanti, anch'esso visibile. L'ultimo elemento del contenuto può essere scorso fino a non essere coperto dalla barra.
- **CA-97** (modifica di CA-80, CA-89 e testi di 4.2.1) Nessun criterio né schermata usa più il riassunto "Regole classiche"; i criteri CA-80 e CA-89 sono aggiornati di conseguenza.

## 8.3 Aspetto

L'aspetto (colori, tipografia, forme, spaziature, animazioni, layout delle schermate) è definito in `docs/design.md`, fonte unica dei token e dei layout, comune ad Android e PWA. Il font è Roboto Flex, incorporato in entrambe le piattaforme (non il font di sistema). Le specifiche funzionali non duplicano i valori: in caso di conflitto sui testi vale questo documento, in caso di conflitto su colori e layout vale `docs/design.md`.

Configurazione a passi (4.2, 4.2.1):

- **CA-99** (modificato) "Nuova partita" apre il passo 1 di 5; l'indicatore mostra "Passo 1 di 5 · Giocatori" con pillole semplici (nessuna barra ondulata) che distinguono passi fatti, corrente e da fare; i nomi dei passi sono "Giocatori", "Modalità", "Opzioni", "Categorie", "Riepilogo". Il passo 1 contiene solo i campi nome e "+ Aggiungi giocatore"; il passo 2 contiene, in ordine, modalità, numero impostori, "Impostori a sorpresa".
- **CA-100** "Avanti" passa al passo successivo; "Indietro", la freccia e il tasto di sistema tornano al precedente senza controllare la validità; dal passo 1 portano alla Home e la configurazione è salvata.
- **CA-101** (modificato) Nel passo 1 un nome duplicato disabilita "Avanti" e mostra "Nome già usato" sopra il pulsante; i passi 2 e 3 non bloccano mai; nel passo 4 "Avanti" è disabilitato senza categorie ("Seleziona almeno una categoria") o senza parole utilizzabili ("Le categorie scelte non contengono parole utilizzabili").
- **CA-102** (modificato) "Inizia" nella barra superiore è presente nei passi 1, 2, 3 e 4. Con configurazione valida salva e apre la Distribuzione ruoli; altrimenti porta al primo passo non valido mostrando il suo errore (dal passo 1 con nome duplicato resta nel passo 1 e mostra "Nome già usato").
- **CA-103** (modificato) Il passo 5 mostra le righe "Giocatori" (passo 1), "Modalità" (passo 2), "Opzioni avanzate" (passo 3), "Categorie" (passo 4) con i valori di 4.2; "Modifica" porta al passo corrispondente e indietro riporta al riepilogo; "Inizia" avvia la partita.
- **CA-104** (modificato) Con "Impostori a sorpresa" attivo (passo 2) il numero di impostori nella riga "Modalità" del riepilogo è "fino a N impostori" (N > 1) e l'etichetta del contatore è "Impostori (massimo)"; con massimo 1 il riepilogo resta "1 impostore".
- **CA-105** (modificato) La configurazione e le sue modifiche (compreso "Impostori a sorpresa") sopravvivono al cambio di passo, alla rotazione e alla chiusura dell'app (stesso formato di prima); "Rigioca (stessi giocatori)" in Rivela non passa dai passi; "Modifica giocatori e opzioni" apre il passo 1.

Giocatori come campi (4.2, passo 1):

- **CA-113** "+ Aggiungi giocatore" aggiunge in fondo un campo vuoto con segnaposto "Giocatore n" (n = nuova posizione) e gli dà il fuoco; il numero di giocatori aumenta di 1 e il numero di impostori non cambia. Logica pura `Passi.aggiungiGiocatore`: vedi la tabella del contratto.
- **CA-114** Con 19 campi "+ Aggiungi giocatore" è abilitato; con 20 è disabilitato e legge "Massimo 20 giocatori"; la logica pura con 20 giocatori restituisce la configurazione invariata. Non esiste alcun contatore del numero di giocatori.
- **CA-115** La "x" di ogni campo rimuove il giocatore ed è presente con 4 o più campi, assente con 3; la logica pura con 3 giocatori (o indice fuori range) restituisce la configurazione invariata.
- **CA-116** Rimuovendo un giocatore i successivi scalano mantenendo il proprio nome; un nome vuoto segue la nuova posizione. Esempio: nomi ["Anna", "", "Carla", ""] (4 giocatori), rimuovendo l'indice 1 → ["Anna", "Carla", ""]: campi "Anna", "Carla", "Giocatore 3". Rimuovendo l'indice 0 di ["", "Bea", "Cia", "Dino"] → ["Bea", "Cia", "Dino"].
- **CA-117** Rimuovendo giocatori il numero di impostori si riduce al nuovo massimo: 7 giocatori e 3 impostori, rimozione → 6 giocatori e 2 impostori; 5 giocatori e 2 impostori → 4 e 1; 6 giocatori e 1 impostore → 1 impostore. Aggiungere non lo modifica.
- **CA-118** Il numero di giocatori è il numero di campi: dopo salvataggio e riapertura i campi sono gli stessi in numero e nomi (`numeroGiocatori` = numero di campi); un nome duplicato creato o risolto con aggiunta/rimozione abilita/disabilita "Avanti" del passo 1 subito (rimuovere uno di due duplicati elimina "Nome già usato").

Distribuzione nell'ordine di parola (4.3, 4.4.1, 5.1, 5.6):

- **CA-119** (logica pura) `Partita.giocatoreAlPasso(k)` = `ordineDiParola()[k]`. Con `ordine` = [0, 2, 1] (3 giocatori): passi 0, 1, 2 → giocatori 0, 2, 1; il primo è sempre `primoGiocatore`. Senza `ordine` e `primoGiocatore` = 2 con 4 giocatori: passi 0..3 → 2, 3, 0, 1. Fuori da 0..N−1 → eccezione di argomento non valido (Kotlin `IllegalArgumentException`, TS `RangeError`).
- **CA-120** In Distribuzione, se si parla 1, 3, 2 (indici 0, 2, 1) il telefono passa nell'ordine Giocatore 1, 3, 2: il Passaggio k mostra il nome, l'iniziale e il ruolo di `ordine[k]`; l'indicatore "Giocatore k di N" conta le posizioni; la fila di avatar segue lo stesso ordine.
- **CA-121** La macchina a stati di CA-18 resta sugli indici di posizione (0..N−1) e non cambia; "Nascondi e inizia" compare all'ultima posizione (l'ultimo a parlare). Uno stato salvato Passaggio k / Rivelazione k si riprende alla posizione k dell'ordine salvato (Rivelazione k → Passaggio k, come in 4.3); con una sessione salvata senza `ordine` la distribuzione segue la rotazione da `primoGiocatore`.
- **CA-122** L'elenco di Rivedi la parola (stato Elenco) mostra i giocatori nell'ordine di parola, come l'elenco di Gioco; toccando un nome si vede il ruolo di quel giocatore (non quello della stessa riga nella lista originale).

- **CA-98** Su Android e sul web tutto il testo dell'interfaccia usa Roboto Flex incorporato nell'app/PWA (nessun caricamento da rete); i testi restano in italiano e leggibili anche offline.

Durata della pressione (4.3, 4.4.1, 4.7, 6):

- **CA-106** (logica pura, JUnit) `DurataPressione.normalizza`: 0→0, 1000→1000, 150→150, 125→150, 124→100, 25→50, 24→0, -30→0, 1049→1000, 5000→1000, `Int.MIN_VALUE`→0, `Int.MAX_VALUE`→1000; il risultato è sempre multiplo di 50 in 0..1000. `daTesto`: null, "", "abc", "NaN", "Infinity" → 150; "200" → 200; " 200 " → 200; "149.6" → 150; "1e9" → 1000. Il predefinito è 150.
- **CA-107** Con durata 0 ms: nessuna barra; il ruolo si rivela al rilascio sul pulsante e non alla pressione; se il dito esce dal pulsante o il gesto è annullato, non si rivela; il pulsante legge "Tocca per scoprire" e la didascalia "Sono <nome> — tocca"; i primi 600 ms restano di blocco; l'azione di accessibilità "Scopri il ruolo" resta. Vale in Distribuzione e in Rivedi. A durata > 0 testi e barra sono quelli di CA-27.
- **CA-108** Con durata d > 0 la barra si riempie in d ms e il ruolo si rivela a barra piena, in Distribuzione e in Rivedi la parola; rilasciando prima di d ms la barra torna a zero e non si rivela nulla. Al primo avvio d = 150 ms.
- **CA-109** Nelle Impostazioni, gruppo "Pressione per scoprire" sotto "Tema": cursore "Durata della pressione" 0-1000 ms a passi di 50 (21 posizioni) con il valore mostrato nel formato "<n> ms" ("0 ms", "150 ms", "1000 ms") che si aggiorna mentre si sposta il cursore, e la descrizione di 4.7.
- **CA-110** La durata scelta si salva subito e sopravvive alla chiusura dell'app; un valore salvato fuori intervallo o non multiplo di 50 è riportato al valore valido più vicino (CA-106), uno illeggibile a 150; la lettura non influisce su tema e colori dinamici (che restano quelli salvati) e viceversa.
- **CA-111** Il cursore è accessibile: nome "Durata della pressione" e valore letto "<n> millisecondi" ("0 millisecondi, basta un tocco" a 0) dal lettore di schermo, incremento/decremento di un passo (50 ms) con le azioni del lettore (Android) o le frecce (web); area di tocco >= 48 dp.
- **CA-112** Cambiare la durata non cambia il blocco anti doppio tocco di 600 ms né l'indistinguibilità dei ruoli (CA-30, CA-65): barra, tempi e vibrazione dipendono solo dalla durata, mai dal ruolo.

## 9. Fuori perimetro (v1)

Voto nell'app, punteggi, classifiche, parole personalizzate (aggiunta/modifica da parte dell'utente), timer, multiplayer e qualsiasi funzione di rete, account, statistiche, suoni, traduzioni diverse dall'italiano.

## 10. Punti da confermare

- Limite impostori floor((N−1)/2) (civili sempre in maggioranza): confermare o proporre un limite diverso.
- "Nome già usato" blocca l'avvio: alternativa, accettare i duplicati.
- Decisione 3 (modificata): la partita interrotta da chiusura app viene ripresa (vedi 6); non è più un punto aperto.
- In Rivelazione k salvata, dopo la chiusura il giocatore k può rivedere il proprio ruolo (una volta) dopo la pressione lunga: confermare che è accettabile.
