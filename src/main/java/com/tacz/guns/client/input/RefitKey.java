package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class RefitKey {
   public static final KeyMapping REFIT_KEY = new KeyMapping("key.tacz.refit.desc", Type.KEYSYM, 90, KeyUtil.CATEGORY);

   public static void onRefitPress(InputEvent.Key event) {
      if (event.getAction() == 1 && KeyUtil.matches(REFIT_KEY, event.getKey(), event.getScanCode())) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return;
         }

         if (InputExtraCheck.isInGame()) {
            if (IGun.mainHandHoldGun(player) && Minecraft.getInstance().gui.screen() == null) {
               IGun iGun = IGun.getIGunOrNull(player.getMainHandItem());
               if (iGun != null && iGun.hasAttachmentLock(player.getMainHandItem())) {
                  return;
               }

               Minecraft.getInstance().gui.setScreen(new GunRefitScreen());
            }
         } else if (Minecraft.getInstance().gui.screen() instanceof GunRefitScreen refitScreen) {
            refitScreen.onClose();
         }
      }
   }
}
