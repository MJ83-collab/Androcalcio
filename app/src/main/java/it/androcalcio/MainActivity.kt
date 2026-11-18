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
        AC.tema = prefs.getInt("tema", if (prefs.getBoolean("tema_pc", false)) 1 else 0)
        setContent {
            AndrocalcioTheme {
                AndrocalcioApp()
            }
        }
    }
}
