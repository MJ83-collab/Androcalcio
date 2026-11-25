"""
Adatta gli overall delle rose predefinite (app/src/main/assets/rose_fisse.csv) alla forza reale dei club,
usando il valore di mercato della rosa (milioni di euro, Transfermarkt via footystats.org e footballwhispers.com,
novembre 2026). Solo massime serie. La media di ogni campionato NON cambia: cambia solo l'ordine e la distanza
tra le squadre dello stesso campionato. Valori con "None" = club non trovato nella fonte: prende il 25° percentile
del campionato. Valori in STIME = fonte incompleta, stimati a mano (vedi report.md).
Uso: python3 strumenti/forza_squadre.py
"""
import csv, math, statistics as st, sys
from collections import defaultdict

CSV = "app/src/main/assets/rose_fisse.csv"

# nome Androcalcio -> valore rosa reale (M€); None = non trovato
VALORI = {
 "Serie A": {"Inter NA":644.5,"Piemonte BN":564.9,"Milano RN":525.7,"Bergamo DA":476.8,"Partenope AZ":467.75,"Firenze VI":333.1,
   "Roma GR":324.5,"Bologna RB":321.2,"Lazio BC":277.6,"Udine ZB":180.5,"Torino GT":175.4,"Parma CR":171.8,"Como":167.7,"Genova GF":159.25,
   "Lecce SA":108.4,"Empoli CE":97.9,"Verona GB":95.25,"Cagliari IS":89.65,"Monza BR":67.65,"Genova BL":None},
 "Primera": {"Royal Madril":1165,"Barcelos BG":1057.5,"Atletic Madril":529.3,"Bilbo RJ2":331.4,"Donosti RJ":323.95,"Villareal AM":225.05,
   "Betis VB":213.2,"Valencio NB2":212.7,"Sivilla NB":174.05,"Girono RJ4":152.6,"Celta CY":124.3,"Las Islas AM2":113.5,"Osasuna RJ3":112.2,
   "Alaves AZ4":82.8,"Malorca RJ5":81.2,"Rayo FR":67.95,"Getafo AZ3":64.3,"Cadis AM3":22.7,"Granado RJ6":35.85,"Almeiro RJ7":None},
 "Premier": {"Manchester Citt SB":1174.9,"Arsenale GN":1056.7,"Liverpoole RD2":942.5,"Chelsfield BL":919.6,"Tottenam SP":770.3,
   "Manchester Unity RD":674.2,"Aston Villan VL":652.95,"Newcastel MG":625.15,"Brighten SG":566.75,"Crystal Palac EG":441.6,"Bornemuth CH":440,
   "Nottingam TT":435.5,"Brentfort BE":396.1,"West Hamm HM":391.3,"Wolverhamp WV":386.45,"Evertonn TF":362.7,"Fulhame CT":361.35,
   "Leicestar FX":244.7,"Southamptn SA":240.85,"Burnlea CL":None},
 "Bundesliga": {"Baviera MO":1040,"Dortmuno GS":564.45,"Lipsia RB2":492.95,"Leverkus BA":480.95,"Stuggart VB4":385.95,"Eintrach FR2":288.25,
   "Hoffenhm TS":285.95,"Freiburgo SC2":211.4,"Augusbur FC2":177.8,"Mainza KL":164.2,"Kolno GT2":154.1,"Glabach FH":138.55,"Bremeno WD":122.08,
   "Union BE":115.78,"Bochium VF":26.95,"Darmstat SV":24.9,"Wolfsbur VW":None,"Heidenhm FC3":None},
 "Ligue 1": {"Parigi SG":1051,"Monaco AS2":410.2,"Marsiglia OM2":311.25,"Lilla LOS":309.7,"Lionese OL2":290.5,"Strasbrg RC3":247.5,"Nissa OGC":228.45,
   "Renn SR":220.35,"Lens RC2":197.4,"Reimso SR2":132.1,"Brest SB29":112.2,"Toulus TF2":110.65,"Nantas FCN2":106.05,"Le Havro HAC":74,
   "Montpelr MH2":59.05,"Mezzo FCM2":None,"Clairmont F632":None,"Loriento FCL":None},
 "Brasile": {"Palmeras VD":138,"Flamengo MG":117.1,"Atletico Minas GL":83.6,"Cruzeiro RP":75.45,"Bahia TB":74.2,"Corintios TM":72,"Internaciol CL":70.8,
   "Gremio TG":69.5,"Botafogo FG":66.5,"Vasco CM":57.05,"Santos PX":52,"Fluminense TL":49.6,"Bragantino MB":40.3,"San Paulo TC":37.75,
   "Fortaleza LP":31.45,"Ceara VZ":18.3,"Juventude PP":5.5,"Paranaense FR":31,"Cuiaba DO":13.5,"Criciuma TG2":13.4},
 "Argentina": {"River ML":119.3,"Velez FT":35,"Rosario CN":25.5,"Lanus GR9":25.2,"Defensa HC":21.8,"Godoy TB2":17.4,"Tigre MT2":16.3,
   "Banfield TD":12.1,"Newells LP":12.1,"Boca XZ":None,"Racing AC":None,"Independencia RJ14":None,"San Lorenzo CC":None,"Estudiantes PR":None,
   "Argentinos BC2":None,"Huracan GB":None,"Talleres MT":None,"Belgrano PI":None,"Instituto GL2":None,"Platense CL2":None},
 "Eredivisie": {"Eindhoven Licht":380.5,"Rotterdam Haven":346.5,"Amsterdam Vlam":249.1,"Alkmaar Kaas":86.6,"Enschede Wind":74,"Utrecht Dom":47.9,
   "Nijmegen Rijn":35.85,"Heerenveen Ijs":30.2,"Groningen Noord":24.2,"Breda Kasteel":21.975,"Zwolle Molen":21.15,"Deventer Hanze":20.2,
   "Tilburg Wolf":15.4,"Almere Polder":13.4,"Den Haag Zee":None,"Arnhem Brug":None,"Venlo Maas":None,"Leiden Tulp":None},
 "Primeira Liga": {"Lisboa Sol":453.2,"Porto Ponte":346.3,"Braga Monte":140.9,"Estoril Costa":62.05,"Guimaraes Castelo":59.6,"Barcelos Galo":32.45},
 "Pro League": {"Antwerpen Haven":55.45,"Liege Acier":22.95,"Mechelen Toren":22.55,"Waregem Piste":7.7},
 "Premiership": {"Glasgow Clyde":158.45,"Aberdeen Granite":26.6,"Edimburgo Rock":19.9,"Motherwell Steel":17,"Kilmarnock Burn":12.6,
   "Dundee Tay":8.85,"Paisley Mill":8.75,"Perth Thistle":6.4},
 "Super Lig": {"Istanbul Bosforo":195.65,"Trabzon Karadeniz":74.7,"Samsun Liman":28.6,"Rize Cay":27.1,"Antalya Sahil":23.3},
 "Super League": {"Salonicco Torre":122,"Atene Ulivo":97.4,"Heraklion Minoa":16.4,"Tripoli Monte":12.7,"Agrinio Foglia":10.1,"Serres Alba":9.75,
   "Volos Argo":9.5,"Lamia Termopili":8.1},
 "J1 League": {"Kashima Kuro":36.8,"Urawa Akai":23.8,"Hiroshima Momiji":23,"Kawasaki Taiyo":18.5,"Osaka Tora":17.1,"Tokyo Ryu":15.7},
}
# Stime a mano dove la fonte era incompleta (grandi club assenti dalla tabella del momento)
STIME = {"Boca XZ":110,"Racing AC":55,"Independencia RJ14":35,"Estudiantes PR":40,"Talleres MT":40,"San Lorenzo CC":25,"Argentinos BC2":30,
         "Brussel Leeuw":120,"Brugge Zwaan":150,"Gent Graaf":60,"Genk Mijn":90,"Yokohama Kai":20,"Kobe Sakura":20,"Nagoya Kaze":14}
