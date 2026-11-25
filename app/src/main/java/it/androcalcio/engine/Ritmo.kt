package it.androcalcio.engine

/** Ritmo di squadra: più veloce = più occasioni ma più fatica; più lento = meno fatica, meno attacco. */
enum class Ritmo(val label: String, val attacco: Double, val consumo: Double) {
    LENTO("Lento", 0.96, 0.85),
    NORMALE("Normale", 1.0, 1.0),
    VELOCE("Veloce", 1.04, 1.2)
}
