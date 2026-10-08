# Impostore - Specifiche funzionali (versione 1)

## 1. Perimetro

App Android per giocare a "Impostore" con un solo telefono che passa di mano in mano. Nessuna rete, nessun multiplayer, nessun account.

Incluso in v1:
- configurazione della partita (giocatori, nomi, impostori, modalità, categorie);
- distribuzione segreta dei ruoli, un giocatore alla volta;
- schermata di gioco con giocatore che inizia;
- rivelazione finale e nuova partita;
- memorizzazione della configurazione tra un avvio e l'altro.

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

Home → Configurazione → Distribuzione ruoli → Gioco → Rivela → (Nuova partita → Distribuzione ruoli | Cambia impostazioni → Configurazione).

### 4.1 Home
- Contenuto: titolo "Impostore", pulsanti "Riprendi partita" (solo se esiste una partita salvata valida, vedi 6; posto sopra "Nuova partita"), "Nuova partita" e "Come si gioca".
- "Riprendi partita" ripristina la partita salvata: apre la Distribuzione ruoli allo stato Passaggio del giocatore k salvato, oppure la schermata di Gioco se la partita era in stato Gioco (stesso giocatore iniziale).
- "Nuova partita" apre la Configurazione. Se esiste una partita salvata valida compare prima la conferma "Esiste una partita in corso. Iniziarne una nuova?" con pulsanti "Annulla" (resta in Home, partita salvata intatta) e "Nuova partita" (apre la Configurazione; la partita salvata viene cancellata quando si avvia la nuova partita con "Inizia", vedi 6). "Come si gioca" apre una schermata di testo statico con le regole delle due modalità.
- Tasto indietro: esce dall'app.

### 4.2 Configurazione
Tutti i campi sono precompilati con l'ultima configurazione salvata (o con i default al primo avvio).

| Campo | Controllo | Regole | Default |
|---|---|---|---|
| Numero giocatori | stepper "−" / "+" con valore visibile | intero 3–20 | 4 |
| Nomi | un campo testo per giocatore, etichetta "Giocatore n" | facoltativo; spazi iniziali/finali rimossi; max 20 caratteri; nome vuoto → "Giocatore n"; nomi duplicati (senza distinzione maiuscole/minuscole) non ammessi | vuoto (mostra "Giocatore 1…N") |
| Numero impostori | stepper | minimo 1; massimo = floor((giocatori − 1) / 2), cioè i civili sono sempre più degli impostori (3-4 giocatori → 1, 5-6 → 2, 7-8 → 3, …, 20 → 9) | 1 |
| Modalità | scelta singola: "Impostore senza parola" / "Parola affine" | obbligatoria | "Impostore senza parola" |
| Mostra categoria all'impostore | interruttore | visibile e attivo solo se la modalità è "Impostore senza parola"; nascosto (non applicato) in "Parola affine" | attivo |
| Categorie | lista con caselle di selezione, più "Seleziona tutte" / "Deseleziona tutte" | almeno una selezionata | tutte selezionate |

Regole di validazione:
- Se il numero giocatori scende, il numero impostori e i nomi in eccesso si adattano: impostori = min(impostori, nuovo massimo). I nomi dei giocatori oltre N restano memorizzati ma non usati.
- Se un nome duplicato è presente, il campo mostra l'errore "Nome già usato" e "Inizia" è disabilitato. Il nome di default "Giocatore n" è considerato per il confronto.
- Se nessuna categoria è selezionata: messaggio "Seleziona almeno una categoria" e "Inizia" disabilitato.
- In modalità "Parola affine" una parola senza affine non può essere estratta (vedi 7).
- "Inizia" salva la configurazione e apre la Distribuzione ruoli. Tasto indietro: torna alla Home (la configurazione corrente viene salvata).

### 4.3 Distribuzione ruoli
Per ogni giocatore, nell'ordine della lista, si alternano tre stati:

1. **Passaggio**: testo "Passa il telefono a <nome>" e pulsante "Sono <nome>". Nessun ruolo visibile. Indicatore "Giocatore k di N".
2. **Rivelazione**: dopo il tocco sul pulsante, il contenuto del ruolo (vedi tabella) e il pulsante "Nascondi e passa".
3. Il tocco su "Nascondi e passa" nasconde il ruolo e passa allo stato 1 del giocatore successivo. Dopo l'ultimo giocatore apre la schermata di Gioco.

