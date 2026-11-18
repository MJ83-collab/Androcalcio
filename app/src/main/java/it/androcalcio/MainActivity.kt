package it.androcalcio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import it.androcalcio.ui.AndrocalcioApp
import it.androcalcio.ui.theme.AndrocalcioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndrocalcioTheme {
                AndrocalcioApp()
            }
        }
    }
}
