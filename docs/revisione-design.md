# Revisione design: Android (Compose) contro PWA (Svelte)

Riferimenti: `docs/design.md` (v2), `docs/specifiche.md` §4, CA-25, 80-82, 89, 92-105.
Percorsi abbreviati: `A:` = `app/src/main/java/it/imposteur/ui/`, `W:` = `web/src/ui/`.
Esclusi (accettati): barra con Home in Distribuzione e Rivedi; molle a mano su Android; niente avatar piccoli in Rivela; in Rivedi la Rivelazione senza fila di avatar. Nomi delle chiavi in `testi.ts` ignorati.

Conteggio: alta 6, media 19, bassa 14.

## Token e invarianti (esito)

- Colori (Chiaro, Scuro, Sistema scuro, Alto contrasto), raggi, spaziature, misure, durate: `A:theme/Colori.kt`, `Forme.kt`, `Spazio.kt`, `Movimento.kt` e `W:tema.css` coincidono con design.md e tra loro. Nessun difetto.
- Tipografia: i token `--testo-*` e gli stili Compose coincidono. Il difetto è nell'uso degli stili (vedi M1, M2, B2).
- ON_STOP / `visibilitychange`: Android `SchermataDistribuzione.kt:111-117` e `SchermataRivedi.kt:56-62`; web `browser.ts:68-95` -> `nascondiRuolo()`. Entrambi riportano Rivelazione(k) a Passaggio(k) e chiudono la revisione. OK.
- FLAG_SECURE: `SchermataDistribuzione.kt:165-174`, usato anche da Rivedi (`SchermataRivedi.kt:51`). OK (sul web non esiste).
- "Parola affine": entrambe le piattaforme usano lo stesso ramo `ParolaSegreta` per civili e impostore (`SchermataDistribuzione.kt:377`, `DistribuzioneRivelazione.svelte:~79`). OK.
- Vibrazione: una sola alla comparsa del fronte per tutti i ruoli. OK. Scostamento minore in B12.

---

## ALTA

### A1. Passaggio: ordine degli elementi diverso
- Atteso (design 4.5): "Passa il telefono a", poi il nome, poi l'avatar grande, poi l'avviso. Web già così.
- Ottenuto Android: avatar grande, poi "Passa il telefono a", poi il nome, poi l'avviso.
- Android `A:schermate/SchermataDistribuzione.kt:262-292` (avatar 262, testo 268-272, nome 273-279). Web `W:schermate/DistribuzionePassaggio.svelte:49-54`.
- Correzione: spostare `Avatar(...)` dopo `TestoAdattivo(nome)` nel Passaggio Android.

### A2. Web: i contatori (passo 1) non sono in carta
- Atteso (design 3, "Contatore"): riga in carta `surfaceContainerLow`, raggio 28, padding 20 a sinistra e 12 a destra, pulsanti tonali da 48.
- Ottenuto web: riga senza sfondo né raggio, spaziatura 4 tra pulsanti e valore. Android ha la carta.
- Android `A:schermate/PassiConfigurazione.kt:387-396`. Web `W:componenti/Contatore.svelte:48-53` (`.contatore` senza background/border/radius) e `:65` (`.tasto`).
- Correzione: dare a `.contatore` background `--colore-contenitore-superficie-basso`, `--raggio-xl`, bordo `--spessore-contorno`, padding `8px 12px 8px 20px`.

### A3. Web: elenco categorie (passo 3) senza carta né separatori
- Atteso (design 3, "Riga con casella"): carta `surfaceContainerLow` raggio 28, righe da 48, separate da 2 dp di sfondo, casella M3 con raggio 4.
- Ottenuto web: elenco nudo, casella nativa del browser da 22 px con `accent-color`. Android ha carta, divisori da 2 dp e `Checkbox` M3.
- Android `A:schermate/PassiConfigurazione.kt:211-243`. Web `W:schermate/PassoCategorie.svelte:65-70`.
- Correzione: avvolgere `.elenco` in una carta con bordo `--spessore-contorno`, separatore 2px `--colore-sfondo`, casella disegnata con raggio 4 (stessa cosa per `DialogoSegnalazione.svelte:169`).

