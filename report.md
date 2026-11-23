# Androcalcio — report (23/11/2026) · ramo `feat/pc7-vivai-extra`

Kotlin 2.0 / Compose / Room (DB v24, **nessuna migrazione nuova**: gli extra stanno come testo nella tabella Stato).
**Test motore: tutti verdi in CI (92+ e i nuovi di `NuoveMeccanicheTest`). Build GitHub Actions: verde** (commit `de9148f`, run 45086065823).
⚠️ Non ho potuto provare l'app su un telefono: grafica e schermate sono verificate solo dalla compilazione.

## 1. Avatar nel vivaio e in tutte le schede
Avatar con volto ora in: vivaio (lista e scheda), scouting, mercato, statistiche, nazionali, prestiti, rosa. Il volto nasce dal nome, come per i giocatori.

## 2. Grafica dai 3 screenshot
- **Tabellone TV** (`TabelloneTV`): punteggio in stile telecronaca blu, al posto del vecchio tabellone nella schermata Partita.
- **Formazione sul campo** (`FormazioneAvatar`): campo verticale con volto e cognome di ogni titolare, nella schermata Tattica.
- **Tabelle a stelle (stile PC Calcio 6.0)** → scouting: stelle per livello e potenziale (stimato, con margine d'errore).

## 3. Scouting anche nei vivai
Nuova scheda **Vivai** in Vivaio: missione dell'osservatore su 4 club × 2 giovani, filtro per ruolo, stelle, "Segui" e "Ingaggia" (prezzo = livello × potenziale × 110, tra 80k e 2,5M; massimo 30 giovani nel tuo vivaio). I vivai delle IA hanno minimo 8 giovani e si rigenerano da soli.

## 4. Le "20 migliorie" — verità
Ammetto un errore: **circa 8 delle 20 che avevo proposto esistevano già** (es. conferenze, psicologo, derby, meteo, prestiti, agenti, giornale, achievement). Ho implementato solo quelle davvero mancanti, nuova schermata **Società**:
| Cosa | Dettaglio |
|---|---|
| Disciplina | Diffida a 4 gialli, squalifica al 5°; rossi 1–3 turni; severità arbitro 1–3; squalificati esclusi dalla formazione |
| Spogliatoio/personalità | 6 tipi (Leader, Professionista, Ambizioso, Mercenario, Sensibile, Fedele); richieste di aumento/cessione (max 3) |
| Scambi | Giocatore + conguaglio; l'IA vuole valore ≥ 105% del tuo; niente scambi di portieri |
| Fair play finanziario | Perdita operativa su 3 anni (esclusi mercato, stadio, multe); soglia 8M + 30% budget; multa 10% dell'eccesso; blocco mercato se eccesso > soglia/4 |
| Storia | Scontri diretti, record di stagione, Hall of Fame (dopo 5 anni, media ≥ 78 o 50 gol), maglia ritirata dopo 8 anni |
| Cerca | Ricerca nella società |
| Autosalvataggio | Slot 4, ogni 4 partite tue; gli slot manuali non lo toccano |
| Dimensione testo | 85/100/115/130% in Opzioni |
| Tutorial | Alla prima apertura e da Opzioni ("Rivedi il tutorial") |

## 5. Controllo bug ("big")
Rilettura del codice + CI. Corretti prima del push finale:
- multa fair play contata due volte nell'operativo dell'anno dopo;
- giocatori "Ambiziosi" che chiedevano la cessione senza che tu avessi scelto i titolari;
- conteggio errato dei squalificati nell'avviso;
- errori di compilazione (`matchParentSize`, `sumOf` ambiguo) e import mancanti.
Morale/personalità sono aggiornati in `dopoMiaPartita` perché `runRounds` sovrascrive i giocatori con una cache.

## 6. Limiti noti
- Non provato su telefono; possibile ritocco di spaziature/dimensioni.
- Soglie fair play e prezzi giovani **non tarati**: serve giocare qualche stagione.
- Non aggiunti: tattica "ampiezza/stile" (pressing, linea, mentalità già presenti), consigli trasferte/turnover.
- L'autosave può fotografare a metà giornata.
- Salvataggi vecchi: gli extra ripartono da zero (meglio nuova carriera).
- Avatar ancora disegnati in codice, non in stile Sakamoto Days.
- Rimasti da prima: `Insurance.ROUNDS=38`, `wipe` non svuota l'albo, `androcalcio.zip` nella radice, README minimo.

## 7. Prossimi passi (decidi tu)
1. Aprire PR di `feat/pc7-vivai-extra` verso `main` e fare merge.
2. Provare l'APK e segnalarmi problemi grafici.
3. Tarare economia/fair play dopo qualche stagione.

**Modello:** basta Sonnet (default). Opus solo per bilanciare l'economia o progettare nuove meccaniche.

---
# Report precedente (22/11/2026, v0.3.0)

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
