# Androcalcio – report (stato al 20/11/2026, sera)

## Dove siamo
- Branch `feat/pc7-meccaniche` (repo `MJ83-collab/Androcalcio`), ultimo commit `0e5ac1c`.
- Build APK e test unitari: CI verde sull'ultimo commit (run `44783330476`).
- Non ho mai avviato l'app sul telefono: tutte le verifiche sono compilazione e test in CI.

## Fatto in questa parte
- **Avvio:** scelta nazione, campionato e squadra. Toccando una squadra si apre la conferma con nome e cognome dell'allenatore. Poi il messaggio del presidente, con il pulsante "Inizia la stagione" che porta al calendario.
- **Calendario:** "Prossima giornata" chiede "Gioca in 2D" o "Simula" solo quando la tua squadra gioca in quella giornata. Ho tolto "Fine stagione", che simulava anche la tua partita.
- **Partita dal vivo:** il pulsante "Fine" è ora "Termina partita".
- **Fine stagione:** finestra con le classifiche della tua nazione, promozioni, retrocessioni ed esiti europei. Pulsanti "Salva ed esci" (primo slot libero, o slot 1 se sono pieni, poi chiude l'app), "Nuovo campionato" (stessi club e giocatori, si riparte da squadra e allenatore) e "Chiudi".
- **Retrocessioni e promozioni:** 4 dalla B alla C; 4 dalla C alla B (le 3 vincitrici di girone più la migliore seconda). Serie C e divisioni minori non retrocedono.
- **Coppe europee (prima divisione di ogni nazione):**
  - Italia, Germania, Francia: 4 posti in Champions.
  - Spagna e Inghilterra: 5 posti in Champions (posto extra per ranking UEFA).
  - In tutti i casi: 2 in Europa League (il secondo alla vincitrice di coppa nazionale, se non già qualificata) e 1 in Conference League.
- **Copa Sudamericana:** Brasile e Argentina, 6 posti ciascuno; approssimazione con i posti dal 7° al 12° della prima divisione.

## Decisioni prese
- Le coppe europee valgono solo per le prime divisioni delle 5 nazioni europee presenti nel gioco.
- La Sudamericana è un'approssimazione: la regola reale usa la tabla anuale in Argentina e la regola di selezione del Brasile. Hai detto che va bene così.
- Alla chiusura della stagione le partite delle altre leghe vengono simulate in blocco. La tua squadra non viene mai simulata senza di te. Hai detto che va bene così.

## Da verificare a mano sul telefono
- Avvio: conferma nome e cognome, messaggio del presidente, "Inizia la stagione".
- Ogni giornata con partita: scelta tra "Gioca in 2D" e "Simula".
- Partita in 2D: "Termina partita", poi "Torna al calendario".
- Fine stagione: classifiche, esiti, "Salva ed esci", "Nuovo campionato".
- Amichevoli di precampionato dal Calendario (riuso della schermata esistente, nessun passo dedicato).

## Non fatto
- Libertadores (non richiesta).
- Regola reale per la Sudamericana (vedi sopra).
- Conteggio del posto europeo extra legato alla finale di Europa League: non simulato.
- Verifica dell'app su schermo: da fare dal tuo telefono.

## Regole di lavoro con Marco
Italiano; risposte precise; chiedere quando qualcosa non è chiaro; confermare ogni modifica salvo autonomia concessa per la sessione; report.md ogni 5 domande circa; poco consumo di token; a fine lavoro dire se bastava un modello inferiore. Solo nomi fittizi, nessun asset originale di PC Calcio.
