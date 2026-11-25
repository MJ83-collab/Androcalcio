package it.androcalcio.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import it.androcalcio.ui.theme.AC

/**
 * Da 1 a 5 stelle con la mezza stella. [mezze] va da 0 a 10 (2 = una stella intera, 1 = mezza stella):
 * stelle piene piene, la mezza riempita solo a sinistra, le altre vuote.
 */
@Composable
fun Stelle(mezze: Int, dimensione: TextUnit = 14.sp, colore: Color = AC.Accent, modifier: Modifier = Modifier) {
    val m = mezze.coerceIn(0, 10)
    Row(modifier) {
        for (i in 0 until 5) {
            Box {
                Text("☆", fontSize = dimensione, color = colore)
                when {
                    m >= 2 * (i + 1) -> Text("★", fontSize = dimensione, color = colore)
                    m == 2 * i + 1 -> Text("★", fontSize = dimensione, color = colore, modifier = Modifier.clipToBounds().drawWithContent {
                        clipRect(left = 0f, top = 0f, right = size.width / 2f, bottom = size.height) { this@drawWithContent.drawContent() }
                    })
                }
            }
        }
    }
}