Contenuto dello stato 2:

| Giocatore | Modalità "Impostore senza parola" | Modalità "Parola affine" |
|---|---|---|
| Civile | "La parola è:" + parola | "La tua parola è:" + parola |
| Impostore | "Sei l'impostore" + (se opzione attiva) "Categoria: <nome categoria>" | "La tua parola è:" + parola affine |

Regole:
- Ogni ruolo si può rivelare una sola volta: dopo "Nascondi e passa" non è possibile tornare indietro né rivedere il ruolo di un giocatore già passato. Il tasto indietro di sistema nelle schermate di Distribuzione è disabilitato oppure chiede conferma "Interrompere la partita?" (Sì → cancella la partita salvata e va alla Configurazione, No → resta); non porta mai a un ruolo precedente.
- Se l'app va in background o la schermata viene ricreata (rotazione, ripristino) durante lo stato 2, si torna allo stato 1 dello stesso giocatore (il ruolo non resta mai visibile senza interazione).
- Lo stato corrente (Passaggio k / Rivelazione k) viene salvato a ogni cambio (vedi 6). Dopo la chiusura dell'app e "Riprendi partita", uno stato salvato Rivelazione k riparte come Passaggio k: il ruolo non è mai visibile all'avvio. Il giocatore k può quindi rivedere il proprio ruolo una sola volta ancora, solo dopo il tocco su "Sono <nome>".
- Lo schermo resta acceso durante la Distribuzione.
- La schermata di Passaggio non mostra mai informazioni sul ruolo.
- In modalità "Parola affine" lo stato 2 di civili e impostori è identico per layout, etichette e colori.

### 4.4 Schermata di gioco
- Testo "Si gioca!" e "Inizia <nome>" dove <nome> è il giocatore che inizia, scelto a caso uniformemente tra tutti i giocatori (indipendentemente dal ruolo).
- Testo di istruzione: "Discutete a voce, poi votate. Quando avete deciso, premete Rivela."
- Pulsante "Rivela" → conferma "Rivelare i ruoli?" (Sì/No) → schermata Rivela.
- Il ruolo non è mostrato in questa schermata.
- Tasto indietro: chiede "Interrompere la partita?" (Sì → Configurazione).

### 4.5 Rivela
Contenuto:
- Intestazione "Svelamento".
- "L'impostore era: <nome>" (singolare) oppure "Gli impostori erano: <nome1>, <nome2>, …" (plurale), nell'ordine della lista giocatori.
- "La parola era: <parola>".
- Solo in modalità "Parola affine": "La parola affine era: <affine>".
- Categoria: "Categoria: <nome categoria>".
- Pulsanti: "Nuova partita" (stessi giocatori e impostazioni: nuova parola, nuovi impostori, nuovo giocatore iniziale → Distribuzione ruoli) e "Cambia impostazioni" (→ Configurazione, precompilata).
- Tasto indietro: come "Cambia impostazioni".
- Entrando in Rivela la partita salvata viene cancellata (la Home non mostra più "Riprendi partita" fino a una nuova partita).

## 5. Regole di gioco

### 5.1 Assegnazione ruoli
- Con N giocatori e K impostori (1 ≤ K ≤ floor((N−1)/2)), si scelgono K giocatori distinti a caso, con distribuzione uniforme; tutti gli altri sono civili.
- Gli impostori non vengono comunicati tra loro (ciascuno vede solo la propria schermata).
- L'ordine di distribuzione è quello della lista dei giocatori (non casuale).

### 5.2 Scelta della parola
1. Si costruisce il pool: tutte le coppie (parola, affine) delle categorie selezionate. In modalità "Parola affine" si escludono le parole con affine assente o vuoto.
2. Si escludono le parole già usate nella sessione (insieme "usate").
3. Se il pool risultante è vuoto, l'insieme "usate" viene azzerato (per il solo pool corrente) e si estrae di nuovo dal pool completo; la nuova parola non coincide con quella dell'ultima partita, se il pool ha almeno 2 parole.
4. Si estrae una parola a caso (uniforme) e la si aggiunge a "usate".
5. L'insieme "usate" è persistente (vedi 6): sopravvive alla chiusura dell'app e si azzera solo per esaurimento del pool (passo 3). Non si azzera cambiando impostazioni o categorie.
6. Se il pool iniziale (passo 1) è vuoto, "Inizia" è disabilitato con messaggio "Le categorie scelte non contengono parole utilizzabili".

