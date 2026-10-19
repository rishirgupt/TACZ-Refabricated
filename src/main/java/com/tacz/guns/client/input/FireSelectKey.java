package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class FireSelectKey {
   public static final KeyMapping FIRE_SELECT_KEY = new KeyMapping("key.tacz.fire_select.desc", Type.KEYSYM, 71, KeyUtil.CATEGORY);

   public static void onFireSelectKeyPress(InputEvent.Key event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matches(FIRE_SELECT_KEY, event.getKey(), event.getScanCode())) {
         doFireSelectLogic();
      }
   }

   public static void onFireSelectMousePress(InputEvent.MouseButton.Post event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matchesMouse(FIRE_SELECT_KEY, event.getButton())) {
         doFireSelectLogic();
      }
   }

   public static boolean onFireSelectControllerPress(boolean isPress) {
      if (InputExtraCheck.isInGame() && isPress) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return false;
         }

         if (IGun.mainHandHoldGun(player)) {
            IClientPlayerGunOperator.fromLocalPlayer(player).fireSelect();
            return true;
         }
      }

      return false;
   }

   private static void doFireSelectLogic() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && !player.isSpectator()) {
         if (IGun.mainHandHoldGun(player)) {
            IClientPlayerGunOperator.fromLocalPlayer(player).fireSelect();
         }
      }
   }
}
