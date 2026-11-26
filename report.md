# Androcalcio — report (aggiornato 26/11/2026)

Repo: https://github.com/MJ83-collab/Androcalcio — `main` = v0.8.0 (tattica FM, squadre storiche, forza reale dei club, forza avversario nelle amichevoli). Ramo precedente `feat/tattica-fm` (da `feat/fine-partita`: v0.6.0 + blocchi 1–6 + tablet + stadio, maglie, coppe europee, stile PC Calcio 7).
Kotlin 2.0 / Compose / Room (DB v24, nessuna migrazione; dati nuovi in tabella Stato). Nessun SDK Android in locale: compila e prova solo GitHub Actions.
Build verde sull'ultimo commit. ⚠️ Nulla di questa sessione è stato provato sul telefono.

## COMANDO PER RIPARTIRE (da incollare nella nuova chat insieme a questo file)
> Riparti da questo report.md. Clona https://github.com/MJ83-collab/Androcalcio, ramo `feat/psicologo` (contiene anche `feat/profondita-fm27`; in prova da Marco; `main` = v0.8.0). Leggi "Cosa manca" e "Decisioni aperte". Prima di tutto ricontrolla la repo, cerca bug e correggi quelli certi; per ogni dubbio o decisione chiedimi sempre. Tienimi aggiornato con "sono al N di M lavori". A fine lavoro aggiorna questo report.md rispettando le regole permanenti.

## Sessione 26/11 sera: tifoserie e rivalità (build verde, da provare)
- `engine/Tifoserie.kt`: curva con nome e passione per club; rivalità nuove dal punteggio degli incontri (soglia 30); rivalità storiche fisse.
- Ogni partita tua: la curva di casa prepara una coreografia in automatico (tipo, qualità); morale alla squadra di casa; contro rivale la curva ospite contesta; pirotecniche in derby possono costare una multa. Esito in posta (Dirigenza).
- APERTO: non c'è ancora una schermata dedicata alle tifoserie; solo i messaggi in posta.
- APERTO: Marco ha segnalato che l'età modificata in editor torna diversa nel gioco: non ancora risolto (vedi sotto).

## Sessione 26/11 pomeriggio: correzioni editor (build verde, da provare)
- Overall ricalcolato dai parametri salvati (se non digiti a mano il Valore). Valori mentali non più resettati.
- Ruoli 2-4: codice libero (1-4 lettere) con suggerimenti. APERTO: numero illimitato di ruoli richiede cambio database (sec5...), 58 punti da adattare. Decisione di Marco pendente.

## Sessione 26/11 mattina: psicologo ed editor completo (ramo feat/psicologo, build verde, NON provata sul telefono)
Stato: sono al 2 di 2 lavori. Merge su `main` solo dopo la prova di Marco.
- **Editor giocatore a schede:** Anagrafica, Fisico, Tecnica, Mentale, Contratto, Ruoli. Ogni caratteristica ha sigla, nome, descrizione e valore (VE RE AG FO condizione, QU PA DR EN FI TI RI GM, morale e 6 valori mentali, contratto, numero, turni di infortunio).
- **Mentalità (`engine/Mente.kt`):** 6 valori (determinazione, leadership, freddezza, professionalità, autocontrollo, spirito di squadra). Base fissa da nome e carattere; modifiche salvate in Stato `mente`. Effetti: freddezza attenua il calo di morale nelle sconfitte, determinazione aiuta a rialzarsi; affiatamento della squadra muove il clima ogni mese.
- **Psicologo (`engine/Psicologo.kt`, ruolo staff `PSI`):** nuova scheda (colonna staff in Home, Stampa, posta). Vede problemi singoli (morale, panchina, ansia, ingaggio), attriti tra compagni e spogliatoio diviso: quanti ne vede dipende dall'abilità. Sedute a pagamento con probabilità di riuscita, attesa 7 giorni; lavoro individuale su un valore mentale (attesa 14 giorni). Pareri su tuoi, altri club (serve conoscenza scout ≥50%) e vivai. Report mensile in posta + "REPORT ORA". Recupero morale a ogni giornata in base all'abilità.
- **Compatibilità:** il vecchio psicologo a livelli diventa automaticamente un membro dello staff. Le squadre AI ricevono uno psicologo.
- **Energia (EN):** approvato da Marco: calo nelle amichevoli e nel ritiro e recupero nei riposi dipendono da resistenza ed età (`CalendarioGiorni.cala`).
- Modello: Sonnet basta.

## Sessione 26/11: profondità FM27 + immediatezza PC Calcio 7 (ramo feat/profondita-fm27, 4 blocchi, NON provata sul telefono)
Stato: sono al 4 di 4 blocchi. Merge su `main` solo dopo la prova di Marco.
- **Blocco 1:** preparatori leggibili (testo scuro su carta, motivo per ogni giocatore); rosa a fasce TITOLARI / PANCHINA / INFERMERIA; sigle ruolo colorate (POR, DC, TS…); voto medio in Statistiche (colonna ordinabile) e nella scheda giocatore; banner notizie lampeggiante in alto finché non leggi; posta con "DA LEGGERE" e colori per categoria; cambi in partita a due colonne con "CAMBIO CONSIGLIATO" e ripresa automatica; Pausa ❚❚ diversa da Riprendi ▶.
- **Blocco 2:** in Tattica, tocco sul giocatore = alternative compatibili per quel ruolo, scambio con un tocco; conferenze stampa su fatti veri (ultimo risultato, voti, prossimo avversario, classifica, mercato, infortuni) e mai su partite non giocate; nomi vari per 19 nazioni (`engine/Nomi.kt`, rosa predefinita rinominata in modo fisso).
- **Blocco 3:** potenziale nascosto: lo stima il vice (tuoi), l'allenatore giovanili (tuo vivaio), gli scout (altri) con conoscenza % e forbice che si stringe; carattere e affinità con la squadra; ogni vivaio ha specialità e qualità diverse; "Osserva €" sul vivaio di un club con rapporto in posta (`engine/Conoscenza.kt`).
- **Blocco 4:** fragilità nascosta per giocatore (pesa su chi si infortuna); test a pagamento dal fisio della prevenzione (costo e precisione dalla sua abilità); operazione per gli infortunati con 2+ turni (costo, turni risparmiati dal fisio del recupero, rischio ricaduta, una per stagione); messaggio di infortunio porta a Fisioterapia (`engine/Fisio.kt`, Stato `test_fisio_<id>`, `operato_<id>`).
- **Note:** i nomi nuovi valgono solo per le nuove carriere. Il potenziale non è più visibile senza staff.
- **Da fare da Marco:** provare l'APK del ramo sul telefono e dire cosa ritoccare (screenshot).
- **Decisioni aperte:** nessuna.
- Modello: Sonnet basta.

## Release v0.8.0 (25/11/2026 sera, autorizzata da Marco prima della prova sul telefono)
- Versione 0.8.0 (versionCode 8): include tattica FM, squadre storiche, forza reale dei club, valori reali, ritiri/ricambio, CONTINUA, schede staff.
- **Amichevoli:** nuova colonna FORZA (media dei migliori 11) per ogni squadra, colorata rispetto alla tua (verde = più debole, rosso = più forte, arancio = pari). Dato da `vm.teamInfo`.
- **Finali europee:** verificato nel codice: si giocano dal vivo solo se c'è la tua squadra e scegli tu (`playNextRoundLive`); le altre sono simulate.
- **Decisioni aperte:** nessuna. **Da fare da te:** provare l'APK su telefono e tablet (esportare i salvataggi e disinstallare la vecchia versione); dire quali schermate ritoccare (servono screenshot).
- Modello: Sonnet basta.