### A4. Web: righe di riepilogo (passo 4) non conformi
- Atteso (design 3, "Riga di riepilogo"): carta raggio 28, altezza minima 72, padding 8 8 8 20; etichetta in `--testo-didascalia` colore `onSurfaceVariant`; valore `--testo-titolo`; seconda riga `--testo-corpo-piccolo` max 2 righe con puntini; badge a destra prima di "Modifica"; tutta la riga toccabile.
- Ottenuto web: padding 12/16, nessuna altezza minima; etichetta in `--testo-etichetta` colore `primary` con badge dentro l'etichetta; seconda riga senza limite di righe; solo il pulsante "Modifica" è cliccabile (`<li>` non interattivo); nessun bordo in Alto contrasto. Android (tutta la riga `Surface(onClick)`, `labelMedium`, `maxLines = 2`) è corretto, salvo la seconda riga in `bodySmall` 12/16 invece di `bodyMedium` (vedi B2).
- Android `A:schermate/PassiConfigurazione.kt:306-351`. Web `W:schermate/PassoRiepilogo.svelte:35-65` e stile `:74-91`.
- Correzione: rendere la riga un `<button>`/`<a>` unico verso il passo, applicare i token sopra e `-webkit-line-clamp: 2`.

### A5. Web: campo nome (passo 1) è "outlined" e ha etichetta e segnaposto doppi
- Atteso (design 3, "Campo nome"): campo riempito `surfaceContainerHigh`, raggio `--raggio-s` solo in alto, indicatore inferiore 2 dp (`primary` in focus, `error` in errore).
- Ottenuto web: riquadro con bordo 1 px e raggio 12 su sfondo `--colore-sfondo`; l'etichetta piccola "Giocatore n" resta visibile e il segnaposto ripete lo stesso testo (due volte "Giocatore n" nello stesso campo). Android: `TextField` riempito `surfaceContainerHigh`, segnaposto solo in focus. Distanza tra i campi: Android 8, web 12.
- Android `A:schermate/PassiConfigurazione.kt:138-170` (spazio 133). Web `W:schermate/ConfigurazioneNome.svelte:32` (placeholder), `:49-61` (`.campo`, `label`), `:62` (`input`); distanza `W:schermate/PassoGiocatori.svelte:85`.
- Correzione: riusare lo stile di `CampoTesto.svelte` (riempito, bordo inferiore 2px), togliere `placeholder` o nasconderlo finché l'etichetta è ferma, distanza `--spazio-2`.

### A6. Barra azioni della configurazione: "Indietro", distanze, allineamento errore
- Atteso (design 3, "Barra azioni"): "Indietro" contorno con peso 1, "Avanti"/"Inizia" pieno con peso 2, distanza 8; errore centrato.
- Ottenuto web: "Indietro" tonale, i due pulsanti hanno larghezza uguale (`flex: 1`), distanza 12, errore allineato a sinistra. Android è conforme (`PulsanteContorno`, pesi 1 e 2, 8, centrato).
- Android `A:schermate/SchermataConfigurazione.kt:83-106` (pesi 97 e 104). Web `W:schermate/Configurazione.svelte:97` (variante `tonale`), `:112-115` (`.piede`, `.pulsanti`, `.errore`).
- Correzione: variante `contorno`, `flex: 1` e `flex: 2`, gap `--spazio-2`, `text-align: center` all'errore.

---

## MEDIA

### M1. Titolo e riga del passo: stili diversi
- Atteso (design 3, "Titolo del passo"): titolo `--testo-titolo-sezione` (22/28, 700); riga `--testo-corpo-piccolo` (14/20); badge a destra del titolo.
- Android: titolo `headlineSmall` (24/32, peso 400 del tema, non è nel design); riga `bodyMedium` corretta; badge subito dopo il titolo. Web: titolo corretto; riga `--testo-corpo` (16/24); badge spinto al bordo destro (`justify-content: space-between`).
- Android `A:schermate/SchermataConfigurazione.kt:165-166`. Web `W:schermate/PassoIntestazione.svelte:23` (space-between) e `:25` (`p`).
- Correzione: Android `titleLarge`; web riga con `--testo-corpo-piccolo`; decidere una sola posizione del badge (suggerito: a destra del titolo come in design, quindi Android con `Modifier.weight(1f)` sul titolo).

