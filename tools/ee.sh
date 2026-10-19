#!/bin/bash
# ee.sh <file-regex> [max]  -- compact errors (message + source line) for matching files
awk -v pat="$1" 'BEGIN{p=0;n=0} /^[A-Z]:.*error:/{p=($0 ~ pat); if(p){n=1; print; next}} p&&n==1{print; n=2; next} p&&/symbol:/{print} ' "$TEMP/tz.log" | perl -pe 's#.*src.main.java.##' | cut -c1-220 | head -${2:-60}
