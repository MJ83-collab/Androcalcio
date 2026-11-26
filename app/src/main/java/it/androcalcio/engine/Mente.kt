package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Mentalità del giocatore: sei valori 1..99. La base nasce dal nome e dal carattere (sempre la stessa);
 * le modifiche (psicologo, editor) si salvano come differenze in Stato "mente".
 */
object Mente {
    enum class Attr(val sigla: String, val nome: String, val descrizione: String) {
        DET("DE", "Determinazione", "Non molla mai: reagisce agli svantaggi e cresce di più in allenamento."),
        LEA("LD", "Leadership", "Guida i compagni: tiene alto il clima dello spogliatoio."),
        FRE("FR", "Freddezza", "Regge la pressione: perde meno morale dopo le sconfitte."),
        PRO("PR", "Professionalità", "Si allena bene, rispetta le regole, non crea casi."),
        AUT("AU", "Autocontrollo", "Evita liti con i compagni e proteste."),
        SQU("SQ", "Spirito di squadra", "Mette il gruppo prima di sé: alza l'affiatamento.")
    }

    const val KEY = "mente"
    val N = Attr.values().size

    /** Valori di base, fissi per giocatore: il carattere sposta i valori tipici. */
    fun base(p: Player): IntArray = base(p.name, p.nationality, p.age)

    fun base(nome: String, naz: String, eta: Int): IntArray {
        val tipo = Personalita.tipo(nome, naz)
        val maturita = ((eta - 22) / 2).coerceIn(0, 6)
        return IntArray(N) { i ->
            val h = Math.floorMod(nome.hashCode() * 131 + naz.hashCode() * 7 + i * 977, 51)
            val bias = when (tipo) {
                Personalita.Tipo.LEADER -> intArrayOf(5, 15, 5, 0, 0, 5)
                Personalita.Tipo.PROFESSIONISTA -> intArrayOf(0, 0, 8, 15, 8, 0)
                Personalita.Tipo.AMBIZIOSO -> intArrayOf(15, 0, 0, 5, -5, -8)
                Personalita.Tipo.MERCENARIO -> intArrayOf(0, -5, 5, 0, 0, -15)
                Personalita.Tipo.SENSIBILE -> intArrayOf(0, -8, -15, 5, 0, 8)
                Personalita.Tipo.FEDELE -> intArrayOf(5, 0, 0, 5, 5, 15)
            }[i]
            val eta = if (i == Attr.LEA.ordinal || i == Attr.PRO.ordinal || i == Attr.AUT.ordinal) maturita else 0
            (30 + h + bias + eta).coerceIn(1, 99)
        }
    }

    fun valori(p: Player, delta: Map<Long, IntArray>): IntArray {
        val b = base(p); val d = delta[p.id] ?: return b
        return IntArray(N) { (b[it] + d.getOrElse(it) { 0 }).coerceIn(1, 99) }
    }

    fun valore(p: Player, delta: Map<Long, IntArray>, a: Attr): Int = valori(p, delta)[a.ordinal]

    /** Nuove differenze che portano il giocatore ai valori assoluti dati. */
    fun deltaPer(p: Player, assoluti: IntArray): IntArray { val b = base(p); return IntArray(N) { assoluti[it].coerceIn(1, 99) - b[it] } }

    fun aggiungi(delta: Map<Long, IntArray>, p: Player, a: Attr, quanto: Int): Map<Long, IntArray> {
        val d = (delta[p.id] ?: IntArray(N)).copyOf()
        val nuovo = (base(p)[a.ordinal] + d[a.ordinal] + quanto).coerceIn(1, 99)
        d[a.ordinal] = nuovo - base(p)[a.ordinal]
        return delta + (p.id to d)
    }

    fun encode(m: Map<Long, IntArray>): String =
        m.filterValues { v -> v.any { it != 0 } }.entries.joinToString(";") { (k, v) -> "$k:" + v.joinToString(",") }

    fun decode(s: String?): Map<Long, IntArray> = s.orEmpty().split(";").mapNotNull {
        val kv = it.split(":"); if (kv.size != 2) return@mapNotNull null
        val id = kv[0].toLongOrNull() ?: return@mapNotNull null
        val v = kv[1].split(",").map { x -> x.toIntOrNull() ?: 0 }
        id to IntArray(N) { i -> v.getOrElse(i) { 0 } }
    }.toMap()

    fun giudizio(v: Int) = when { v >= 80 -> "Ottima"; v >= 65 -> "Buona"; v >= 45 -> "Nella media"; v >= 30 -> "Scarsa"; else -> "Pessima" }

    /** Affiatamento della squadra 0..100: spirito di squadra e autocontrollo dei giocatori, più il clima. */
    fun affiatamento(rosa: List<Player>, delta: Map<Long, IntArray>, clima: Int): Int {
        if (rosa.isEmpty()) return 50
        val v = rosa.map { valori(it, delta) }
        val sq = v.map { it[Attr.SQU.ordinal] }.average()
        val au = v.map { it[Attr.AUT.ordinal] }.average()
        val leader = v.maxOf { it[Attr.LEA.ordinal] }
        return (sq * 0.5 + au * 0.2 + clima * 0.2 + leader * 0.1).toInt().coerceIn(0, 100)
    }

    /** Correzione del morale dopo una partita: la freddezza attenua le cadute, la determinazione aiuta a rialzarsi. */
    fun correggiMorale(d: Int, v: IntArray): Int = when {
        d < 0 -> Math.round(d * (1.5 - v[Attr.FRE.ordinal] / 100.0)).toInt()
        d > 0 && v[Attr.DET.ordinal] >= 70 -> d + 1
        else -> d
    }
}
