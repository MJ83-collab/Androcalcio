package it.androcalcio.engine

import kotlin.random.Random

/** Il "social" del club: dopo ogni partita tifosi, giornalisti e presidente commentano, con l'ironia del calcio di oggi. */
object Social {
    fun post(mia: String, avv: String, gf: Int, gs: Int, derby: Boolean, migliore: String?, arbitro: String?, rng: Random): List<String> {
        val v = gf > gs; val p = gf == gs
        val l = mutableListOf<String>()
        l += when {
            v && derby -> "@CurvaViva: DERBY NOSTRO. Oggi non si lavora, domani nemmeno. #${mia.filter { it.isLetter() }}"
            v -> "@CurvaViva: ${gf}-${gs}! Dopo tre mesi di sofferenza ci siamo ripresi. Perdoniamo anche la formazione."
            p -> "@CurvaViva: ${gf}-${gs}. Un punto, un po' di rabbia e un panino al bar. Come sempre."
            derby -> "@CurvaViva: Derby perso. Chiudo l'app. Torno tra cinque minuti."
            else -> "@CurvaViva: ${gf}-${gs}. Chiedo scusa a mia moglie per ciò che ho urlato davanti alla TV."
        }
        l += if (migliore != null) "@PagellaPazza: $migliore migliore in campo, voto spedito dal telefono, rigorosamente non a caso."
            else "@PagellaPazza: Oggi voti a sorpresa. Nessuno ha capito perché."
        l += when {
            v -> "@PresidenteOfficiale: Orgoglio per il gruppo. Il progetto è sulla strada giusta (la strada è quella di sempre)."
            p -> "@PresidenteOfficiale: Fiducia assoluta nel mister. Confermiamo. Per ora."
            else -> "@PresidenteOfficiale: Analizzeremo la situazione con calma e un team di consulenti che ancora non ha un nome."
        }
        l += when (rng.nextInt(4)) {
            0 -> "@FonteVicinaAlClub: Si parla di un nuovo allenatore in arrivo. Anche se non c'è alcun motivo. Ma si parla."
            1 -> "@TalkShowDelLunedi: Cinque ospiti, tre opinioni, zero idee. Domani rivediamo il contatto al rallentatore."
            2 -> "@Tifoso_Cinico: Se vinciamo il merito è dei giocatori, se perdiamo è colpa del mister. Fin qui la mia dottrina."
            else -> "@Bomber2000: Il calcio moderno è bello ma una volta almeno c'era la bandierina sul pallone."
        }
        if (arbitro != null) l += "@FischioFacile: $arbitro oggi ha fischiato ${if (rng.nextBoolean()) "troppo" else "troppo poco"}. Cosa sia successo in realtà lo sapremo dal VAR."
        return l
    }

    fun encode(l: List<String>) = l.joinToString("§")
    fun decode(s: String?): List<String> = s.orEmpty().split("§").filter { it.isNotBlank() }
}
