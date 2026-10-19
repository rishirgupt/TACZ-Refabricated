#!/bin/bash
# jp.sh <fully.qualified.Class> [grep-pattern]  -- print the 26.2 API of a class
JP="/c/Program Files/Java/jdk-26.0.2.1/bin/javap.exe"
J=$(cygpath -w ~/.gradle/caches/fabric-loom/26.2/minecraft-merged.jar)
shift_pat="$2"
if [ -n "$shift_pat" ]; then "$JP" -cp "$J" "$1" | grep -E "$shift_pat"; else "$JP" -cp "$J" "$1"; fi