### 5.3 Giocatore che inizia
Uniforme tra tutti gli N giocatori, estratto a ogni partita.

### 5.4 Casualità
La logica di gioco riceve la sorgente casuale come parametro (iniettabile) per consentire test deterministici.

## 6. Persistenza

Memorizzata in locale (es. DataStore/SharedPreferences) e ripristinata all'avvio: numero giocatori, nomi inseriti, numero impostori, modalità, opzione "mostra categoria", id delle categorie selezionate.

- Al ripristino: un id di categoria non più presente nel file parole viene ignorato; se nessuna categoria valida resta, si selezionano tutte.
- Valori fuori limite (es. impostori oltre il massimo) vengono corretti ai limiti.
**Partita in corso.** La sessione di gioco sopravvive alla chiusura dell'app. A ogni cambio di stato si salva:
- giocatori (nomi effettivi, nell'ordine), indici degli impostori, voce (id categoria, parola, affine), modalità, mostraCategoria, indice del giocatore che inizia, stato della distribuzione (Passaggio k / Rivelazione k / Gioco);
- l'insieme delle parole usate (chiave: id categoria + parola normalizzata, vedi 7), aggiornato a ogni estrazione. Non si azzera alla chiusura dell'app: persiste finché il pool non si esaurisce (5.2.3).

Regole:
- Ripristino: uno stato Rivelazione k diventa Passaggio k; il ruolo non è mai visibile all'avvio.
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
- **CA-19** La serializzazione/deserializzazione della configurazione è reversibile (round-trip identico); id di categoria sconosciuti vengono scartati e, se non ne resta nessuno, si selezionano tutte; valori fuori limite vengono corretti ai limiti.
- **CA-20** "Nuova partita" con le stesse impostazioni produce nuovi ruoli, nuova parola e nuovo giocatore iniziale mantenendo giocatori, nomi e impostazioni.
- **CA-21** Il testo di svelamento usa la forma singolare con K=1 ("L'impostore era: …") e plurale con K>1 ("Gli impostori erano: …"), e include l'affine solo in modalità "Parola affine".
- **CA-37** La serializzazione/deserializzazione dello stato partita (giocatori, indici impostori, categoria, parola, affine, modalità, mostraCategoria, primo giocatore, stato distribuzione) è reversibile (round-trip identico) per ciascuno stato: Passaggio k, Rivelazione k, Gioco.
- **CA-38** La serializzazione/deserializzazione dell'insieme delle parole usate è reversibile (round-trip identico), compreso l'insieme vuoto.
- **CA-39** Il ripristino normalizza Rivelazione k in Passaggio k (stesso k); Passaggio k e Gioco restano invariati.
- **CA-40** Una partita salvata con dati illeggibili o incoerenti (JSON malformato, campo mancante, indici impostori fuori [0, N) o duplicati, k fuori [0, N), stato sconosciuto) è giudicata non valida senza eccezioni non gestite.
- **CA-41** Una partita salvata la cui categoria o parola (o affine, in modalità "Parola affine") non esiste più nel file parole è giudicata non valida; con dati coerenti col file è valida.
- **CA-42** Le parole usate persistono tra un ripristino e l'altro: dopo salvataggio e ripristino, l'estrazione successiva esclude le parole già usate; l'esaurimento del pool le azzera come da 5.2.3. Le parole usate non più presenti nel file sono ignorate.

### 8.2 Interfaccia (test strumentati/manuali)

- **CA-22** Al primo avvio la Configurazione mostra: 4 giocatori, 1 impostore, modalità "Impostore senza parola", opzione categoria attiva, tutte le categorie selezionate, nomi "Giocatore 1…4".
- **CA-23** I pulsanti "−"/"+" del numero giocatori si fermano a 3 e 20; quelli degli impostori si fermano a 1 e al massimo consentito; il massimo si aggiorna al variare dei giocatori.
- **CA-24** L'interruttore "Mostra categoria all'impostore" è visibile solo con modalità "Impostore senza parola".
- **CA-25** Con nessuna categoria selezionata compare "Seleziona almeno una categoria" e "Inizia" è disabilitato; con un nome duplicato compare "Nome già usato" e "Inizia" è disabilitato.
- **CA-26** Chiudendo e riaprendo l'app, la Configurazione mostra gli stessi valori impostati prima della chiusura.
- **CA-27** La schermata "Passa il telefono a <nome>" non contiene né parola né testo di ruolo; il ruolo compare solo dopo il tocco su "Sono <nome>".
- **CA-28** Dopo "Nascondi e passa" il ruolo non è più visibile e non esiste alcun controllo né gesto (incluso il tasto indietro) per rivedere il ruolo di un giocatore precedente.
- **CA-29** Mettere l'app in background o ruotare lo schermo mentre un ruolo è visibile riporta alla schermata "Passa il telefono a <nome>" dello stesso giocatore.
- **CA-30** In modalità "Parola affine" le schermate di rivelazione di un civile e di un impostore sono indistinguibili in layout, etichette e colori (differiscono solo per la parola).
- **CA-31** Dopo l'ultimo giocatore compare la schermata di Gioco con "Inizia <nome>"; il ruolo di nessuno è mostrato; "Rivela" chiede conferma prima di procedere.
- **CA-32** La schermata Rivela mostra impostore/i, parola, categoria e (solo in modalità affine) la parola affine, con i testi di sezione 4.5.
- **CA-33** "Nuova partita" nella schermata Rivela porta a "Passa il telefono a <primo giocatore>" con la stessa lista di giocatori; "Cambia impostazioni" porta alla Configurazione precompilata.
- **CA-34** Con `parole.json` illeggibile la Home mostra "Impossibile caricare le parole" e "Nuova partita" è disabilitato.
- **CA-35** Il tasto indietro in Distribuzione e Gioco chiede "Interrompere la partita?" e non porta mai a un ruolo precedente.
- **CA-36** Tutti i testi dell'interfaccia sono in italiano e coincidono con quelli citati in queste specifiche.
- **CA-43** Senza partita salvata la Home non mostra "Riprendi partita" e "Nuova partita" apre direttamente la Configurazione. Con una partita salvata valida "Riprendi partita" compare sopra "Nuova partita".
- **CA-44** Con una partita salvata, "Nuova partita" mostra "Esiste una partita in corso. Iniziarne una nuova?"; "Annulla" resta in Home con la partita intatta; "Nuova partita" apre la Configurazione e, dopo "Inizia", la partita salvata è sostituita dalla nuova.
- **CA-45** Chiudendo l'app (processo terminato) in Passaggio k, "Riprendi partita" mostra "Passa il telefono a <nome k>" con gli stessi ruoli, parola e giocatore iniziale di prima.
- **CA-46** Chiudendo l'app mentre il ruolo del giocatore k è visibile, "Riprendi partita" mostra "Passa il telefono a <nome k>" e nessun ruolo; il ruolo compare solo dopo il tocco su "Sono <nome k>".
- **CA-47** Chiudendo l'app nella schermata di Gioco, "Riprendi partita" riporta alla schermata di Gioco con lo stesso "Inizia <nome>".
- **CA-48** La partita salvata è cancellata (la Home non mostra "Riprendi partita") dopo: ingresso in Rivela, conferma "Interrompere la partita?" con Sì.
- **CA-49** Con la partita salvata incoerente col file parole (categoria o parola rimossa) o dati illeggibili, la Home non mostra "Riprendi partita" e non compare alcun messaggio di errore.
- **CA-50** Le parole usate sopravvivono alla chiusura dell'app: dopo riavvio, una nuova partita non estrae parole già usate finché il pool non è esaurito.

## 9. Fuori perimetro (v1)

Voto nell'app, punteggi, classifiche, parole personalizzate (aggiunta/modifica da parte dell'utente), timer, multiplayer e qualsiasi funzione di rete, account, statistiche, suoni, traduzioni diverse dall'italiano.

## 10. Punti da confermare

- Limite impostori floor((N−1)/2) (civili sempre in maggioranza): confermare o proporre un limite diverso.
- "Nome già usato" blocca l'avvio: alternativa, accettare i duplicati.
- Decisione 3 (modificata): la partita interrotta da chiusura app viene ripresa (vedi 6); non è più un punto aperto.
- In Rivelazione k salvata, dopo la chiusura il giocatore k può rivedere il proprio ruolo (una volta) dopo "Sono <nome>": confermare che è accettabile.
