# Androcalcio — report (23/11/2026) · v0.6.0

Kotlin 2.0 / Compose / Room (DB v24, **nessuna migrazione**; dati nuovi in tabella Stato).
Test e build GitHub Actions verdi (debug e release R8). ⚠️ Non provato su telefono.

## Economia (punto A) — meno punitiva
| Voce | Prima | Ora |
|---|---|---|
| Sponsor (quota sul valore di un titolare) | 0,12 | **0,18** (+50%) |
| Premio sponsor a vittoria | 40k fisso | **2% dei compensi annui**, minimo 60k; +25% senza subire gol |
| Merchandising per 1.000 posti | 15k | **25k** |
| Diritti TV (A / B / 3ª serie / C) | 6 / 1,2 / 0,8 / 0,6 M | **7 / 3,2 / 1,8 / 1,5 M** |
| Premio piazzamento B / 3ª serie / minori | 2 / 1 / 0,8 M | **3 / 1,5 / 1 M** |
| Taglio del presidente per debito | sopra 15 M, 3% | **sopra 25 M, 2%** |
| Penale rifinanziamento | 2% | **1%** (estinzione resta 2%) |

**Bancarotta → nuovo proprietario.** A fine stagione, se la cassa è a zero (o sotto il 5% degli ingaggi con debiti aperti):
- arriva un nuovo proprietario (nome di fantasia, sempre diverso dal precedente);
- azzera mutui, rate e storico fair play;
- versa capitale: 30% del monte ingaggi, minimo 8 M (A), 3 M (B), 1,5 M (C);
- fiducia a 55, tifosi +5. **Nessuna penalità in classifica.**
- Il proprietario si vede in Finanze → Proprietà.

Verifica (test con le rose vere, una stagione tipo con 3 sponsor): Serie A mediana circa +5 M, Serie B circa +2 M, Serie C circa in pari. Nessuna squadra di A perde più di 8 M.

## Le 20 migliorie
| # | Miglioria | Dove |
|---|---|---|
| 1 | Playoff (4 squadre, gara secca) per l'ultimo posto promozione in ogni serie minore; playout in B (16ª-17ª); playoff Serie C tra le seconde e la migliore terza. Vincitori nell'albo | Fine stagione, Classifica (◆) |
| 2 | Grafico della posizione giornata per giornata | Società → Analisi |
| 3 | Minuti giocati; chi è tra i 14 migliori e gioca meno del 30% perde morale | Analisi, scheda |
| 4 | Vicecapitano | Scheda giocatore |
| 5 | 3 formazioni salvate (titolari, modulo, mentalità, pressing, linea) | Tattica |
| 6 | Trappola del fuorigioco (solo con linea alta): ferma il 16% delle azioni, nel 5% l'avversario va in porta da solo | Tattica, partita |
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
| 18 | Stato del prato; cura Ordinaria/Curata/Professionale; sotto 50 meno occasioni in casa | Finanze |
| 19 | Storico infortuni (3+ = "fragile") | Scheda giocatore |
| 20 | Esporta riepilogo stagione in CSV | Salvataggi |

**Errore mio:** avevo scritto che le 20 non c'erano nel codice. Due esistevano in parte: il monte ingaggi (n. 10, già un tetto) e le stagioni nella scheda (n. 12, solo presenze e gol). Le ho trasformate in qualcosa di nuovo (richiesta al presidente; squadra e voto per stagione).

## Correzioni B–F
- **B** Cessioni a rate (con "A rate" attivo, sopra 1 M).
- **C** Rimborso assicurazione sulle giornate vere (34 con 18 squadre).
- **D** Il reset cancella anche l'albo d'oro.
- **E** Confronto giocatori: ricerca per nome in tutto il database.
- **F** Tema Chiaro (Opzioni → Tema). I pannelli navy restano scuri.

## Correzione avvio (v0.6.0, 23/11 pomeriggio)
- **Crash all'avvio** (c'era già da v0.4): alcune operazioni iniziali partivano prima che il gioco fosse pronto (stato dei mutui ancora nullo). Ora partono solo a gioco pronto.
- **Prova automatica su emulatore** a ogni build: avvio + 9.000 tocchi casuali. Nessun crash.
- **Chiave di firma fissa** (`app/androcalcio.keystore`): le prossime versioni si installano sopra senza disinstallare. Questa volta serve disinstallare la vecchia una volta (prima esporta i salvataggi).
- **Un solo file**: `Androcalcio-0.6.0.apk` (rose e dati già dentro). Niente più debug/release/rose separati.

