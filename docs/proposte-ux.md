# Proposte di usabilità e interfaccia

Revisione di sola lettura. Contesto d'uso: un telefono che passa di mano in mano, la sera, di fretta.
File letti: `ui/schermate/*.kt`, `ui/theme/Theme.kt`, `res/values/strings.xml`, firme del ViewModel. Percorsi sotto `app/src/main/java/it/imposteur/` (le stringhe in `app/src/main/res/values/strings.xml`).
Ordine: rapporto beneficio/costo decrescente. Costo: S = poche righe, M = una schermata, L = più schermate o logica nuova.

## 1. Frase di passaggio che dice a chi dare il telefono (testi)
- Problema: in `SchermataDistribuzione.kt` (Passaggio) il testo "Passa il telefono a X" e il pulsante "Sono X" sono corretti. Alla fine dell'ultimo giocatore però "Nascondi e passa" porta a "Si gioca!" senza dire a chi dare il telefono o dove metterlo. Con "Nascondi e passa" l'ultimo giocatore non sa che il giro è finito.
- Proposta: sull'ultimo giocatore il pulsante diventa "Nascondi e inizia" (nuova stringa `distribuzione_nascondi_ultimo`). Nel Passaggio, sotto il nome, una riga piccola "Gli altri non guardino lo schermo".
- Costo: S. Cambia il comportamento: no.

## 2. Etichette dei dialoghi di conferma al posto di Sì/No (testi)
- Problema: `Componenti.kt` usa "Sì"/"No" per "Interrompere la partita?" e "Rivelare i ruoli?". Al buio e di fretta "No" e "Sì" si confondono, e "Sì" per interrompere è rischioso. I `TextButton` sono piccoli.
- Proposta: `DialogoConferma` riceve le etichette. Interruzione: "Interrompi" / "Continua a giocare". Rivela: "Rivela" / "Non ancora". Aggiungere nel corpo una riga che spiega la conseguenza ("La partita andrà persa").
- Costo: S. Cambia il comportamento: no.

