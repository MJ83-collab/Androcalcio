#!/usr/bin/env bash
# Installa l'APK ottimizzato, lo avvia e lo stressa con tocchi casuali. Fallisce se l'app va in crash.
set -u
APK=$(ls app/build/outputs/apk/release/*.apk | head -1)
adb install -r "$APK" || exit 1
adb logcat -c
adb shell am start -W -n it.androcalcio/.MainActivity
sleep 15
: > monkey.txt
for SEED in 42 7 99 2026 5 11; do
  adb shell monkey -p it.androcalcio -s $SEED --throttle 80 --pct-syskeys 0 --pct-appswitch 0 --ignore-security-exceptions --ignore-crashes --pct-nav 0 --pct-majornav 0 -v 1500 >> monkey.txt 2>&1
  if grep -q "// CRASH: it.androcalcio" monkey.txt; then break; fi
done
sleep 3
adb logcat -d > logcat.txt
{ echo "=== MONKEY ==="; tail -40 monkey.txt; echo "=== CRASH ==="; grep -A30 "Process: it.androcalcio" logcat.txt | head -80; } > smoke-log.txt
cat smoke-log.txt
if grep -q "Process: it.androcalcio" logcat.txt || grep -q "// CRASH: it.androcalcio" monkey.txt; then echo "CRASH TROVATO"; exit 1; fi
echo "Nessun crash."
