package com.tacz.guns.config.common;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class OtherConfig {
   public static BooleanValue DEFAULT_PACK_DEBUG;
   public static IntValue TARGET_SOUND_DISTANCE;
   public static DoubleValue SERVER_HITBOX_OFFSET;
   public static BooleanValue SERVER_HITBOX_LATENCY_FIX;
   public static DoubleValue SERVER_HITBOX_LATENCY_MAX_SAVE_MS;

   public static void init(Builder builder) {
      builder.push("other");
      builder.comment("Deprecated: now move to .minecraft/tacz/tacz-pre.toml or <your version>/tacz/tacz-pre.toml");
      builder.comment("When enabled, the reload command will not overwrite the default model file under config");
      DEFAULT_PACK_DEBUG = builder.define("DefaultPackDebug", false);
      builder.comment("The farthest sound distance of the target, including minecarts type");
      TARGET_SOUND_DISTANCE = builder.defineInRange("TargetSoundDistance", 128, 0, Integer.MAX_VALUE);
      serverConfig(builder);
      builder.pop();
   }

   private static void serverConfig(Builder builder) {
      builder.comment("DEV: Server hitbox offset (If the hitbox is ahead, fill in a negative number)");
      SERVER_HITBOX_OFFSET = builder.defineInRange("ServerHitboxOffset", 3.0, -Double.MAX_VALUE, Double.MAX_VALUE);
      builder.comment("Server hitbox latency fix");
      SERVER_HITBOX_LATENCY_FIX = builder.define("ServerHitboxLatencyFix", true);
      builder.comment("The maximum latency (in milliseconds) for the server hitbox latency fix saved");
      SERVER_HITBOX_LATENCY_MAX_SAVE_MS = builder.defineInRange("ServerHitboxLatencyMaxSaveMs", 1000.0, 250.0, Double.MAX_VALUE);
   }
}
