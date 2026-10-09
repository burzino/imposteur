# Imposteur - Design (versione 2, 2026-10-09: configurazione a passi)

Riferimento visivo: `docs/design/bozza.html` (file autonomo, tutte le schermate principali in tema chiaro e scuro, 390 px).
Android (Compose Material 3) e PWA (Svelte + CSS) devono essere identiche per layout, colori, tipografia e movimento. Ogni valore è un token con un nome unico; questa pagina è l'unica fonte. Il codice usa i nomi CSS (`web/src/ui/tema.css`) e i nomi Compose (`ui/theme/`) indicati in tabella.

## 1. Principi

1. **Un telefono, tante mani.** Testi grandi e leggibili a un braccio di distanza, bersagli di tocco generosi (min 48 dp, 56 dp per le azioni principali), una sola azione principale per schermata.
2. **Il segreto non si vede mai per errore.** La schermata di Passaggio e quella di Rivelazione hanno lo stesso ingombro, lo stesso sfondo e lo stesso movimento per tutti i ruoli. In "Parola affine" civili e impostore vedono esattamente lo stesso aspetto. Nessun colore, icona o animazione dipende dal ruolo. La vibrazione è una sola, uguale per tutti.
3. **Expressive, ma sobrio.** Forme ampie (pillole e angoli da 28 dp), tipografia marcata (peso 700-800 per titoli e nomi), superfici a livelli tonali senza ombre, movimento a molla con un leggero rimbalzo. Una sola tinta (viola) più il tono caldo dell'avatar; niente decorazioni.
4. **Edge-to-edge.** Lo sfondo passa sotto barre di sistema e barra di navigazione; il contenuto rispetta gli inset (`WindowInsets.safeDrawing` / `env(safe-area-inset-*)`). Le azioni fisse in basso restano sopra la tastiera.
5. **Un passo, un compito.** La configurazione è un percorso di 5 passi (Giocatori, Modalità, Opzioni, Categorie, Riepilogo); ogni passo ha pochi controlli e una sola azione principale, con la barra azioni sempre visibile in basso.
6. **Accessibile per costruzione.** Contrasto AA in tutti i temi, AAA in Alto contrasto; movimento ridotto rispettato; ogni informazione trasmessa dal colore ha anche testo o forma.
7. **I colori attuali restano.** Viola `#3F2B96` / `#CBBEFF`, giallo dell'Alto contrasto, palette degli avatar: invariati. Cambiano solo le superfici (vedi 2.1).

## 2. Token

### 2.1 Colori

I ruoli sono quelli di Material 3 (`ColorScheme`). I valori marcati **NUOVO** o **CAMBIA** sono le uniche differenze dai colori di oggi.

Tema Chiaro:

| Token CSS | Ruolo Compose | Valore | Note |
|---|---|---|---|
| `--colore-primario` | `primary` | `#3F2B96` | invariato |
| `--colore-su-primario` | `onPrimary` | `#FFFFFF` | invariato |
| `--colore-contenitore-primario` | `primaryContainer` | `#E6DEFF` | invariato |
| `--colore-su-contenitore-primario` | `onPrimaryContainer` | `#1B0F5C` | invariato |
| `--colore-secondario` | `secondary` | `#625B71` | invariato |
| `--colore-su-secondario` | `onSecondary` | `#FFFFFF` | invariato |
| `--colore-contenitore-secondario` | `secondaryContainer` | `#E8DEF8` | invariato |
| `--colore-su-contenitore-secondario` | `onSecondaryContainer` | `#1D192B` | invariato |
| `--colore-terziario` | `tertiary` | `#7D5260` | invariato |
| `--colore-contenitore-terziario` | `tertiaryContainer` | `#FFD8E4` | NUOVO (overlay "Tutti pronti!", avviso) |
| `--colore-su-contenitore-terziario` | `onTertiaryContainer` | `#31111D` | NUOVO |
| `--colore-sfondo` / `--colore-superficie` | `background` / `surface` | `#FEF7FF` | CAMBIA da `#FFFBFE`: tinta viola appena percepibile, base delle superfici a livelli |
| `--colore-su-sfondo` / `--colore-su-superficie` | `onBackground` / `onSurface` | `#1D1B20` | CAMBIA da `#1C1B1F` (stesso grigio, tonalità allineata) |
| `--colore-contenitore-superficie-minimo` | `surfaceContainerLowest` | `#FFFFFF` | NUOVO |
| `--colore-contenitore-superficie-basso` | `surfaceContainerLow` | `#F7F2FA` | NUOVO |
| `--colore-contenitore-superficie` | `surfaceContainer` | `#F3EDF7` | NUOVO |
| `--colore-contenitore-superficie-alto` | `surfaceContainerHigh` | `#ECE6F0` | NUOVO |
| `--colore-contenitore-superficie-massimo` | `surfaceContainerHighest` | `#E6E0E9` | NUOVO |
| `--colore-superficie-variante` | `surfaceVariant` | `#E7E0EC` | invariato (resta per compatibilità, le carte usano i contenitori) |
| `--colore-su-superficie-variante` | `onSurfaceVariant` | `#49454F` | invariato |
| `--colore-contorno` | `outline` | `#79747E` | invariato |
| `--colore-contorno-variante` | `outlineVariant` | `#CAC4D0` | invariato |
| `--colore-errore` / `--colore-su-errore` | `error` / `onError` | `#B3261E` / `#FFFFFF` | invariato |
| `--colore-contenitore-errore` / `--colore-su-contenitore-errore` | `errorContainer` / `onErrorContainer` | `#F9DEDC` / `#410E0B` | invariato |
| `--colore-scrim` | `scrim` | `#000000` al 32% | NUOVO (dialoghi) |

Tema Scuro (anche "Sistema" con sistema scuro):

| Token CSS | Valore | Note |
|---|---|---|
| `--colore-primario` / `--colore-su-primario` | `#CBBEFF` / `#2A1A7A` | invariati |
| `--colore-contenitore-primario` / `--colore-su-contenitore-primario` | `#4A3AA8` / `#E6DEFF` | invariati |
| `--colore-secondario` / `--colore-su-secondario` | `#CCC2DC` / `#332D41` | invariati |
| `--colore-contenitore-secondario` / `--colore-su-contenitore-secondario` | `#4A4458` / `#E8DEF8` | invariati |
| `--colore-terziario` / `--colore-su-terziario` | `#EFB8C8` / `#492532` | invariati |
| `--colore-contenitore-terziario` / `--colore-su-contenitore-terziario` | `#633B48` / `#FFD8E4` | NUOVO |
| `--colore-sfondo` / `--colore-superficie` | `#141218` | CAMBIA da `#1C1B1F`: più scuro, lascia spazio ai livelli (risparmia anche batteria su OLED) |
| `--colore-su-sfondo` / `--colore-su-superficie` | `#E6E0E9` | CAMBIA da `#E6E1E5` |
| `--colore-contenitore-superficie-minimo` | `#0F0D13` | NUOVO |
| `--colore-contenitore-superficie-basso` | `#1D1B20` | NUOVO |
| `--colore-contenitore-superficie` | `#211F26` | NUOVO |
| `--colore-contenitore-superficie-alto` | `#2B2930` | NUOVO |
| `--colore-contenitore-superficie-massimo` | `#36343B` | NUOVO |
| `--colore-superficie-variante` / `--colore-su-superficie-variante` | `#49454F` / `#CAC4D0` | invariati |
| `--colore-contorno` / `--colore-contorno-variante` | `#938F99` / `#49454F` | invariati |
| `--colore-errore` / `--colore-su-errore` | `#F2B8B5` / `#601410` | invariati |
| `--colore-contenitore-errore` / `--colore-su-contenitore-errore` | `#8C1D18` / `#F9DEDC` | invariati |

