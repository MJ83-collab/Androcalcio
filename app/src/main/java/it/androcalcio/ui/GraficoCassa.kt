package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Movimento
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

private val Verde = Color(0xFF3DDC84)
private val Rosso = Color(0xFFFF5C72)

/**
 * Andamento della cassa: linea del saldo movimento per movimento, verde dove sale e rossa dove scende,
 * con la riga tratteggiata dello zero (sotto lo zero il club rischia il fallimento) e il totale di ricavi e costi.
 * [movimenti] arriva dal più recente al più vecchio.
 */
@Composable
fun GraficoCassa(movimenti: List<Movimento>) {
    SectionTitle("Andamento della cassa")
    val m = movimenti.asReversed()
    if (m.size < 2) {
        Text("Servono almeno due movimenti per disegnare il grafico.", color = AC.TextDim, fontSize = 12.sp)
        return
    }
    val saldi = m.map { it.saldo.toDouble() }
    val ricavi = m.filter { it.importo > 0 }.sumOf { it.importo }
    val costi = m.filter { it.importo < 0 }.sumOf { -it.importo }
    val ultimo = saldi.last()
    val trend = ultimo - saldi.first()
    val bene = ultimo > 0 && trend >= 0
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                when {
                    ultimo <= 0 -> "Cassa a zero: rischio di fallimento"
                    trend >= 0 -> "La società sta migliorando"
                    else -> "La cassa sta calando"
                },
                color = if (bene) Verde else Rosso, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ricavi +${money(ricavi)}", color = Verde, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Costi -${money(costi)}", color = Rosso, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Canvas(Modifier.fillMaxWidth().height(140.dp).padding(top = 8.dp)) {
                val lo = minOf(saldi.min(), 0.0)
                val hi = maxOf(saldi.max(), 1.0)
                val span = (hi - lo).coerceAtLeast(1.0)
                fun x(i: Int): Float = size.width * i / (saldi.size - 1)
                fun y(v: Double): Float = (size.height * (1 - (v - lo) / span)).toFloat()
                if (lo < 0.0) {
                    drawLine(
                        Color.White.copy(alpha = 0.5f), Offset(0f, y(0.0)), Offset(size.width, y(0.0)),
                        strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
                for (i in 1 until saldi.size) {
                    drawLine(
                        if (saldi[i] >= saldi[i - 1]) Verde else Rosso,
                        Offset(x(i - 1), y(saldi[i - 1])), Offset(x(i), y(saldi[i])), strokeWidth = 4f
                    )
                }
            }
            Text("Ultimi ${m.size} movimenti · saldo ${money(ultimo.toLong())}", color = AC.TextDim, fontSize = 11.sp)
        }
    }
}
