package com.tacz.guns.config;

import com.tacz.guns.config.client.KeyConfig;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.client.ResourceConfig;
import com.tacz.guns.config.client.SoundConfig;
import com.tacz.guns.config.client.ZoomConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class ClientConfig {
   public static ModConfigSpec init() {
      Builder builder = new Builder();
      KeyConfig.init(builder);
      RenderConfig.init(builder);
      ResourceConfig.init(builder);
      SoundConfig.init(builder);
      ZoomConfig.init(builder);
      return builder.build();
   }
}
