package it.androcalcio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import it.androcalcio.ui.AndrocalcioApp
import it.androcalcio.ui.theme.AC
import it.androcalcio.ui.theme.AndrocalcioTheme

class MainActivity : ComponentActivity() {
    private val vm: AndrocalcioViewModel by viewModels()

    /** In background la partita live si ferma da sola: niente CPU né batteria consumate a schermo spento. */
    override fun onStop() {
        super.onStop()
        vm.setLivePaused(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // v0.7: una volta sola si torna al tema PC Calcio 7; poi la scelta in Opzioni vale (manga resta disponibile)
        val pr = getSharedPreferences("androcalcio", 0)
        if (!pr.getBoolean("tema_v07", false)) pr.edit().putInt("tema_scelto", AC.PREFERENZA_PREDEFINITA).putBoolean("tema_nero", false).putBoolean("tema_v07", true).apply()
        AC.tema = AC.daPreferenza(pr.getInt("tema_scelto", if (pr.getBoolean("tema_nero", false)) 1 else AC.PREFERENZA_PREDEFINITA))
        setContent {
            AndrocalcioTheme {
                AndrocalcioApp()
            }
        }
    }
}
