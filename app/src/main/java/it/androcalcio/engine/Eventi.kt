package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/** Effetto di una scelta: morale dei giocatori coinvolti, clima, fiducia dirigenza, tifosi, condizione rosa. */
data class Scelta(val label: String, val morale: Int = 0, val clima: Int = 0, val fiducia: Int = 0,
                  val tifosi: Int = 0, val condizione: Int = 0, val esito: String = "")

data class Evento(val titolo: String, val testo: String, val giocatori: List<Long>, val scelte: List<Scelta>)

/**
 * Spogliatoio e interazioni alla PC Calcio/FM.
 * Ogni giornata: clima e tifosi si muovono col risultato; il clima sposta il morale di tutti;
 * con probabilità ~35% arriva un evento con 2-3 scelte.
 */
object Spogliatoio {
    /** Variazioni dopo una partita della tua squadra: gf/gs = gol fatti/subiti. */
    fun dopoPartita(gf: Int, gs: Int): Pair<Int, Int> = when {   // (clima, tifosi)
        gf > gs -> 2 to (if (gf - gs >= 3) 3 else 2)
        gf < gs -> -2 to (if (gs - gf >= 3) -4 else -2)
        else -> 0 to -1
    }

    /** Spinta del clima sul morale di tutti: clima 80 = +1 a giornata, 30 = -2. */
    fun spintaMorale(clima: Int) = (clima - 60) / 15

    fun evento(rosa: List<Player>, clima: Int, fiducia: Int, tifosi: Int, perso: Boolean,
               haPreparatore: Boolean, rng: Random): Evento? {
        if (rosa.size < 4 || rng.nextInt(100) >= 35) return null
        val panchina = rosa.filter { !it.starter && it.overall >= rosa.maxOf { p -> p.overall } - 8 && it.injuredRounds == 0 }
        val tipi = buildList {
            if (panchina.isNotEmpty()) add(0)
            add(1); add(2)
            if (haPreparatore && rosa.count { it.condition < 75 } >= 3) add(3)
            if (perso && tifosi < 50) add(4)
            if (fiducia < 40) add(5)
            if (clima < 45) add(6)
        }
        val a = rosa.random(rng)
        val b = (rosa - a).random(rng)
        if (rng.nextInt(100) < 45) return imprevisto(a.name, b.name, rng)
        return when (tipi.random(rng)) {
            0 -> panchina.random(rng).let { p -> Evento("Spazio in squadra",
                "${p.name} (${p.overall}) chiede di giocare di più.", listOf(p.id), listOf(
                Scelta("Gli prometto spazio", morale = 12, clima = 1, esito = "${p.name} è motivato."),
                Scelta("Deve meritarselo", morale = -8, clima = 1, esito = "Il gruppo apprezza la fermezza."),
                Scelta("Può cercarsi un'altra squadra", morale = -20, clima = -3, esito = "${p.name} è furioso."))) }
            1 -> Evento("Lite in allenamento", "${a.name} e ${b.name} sono venuti alle mani.", listOf(a.id, b.id), listOf(
                Scelta("Multa a entrambi", morale = -6, clima = 3, esito = "Disciplina ristabilita."),
                Scelta("Li faccio chiarire", morale = 3, clima = 1, esito = "Si sono stretti la mano."),
                Scelta("Lascio correre", clima = -5, esito = "Lo spogliatoio si divide.")))
            2 -> Evento("Intesa in campo", "${a.name} e ${b.name} si trovano a meraviglia.", listOf(a.id, b.id), listOf(
                Scelta("Complimenti pubblici", morale = 6, clima = 2, esito = "Morale alle stelle."),
                Scelta("Restiamo concentrati", morale = 2, clima = 1, esito = "Ricevuto, mister.")))
            3 -> Evento("Il preparatore avverte", "Molti giocatori sono stanchi: propone un giorno di riposo.", emptyList(), listOf(
                Scelta("Giorno di riposo", condizione = 10, clima = 2, esito = "La squadra recupera."),
                Scelta("Si lavora lo stesso", condizione = -3, clima = -2, esito = "Qualcuno brontola.")))
            4 -> Evento("Contestazione", "I tifosi contestano dopo la sconfitta.", emptyList(), listOf(
                Scelta("Vado a parlarci", tifosi = 6, fiducia = 1, esito = "Applausi alla fine."),
                Scelta("Difendo i giocatori", tifosi = -2, clima = 4, esito = "La squadra si compatta."),
                Scelta("Nessun commento", tifosi = -5, esito = "La curva fischia.")))
            5 -> Evento("Il presidente chiama", "La dirigenza è preoccupata dai risultati.", emptyList(), listOf(
                Scelta("Prometto una svolta", fiducia = 5, clima = -1, esito = "Ti danno tempo, ma ora devi vincere."),
                Scelta("Chiedo pazienza", fiducia = -2, clima = 2, esito = "Il presidente non è convinto.")))
            else -> Evento("Spogliatoio teso", "Il capitano dice che il gruppo è sfiduciato.", emptyList(), listOf(
                Scelta("Cena di squadra", morale = 4, clima = 6, esito = "Serata riuscita."),
                Scelta("Allenamenti più duri", clima = -3, condizione = -2, esito = "Musi lunghi."),
                Scelta("Ci parla il vice", clima = 3, esito = "Il clima migliora un po'.")))
        }
    }

