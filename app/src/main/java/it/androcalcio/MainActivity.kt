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
        // Tema unico: PC Calcio 7 (le preferenze salvate sul tema vengono ignorate).
        AC.tema = 3
        setContent {
            AndrocalcioTheme {
                AndrocalcioApp()
            }
        }
    }
}
