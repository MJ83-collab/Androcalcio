package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/**
 * Psicologo dello staff: scopre i problemi della rosa (singoli, tra compagni, di gruppo),
 * li affronta con sedute a pagamento, lavora sulla mentalità e scrive un report mensile.
 * Più è bravo, più problemi vede, più spesso le sedute riescono e più precisi sono i pareri.
 */
object Psicologo {
    const val KEY_CD = "psi_cd"          // chiave problema o "lav_<id>" -> giorno dell'ultimo intervento
    const val KEY_REPORT = "psi_report"   // ultimi report separati da §
    const val KEY_MESE = "psi_mese"
    const val ATTESA_SEDUTA = 7
    const val ATTESA_LAVORO = 14

    enum class Tipo(val nome: String, val azione: String, val icona: String) {
        MORALE("Morale a terra", "Colloquio di sostegno", "😞"),
        PANCHINA("Scontento per la panchina", "Colloquio sul ruolo", "🪑"),
        PRESSIONE("Ansia da prestazione", "Tecniche di gestione dello stress", "😰"),
        INGAGGIO("Si sente sottopagato", "Colloquio sulle motivazioni", "💶"),
        CONFLITTO("Attrito tra compagni", "Mediazione", "⚡"),
        GRUPPO("Spogliatoio diviso", "Seduta di gruppo", "👥")
    }

    data class Problema(val chiave: String, val tipo: Tipo, val ids: List<Long>, val gravita: Int, val testo: String)

    fun livello(skill: Int?): Int = when { skill == null -> 0; skill >= 75 -> 3; skill >= 55 -> 2; else -> 1 }

    /** Quanti problemi vede: un esperto li scopre quasi tutti, uno scarso solo i più evidenti. */
    fun visibili(skill: Int): Int = (1 + skill / 14).coerceIn(2, 8)

    fun problemi(rosa: List<Player>, delta: Map<Long, IntArray>, clima: Int): List<Problema> {
        val out = mutableListOf<Problema>()
        val haTitolari = rosa.any { it.starter }
        fun v(p: Player) = Mente.valori(p, delta)
        for (p in rosa) {
            val m = v(p)
            val tipo = Personalita.tipo(p)
            when {
                p.morale < 45 -> out += Problema("mor_${p.id}", Tipo.MORALE, listOf(p.id), if (p.morale < 30) 3 else 2,
                    "${p.name} ha il morale a ${p.morale}: rende meno e rischia di chiedere la cessione.")
                haTitolari && tipo == Personalita.Tipo.AMBIZIOSO && !p.starter && p.injuredRounds == 0 && p.morale < 65 ->
                    out += Problema("pan_${p.id}", Tipo.PANCHINA, listOf(p.id), 2,
                        "${p.name} (ambizioso) non accetta la panchina: vuole spazio o andrà via.")
                m[Mente.Attr.FRE.ordinal] < 40 && p.fo < 65 -> out += Problema("pre_${p.id}", Tipo.PRESSIONE, listOf(p.id), 1,
                    "${p.name} sente troppo la pressione (freddezza ${m[Mente.Attr.FRE.ordinal]}): forma in calo.")
                tipo == Personalita.Tipo.MERCENARIO && p.salary * 10 < Personalita.ingaggioGiusto(p) * 7 ->
                    out += Problema("ing_${p.id}", Tipo.INGAGGIO, listOf(p.id), 1,
                        "${p.name} pensa di valere di più: senza attenzioni chiederà un aumento.")
            }
        }
        // Attriti: stesso ruolo, uno titolare e l'altro no, poco autocontrollo; oppure due leader forti nello stesso reparto
        val conflitti = mutableListOf<Problema>()
        val lista = rosa.filter { it.injuredRounds == 0 }
        for (i in lista.indices) for (j in i + 1 until lista.size) {
            val a = lista[i]; val b = lista[j]
            val va = v(a); val vb = v(b)
            val au = Mente.Attr.AUT.ordinal; val ld = Mente.Attr.LEA.ordinal
            if (a.pos.isNotEmpty() && a.pos == b.pos && a.starter != b.starter && va[au] < 45 && vb[au] < 45)
                conflitti += Problema("con_${minOf(a.id, b.id)}_${maxOf(a.id, b.id)}", Tipo.CONFLITTO, listOf(a.id, b.id), 2,
                    "${a.name} e ${b.name} si contendono il posto (${a.pos}) e non si sopportano.")
            else if (a.role == b.role && va[ld] >= 75 && vb[ld] >= 75)
                conflitti += Problema("con_${minOf(a.id, b.id)}_${maxOf(a.id, b.id)}", Tipo.CONFLITTO, listOf(a.id, b.id), 1,
                    "${a.name} e ${b.name}: due leader nello stesso reparto, il gruppo si divide.")
        }
        out += conflitti.sortedByDescending { it.gravita }.take(3)
        val aff = Mente.affiatamento(rosa, delta, clima)
        if (clima < 40 || aff < 45) out += Problema("gruppo", Tipo.GRUPPO, emptyList(), if (clima < 30) 3 else 2,
            "Spogliatoio diviso: clima $clima, affiatamento $aff. Il rendimento collettivo ne risente.")
        return out.sortedByDescending { it.gravita }
    }