Alto contrasto: invariato (nero `#000`, bianco `#FFF`, giallo `#FFD600`, rosso `#FF8A80`, secondaryContainer `#1A1A1A`, scala testo 1,15). Tutti i contenitori di superficie sono `#000000`; i livelli si distinguono con un **contorno bianco da 2 dp** (vedi 2.5). Aggiunta: `tertiaryContainer` = `#000000` con `onTertiaryContainer` = `#FFD600` e contorno giallo.

Tema Sistema: segue Chiaro o Scuro. "Colori del telefono" (Material You, Android 12+): se attivo sostituisce solo i ruoli di colore, non i token di forma, tipografia e movimento. Sul web non esiste.

Colori dei giocatori (invariati, uguali in tutti i temi, testo `#FFFFFF`): `--colore-avatar-0..7` = `#C62828 #AD1457 #6A1B9A #4527A0 #1565C0 #00695C #2E7D32 #BF360C`. Compose: lista `ColoriAvatar` in `ui/theme/`.

Rapporti di contrasto calcolati (WCAG 2.x). Soglie: testo 4,5 (AA), componenti 3, AAA 7.

| Coppia | Rapporto |
|---|---|
| Chiaro: onSurface su sfondo | 16,2 |
| Chiaro: onSurface su contenitore massimo | 13,2 |
| Chiaro: onSurfaceVariant su contenitore alto / massimo | 7,6 / 7,2 |
| Chiaro: primario su sfondo / su contenitore alto | 10,0 / 8,6 |
| Chiaro: bianco su primario | 10,5 |
| Chiaro: onPrimaryContainer su primaryContainer | 12,8 |
| Chiaro: onSecondaryContainer su secondaryContainer | 13,2 |
| Chiaro: errore su sfondo / onErrorContainer su errorContainer | 6,2 / 12,8 |
| Chiaro: onTertiaryContainer su tertiaryContainer | 13,2 |
| Chiaro: contorno su sfondo (solo componenti, soglia 3) | 4,3 |
| Scuro: onSurface su sfondo / su contenitore massimo | 14,4 / 9,5 |
| Scuro: onSurfaceVariant su contenitore alto / massimo | 8,4 / 7,2 |
| Scuro: primario su sfondo / su contenitore alto | 10,9 / 8,4 |
| Scuro: onPrimary su primario | 8,1 |
| Scuro: onPrimaryContainer su primaryContainer | 6,6 |
| Scuro: onSecondaryContainer su secondaryContainer | 7,2 |
| Scuro: errore su sfondo / onErrorContainer su errorContainer | 10,9 / 7,2 |
| Scuro: onTertiaryContainer su tertiaryContainer | 7,2 |
| Scuro: contorno su sfondo (componenti) | 5,9 |
| Alto contrasto: bianco su nero | 21,0 |
| Alto contrasto: giallo su nero | 14,9 (AAA) |
| Alto contrasto: rosso `#FF8A80` su nero | 9,2 (AAA) |
| Alto contrasto: bianco su `#1A1A1A` | 17,4 (AAA) |
| Avatar: bianco su ciascuno degli 8 colori | 5,1 - 10,2 (minimo: avatar 6, 5,1) |

Tutti i testi superano AA; gli Alti contrasto superano AAA. Il token `--colore-contorno` chiaro (4,3) è usato solo per bordi di componenti (soglia 3).

### 2.2 Tipografia

