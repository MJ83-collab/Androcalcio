package it.androcalcio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import it.androcalcio.ui.AndrocalcioApp
import it.androcalcio.ui.theme.AC
import it.androcalcio.ui.theme.AndrocalcioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("androcalcio", 0)
        AC.pcCalcio = prefs.getBoolean("tema_pc", false)
        setContent {
            AndrocalcioTheme {
                AndrocalcioApp()
            }
        }
    }
}
