package com.tacz.guns.config;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class PreLoadConfig {
   private static ModConfigSpec spec;
   public static BooleanValue override;

   public static void init() {
      ConfigRegistry.INSTANCE.register("tacz", Type.COMMON, spec, "tacz-pre.toml");
   }

   static {
      Builder builder = new Builder();
      builder.push("gunpack");
      builder.comment(
         "When enabled, the mod will not try to overwrite the default pack under .minecraft/tacz\nSince 1.0.4, the overwriting will only run when you start client or a dedicated server"
      );
      override = builder.define("DefaultPackDebug", false);
      builder.pop();
      spec = builder.build();
   }
}
