package it.androcalcio.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoseFisseTest {

    /** Squadre nell'ordine di assets/squadre.json (ogni oggetto ha nazione, campionato e squadra in quest'ordine). */
    private val squadre: List<RoseFisse.Squadra> by lazy {
        val testo = File("src/main/assets/squadre.json").readText()
        val regex = Regex("\"nazione\": \"([^\"]*)\",\\s*\"campionato\": \"([^\"]*)\",\\s*\"squadra\": \"([^\"]*)\"")
        val trovate = regex.findAll(testo).map {
            RoseFisse.Squadra(it.groupValues[1], it.groupValues[2], it.groupValues[3])
        }.toList()
        assertEquals("Il regex deve leggere tutte le squadre del file", testo.split("\"squadra\":").size - 1, trovate.size)
        trovate
    }

    @Test
    fun generazioneEDeterministica() {
        val prima = RoseFisse.toCsv(RoseFisse.genera(squadre))
        val seconda = RoseFisse.toCsv(RoseFisse.genera(squadre))
        assertEquals(prima, seconda)
    }

    @Test
    fun csvSiRileggeUguale() {
        val csv = RoseFisse.toCsv(RoseFisse.genera(squadre))
        assertEquals(csv, RoseFisse.toCsv(RoseFisse.fromCsv(csv)))
    }

    /**
     * Scrive il CSV in app/build/rose_fisse.csv (la CI lo carica come artifact).
     * Se assets/rose_fisse.csv esiste, le rose devono coincidere: così un cambio al generatore
     * non altera le rose congelate per sbaglio.
     */
    @Test
    fun rosaCoincideConIlFileCongelato() {
        val csv = RoseFisse.toCsv(RoseFisse.genera(squadre))
        File("build/rose_fisse.csv").apply { parentFile.mkdirs(); writeText(csv) }
        val congelato = File("src/main/assets/rose_fisse.csv")
        if (congelato.exists()) {
            assertEquals(
                "Le rose generate non coincidono con assets/rose_fisse.csv: rigenera il file dall'artifact",
                congelato.readText(), csv
            )
        } else {
            // Primo congelamento: il CSV finisce nel log della CI tra i due marcatori
            println("ROSE_FISSE_INIZIO")
            print(csv)
            println("ROSE_FISSE_FINE")
        }
        assertTrue(csv.lines().size > 500)
    }
}
