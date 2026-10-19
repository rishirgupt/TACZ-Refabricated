package com.tacz.guns.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LaserColorUtil {
   public static int getLaserColor(ItemStack stack, @NotNull LaserConfig defaultConfig) {
      if (stack == null) {
         return defaultConfig.getDefaultColor();
      } else if (stack.getItem() instanceof IAttachment iAttachment) {
         return iAttachment.hasCustomLaserColor(stack) ? iAttachment.getLaserColor(stack) : defaultConfig.getDefaultColor();
      } else if (stack.getItem() instanceof IGun gun) {
         return gun.hasCustomLaserColor(stack) ? gun.getLaserColor(stack) : defaultConfig.getDefaultColor();
      } else {
         return defaultConfig.getDefaultColor();
      }
   }

   public static int getLaserColor(ItemStack stack) {
      if (stack == null) {
         return 16711680;
      } else if (stack.getItem() instanceof IAttachment iAttachment) {
         return iAttachment.hasCustomLaserColor(stack)
            ? iAttachment.getLaserColor(stack)
            : TimelessAPI.getClientAttachmentIndex(iAttachment.getAttachmentId(stack))
               .map(ClientAttachmentIndex::getLaserConfig)
               .map(LaserConfig::getDefaultColor)
               .orElse(16711680);
      } else if (stack.getItem() instanceof IGun gun) {
         return gun.hasCustomLaserColor(stack)
            ? gun.getLaserColor(stack)
            : TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getLaserConfig).map(LaserConfig::getDefaultColor).orElse(16711680);
      } else {
         return 16711680;
      }
   }
}