## Regole permanenti (valgono per ogni report futuro)
1. Ricontrollare la repo e cercare bug a ogni ripartenza; correggere i bug certi, chiedere per i dubbi.
2. Le decisioni di progetto le prende Marco: in caso di dubbio chiedere sempre.
3. Aggiornare Marco con lo stato dei compiti ("sono al N di M"), sia durante il lavoro sia nel report.
4. Italiano educato e preciso, poche parole; dire se basta un modello più piccolo (Sonnet basta per UI e merge; Opus solo per nuove tarature).
5. Dopo 5 domande su un argomento: nuovo report.md e nuova chat. Il report deve sempre avere: comando di ripartenza, cosa manca, decisioni aperte.
6. Il merge su `main` / release v0.7.0 solo dopo la prova di Marco sul telefono (prima: esportare i salvataggi, disinstallare la vecchia versione una volta).

## Sessione 25/11 sera: Tattica in stile FM27 (ramo feat/tattica-fm, build verde con test, NON provata sul telefono)
Scelte di Marco: tutto, motore compreso; due ruoli per giocatore (con e senza palla); 11 stili con i nomi dell'articolo FM Religion.
- **`engine/Stili.kt`:** `StileTattico` (11 stili con intensità, complessità, pregi/rischi, istruzioni), `PianoTattico` (passaggi, creatività, transizione, costruzione portiere, frequenza pressing, contrasti, contropressing, blocco portiere, ritmo, ruoli senza palla per id), `Effetti`, `RuoliFm` (stelle del ruolo e attributi chiave). Pressing, linea, ampiezza, fasce e piazzati restano le impostazioni di prima: lo stile le scrive (`vm.applicaStile`).
- **Motore:** `LiveMatch`/`MatchEngine.simulate` hanno `homePiano`/`awayPiano` opzionali (solo la tua squadra; le AI non cambiano). Effetti piccoli su possesso, attacco, difesa e fatica; efficacia dello stile ±4% in base alla rosa (`idoneita`); ruolo senza palla più congeniale: difesa fino a ±8%. Ritmo iniziale e contropiede iniziale vengono dallo stile.
- **Salvataggio:** SharedPreferences, chiave `piano_tattico` (nessuna migrazione DB).
- **UI `ui/TatticaFm.kt`:** campo con viste Combinata / In possesso / Non in possesso, scelta ruolo con stelle, ruoli da imparare, attributi chiave e compito, istruzioni di squadra a due fasi (tocco = cambia valore, punto giallo = modificato), scelta stile con "Consigliato" ai 3 più adatti. In `AndrocalcioApp` la Tattica ha un interruttore Stile FM / Classica (la vecchia schermata è intatta).
- **Test:** `PianoTatticoTest` (salvataggio, effetti, gol medi 1,5-4,2 per ogni stile). Tutti verdi.
- Modello: Sonnet basta per UI e collegamenti; Opus solo se si vuole tarare a fondo gli effetti.

### Seconda parte (25/11 sera, Opus, build verde, NON provata sul telefono)
Decisioni di Marco: 1) squadre AI con gli 11 stili, in base all'allenatore e alle 4 squadre più storiche e forti; 2) effetti alzati di poco; 3) Tiki-Taka verticale ok così; 4) istruzioni per giocatore e ruoli per fase in partita, subito.
- **`StiliAi`:** le 4 squadre più storiche e forti di ogni nazione (stadio più grande, poi budget) scelgono tra Controllo del possesso, Tiki-Taka, Tiki-Taka verticale, Gegenpress; le altre tra gli stili d'attacco o di difesa secondo l'allenatore. Vince lo stile più adatto alla rosa. Per le AI pressing, linea, ampiezza e fasce vengono dallo stile.
- **Effetti alzati** di circa un quarto; ruolo senza palla fino a ±10%.
- **`IstrGiocatore`:** 11 istruzioni per giocatore (tiro, dribbling, passaggi, sortite, pressing, contrasti), effetto totale massimo ±5%; "Tira di più/meno" cambia chi tira. Nella scelta ruolo; punto "•" sul campo.
- **Ruoli per fase in partita:** nella partita dal vivo e nella vista "Non in possesso" i giocatori vanno verso il ruolo senza palla (`PostoRuolo`).
- **Tecnico:** `closeSeason` di nuovo al limite ("Method too large"): partita spostata in `simChiusura`. Ogni aggiunta futura lì va in funzioni separate.

- **Squadre storiche (25/11 sera):** elenco fisso `StiliAi.STORICHE` per le 14 nazioni, preso dagli albi d'oro reali e associato ai nomi del gioco. Se una squadra è rinominata nell'editor, si completa con stadio e budget.

- **Forza delle squadre reali (25/11 sera):** `strumenti/forza_squadre.py` adatta gli overall di `rose_fisse.csv` (massime serie con rose fisse) al valore di mercato reale della rosa (Transfermarkt via footystats.org e footballwhispers.com). La media di ogni campionato non cambia: cambia l'ordine e la distanza tra squadre (circa ±8 punti, estremi limitati). Nei campionati con rose generate (Olanda, Portogallo, Belgio, Scozia, Turchia, Grecia, Giappone) le squadre sono riordinate nei JSON per valore reale (la forza dipende dalla posizione). Solo carriere nuove. Stime a mano dove la fonte era incompleta: Boca, Racing, Independiente, Estudiantes, Talleres, San Lorenzo, Argentinos, Anderlecht, Bruges, Gent, Genk, Yokohama, Kobe, Nagoya. Squadre non trovate (es. Sampdoria, Wolfsburg, Burnley): 25° percentile del campionato.

### Decisioni aperte (Tattica FM)
1. Squadre storiche: **confermate da Marco il 25/11** (Napoli 4ª italiana; Saint-Étienne, Schalke, Amburgo e Norimberga escluse perché in seconda divisione; una sola squadra per Istanbul e Atene).
2. Da provare sul telefono: leggibilità del campo, scelta ruolo, istruzioni, movimento senza palla in partita, equilibrio dei campionati con le AI a stili.

## Stato dei compiti (24/11 sera)
Fatti nel codice, build verde, da provare sul telefono: stile marmo ovunque, icone staff, centro sportivo, movimento giocatori per fotogramma, ruoli FD/FS/AD/AS, allenamento ruoli/piede/Cinico, cronaca sincronizzata, importazione JSON con id, lettura giocatori a pagine, correzioni dei bug sotto.

## Cosa manca
- **Prova sul telefono** di tutto il ramo `feat/stadio` (x1 fluido? palla portata/passata credibile? Cinico bilanciato? coppe a metà settimana? moduli con ali? icone staff leggibili?).
- **Ritocchi grafici** dopo la prova (punto 7) e **avatar** (punto 8): decide Marco.
- **Merge su `main` e release v0.7.0**: solo dopo il via di Marco dopo la prova.
- **Bug da tenere d'occhio:** crash "Couldn't read row 1444 … CursorWindow" nella prova su emulatore (visto 2 volte, poi corretto con letture a pagine). Se ricompare, cercare un'altra lettura grande.