### M2. Titoli di sezione "Modalità" e "Nomi"
- Atteso (design 2.2, "Nomi", "Modalità", "Categorie"): `--testo-titolo-sezione`.
- Android `titleMedium` (16/24) primary; web `--testo-etichetta` (14/20) primary. Né uno né l'altro coincide col design, né tra loro.
- Android `A:schermate/PassiConfigurazione.kt:368`. Web `W:schermate/PassoGiocatori.svelte:84`.
- Correzione: `titleLarge` su Android e `--testo-titolo-sezione` sul web (colore da decidere, oggi primary in entrambi).

### M3. Alto contrasto: il web perde il contorno su carte e righe
- Atteso (design 2.5): ogni livello ha contorno 2 px (`--spessore-contorno`).
- Ottenuto web: `.scheda` del passo 2 e `.riga` del passo 4 non hanno `border` (e `.contatore`, elenco categorie dopo A2/A3); in Alto contrasto sono nero su nero senza confine. Android applica `bordoLivello()`. Android: `PulsantePressioneLunga` senza `bordoLivello()` (il web lo ha).
- Web `W:schermate/PassoOpzioni.svelte:86-92`, `W:schermate/PassoRiepilogo.svelte:74-80`. Android `A:componenti/PulsantePressioneLunga.kt:62-66` (manca il bordo).
- Correzione: aggiungere `border: var(--spessore-contorno) solid var(--colore-bordo-livello)` sul web; `.border(bordoLivello())` sul pulsante Android.

### M4. Freccia indietro presente solo sul web in Gioco, Distribuzione e Rivedi (Passaggio/Rivelazione)
- Atteso (design 4.6): barra "Si gioca!" senza freccia, indietro chiede conferma. Android senza freccia (solo Home), web con freccia che apre il dialogo.
- Android `A:schermate/SchermataGioco.kt:59-62`, `SchermataDistribuzione.kt:139`, `SchermataRivedi.kt:75`. Web `W:schermate/Gioco.svelte:37` (`onIndietro`), `DistribuzionePassaggio.svelte:40`, `DistribuzioneRivelazione.svelte` (`<Pagina {titolo} {onHome} {onIndietro}>`), `Rivedi.svelte:39-60`.
- Correzione: non passare `onIndietro` a `Pagina` in queste schermate (il tasto di sistema e il gesto restano gestiti dal router/`richiestaInterruzione`).

### M5. Home: composizione verticale
- Atteso (design 4.1): in alto medaglione e titolo, in basso i tre pulsanti; medaglione-titolo distanti.
- Android: tutto in un'unica colonna centrata verticalmente, medaglione-titolo a 12 dp, pulsanti subito sotto il titolo. Web: medaglione e titolo centrati nello spazio libero, pulsanti ancorati in basso, medaglione-titolo a 24.
- Android `A:schermate/SchermataHome.kt:59` (`Arrangement.spacedBy(s3, CenterVertically)`), `:68`. Web `W:schermate/Home.svelte:85-93` (`.hero`, gap `:91`) e `:109-114`.
- Correzione: Android con `Spacer(Modifier.weight(1f))` tra il blocco hero e i pulsanti e distanza `s5` tra medaglione e titolo (o, se si preferisce il centrato, adeguare il web: va deciso e scritto in design.md).

### M6. Gioco: struttura della schermata
- Atteso (design 4.6): carta hero, "Ordine di parola" con elenco numerato in carta `surfaceContainerLow`, testo di istruzione.
- Android: manca il testo di istruzione (la stringa `gioco_istruzioni` esiste in `strings.xml:138` ma non è usata); il titolo "Ordine di parola" sta fuori dalla carta; l'elenco scorre dentro una carta alta quanto lo schermo; i nomi sono centrati (`TestoAdattivo` forza `TextAlign.Center`) e hanno peso 600; distanza tra righe 4. Web: istruzione presente tra hero e carta, titolo dentro la carta, scorre la pagina, nomi a sinistra con peso 400 (700 per il primo), distanza tra righe 8.
- Android `A:schermate/SchermataGioco.kt:100-106` (titolo), `:115-171` (lista), `:159-167` (nome), `Componenti.kt:~35` (`textAlign = TextAlign.Center`). Web `W:schermate/Gioco.svelte:43-60`, `:131-162`.
- Correzione: aggiungere `Text(gioco_istruzioni)` su Android, spostare il titolo nella carta, fare allineare a sinistra il nome in lista (parametro `textAlign` in `TestoAdattivo`), uniformare peso e distanza.

