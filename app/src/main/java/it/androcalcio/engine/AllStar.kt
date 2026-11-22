package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team

/**
 * Squadre All Star di ogni decennio, per le amichevoli: nomi storpiati e comici (nessun nome reale).
 * Ogni rosa: 2 portieri, 5 difensori, 4 centrocampisti, 3 attaccanti.
 */
object AllStar {
    const val NAZIONE = "All Star"
    const val CAMPIONATO = "Leggende"

    data class Decennio(val anno: Int, val sigla: String, val colore1: Long, val colore2: Long, val rosa: List<Voce>) {
        val nome get() = "All Star Anni '${sigla.drop(1)}"
    }
    /** ruolo P/D/C/A, nome storpiato, nazionalità. */
    data class Voce(val ruolo: String, val nome: String, val nazione: String)

    private fun r(s: String): List<Voce> = s.split(";").map {
        val p = it.split("|"); Voce(p[0], p[1], p[2])
    }

    val DECENNI: List<Decennio> = listOf(
        Decennio(1950, "A50", 0xFF5D4037, 0xFFFFD54F, r(
            "P|Lev Iascino|Russia;P|Gilmar dos Pesci|Brasile;D|Nilton Santoss|Brasile;D|Giacinto Fachetto|Italia;D|Djalma Santoso|Brasile;D|Bobby Mooro|Inghilterra;D|Horst Eckelmann|Germania;" +
            "C|Raymond Koppa|Francia;C|Didi Valdivio|Brasile;C|Luis Suarenz|Spagna;C|Omar Sivora|Argentina;A|Ferenc Puskasso|Ungheria;A|Alfredo Di Stefanio|Argentina;A|Garrinchio|Brasile")),
        Decennio(1960, "A60", 0xFF1565C0, 0xFFFFFFFF, r(
            "P|Gordon Bankz|Inghilterra;P|Dino Zoffa|Italia;D|Giacinto Faccheto|Italia;D|Carlos Alberto Torrese|Brasile;D|Bobby Muro|Inghilterra;D|Tarcisio Burgnich|Italia;D|Karl Schnellinger|Germania;" +
            "C|Gianni Riviera|Italia;C|Bobby Carlton|Inghilterra;C|Gerd Mulleri|Germania;C|Luisito Suarez|Spagna;A|Eusebio da Silvia|Portogallo;A|Pelè Pelezzi|Brasile;A|Gigi Riva Rivano|Italia")),
        Decennio(1970, "A70", 0xFFEF6C00, 0xFF212121, r(
            "P|Sepp Maiero|Germania;P|Ricardo Zamora Junior|Spagna;D|Franz Beckenbauro|Germania;D|Ruud Krolls|Olanda;D|Claudio Gentile Gentilo|Italia;D|Gaetano Scirea Scirea|Italia;D|Berti Vogtz|Germania;" +
            "C|Johan Cruiff|Olanda;C|Johan Neeskens Neeskens|Olanda;C|Gianni Riverino|Italia;C|Marco Tardello|Italia;A|Kevin Kigan|Inghilterra;A|Paolo Rosso Rossi|Italia;A|Gerd Mullero|Germania")),
        Decennio(1980, "A80", 0xFF2E7D32, 0xFFFFEB3B, r(
            "P|Dino Zoffo|Italia;P|Jean Pfaffo|Belgio;D|Franco Barezzi|Italia;D|Paolo Maldino Senior|Italia;D|Andreas Bremmo|Germania;D|Claudio Gentilini|Italia;D|Mauro Tassotti Tassotto|Italia;" +
            "C|Michele Platinì|Francia;C|Dieco Maradonna|Argentina;C|Zico Zicone|Brasile;C|Karl-Heinz Rummenicco|Germania;A|Marco Van Bastone|Olanda;A|Gary Linekero|Inghilterra;A|Hugo Sanchez Sanchizzo|Messico")),
        Decennio(1990, "A90", 0xFFC62828, 0xFF000000, r(
            "P|Peter Schmeichello|Danimarca;P|Gianluca Pagliucchi|Italia;D|Paolo Maldino|Italia;D|Franco Baresino|Italia;D|Alessandro Nestino|Italia;D|Roberto Carlos Carlossi|Brasile;D|Lilian Thuramo|Francia;" +
            "C|Zinedine Zidanè|Francia;C|Roberto Baggino|Italia;C|Paolo Scholsi|Inghilterra;C|Rivaldo Rivaldino|Brasile;A|Ronaldão Nazario|Brasile;A|Gabriele Batistuto|Argentina;A|Alessandro Del Pierino|Italia")),
        Decennio(2000, "A00", 0xFF6A1B9A, 0xFFFFFFFF, r(
            "P|Gigi Buffone|Italia;P|Iker Casillo|Spagna;D|Fabio Cannavarro Cannavaro|Italia;D|Carles Puyolo|Spagna;D|Paolo Maldini Maldino|Italia;D|Philippe Lahmo|Germania;D|Alessandro Nesta Nestoni|Italia;" +
            "C|Andrea Pirlino|Italia;C|Xavi Ernandez|Spagna;C|Ronaldinho Gauchino|Brasile;C|Steven Gerrardo|Inghilterra;A|Thierry Enri|Francia;A|Raul Gonzalez Gonzalo|Spagna;A|Francesco Tottino|Italia")),
        Decennio(2010, "A10", 0xFF00838F, 0xFFFFCDD2, r(
            "P|Manuel Neuero|Germania;P|Thibaut Courtoise|Belgio;D|Sergio Ramosso|Spagna;D|Gerard Piquè|Spagna;D|Giorgio Chiellino|Italia;D|Dani Alvesso|Brasile;D|Marcelo Marcello|Brasile;" +
            "C|Andrea Iniesto|Spagna;C|Luka Modrico|Croazia;C|Toni Kroos Krusa|Germania;C|N'Golo Cantè|Francia;A|Lionel Messo|Argentina;A|Cristiano Ronaldò|Portogallo;A|Luis Suarezz|Uruguay")),
        Decennio(2020, "A20", 0xFF283593, 0xFFFFD600, r(
            "P|Alisson Beckero|Brasile;P|Gianluigi Donnarummo|Italia;D|Virgil Van Dijko|Olanda;D|Achraf Hakimo|Marocco;D|Alessandro Bastoni Bastone|Italia;D|Ruben Diaz Dias|Portogallo;D|Theo Hernandezz|Francia;" +
            "C|Kevin De Bruino|Belgio;C|Jude Bellinghamo|Inghilterra;C|Nicolò Barellino|Italia;C|Rodri Rodrigo|Spagna;A|Kylian Mbappè Mbappino|Francia;A|Erling Haalando|Norvegia;A|Vinicius Juniorr|Brasile"))
    )

