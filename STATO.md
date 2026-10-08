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
analista-funzionale, redattore-parole, sviluppatore, tester, esecutore-build, collaudatore.
Vengono caricati solo all'avvio di una nuova sessione. L'ondata 1 ha usato agenti generici che leggevano la definizione del proprio ruolo.

## Ondate
1. [specifiche fatte: 36 CA, 8 decisioni da confermare in §10] Specifiche (`docs/specifiche.md`) e parole (`parole.json`).
2. [fatto; prima build fallita per compileSdk 35, portato a 36, da ricompilare a fine ondata 3] Scheletro del progetto Gradle con wrapper, poi build della app vuota.
3. [fatto] Logica di gioco (`game`) e caricamento delle parole (`data`), più test unitari.
4. [fatto; build verde, 96/96 test, commit b5d0a03] Salvataggio/ripresa sessione (decisione utente 2026-10-06; anche parole usate persistenti): specifiche (analista), logica game/data (sviluppatore); poi test + UI ripresa. Contratto v1.1 in docs/contratto-api.md.
5. [fatto] Revisione statica della UI (docs/revisione-ui.md): corretti D1-D8 e aggiunto FLAG_SECURE in Distribuzione. Build verde, 96/96.
6. [da fare, serve l utente] Collaudo su telefono fisico via USB. L emulatore non parte: manca l accelerazione hardware (Hyper-V/WHPX non attivi, servono i privilegi di amministratore). Verificare anche: doppio tocco, am kill (process death), Recents, back predittivo.

## Punti aperti
- Collaudo sul telefono fisico da fare con l utente (ondata 6).
- Rivedere a mano la qualità delle coppie in parole.json (336 coppie, controllo a campione OK).
- Specifiche §10: decisioni dell analista da confermare con l utente.