### M7. Rivedi, elenco giocatori
- Android: una carta (raggio 28, 64 dp) per giocatore, distanza 8, nome centrato (titleMedium). Web: una sola carta con righe da 64, nome a sinistra a 1,125 rem. Intestazione: Android titleLarge, padding 16/8; web titolo-sezione, padding 8/4/12.
- Android `A:schermate/SchermataRivedi.kt:100-145`. Web `W:schermate/Rivedi.svelte:103-137`, `:92-102`.
- Correzione: scegliere un'unica soluzione (design 4.7: "righe di 64 dp in carta"); Android con nome allineato a sinistra.

### M8. Avatar "già visto"
- Atteso (design 3, Avatar): pieno con spunta bianca di 16 dp al posto dell'iniziale.
- Android: spunta bianca 16 dp centrata, iniziale sostituita. Web: iniziale mantenuta con un badge circolare `primary` con spunta nell'angolo in basso a destra.
- Android `A:componenti/Avatar.kt:84-93`. Web `W:componenti/Avatar.svelte:27-32` e `:65-71`.
- Correzione: sul web sostituire l'iniziale con la spunta bianca centrata.

### M9. Fila di avatar: soglia di scorrimento e taglio sul web
- Android passa allo scorrimento oltre 6 giocatori; web oltre 8. Con 8 giocatori su uno schermo da 360-390 px la fila misura 404 px (8x40 + 7x12) contro circa 358 disponibili: sul web, `justify-content: center` senza scorrimento taglia gli avatar ai due lati.
- Android `A:componenti/Avatar.kt:107`. Web `W:schermate/DistribuzioneFilaAvatar.svelte:8` (`> 8`) e `:38-46`.
- Correzione: web `giocatori.length > 6` (stessa soglia, e meglio con `margin-inline: auto` al posto di `justify-content: center` per evitare il taglio).

### M10. Rivelazione: colori dei testi e dimensioni dell'icona
- Android: parola e "Sei l'impostore" in `primary`, etichetta "La parola è:" in `onSurfaceVariant`, icona impostore 120 dp in `primary`. Web: tutto `onSurface` (nessun colore impostato), icona 96 px in `currentColor`.
- Android `A:schermate/SchermataDistribuzione.kt:383-408` (colori 386, 392, 406; icona 398-401). Web `W:schermate/DistribuzioneRivelazione.svelte:83` (icona 96), `:124-137` (`.intro`, `.dist-parola`, `.dist-impostore`).
- Correzione: scegliere la variante Android (parola e titolo ruolo in `--colore-primario`, etichetta in `--colore-su-superficie-variante`, icona 120) e allineare il web.

### M11. Retro della carta che si gira
- Atteso (design 3): "lo schema del Passaggio con il punto interrogativo".
- Android: icona della maschera (launcher monochrome) 160 dp in `primary`. Web: carattere "?" da 6 rem in `primary`.
- Android `A:schermate/SchermataDistribuzione.kt:362-369`. Web `W:componenti/CartaGirevole.svelte:18` e `:59-64`.
- Correzione: allineare (suggerito: Android al "?" come da design, o aggiornare design.md se si vuole la maschera).

### M12. Distribuzione: contenitore dell'azione in basso
- Android: il pulsante a pressione lunga e "Nascondi e passa" stanno sullo sfondo, con margini laterali 24 e distanza tra elementi 24. Web: usano il piede di `Pagina` (sfondo `surfaceContainer`, raggio superiore 28, margini 16), distanza tra elementi 16. Il design 4.5 dice "la rivelazione non cambia sfondo".
- Android `A:schermate/SchermataDistribuzione.kt:188-205` (padding 191-192, spacing 200). Web `W:componenti/Pagina.svelte:76-89` (footer) usato da `DistribuzionePassaggio.svelte:~112-120` e `DistribuzioneRivelazione.svelte:~107-111`; `.scena` `:66-72` (gap 16).
- Correzione: scegliere una sola soluzione; più vicina al design è lasciare l'azione senza barra e con margine 16.

