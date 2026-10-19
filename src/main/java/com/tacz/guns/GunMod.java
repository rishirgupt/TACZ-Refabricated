package com.tacz.guns;

import com.tacz.guns.api.resource.ResourceManager;
import com.tacz.guns.init.CommonRegistry;
import com.tacz.guns.init.ModAttributes;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.init.ModContainer;
import com.tacz.guns.init.ModCreativeTabs;
import com.tacz.guns.init.ModEntities;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.init.ModPainting;
import com.tacz.guns.init.ModParticles;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.init.ModSounds;
import com.tacz.guns.resource.GunPackLoader;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GunMod {
   public static final String MOD_ID = "tacz";
   public static final Logger LOGGER = LoggerFactory.getLogger("tacz");
   public static final String DEFAULT_GUN_PACK_NAME = "tacz_default_gun";

   public static void setup() {
      EnvType side = FabricLoader.getInstance().getEnvironmentType();
      GunPackLoader.INSTANCE.packType = side == EnvType.CLIENT ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
      CommonRegistry.onSetupEvent();
      ModBlocks.init();
      ModItems.init();
      ModCreativeTabs.init();
      ModEntities.init();
      ModRecipe.init();
      ModContainer.init();
      ModSounds.init();
      ModParticles.init();
      ModAttributes.init();
      ModPainting.init();
      if (FabricLoader.getInstance().isModLoaded("kubejs")) {
      }

      registerDefaultExtraGunPack();
      AttachmentPropertyManager.registerModifier();
   }

   private static void registerDefaultExtraGunPack() {
      String jarDefaultPackPath = String.format("/assets/%s/custom/%s", "tacz", "tacz_default_gun");
      ResourceManager.registerExportResource(GunMod.class, jarDefaultPackPath);
   }
}
