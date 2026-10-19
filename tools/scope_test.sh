#!/bin/bash
# scope_test.sh <scope id> [extra steps] -- hip + aim screenshots of an M700 with the given scope against a striped wall
cd "$(dirname "$0")/.."
S='cmd:tp @p 0.5 -60 0.5 0 0;cmd:fill -6 -60 25 6 -52 25 minecraft:white_wool;cmd:fill -4 -60 25 -3 -52 25 minecraft:red_wool;cmd:fill 0 -60 25 1 -52 25 minecraft:blue_wool;cmd:fill 4 -60 25 5 -52 25 minecraft:lime_wool'
S="$S;cmd:give @p tacz:modern_kinetic_gun[custom_data={GunId:\"tacz:m700\",GunCurrentAmmoCount:5,AttachmentSCOPE:{id:\"tacz:attachment\",count:1,components:{\"minecraft:custom_data\":{AttachmentId:\"$1\"}}}}];wait:${WAIT:-60};shot:hip;hold:key.mouse.right;wait:60;shot:aim;release:key.mouse.right;wait:15$2"
timeout 900 ./gt.sh "$S" 5 >/dev/null