### M13. Passo 3: pulsanti "Seleziona tutte" / "Deseleziona tutte"
- Android: "Seleziona tutte" tonale e "Deseleziona tutte" contorno. Web: entrambi contorno. Con `flex-wrap` e `width: auto` sul web i due pulsanti possono andare a capo su schermi stretti.
- Android `A:schermate/PassiConfigurazione.kt:186-197`. Web `W:schermate/PassoCategorie.svelte:58-59` (stile) e le due `<Pulsante variante="contorno">` del markup.
- Correzione: stessa variante (design 4.4 non indica quale; suggerito tonale + contorno come Android) e `flex-wrap: nowrap`.

### M14. Indicatore dei passi: spaziature verticali
- Android: margine verticale 8 sopra e sotto, etichetta a 4 dp dalla scatola da 48 dp. Web: nessun margine sopra, etichetta con `margin-top: -8px`, quindi l'etichetta sta circa 12 px più in alto e l'intero blocco è più basso di circa 16.
- Android `A:schermate/SchermataConfigurazione.kt:117` e `A:componenti/IndicatorePassi.kt:110`. Web `W:componenti/IndicatorePassi.svelte:95-96`, `W:schermate/Configurazione.svelte:111`, `W:componenti/Pagina.svelte:57-60`.
- Correzione: `padding-top: 8px` alla testata web, `margin-top: -4px` all'etichetta (o Android con -4); unificare e verificare con uno screenshot a 390 px.

### M15. Cambio passo: cosa si muove
- Atteso (design 4.4): scorrimento orizzontale di 48 dp con dissolvenza; con "Riduci animazioni" solo dissolvenza di 100 ms.
- Android: l'intera schermata (barra, indicatore, barra azioni) scorre per 48 dp in entrata e in uscita (NavHost). Web: `{#key passo}` ricrea `Pagina`, quindi barra e piede sono rimontati e solo `.passo` entra con `fly`; nessuna animazione di uscita; l'indicatore non anima mai la larghezza (arriva già nello stato finale), contrariamente a design 3. Il titolo prende il focus a ogni passo (`BarraApp.svelte:~16`).
- Android `A:navigazione/ImpostoreNavHost.kt:107-130`, `:180-187`. Web `W:schermate/Configurazione.svelte:59-60` (`{#key passo}`), `:78` (`in:ingresso`), `:31-33`.
- Correzione: web spostare `{#key}` dentro il solo contenuto; far animare `flex-grow` mantenendo vivo `IndicatorePassi`; scelta di design da fare per Android (animare solo il contenuto) per ottenere lo stesso effetto.

### M16. Impostazioni: segmenti del tema su due righe
- Android: due `SingleChoiceSegmentedButtonRow` sovrapposte con distanza 12 e quattro angoli pieni per riga. Web: griglia 2x2 con 2 px tra le righe, angoli pieni solo sui quattro esterni (un unico gruppo connesso).
- Android `A:schermate/SchermataImpostazioni.kt:88-96`. Web `W:componenti/Selettore.svelte:54-58` e `:18-29`.
- Correzione: scegliere il gruppo connesso e ridurre su Android la distanza tra le righe a 2 dp, con angoli interni `extraSmall`.

### M17. Campi di testo e caselle in Impostazioni e nel dialogo di segnalazione
- Atteso (design 3): campi "riempiti". Android usa `OutlinedTextField` (Impostazioni e dialogo). Web: campi riempiti (`CampoTesto`, textarea del dialogo). Caselle: Android `Checkbox` M3, web casella nativa.
- Android `A:schermate/SchermataImpostazioni.kt:113-126`, `A:schermate/SchermataRivela.kt:288-321`, `:284`. Web `W:componenti/CampoTesto.svelte:73-76`, `W:schermate/DialogoSegnalazione.svelte:98`, `:169-173`.
- Correzione: `TextField` riempito su Android; casella disegnata uguale all'M3 sul web.