## Decisioni (aggiornate 25/11 pomeriggio)
Prese da Marco il 25/11:
1. Base rigori 0,64: **confermata**.
2. Cinico nelle serie simulate: **fatto** (vedi sotto).
3. "In bilico" conta anche i punteggi larghi: **confermato, nessuna modifica** (oggi `VANTAGGIO_MAX_BILICO` conta anche i pari e i sotto di molti gol).
5. Bonus analista (+1 affinità): **fatto**.
6. D con FD mai in linea C: **lasciato com'è** (limite noto).
7. Scala stipendi staff sulla media della TUA rosa: **confermata**.
8. Messaggi: report mensile, staff, sponsor: **fatto**.
**Ancora aperte:**
- Salvataggi vecchi: le coppe già calendarizzate "recuperano" i turni.
- Stelle intere in vivaio/scouting giovani e maglie.

## Release v0.7.0 (25/11/2026, autorizzata da Marco a voce prima della prova sul telefono)
- Versione 0.7.0 (versionCode 7), merge di `feat/stadio` su `main` (lineare, `main` era ferma a v0.6.0). Il workflow `release.yml` crea la release `v0.7.0` con un solo APK.
- ⚠️ Prima installazione: esportare i salvataggi e disinstallare la vecchia versione una volta (chiave di firma nuova).
- Mezze stelle (0,5) ovunque: vivaio, scouting giovani, maglie, Rosa, Mercato, scheda giocatore (`Stelle`, `ScoutingGiovani.mezzeStelle`, `Maglie.mezzeStelle`). Avatar: restano disegnati in codice (deciso). Coppe già calendarizzate nei vecchi salvataggi: continuano a recuperare i turni (deciso).
- **Editor giocatore:** nazionale a tendina, piede (destro/mancino/ambidestro), posizione preferita per ruolo, ruoli affini 2-4 con affinità %. Corretto: "Aggiungi giocatore" metteva "ITA" invece del nome della nazione. Esportazione/importazione e "Usa come predefinito" salvavano già questi campi.
- Ritocchi grafici generici: non fatti (senza screenshot non so quali schermate sistemare): dire quali.

## Sessione 25/11 pomeriggio (2): decisioni attuate (ramo feat/stadio, commit 00b0aa0, build verde, non provata sul telefono)
- **Controllo repo:** nessun bug certo trovato (letture grandi già a pagine, niente `!!` a rischio). Controllo non esaustivo: lettura mirata del motore.
- **Cinico nei rigori simulati:** `Cup.finalize(m, all, rng, cinici)`; `cinici` = per squadra, quanti Cinici tra i 5 rigoristi (migliori per `fi`, portiere escluso, `AndrocalcioViewModel.ciniciRigori`). Ognuno tira con +0,04 (75% → 79%). Usato da `finalizeKey` e `avanzaEuropa`. La finale dal vivo usa già i nomi. Test `CinicoRigoriSimulatiTest`.
- **Analista video:** dopo ogni partita analizzata, chi ha giocato guadagna +1 di affinità a ogni ruolo secondario sotto 100 (`AnalistaPartita.confidenza`, in `salvaAnalisi`). Test `AnalistaConfidenzaTest`.
- **Messaggi:** nuovi avvisi per contratto staff in scadenza, sponsor in scadenza (apre Marketing), report mensile del preparatore capo (apre Rosa). `Posta.Snapshot` ha tre campi nuovi con valore predefinito. Test in `PostaTest`.
- Modello: Sonnet basta.

- **Rigori con `ri` (scelta di Claude su delega di Marco, 25/11):** probabilità di segnare = `0,64 + 0,20 × ri/99` (prima `fi`). Anche i rigoristi si ordinano per `ri` (portiere ultimo). Motivo: `ri` ha meno scarto tra i ruoli (A +2, C -5, D -12 sul livello, contro fi A +8, C -8, D -22), quindi un difensore o centrocampista bravo dagli 11 metri non è più penalizzato. Stima: conversione quasi invariata (circa -1 punto, resta nel 75-80%); il test `CinicoTaraturaTest` la verifica (74-83%). Costante rinominata `RIGORE_RI`.

## Sessione 25/11 mattina: test stagioni, ritiri e ricambio dal vivaio (ramo feat/stadio, build verde, non provata sul telefono)
- **Test `StagioniDiFilaTest`:** tre stagioni in memoria per le 11 nazioni europee: campionati, coppa nazionale, Supercoppa, qualificate europee dalla stagione prima, gironi e turni delle tre coppe, nessuna squadra due volte nello stesso turno. Non copre il database vero (`closeSeason`).
- **Ritiri a fine stagione (`closeSeason`):** per età come prima (38-45). Nuovo: 33+ anni con stop di 20+ turni a fine stagione. In prima divisione, nel 50% dei casi scende per 1-2 anni in una squadra di categoria inferiore della stessa nazione (stipendio un terzo, chiave `declino`) e poi si ritira alla scadenza; altrimenti si ritira subito. Nelle nazioni con una sola serie si ritira subito.
- **Ricambio:** ogni ritirato è sostituito da un giovane (17-19 anni) dello stesso ruolo e della stessa squadra, con nome e caratteristiche casuali e **lo stesso ID**, quindi il database non cresce. Il ricambio conta nel numero dei 16 minimi. Test `RicambioGiovaniTest`.
- **Storico azzerato per l'ID riusato** (`dimenticaGiocatori`): statistiche, gol, convocazioni e rosa nazionale, scouting, clausole, capitano/vice, preferiti, squalifiche e ammonizioni, allenamento individuale, apparizioni, squadra B, voti, tratti (Cinico). Nomi e premi dell'albo restano come testo.
- **Ordine delle partite in `closeSeason`:** dentro ogni competizione l'ordine per giornata coincide con quello dei turni; le coppe si giocano dopo l'intero campionato. Lasciato così: nulla nei risultati dipende da questo ordine.
- **Tecnico:** `closeSeason` era arrivato al limite di dimensione del metodo (errore "Method too large"). Spostati in funzioni a parte `pagaClausole` e `rinnovaRosa`. Qualsiasi aggiunta futura va in funzioni separate.
- **Da provare sul telefono:** chiudere una stagione e controllare ritiri, ricambi e la scheda dei nuovi giovani.

- **Giornata (Calendario):** tolta la voce Mercato dalla scheda "Cosa fai oggi?"; aggiunta **Rifinitura** (condizione −1, preparazione +1; 30% di probabilità di +1 a passaggio/dribbling/tiro fino a 90; +1 di affinità a ogni ruolo secondario sotto 100). I vecchi "Mercato" nel diario vengono ignorati.
- **Stelle:** nuovo componente `ui/components/Stelle.kt`, da 1 a 5 stelle con mezza stella disegnata (non più il pallino ◐). Usato per staff (lista, candidati, dettaglio) e media squadra (Home e Avversario). Restano a stelle intere vivaio/scouting giovani e bellezza maglie.
- **Staff:** ogni annuncio dà **minimo 3 risposte** (fino a 5). Stipendi in scala con la categoria (`Staffing.scalaCategoria` dalla forza media della rosa: A ~0,9-1, B ~0,6, C ~0,4) per candidati, risposte, rinnovi e staff IA. **Corso di formazione** pagato dal club (dettaglio staff, una volta a stagione per persona: +3/+2/+1 abilità, costo in scala con la categoria). Test `StaffRifinituraTest`.
- **Da decidere/provare:** la scala usa la media della TUA rosa, non la media del campionato; gli staff già assunti mantengono lo stipendio fino al rinnovo.

