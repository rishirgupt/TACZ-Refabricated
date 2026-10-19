#!/bin/bash
# compile and print unique errors: ./cc.sh [maxlines]
cd "$(dirname "$0")"
./gradlew.bat compileJava > "$TEMP/tz.log" 2>&1
grep -E "^[A-Z]:.*error:" "$TEMP/tz.log" | perl -pe "s#.*src.main.java.##" | sort -u > "$TEMP/tz.err"
echo "errors: $(wc -l < "$TEMP/tz.err")  $(grep -E 'BUILD' "$TEMP/tz.log")"
head -${1:-60} "$TEMP/tz.err" | cut -c1-230
