# Stato del progetto

## Decisioni dell'utente (2026-10-06)
- Un solo telefono che gira tra i giocatori. Solo Android: Kotlin + Jetpack Compose.
- Due modalità: "Impostore senza parola" e "Parola affine" (Undercover).
- Solo parole preesistenti, generate da Claude in `app/src/main/assets/parole.json`.
- Fuori perimetro v1: voto in app, punteggi, classifiche, parole personalizzate.

## Ambiente
- JDK: `<ANDROID_STUDIO>\jbr` (OpenJDK 25), quindi Gradle 9.1 o superiore.
- SDK: `%LOCALAPPDATA%\Android\Sdk` (piattaforme 35 e 37.0, build-tools 36.0.0).
- Versioni previste: Gradle 9.1.0, AGP 8.13.0, Kotlin 2.2.20, Compose BOM 2025.09.01, compileSdk/targetSdk 36 (activity 1.11 richiede 36; la piattaforma la scarica AGP), minSdk 26.

## Agenti (`.claude/agents/`)
analista-funzionale, redattore-parole, sviluppatore, tester, revisore, designer-ui, esecutore-build, collaudatore.
Vengono caricati solo all'avvio di una nuova sessione. L'ondata 1 ha usato agenti generici che leggevano la definizione del proprio ruolo.

## Ondate
1. [specifiche fatte: 36 CA, 8 decisioni da confermare in §10] Specifiche (`docs/specifiche.md`) e parole (`parole.json`).
2. [fatto; prima build fallita per compileSdk 35, portato a 36, da ricompilare a fine ondata 3] Scheletro del progetto Gradle con wrapper, poi build della app vuota.
3. [fatto] Logica di gioco (`game`) e caricamento delle parole (`data`), più test unitari.
4. [fatto; build verde, 96/96 test, commit b5d0a03] Salvataggio/ripresa sessione (decisione utente 2026-10-06; anche parole usate persistenti): specifiche (analista), logica game/data (sviluppatore); poi test + UI ripresa. Contratto v1.1 in docs/contratto-api.md.
5. [fatto] Revisione statica della UI (docs/revisione-ui.md): corretti D1-D8 e aggiunto FLAG_SECURE in Distribuzione. Build verde, 96/96.
6. [da fare, serve l utente] Collaudo su telefono fisico via USB. L emulatore non parte: manca l accelerazione hardware (Hyper-V/WHPX non attivi, servono i privilegi di amministratore). Verificare anche: doppio tocco, am kill (process death), Recents, back predittivo.

## Decisioni recenti (2026-10-08)
- Nome dell'app: "Imposteur". Cambia solo il nome mostrato: package e id restano invariati.
- Aggiunti: Rivedi la parola; proposte UX 1–7, 13, 14 e 16; tema (Sistema, Chiaro, Scuro, Alto contrasto, più i colori dinamici); segnalazioni di coppie e suggerimenti in `Android/data/it.imposteur/files/segnalazioni.jsonl`, da leggere con adb.
- La vibrazione è identica per tutti, per non rivelare l'impostore.