## Sessione 24/11 notte (12): punti 4, 6, 3, 5 (ramo feat/stadio, build verde, non provata sul telefono)
- **Punto 4 – schemi con ruoli:** `Formation.posti` = 10 posti per modulo (TS, DC, FS, FD, AS, AD...). Nuovi moduli in fondo all'enum (ordinal salvato): 4-3-3 ali, 4-2-3-1 ali, 3-4-3 ali. Scelta automatica `Lineup.scegliPerPosti` (con `AFFINITA_MIN=100`, fuori posto x0,95), panchina `coachPickConPanchina`; ordine in campo `Lineup.perPosti`. Nel motore mai id (nei test sono 0): solo riferimenti. Test `PostiModuloTest`.
- **Punto 6 – calendario a turni** (`engine/Turni.kt`): campionato chiave 2*giornata, coppe a metà settimana (mercoledì) tra le giornate; coppa nazionale e coppe europee senza sovrapposizioni e mai nei turni delle nazionali. `runRounds`, partita dal vivo e Calendario usano le chiavi. Test `TurniTest`.
- **Punto 3 – palla 2D:** `PallonePista` (`ui/Manga.kt`): la palla sta ai piedi di un giocatore che la porta, passa al compagno vicino alla tappa (verso la sua posizione attuale), tiro libero. `rememberPalla` dà comandi `passa/porta/tira`; MangaPitch riceve `sim` (nullo nel Replay, che resta come prima).
- **Punto 5 – Cinico** (Opus, ramo `feat/cinico` già unito): peso tiro 1,15 (+12,2% gol del Cinico, gol totali +0,5%); rigori +0,04; finali +10% dal 75', +20% dall'83'; serie dal vivo con nomi dei tiratori (`LiveMatch.rigoristi`). Test `CinicoTaraturaTest`.
- Modello: Sonnet basta per tutto tranne la taratura del punto 5 (Opus).

## Sessione 24/11 notte (11): controllo repo
- **Bug corretto:** `allPlayers` e `teamInfo` (ViewModel) leggevano ancora tutti i giocatori in un colpo con `allPlayersFlow()`, senza pagine né nuovo tentativo. Ora usano `osserva("players") { dao.allPlayers() }` (pagine da 500 + un nuovo tentativo). Probabile causa del crash CursorWindow riga 1444. Non compilato in locale: verifica con la build su GitHub Actions.
- Altre letture complete (`allStats`, `youths`, `staff`) sono piccole: lasciate.

## Sessione 24/11 sera (10): staff, movimento, id, bug
- **Staff (Home):** colonna con 8 tessere (ALL lavagna, ASS cartellina, GIO figura con stella, PRE bilanciere, FIS croce, SCO lente, GIA erba, ANA schermo con grafico), sigla sotto ciascuna.
- **2D:** ogni giocatore ha una posizione propria che si aggiorna a ogni fotogramma (accelerazione e velocità massima, `Manga.kt`).
- **Stile:** sfondo marmo (`fondoApp`) anche in Nuova partita, partita dal vivo, fine partita, fine stagione.
- **Editor, importazione JSON:** mantiene gli id di squadre, giocatori e staff del file (se validi e non doppi; altrimenti li assegna il database). L'esportazione li scriveva già.
- **Bug corretti nel controllo della repo:** (1) i tratti "Cinico" acquisiti e gli allenamenti individuali restavano in memoria dopo "nuova carriera" o importazione e non finivano nei salvataggi: ora "tratti" è tra le chiavi salvate e la memoria si azzera; (2) grafico andamento in Società andava in errore senza giornate giocate; (3) Nuova partita andava in errore se la lista squadre era vuota; (4) lettura di tutti i giocatori a pagine.

