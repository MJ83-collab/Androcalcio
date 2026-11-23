# Room e Compose portano già le proprie regole. Qui solo il necessario per l'app.
-keepattributes *Annotation*
# Nomi di classi e metodi leggibili negli errori (R8 toglie comunque il codice inutile)
-dontobfuscate
-keepattributes SourceFile,LineNumberTable
