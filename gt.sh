#!/bin/bash
# usage: ./gt.sh "<script>"  -> runs the scripted client test, prints the state log
cd "$(dirname "$0")"
rm -rf build/run-gametest/screenshots
mkdir -p build; printf "%s" "$1" > build/tz-script.txt
JDK="/c/Program Files/Java/jdk-26.0.2.1/bin"
: > "$TEMP/gt.log"; rm -f "$TEMP/gt-hang.txt"

# Watchdog: once the game is up, a log that stays silent for 20 seconds means the game is frozen.
# Take a thread dump (to see what is stuck) and close the game instead of leaving a dead window open.
(
  frozen=0
  while sleep 4; do
    [ -f "$TEMP/gt.done" ] && exit 0
    pid=$("$JDK/jps.exe" -lv 2>/dev/null | grep "fabric.client.gametest" | cut -d" " -f1 | head -1)
    [ -z "$pid" ] && { frozen=0; continue; }
    # Windows marks a window "Not Responding" when its main thread stops pumping events: a real freeze,
    # unlike a quiet log (the game idles at low fps when its window is in the background).
    if tasklist //FI "PID eq $pid" //FI "STATUS eq NOT RESPONDING" 2>/dev/null | grep -q "$pid"; then
      frozen=$((frozen + 4))
    else
      frozen=0
    fi
    if [ "$frozen" -ge 20 ]; then
      "$JDK/jstack.exe" "$pid" > "$TEMP/gt-hang.txt" 2>&1
      taskkill //F //PID "$pid" > /dev/null 2>&1
      echo "WATCHDOG: game not responding for ${frozen}s, closed it (thread dump: $TEMP/gt-hang.txt)"
      exit 0
    fi
  done
) &
WATCHDOG=$!
rm -f "$TEMP/gt.done"
./gradlew.bat runClientGameTest > "$TEMP/gt.log" 2>&1
echo "exit=$?"
touch "$TEMP/gt.done"; wait $WATCHDOG 2>/dev/null
grep -nE "error:|tz-test|BUILD|AssertionError|/ERROR\]|at knot//com\.leon" "$TEMP/gt.log" | grep -vE "HkeyPerf|Anisotropic|blood_tempest_armor|profile key" | cut -c1-320 | head -${2:-80}
ls build/run-gametest/screenshots 2>/dev/null