## Sessione 24/11 sera (4): tarature, maglie e merchandising (ramo feat/stadio, non provato sul telefono)
- **Premi dei campionati nuovi** (`Economia.PREMI_ESTERI`): premio della prima 1,6 M, diritti TV base Olanda 9, Portogallo 9, Belgio 9,5, Turchia 6,5, Grecia 8,5, Scozia 12 M. Motivo: ingaggi minimi alti e stadi piccoli. Replica Python (40 estrazioni): mediana +3..+4 M, peggiore tipica circa -1,5 M. Prima la Scozia aveva mediana +1,3 M e peggiore -4,8 M. Test: `EconomiaTest.campionatiNuoviPiuPiatti`.
- **Coppa dei Campioni più bassa:** partecipazione 3 M, vittoria nel girone 0,6 M, pareggio 0,2 M, turni 1/1,5/2,5/3,5 M, titolo 6 M. Vincitrice circa 21 M (prima 29 M). Coppa Europa e Coppa delle Coppe invariate.
- **Maglie** (`engine/Maglie.kt`, `ui/MaglieScreen.kt`, menu "Maglie" e Home): 8 marchi di fantasia (Fulmia, Korvo, Ardena, Taurex, Velka, Montano Sport, Zefiro, Gryp), 14 stili, 3 colletti, bordino. Casa, trasferta e terza. Lo sponsor tecnico è sempre uno degli 8 marchi; senza sponsor tecnico le fa il club. A ogni stagione: tieni o scegli tra 3 proposte, una volta sola. Con un marchio nuovo si cambiano per forza. Salvataggi vecchi: il nome dello sponsor tecnico diventa un marchio fisso.
- **Vendite delle maglie** (sostituiscono il vecchio merchandising): tifosi, piazzamento, giocatori 84+, novità (+20%), bellezza, negozi e sito, prezzo scelto dall'utente. Prezzo standard A 90 €, B 65 €, minori 45-50 €. Costo 45% dello standard. Sensibilità al prezzo: A 1,6, B 2,0, minori 2,6. Con valori medi il guadagno è uguale al vecchio merchandising, quindi il test economico non cambia. Voce "Maglie e merchandising" nel bilancio. Test: `MaglieTest`.
- Stato nuovo (nell'export): "maglie", "maglie_stagione", "maglie_novita", "maglie_proposte", "maglie_prezzo".

## Sessione 24/11 sera (8): Partita e Calendario in stile PC Calcio 7 (ramo feat/stadio, build verde, non provato sul telefono)
- **Prossima partita:** banner come nel menu originale (stemma e nome a sinistra, riquadro bianco con "Giornata N", nome e stemma a destra): `BannerPartita` in `ui/GraficaV07.kt`.
- **Calendario, "Cosa fai oggi":** le attività (Allenamento, Riposo, Amichevole, Mercato, Ritiro) sono nastri colorati con titolo e spiegazione (`NastroTesto`).
- Le altre schermate usano già i componenti del tema (titoli a nastro, pulsanti neri, tabelle su carta): con PC Calcio 7 come tema di partenza prendono quell'aspetto. Non ho riscritto ognuna a mano, e senza screenshot non so quali sembrino ancora fuori posto.
- **Da tenere d'occhio:** una build (14a74be) è fallita nella prova su emulatore con "Couldn't read row 1444 from CursorWindow" (lettura del database mentre i dati cambiano). La build successiva, con lo stesso codice, è passata: sembra un caso raro. Se si ripresenta sul telefono (crash dopo "nuova carriera" o caricamento di un salvataggio), da indagare.

## Sessione 24/11 sera (7): maglie delle nazionali (ramo feat/stadio)
- Prima le nazionali usavano i colori di un club a caso (`Nazionali.squadra` copiava la prima squadra del database). Ora ognuna ha la maglia tipica da casa e da trasferta (`Nazionali.colori`, 26 nazioni; le altre bianco in casa e nero fuori). La squadra di casa (indice pari) indossa la maglia da casa, l'ospite quella da trasferta. Se le due maglie si somigliano, l'ospite scambia i due colori. Valgono anche per le Under 21. Test: `NazionaliColoriTest`.

## Sessione 24/11 sera (6): ritorno allo stile PC Calcio 7 (ramo feat/stadio, non provato sul telefono)
- **Perché non si vedeva:** il tema di partenza era manga (blu notte e rosso). Il tema "Blu PC Calcio 7" esisteva ma andava scelto in Opzioni. Partita e Calendario non erano mai stati rifatti a parte: usano i componenti del tema.
- **Tema predefinito = PC Calcio 7**, anche per chi aveva manga: una sola volta all'avvio (`tema_v07`) la scelta torna a PC Calcio 7. In Opzioni → Tema manga resta scegliibile.
- **Home più vicina alla foto originale:** sfondo azzurro marmorizzato, colonna con le figure dello staff a sinistra (tocca per aprire Staff), barra nera con un dato vero sopra i nastri (campionato, avversario, cassa, posti dello stadio).
- Da guardare sul telefono: Partita, Calendario e le altre schermate nel tema PC Calcio 7; se qualcosa non torna, dire quale schermata.

## Sessione 24/11 sera (5): grafica altre schermate (ramo feat/stadio, build verde, non provato sul telefono)
- **Rosa:** valori colorati (verde 80+, arancio sotto 65, rosso sotto 50, anche morale) e nuova colonna CT con gli anni di contratto (rosso 1 anno o meno, arancio 2, verde di più).
- **Scheda giocatore:** blocco Contratto (`ui/GraficaV07.kt`): etichetta IN SCADENZA / N ANNI, ingaggio annuo e mensile, valore di mercato, ingaggio in % del valore.
- **Prestagione:** amichevoli a tabella (bandiera, avversario, casa/trasferta, risultato o da giocare), contatore colorato (rosso sotto 3, arancio 3-4, verde da 5), lista squadre con bandiera, stadio e pulsante Chiedi.
- Non fatto: forza della squadra avversaria nella lista amichevoli (la schermata non la riceve).

## Sessione 24/11 sera (3): campionati nuovi (ramo feat/stadio, build verde, non provato sul telefono)
- `assets/squadre_europa.json`: 100 squadre inventate, solo massima serie: Olanda 18, Portogallo 18, Belgio 18, Scozia 10, Turchia 18, Grecia 18. Giocatori generati (`SeedData.insertEuropa`), cognomi comuni con iniziali casuali.
- **Solo carriere nuove** (primo avvio e "nuova carriera"); i salvataggi esistenti non li ricevono. Chi usa "predefinito utente" non li vede finché non ne crea uno nuovo.
- Nessuna promozione/retrocessione in questi paesi (una sola serie). Premi e diritti TV tarati nella sessione 4. Stadi più piccoli con `FATTORE_STADIO`.
- Forza per paese in `RoseFisse.leagueRange`. Scozia, Turchia e Grecia aggiunte a `Nazionali.EUROPA`. Bandiere aggiunte (FlagBox, Calendario, scheda giocatore). Test: `SquadreEuropaTest`.
- Esistono già le qualificazioni europee come etichette (`engine/Societa.kt`, oggetto `Europa`: Champions/Europa/Conference League, nomi reali) e `primeDivisioni` nel ViewModel (~riga 3101) ha solo i 5 paesi maggiori + Brasile e Argentina. Il passo 3 (coppe di fantasia) è fatto: vedi "Coppe europee" più sotto.

## Sessione 24/11 sera (2): Home, trofei e piano coppe europee (ramo feat/stadio, build verde)
- Messaggio del presidente: pulsanti "Fissa le amichevoli" (apre il Calendario) e "Salta le amichevoli".
- Home: al centro scudetto con i colori della nazione (campionato), coppa o supercoppa (coppe), stretta di mano (amichevole). Prima della 1ª giornata la prossima partita è l'amichevole, se ce n'è una.
- `ui/components/Trofei.kt`: 9 trofei disegnati (scudetto, coppa nazionale, supercoppa, Coppa dei Campioni d'Europa, Coppa Europa, Coppa delle Coppe, Mondiali, Europei, amichevole). Usati in Home, Albo d'oro e Nazionali.
- Decisioni di Marco: coppe europee giocabili (Campioni: gironi + eliminazione; Europa: eliminazione A/R; Coppe: vincitrici delle coppe), con sorteggi in stile PC Calcio 7. Nuovi campionati (solo massima serie, nomi e stemmi di fantasia, giocatori generati): Olanda, Portogallo, Belgio, Scozia, Turchia, Grecia.
- Ordine: 1) trofei (fatto) 2) campionati nuovi 3) coppe europee e sorteggi.