    fun costoSeduta(skill: Int, tipo: Tipo): Long = (if (tipo == Tipo.GRUPPO) 60_000L else 20_000L) + skill * 400L
    fun costoLavoro(skill: Int): Long = 40_000L + skill * 800L
    fun probabilita(skill: Int, extra: Int = 0): Int = (35 + skill / 2 + extra).coerceIn(20, 92)

    data class Esito(val riuscito: Boolean, val morale: Map<Long, Int>, val mente: List<Triple<Long, Mente.Attr, Int>>, val clima: Int, val testo: String)

    /** Esito di una seduta sul problema. */
    fun seduta(pb: Problema, rosa: List<Player>, skill: Int, rng: Random): Esito {
        val ok = rng.nextInt(100) < probabilita(skill)
        val forza = 1 + skill / 40
        val nomi = rosa.associate { it.id to it.name }
        val chi = pb.ids.joinToString(" e ") { nomi[it] ?: "?" }
        if (!ok) return Esito(false, pb.ids.associateWith { -2 }, emptyList(), if (pb.tipo == Tipo.GRUPPO) -1 else 0,
            "${pb.tipo.azione}: non è andata bene ($chi). Riprova tra qualche giorno.")
        return when (pb.tipo) {
            Tipo.MORALE -> Esito(true, pb.ids.associateWith { 8 + forza * 3 }, pb.ids.map { Triple(it, Mente.Attr.DET, forza) }, 0,
                "$chi ritrova fiducia: morale in salita.")
            Tipo.PANCHINA -> Esito(true, pb.ids.associateWith { 10 }, pb.ids.map { Triple(it, Mente.Attr.PRO, forza) }, 1,
                "$chi accetta il suo ruolo e si mette a disposizione.")
            Tipo.PRESSIONE -> Esito(true, pb.ids.associateWith { 6 }, pb.ids.map { Triple(it, Mente.Attr.FRE, forza + 1) }, 0,
                "$chi impara a gestire la tensione: più freddezza.")
            Tipo.INGAGGIO -> Esito(true, pb.ids.associateWith { 6 }, pb.ids.map { Triple(it, Mente.Attr.SQU, forza) }, 0,
                "$chi rimette il gruppo davanti ai soldi, per ora.")
            Tipo.CONFLITTO -> Esito(true, pb.ids.associateWith { 5 }, pb.ids.map { Triple(it, Mente.Attr.AUT, forza + 1) }, 2,
                "Pace fatta tra $chi: più autocontrollo e clima migliore.")
            Tipo.GRUPPO -> Esito(true, rosa.associate { it.id to 3 }, rosa.map { Triple(it.id, Mente.Attr.SQU, 1) }, 5 + forza,
                "Seduta di gruppo riuscita: lo spogliatoio è più unito.")
        }
    }