### M18. Impostazioni: riga "Segnalazioni salvate / Cancella"
- Android: testo con peso 1 e pulsante "Cancella" a larghezza naturale. Web: `.riga :global(.pulsante) { flex: 1 1 10rem }`, quindi il pulsante occupa circa metà riga. Contatore dei caratteri: Android `bodySmall` 12/16, peso 400; web `--testo-didascalia`, peso 500. Nota finale: Android `bodySmall` 12/16, web `--testo-corpo-piccolo` 14/20.
- Android `A:schermate/SchermataImpostazioni.kt:143-159`. Web `W:schermate/Impostazioni.svelte:132-136`, `:148-150`, `:159-162`.
- Correzione: rendere il pulsante web `width: auto; flex: none`; Android `bodyMedium` per la nota.

### M19. Larghezza massima su schermi larghi (landscape, tablet)
- Atteso (design 2.4): colonna da 480, centrata. Web: barra superiore, contenuto e piede sono tutti nella stessa colonna da 480. Android: il contenuto è nei 480, ma `TopAppBar` e `BarraAzioni` occupano tutta la larghezza (il titolo sta a sinistra dello schermo, i pulsanti si allargano).
- Android `A:componenti/BarraSuperiore.kt:130-141` (`fillMaxWidth` senza `widthIn`) e `:265-286`. Web `W:componenti/Pagina.svelte:69-74`, `W:componenti/BarraApp.svelte:60-67`.
- Correzione: `widthIn(max = 480.dp)` sul `Column` interno di `BarraAzioni` e sul titolo/azioni della barra; sul web margine laterale 24 sopra i 600 dp (vedi B6) e Android uguale.

---

## BASSA

- B1. Regole e Impostazioni: titolo di sezione `primary` su Android (`SchermataRegole.kt:52`, `SchermataImpostazioni.kt:85`), `onSurface` sul web (`Regole.svelte:44`, `Impostazioni.svelte:129`). Padding verticale delle carte: Android 20 (Regole `:51`, Impostazioni `:188`), web 24 e 16 (`Regole.svelte:39`, `Impostazioni.svelte:124`). Margine superiore del contenuto: Android 16 (`SchermataRegole.kt:33`), web 8 (`Pagina.svelte:73`).
- B2. Stile `bodySmall` (12/16) usato su Android dove il design prevede `--testo-corpo-piccolo` (14/20): errore nella barra (`SchermataConfigurazione.kt:86`), seconda riga del riepilogo (`PassiConfigurazione.kt:334`), nota Impostazioni (`SchermataImpostazioni.kt:157`), errore del dialogo (`SchermataRivela.kt:326`).
- B3. Carattere dei pulsanti testo: Android `labelLarge` 14/20 (`Pulsanti.kt:149`), web `--testo-titolo` 16/24 (`Pulsante.svelte:61` + `.pulsante` 26). "Inizia" nella barra web usa `--testo-titolo` (`Configurazione.svelte:119`).
- B4. Badge "N attive": Android `labelMedium` 12 con raggio `large` (`PassiConfigurazione.kt:355-362`); web passo 2 `--testo-etichetta` 14, padding 4/12 (`PassoOpzioni.svelte:94-97`); web passo 4 `--testo-didascalia`, padding 2/10 (`PassoRiepilogo.svelte:85-89`). Tre varianti.
- B5. Rivela: testo hero Android `headlineMedium` 28 / `titleLarge` 22 (`SchermataRivela.kt:134`), web 28 / 24 px (`Rivela.svelte:127-134`); categoria Android `titleMedium` 600 (`:160`), web `--testo-corpo` 400 (`Rivela.svelte:148`); icona del promemoria: Android `Icons.Filled.Info` (`:177`), web carattere "ⓘ" (`Rivela.svelte:87`).
- B6. Margine di schermata: 16 fisso su Android (`Spazio.kt:margineSchermata`), 24 da 600 px sul web (`Pagina.svelte:94-99`). Design 2.4: 24 da 600.
- B7. Gioco e Rivela: il pulsante "Rivedi la parola" e "Segnala" hanno l'icona su Android (`SchermataGioco.kt:67`, `SchermataRivela.kt:101`), non sul web (`Gioco.svelte:66`, `Rivela.svelte:98`). Distanza tra i pulsanti del piede Gioco: Android 8, web 4 (`Gioco.svelte:163-168`).
- B8. Manifest: `theme_color` e `background_color` solo `#141218` (`web/vite.config.ts:26-27`); design 6 PWA chiede `#FEF7FF` / `#141218`. Il tema chiaro mostra il lampo scuro all'avvio.
- B9. Contatore: spazio tra pulsanti e valore 0 (Android `PassiConfigurazione.kt:398-415`), 4 px sul web (`Contatore.svelte:~51`).
- B10. "Passa il telefono a": colore `onSurfaceVariant` su Android (`SchermataDistribuzione.kt:271`), `--colore-su-sfondo` sul web (`DistribuzionePassaggio.svelte:94`). Avviso: Android `bodyMedium` 14/20 (`:289`), web `--testo-corpo-piccolo` uguale.
- B11. "Giri di indizi": titolo Android `titleMedium` (`OpzioniAvanzate.kt:67`), web `--testo-corpo` (`PassoOpzioni.svelte:100`); scheda di gruppo padding orizzontale 20 su Android (`OpzioniAvanzate.kt:100`), 16 sul web (`PassoOpzioni.svelte:88`); distanza tra le schede 12 su Android (`:33`), 16 sul web (`:85`).
- B12. Haptic: Android usa `LongPress` alla rivelazione e `TextHandleMove` (quasi impercettibile) su "Nascondi" (`SchermataDistribuzione.kt:327`, `:345`); il web usa `vibra(30)` per entrambi (`DistribuzioneRivelazione.svelte:~50-56`). Gli effetti non sono confrontabili e la vibrazione di "Nascondi" non esiste di fatto su Android.
- B13. "Riduci animazioni": su Android il morph del pulsante (100 ms) e la scala 0,97 restano attivi (`Pulsanti.kt:40-50`); sul web sono azzerati dalla regola globale (`tema.css:311-317`). Anche l'anello dell'avatar corrente e il pollice dell'interruttore web scattano; Android no per l'interruttore di sistema. Differenza minima.
- B14. Iniziale dell'avatar: Android `titleMedium` 16 per 40 dp e `displaySmall` 36 per 96 dp (`Avatar.kt:89`); web 42% del diametro (16,8 e 40,3 px) (`Avatar.svelte:45`).