    /** Giocatore dalle statistiche coerenti con ruolo e valore: i migliori della lista hanno 90, gli ultimi 84. */
    fun giocatore(v: Voce, idx: Int, teamId: Long): Player {
        val ov = (90 - idx * 6 / 13).coerceIn(84, 91)
        val ruolo = v.ruolo
        val j = (v.nome.hashCode() and 0x7) - 3
        fun s(x: Int) = (x + j).coerceIn(30, 99)
        val pos = when (ruolo) {
            "P" -> "POR"
            "D" -> listOf("DC", "DC", "TD", "TS", "DC")[idx % 5]
            "C" -> listOf("MED", "REG", "ES", "MP")[idx % 4]
            else -> listOf("CA", "CA", "MP")[idx % 3]
        }
        return Player(
            teamId = teamId, name = v.nome, role = ruolo, age = 28, overall = ov, morale = 90, salary = 0L,
            nationality = v.nazione, potential = ov, contract = 1,
            ve = s(ov - 8), re = s(ov - 6), ag = s(60), qu = s(ov), fo = 95,
            gm = if (ruolo == "P") s(ov) else 20, en = if (ruolo == "D") s(ov) else s(ov - 22),
            pa = s(if (ruolo == "C") ov else ov - 12), dr = s(if (ruolo == "A") ov else ov - 20),
            fi = s(if (ruolo == "A") ov else ov - 30), ti = s(if (ruolo == "A" || ruolo == "C") ov - 4 else ov - 25),
            ri = s(ov - 15), foot = if (idx % 3 == 0) "S" else "D", pos = pos
        )
    }

    fun rosa(d: Decennio, teamId: Long): List<Player> = d.rosa.mapIndexed { i, v -> giocatore(v, i, teamId) }

    fun squadra(d: Decennio) = Team(
        name = d.nome, shortName = d.sigla, colorPrimary = d.colore1 and 0xFFFFFFFFL, colorSecondary = d.colore2 and 0xFFFFFFFFL,
        budget = 0L, stadiumCapacity = 40_000, ticketPrice = 40, sponsor = "Gloria Eterna",
        nazione = NAZIONE, campionato = CAMPIONATO, youthCoachSkill = 50, scoutSkill = 50
    )

    fun isAllStar(t: Team) = t.nazione == NAZIONE
}
