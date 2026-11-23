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
## Cronologia
- v0.3.0 (22/11): 20 migliorie approvate (conferenze, sponsor, vivaio 10–16, nazionali U21, All Star, difficoltà, prestiti, giornale, meteo, achievement…).
- 23/11 (ramo `feat/pc7-vivai-extra`): avatar ovunque, tabellone TV, formazione con volti, scouting nei vivai, Società (disciplina, personalità, scambi, fair play, storia, ricerca), tutorial, autosalvataggio.
- v0.4.0 (questo report): mutui, batteria/prestazioni, Analisi, pulizia.