---

## Da verificare sul dispositivo

- Sfondo oscurato dei dialoghi su Android: `AlertDialog` e `Dialog` di Compose usano il dim di piattaforma (circa 60%), non il 32% di design 2.1; sul web è `--colore-scrim` (32%, 60% in Alto contrasto). `A:componenti/Dialogo.kt:56-65`, `SchermataRivela.kt:243-257`.
- Peso 800 di Roboto Flex su Android: `Typography.kt` crea cinque istanze dello stesso file variabile con `FontVariation.weight`; controllare che 700/800 siano resi diversi dal 400 su un telefono reale, e che il web carichi i due `.woff2` di `tema.css:4-23` anche nella build (percorso `../../node_modules/...`).
- Altezza dei `SegmentedButton` (min 48 dp) e ombra/elevazione assente: M3 usa un bordo `outline` di 1 dp; sul web `--colore-contorno` 1 px. Controllare gli spigoli.
- Tastiera aperta in configurazione: `BarraAzioni` usa `safeDrawing` (`BarraSuperiore.kt:136`), ma il contenuto non ha `imePadding()` (`SchermataConfigurazione.kt:119-126`); verificare che l'ultimo campo nome non resti coperto (CA-96). Sul web `interactive-widget=resizes-content` fa la stessa cosa.
- `TestoAdattivo` Android riduce il carattere ricalcolando il layout a ogni passo (`Componenti.kt:180-188`): controllare nomi lunghi da 20 caratteri in Passaggio/Gioco (sfarfallio).
- Ordine di tabulazione e focus del titolo a ogni passo sul web (`BarraApp.svelte` `onMount` con `focus()` dentro `{#key passo}`): potrebbe far rileggere il titolo a ogni "Avanti" con VoiceOver/TalkBack.