CAMPIONATO = {"Italia":"Serie A","Spagna":"Primera","Inghilterra":"Premier","Germania":"Bundesliga","Francia":"Ligue 1","Brasile":"Serie A",
              "Argentina":"Primera","Olanda":"Eredivisie","Portogallo":"Primeira Liga","Belgio":"Pro League","Scozia":"Premiership",
              "Turchia":"Super Lig","Grecia":"Super League","Giappone":"J1 League"}
CHIAVE = {("Brasile","Serie A"):"Brasile",("Argentina","Primera"):"Argentina"}
ATTR = ["ve","re","ag","qu","gm","en","pa","dr","fi","ti","ri"]

def main():
    import json
    squadra_lega = {}
    for f in ["squadre.json","squadre_europa.json","squadre_giappone.json"]:
        for t in json.load(open("app/src/main/assets/"+f, encoding="utf-8")):
            if CAMPIONATO.get(t["nazione"]) == t["campionato"]:
                squadra_lega[t["squadra"]] = CHIAVE.get((t["nazione"], t["campionato"]), t["campionato"])
    righe = list(csv.DictReader(open(CSV, encoding="utf-8"), delimiter="|"))
    campi = list(righe[0].keys())
    per_sq = defaultdict(list)
    for r in righe: per_sq[r["squadra"]].append(r)
    def media(sq): return st.mean(sorted((int(r["overall"]) for r in per_sq[sq]), reverse=True)[:16])
    out = []
    for lega in set(squadra_lega.values()):
        sq = [s for s, l in squadra_lega.items() if l == lega and per_sq.get(s)]
        if len(sq) < 2: print("salto", lega, "(rose generate in automatico)"); continue
        valori = dict(VALORI.get(lega, {}))
        for s in sq:
            if valori.get(s) is None and s in STIME: valori[s] = STIME[s]
        noti = [v for v in valori.values() if v]
        q25 = sorted(noti)[len(noti) // 4] if noti else 10
        log = {s: math.log(valori.get(s) or q25) for s in sq}
        mu, sd = st.mean(log.values()), st.pstdev(log.values()) or 1
        medie = {s: media(s) for s in sq}
        M = st.mean(medie.values())
        spread = max(st.pstdev(medie.values()), 4.0)   # distanza tra prima e ultima: almeno ~±8 punti
        for s in sq:
            z = max(-2.0, min(2.5, (log[s] - mu) / sd))
            target = M + z * spread
            delta = round(target - medie[s])
            for r in per_sq[s]:
                for k in ["overall", "potenziale"] + ATTR:
                    if r.get(k, "").strip() and r[k] != "0":
                        r[k] = str(max(20, min(99, int(r[k]) + delta)))
                if int(r["potenziale"]) < int(r["overall"]): r["potenziale"] = r["overall"]
            out.append((lega, s, round(medie[s], 1), round(medie[s] + delta, 1), valori.get(s), s in STIME))
    with open(CSV, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=campi, delimiter="|", lineterminator="\n"); w.writeheader(); w.writerows(righe)
    for lega in sorted({o[0] for o in out}):
        print("==", lega)
        for o in sorted([o for o in out if o[0] == lega], key=lambda o: -o[3]):
            print(f"  {o[1]:22} {o[2]:5} -> {o[3]:5}  {'stima ' if o[5] else ''}{o[4] if o[4] else '(non trovato)'}")

def riordina():
    """Campionati con rose generate (forza in base alla posizione nel file): ordina le squadre per valore reale."""
    import json
    for f in ["squadre_europa.json", "squadre_giappone.json"]:
        path = "app/src/main/assets/" + f
        dati = json.load(open(path, encoding="utf-8"))
        gruppi, ordine = defaultdict(list), []
        for t in dati:
            k = (t["nazione"], t["campionato"])
            if k not in gruppi: ordine.append(k)
            gruppi[k].append(t)
        out = []
        for k in ordine:
            lega = CAMPIONATO.get(k[0]) if CAMPIONATO.get(k[0]) == k[1] else None
            val = {**VALORI.get(lega, {}), **STIME} if lega else {}
            lista = gruppi[k]
            noti = sorted([t for t in lista if val.get(t["squadra"])], key=lambda t: -val[t["squadra"]])
            out += noti + [t for t in lista if t not in noti]
            if noti: print(k[1], "->", ", ".join(t["squadra"] for t in (noti + [t for t in lista if t not in noti])[:6]), "...")
        json.dump(out, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

if __name__ == "__main__":
    main()
    riordina()
