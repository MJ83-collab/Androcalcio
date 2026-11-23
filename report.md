# Androcalcio — report (23/11/2026) · v0.4.0

Kotlin 2.0 / Compose / Room (DB v24, **nessuna migrazione**: gli extra stanno come testo nella tabella Stato).
**Test motore e build GitHub Actions: verdi** (anche APK release con R8).
⚠️ Non ho potuto provare l'app su un telefono: grafica e comportamento in release (R8) sono verificati solo da compilazione e test.

## 1. Mutui bancari (Finanze → "Mutui bancari")
6 banche con tasso, tetto e durata propri; soldi subito nel budget, rata fissa a fine stagione (capitale + interessi).
| Banca | Tasso | Fino a | Anni |
|---|---|---|---|
| Cassa Rurale del Cugino Gigi | 4,0% | 3M | 3 |
| Banca Popolare dello Zio Gaetano | 6,5% | 8M | 5 |
| Credito Cooperativo Pantofola d'Oro | 9,0% | 15M | 4 |
| Monte dei Pegni Svelti | 12,0% | 10M | 3 |
| Istituto Sogni d'Oro & Figli | 7,5% | 20M | 8 |
| Banca dei Fratelli Strozzi | 18,0% | 25M | 2 |
Regole: un mutuo per banca, debito massimo 40M, minimo 500k; estinzione anticipata con penale 2%; se a fine stagione manca il budget si paga il possibile e il resto resta in debito con mora 5%. Gli **interessi** pesano sul fair play, il capitale no. Compaiono nel bilancio di stagione (categorie "Mutui" e "Interessi"). Salvati in Stato (chiave `mutui`), inclusi in export/reset.

## 2. Prestazioni e batteria
- **Eliminato il polling**: 12 flussi interrogavano il database ogni 1,5–3 s anche con l'app ferma. Ora si rileggono solo quando la tabella cambia (`osserva(...)` con `InvalidationTracker`).
- Le schermate smettono di aggiornarsi in background (`collectAsStateWithLifecycle`).
- La partita dal vivo si mette in **pausa** quando l'app va in background.
- **Opzione "Risparmio batteria"** (Opzioni): la partita avanza al massimo 8 volte al secondo.
- **R8 + riduzione risorse** nell'APK release (firmato con chiave di debug: installabile subito).

## 3. Le 20 migliorie (scheda Società → "Analisi", più opzioni)
1 Contratti in scadenza · 2 Infermeria · 3 Giocatori stanchi · 4 Ingaggi per reparto · 5 Età media/media/valore per reparto · 6 Giovani promesse · 7 Forma ultime 5 · 8 Serie (vittorie, imbattibilità, porta inviolata, senza vincere) · 9 Rendimento casa/trasferta · 10 Marcatori del club · 11 Consigli del vice per la prossima partita · 12 Peso ingaggi sul budget ingaggi · 13 Avviso rate dei mutui vs budget · 14 Risparmio batteria · 15 Gol fatti/subiti a partita · 16 Capitano suggerito · 17 Scontenti · 18 Attaccanti a secco · 19 Valore della rosa · 20 Rinnovi consigliati.
Controllato che non esistessero già (a differenza del giro precedente).

## 4. Pulizia
Tolti dalla radice e dal repository: `androcalcio.zip` e la cartella `.gradle/` (cache di build tracciata per errore). `.gitignore` esteso (`.idea`, `local.properties`, `*.apk`, `*.zip`). README riscritto.

## 5. Bug-check
Nuovo codice riletto; la CI compila e passa tutti i test (nuovi: `MutuiTest`, `AnalisiTest`). Corretto durante il lavoro: ramo morto in `Mutui.paga`. Verificato che i nomi tabella usati da `osserva` esistano (un nome errato farebbe crashare).

## 6. Limiti noti
- Non provato su telefono; l'APK release (R8) non è stato avviato: se crasha, usare l'APK debug e segnalarmelo.
- Tassi, tetti e soglie fair play non tarati con partite lunghe.
- Il debito non influisce su fiducia/dirigenza (solo mora e fair play).
- Alcune migliorie sono sola lettura (Analisi mostra, non agisce).
- Avatar ancora disegnati in codice. Restano: `Insurance.ROUNDS=38`, `wipe` non svuota l'albo.

**Modello:** basta Sonnet; Opus solo per tarare l'economia (tassi/soglie) o nuove meccaniche.

---
# Report precedente (22/11/2026)

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