## PWA (decisa il 2026-10-08, da avviare dopo la chiusura della v1 Android)
- Strada A: app web separata in `web/` con **Svelte 5 + TypeScript + Vite + vite-plugin-pwa** (scelta dell'utente, 2026-10-08), test con Vitest. Lo stesso `parole.json` (`app/src/main/assets/`) resta l'unica fonte e viene letto al momento della build. Le regole vengono dalle specifiche e sono verificate con test equivalenti che citano gli stessi CA-xx.
- Perimetro (decisione dell'utente: "traduci tutta l'applicazione"): tutte le funzioni Android, quindi due modalità, opzioni avanzate, salvataggio della partita e delle parole usate, Rivedi, ordine di parola, contatore con Azzera, temi, segnalazioni (esportate come file), restyling della distribuzione e tasto Home. Testi in italiano identici a `strings.xml`.
- Salvataggi in localStorage. Al posto di FLAG_SECURE e ON_STOP: `visibilitychange` (pagina nascosta) trasforma Rivelazione(k) in Passaggio(k).
- Indirizzo: GitHub Pages (burzino.github.io/imposteur), poi `imposteur.burzi.eu` con un CNAME su Aruba verso `burzino.github.io.`.
- Repository pubblico: https://github.com/burzino/imposteur (ramo `main`, remote `origin`). Hosting previsto: GitHub Pages, all'indirizzo burzino.github.io/imposteur.
- Il token di `gh` non ha lo scope `workflow`: prima di pubblicare `.github/workflows/` serve `gh auth refresh -s workflow`, da far eseguire all'utente.
- I percorsi locali stanno in `.claude/ambiente-locale.md` (in `.gitignore`); la storia è stata ripulita prima del primo push.
- Le segnalazioni nella PWA si esportano come file, perché adb non è disponibile.

## Decisioni PWA (2026-10-08, sessione di avvio)
- Utente: Screen Wake Lock in Distribuzione e Rivedi; nota di installazione iOS in "Come si gioca" (solo iOS Safari non installato); "Esporta segnalazioni" nelle impostazioni, visibile solo se ce ne sono.
- Sessione principale: rotte in hash (#/home…); colori dinamici assenti sul web; rotazione non azzera il ruolo (CA-W17); ritardo "Nascondi e passa" 600 ms come nel codice.
- Specifiche: §4.4.1, CA-54/55, CA-46 e la riga "Ripristino" di §4.3 citano ancora "Sono <nome>" e il tocco: da allineare alla pressione lunga.
- Mancano le icone PNG `web/public/icona-192.png` e `icona-512.png`.

## Ultimi passi (2026-10-08)
- Aggiunti: opzioni avanzate (fisarmonica, 6 opzioni), tasto Home in alto, ordine di parola, griglia di Rivedi.
- Collaudo su OnePlus 9 superato. Corretti il doppio tocco (ritardo di 600 ms) e il dialogo di segnalazione sopra la tastiera. Script di collaudo: `tools/collaudo/ui.py`.
- Restyling della distribuzione (commit 9c15180): "Tieni premuto per scoprire" (pressione lunga), carta che si gira, fila di avatar, overlay "Tutti pronti!". 150/150 test. In attesa del parere dell'utente.

## PWA: stato (2026-10-08 sera)
- Fatte le ondate 1-3: specifiche web, contratto TS v2.0 e UI v2.1, porting di game/data, 8 schermate Svelte, 277 test Vitest verdi, build verde, vitest 5 (0 vulnerabilità), icone ricavate da quella Android.
- Pubblicata su https://burzino.github.io/imposteur/ dal workflow `.github/workflows/pages.yml` (Pages con sorgente GitHub Actions, scope `workflow` concesso).
- Collaudo in Chrome headless (puppeteer-core, script e rapporto nello scratchpad della sessione): 8 flussi OK, console pulita, 360 px senza tagli.
- Agenti in parallelo: lo sviluppatore A aveva sovrascritto i test del tester; ora sviluppatore.md vieta di toccare i test.

- Rivedi: vale il CA-54 (utente, 2026-10-08): niente "Giocatore k di N" nel Passaggio, anche su Android (`mostraAvanzamento = false`). APK da reinstallare per il collaudo.

## Ondata del 2026-10-09: restyling 2026 e configurazione a passi
- Durata della pressione regolabile in Impostazioni (cursore 0-1000 ms, passo 50, predefinita 150; a 0 ms rivela al rilascio; CA-106..112, contratto v1.8/v2.3). Blocco anti doppio tocco 600 ms invariato. Firma release da `keystore.properties` (escluso da git, modello in `keystore.properties.esempio`): l'utente deve creare la chiave con keytool.
- BOM Compose 2026.09.00, AGP 9.1.1 (Kotlin integrato), Gradle 9.3.1, compileSdk 37. material3 resta 1.4.0: API Expressive internal/sperimentali, molle scritte a mano in `ui/theme/Movimento.kt`.
- Design unico in `docs/design.md` (agente `designer-ui`), bozza `docs/design/bozza.html`. Roboto Flex incorporato (Android res/font, 1,7 MB non ridotto; PWA @fontsource-variable latin/latin-ext).
- Configurazione in 4 passi: Giocatori (con "Impostori a sorpresa"), Opzioni, Categorie, Riepilogo; "Inizia" in barra nei passi 1-3. Logica in `game/Passi.kt` e `web/src/game/passi.ts` (contratto v1.7/v2.2), CA-99..105.
- Revisione Android/PWA in `docs/revisione-design.md`: chiusi tutti (A, M, B).
- Test: Android 198/198, PWA 345/345. APK con cursore installato sul OnePlus 9. Da verificare a occhio: allineamento tacche/maniglia del cursore Android (Slider stabile + Canvas).
- Collaudo headless (09-10): tema persistente, localStorage bloccato, rotazione, visibilitychange OK. Export segnalazioni non verificato (script instabile): resta nella scaletta del telefono.

## Passi del 2026-10-09 (sera)
- Nomi su una riga separata in Gioco e Rivela: gioco_inizia, rivela_impostore e rivela_impostori sono ora etichette senza segnaposto ("Parla per primo:", ecc.), con il nome sotto. Android e PWA.
- Workflow Pages: checkout@v7, setup-node@v7, configure-pages@v6, upload-pages-artifact@v5, deploy-pages@v5 (Node 24).
- Test: Android 198/198, PWA 345/345, build verdi.

## Ondata in corso (2026-10-09 sera): configurazione a 5 passi e distribuzione nell'ordine di parola
- Specifiche e contratto aggiornati (CA-113..122, contratto v1.9/v2.4): Giocatori (solo nomi, "+ Aggiungi giocatore", la x rimuove), Modalità (modalità, impostori, sorpresa), Opzioni, Categorie, Riepilogo. Distribuzione e Rivedi seguono l'ordine di parola (`giocatoreAlPasso(k)`).
- Fuoco dopo la rimozione di un campo: nessuno spostamento esplicito (decisione della sessione principale).
- Fatto: design.md (§3 Riga giocatore, §4.4 a 5 passi), codice Android e PWA, test. Android 231/231, PWA 377/377, build verdi. Avatar con nome vuoto: numero del giocatore. Da verificare sul telefono.

## Foto degli avatar (decisa il 2026-10-09, ondata successiva ai 5 passi)
- Facoltativa, salvata sul dispositivo e riusata nelle partite successive. Legata al NOME del giocatore (non alla posizione).
- Sorgente: galleria e fotocamera (Android: Photo Picker senza permessi, fotocamera con permesso al primo scatto; PWA: input file con capture).

## Dominio burzi.eu (2026-10-09, attivo)
- burzi.eu = pagina principale "Giochi di gruppo" (repository `burzino/burzino.github.io`, cartella locale `Z:urzino.github.io`, HTML statico con `CNAME` e `.nojekyll`). Imposteur su burzi.eu/imposteur/ (dominio ereditato, BASE_PATH resta /imposteur/); burzino.github.io/imposteur e www.burzi.eu rimandano lì.
- DNS Aruba: 4 A di GitHub su @, CNAME www → burzino.github.io, TXT `_github-pages-challenge-burzino`; record di posta e Aruba lasciati.
- Un nuovo gioco: repository con Pages attivo + carta in `index.html` della pagina principale.
- La PWA su burzi.eu ha un'origine nuova: installazione e salvataggi di burzino.github.io non si trasferiscono.

## Prossimi passi
1. Installare l APK aggiornato (telefono scollegato al momento della build). Screenshot mancanti della PWA: Gioco, Rivedi, Rivela, Impostazioni.
2. Collaudo su telefono: APK nuovo (restyling, 4 passi, 300 ms) e PWA secondo `docs/collaudo-pwa-telefono.md`. Su Android verificare medaglione Home, pesi 700/800 di Roboto Flex, "Inizia" sopra la tastiera con 20 giocatori.
3. HTTPS su burzi.eu: quando GitHub ha emesso il certificato, spuntare "Enforce HTTPS" nel repository burzino.github.io (lo fa l'utente).
4. Verificare a occhio, sul telefono, i nomi su una riga separata in Gioco e Rivela.

## Punti aperti
- Build release firmata: la chiave la crea l utente.
- material3 1.5 stabile: quando esce, passare alle API Expressive vere.
