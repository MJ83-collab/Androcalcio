# Androcalcio — report (aggiornato 24/11/2026)

Ramo attuale: `feat/fine-partita` (v0.6.0 + blocchi 1–6 + tablet).
Kotlin 2.0 / Compose / Room (DB v24, nessuna migrazione; dati nuovi in tabella Stato).
Build GitHub Actions verdi sull'ultimo commit. ⚠️ Nulla è stato provato sul telefono.

## Da provare
- **v0.6.0 e ramo feat/fine-partita:** mai provati sul telefono. La v0.6.0 è stata provata solo su emulatore (Android 11).
- **Blocchi 1–6 e tablet:** da provare sul telefono e sul tablet.
- **Tema Chiaro:** controllare contrasto e grafico (i pannelli navy restano scuri).

## Da decidere o da fare
- **Merge su `main` e v0.7.0:** da decidere con Marco.
- **Installazione:** la chiave di firma è fissa, ma questa volta bisogna disinstallare la vecchia versione una volta. Esportare prima i salvataggi.

## Da verificare
- **Verifica economica con le rose vere:** da eseguire (Serie A, B, C e tetto di 8 M). Valori attesi: Serie A mediana circa +5 M, Serie B circa +2 M, Serie C circa in pari; nessuna squadra di A perde più di 8 M. Non ancora eseguita.

## Limiti noti
- La squadra B non gioca partite proprie: cresce soltanto.
- Prelazione sui giovani del satellite: non fatta.
- Rivendita solo verso club della stessa nazione.
- Playoff simulati con la forza delle rose, senza partita 2D.
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
- **Prova automatica su emulatore** a ogni build: avvio + 9.000 tocchi casuali. Nessun crash.
- **Un solo file:** `Androcalcio-0.6.0.apk` (rose e dati già dentro).

## Cronologia
- v0.3.0 (22/11): 20 migliorie (conferenze, sponsor, vivaio, nazionali U21, All Star…).
- v0.4.0: mutui, batteria/prestazioni, Analisi.
- v0.5.0: 16 migliorie (rifinanziamento, rate, ampiezza, backup .zip, tema nero…).
- v0.6.0 (23/11): economia più generosa, nuovo proprietario, 20 migliorie, B–F.
- feat/fine-partita (24/11): blocchi 1–6 e tablet, non ancora una release.

## Modello
Sonnet basta per altre migliorie; Opus solo per nuove tarature.
