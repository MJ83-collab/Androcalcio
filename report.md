# Report Androcalcio — sessione del 22/11/2026

Ramo: `feat/pc7-meccaniche` (PR #2, non unita). Test del motore in locale: **45/45 ok** (prima 36).

## Bug corretti
- **Crash migrazioni DB**: nelle migrazioni 13→14, 14→15, 16→17 la tabella era chiamata `Player` invece di `players`.
- **Prezzo mercato incoerente**: trattativa e risposta del club usavano formule diverse. Ora un'unica `Market.richiestaClub`.
- **Serie C**: i 3 gironi avevano livelli diversi in rose e stadi. Ora `RoseFisse.livelli` li tratta come un solo livello (CSV congelato invariato, verificato dal test).
- **Confronti tra campionati**: economia, vendite e rivali in PartitaScreen mischiavano squadre di nazioni diverse. Ora filtrano nazione + campionato.
- **Soldi dal nulla in vendita**: `sell()` ora richiede un acquirente della stessa divisione con budget sufficiente e lo scala.
- **Clausole**: pulizia O(n²) resa lineare.
- **Salvataggio/import JSON**: fiducia, tifosi, reputazione, clima, livelli strutture, stipendio allenatore, esonero e sponsor ora vengono esportati e ripristinati.
- **Nuovo campionato**: non eredita più stato (fiducia, sponsor, esonero, offerte) dalla carriera precedente.

## Economia
Estratta in `engine/Economia.kt` (funzioni pure, testate). Il PDF partiva dall'idea che i club fossero in bancarotta: dal codice risultava l'opposto (entrate troppo generose). Ricalibrati: quota incasso stadio 55%, riempimento 45–90% in base alla forza, premi e diritti TV per divisione, prezzi biglietto per divisione/nazione. **Da verificare giocando**: i numeri vanno provati su una partita vera.

## Test aggiunti
EconomiaTest (monotonia premi, range, prezzi, incasso) e FixturesTest (andata/ritorno, nessun doppio impegno, casi limite).

## CI
Il workflow ora parte anche su push di `feat/**` e sulle pull request, quindi esegue `testDebugUnitTest` e `assembleDebug`. Aggiunto `.gitignore` per `app/build/`.

## Non verificato
- L'APK **non è stato compilato** qui (niente Android SDK): le modifiche a ViewModel, UI e Room sono state controllate solo leggendo il codice. Serve la build della CI.
- Non ho potuto lanciare il workflow da GitHub (token non valido).
- I salvataggi esistenti mantengono i vecchi dati degli stadi di Serie C: **iniziare una NUOVA partita**.
- `Insurance.ROUNDS=38` fisso; `wipe` non svuota l'albo; `androcalcio.zip` in radice; README minimale.

## Idee del PDF non implementate
Prestiti, oneri gestionali, premi coppa, bonus vivaio.

## Domande per te
1. Mi mandi l'input grafico?
2. Tengo la ricalibrazione dell'economia dopo una prova sull'APK?

Modello: per l'implementazione basta quello di default (Sonnet) o anche Haiku; Opus serve solo per la taratura del bilanciamento.
