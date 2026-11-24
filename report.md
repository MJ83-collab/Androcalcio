# Androcalcio — report (aggiornato 24/11/2026, sera 4)

Ramo attuale: `feat/stadio` (nato da `feat/fine-partita`: v0.6.0 + blocchi 1–6 + tablet + stadio, maglie, coppe europee).
Kotlin 2.0 / Compose / Room (DB v24, nessuna migrazione; dati nuovi in tabella Stato).
Build GitHub Actions verdi sull'ultimo commit. ⚠️ Nulla è stato provato sul telefono.

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
5. Coppe europee a metà settimana (oggi si giocano dopo campionato e coppa).

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

## Cronologia
- v0.3.0 (22/11): 20 migliorie (conferenze, sponsor, vivaio, nazionali U21, All Star…).
- v0.4.0: mutui, batteria/prestazioni, Analisi.
- v0.5.0: 16 migliorie (rifinanziamento, rate, ampiezza, backup .zip, tema nero…).
- v0.6.0 (23/11): economia più generosa, nuovo proprietario, 20 migliorie, B–F.
- feat/fine-partita (24/11): blocchi 1–6 e tablet, non ancora una release.
- feat/stadio (24/11): stadio personale, trofei, sei nuovi campionati, coppe europee, maglie e vendite, tarature. Non ancora una release.

## Modello
Sonnet basta per la prova, il merge e la grafica; Opus solo per nuove tarature.
