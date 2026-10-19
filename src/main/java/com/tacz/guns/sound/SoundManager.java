package com.tacz.guns.sound;

import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSound;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

public class SoundManager {
   public static String SHOOT_SOUND = "shoot";
   public static String SHOOT_3P_SOUND = "shoot_3p";
   public static String SILENCE_SOUND = "silence";
   public static String SILENCE_3P_SOUND = "silence_3p";
   public static String MELEE_BAYONET = "melee_bayonet";
   public static String MELEE_PUSH = "melee_push";
   public static String MELEE_STOCK = "melee_stock";
   public static String DRY_FIRE_SOUND = "dry_fire";
   public static String RELOAD_EMPTY_SOUND = "reload_empty";
   public static String RELOAD_TACTICAL_SOUND = "reload_tactical";
   public static String INSPECT_EMPTY_SOUND = "inspect_empty";
   public static String INSPECT_SOUND = "inspect";
   public static String DRAW_SOUND = "draw";
   public static String PUT_AWAY_SOUND = "put_away";
   public static String BOLT_SOUND = "bolt";
   public static String FIRE_SELECT = "fire_select";
   public static String HEAD_HIT_SOUND = "head_hit";
   public static String FLESH_HIT_SOUND = "flesh_hit";
   public static String KILL_SOUND = "kill";
   public static String UNINSTALL_SOUND = "uninstall";
   public static String INSTALL_SOUND = "install";

   public static void sendSoundToNearby(
      LivingEntity sourceEntity, int distance, Identifier gunId, Identifier gunDisplayId, String soundName, float volume, float pitch
   ) {
      if (sourceEntity.level() instanceof ServerLevel serverLevel) {
         BlockPos pos = sourceEntity.blockPosition();
         ServerMessageSound soundMessage = new ServerMessageSound(
            sourceEntity.getId(), gunId, gunDisplayId, new ServerMessageSound.MessageSound(soundName, volume, pitch, distance)
         );
         serverLevel.getChunkSource()
            .chunkMap
            .getPlayers(ChunkPos.containing(pos), false)
            .stream()
            .filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < distance * distance)
            .filter(p -> p.getId() != sourceEntity.getId())
            .forEach(p -> NetworkHandler.sendToClientPlayer(soundMessage, p));
      }
   }
}
