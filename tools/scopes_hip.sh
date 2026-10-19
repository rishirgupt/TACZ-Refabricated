#!/bin/bash
# scopes_hip.sh <scope names...> -- hip screenshot of an M700 with each scope (hotbar slots 1..n), montage in build/run-gametest/scopes_hip.png
cd "$(dirname "$0")/.."
S='cmd:tp @p 0.5 -60 0.5 0 0;cmd:fill -6 -60 25 6 -52 25 minecraft:white_wool;cmd:fill -4 -60 25 -3 -52 25 minecraft:red_wool;cmd:fill 0 -60 25 1 -52 25 minecraft:blue_wool;cmd:fill 4 -60 25 5 -52 25 minecraft:lime_wool'
n=0
for s in "$@"; do n=$((n+1)); S="$S;cmd:give @p tacz:modern_kinetic_gun[custom_data={GunId:\"tacz:m700\",GunCurrentAmmoCount:5,AttachmentSCOPE:{id:\"tacz:attachment\",count:1,components:{\"minecraft:custom_data\":{AttachmentId:\"tacz:$s\"}}}}]"; done
S="$S;wait:80"
for i in $(seq 1 $n); do S="$S;press:key.keyboard.$i;wait:50;shot:s$i"; done
timeout 900 ./gt.sh "$S" 5 >/dev/null
cd build/run-gametest/screenshots
"/c/Program Files/ImageMagick-7.1.2-Q16-HDRI/magick" montage *.png -crop 420x300+434+180 +repage -tile 4x -geometry 420x300+2+2 ../scopes_hip.png
