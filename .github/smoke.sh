#!/usr/bin/env bash
# Installa l'APK ottimizzato, lo avvia e lo stressa con tocchi casuali. Fallisce se l'app va in crash.
set -u
APK=$(ls app/build/outputs/apk/release/*.apk | head -1)
adb install -r "$APK" || exit 1
adb logcat -c
adb shell am start -W -n it.androcalcio/.MainActivity
sleep 15
adb shell monkey -p it.androcalcio -s 42 --throttle 120 --pct-syskeys 0 --pct-appswitch 0 --ignore-security-exceptions -v 1500 > monkey.txt 2>&1
sleep 3
adb logcat -d > logcat.txt
{ echo "=== MONKEY ==="; tail -40 monkey.txt; echo "=== CRASH ==="; grep -A40 "FATAL EXCEPTION\|AndroidRuntime" logcat.txt | head -200; } > smoke-log.txt
cat smoke-log.txt
if grep -q "FATAL EXCEPTION" logcat.txt || grep -q "// CRASH" monkey.txt; then echo "CRASH TROVATO"; exit 1; fi
echo "Nessun crash."
