# Androcalcio — report per la nuova chat (22/11/2026)

Repo `MJ83-collab/Androcalcio`, ramo `feat/pc7-meccaniche` (PR #2). Kotlin/Compose/Room, DB v24, app v0.2.0.
Test motore in locale: 48/48 ok. **Ultima build CI non partita**: GitHub ha bloccato le Actions per un problema di pagamento/limite di spesa dell'account (Settings → Billing & plans). Ultima build verde: `d97c5f9`.

## Fatto finora
- Bug: migrazioni DB, prezzo mercato, gironi Serie C, confronti tra nazioni, vendite che creavano soldi, clausole lente, stato carriera in import/export e a nuova carriera.
- Economia estratta in `engine/Economia.kt` e testata. Difficoltà (budget iniziale) in Opzioni.
- Grafica "mista": menu e tabelle PC Calcio 7, partita dal vivo notturna neon (ciano casa, magenta ospiti, giallo gol).
- Home con logo + versione su una riga, prossima partita; menu laterale a scomparsa; mentalità leggibile.
- Stemmi: scudo con colori sociali + mascotte ispirata al simbolo del club (Inter serpente, Milan diavolo, Juve zebra, Roma lupo, Torino toro…), **unica per campionato** (`engine/Stemmi.kt`, generato). Niente loghi reali.
- Calendario: data di ogni giornata (domenica, sosta di Natale); tolta la mentalità.
- Mercato: filtri liberi a intervallo (età 10–45, media, contratto), ruolo preciso, prezzo massimo, svincolati.
- Gabriele Jannino: MED + DC 90 + TS 85 + TD 85 (quarto ruolo `sec4`, migrazione 23→24).
- Avatar a fumetto procedurali: giudicati **insufficienti** dall'utente.

## Da fare (richieste dell'utente, in ordine proposto)
1. **Avatar stile Sakamoto Days**: disegnati a codice non raggiungono quella qualità. Proposta: immagini PNG/WebP (generate dall'utente con un'IA di immagini o un illustratore) da importare come risorse; il codice le assegna per ruolo/età/nome. Decidere quante (es. 40 volti).
2. **Vivaio**: giovani 10–16 anni, crescono anno per anno nelle giovanili; avatar; scheda dettagliata dei progressi dall'allenatore delle giovanili (annuale o su richiesta); scelta tra "convoca per la prossima partita" e "promuovi in prima squadra".
3. **Stadio**: almeno 30 migliorie con costo, tempi di lavoro (in giornate), ritorno economico (incassi, merchandising, tifosi), grafica dello stadio che cresce.
4. **Sponsor**: tecnico, abbigliamento, maglia, stadio, ecc. (almeno 10 categorie); nuova figura **addetto marketing** che trova offerte migliori.
5. **Difficoltà** più ampia (5 livelli che toccano budget, fiducia, IA avversarie, infortuni, mercato) + **profilo personalizzato**.
6. **Nazionali Under 21** con calendari propri.
7. **Psicologo** nello staff: migliora il morale dei giocatori in difficoltà.
8. **Squadre All Star per decennio** (anni '60, '70, '80, '90, 2000, 2010, 2020), giocabili solo in amichevole. Nomi dei campioni **storpiati** come per le squadre (deciso dall'utente).
9. **Tutto collegato**: in ogni scheda e in ogni riga, toccando un nome, una bandiera di nazionalità o una squadra si apre la scheda relativa (giocatore → squadra → nazionale → staff…). Serve una navigazione unica con "indietro".

## 20 migliorie consigliate
1. Conferenza stampa pre/post partita con risposte che toccano morale e tifosi.
2. Colloqui individuali con i giocatori (minuti, contratto, ruolo).
3. Infortuni con diagnosi, tempi e staff medico.
4. Allenamento settimanale a calendario (giorni, carichi, riposo).
5. Clausole rescissorie e prestiti con diritto di riscatto.
6. Osservatori con rapporti a stelle e "nebbia" sui parametri.
7. Notizie e giornale sportivo con voti in pagella.
8. Rivalità e derby con premi extra e pressione.
9. Obiettivi stagionali della dirigenza (salvezza, Europa, scudetto).
10. Premi individuali (capocannoniere, miglior giovane, allenatore del mese).
11. Storico carriera dell'allenatore con trofei e grafico.
12. Mercato con finestre estiva/invernale e ultimo giorno.
13. Agenti dei giocatori e commissioni.
14. Fair play finanziario e bilancio mensile.
15. Statistiche avanzate partita (xG, mappa di calore, passaggi).
16. Highlights testuali/2D dei gol rivedibili.
17. Meteo e condizioni del campo che influenzano la gara.
18. Tifoseria: coreografie, abbonamenti, prezzi per settore.
19. Achievement e sfide (es. vinci con squadra di Serie C).
20. Salvataggio automatico e backup cloud/esportazione.

## Domande aperte
- Avatar: generi tu le immagini o preferisci che prepari i prompt da usare con un'IA di immagini?
- Ordine dei lavori: va bene quello sopra?
- GitHub Actions: sbloccare il pagamento per tornare a compilare l'APK.

Modello: per implementare basta Sonnet; Opus solo per bilanciamento/progettazione.
