# Androcalcio — report (23/11/2026) · v0.5.0

Kotlin 2.0 / Compose / Room (DB v24, **nessuna migrazione**; extra in tabella Stato).
Test e build GitHub Actions verdi (debug e release R8). ⚠️ Non provato su telefono.

## Le 20 migliorie approvate: 16 nuove, 4 già presenti
| # | Miglioria | Dove | Stato |
|---|---|---|---|
| 1 | Debito → fiducia del presidente: sopra 15M taglia il 3% dell'eccedenza a fine stagione | Finanze | nuova |
| 2 | Rifinanziamento: sposti un mutuo su una banca più economica (penale 2%) | Finanze | nuova |
| 3 | Premio sponsor: +40k a vittoria, +50k senza subire gol | automatico | nuova |
| 4 | Bilancio previsto a fine stagione | Società → Analisi | nuova |
| 5 | Acquisti a rate: metà subito, metà a fine stagione | Finanze | nuova |
| 6 | Ampiezza (stretta/normale/larga) nel motore di gioco | Tattica | nuova |
| 7 | Rigoristi e piazzati | Tattica | **già presente** |
| 8 | Cambio programmato al 60', 70' o 80' | Tattica | nuova |
| 9 | Statistiche di fine partita | Partita | **già presenti** |
| 10 | Precedenti con la prossima avversaria | Analisi | nuova |
| 11 | Capitano scelto da te | scheda giocatore | nuova |
| 12 | Allenamento individuale | scheda giocatore | nuova |
| 13 | Riposo automatico (cambia i titolari stanchi) | Analisi | nuova |
| 14 | Preferiti (seguiti) | scheda giocatore, Analisi | nuova |
| 15 | Confronto tra due giocatori | Analisi | nuova |
| 16 | Obiettivi del presidente | — | **già presente** |
| 17 | Tifosi e incassi | — | **già presente** |
| 18 | Voci di mercato (club che seguono i tuoi giocatori) | Analisi | nuova |
| 19 | Backup su file (esporta/importa slot in .zip) | Salvataggi | nuova |
| 20 | Tema nero puro (meno batteria su OLED) | Opzioni | nuova |
Totale: **16 nuove** e **4 già presenti** (7, 9, 16, 17: verificati nel codice). Ammetto che avrei dovuto controllarlo prima di proporle.

## Dettagli utili
- **Backup su file**: l'importazione controlla che sia un backup di Androcalcio e della stessa versione dello schema; poi premi "Carica" sullo slot.
- **Ampiezza**: stretta = più possesso e difesa solida; larga = più occasioni ma più spazi dietro.
- **Cambio programmato**: usa la logica del cambio automatico (esce il più stanco, entra il migliore dello stesso ruolo).
- **Allenamento individuale** e **capitano** sono letti dal motore tramite valori globali (`Training.individuali`, `Leadership.scelto`), riletti all'avvio.

## Limiti noti
- Non provato su telefono; l'APK ottimizzato (R8) non è stato avviato.
- Nuovi tassi/penali/premi (3%, 2%, 40k) non tarati con partite lunghe.
- Il confronto mostra solo giocatori della tua rosa e dei seguiti.
- La vendita a rate vale solo per gli acquisti, non per le cessioni.
- Tema: il vero chiaro non c'è; c'è il nero.
- Restano: `Insurance.ROUNDS=38`, `wipe` non svuota l'albo, avatar disegnati in codice.

**Modello:** Sonnet basta; Opus solo per tarare l'economia.

---
## Cronologia
- v0.3.0 (22/11): 20 migliorie approvate (conferenze, sponsor, vivaio 10–16, nazionali U21, All Star, difficoltà, prestiti, giornale, meteo, achievement…).
- 23/11 (ramo `feat/pc7-vivai-extra`): avatar ovunque, tabellone TV, formazione con volti, scouting nei vivai, Società (disciplina, personalità, scambi, fair play, storia, ricerca), tutorial, autosalvataggio.
- v0.4.0: mutui, batteria/prestazioni, Analisi, pulizia.
- v0.5.0 (questo report): 16 nuove migliorie (vedi tabella).
