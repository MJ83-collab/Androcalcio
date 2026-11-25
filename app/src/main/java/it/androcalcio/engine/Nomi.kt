package it.androcalcio.engine

import kotlin.random.Random

/**
 * Nomi dei giocatori generati: cognomi diversi per ogni nazione (prima erano 50 cognomi uguali per tutti).
 * Inventati o molto comuni: nessun riferimento a giocatori reali. Formato "X. Cognome".
 */
object Nomi {
    private val COGNOMI: Map<String, List<String>> = mapOf(
        "Italia" to ("Rossi Bianchi Verdi Neri Costa Ferri Greco Russo Conti Moretti Galli Fontana Serra Pace Leone Sala Longo " +
            "Rinaldi Barone Mariani Colombo Ricci Marino Bruno Gallo Esposito Romano Lombardi Mancini Rizzo Caruso Ferrara Santoro " +
            "Martini Fabbri Bellini Vitale Testa Pellegrini Palumbo Sanna Fiore Farina Monti Cattaneo Grasso Gentile Morelli Ferraro " +
            "Messina Orlando Valentini Silvestri Parisi Bassi Benedetti Marchi Rota Villa Gatti Riva Brambilla Ghezzi Sartori Zanetti " +
            "Bonetti Guidi Caputo Ruggiero Carbone Piras Melis Cocco Pagano Tosi Bertolini Donati Mazza Montanari Negri Basile " +
            "Lucchesi Marchetti Pastore Vaccaro Bernardi Cirillo Amato Gualtieri Zanella Ferretti Tedesco Lazzari Rizzi Fumagalli " +
            "Crippa Bianco Carraro Zanon Dal Pozzo De Luca Di Stefano La Rosa Lo Bello Del Vecchio De Santis Di Marco Mele Barbieri").split(" "),
        "Spagna" to ("Garcia Fernandez Gonzalez Rodriguez Lopez Martinez Sanchez Perez Gomez Martin Jimenez Ruiz Hernandez Diaz " +
            "Moreno Alvarez Romero Navarro Torres Dominguez Vazquez Ramos Gil Ramirez Serrano Blanco Molina Morales Suarez Ortega " +
            "Delgado Castro Ortiz Rubio Marin Sanz Iglesias Nuñez Medina Garrido Cortes Castillo Santos Lozano Guerrero Cano Prieto " +
            "Mendez Cruz Calvo Gallego Herrera Marquez Leon Peña Flores Cabrera Campos Vega Fuentes Carrasco Diez Reyes Caballero").split(" "),
        "Inghilterra" to ("Smith Jones Taylor Brown Williams Wilson Johnson Davies Robinson Wright Thompson Evans Walker White " +
            "Roberts Green Hall Wood Jackson Clarke Hughes Edwards Turner Hill Moore Clark Harrison Scott Cooper Ward Morris King " +
            "Watson Harris Baker Young Allen Mitchell Bennett Bell Kelly Price Cox Lee Bailey Parker Collins Shaw Barnes Fisher " +
            "Webb Holmes Ellis Lloyd Marshall Owen Gibbs Palmer Mason Rhodes Fletcher Sutton Hayes Dixon Lowe Pearce Nash").split(" "),
        "Germania" to ("Müller Schmidt Schneider Fischer Weber Meyer Wagner Becker Schulz Hoffmann Koch Richter Klein Wolf " +
            "Schröder Neumann Schwarz Braun Zimmermann Krüger Hartmann Lange Werner Krause Lehmann Köhler Maier Herrmann Walter " +
            "König Huber Kaiser Fuchs Peters Lang Scholz Möller Weiß Jung Hahn Vogel Friedrich Keller Günther Frank Berger Winkler " +
            "Roth Beck Lorenz Baumann Franke Albrecht Ludwig Brandt Graf Kuhn Pohl Engel Horn Busch Sommer Seidel Haas").split(" "),
        "Francia" to ("Martin Bernard Dubois Thomas Robert Richard Petit Durand Leroy Moreau Simon Laurent Lefebvre Michel " +
            "Garcia David Bertrand Roux Vincent Fournier Morel Girard Andre Mercier Dupont Lambert Bonnet Francois Martinez Legrand " +
            "Garnier Faure Rousseau Blanc Guerin Muller Henry Roussel Nicolas Perrin Morin Mathieu Clement Gauthier Dumont Lopez " +
            "Fontaine Chevalier Robin Masson Sanchez Gerard Nguyen Boyer Denis Lemaire Duval Joly Gautier Roger Roche Roy Noel").split(" "),
        "Brasile" to ("Silva Santos Oliveira Souza Rodrigues Ferreira Alves Pereira Lima Gomes Costa Ribeiro Martins Carvalho " +
            "Almeida Lopes Soares Fernandes Vieira Barbosa Rocha Dias Nascimento Andrade Moreira Nunes Marques Machado Mendes " +
            "Freitas Cardoso Ramos Goncalves Santana Teixeira Araujo Pinto Campos Correia Moura Cavalcanti Batista Monteiro " +
            "Rezende Fonseca Medeiros Tavares Bezerra Farias Peixoto Paiva Siqueira Brito Queiroz Coelho Aguiar Viana").split(" "),
        "Argentina" to ("Gonzalez Rodriguez Gomez Fernandez Lopez Diaz Martinez Perez Garcia Sanchez Romero Sosa Alvarez Torres " +
            "Ruiz Ramirez Flores Acosta Benitez Medina Herrera Suarez Aguirre Gimenez Gutierrez Pereyra Rojas Molina Castro Ortiz " +
            "Silva Nuñez Luna Juarez Cabrera Rios Ferreyra Godoy Morales Dominguez Moreno Peralta Vega Carrizo Quiroga Ledesma " +
            "Ojeda Ponce Vera Cardozo Paz Villalba Figueroa Correa Cáceres Arias Bustos Coronel Mansilla Funes").split(" "),
        "Portogallo" to ("Ferreira Pereira Oliveira Santos Rodrigues Martins Fernandes Goncalves Almeida Ribeiro Pinto Carvalho " +
            "Teixeira Moreira Correia Mendes Nunes Soares Vieira Cardoso Monteiro Sousa Lopes Marques Gomes Rocha Silva Costa Coelho " +
            "Cunha Leite Magalhaes Matos Pires Antunes Barros Tavares Fonseca Rebelo Simoes Faria Neves Loureiro Brandao Morais").split(" "),
        "Olanda" to ("De Boer Visser Smit Meijer De Jong Mulder Bakker Jansen Dekker Vos Kuiper Hoek Brouwer Peters Kok Veenstra " +
            "Willems Hendriks Maas Postma Bos Koster Prins Schouten Van Dijk De Vries Van den Berg Van Leeuwen Dijkstra Vermeulen " +
            "Van der Meer Hoekstra Kramer Scholten Huisman Timmermans Verhoeven Groen Wouters Molenaar Blom Koning").split(" "),
        "Belgio" to ("Peeters Janssens Maes Jacobs Mertens Willems Claes Goossens Wouters De Smet Vermeulen Dubois Lambert Dupont " +
            "Leclercq Renard Lemaire Simon Hermans Aerts Bogaert Desmet Coppens Michiels Verstraete Pauwels Smets Martens Hendrickx " +
            "Van Damme Lejeune Denis Cools Stevens Vandenberghe Claeys Lauwers Segers Thys").split(" "),
        "Croazia" to ("Horvat Kovacevic Babic Maric Juric Novak Kovacic Knezevic Vukovic Markovic Petrovic Matic Tomic Pavlovic " +
            "Kovac Bozic Blazevic Grgic Pavic Radic Peric Lovric Vidovic Perkovic Saric Brkic Jukic Mikulic Barisic Vucic").split(" "),
        "Polonia" to ("Nowak Kowalski Wisniewski Wojcik Kowalczyk Kaminski Lewandowski Zielinski Szymanski Wozniak Dabrowski " +
            "Kozlowski Jankowski Mazur Kwiatkowski Krawczyk Piotrowski Grabowski Nowakowski Pawlowski Michalski Krol Wieczorek " +
            "Jablonski Wrobel Nowicki Majewski Olszewski Stepien Malinowski").split(" "),
        "Serbia" to ("Jovanovic Petrovic Nikolic Markovic Djordjevic Stojanovic Ilic Stankovic Pavlovic Milosevic Popovic " +
            "Zivkovic Todorovic Kostic Ristic Mitrovic Savic Lazic Milic Radovanovic Simic Tasic Vasic Jankovic Marinkovic").split(" "),
        "Stati Uniti" to ("Miller Davis Anderson Thomas Jackson Harris Martin Thompson Moore Clark Lewis Walker Hall Allen Young " +
            "Hernandez King Wright Scott Green Adams Baker Nelson Carter Mitchell Perez Roberts Turner Phillips Campbell Parker " +
            "Evans Edwards Collins Stewart Morris Rogers Reed Cook Morgan Bell Murphy Rivera Cooper Richardson Howard Ward Brooks").split(" "),
        "Giappone" to ("Tanaka Sato Suzuki Takahashi Watanabe Ito Yamamoto Nakamura Kobayashi Kato Yoshida Yamada Sasaki Matsumoto " +
            "Inoue Kimura Hayashi Shimizu Mori Ikeda Hashimoto Abe Ogawa Fujita Okada Goto Nishimura Fukuda Ota Miura Endo Aoki " +
            "Sakamoto Ishikawa Maeda Fujii Kondo Murakami Okamoto Ueda Morita Hara Shibata Sakai Kudo").split(" "),
        "Marocco" to ("Alaoui Bennani Tazi Idrissi El Amrani Berrada Chraibi Fassi Haddad Benali Ziani Lahlou Amrani Bouzid " +
            "Saidi Mansouri Naciri Ouazzani Rami Kettani Benjelloun Hakimi El Idrissi Jabri Belhaj Sabri Ait Ali").split(" "),
        "Turchia" to ("Yilmaz Kaya Demir Sahin Celik Yildiz Aydin Ozdemir Arslan Dogan Kilic Aslan Cetin Kara Koc Kurt Ozturk " +
            "Polat Tas Guler Aksoy Bulut Korkmaz Ozkan Simsek Erdem Unal Gunes Akin Yavuz Karaca Tekin").split(" "),
        "Scozia" to ("Campbell Stewart Murray Reid Ross Graham Grant Fraser Duncan Hamilton Kerr Boyd Gibson Wallace Craig " +
            "Sinclair Munro Paterson Lennox Douglas Cameron Mackay McLean MacDonald Robertson Henderson Ferguson Bruce Kennedy").split(" "),
        "Grecia" to ("Papadopoulos Georgiou Nikolaou Dimitriou Karagiannis Vasiliou Pappas Antoniou Ioannou Alexiou Konstantinou " +
            "Makris Oikonomou Stavrou Theodorou Kotsis Lazaridis Mavros Zervas Christodoulou Anagnostou Panagiotou Raptis").split(" ")
    )

