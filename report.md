# Androcalcio — report (22/11/2026) · versione 0.3.0

Repo `MJ83-collab/Androcalcio`, ramo `feat/pc7-meccaniche` = `main`. Kotlin 2.0 / Compose / Room (DB v24, nessuna migrazione nuova: gli extra stanno nella tabella Stato).
**Test motore: 92/92 ok. Build GitHub Actions: verde.** Non ho potuto provare l'app su un telefono: la parte grafica è verificata solo dalla compilazione.

## Le 20 migliorie approvate (tutte fatte)
| # | Cosa | Dove nell'app | Note |
|---|------|---------------|------|
| 1 | Conferenze stampa | Stampa | 8 domande, 3 risposte con effetto su morale, tifosi, fiducia; una a giornata |
| 2 | Colloqui individuali | Stampa | Elogio / Rassicura / Rimprovero, effetto diverso secondo il morale; uno a giornata per giocatore |
| 3 | Psicologo | Stampa | 3 livelli, recupera morale a chi è sotto 60 a ogni partita; costo annuo |
| 4 | Addetto marketing + 10 sponsor | Marketing | 10 tipi (maglia, tecnico, stadio, abbigliamento, manica, banca, media, retro, bevande, pantaloncini); 3 livelli di addetto: più offerte e compensi più alti |
| 5 | Vivaio 10–16 anni | Vivaio | Crescono ogni anno; scheda dei progressi su richiesta; "Prova" (apparizione) o "Promuovi"; a 18 anni chi non è promosso lascia il vivaio |
| 6 | Nazionali Under 21 | Nazionali | Solo IA: calendario proprio (giornate 8, 14, 32) ed Europeo U21 negli anni dispari |
| 7 | All Star per decennio | Amichevoli (nazione "All Star") | 8 squadre (anni '50–2020), 14 giocatori ciascuna con nomi storpiati; accettano sempre |
| 8 | Schede collegate | ovunque | Stemma/nome squadra → scheda squadra; bandiera/nazionalità → scheda nazione; nomi → scheda giocatore |
| 9 | Difficoltà | Opzioni | 5 livelli + Personalizzata (budget, incassi, infortuni, severità dirigenza) |
| 10 | Finestre di mercato | Prestiti | Estiva (giornate 1–3) e invernale (a metà campionato); ultimo giorno: i club accettano più facilmente |
| 11 | Prestiti | Prestiti | Presta o prendi un giocatore fino a fine stagione; riscatto |
| 12 | Agenti | Mercato (firme) | Commissione 2–12% dell'ingaggio per gli anni di contratto |
| 13 | Obiettivi della dirigenza | Obiettivi | Fissati a inizio stagione dalla tua forza; bonus in denaro e fiducia |
| 14 | Premi individuali | Obiettivi | Capocannoniere, miglior giovane, miglior portiere, giocatore e allenatore dell'anno (a fine stagione) |
| 15 | Giornale sportivo | Giornale | Titolo, sommario e pagelle dell'ultima partita |
| 16 | Derby | Partite | Esistevano già (coppie in `Rivalita`); ora i derby vinti contano nei trofei e nel giornale |
| 17 | Meteo e campo | Giornale / partite | 7 tempi, 5 stati del campo; ibrido e riscaldamento aiutano; cambia le occasioni da gol |
| 18 | Statistiche avanzate | Giornale | Tiri, xG stimato, rendimento attacco/difesa, possesso, mappa di calore dei tiri |
| 19 | Prezzi per settore | Marketing | 4 settori, prezzo 50–200%; elasticità; effetto sugli incassi di fine stagione |
| 20 | Achievement | Obiettivi | 29 trofei che si sbloccano da soli |

## Correzione importante
Nell'ultimo giro (stadio e centro) gli **incassi extra e la manutenzione delle migliorie comparivano nel bilancio ma non entravano nel budget**. Ora entrano davvero (insieme agli extra nuovi: prezzi per settore, bonus obiettivo, psicologo, marketing, difficoltà).

## Limiti noti (onesti)
- **Avatar: fatti in codice** (non immagini). 13 elementi (14 tagli, 4 forme viso, 6 occhi, barba, occhiali, extra, 10 colori giacca/capelli/sfondo, 8 carnagioni, 5 colletti): milioni di combinazioni. Il volto nasce dal nome; toccando un avatar (scheda giocatore, staff, osservatori) si apre l'editor: selettori, "Casuale", "Dal nome", "Ripristina". Le modifiche sono salvate ed entrano in export/reset. Non è lo stile Sakamoto Days vero (servirebbero illustrazioni): è un fumetto a contorni neri. Restano senza avatar liste e vivaio.
- Difficoltà: il parametro "infortuni" sopra il 100% aggiunge infortuni solo alla tua squadra; sotto il 100% ne cura qualcuno subito.
- Under 21: la tua nazionale personale resta solo senior.
- "Allenatore del mese" è diventato "Allenatore dell'anno" (premio a fine stagione).
- Le squadre All Star vivono nel database come nazione "All Star": escluse da mercato, nazionali e fine stagione, ma visibili in Squadre.
- Salvataggi vecchi: gli extra partono da zero; l'obiettivo si fissa alla prima apertura. Per vedere i nuovi equilibri economici conviene una nuova carriera.
- Rimasti da prima: `Insurance.ROUNDS=38` fisso, `wipe` non svuota l'albo, `androcalcio.zip` nella radice, README minimo.

## Prossimi passi
1. Mostrare gli avatar anche nel vivaio e nelle liste.
2. Giocare qualche stagione per tarare economia, prezzi per settore e sponsor.
3. Opzionale: release GitHub v0.3.0 con l'APK; chiudere la PR #1 e cancellare il ramo `feat/pc7-numeri-campo` (azione tua).

Modello: per proseguire basta Sonnet (default). Opus solo per bilanciare l'economia o progettare le nuove meccaniche.
