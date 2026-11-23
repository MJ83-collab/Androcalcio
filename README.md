# Androcalcio

Gioco manageriale di calcio per Android, ispirato a PC Calcio 6.0. Kotlin 2.0, Jetpack Compose, Room (DB v24).

## Cosa c'è
- Campionati, coppe, europee, nazionali; partita dal vivo con tabellone TV e formazione con volti.
- Mercato, prestiti, scambi, agenti, scouting (anche nei vivai delle altre squadre), vivaio.
- Finanze: sponsor, stadio, strutture, assicurazione, **mutui bancari** con banche dai nomi comici, fair play finanziario.
- Società: disciplina, spogliatoio, storia del club, **analisi** (forma, serie, casa/trasferta, contratti, infermeria, consigli del vice).
- Opzioni: difficoltà, dimensione del testo, **risparmio batteria**.

## Compilare
- Si compila con GitHub Actions (`.github/workflows/build-apk.yml`): test del motore, APK debug e APK release ottimizzato (R8).
- In locale: `gradle testDebugUnitTest assembleDebug` (JDK 17, Android SDK 35).

## Struttura
- `app/src/main/java/it/androcalcio/engine` — regole di gioco pure (testate).
- `app/src/main/java/it/androcalcio/data` — Room, salvataggi, import/export.
- `app/src/main/java/it/androcalcio/ui` — schermate Compose.
- `report.md` — stato del lavoro e limiti noti.
