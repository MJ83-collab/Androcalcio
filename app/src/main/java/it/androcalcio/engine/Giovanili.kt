package it.androcalcio.engine

import it.androcalcio.data.SeedData
import it.androcalcio.data.Team
import it.androcalcio.data.Youth
import kotlin.random.Random

/**
 * Settore giovanile: ragazzi da 10 a 16 anni che crescono di anno in anno.
 * A 17 anni è l'ultima stagione utile: o li promuovi in prima squadra o a 18 lasciano il vivaio.
 */
object Giovanili {
    const val ETA_MIN = 10
    const val ETA_MAX = 16
    const val ETA_ULTIMA = 17
    const val ETA_APPARIZIONE = 15   // da questa età può aggregarsi alla prima squadra per una partita

    /** Overall tipico di un ragazzo di quell'età con quel potenziale: i più piccoli partono da più lontano. */
    fun overallAllEta(pot: Int, eta: Int, rng: Random): Int {
        val gap = ((19 - eta) * 3.2).toInt() + rng.nextInt(-4, 5)
        return (pot - gap).coerceIn(25, 80)
    }

    fun genera(team: Team, season: String, n: Int, etaMin: Int, etaMax: Int, rng: Random = Random.Default, bonusVivaio: Int = 0): List<Youth> {
        val q = (team.budget / 1_000_000).toInt().coerceIn(1, 45)
        val nat = team.nazione.ifEmpty { "Italia" }
        val ruoli = listOf("P", "D", "D", "D", "C", "C", "C", "A", "A", "D", "C", "A")
        val vivaio = (team.youthCoachSkill - Staffing.NEUTRAL) / 10
        return List(n) { i ->
            val eta = rng.nextInt(etaMin, etaMax + 1)
            val pot = (52 + q / 2 + vivaio + bonusVivaio + rng.nextInt(-8, 25)).coerceIn(45, 95)
            val nazY = if (rng.nextInt(100) < 85) nat else SeedData.codes.keys.random(rng)
            Youth(
                teamId = team.id, season = season,
                name = Nomi.genera(rng, nazY),
                role = ruoli[i % ruoli.size], age = eta, overall = overallAllEta(pot, eta, rng), potential = pot,
                nationality = nazY
            )
        }
    }

    /** Prima volta: una rosa del settore giovanile con tutte le età da 10 a 16. */
    fun iniziale(team: Team, season: String, rng: Random = Random.Default, bonusVivaio: Int = 0): List<Youth> =
        genera(team, season, 14, ETA_MIN, ETA_MAX, rng, bonusVivaio)

    /** Ogni stagione arriva una nuova leva di piccoli. */
    fun nuovaLeva(team: Team, season: String, rng: Random = Random.Default, bonusVivaio: Int = 0): List<Youth> =
        genera(team, season, 5, ETA_MIN, ETA_MIN + 2, rng, bonusVivaio)

    /** Un anno di crescita: l'allenatore delle giovanili e le strutture aiutano. */
    fun cresci(y: Youth, rng: Random, coachSkill: Int, bonusVivaio: Int = 0): Youth {
        val anniRimasti = (19 - y.age).coerceAtLeast(1)
        val base = (y.potential - y.overall).toDouble() / anniRimasti * (0.8 + rng.nextDouble() * 0.5)
        val aiuto = (coachSkill - Staffing.NEUTRAL) / 50.0 + bonusVivaio / 10.0
        val delta = Math.round(base + aiuto).toInt().coerceIn(0, (y.potential - y.overall).coerceAtLeast(0))
        return y.copy(age = y.age + 1, overall = y.overall + delta)
    }

    fun scaduto(y: Youth) = y.age > ETA_ULTIMA
    fun puoDebuttare(y: Youth) = y.age >= ETA_APPARIZIONE

    data class Scheda(val titolo: String, val righe: List<String>, val consiglio: String)

    /** Scheda dell'allenatore delle giovanili: progressi, giudizio e consiglio. ovPrima = overall un anno fa (o all'ultima scheda). */
    fun scheda(y: Youth, ovPrima: Int, coachSkill: Int): Scheda {
        val delta = y.overall - ovPrima
        val pot = Youths.perceived(y, coachSkill)
        val tendenza = when {
            delta >= 6 -> "crescita fortissima"
            delta >= 3 -> "buona crescita"
            delta >= 1 -> "crescita lenta"
            else -> "fermo"
        }
        val consiglio = when {
            y.age >= ETA_ULTIMA && y.overall >= 55 -> "È l'ultimo anno: promuovilo, o lascerà il vivaio."
            y.age >= ETA_ULTIMA -> "Ultimo anno e livello insufficiente: meglio liberarlo."
            y.age >= ETA_APPARIZIONE && y.overall >= 60 -> "Pronto per un'apparizione in prima squadra."
            y.overall >= 70 -> "Livello alto per la sua età: tienilo d'occhio."
            pot >= 80 -> "Potenziale importante: lascialo maturare."
            pot < 55 -> "Potenziale scarso: difficilmente arriverà."
            else -> "Continua il percorso nelle giovanili."
        }
        return Scheda(
            "${y.name} · ${y.age} anni · ${y.role}",
            listOf(
                "Livello: ${Youths.judge(y.overall)} (${y.overall})",
                "Un anno fa: $ovPrima · ${if (delta >= 0) "+" else ""}$delta · $tendenza",
                "Potenziale secondo l'allenatore: ${Youths.judge(pot)}",
                "Nazionalità: ${y.nationality}"
            ),
            consiglio
        )
    }

    fun encodeApparizioni(l: List<Pair<Long, Long>>) = l.joinToString(";") { "${it.first}:${it.second}" }
    fun decodeApparizioni(s: String?): List<Pair<Long, Long>> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":")
        if (p.size == 2) (p[0].toLongOrNull() ?: return@mapNotNull null) to (p[1].toLongOrNull() ?: return@mapNotNull null) else null
    }
}