    /** Cognomi composti (es. "De Luca") sono scritti con lo spazio: qui si ricompongono quelli spezzati dallo split. */
    private val UNIRE = setOf("De", "Di", "Dal", "Del", "La", "Lo", "Van", "El", "Ait", "Den", "Der")

    private val elenco: Map<String, List<String>> = COGNOMI.mapValues { (_, l) ->
        val out = mutableListOf<String>(); var i = 0
        while (i < l.size) {
            var c = l[i]
            while (c.split(" ").last() in UNIRE && i + 1 < l.size) { i++; c += " " + l[i] }
            out += c; i++
        }
        out.distinct()
    }

    private const val INIZIALI = "ABCDEFGHILMNOPRSTV"
    private val INIZIALI_PER = mapOf("Giappone" to "AHKMNRSTY", "Marocco" to "ABHIKMNOSY", "Inghilterra" to "ABCDEGHJKLMNPRST")

    /** Tutti i cognomi conosciuti, per le nazioni senza elenco proprio. */
    private val tutti: List<String> by lazy { elenco.values.flatten().distinct() }

    /** Nome casuale coerente con la nazionalità ("X. Cognome"). */
    fun genera(rng: Random, nazionalita: String? = null): String {
        val l = elenco[nazionalita] ?: tutti
        val ini = INIZIALI_PER[nazionalita] ?: INIZIALI
        return "${ini[rng.nextInt(ini.length)]}. ${l[rng.nextInt(l.size)]}"
    }
}