## 3. Pulsante Rivela più difficile da premere per errore e più chiaro (gioco)
- Problema: `SchermataGioco.kt` mette il pulsante "Rivela" a tutta larghezza subito sotto il testo, nel centro dello schermo, con lo stesso peso visivo del titolo. Chi passa il telefono può toccarlo per sbaglio. Il titolo "Si gioca!" occupa il posto più grande e non dà informazione.
- Proposta: titolo più piccolo, "Inizia X" come elemento principale (grande, è l'informazione utile), pulsante "Rivela" ancorato in basso (zona del pollice) come `FilledTonalButton` o `OutlinedButton`, in modo che non sia il pulsante più vistoso. Il dialogo di conferma resta.
- Costo: S. Cambia il comportamento: no.

## 4. Parole lunghe che escono dallo schermo o si spezzano (leggibilità)
- Problema: `SchermataDistribuzione.kt` mostra la parola con `displayLarge` (circa 57 sp) senza limiti. Parole come "Elettrodomestico" o con due parole vanno a capo a metà parola; con carattere di sistema grande escono dal contenitore. Anche i nomi giocatori in `displaySmall` (Passaggio) e in `SchermataRivela.kt` (`headlineMedium`) possono spezzarsi.
- Proposta: componente `TestoAdattivo` che riduce la dimensione in base alla larghezza (`BoxWithConstraints` o `onTextLayout` con riduzione del 10% fino a un minimo), `maxLines = 2`, `softWrap` solo sugli spazi. Aggiungere `hyphens`/`lineBreak` semplice per i nomi.
- Costo: M. Cambia il comportamento: no.

## 5. Configurazione giocatori: tastiera e tabulazione (configurazione veloce)
- Problema: `SchermataConfigurazione.kt` ha fino a 20 `OutlinedTextField` senza `KeyboardOptions`. Il tasto sulla tastiera è "a capo/fatto" e non passa al campo successivo: per inserire 6 nomi servono tocchi ripetuti. Non c'è `capitalization`, quindi i nomi partono minuscoli. Il campo vuoto non mostra il nome che verrà usato (il segnaposto "Giocatore N" è solo come etichetta).
- Proposta: `KeyboardOptions(capitalization = Words, imeAction = Next)` (ultimo campo: `Done` che chiude la tastiera) con `KeyboardActions` e `FocusRequester`. Mostrare "Giocatore N" come `placeholder` dentro il campo. Aggiungere un pulsante "x" (clear) nel campo.
- Costo: S/M. Cambia il comportamento: no.

## 6. Motivo per cui "Inizia" è disattivato (configurazione)
- Problema: `SchermataConfigurazione.kt` disabilita il pulsante "Inizia" in `bottomBar` (`enabled = stato.puoIniziare`) senza dire perché. Gli errori (nome duplicato, nessuna categoria, pool vuoto) stanno molto più in basso, nella lista, fuori vista: l'utente vede un pulsante grigio e non sa che fare.
- Proposta: sopra il pulsante, una riga con il primo errore ("Seleziona almeno una categoria") in rosso; al tocco su un pulsante disattivato scorrere fino all'errore, oppure tenere il pulsante attivo e al tocco scorrere al problema con `animateScrollTo`.
- Costo: S/M. Cambia il comportamento: no.

## 7. Feedback tattile nella rivelazione (rivelazione)
- Problema: nessun uso di vibrazione/haptic in tutto il codice (nessuna occorrenza di `haptic`/`Vibrat`). In `Passaggio` → `Rivelazione` il ruolo compare senza alcun segnale: chi sta guardando altrove non sa quando il compagno ha finito, e chi riceve il telefono non ha conferma di aver premuto.
- Proposta: `LocalHapticFeedback` con `LongPress` alla comparsa del ruolo e `TextHandleMove` (o `Confirm`) a "Nascondi e passa". Un segnale diverso e più marcato per l'impostore, utile per chi tiene il telefono vicino al petto. Nessuna dipendenza nuova, nessun permesso.
- Costo: S. Cambia il comportamento: no.

## 8. Schermate di distribuzione scure e a basso contrasto di luce (buio)
- Problema: `Theme.kt` segue il tema di sistema e usa i colori dinamici (`dynamicColor = true`) su Android 12+. Di sera in tema chiaro il ruolo compare su uno schermo molto luminoso, visibile a chi sta vicino; con i colori dinamici il contrasto di `primary` sulla superficie non è garantito (parola in `primary` in `SchermataDistribuzione.kt`, titolo in `primary` altrove).
- Proposta: nelle schermate di distribuzione e gioco forzare uno schema scuro (superficie quasi nera, testo bianco, parola in colore ad alto contrasto) oppure ridurre la luminosità della finestra (`screenBrightness`) tramite il flag già presente per `FLAG_SECURE`. Disattivare i colori dinamici o verificare il contrasto (almeno 7:1 per la parola).
- Costo: M. Cambia il comportamento: no.

## 9. Impostore riconoscibile senza leggere (rivelazione)
- Problema: in `SchermataDistribuzione.kt` (Rivelazione) la differenza tra civile e impostore è solo il testo e il colore `error` (rosso). Un giocatore daltonico, o che guarda di sfuggita, può confondersi; ai civili la parola è in `primary`, che con i colori dinamici può essere rossastra anch'essa. In modalità "Parola affine" il testo è identico per tutti (voluto), quindi il colore non deve differenziare.
- Proposta: nella modalità "senza parola" dare all'impostore anche un'icona e uno sfondo diverso (fascia a tutta larghezza), sempre e solo in quella modalità; ai civili sfondo neutro. In "Parola affine" tutti hanno lo stesso aspetto.
- Costo: M. Cambia il comportamento: no.

## 10. Stepper dei giocatori più veloce (configurazione)
- Problema: `Stepper` in `SchermataConfigurazione.kt` ha pulsanti di 48 dp alti e stretti, al bordo destro dello schermo, in alto: poco raggiungibili con il pollice. Per passare da 3 a 8 giocatori servono 5 tocchi e ad ogni tocco la lista dei nomi sotto si allunga spostando tutto.
- Proposta: aumentare i pulsanti (56 dp, almeno 72 dp di larghezza), ripetizione automatica con pressione prolungata, valore centrale più grande (`displaySmall`). Dopo ogni aumento la lista dei nomi non deve spostare lo stepper (tenerlo in alto fisso o in una `stickyHeader`).
- Costo: M. Cambia il comportamento: no.

## 11. Home: un solo pulsante principale (coerenza)
- Problema: `SchermataHome.kt` mostra "Riprendi partita" e "Nuova partita" entrambi come `Button` pieno alti 56 dp: due azioni primarie uguali. Il titolo è solo testo, senza identità. Nella schermata `SchermataRivela.kt` invece il secondo pulsante è `OutlinedButton`: la gerarchia non è uniforme tra le schermate.
- Proposta: se c'è una partita in corso, "Riprendi" è `Button`, "Nuova partita" `FilledTonalButton`, "Come si gioca" `TextButton`. Altrimenti "Nuova partita" è l'unico `Button`. Stessa regola in tutte le schermate: azione principale piena, secondaria outlined, terziaria testo. Altezza uniforme 64 dp come nel gioco (ora 56 in home/rivela e 64 altrove).
- Costo: S. Cambia il comportamento: no.

## 12. Primo utilizzo: regole brevi in testa (primo utilizzo)
- Problema: `SchermataRegole.kt` mostra tre paragrafi lunghi senza gerarchia, la regola "Dopo la discussione si vota" è in fondo. Chi apre l'app la prima volta non ha un riassunto di quattro righe e la schermata Configurazione non spiega cosa fa il numero di impostori né le due modalità (solo i nomi).
- Proposta: in cima alle regole un riquadro "In breve" con 3 passi numerati (1 Ognuno vede il proprio ruolo in segreto. 2 Si discute a voce. 3 Si vota e si rivela). In Configurazione, sotto ogni modalità, una riga `bodySmall` con una descrizione di una frase. Alla prima apertura (flag in preferenze) evidenziare "Come si gioca".
- Costo: M. Cambia il comportamento: no.

## 13. Testi poco chiari e incoerenti (testi)
- Problema: in `strings.xml`: "Svelamento" (titolo) e "Rivela" (pulsante) e "Rivelare i ruoli?" sono tre parole per lo stesso concetto, e "Rivela" è usato anche per la rivelazione del ruolo individuale ("Nascondi e passa"). "Nuova partita" nella schermata finale non dice che riparte con gli stessi giocatori; "Cambia impostazioni" è vago. "Inizia %1$s" in `gioco_inizia` può sembrare "avvia la partita". "Mostra categoria all'impostore" non dice che vale solo nella modalità senza parola (appare solo se attiva, bene) ma resta ambiguo.
- Proposta: titolo finale "Il risultato" o "Chi era l'impostore"; pulsanti "Rigioca (stessi giocatori)" e "Modifica giocatori e opzioni"; "Parla per primo: %1$s" o "Comincia a parlare: %1$s"; "L'impostore vede la categoria".
- Costo: S. Cambia il comportamento: no (solo testi; verificare se "Nuova partita" riusa davvero la configurazione).

## 14. Pulsanti in basso nelle schermate di gioco e rivelazione (pollice)
- Problema: `SchermataGioco.kt`, `SchermataRivela.kt`, `SchermataDistribuzione.kt` usano `Arrangement.Center`: i pulsanti stanno a metà schermo, non nella zona del pollice, e salgono o scendono con la lunghezza del testo (parola a una o due righe), quindi la posizione cambia da un giocatore al successivo. Chi passa il telefono deve cercare il pulsante.
- Proposta: struttura fissa: contenuto al centro con `weight(1f)`, pulsante ancorato in basso con `navigationBarsPadding` (come già fa `bottomBar` della configurazione). Stessa posizione in Passaggio e Rivelazione, così "Sono X" e "Nascondi e passa" cadono sotto lo stesso punto e il doppio tocco accidentale va evitato con un breve ritardo (300 ms) prima di abilitare "Nascondi e passa".
- Costo: M. Cambia il comportamento: no (il ritardo anti doppio tocco è un cambiamento minimo).

## 15. Schermata finale: gerarchia delle informazioni (rivelazione)
- Problema: `SchermataRivela.kt` mostra impostore (`headlineMedium`), parola (`titleLarge`), affine (`titleLarge`), categoria (`titleMedium`) con poca differenza e tutto al centro. La parola, che è la risposta alla discussione, è più piccola dei nomi. Non c'è effetto di scoperta: tutto compare insieme e il gruppo non ha il tempo di commentare.
- Proposta: parola in `displaySmall` con colore `primary`, impostori in carta/riquadro colorata, categoria in piccolo. Facoltativo: due passi, prima i nomi degli impostori e poi, con un tocco su "Mostra la parola", la parola.
- Costo: S (gerarchia) / M (due passi). Cambia il comportamento: no per la gerarchia; il passo a due tocchi cambia leggermente il flusso.

## Idee future (fuori perimetro v1)
- Parole personalizzate dal gruppo e liste salvate di nomi ricorrenti.
- Timer di discussione con avviso tattile.
- Voto guidato sul telefono e punteggi tra le partite.
- Più lingue e profili di accessibilità (caratteri molto grandi, alto contrasto).