    /** Imprevisti positivi e negativi, con l'ironia di chi ha visto troppo calcio moderno. */
    private fun imprevisto(a: String, b: String, rng: Random): Evento = when (rng.nextInt(24)) {
        0 -> Evento("Il procuratore chiama", "Il procuratore di $a: \"Ha tante offerte. Anche una in Arabia. Sono sempre in Arabia.\"", emptyList(), listOf(
            Scelta("Il contratto vale ancora", morale = -3, clima = 1, esito = "Il procuratore sbuffa: commissione a rischio."),
            Scelta("Facciamo un premio", morale = 8, fiducia = -2, esito = "Il procuratore sorride. Il presidente no.")))
        1 -> Evento("Foto sui social", "$a ha postato una storia in discoteca la sera prima del match. Filtro incluso.", emptyList(), listOf(
            Scelta("Multa e fine", morale = -4, clima = 3, esito = "Storia cancellata, dignità no."),
            Scelta("Faccio finta di niente", clima = -3, tifosi = -2, esito = "I tifosi hanno fatto lo screenshot."),
            Scelta("Mi faccio un selfie anch'io", morale = 4, tifosi = 2, esito = "Diventi virale. Non è chiaro se sia un bene.")))
        2 -> Evento("Tifosi in ritiro", "Cinquecento tifosi applaudono all'allenamento. Qualcuno ha portato i panini.", emptyList(), listOf(
            Scelta("Autografi per tutti", tifosi = 6, morale = 3, condizione = -2, esito = "Serata lunga, entusiasmo altissimo."),
            Scelta("Saluto e si lavora", tifosi = 2, esito = "Professionali, un po' freddi.")))
        3 -> Evento("Premio di sponsor", "Uno sponsor tecnico regala scarpini nuovi. Color fluo, ovviamente.", emptyList(), listOf(
            Scelta("Li usiamo tutti", morale = 5, tifosi = 1, esito = "Squadra visibile anche di notte."),
            Scelta("Ognuno sceglie i suoi", morale = 2, clima = 1, esito = "Libertà di scarpino.")))
        4 -> Evento("Conferenza stampa", "Un giornalista chiede: \"Mister, è un progetto o una serie di scommesse?\"", emptyList(), listOf(
            Scelta("\"È un progetto triennale\"", fiducia = 3, tifosi = -1, esito = "Il presidente annuisce senza ascoltare."),
            Scelta("\"Una partita alla volta\"", clima = 1, esito = "Frase vuota, perfetta per i titoli."),
            Scelta("\"Domanda interessante\"", fiducia = -1, tifosi = 2, esito = "Applausi ironici in sala stampa.")))
        5 -> Evento("Polemica arbitrale", "Il solito talk show analizza per 40 minuti un contatto di gioco al rallentatore.", emptyList(), listOf(
            Scelta("Parlo di campo", clima = 2, fiducia = 1, esito = "Sei l'unico a farlo."),
            Scelta("Attacco la classe arbitrale", tifosi = 5, fiducia = -3, esito = "I tifosi ti adorano, la Federazione meno.")))
        6 -> Evento("Giornalista dell'ultima ora", "\"Fonti vicine al club\" annunciano il tuo esonero. La fonte è un cugino del magazziniere.", emptyList(), listOf(
            Scelta("Smentisco con ironia", tifosi = 3, clima = 1, esito = "La smentita fa più clic della notizia."),
            Scelta("Ignoro", clima = -1, esito = "Il cugino sale di grado.")))
        7 -> Evento("Idea del presidente", "Il presidente vuole una maglia speciale per l'anniversario. Con il QR code sul colletto.", emptyList(), listOf(
            Scelta("Va benissimo", fiducia = 3, tifosi = -2, esito = "Il merchandising ringrazia, i tifosi sospirano."),
            Scelta("Tradizione prima di tutto", fiducia = -2, tifosi = 3, esito = "La curva intona un coro per te.")))
        8 -> Evento("Giornata di sole", "Il giardiniere ha fatto un prato perfetto. Sembra una moquette di lusso.", emptyList(), listOf(
            Scelta("Allenamento extra sul campo", morale = 4, condizione = -2, esito = "Tutti vogliono giocare qui."),
            Scelta("Giornata di relax in giardino", morale = 3, condizione = 6, clima = 2, esito = "Lo spogliatoio recupera, il giardiniere piange.")))
        9 -> Evento("Torneo di play station", "$a e $b si sfidano ai videogiochi: in campo non hanno mai giocato così bene insieme.", listOf(), listOf(
            Scelta("Organizzo un torneo di squadra", morale = 6, clima = 5, esito = "Vince il magazziniere."),
            Scelta("Meno schermi, più campo", clima = -1, condizione = 2, esito = "Qualcuno protesta, poi corre.")))
        10 -> Evento("Pasta party", "Il cuoco sbaglia la cottura. La pasta è un mattone: la squadra vota se mangiarla.", emptyList(), listOf(
            Scelta("Mangiamola comunque", morale = 2, condizione = -3, esito = "Eroi, ma poco digeriti."),
            Scelta("Ordino pizza", morale = 5, clima = 2, esito = "Moral boost immediato.")))
        11 -> Evento("Cori goliardici", "Dopo la vittoria dello spogliatoio parte un coro scritto da $a. Rima: pessima. Entusiasmo: altissimo.", emptyList(), listOf(
            Scelta("Cantiamo tutti", morale = 5, clima = 5, esito = "Un nuovo inno interno è nato."),
            Scelta("Per favore, basta", clima = -1, esito = "$a è deluso ma resta autore.")))
        12 -> Evento("Il drone", "Un drone sorvola il campo di allenamento. Sulle ali la scritta \"Studio tattiche\". Scopri che è dei bambini della scuola calcio.", emptyList(), listOf(
            Scelta("Lo faccio atterrare e firmo", tifosi = 4, morale = 2, esito = "Foto con i piccoli tifosi, tutti felici."),
            Scelta("Chiamo la sicurezza", tifosi = -2, esito = "Hai perso una giornata di pubblicità gratis.")))
        13 -> Evento("Offerta di una web tv", "Una piattaforma vuole un documentario sullo spogliatoio: \"autentico, ma con la regia\".", emptyList(), listOf(
            Scelta("Accetto", tifosi = 4, fiducia = 2, clima = -2, esito = "Ottimi ascolti, qualche battuta di troppo."),
            Scelta("Rifiuto", clima = 3, esito = "Lo spogliatoio rimane lo spogliatoio.")))
        14 -> Evento("Premio fair play", "La società ritira un premio fair play per aver dimenticato di protestare con l'arbitro.", emptyList(), listOf(
            Scelta("Lo mostro a tutti", fiducia = 3, tifosi = 2, esito = "Targa in bacheca, accanto a quella dei tifosi."),
            Scelta("Lo metto nello sgabuzzino", morale = 1, esito = "Il magazziniere lo usa come fermaporta.")))
        16 -> Evento("$a chiede di partire", "$a vuole più spazio, o meglio più stipendio, o meglio entrambe. Il procuratore è già al telefono.", emptyList(), listOf(
            Scelta("Parliamone: ti do minuti", morale = 6, clima = -1, esito = "Per ora si calma, vedremo."),
            Scelta("Resti e basta", morale = -6, clima = 2, fiducia = 1, esito = "Muso lungo, ma nessuno lo nota."),
            Scelta("Lo metto sul mercato", morale = -3, clima = -2, esito = "Il gruppo capisce che la società fa sul serio.")))
        17 -> Evento("Promessa mantenuta?", "Avevi promesso a $b più minuti. Se ne è accorto, e se n'è accorto anche il suo procuratore.", emptyList(), listOf(
            Scelta("Lo faccio giocare", morale = 7, clima = 1, esito = "Un patto è un patto."),
            Scelta("Spiego le mie scelte", morale = 2, clima = 1, esito = "Capisce, a metà."),
            Scelta("Finta di niente", morale = -5, clima = -2, esito = "Alla prossima promessa non ci crederà.")))
        18 -> Evento("Il presidente megalomane", "Il presidente annuncia in tv: \"Puntiamo alla Champions!\". Non hai neanche il portiere di riserva.", emptyList(), listOf(
            Scelta("Ci credo anch'io", tifosi = 4, fiducia = 2, clima = -2, esito = "Aspettative alle stelle, pressione pure."),
            Scelta("Abbasso i toni", tifosi = -2, fiducia = -2, clima = 2, esito = "Realista, ma poco social."),
            Scelta("Sorrido e mi taccio", fiducia = 1, esito = "Strategia da veterano.")))
        19 -> Evento("Il presidente tirchio", "Il presidente taglia le trasferte: \"Torniamo in treno, che fa bene al gruppo\".", emptyList(), listOf(
            Scelta("Va bene, viaggiamo uniti", clima = 5, condizione = -3, fiducia = 2, esito = "Il treno diventa uno spogliatoio mobile."),
            Scelta("Protesto", clima = -2, fiducia = -3, condizione = 3, esito = "Si vola, ma l'umore al presidente scende.")))
        20 -> Evento("Riunione con il direttore sportivo", "Il DS propone di puntare su un giovane misterioso: \"Ho una fonte in Sudamerica\". La fonte è sua cognata.", emptyList(), listOf(
            Scelta("Fidiamoci", morale = 2, fiducia = 2, esito = "Si vedrà. Intanto il DS è felice."),
            Scelta("Chiedo i video", fiducia = 1, clima = 1, esito = "I video sono girati con un telefono vecchio.")))
        21 -> Evento("Ritiro in una località esclusiva", "La società propone un weekend di ritiro in una spa sul lago. \"Per cementare il gruppo\".", emptyList(), listOf(
            Scelta("Accetto", morale = 4, condizione = 6, clima = 4, fiducia = -1, esito = "Squadra rilassata, conto molto lungo."),
            Scelta("Meglio il campo", clima = 1, condizione = 1, fiducia = 1, esito = "Nessuno si diverte, ma il presidente risparmia.")))
        22 -> Evento("Invito a un'amichevole", "Una squadra importante chiede un'amichevole con grande incasso. In piena stagione.", emptyList(), listOf(
            Scelta("Accetto", tifosi = 4, fiducia = 3, condizione = -6, esito = "Casa piena, gambe pesanti."),
            Scelta("Declino", condizione = 2, fiducia = -2, tifosi = -1, esito = "Il presidente borbotta, i giocatori recuperano.")))
        23 -> Evento("Il senatore dello spogliatoio", "Il veterano chiede di riunire il gruppo prima del match: \"Parlo io, mister\".", emptyList(), listOf(
            Scelta("Lascialo parlare", morale = 4, clima = 5, esito = "Discorso emozionante. Qualcuno piange."),
            Scelta("Parlo io", clima = 2, fiducia = 1, esito = "Rispetto, ma meno trasporto."),
            Scelta("Nessuno parla", clima = -1, esito = "Silenzio teso: cinque minuti di nulla.")))
        else -> Evento("Cambio di moda", "Un giocatore si presenta con la sciarpa in estate. \"È un look\".", emptyList(), listOf(
            Scelta("Nessun problema", morale = 3, clima = 1, esito = "La moda scalda, in effetti."),
            Scelta("Regole sull'abbigliamento", clima = -1, fiducia = 1, esito = "Il fashion look è bocciato.")))
    }
}
