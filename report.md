# Androcalcio – report (stato al passo 12)

## Stack
Kotlin 2.0.20, Jetpack Compose (Material 3), Room 2.6.1 (versione DB 6, con migrazione 5→6 che conserva i dati), minSdk 26. Build in cloud con GitHub Actions (artifact `androcalcio-debug-apk`).

## Fatto
1. Grafica e menu: tema scuro del canvas, stemmi generati, Home con 9 voci, Rosa, Finanze.
2. Dati: 272 squadre (7 nazioni, 14 divisioni) da `assets/squadre.json`, 18 giocatori generati per squadra.
3. Partita: motore a eventi discreti + vista 2D, 7 moduli con bonus/malus.
4. Calendario andata/ritorno, classifica, risultati salvati.
5. Mercato: offerte, controfferte, vendite, budget e limiti di rosa.
6. Vivaio e scouting: leva giovanile con parere dell'allenatore, report scout con intervalli che si stringono, giudizio di adattamento alla rosa.
7. Editor dati, copia personalizzata/predefiniti all'avvio, export/import JSON.
8. Nuove stagioni: età, crescita verso il potenziale, ritiri a età fissa casuale 38-45, contratti (rinnovo), bilancio di fine stagione.
9. Fine stagione: tutti i campionati vengono completati, premi per piazzamento, 3 promozioni/retrocessioni tra divisioni vicine.
10. Staff (allenatore e preparatore della tua squadra):
    - Allenatore: moltiplicatore sulla forza di squadra in partita (da -10% a +10%) e fino a ±2 punti di crescita per i giocatori fino a 28 anni.
    - Preparatore: rischio di infortunio dopo ogni partita (da 2,5% a 7,5%); l'infortunato sta fuori 1-3 turni e non scende in campo.
    - Candidati casuali, ingaggio annuo pagato a fine stagione, contratto in anni: a scadenza lo staff lascia la squadra.
    - Il giudizio sull'abilità è sintetico (stessa scala del vivaio). Export/import JSON include lo staff.
11. Allenatore delle giovanili e scout assumibili: l'assunzione aggiorna l'abilità del vivaio e dello scout del club; a licenziamento o scadenza del contratto il club torna all'abilità 40 ("vuoto"). Il valore inserito a mano nell'editor viene sovrascritto dall'assunzione.

12. Stile PC Calcio: tema turchese con interruttore nella Home (salvato), barra in alto (squadra, stagione, titolo) e menu laterale a testo al posto della barra in basso. Tre temi (scuro, turchese, blu navy) scelti dalla Home. Non fatti: testata a banner con bandiera, grafico di progresso.

13. Statistiche: marcatori e portieri (gol subiti) del tuo campionato fino alla giornata scelta (DB versione 7, tabella gol; salvati solo con Prossima giornata / Fine stagione). Classifica per giornata con ▲ promozione e ▼ retrocessione.

## Non fatto
- Infortuni in partite simulate a fine stagione (`Chiudi stagione`): non vengono tirati. Solo le giornate giocate con "Prossima giornata" o "Fine stagione" li generano.
- Staff per le squadre AI: giocano sempre con effetto neutro.
- Clausole contrattuali, bilancio dettagliato.
- Editor dati con potenziale modificabile, salvataggi multipli.
- Partite di campionato senza vista 2D; nessuna tattica in partita, sostituzioni, forma fisica.
- Taratura del motore (media gol, % pareggi) non verificata; app mai provata da me su schermo.
- Font Sora/Manrope non inclusi.

## Prossimi passi consigliati
Verifica su schermo di staff e infortuni (build in cloud, poi test con Marco). Poi staff per scout e giovanili, oppure allenamento e forma fisica.
