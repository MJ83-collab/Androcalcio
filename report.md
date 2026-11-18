# Androcalcio – report (stato al passo 6)

## Stack
Kotlin 2.0.20, Jetpack Compose (Material 3), Room 2.6.1 (versione DB 4, ricreato da zero a ogni cambio schema), minSdk 26. Build in cloud con GitHub Actions (artifact `androcalcio-debug-apk`).

## Fatto
1. Grafica e menu: tema scuro del canvas, stemmi generati, Home con 9 voci, Rosa, Finanze.
2. Dati: 272 squadre (7 nazioni, 14 divisioni) da `assets/squadre.json`, 18 giocatori generati per squadra.
3. Partita: motore a eventi discreti + vista 2D, 7 moduli con bonus/malus.
4. Calendario andata/ritorno, classifica, risultati salvati.
5. Mercato: offerte, controfferte, vendite, budget e limiti di rosa.
6. Vivaio e scouting: leva giovanile con parere dell'allenatore, report scout con intervalli che si stringono, giudizio di adattamento alla rosa.

## Non fatto
- Contratti, ingaggi dinamici, crescita/invecchiamento dei giocatori (il potenziale dei giovani non viene conservato dopo la promozione).
- Staff completo, finanze con bilancio, editor dati, export/import JSON, salvataggio multiplo.
- Partite di campionato senza vista 2D; nessuna tattica in partita, infortuni, sostituzioni.
- Taratura del motore (media gol, % pareggi) non verificata; app mai provata da me su schermo.
- Font Sora/Manrope non inclusi.

## Prossimi passi consigliati
Editor dati con export/import, poi stagioni successive (età, crescita, contratti), poi staff e bilancio.
