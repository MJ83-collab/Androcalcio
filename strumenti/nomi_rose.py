"""Aggiorna i nomi generici "X. Cognome" di app/src/main/assets/rose_fisse.csv con nomi per nazione.

Stesso calcolo di Nomi.fisso (Kotlin): riga i -> iniziale (i*31+7) e cognome (i*7919+13) dall'elenco della nazione.
Uso: python3 strumenti/nomi_rose.py
"""
import re, pathlib
ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "app/src/main/java/it/androcalcio"
nomi_kt = (SRC / "engine/Nomi.kt").read_text(encoding="utf-8")
rose_kt = (SRC / "data/RoseFisse.kt").read_text(encoding="utf-8")

blocco = nomi_kt[nomi_kt.index("mapOf(") : nomi_kt.index("private val UNIRE")]
elenco = {}
for m in re.finditer(r'"([^"]+)" to \(((?:\s*"[^"]*"\s*\+?)+)\)\.split\(" "\)', blocco):
    testo = "".join(re.findall(r'"([^"]*)"', m.group(2)))
    elenco[m.group(1)] = testo.split(" ")
UNIRE = set(re.search(r'UNIRE = setOf\(([^)]*)\)', nomi_kt).group(1).replace('"', '').replace(' ', '').split(','))

def unisci(l):
    out, i = [], 0
    while i < len(l):
        c = l[i]
        while c.split(" ")[-1] in UNIRE and i + 1 < len(l):
            i += 1; c += " " + l[i]
        out.append(c); i += 1
    return list(dict.fromkeys(out))

elenco = {k: unisci(v) for k, v in elenco.items()}
tutti = list(dict.fromkeys(x for v in elenco.values() for x in v))
INIZIALI = re.search(r'const val INIZIALI = "([A-Z]+)"', nomi_kt).group(1)
PER = dict(re.findall(r'"([^"]+)" to "([A-Z]+)"', nomi_kt[nomi_kt.index("INIZIALI_PER"):]))
vecchi = set(re.findall(r'"([A-Z][a-z]+)"', rose_kt[rose_kt.index("val surnames"):rose_kt.index("val codes")]))

def fisso(naz, i):
    l = elenco.get(naz, tutti); ini = PER.get(naz, INIZIALI)
    return f"{ini[(i * 31 + 7) % len(ini)]}. {l[(i * 7919 + 13) % len(l)]}"

csv = ROOT / "app/src/main/assets/rose_fisse.csv"
righe = csv.read_text(encoding="utf-8").split("\n")
cambiati = 0
for k in range(1, len(righe)):
    if not righe[k].strip():
        continue
    c = righe[k].split("|")
    n = c[9]
    if len(n) > 3 and n[1] == "." and n[3:] in vecchi:
        c[9] = fisso(c[6], k - 1); righe[k] = "|".join(c); cambiati += 1
csv.write_text("\n".join(righe), encoding="utf-8")
print("nazioni", len(elenco), "cognomi", len(tutti), "rinominati", cambiati)
