package it.androcalcio.data

/**
 * Dati di esempio inventati (nomi fittizi). Inseriti solo al primo avvio,
 * quando il database è vuoto. Da sostituire con il database reale del progetto.
 */
object SeedData {

    suspend fun insertInto(dao: AndrocalcioDao) {
        val teamIds = dao.insertTeams(
            listOf(
                Team(
                    name = "Lombardia Calcio", shortName = "LOM",
                    colorPrimary = 0xFF1E5AA8L, colorSecondary = 0xFFFFFFFFL,
                    budget = 12_000_000, stadiumCapacity = 18_000,
                    ticketPrice = 18, sponsor = "Banca Fittizia"
                ),
                Team(
                    name = "Riviera Sport", shortName = "RIV",
                    colorPrimary = 0xFFC8102EL, colorSecondary = 0xFF000000L,
                    budget = 9_500_000, stadiumCapacity = 12_000,
                    ticketPrice = 15, sponsor = "Assicurazioni Esempio"
                )
            )
        )

        dao.insertPlayers(
            listOf(
                Player(teamId = teamIds[0], name = "M. Rossi", role = "P", age = 29, overall = 71, morale = 70, salary = 300_000, nationality = "ITA"),
                Player(teamId = teamIds[0], name = "L. Bianchi", role = "D", age = 24, overall = 68, morale = 65, salary = 220_000, nationality = "ITA"),
                Player(teamId = teamIds[0], name = "G. Verdi", role = "C", age = 31, overall = 75, morale = 80, salary = 450_000, nationality = "ITA"),
                Player(teamId = teamIds[0], name = "A. Neri", role = "A", age = 22, overall = 70, morale = 60, salary = 180_000, nationality = "ARG"),
                Player(teamId = teamIds[1], name = "S. Costa", role = "P", age = 33, overall = 69, morale = 75, salary = 260_000, nationality = "ITA"),
                Player(teamId = teamIds[1], name = "R. Ferri", role = "D", age = 27, overall = 73, morale = 70, salary = 340_000, nationality = "ESP"),
                Player(teamId = teamIds[1], name = "P. Greco", role = "C", age = 26, overall = 72, morale = 68, salary = 290_000, nationality = "ITA"),
                Player(teamId = teamIds[1], name = "D. Russo", role = "A", age = 19, overall = 64, morale = 85, salary = 90_000, nationality = "BRA")
            )
        )
    }
}
