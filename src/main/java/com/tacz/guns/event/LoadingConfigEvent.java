package com.tacz.guns.event;

import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import com.tacz.guns.config.util.InteractKeyConfigRead;
import net.neoforged.fml.config.ModConfig;

public class LoadingConfigEvent {
   private static final String CONFIG_NAME = "tacz-server.toml";

   public static void onLoadingConfig(ModConfig config) {
      String fileName = config.getFileName();
      if ("tacz-server.toml".equals(fileName)) {
         HeadShotAABBConfigRead.init();
         InteractKeyConfigRead.init();
      }
   }

   public static void onReloadingConfig(ModConfig config) {
      String fileName = config.getFileName();
      if ("tacz-server.toml".equals(fileName)) {
         HeadShotAABBConfigRead.init();
         InteractKeyConfigRead.init();
      }
   }
}
