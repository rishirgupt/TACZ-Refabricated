#!/bin/bash
# jf.sh <fully.qualified.Class> [grep-pattern]  -- print the API of a class from the compile classpath (mods/libs/Minecraft)
JP="/c/Program Files/Java/jdk-26.0.2.1/bin/javap.exe"
CP="$(cat "$(dirname "$0")/../build/cp.txt");$(cygpath -w ~/.gradle/caches/fabric-loom/26.2/minecraft-merged.jar)"
if [ -n "$2" ]; then "$JP" -cp "$CP" "$1" | grep -E "$2"; else "$JP" -cp "$CP" "$1"; fi
