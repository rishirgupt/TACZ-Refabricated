package com.tacz.guns.event;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.AttachmentPropertyEvent;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ChangeGunPropertyEvent {
   @Internal
   public static void internalOnAttachmentPropertyEvent(AttachmentPropertyEvent event) {
      ItemStack gunItem = event.getGunItem();
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun != null) {
         Identifier gunId = iGun.getGunId(gunItem);
         TimelessAPI.getCommonGunIndex(gunId).ifPresent(gunIndex -> event.getCacheProperty().eval(gunItem, gunIndex.getGunData()));
      }
   }
}
