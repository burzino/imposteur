# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

App Android "Impostore" per giocare al gioco dell'impostore con un solo telefono che passa di mano in mano. Kotlin, Jetpack Compose (Material 3), modulo unico `app`, package `it.imposteur`. Rispondi in italiano; testi dell'interfaccia e parole di gioco sono in italiano.

## Comandi

Il JDK di sistema è Java 8: usa sempre quello di Android Studio (JDK 25, quindi Gradle 9.1 o superiore).

```bash
export JAVA_HOME="<ANDROID_STUDIO>/jbr"   # percorso reale in .claude/ambiente-locale.md (non versionato)
./gradlew :app:assembleDebug                 # APK in app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest             # test JVM (JUnit 4)
./gradlew :app:testDebugUnitTest --tests "it.imposteur.game.RegoleTest"            # una classe
./gradlew :app:testDebugUnitTest --tests "it.imposteur.game.RegoleTest.*CA-01*"    # un test
```

Build e test non si lanciano nella sessione principale: li esegue l'agente `esecutore-build`. L'emulatore non parte su questo PC (manca l'accelerazione hardware): il collaudo si fa su un telefono fisico collegato via USB.

## Architettura

- `game/`: logica pura, senza import `android.*`. Contiene regole e validazione, `GestorePartite` (estrazione della parola senza ripetizioni, con l'insieme delle parole usate; casualità tramite `Random` iniettato), `Partita` / `ContenutoRuolo` (ciò che vede ogni giocatore) e la macchina a stati `Distribuzione` (Passaggio k → Rivelazione k → Passaggio k+1 | Gioco, mai all'indietro).
- `data/`: parser di `assets/parole.json` e serializzazione JSON di configurazione e sessione, in Kotlin puro (kotlinx.serialization). Contiene anche i repository Android su un unico DataStore Preferences, con chiavi distinte per configurazione e sessione.
- `ui/`: `ImpostoreViewModel`, l'unica fonte di stato; un NavHost con le rotte home, regole, configurazione, distribuzione, gioco, rivela; una schermata per file.

Invarianti che attraversano più file:
- Un ruolo non deve mai restare visibile: ON_STOP, la ripresa e il ripristino trasformano Rivelazione(k) in Passaggio(k). La Distribuzione usa FLAG_SECURE e il tasto indietro chiede conferma.
- La sessione (partita in corso più parole usate) viene salvata a ogni cambio di stato. Al ripristino si scarta in silenzio se non è più coerente con `parole.json`.
- In modalità "Parola affine" civili e impostore ricevono lo stesso tipo di contenuto (`ParolaSegreta`): l'interfaccia non deve distinguerli.

## PWA (in costruzione)

`web/` conterrà la versione web: Svelte 5 + TypeScript + Vite + vite-plugin-pwa, test con Vitest. La logica in `web/src/game/` è il porting in TS puro di `game/`. Le parole si leggono da `app/src/main/assets/parole.json`, senza copie. I comandi (`npm ci`, `npm test`, `npm run build`, da `web/`) li esegue `esecutore-build`. Il piano è in `STATO.md`.

## Documenti

- `docs/specifiche.md`: specifiche e criteri di accettazione CA-xx; i nomi dei test citano il CA che verificano.
- `docs/contratto-api.md`: firme condivise tra logica, test e UI. Va cambiato prima del codice.
- `STATO.md`: stato del lavoro e punti aperti, per riprendere da una nuova sessione.
- `app/src/main/assets/parole.json`: categorie con coppie parola/affine (formato in specifiche §7).

Agenti di progetto in `.claude/agents/`: Sonnet: analista-funzionale, redattore-parole, sviluppatore, tester, revisore. Haiku: esecutore-build, collaudatore.