    /** Lavoro individuale su un valore mentale: riesce più spesso con un professionista. */
    fun lavoro(valori: IntArray, a: Mente.Attr, skill: Int, rng: Random): Int {
        val extra = (valori[Mente.Attr.PRO.ordinal] - 50) / 5 - (valori[a.ordinal] - 60).coerceAtLeast(0) / 3
        if (rng.nextInt(100) >= probabilita(skill, extra)) return 0
        return 1 + rng.nextInt(1 + skill / 35)
    }

    /** Parere su qualsiasi giocatore (tuo, di altre squadre o del vivaio). conoscenza < 50: solo indizi. */
    fun parere(nome: String, valori: IntArray, tipo: Personalita.Tipo, conoscenza: Int, skill: Int?): List<String> {
        if (skill == null) return listOf("Nessuno psicologo nello staff: assumilo per avere un parere.")
        if (conoscenza < Conoscenza.MIN_CARATTERE) return listOf("Lo conosciamo troppo poco: fallo osservare dagli scout.")
        val err = ((100 - skill) / 8).coerceAtLeast(1)
        val visti = valori.map { (it + (nome.hashCode() % err)).coerceIn(1, 99) }
        val forti = Mente.Attr.values().filter { visti[it.ordinal] >= 70 }.joinToString { it.nome.lowercase() }
        val deboli = Mente.Attr.values().filter { visti[it.ordinal] < 40 }.joinToString { it.nome.lowercase() }
        val out = mutableListOf("Carattere: ${tipo.nome}. ${tipo.descrizione}")
        if (forti.isNotEmpty()) out += "Punti di forza: $forti."
        if (deboli.isNotEmpty()) out += "Da seguire: $deboli."
        if (forti.isEmpty() && deboli.isEmpty()) out += "Profilo equilibrato, nessun eccesso."
        out += when {
            visti[Mente.Attr.AUT.ordinal] < 35 -> "Rischio: può litigare con i compagni."
            visti[Mente.Attr.FRE.ordinal] < 35 -> "Rischio: soffre le partite importanti."
            visti[Mente.Attr.LEA.ordinal] >= 80 -> "Potenziale capitano."
            else -> "Nessun rischio particolare."
        }
        return out
    }

    /** Report: chi sta male, cosa si è fatto, consigli. */
    fun report(chi: String, periodo: String, rosa: List<Player>, delta: Map<Long, IntArray>, clima: Int,
               visti: List<Problema>, interventi: Int): String {
        val aff = Mente.affiatamento(rosa, delta, clima)
        val morale = rosa.map { it.morale }.ifEmpty { listOf(0) }.average().toInt()
        val capitano = rosa.maxByOrNull { Mente.valore(it, delta, Mente.Attr.LEA) }
        val fragile = rosa.minByOrNull { Mente.valore(it, delta, Mente.Attr.FRE) }
        val righe = mutableListOf(
            "🧠 Report dello psicologo $chi · $periodo",
            "Morale medio $morale · clima $clima · affiatamento $aff · interventi $interventi.")
        righe += if (visti.isEmpty()) "Nessun problema rilevato." else "Problemi: " + visti.take(4).joinToString(" · ") { "${it.tipo.icona} ${it.testo}" }
        capitano?.let { righe += "Leader naturale: ${it.name}." }
        fragile?.let { righe += "Da proteggere: ${it.name} (soffre la pressione)." }
        righe += when {
            aff < 45 -> "Consiglio: seduta di gruppo subito."
            visti.any { it.tipo == Tipo.CONFLITTO } -> "Consiglio: mediazione tra i giocatori in attrito."
            morale < 55 -> "Consiglio: colloqui individuali con i più giù."
            else -> "Consiglio: lavorare sulla mentalità dei giovani."
        }
        return righe.joinToString("\n")
    }

    fun encodeCd(m: Map<String, Long>) = m.entries.joinToString(";") { "${it.key}=${it.value}" }
    fun decodeCd(s: String?): Map<String, Long> = s.orEmpty().split(";").mapNotNull {
        val kv = it.split("="); if (kv.size == 2) kv[0] to (kv[1].toLongOrNull() ?: return@mapNotNull null) else null
    }.toMap()
}
