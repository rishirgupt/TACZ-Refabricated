package com.tacz.guns.config;

import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.config.common.OtherConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public final class CommonConfig {
   public static ModConfigSpec init() {
      Builder builder = new Builder();
      GunConfig.init(builder);
      AmmoConfig.init(builder);
      OtherConfig.init(builder);
      return builder.build();
   }
}