## Limiti noti
- Provato su emulatore (Android 11), non sul telefono. Da guardare: tema Chiaro (contrasto) e grafico.
- Playoff simulati con la forza delle rose (niente partita 2D).
- Rivendita: solo verso club della stessa nazione.
- Restano: avatar disegnati in codice.

**Modello:** Sonnet basta per altre migliorie; Opus solo per nuove tarature.

## Dopo v0.6.0 – ramo feat/fine-partita (24/11/2026, build verdi, non provato su telefono)
- Palla: passa da giocatore a giocatore, i vicini le vanno incontro, traiettoria blu tratteggiata dell'azione (`ui/Manga.kt`, `rememberPalla`).
- CI: un solo allegato (APK).
- Blocco 14 – Cambi con condizioni (`engine/CambiCondizioni.kt`): stanco, ammonito, sotto dal 60', sopra dal 75'; max un cambio ogni 5 minuti dal 30'. Tattica > "Cambi con condizioni".
- Blocco 15 – Atmosfera stadio (`engine/StatExtra.kt`, solo partita dal vivo: ±4% sul possesso di chi gioca in casa; tifosi e capienza). Passaggi, precisione, duelli, km in Stat e nell'analisi finale.
- Blocco 16 – Gioco sulle fasce (`engine/FasceSchemi.kt`: Centrale/Misto/Fasce, ±8% in base ai giocatori della zona) e schema sui calci piazzati (Normale/Palloni in area/Tiro diretto/Corto). Predefiniti = nessun effetto, taratura invariata.
- Blocco 17 – Ritiro precampionato (attività del calendario, luglio-agosto, 20.000 €/giorno), bonus fedeltà a fine stagione (3+ stagioni, 3% dell'ingaggio, morale +3; chiave `fedelta`), sfide a scelta (facile/media/difficile) con serie di 3.
- Da fare: provare sul telefono; merge su main e v0.7.0 (chiedere a Marco); grandi: squadra B, satellite, amichevoli su invito.

---
## Cronologia
- v0.3.0 (22/11): 20 migliorie (conferenze, sponsor, vivaio, nazionali U21, All Star…).
- v0.4.0: mutui, batteria/prestazioni, Analisi.
- v0.5.0: 16 migliorie (rifinanziamento, rate, ampiezza, backup .zip, tema nero…).
- v0.6.0 (questo report): economia più generosa, nuovo proprietario, 20 migliorie, B–F.

## Blocchi 18-23 e tablet – ramo feat/fine-partita (24/11/2026)
- Pallone e giocatori: pallini numerati stile FM, pallone che ruota e cresce se alto, traiettoria blu tratteggiata, movimento in base all'azione.
- Blocco 1/6 partita: meteo sul campo, recupero dinamico (90+N), replay del gol, linea del fuorigioco, scena infortunio, mappa di calore (tocca il nome a fine partita).
- Blocco 2/6: calci piazzati in scena (battitore e zona di porta); rigori a scelta nelle coppe (scegli dove tirare e dove tuffarti).
- Blocco 3/6: preparatore atletico capo (serve il preparatore generale in staff): decide allenamenti e riposi nei giorni liberi, piano di squadra e piani per reparto, crescita mensile del vivaio, report mensile nella scheda Rosa. Interruttore on/off.
- Blocco 4/6: analista video (nuovo posto staff) e report sull'avversario; missioni osservatori "Promesse" per nazione (giovani 16-20 anni).
- Blocco 5/6: rinnovi con clausole (bonus gol doppio -6% ingaggio, rescissoria -8%); ultras (umore, contestazioni) e coreografie (15.000 €) nella schermata Giornale.
- Blocco 6/6: squadra B/Under 23 e satellite (max 8, solo fino a 23 anni, crescita mensile extra; satellite 40.000 €/anno), seguite dal preparatore capo; amichevoli su invito (2 club al mese).
- Tablet: rotazione libera, menu laterale sempre visibile da 720 dp, contenuto centrato, partita su due colonne.
- Già presenti e non rifatti: diritti TV per posizione (16), offerte di altri club e ct nazionale (20).
- Non fatto: prelazione sui giovani del satellite; la squadra B non gioca partite proprie (solo crescita).
- Da fare: prova su telefono e tablet, poi merge su main / v0.7.0.