## Sessione 24/11 sera: stadio personale (ramo feat/stadio, da feat/fine-partita; build verde, non provato sul telefono)
- Nuova schermata **Stadio** (Home → Finanze → Stadio; anche nel menu classico): pianta dall'alto, nome, colore dei seggiolini (12), settori, atmosfera, strutture.
- **Prezzi per settore spostati** da Marketing allo Stadio (stessa chiave "prezzi"). Elasticità per settore: curva 1,7, distinti 1,5, tribuna 1,3, VIP 0,9.
- **Posti per settore legati alle migliorie** (`StadioPersonale.settori`): curve ampliate → curva, anello e settore famiglie → distinti, tribuna coperta → tribuna, skybox e hospitality → VIP. A prezzi standard l'incasso non cambia.
- **Atmosfera**: in casa + strutture (copertura 5, faretti 3, tribuna 2, maxischermo 2, LED 1, anello 1) + curva piena (-6..+6). Vantaggio dal vivo portato a ±5% di possesso.
- **Negozi e sito**: dopo lo Store ufficiale, Sito web → Shop mondiale → App del club; Secondo store → Store in altra città → Store all'estero. `negozi()` e `livelloSito()` pronti per le vendite delle maglie.
- Stato nuovo: "stadio_nome", "stadio_colore" (nell'export). Test: `StadioPersonaleTest`.
- Restano da fare: maglie, merchandising, grafica altre schermate, prova su telefono e tablet.

## Sessione 24/11 pomeriggio (ramo feat/fine-partita, non ancora provato sul telefono)
- **Economia:** Serie C mai in passivo (TV B 4 M, C 3,8 M; premio piazzamento serie minori 2,5 M; sponsor di maglia minimo circa 0,8 M). Test: A mediana +5,6 M, B +4,2 M, C +1,5 M, nessuna di C sotto zero (replica Python del test).
- **Playoff/playout reali:** B a 6 squadre (preliminare secco 5-8 e 6-7, supplementari poi meglio piazzata; semifinali e finale andata/ritorno, meglio piazzata; finale a pari punti supplementari e rigori), terza diretta con più di 14 punti sulla quarta, perimetro playoff entro 14 punti, playout andata/ritorno e non disputato con più di 4 punti di distacco. Serie C: semifinali meglio piazzata, finale supplementari e rigori. Inghilterra e Spagna con regole proprie. Dal vivo se c'è la tua squadra (rigori scelti da te), simulati altrimenti. `Spareggi` in `engine/V06.kt`, `giocaTurno` nel ViewModel.
- **Stemmi:** 8 sagome, 14 motivi, 15 simboli o monogramma, stelle, corona, alloro, doppio bordo, oro (`ui/components/Components.kt`, `StileStemma`).
- **Volti manga:** collo, ombre, capelli con ciocche, iride sfumata, guance. Modificabili solo nell'editor (`LocalEditorAttivo`).
- **Editor:** "Usa come predefinito" salva squadre, giocatori, staff e volti in `predefinito_utente.json`: li usa ogni nuova carriera e "Ripristina". "Torna all'originale" lo cancella.
- **Home** in stile PC Calcio 7 (`ui/HomePc.kt`): intestazione partita, blocchi Campionato/Allenatore/Mercato/Finanze, trofeo, barra Salva/Notizie/Continua.
- **Grafica:** tabellone con luci, campo formazioni in prospettiva.
- **Telecronisti** di fantasia (`engine/Telecronisti.kt`): prime voci Ferrandi (sobrio), Strepponi (urlatore), Bucchetti (immaginifico), Lanfranchi (enciclopedico); seconde voci Contrafatti (poeta tattico), Sbaraglia (ex attaccante schietto, dialetto), Percentini (statistico), Marcolin (ex difensore tecnico), Spuntazzo (ironico). Coppia casuale a ogni partita.
- **Squadra B** con partita mensile simulata dal vice; prelazione sul satellite; fedeltà per id; rivendita anche all'estero.

## Prossimi lavori decisi con Marco
- Maglie, merchandising e stadio personalizzabile: **fatti** (sessioni 24/11 sera).
- Grafica delle altre schermate: **fatta** (sessione 5), da provare sul telefono.

## Da provare
- **v0.6.0 e ramo feat/fine-partita:** mai provati sul telefono. La v0.6.0 è stata provata solo su emulatore (Android 11).
- **Blocchi 1–6 e tablet:** da provare sul telefono e sul tablet.
- **Tema Chiaro:** controllare contrasto e grafico (i pannelli navy restano scuri).

## Da decidere o da fare
- **Merge su `main` e v0.7.0:** deciso il 24/11: solo dopo la prova sul telefono.
- **Installazione:** la chiave di firma è fissa, ma questa volta bisogna disinstallare la vecchia versione una volta. Esportare prima i salvataggi.

## Da verificare
- **Verifica economica:** fatta. Il test `EconomiaV06Test` ha soglie strette: A mediana 4..7,5 M, B 3..5,5 M, C 0,8..2,5 M, nessuna di C in passivo, nessuna di A sotto -8 M.

## Limiti noti
- La squadra B gioca una partita al mese simulata (nessuna partita 2D).
- Playoff e playout: giocati dal vivo solo se c'è la tua squadra. In parità passa la meglio piazzata, tranne nelle finali (supplementari e rigori) e nel preliminare di B (supplementari, poi meglio piazzata).
- Avatar ancora disegnati in codice.

## Blocchi del ramo feat/fine-partita
- **Palla e giocatori:** pallini numerati stile FM; pallone che ruota e cresce se alza; traiettoria blu tratteggiata; movimento dei giocatori in base all'azione (`ui/Manga.kt`, `rememberPalla`). CI con un solo allegato (APK).
- **Blocco 1/6 – partita:** meteo sul campo, recupero dinamico (90+N), replay del gol, linea del fuorigioco, scena infortunio, mappa di calore (tocca il nome a fine partita).
- **Blocco 2/6 – calci piazzati e rigori:** calci piazzati in scena (battitore e zona di porta); rigori a scelta nelle coppe (dove tirare e dove tuffarti).
- **Blocco 3/6 – preparatore atletico capo:** serve un preparatore generale in staff. Decide allenamenti e riposi nei giorni liberi, piano di squadra e piani per reparto, crescita mensile del vivaio. Report mensile nella scheda Rosa. Interruttore on/off.
- **Blocco 4/6 – analista video:** nuovo posto in staff con report sull'avversario. Missioni osservatori "Promesse" per nazione (giovani 16–20 anni).
- **Blocco 5/6 – società:** rinnovi con clausole (bonus gol doppio con ingaggio −6%; rescissoria con ingaggio −8%). Ultras con umore e contestazioni; coreografia da 15.000 € nella schermata Giornale.
- **Blocco 6/6 – squadra B e amichevoli:** squadra B/Under 23 e satellite (max 8 giocatori, solo fino a 23 anni, crescita mensile extra; satellite 40.000 €/anno), seguite dal preparatore capo. Amichevoli su invito (due club al mese).
- **Tablet:** rotazione libera; menu laterale sempre visibile da 720 dp; contenuto centrato; partita su due colonne.
- **Già presenti nel gioco, non rifatti:** diritti TV per posizione; offerte di altri club e ct di nazionale.

## Blocchi 14–17 del ramo feat/fine-partita (nel codice, rimessi nel report il 24/11)
- **Blocco 14 – Cambi con condizioni** (`engine/CambiCondizioni.kt`): stanco (<88%), ammonito, sotto dal 60' entra un attaccante, sopra dal 75' entra un difensore; max un cambio ogni 5 minuti dal 30'. Tattica → Cambi con condizioni.
- **Blocco 15 – Atmosfera stadio** (`engine/StatExtra.kt`): solo partita dal vivo, ±4% sul possesso di chi gioca in casa (tifosi e capienza). Passaggi, precisione, duelli e km in Stat e nell'analisi finale.
- **Blocco 16 – Gioco sulle fasce e calci piazzati** (`engine/FasceSchemi.kt`): Centrale/Misto/Fasce (±8% in base ai giocatori della zona); schema Normale/Palloni in area/Tiro diretto/Corto. I predefiniti non hanno effetto.
- **Blocco 17 – Ritiro, fedeltà, sfide** (`engine/Fedelta.kt`, `engine/Sfide.kt`): ritiro precampionato (luglio-agosto, 20.000 €/giorno); bonus fedeltà a fine stagione (3+ stagioni, 3% dell'ingaggio, morale +3); sfide a scelta facile/media/difficile.

## Da fare (priorità)
1. Provare tutto su telefono e tablet (playoff con supplementari e rigori, stadio, maglie, coppe europee, nuovi campionati). Da verificare: uscita dall'app a metà gara di playoff.
2. Merge su `main` e v0.7.0, dopo la prova.
3. Eventuale ritocco della grafica dopo la prova sul telefono.
4. Avatar: da decidere cosa si vuole (oggi disegnati in codice).
5. (fatto: coppe a metà settimana)

## Fatto il 24/11 (dopo i playoff)
- **Squadra B con partite proprie:** una al mese contro un club della tua nazione, simulata dall'allenatore in seconda (senza vice non si gioca). Vittoria +4% e pareggio +2% alla crescita del mese; ultimi risultati nella scheda Rosa.
- **Prelazione sul satellite:** i giovani (max 23 anni) del satellite con contratto in scadenza restano 3 anni allo stesso ingaggio.
- **Bonus fedeltà per id:** gli omonimi non si confondono più (le vecchie chiavi a nome valgono una volta).
- **Rivendita:** 25% delle volte verso un club di un'altra nazione.
- **Test economico misurato in CI:** Serie A mediana +5,6 M (peggiore −2,8 M), Serie B +2,2 M. Serie C mediana −2,9 M: ritarata nel pomeriggio (ora circa +1,5 M, nessuna in passivo).

## Note di controllo del 24/11 (lettura del codice)
- La verifica economica è il test `EconomiaV06Test.squadraMediaInPariOUtile` e gira in CI. Soglie strette dal pomeriggio del 24/11 (vedi "Da verificare").
- La prova su emulatore in CI usa **1.500 tocchi** (`.github/smoke.sh`), non 9.000 come scritto sopra.

## Versione v0.6.0 (23/11/2026)
### Economia — meno punitiva
| Voce | Prima | Ora |
|---|---|---|
| Sponsor (quota sul valore di un titolare) | 0,12 | 0,18 (+50%) |
| Premio sponsor a vittoria | 40k fisso | 2% dei compensi annui, minimo 60k; +25% senza subire gol |
| Merchandising per 1.000 posti | 15k | 25k |
| Diritti TV (A / B / 3ª / C) | 6 / 1,2 / 0,8 / 0,6 M | 7 / 3,2 / 1,8 / 1,5 M |
| Premio piazzamento B / 3ª / minori | 2 / 1 / 0,8 M | 3 / 1,5 / 1 M |
| Taglio del presidente per debito | sopra 15 M, 3% | sopra 25 M, 2% |
| Penale rifinanziamento | 2% | 1% (estinzione resta 2%) |

**Bancarotta → nuovo proprietario.** A fine stagione, se la cassa è a zero (o sotto il 5% degli ingaggi con debiti aperti):
- arriva un nuovo proprietario con nome di fantasia, sempre diverso dal precedente;
- azzera mutui, rate e storico fair play;
- versa capitale: 30% del monte ingaggi, minimo 8 M (A), 3 M (B), 1,5 M (C);
- fiducia a 55, tifosi +5; nessuna penalità in classifica;
- il proprietario si vede in Finanze → Proprietà.

### Le 20 migliorie
| # | Miglioria | Dove |
|---|---|---|
| 1 | Playoff per l'ultimo posto promozione in ogni serie minore; playout in B; playoff Serie C. Vincitori nell'albo | Fine stagione, Classifica (◆) |
| 2 | Grafico della posizione giornata per giornata | Società → Analisi |
| 3 | Minuti giocati; chi è tra i 14 migliori e gioca meno del 30% perde morale | Analisi, scheda |
| 4 | Vicecapitano | Scheda giocatore |
| 5 | 3 formazioni salvate (titolari, modulo, mentalità, pressing, linea) | Tattica |
| 6 | Trappola del fuorigioco (solo con linea alta) | Tattica, partita |
| 7 | Plusvalenze della stagione | Finanze |
| 8 | Cessione con 15% sulla futura rivendita (5% in meno subito) | Finanze, Mercato |
| 9 | Ammortamento dei cartellini sugli anni di contratto | Finanze |
| 10 | Richiesta al presidente di più monte ingaggi | Dirigenza |
| 11 | Nuovo stadio: 3 stagioni, 1.500 € a posto, rate a fine stagione | Finanze |
| 12 | Carriera: squadra e voto medio per stagione, totali | Scheda giocatore |
| 13 | Confronto tra due squadre (anche di altri campionati) | Analisi |
| 14 | Classifica Totale/Casa/Trasferta e colonna Forma | Classifica |
| 15 | Simula fino a: +5 giornate, mercato invernale, vigilia del derby | Calendario |
| 16 | Premio partita: morale +6, si paga solo se vinci | Tattica |
| 17 | Ricerca per caratteristica (veloce, tecnico, rigorista…; soglia 78) | Mercato → Filtri |
| 18 | Stato del prato; cura Ordinaria/Curata/Professionale | Finanze |
| 19 | Storico infortuni (3+ = "fragile") | Scheda giocatore |
| 20 | Esporta riepilogo stagione in CSV | Salvataggi |

Nota: nel primo report avevo scritto che le 20 non c'erano nel codice. Due esistevano in parte (monte ingaggi, n. 10; stagioni nella scheda, n. 12). Le ho trasformate in funzioni nuove.

### Correzioni B–F
- **B** Cessioni a rate (con "A rate" attivo, sopra 1 M).
- **C** Rimborso assicurazione sulle giornate vere (34 con 18 squadre).
- **D** Il reset cancella anche l'albo d'oro.
- **E** Confronto giocatori: ricerca per nome in tutto il database.
- **F** Tema Chiaro (Opzioni → Tema).

### Avvio e distribuzione
- **Crash all'avvio** (presente da v0.4): alcune operazioni iniziali partivano prima che il gioco fosse pronto. Ora partono solo a gioco pronto.
- **Prova automatica su emulatore** a ogni build: avvio + 1.500 tocchi casuali. Nessun crash.
- **Un solo file:** `Androcalcio-0.6.0.apk` (rose e dati già dentro).

### Coppe europee (ramo feat/stadio)
- **Tre coppe:** Coppa dei Campioni d'Europa (32 squadre, 8 gironi da 4, poi ottavi, quarti, semifinali, finale), Coppa Europa (16) e Coppa delle Coppe (16), a eliminazione diretta.
- **Undici nazioni:** Italia, Spagna, Inghilterra, Germania, Francia, Olanda, Portogallo, Belgio, Turchia, Scozia, Grecia. Brasile e Argentina restano con la Sudamericana.
- **Qualificate:** dalla classifica e dalla coppa nazionale. I posti non presi scendono in classifica. Una squadra gioca una sola coppa.
- **Sorteggio:** 4 urne per forza, mai due squadre della stessa nazione nello stesso girone. Si crea a inizio stagione.
- **Partite:** le tue le decidi tu (Gioca dal vivo o Simula). Le altre si simulano da sole. Nei gironi i pareggi restano; supplementari e rigori solo a eliminazione.
- **Premi fissi (tarati il 24/11 sera):** Campioni 3 M di partecipazione, 0,6 M per vittoria nel girone, 0,2 M per pareggio, turni 1/1,5/2,5/3,5 M, titolo 6 M. Europa e Coppe più bassi. Finiscono nel bilancio di fine stagione e nell'albo d'oro (nazione "Europa").
- **Schermata** "Coppe europee" nel menu: urna, gironi con maglie e bandiere, tabellone.
- **Limite:** le partite europee si giocano dopo campionato e coppa nazionale, non a metà settimana. Servirebbe rifare l'ordine del calendario.
- **Da fare:** gestire la finale dal vivo con rigori scelti da te nei turni europei (già previsto, da provare); provare tutto su telefono e tablet.
- File nuovi: `engine/CoppeEuropee.kt`, `ui/EuropaScreen.kt`, test `CoppeEuropeeTest`.

### Sessione 24/11 sera (9): stile, movimento, ruoli
- **Stile Home ovunque (tema PC Calcio 7 blu):** sfondo azzurro marmorizzato su tutte le schermate, titoli di sezione a nastro obliquo, riquadri leggermente trasparenti.
- **Centro sportivo:** nella scheda "Centro sportivo" ora c'è un disegno dedicato (campi con strisce, porte e coni, palestra, centro medico, video, GPS, piscina, crio, mensa, relax, foresteria, campo coperto). Gli edifici si colorano con i lavori.
- **Movimento fluido a x1:** la palla va a velocità costante (non frena a ogni passaggio), i giocatori seguono la palla con una molla (nessuno scatto), l'anello giallo compare con più margine.
- **Nuovi ruoli:** Esterno destro (FD), Esterno sinistro (FS), Ala destra (AD), Ala sinistra (AS). Compaiono nei filtri del Mercato e nelle affinità. Difesa già con destro/sinistro/centrale.
- **Allenamento:** con focus Tattica un giocatore può imparare un ruolo nuovo (parte a metà affinità) o salire di 4 punti in uno noto; con focus Tecnica può diventare ambidestro (lento, dipende da età e allenatore).
- **Cinico ("portato al risultato"):** circa 1 giocatore su 6 lo è dalla nascita, gli altri possono acquisirlo con l'allenamento tattico (molto raro). Ha il 20% di peso in più tra i tiratori. Salvato in Stato ("tratti"). Etichetta "Cinico" nella scheda giocatore.
- **Cronaca sincronizzata:** il testo, la cronaca e il punteggio di un'azione compaiono quando la palla ha finito il tragitto (non a x4 o più).
- **Da provare sul telefono:** fluidità reale, bilanciamento del Cinico, nuovi ruoli in formazione (nessuna formazione li usa ancora in automatico: si scelgono dalle affinità).

## Cronologia
- v0.3.0 (22/11): 20 migliorie (conferenze, sponsor, vivaio, nazionali U21, All Star…).
- v0.4.0: mutui, batteria/prestazioni, Analisi.
- v0.5.0: 16 migliorie (rifinanziamento, rate, ampiezza, backup .zip, tema nero…).
- v0.6.0 (23/11): economia più generosa, nuovo proprietario, 20 migliorie, B–F.
- feat/fine-partita (24/11): blocchi 1–6 e tablet, non ancora una release.
- feat/stadio (24/11): stadio personale, trofei, sei nuovi campionati, coppe europee, maglie e vendite, tarature. Non ancora una release.

## Modello
Sonnet basta per la prova, il merge e la grafica; Opus solo per nuove tarature.

## 25/11 — CONTINUA ibrido + Messaggi
- Barra CONTINUA sempre visibile in ogni schermata; passa i giorni da sola (attività automatica o preparatore capo) e si ferma al prossimo evento o al giorno della partita (max 120 giorni).
- Nuova schermata **Messaggi** (menu + pulsante "✉ n" accanto a CONTINUA se ci sono non letti). Un tocco apre la schermata giusta.
- Eventi: Partita e coppe (nuovi turni di coppa), Mercato (apre/chiude), Giocatori e società (infortuni, contratti in scadenza, offerte, richieste), Staff e strutture (candidati, cantieri finiti).
- Codice: `engine/Posta.kt` (diff snapshot, test `PostaTest`), `ui/PostaScreen.kt`, VM `continuaGiorno()`.
- Non incluso: report mensile, scadenza contratti staff, sponsor (da aggiungere se servono).
- CI verde (emulatore: primo tentativo morto per DeadSystemException, ripetuto OK).
- Da fare da te: prova su telefono; OK per merge su main e release v0.7.0.
- Suggerimento: apri una nuova chat con questo report.md (limite 5 domande superato).

## 25/11 pomeriggio — Schede staff, amichevoli, capo preparatori, analista
- Icone staff della home (ALL, ASS, GIO, PRE, FIS, SCO, GIA, ANA) aprono la scheda del ruolo: ALL→Tattica, ASS→Calendario, GIO/SCO→Vivaio, PRE→Preparatori, FIS→Fisioterapisti, GIA→Giardiniere (campo + cantieri), ANA→Analista. Contratti restano in Staff (menu). Codice: `ui/SchedeStaff.kt`, enum `Screen` (PREPARATORI, FISIO, GIARDINIERE, ANALISTA).
- Amichevoli: al primo tocco di "Amichevole" in stagione chiede "delego all'allenatore in seconda o scelgo io" (Stato `amich_scelta_<stagione>`); delega = `vm.delegaAmichevoli()` (squadre di forza simile, stessa nazione).
- Capo preparatori: `PreparatoreCapo.giornata/attivitaGiocatore` — attività per singolo giocatore (recupero, riposo, allenamento, rifinitura il giorno prima), usata da CONTINUA e giorni saltati.
- Analista video: `engine/AnalistaPartita.kt`, report dell'ultima partita salvato in Stato `ana_ultima` (✔ bene, ✘ male, da migliorare) + avversario sotto. Compare solo dopo una partita giocata con l'analista assunto.
- CI verde (test motore, build, emulatore).

## Da decidere / fare
- Prova su telefono di tutto `feat/stadio`; graphics tweaks (7); avatar (8); OK per merge su main + release v0.7.0 (9).
- Messaggi: fatti anche report mensile, staff e sponsor (25/11 pomeriggio).
- Decisioni aperte precedenti: Cinico (rigori base 0.64, Cup.shootout, "in bilico"), D con FD mai in linea C, vecchi salvataggi e turni coppa, scala stipendi staff sulla tua rosa, stelle youth/scouting/maglie ancora intere.

## Per ripartire in una nuova chat
Repo: https://github.com/MJ83-collab/Androcalcio, branch `feat/stadio`. Niente SDK locale: compilazione e test solo con GitHub Actions (`gh run list --branch feat/stadio`). Regole di Marco: risposte brevi e precise, chiedere prima di modificare, report.md ogni 5 domande.


## Aggiornamento 25/11 sera (2): valori reali aggiunti
Per Cádiz (22,7 M€), Granada (35,85 M€), Athletico Paranaense (~31 M€), Cuiabá (~13,5 M€) e Criciúma (~13,4 M€) ho usato valori Transfermarkt (footystats; i brasiliani da R$ a € a circa 6,2). Ricalcolo fatto dai file originali (prima della scalatura). Restano senza valore: Sampdoria, Wolfsburg, Heidenheim, Burnley, Almería, Metz, Clermont, Lorient: le fonti trovate usano altre stime (non confrontabili con Transfermarkt), quindi non le ho mescolate.

## Export database per l'editor (serie, squadre, giocatori)

- Nuovo file `androcalcio-editor.json`, esportato dal pulsante "Esporta serie, squadre, giocatori" nell'Editor.
- Formato `androcalcio-editor`, versione 1. Contiene:
  - `serie`: ogni coppia nazione + campionato, con l'elenco delle squadre.
  - `squadre`: tutte le squadre, con parametri economici e tecnici, `giocatori` (parametri, mentalità, ruoli extra) e `staff`.
- Il salvataggio di gioco ("Esporta JSON") resta invariato e non viene sostituito.
- Stato: non verificato con la build locale (non disponibile). Verificato con la CI dopo il push.

## Import database per l'editor

- Nuovo pulsante "Importa serie, squadre, giocatori" nell'Editor. Legge `androcalcio-editor.json`.
- Regole:
  - squadre, giocatori e staff con lo stesso id vengono aggiornati;
  - gli elementi nuovi vengono creati;
  - ciò che non è nel file resta invariato: l'import non cancella nulla;
  - mentalità e ruoli extra vengono salvati, come nell'editor.
- Le serie non si importano a parte: sono ricavate dalle squadre.
- Il pulsante "Importa JSON" resta per il salvataggio di gioco.
- Non verificato in locale: la verifica è la build su GitHub Actions e il test su telefono.

## Pulsanti database e ricerca società

- Editor dati: "Esporta tutto il database (JSON)" e "Importa tutto il database (JSON)", una per riga a tutta larghezza. Prima i pulsanti erano in una riga sola e "Importa" usciva dallo schermo.
- Salvataggio di gioco: "Esporta salvataggio (JSON)" e "Importa salvataggio (JSON)", separati sotto un'etichetta.
- Elenco società nell'Editor: campo "Cerca società, campionato o nazione".

## Tattica: trascina i giocatori per cambiare posizione

- Nel campo (Tattica, scheda Disposizione) trascini un giocatore su un altro: i due si scambiano la posizione in campo.
- Lo scambio cambia il ruolo assegnato (ruolo in campo e compito azzerato), come il cambio di ruolo dal menu.
- Attivo in Combinata e In possesso. In "Non in possesso" il trascinamento è disattivato.
- Limite: si scambiano solo due giocatori già in campo. Non c'è ancora il trascinamento su uno slot vuoto (non esistono slot vuoti con 11 titolari).
