package com.tacz.guns.api.resource;

import com.google.common.collect.Lists;
import com.tacz.guns.GunMod;
import java.nio.file.Paths;
import java.util.List;

public final class ResourceManager {
   public static final List<ResourceManager.ExtraEntry> EXTRA_ENTRIES = Lists.newArrayList();

   @Deprecated
   public static void registerExtraGunPack(Class<?> modMainClass, String extraFolderPath) {
      GunMod.LOGGER.warn("some mod is using deprecated method to export gun pack, notifying the mod author to update it: {}", extraFolderPath);
   }

   public static void registerExportResource(Class<?> modMainClass, String extraFolderPath) {
      EXTRA_ENTRIES.add(new ResourceManager.ExtraEntry(modMainClass, extraFolderPath, Paths.get(extraFolderPath).getFileName().toString()));
   }

   public record ExtraEntry(Class<?> modMainClass, String srcPath, String extraDirName) {
   }
}
