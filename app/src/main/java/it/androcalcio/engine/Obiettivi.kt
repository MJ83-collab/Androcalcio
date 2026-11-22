package it.androcalcio.engine

/** Obiettivi stagionali della dirigenza: fissati a inizio stagione in base alla forza, verificati a fine stagione. */
object Obiettivi {
    enum class Tipo(val nome: String) {
        SALVEZZA("Salvezza"), META_CLASSIFICA("Metà classifica"), ZONA_ALTA("Zona alta"), TITOLO("Vincere il campionato")
    }

    data class Obiettivo(val tipo: Tipo, val posizioneMax: Int, val bonus: Long, val penale: Int)

    /** pos = posizione attesa per forza (1 = la più forte), squadre = numero di squadre del campionato. */
    fun fissa(pos: Int, squadre: Int, bonusBase: Long): Obiettivo {
        val n = squadre.coerceAtLeast(2)
        val frac = (pos - 1).toDouble() / (n - 1)
        return when {
            frac <= 0.10 -> Obiettivo(Tipo.TITOLO, 1, bonusBase * 3, 12)
            frac <= 0.35 -> Obiettivo(Tipo.ZONA_ALTA, (n * 0.25).toInt().coerceAtLeast(2), bonusBase * 2, 8)
            frac <= 0.70 -> Obiettivo(Tipo.META_CLASSIFICA, (n * 0.55).toInt().coerceAtLeast(3), bonusBase, 5)
            else -> Obiettivo(Tipo.SALVEZZA, (n * 0.75).toInt().coerceAtLeast(4), bonusBase / 2, 4)
        }
    }

    fun raggiunto(o: Obiettivo, posizioneFinale: Int) = posizioneFinale <= o.posizioneMax

    /** Variazione di fiducia dirigenza: bonus se raggiunto, penale se mancato; più forte se si supera di molto. */
    fun fiducia(o: Obiettivo, posizioneFinale: Int): Int =
        if (raggiunto(o, posizioneFinale)) 4 + (o.posizioneMax - posizioneFinale).coerceAtMost(4) else -o.penale

    fun encode(o: Obiettivo) = "${o.tipo.name},${o.posizioneMax},${o.bonus},${o.penale}"
    fun decode(s: String?): Obiettivo? {
        val p = s.orEmpty().split(",")
        if (p.size != 4) return null
        val t = Tipo.values().firstOrNull { it.name == p[0] } ?: return null
        return Obiettivo(t, p[1].toIntOrNull() ?: return null, p[2].toLongOrNull() ?: return null, p[3].toIntOrNull() ?: return null)
    }
}
