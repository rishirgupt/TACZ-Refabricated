package com.tacz.guns.config.client;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class ResourceConfig {
   public static BooleanValue ENABLE_LAZY_CLIENT_ASSET_LOAD;
   public static BooleanValue ENABLE_LAZY_SOUND_LOAD;

   public static void init(Builder builder) {
      builder.push("resource");
      builder.comment(
         new String[]{
            "Build heavy TACZ client assets such as models and animation state machines on demand.",
            "Inventory items are pre-warmed in the background when possible.",
            "If a render needs an asset before warmup finishes, the render thread will wait for it once."
         }
      );
      ENABLE_LAZY_CLIENT_ASSET_LOAD = builder.define("EnableLazyClientAssetLoad", true);
      builder.pop();
   }
}