Famiglia: Roboto Flex incorporato (sottoinsieme latino) su Android e web (decisione 2026-10-09, punto aperto 1 chiuso); `system-ui` solo come fallback.
Il web usa `rem` con `html { font-size: calc(100% * var(--scala-testo)) }`; Compose usa `sp` e `Typography().scala(f)` (già presente per l'Alto contrasto, 1,15).

| Token CSS | Stile Compose | Dimensione / interlinea | Peso | Uso |
|---|---|---|---|---|
| `--testo-display` | `displayMedium` enfatizzato | 44 / 52 sp | 800 | titolo Home, "Tutti pronti!" |
| `--testo-nome-grande` | `displaySmall` enfatizzato | 36 / 44 sp | 800 | nome nel Passaggio, parola nella Rivelazione |
| `--testo-titolo-schermata` | `headlineMedium` enfatizzato | 28 / 36 sp | 700 | titolo della barra |
| `--testo-titolo-sezione` | `titleLarge` enfatizzato | 22 / 28 sp | 700 | "Nomi", "Modalità", "Categorie" |
| `--testo-titolo` | `titleMedium` | 16 / 24 sp | 600 | etichette di riga, pulsanti |
| `--testo-corpo` | `bodyLarge` | 16 / 24 sp | 400 | testo normale |
| `--testo-corpo-piccolo` | `bodyMedium` | 14 / 20 sp | 400 | descrizioni, riepilogo |
| `--testo-etichetta` | `labelLarge` | 14 / 20 sp | 600 | pulsanti piccoli, badge |
| `--testo-didascalia` | `labelMedium` | 12 / 16 sp | 500 | indicatori ("Giocatore 2 di 5") |

I pesi 700/800 richiedono un font variabile (Roboto Flex lo è). Compose: `FontWeight(800)` con `fontVariationSettings` se si incorpora il font; con il font di sistema si usa il peso più vicino. Sul web `font-weight: 800`.

### 2.3 Forme

| Token CSS | Compose (`Shapes`) | Valore | Uso |
|---|---|---|---|
| `--raggio-xs` | `extraSmall` | 4 dp | cursore, segmenti interni |
| `--raggio-s` | `small` | 8 dp | campi, chip |
| `--raggio-m` | `medium` | 12 dp | pulsante premuto (morph), riga lista |
| `--raggio-l` | `large` | 20 dp | invariato, elementi minori |
| `--raggio-xl` | `extraLarge` | 28 dp | carte, schede di gruppo, dialoghi |
| `--raggio-xxl` | `RoundedCornerShape(36.dp)` | 36 dp | carta che si gira, pulsante a pressione lunga |
| `--raggio-pieno` | `CircleShape` | 999 | pulsanti, badge, avatar |

I pulsanti sono pillole a riposo e passano a `--raggio-m` mentre sono premuti (morph in `--durata-veloce`). Stessa regola per le due piattaforme.

### 2.4 Spaziature e misure

| Token CSS | Compose | Valore |
|---|---|---|
| `--spazio-1..6` | `Spazio.s1..s6` | 4, 8, 12, 16, 24, 32 dp (invariati) |
| `--spazio-7` | `Spazio.s7` | 48 dp (NUOVO) |
| `--margine-schermata` | `Spazio.s4` | 16 dp (24 dp da 600 dp di larghezza) |
| `--altezza-tocco` | `48.dp` | minimo ovunque (invariato) |
| `--altezza-pulsante` | `56.dp` | pulsante principale e secondario |
| `--altezza-pulsante-grande` | `72.dp` | solo pressione lunga |
| `--larghezza-max` | `widthIn(max = 480.dp)` | invariato; sopra 600 dp il contenuto resta centrato |
| `--misura-avatar-riga` / `--misura-avatar-grande` | `40.dp` / `96.dp` | |
| `--passo-altezza` | `8.dp` | spessore dei segmenti dell'indicatore dei passi |
| `--passo-spazio` | `4.dp` | distanza tra i segmenti |
| `--passo-corrente-peso` | `2.4f` (peso `Modifier.weight`) | il segmento corrente è 2,4 volte più largo degli altri (flex-grow su web) |

### 2.5 Elevazione tonale

Niente ombre. La profondità si ottiene con i contenitori di superficie.

| Livello | Token | Uso |
|---|---|---|
| 0 | `--colore-sfondo` | pagina |
| 1 | contenitore basso | carte di contenuto (liste, categorie) |
| 2 | contenitore | barra azioni in basso |
| 3 | contenitore alto | campi, carta del ruolo |
| 4 | contenitore massimo | dialoghi, segmento selezionato non primario |

Alto contrasto: ogni livello è nero con `--spessore-contorno: 2px` (Compose `BorderStroke(2.dp, onSurface)`); gli altri temi `0`.

### 2.6 Animazioni (MotionScheme expressive)

| Token CSS | Compose | Valore | Uso |
|---|---|---|---|
| `--molla-spaziale` | `MotionScheme.expressive().defaultSpatialSpec()` (smorzamento 0,8, rigidezza 700) | CSS: `450ms cubic-bezier(0.34, 1.4, 0.64, 1)` (approssimazione con leggero rimbalzo) | spostamenti, morph di forma, fisarmonica |
| `--molla-spaziale-lenta` | `slowSpatialSpec()` (0,8, 300) | `600ms cubic-bezier(0.34, 1.3, 0.64, 1)` | rotazione della carta |
| `--molla-effetti` | `defaultEffectsSpec()` (1,0, 1600) | `200ms cubic-bezier(0.2, 0, 0, 1)` | colore, opacità |
| `--durata-veloce` | `100.ms` tween | 100 ms | morph del pulsante premuto |
| `--durata-barra` | `durataPressioneMs.ms` lineare (variabile, impostata dal codice) | 150 ms predefinito (0-1000, Impostazioni) | riempimento della barra (specifiche 4.3, 4.7); a 0 la barra non esiste |
| `--durata-rilascio-barra` | `150.ms` | 150 ms | barra che torna a zero |
| `--durata-pronti` | `1200.ms` | 1200 ms | overlay "Tutti pronti!" (da specifiche) |

Con "Riduci animazioni" (Android: scala animazioni 0; web: `prefers-reduced-motion`): niente rimbalzo né rotazione 3D; la carta passa con dissolvenza di 100 ms; la barra si riempie comunque nella durata scelta (è informazione, non decorazione).
Lo schermo resta acceso in Distribuzione/Rivedi (invariato).

## 3. Componenti

Per ogni componente: aspetto, stati, Compose, CSS. Compose è verificato sulla BOM `2025.09.01`, cioè material3 1.4.0 (stabile). Le API Expressive in quella versione (`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ButtonGroup`, `ToggleButton`, `LoadingIndicator`) sono ancora annotate `@ExperimentalMaterial3ExpressiveApi`: usarle con `@OptIn` solo dove indicato; vedi punto aperto 2. Dove un componente non esiste, si disegna con i primitivi (`Box`, `Row`, `clip`, `animateFloatAsState`).

**Pulsante pieno (azione principale)**: pillola, altezza 56, `primary` / `onPrimary`, testo `--testo-titolo`. Premuto: raggio 12 (`--raggio-m`), scala 0,97. Disabilitato: contenitore `onSurface` 12%, testo `onSurface` 38%. Compose: `Button` con `shape` animata (`interactionSource.collectIsPressedAsState`). CSS: `.pulsante-pieno`.
**Pulsante tonale (secondario)**: come sopra con `secondaryContainer` / `onSecondaryContainer`. Compose `FilledTonalButton`.
**Pulsante contorno**: bordo 1 dp `outline`, testo `primary`. Compose `OutlinedButton`.
**Pulsante testo**: senza contenitore, `primary`, altezza 48. Compose `TextButton`. Usato per "Rivedi la parola" e "Azzera".
Tutti: area di tocco >= 48, ripple Android, `:active` + `:focus-visible` (outline 3 px `primary`, offset 2) sul web.

**Pulsante a pressione lunga ("Tieni premuto per scoprire")**: pillola grande 72 dp, raggio `--raggio-xxl`. Binario: `primaryContainer` con testo `onPrimaryContainer`; riempimento: `primary` che avanza da sinistra a destra in `--durata-barra` (durata scelta nelle Impostazioni, predefinita 150 ms) lineari mentre si tiene premuto. **Durata 0 ms**: nessuna barra né secondo livello di testo; il pulsante è un `primaryContainer` pieno con solo il morph di pressione (raggio 12, scala 0,97) come riscontro; il ruolo si rivela al rilascio; testo "Tocca per scoprire" e didascalia "Sono <nome> — tocca". Il testo ha due livelli identici sovrapposti: quello sotto in `onPrimaryContainer`, quello sopra in `onPrimary` e ritagliato dalla stessa larghezza del riempimento, così il contrasto è corretto in ogni punto (10,5 sulla parte piena). Rilascio prima della fine: la barra torna a zero in 150 ms, nessun effetto. Barra piena: rivela. Primi 600 ms: disabilitato (opacità 38%). Sotto: didascalia "Sono <nome> — tieni premuto" in `--testo-corpo-piccolo`. Azione di accessibilità "Scopri il ruolo" (invariata). Compose: `Box` con `pointerInput(detectTapGestures onPress)` e `drawWithContent` + `clipRect`; CSS: `<button>` con due `<span>` e `clip-path: inset(0 calc(100% - var(--avanzamento)) 0 0)`.

**Carta che si gira (Rivelazione)**: superficie `surfaceContainerHigh`, raggio `--raggio-xxl`, padding 32, altezza minima 280. Rotazione 3D intorno all'asse Y da 0 a 180 gradi in `--molla-spaziale-lenta`; il fronte (retro della carta, cioè lo schema del Passaggio con il punto interrogativo) sparisce a 90 gradi e compare il contenuto. Stessa carta, stesso colore, stessa durata per tutti i ruoli. Compose: `graphicsLayer { rotationY; cameraDistance = 12 * density }`. CSS: `transform: rotateY()` con `perspective: 1200px` e `backface-visibility: hidden`. Il contenuto (parola/ruolo) usa `--testo-nome-grande` per la parola e `--testo-titolo-sezione` per l'etichetta.

**Avatar**: cerchio con iniziale (`--testo-titolo`, peso 700), colore `--colore-avatar-n`. Riga (40 dp): già visto = pieno con spunta (16 dp, bianca); corrente = anello 3 dp `primary` a 3 dp dall'avatar, ingrandito a 1,15; successivo = opacità 38%. Grande (96 dp) nel Passaggio e nel Rivedi, senza anello. Compose: `Box(CircleShape)`; CSS: `.avatar`.

**Contatore (giocatori, impostori)**: riga in carta `surfaceContainerLow` raggio 28: etichetta a sinistra, a destra "−" valore "+" con due pulsanti tonali circolari da 48 dp e valore in `--testo-titolo-sezione` al centro (min 40 dp). Al limite il pulsante è disabilitato. Il valore cambia con un rimbalzo breve (scala 1 -> 1,12 -> 1). Compose: `FilledTonalIconButton`; CSS: `.contatore`.

**Selettore a segmenti (Giri di indizi: 1 | 2 | 3)**: gruppo connesso di 3 pulsanti alti 48, raggio esterno pieno, interno `--raggio-xs`; selezionato: `secondaryContainer`, con spunta. Compose: `SingleChoiceSegmentedButtonRow` (stabile) o `ButtonGroup` con `ToggleButton` (sperimentale). CSS: `.segmenti`.

**Selettore di modalità**: due carte selezionabili (radio) in colonna, ciascuna con titolo, riga di spiegazione e indicatore a sinistra. Selezionata: contenitore `primaryContainer`, bordo 2 dp `primary`; altra: `surfaceContainerLow`. Altezza minima 72. Compose: `Surface(selected, onClick, role = RadioButton)` dentro un `Column(selectableGroup())`; CSS: `<label>` con `<input type="radio">` visivamente nascosto ma focalizzabile.

**Interruttore con descrizione**: riga di altezza min 64: etichetta (`--testo-titolo`) + descrizione (`--testo-corpo-piccolo`, `onSurfaceVariant`) e a destra l'interruttore M3 (con spunta nel pollice quando attivo). Tutta la riga è toccabile. Compose: `Switch` con `Modifier.toggleable`; CSS: `.interruttore`.

**Riga con casella (categorie)**: altezza min 48, casella M3 con raggio 4, raggruppate in una carta `surfaceContainerLow` raggio 28, separate da 2 dp di sfondo. Compose `Checkbox` in `Modifier.toggleable`.

**Campo nome**: campo "riempito" `surfaceContainerHigh`, raggio `--raggio-s` in alto, indicatore inferiore 2 dp (`primary` in focus, `error` in errore), etichetta "Giocatore n", messaggio di errore "Nome già usato" sotto. Compose `TextField` con `ImeAction.Next`; CSS: `.campo`.

**Riga giocatore** (passo 1): `Row` alta almeno 56, distanza interna 8, composta da tre parti. A sinistra l'**avatar** (cerchio 40 dp con l'iniziale del nome, `--colore-avatar-n`, stesso componente di Distribuzione, 3 sopra; con nome vuoto l'iniziale è quella di "Giocatore n", cioè "G") dentro un'area riservata di 48 x 48 dp. Al centro il campo nome, che occupa lo spazio rimasto. A destra la **"x"** (pulsante icona 48 dp, nome accessibile "Rimuovi giocatore n", n = posizione; icona `onSurfaceVariant`, ripple/`:active` in cerchio). Con 3 campi la "x" non è disegnata né focalizzabile, ma il suo spazio di 48 dp resta riservato, così la larghezza del campo non salta passando da 3 a 4 giocatori. L'area dell'avatar è riservata fin d'ora e per ora non è interattiva; in un'ondata successiva diventerà un bersaglio toccabile per una foto facoltativa (nessun aspetto da progettare adesso: nessun indicatore, badge o ripple, per non suggerire un'azione che non c'è). Compose: `Row { AvatarGiocatore(Modifier.size(48.dp)); TextField(Modifier.weight(1f)); IconButton }`; CSS: `.riga-giocatore` con `display:grid; grid-template-columns:48px 1fr 48px`.

**Pulsante "+ Aggiungi giocatore"**: pulsante testo (`primary`, altezza 48), testo con il segno "+" come parte della stringa, allineato a sinistra, nel flusso sotto l'ultima riga giocatore. Disabilitato (`onSurface` 38%, testo "Massimo 20 giocatori") con 20 campi. Compose `TextButton`; CSS `.pulsante-testo`.

**Indicatore dei passi** (nuovo, stile Expressive): una riga di N segmenti (qui 5) alti `--passo-altezza`, forma a pillola, distanza `--passo-spazio`, margine laterale 16, sotto la barra superiore. Passo fatto: riempito `primary`. Passo corrente: riempito `primary` e largo `--passo-corrente-peso` volte gli altri; passando da un passo all'altro la larghezza si anima con `--molla-spaziale` (rimbalzo leggero) e la lunghezza ceduta si ridistribuisce. Passo da fare: solo contorno 2 dp `outline` (contrasto 4,3 in Chiaro e 5,9 in Scuro contro lo sfondo, sopra il 3:1 richiesto per gli elementi grafici), niente riempimento. Sotto l'indicatore, l'etichetta "Passo k di N · Nome" in `--testo-didascalia`, `onSurfaceVariant`: l'informazione non è mai solo nel colore o nella larghezza. I segmenti dei passi già fatti sono bersagli di tocco (area 48 dp di altezza attorno a una barra da 8) e riportano a quel passo; il corrente e i futuri non sono interattivi. Accessibilità: il gruppo è un indicatore di avanzamento (`progressBarRangeInfo` da k a N; web `role="progressbar"` con `aria-valuenow`, `aria-valuemax` e `aria-valuetext="Passo 2 di 5: Modalità"`). Con "Riduci animazioni" la larghezza cambia senza molla. Compose: `Row` di `Box(Modifier.weight(animateFloatAsState(...)))` con `clip(CircleShape)` e `border`; nessun componente M3 stabile equivalente. Opzionale e sperimentale: `LinearWavyProgressIndicator` per il solo segmento corrente (vedi punto aperto 6). CSS: `.passi` / `.passo` con `transition: flex-grow var(--molla-spaziale)`.
**Scheda di gruppo (Opzioni)**: carta `surfaceContainerLow`, raggio 28, padding 12 20; titolo del gruppo in `--testo-etichetta` colore `primary` ("Ruoli", "Turni", "Fine partita"), poi le righe "Interruttore con descrizione" e il selettore a segmenti dei giri. Sostituisce il corpo della vecchia fisarmonica; `Fisarmonica.svelte` e `OpzioniAvanzate.kt` perdono la parte a fisarmonica.
**Titolo del passo**: sotto l'indicatore, titolo in `--testo-titolo-sezione` e riga sotto in `--testo-corpo-piccolo` (`onSurfaceVariant`); a destra del titolo, quando serve, il badge "N attive" (stesso stile di prima: `primaryContainer`, `--testo-etichetta`).
**Riga di riepilogo**: carta `surfaceContainerLow`, raggio 28, altezza min 72, padding 8 8 8 20. Etichetta in `--testo-didascalia` (`onSurfaceVariant`), valore in `--testo-titolo`, eventuale seconda riga in `--testo-corpo-piccolo` (max 2 righe, poi puntini), badge opzionale, a destra pulsante testo "Modifica" (48 dp). Tutta la riga è toccabile e porta al passo corrispondente. Compose: `Card(onClick)`; CSS: `.riga-riepilogo`.

**Barra azioni in basso**: contenitore `surfaceContainer`, raggio superiore 28, padding 16, sopra gli inset di sistema e la tastiera. Contiene, se serve, l'errore in `error` (`--testo-corpo-piccolo`, `role=alert`, centrato) e sotto una riga di pulsanti da 56: nella configurazione "Indietro" (contorno, peso 1) e "Avanti" / "Inizia" (pieno, peso 2), distanza 8; nel passo 1 il solo "Avanti" a tutta larghezza (il passo 1 non ha "Indietro": la freccia della barra porta alla Home). Altrove (Gioco, Rivela) come prima. Compose: `Scaffold(bottomBar = ...)` (già presente); CSS: `footer.piede` (già presente).

**Dialoghi**: `AlertDialog`, raggio 28, `surfaceContainerHigh`, titolo `--testo-titolo-sezione`, messaggio `--testo-corpo`, pulsanti testo allineati a destra (ordine: negativo, positivo). Scrim 32%. Ingresso: scala 0,9 -> 1 con `--molla-spaziale` e dissolvenza. Web: `<dialog>` con `showModal()`; focus sul primo pulsante.

**Barra superiore**: titolo a sinistra con `--testo-titolo-schermata`, freccia indietro 48 dp, azione Home dove esiste. Sfondo `--colore-sfondo`; scorrendo passa a `surfaceContainer` (tonale, senza ombra). Compose: `TopAppBar` (o `LargeFlexibleTopAppBar` sperimentale solo per Home/Regole).

## 4. Layout delle schermate

Colonna unica, larghezza max 480, margini 16, titolo nella barra. Misure in dp/px uguali.

### 4.1 Home
Centrata in verticale. In alto, medaglione 120 dp con la maschera (raggio 36, sfondo `#1E1029` come l'icona, occhi viola e ambra: identità invariata) e sotto "Imposteur" in `--testo-display`. In basso, tre pulsanti a tutta larghezza, altezza 56, distanza 12: "Riprendi partita" (pieno, solo se esiste), "Nuova partita" (pieno se manca "Riprendi", altrimenti tonale), "Come si gioca" (contorno). In alto a destra, icona Impostazioni 48 dp. Nessuna barra del titolo.
### 4.2 Regole
Barra con "Come si gioca". Tre carte (`surfaceContainerLow`, raggio 28, padding 20): "Impostore senza parola", "Parola affine", "Come finisce"; titolo `--testo-titolo-sezione`, testo `--testo-corpo`. Testi di `regole_*`.
### 4.3 Impostazioni
Barra "Impostazioni". Gruppo "Tema": selettore a segmenti a 4 posizioni su 2 righe (Sistema, Chiaro, Scuro, Alto contrasto), con spunta sul selezionato. Riga con interruttore "Colori del telefono" + descrizione (solo Android 12+; assente sul web). Sotto il Tema, gruppo "Pressione per scoprire" con il **cursore** (sotto). Riga "Segnalazioni" (apre l'elenco). Carte raggio 28.

**Cursore "Durata della pressione"** (nuovo): carta `surfaceContainerLow`, raggio 28, padding 12 20, titolo del gruppo in `--testo-etichetta` colore `primary`. Riga di testa: etichetta "Durata della pressione" (`--testo-titolo`) a sinistra e il valore "150 ms" a destra (`--testo-titolo-sezione`, `primary`, larghezza minima per 7 caratteri "1000 ms" per non far saltare il layout); sotto, la descrizione (`--testo-corpo-piccolo`, `onSurfaceVariant`) e poi il cursore a tutta larghezza. Aspetto M3 Expressive: binario alto 16 dp con estremi arrotondati (raggio pieno, interno `--raggio-xs`), parte attiva `primary`, parte inattiva `secondaryContainer`; maniglia a barra verticale 4 x 44 dp `primary` (si restringe a 2 dp e mostra un'etichetta valore sopra durante il trascinamento); 21 tacche (una ogni 50 ms) come punti da 4 dp `onPrimary` sulla parte attiva e `primary` su quella inattiva; area di tocco >= 48 dp; spostamento della maniglia con `--molla-spaziale` (senza molla con "Riduci animazioni"). Disabilitato mai. Compose: `Slider(valueRange = 0f..1000f, steps = 19)` con `SliderDefaults.Track` personalizzato (`@OptIn` Expressive solo se si usa `Slider` Expressive; altrimenti Slider stabile con `thumb` e `track` ridisegnati); `Modifier.semantics { stateDescription }`. CSS: `<input type="range" min="0" max="1000" step="50">` con `appearance: none`, `::-webkit-slider-runnable-track` / `::-moz-range-track` e `::-webkit-slider-thumb` / `::-moz-range-thumb`, parte attiva con `linear-gradient` su `--avanzamento`; `:focus-visible` outline 3 px `primary`, offset 2.
### 4.4 Configurazione a passi
Sostituisce la pagina unica (decisione utente 2026-10-09; portata a 5 passi lo stesso giorno con i giocatori come campi e la modalità in un passo proprio). Cinque passi, ognuno una rotta (`configurazione/1` ... `configurazione/5`), con la configurazione nello stato condiviso (ViewModel / store) e salvata come oggi. "Nuova partita" dalla Home e "Modifica giocatori e opzioni" dalla Rivela aprono sempre il passo 1; il passo corrente non viene salvato.

**Perché 5 passi e perché questo ordine.** I passi di input seguono l'importanza per chi gioca: prima chi gioca (cambia a ogni serata; il passo è solo la lista dei nomi, che cresce fino a 20 righe e non contende lo schermo ad altri controlli), poi che tipo di partita (modalità e impostori: pochi controlli a altezza fissa), poi le regole facoltative (di solito non si toccano), per ultime le categorie (di solito restano tutte). Il quinto è un riepilogo: dà una casa al riassunto delle opzioni, fa vedere in un colpo ciò che influenza la partita (incluse le opzioni nascoste nel passo 3) e permette di correggere con "Modifica" senza ripercorrere tutto. Non esiste più il contatore dei giocatori: il numero è il numero di campi. La modalità sta prima degli impostori perché li condiziona (affine, parole disponibili, "L'impostore vede la categoria"); impostori e "Impostori a sorpresa" restano adiacenti.

**Struttura comune** (colonna unica, margini 16):
1. Barra superiore: freccia indietro, titolo "Configurazione" (`--testo-titolo-schermata`). Nei passi 1, 2, 3 e 4 a destra il pulsante testo "Inizia" (scorciatoia, vedi sotto).
2. Indicatore dei passi: 5 pillole semplici (fatto / corrente / da fare, niente barra ondulata) con etichetta "Passo k di 5 · Nome" (componente in 3). A 360 dp ogni pillola non corrente misura circa 49 dp, quindi i passi fatti restano bersagli larghi almeno 48 dp.
3. Contenuto che scorre: titolo del passo e controlli; spazio finale di 96 dp.
4. Barra azioni fissa (componente in 3): errore del passo (se c'è) e i pulsanti. Con la tastiera aperta la barra sale sopra la tastiera e resta con errore e pulsanti.
Cambio passo: scorrimento orizzontale di 48 dp con dissolvenza, `--molla-spaziale` (verso destra avanzando, sinistra tornando); con "Riduci animazioni" solo dissolvenza di 100 ms.

**Passo 1, "Giocatori"** (etichetta nell'indicatore "Giocatori"). Titolo "Chi gioca?", riga "Come vi chiamate. Da 3 a 20 giocatori." Contiene solo i campi nome e il pulsante per aggiungerne, in quest'ordine:
1. Un campo nome per giocatore (componente "Riga giocatore" in 3), distanza 8 dp tra le righe, segnaposto "Giocatore n". Aggiungendo un campo, questo riceve il fuoco e il contenuto scorre finché è visibile sopra la barra azioni e la tastiera. Tasto Avanti della tastiera: campo successivo.
2. Subito sotto l'ultimo campo (nel flusso, scorre con i campi), il pulsante testuale "+ Aggiungi giocatore" (pulsante testo, altezza 48, allineato a sinistra con il bordo dei campi). Con 20 campi è disabilitato e il testo diventa "Massimo 20 giocatori" (stessa posizione, `onSurface` 38%).
Pulsanti: solo "Avanti". Blocco: "Avanti" disabilitato se c'è un nome duplicato; sopra il pulsante l'errore "Nome già usato" (il campo mostra lo stesso testo e l'indicatore inferiore rosso). Aggiungere o rimuovere ricalcola subito la validità. Il numero di giocatori (3-20) non può essere invalido: la "x" sparisce a 3 e "+ Aggiungi giocatore" si disabilita a 20.

**Passo 2, "Modalità"** (etichetta "Modalità"). Titolo "Come si gioca?", riga "Scegli la modalità e quanti impostori." Ordine:
1. "Modalità": le due carte selezionabili con le righe di spiegazione (invariate, CA-95).
2. Contatore "Numero impostori" / "Impostori (massimo)" quando "Impostori a sorpresa" è attivo; limiti 1 e floor((giocatori − 1) / 2). Se nel passo 1 si sono tolti giocatori, il valore è già stato portato al nuovo massimo.
3. Interruttore "Impostori a sorpresa" (con la sua riga descrittiva), subito sotto il contatore; non fa parte del passo 3 né del badge.
Pulsanti: "Indietro" e "Avanti", sempre abilitati.

**Passo 3, "Opzioni"** (etichetta "Opzioni"). Titolo "Opzioni avanzate", riga "Sono tutte facoltative: puoi andare avanti senza toccarle.", a destra del titolo il badge "N attive" (assente se 0). Tre schede di gruppo "Ruoli", "Turni", "Fine partita" con gli stessi controlli di prima (invariati: "L'impostore vede la categoria" visibile solo in "Impostore senza parola"). Sempre aperti: la fisarmonica non esiste più. Pulsanti: "Indietro" e "Avanti", sempre abilitati.

**Passo 4, "Categorie"** (etichetta "Categorie"). Titolo "Categorie", riga "Da quali argomenti pescare le parole." Poi i controlli di oggi: "Seleziona tutte" / "Deseleziona tutte", "Parole ancora da giocare: X / Y" con "Azzera", elenco con caselle. Pulsanti: "Indietro" e "Avanti". Blocco: "Avanti" disabilitato se nessuna categoria ("Seleziona almeno una categoria") o se le categorie scelte non hanno parole utilizzabili ("Le categorie scelte non contengono parole utilizzabili"); l'errore sta sopra i pulsanti.

**Passo 5, "Riepilogo"** (etichetta "Riepilogo"). Titolo "Tutto pronto?", riga "Controlla e inizia." Quattro righe di riepilogo, ognuna con "Modifica" che porta al passo indicato:
1. "Giocatori" (passo 1): valore "5 giocatori"; seconda riga i nomi separati da ", " (i vuoti come "Giocatore n").
2. "Modalità" (passo 2): valore "Impostore senza parola" o "Parola affine"; seconda riga il numero di impostori: "1 impostore" / "2 impostori"; con "Impostori a sorpresa" attivo e massimo > 1 "fino a 2 impostori" (con massimo 1 resta "1 impostore"). Il numero sta qui perché si modifica nel passo 2.
3. "Opzioni avanzate" (passo 3): l'elenco di ciò che non è predefinito, separato da ", ", nell'ordine di prima: nomi brevi esistenti, "senza categoria" (se spento e modalità "Impostore senza parola"), "N giri" (se > 1); se non c'è nulla, "Nessuna opzione attiva". Badge "N attive" come nel passo 3 (assente se 0). Il numero di impostori NON è in questo elenco.
4. "Categorie" (passo 4): "N categorie" ("1 categoria"), seconda riga "Parole ancora da giocare: X / Y".
Pulsanti: "Indietro" e "Inizia" (pieno). "Inizia" è sempre abilitato (si arriva qui solo con i passi 1-4 validi).

**Avanti, Indietro, tasto di sistema.** "Avanti" porta al passo successivo se il passo corrente è valido. "Indietro" (pulsante) e il tasto indietro di sistema/freccia della barra tornano al passo precedente senza mai essere bloccati dalla validazione; dal passo 1 tornano alla Home salvando la configurazione (come oggi). Android: ogni passo è una destinazione del NavHost, quindi indietro è il normale `popBackStack`; web: ogni passo aggiunge una voce di cronologia (`#/configurazione/2`), quindi il tasto indietro del browser fa lo stesso. "Modifica" del riepilogo e le pillole dei passi fatti navigano al passo scelto aggiungendolo alla cronologia (indietro riporta al riepilogo). Le modifiche si salvano a ogni cambio di passo, a ogni uscita e a "Inizia".

**Scorciatoia per chi rigioca: "Inizia" nella barra, passi 1-4** (decisione utente 2026-10-09: disponibile anche nel passo 1). Scelta: tasto testo "Inizia" nella barra superiore, non un secondo pulsante in basso (due pulsanti pieni insieme alla riga Indietro/Avanti sarebbero tre azioni in 56 dp e farebbero confondere "Avanti" con "Inizia"). Il passo 5 ha già "Inizia" come pulsante pieno e non duplica l'azione in barra. Se la configurazione non è valida (nome duplicato, nessuna categoria), "Inizia" porta al primo passo non valido (in ordine 1, 2, 3, 4) mostrando il suo errore, invece di restare disabilitato senza spiegazione; se è il passo corrente resta lì e mostra l'errore. Altrimenti salva e apre la Distribuzione.
### 4.5 Distribuzione
Schermo intero, senza barra; indietro chiede conferma. Il telefono passa nell'ordine di parola (specifiche §4.3, CA-119..CA-122): il Passaggio k mostra il giocatore alla posizione k dell'ordine; il layout non cambia. "Nascondi e inizia" compare all'ultima posizione. FLAG_SECURE su Android. Sfondo `--colore-sfondo`; la rivelazione non cambia sfondo.
- **Passaggio**: in alto la fila di avatar (40 dp, centrata sul corrente; segue l'ordine di parola, CA-120) e sotto "Giocatore k di N" (`--testo-didascalia`, k = posizione nell'ordine); al centro "Passa il telefono a" (`--testo-corpo`), il nome in `--testo-nome-grande`, avatar grande 96 dp; poi la riga d'avviso "Gli altri non guardino lo schermo" in una pillola `tertiaryContainer` con testo `onTertiaryContainer`; in basso il pulsante a pressione lunga e la didascalia "Sono <nome> — tieni premuto" (con durata 0: "Tocca per scoprire" e "Sono <nome> — tocca", senza barra).
- **Rivelazione**: stessa fila di avatar; al centro la carta che si gira (stessa posizione del nome nel Passaggio); in basso il pulsante pieno "Nascondi e passa" ("Nascondi e inizia" per l'ultimo), disabilitato 600 ms.
- **Overlay "Tutti pronti!"**: schermo intero `tertiaryContainer`, "Tutti pronti!" in `--testo-display` (`onTertiaryContainer`) e sotto "Che il bluff abbia inizio" in `--testo-titolo`; ingresso con scala 0,8 -> 1 (`--molla-spaziale`); 1200 ms o tocco.
### 4.6 Gioco
Barra "Si gioca!" (senza freccia: indietro chiede conferma). Carta in evidenza `primaryContainer` raggio 28: "Parla per primo:" e il nome in `--testo-nome-grande`. Sotto, "Ordine di parola" con elenco numerato (numero in cerchio 32 `secondaryContainer` + nome), in carta `surfaceContainerLow`; con più giri, intestazioni "Giro n". Testo di istruzione. In basso, barra azioni con "Rivela" (pieno) e sopra, piccolo, "Rivedi la parola" (pulsante testo).
### 4.7 Rivedi la parola
Barra "Rivedi la parola" con freccia. Elenco nell'ordine di parola (come Gioco, CA-122): testo "Tocca il tuo nome" e righe di 64 dp in carta (avatar 40 + nome, tocco su tutta la riga). Passaggio: come 4.5 senza fila di avatar e senza "Giocatore k di N". Rivelazione: carta come 4.5 e pulsante "Nascondi".
### 4.8 Rivela
Barra "Il risultato". Carta hero `primaryContainer` raggio 36 con "L'impostore era:" / "Gli impostori erano:" e i nomi con avatar piccoli (o "Nessun impostore: era una partita trappola!"). Sotto una carta `surfaceContainerLow` con "La parola era", "La parola affine era" (solo affine) e "Categoria". Riquadro promemoria `tertiaryContainer` se previsto. In basso, barra azioni: "Rigioca (stessi giocatori)" (pieno) e "Modifica giocatori e opzioni" (tonale).

## 5. Testi nuovi o cambiati

| Dove | Testo | Motivo |
|---|---|---|
| Modalità, riga sotto "Impostore senza parola" | "I civili conoscono la parola, l'impostore deve bluffare." | richiesta utente |
| Modalità, riga sotto "Parola affine" | "L'impostore riceve una parola simile ma diversa." | richiesta utente |
| Riepilogo delle opzioni (passo 5 e 3) | "senza categoria", "Nessuna opzione attiva", nomi brevi esistenti separati da ", " | non esiste più la fisarmonica; il numero di impostori esce da questo elenco |
| Passi: pulsanti | "Avanti", "Indietro", "Inizia", "Modifica" | percorso a passi (verificare quali esistono già in `strings.xml`) |
| Indicatore | "Passo %1$d di %2$d · %3$s" (visibile) e "Passo %1$d di %2$d: %3$s" (descrizione per l'accessibilità) | nuovo |
| Nomi dei passi | "Giocatori", "Modalità", "Opzioni", "Categorie", "Riepilogo" | nuovo |
| Passo 1, aggiunta | "+ Aggiungi giocatore"; con 20 campi "Massimo 20 giocatori" | giocatori come campi (CA-113, CA-114) |
| Passo 1, rimozione | descrizione accessibile della "x": "Rimuovi giocatore %1$d" | CA-115 |
| Titoli e righe dei passi | "Chi gioca?" / "Come vi chiamate. Da 3 a 20 giocatori."; "Come si gioca?" / "Scegli la modalità e quanti impostori."; "Opzioni avanzate" / "Sono tutte facoltative: puoi andare avanti senza toccarle."; "Categorie" / "Da quali argomenti pescare le parole."; "Tutto pronto?" / "Controlla e inizia." | nuovo |
| Righe del riepilogo | etichette "Giocatori", "Modalità", "Opzioni avanzate", "Categorie"; "Modifica" su ogni riga; valori "%d giocatori", nome della modalità con seconda riga "1 impostore", "%d impostori", "fino a %d impostori", "1 categoria", "%d categorie" | nuovo |
| Impostazioni, gruppo | "Pressione per scoprire" | durata della pressione (decisione 2026-10-09) |
| Impostazioni, cursore | "Durata della pressione"; valore "%1$d ms"; descrizione "Quanto tempo tenere premuto per scoprire il ruolo. Con 0 basta un tocco."; accessibilità "%1$d millisecondi" e "0 millisecondi, basta un tocco" | nuovo |
| Passaggio con durata 0 | "Tocca per scoprire"; "Sono %1$s — tocca" | nuovo |

Tutti gli altri testi restano quelli di `strings.xml`.

## 6. Differenze rispetto a oggi (piano per gli sviluppatori)

Le voci sono ordinate per passo implementabile. Dove non ho letto il codice Android è indicato "verificare".

### Android
1. **Tema** (`ui/theme/Theme.kt`): aggiungere i ruoli `surfaceContainer*`, `tertiaryContainer`, `scrim` e i nuovi `background/surface/onSurface` per Chiaro e Scuro (sezione 2.1); in Alto contrasto `tertiaryContainer` nero/giallo.
2. **Forme e tipografia**: definire `Shapes` (4, 8, 12, 20, 28) e una `Typography` con i pesi 700/800 della tabella 2.2; `Spazio` per i token 2.4. Passare a `MaterialExpressiveTheme` con `MotionScheme.expressive()` solo se il punto aperto 2 è sciolto; altrimenti usare `MaterialTheme` e le molle a mano (`spring(0.8f, 700f)`).
3. **Pulsanti**: pillole da 56 dp con morph del raggio da premuto; nuovo componente `PulsanteConMorph` in `Componenti.kt` (verificare i pulsanti attuali).
4. **Pressione lunga**: barra a due livelli di testo (2.3 componenti); altezza 72, raggio 36.
5. **Carta che si gira**: raggio 36, `surfaceContainerHigh`, molla lenta; con animazioni ridotte, dissolvenza.
6. **Avatar**: riga con anello e spunta, grande 96 dp nel Passaggio e nel Rivedi (verificare).
7. **Configurazione a passi** (4.4): cinque destinazioni `configurazione/{1..5}` nel NavHost, stato nel ViewModel esistente (nessun cambio di formato salvato). Nuovo `IndicatorePassi` in `Componenti.kt` (3), `RigaGiocatore` (avatar, campo, "x"; l'avatar è lo spazio riservato per la foto futura), `SchedaGruppo`, `RigaRiepilogo`, barra azioni a due pulsanti (`PiedeConfigurazione`). Contatore degli impostori con pulsanti circolari tonali (il contatore dei giocatori non esiste più); modalità come carte selezionabili (2 stringhe nuove); categorie in carta con righe a 48 dp; campi riempiti; spazio finale 96 dp. La validazione per passo (4.4) usa le funzioni di validazione già esistenti: nessuna logica nuova in `game/`. Stringhe nuove in `strings.xml` (sezione 5).
8. **Opzioni avanzate** (`OpzioniAvanzate.kt`): togliere la fisarmonica; le tre schede di gruppo diventano il corpo del passo 3; il riepilogo diventa la riga "Opzioni avanzate" del passo 5 (senza il numero di impostori, con "Nessuna opzione attiva"); badge nel titolo del passo e nella riga.
9. **Dialoghi**: raggio 28, ingresso a molla.
10. **Schermate**: Home con medaglione maschera e titolo display; Gioco con carta hero; Rivela con carta hero; Regole in tre carte; Impostazioni con segmenti a 4 (4.1-4.8).
11. **Edge-to-edge**: `enableEdgeToEdge()` e inset su ogni schermata (verificare lo stato attuale).

### PWA
1. **`tema.css`**: aggiungere i token di colore 2.1 (stessi nomi), tipografia (`--testo-*`), forme (`--raggio-xs/xl/xxl/pieno`), `--spazio-7`, misure, molle, durate e `--spessore-contorno`. Cambiare i valori di sfondo/superficie chiaro e scuro. Rinominare nulla: i token esistenti restano.
2. **Componenti** (`web/src/ui/componenti/`): `RigaGiocatore.svelte`; `Pulsante` pillola con morph e `:active`; `Contatore` con pulsanti circolari; `Selettore` a segmenti connessi con spunta; `Interruttore` con spunta nel pollice; `DialogoConferma` raggio 28 e animazione; `Avatar` con anello/spunta.
3. **`DistribuzioneTieniPremuto.svelte`**: doppio livello di testo ritagliato (clip-path) al posto del solo riempimento; altezza 72, raggio 36. Nota: il file ha modifiche non committate.
4. **`DistribuzioneRivelazione.svelte`**: rotazione `rotateY` con `perspective`, `backface-visibility`, molla lenta; `prefers-reduced-motion` -> dissolvenza.
5. **`Configurazione.svelte`**: diventa un contenitore di passi (rotte `#/configurazione/1..5`) con sotto-componenti per passo; nuovo `IndicatorePassi.svelte` (`role="progressbar"`, segmenti con `flex-grow` animato), `RigaRiepilogo.svelte`. Modalità come carte selezionabili con riga di spiegazione (`t.modalitaDescSenzaParola`, `t.modalitaDescAffine`); categorie in carta; spazio finale 96 px. Il piede fisso esiste già (`Pagina` con `piede`): sfondo `--colore-contenitore-superficie`, raggio superiore 28, `padding-bottom: env(safe-area-inset-bottom)`, due pulsanti, `interactive-widget=resizes-content` nel meta viewport. Testi nuovi in `testi.ts` (sezione 5).
6. **`OpzioniAvanzate.svelte`** e `Fisarmonica.svelte`: la fisarmonica non serve più alla configurazione (il componente può restare se usato altrove, altrimenti va tolto); le schede di gruppo sono il corpo del passo 3; il riepilogo (ora senza "N impostori") diventa una riga del passo 5, separatore ", " al posto di " · ".
7. **Schermate**: Home (medaglione SVG della maschera), Regole, Gioco, Rivela, Impostazioni (4.1-4.8). Impostazioni senza "Colori del telefono".
8. **PWA**: `theme-color` e `background-color` del manifest allineati a `#FEF7FF` / `#141218`.

### Test
Nessuna modifica alla logica di gioco. Tester: aggiornare i test di UI che citano il riepilogo della fisarmonica e aggiungere test per il percorso a passi (blocco di "Avanti" per passo, indietro, scorciatoia "Inizia", "fino a N impostori"); i CA sono nella sezione 8.

## 7. Punti aperti per l'utente

1. **Font**: DECISO (2026-10-09) Roboto Flex incorporato su Android e PWA (sottoinsieme latino, +~100 KB). Il paragrafo 2.2 e il piano vanno letti di conseguenza: non si usa il font di sistema.
2. **API Expressive / BOM di Compose**: DECISO (2026-10-09) la BOM va aggiornata per avere il set Expressive completo; da far compilare all'esecutore.
3. **Riepilogo con "N impostori"**: DECISO (2026-10-09), SUPERATO dalla configurazione a passi (4.4: il numero sta nella riga "Modalità" del riepilogo); in origine il numero di impostori compare SEMPRE nel riepilogo ("1 impostore" / "N impostori"), seguito dalle opzioni avanzate non predefinite; "Regole classiche" non esiste più; il badge conta come prima. Recepito in specifiche 4.2.1 e CA-92..CA-94 (la regola "N impostori se > 1" in 3 e 6 è superata).
4. **Superfici chiara e scura più scure/tinte** (2.1): l'unico cambio di colore proposto. Se non piace, i livelli si ricavano comunque con gli stessi token ma partendo da `#FFFBFE` e `#1C1B1F`.
5. **Bozza**: i rimbalzi e la rotazione 3D sono descritti ma non animati nel file statico.
6. **Indicatore dei passi**: DECISO (2026-10-09) pillole semplici, niente barra ondulata (`LinearWavyProgressIndicator` escluso).
7. **Dipendenza tra passi**: DECISO (2026-10-09) "Impostori a sorpresa" sta nel passo 2 (Modalità), subito sotto il contatore degli impostori; esce dalle opzioni avanzate e dal badge. Con l'interruttore attivo: etichetta "Impostori (massimo)" e riepilogo "fino a N impostori" (con massimo 1 resta "1 impostore").
8. **"Inizia" nella barra**: DECISO (2026-10-09) disponibile dai passi 1-4, compreso il passo 1. Con configurazione non valida porta al primo passo non valido e ne mostra l'errore. Nel passo 5 resta il pulsante pieno "Inizia" (nessun doppione in barra: interpretazione, da confermare).

## 8. Modifiche alle specifiche

Recepite in `docs/specifiche.md` §4.2, §4.2.1, §4.3 e nei CA-80..CA-105, CA-113..CA-118 (configurazione a 5 passi, giocatori come campi) e CA-119..CA-122 (distribuzione nell'ordine di parola). Questo documento non le duplica: la fonte è la specifica. Nessuna modifica al formato della configurazione salvata né alla sessione.
