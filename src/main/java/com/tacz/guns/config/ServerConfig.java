package com.tacz.guns.config;

import com.tacz.guns.config.sync.SyncConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class ServerConfig {
   public static ModConfigSpec SERVER_CONFIG_SPEC;

   public static ModConfigSpec init() {
      Builder builder = new Builder();
      SyncConfig.init(builder);
      SERVER_CONFIG_SPEC = builder.build();
      return SERVER_CONFIG_SPEC;
   }
}
