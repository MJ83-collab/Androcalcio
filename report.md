# Androcalcio – report progetto Android (scaffold v0.1.0)

## Cosa contiene lo ZIP
- Progetto Gradle Kotlin DSL, modulo `app`, package `it.androcalcio`.
- Stack: Kotlin 2.0.20, Jetpack Compose (BOM 2024.09.00, Material 3), Room 2.6.1 via KSP, minSdk 26, targetSdk 35.
- Entità Room: `Team`, `Player`, `MatchResult` (da sezione 14.1 del documento; campi semplificati).
- DAO e database Room con seed automatico di 2 squadre e 8 giocatori fittizi al primo avvio.
- Tema scuro con i colori del canvas (sfondo #0A0E13, accent #B6FF3C, cyan #4FD1FF).
- Schermate: Squadre (lista e selezione), Rosa (giocatori della squadra selezionata), tre segnaposto (Partita, Mercato, Vivaio).

## Non ancora fatto
- Non compilato: in questo ambiente non ci sono Android SDK e la rete per Gradle è disattivata. Il primo build può quindi rivelare errori di versione o di import.
- Gradle wrapper (`gradlew`) non incluso: Android Studio lo genera all'apertura del progetto.
- Font Sora e Manrope non inclusi: si usa il font di sistema.
- Motore partita 2D, mercato, vivaio, staff, scouting, bilancio: solo segnaposto.
- Editor dati e salvataggio/esportazione JSON v2: non implementati.
- Database PC Calcio riutilizzato: non importato; i dati seed sono inventati.
- Il nome del package è `it.androcalcio`; per cambiarlo servono rinomina di cartelle e riferimenti.

## Come compilare l'APK
1. Apri la cartella `Androcalcio` con Android Studio (versione Ladybug o successiva).
2. Attendi il sync di Gradle; Android Studio scarica le dipendenze e crea il wrapper.
3. Menu Build → Build APK(s). L'APK esce in `app/build/outputs/apk/debug/`.

## Passi successivi consigliati
1. Verificare il primo build e correggere le versioni eventualmente incompatibili.
2. Implementare il motore partita a eventi discreti (sezione 14.4).
3. Aggiungere export/import JSON v2 per il salvataggio.
4. Portare nel progetto il database reale delle squadre.
