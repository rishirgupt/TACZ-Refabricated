package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class InspectKey {
   public static final KeyMapping INSPECT_KEY = new KeyMapping("key.tacz.inspect.desc", Type.KEYSYM, 72, KeyUtil.CATEGORY);

   public static void onInspectPress(InputEvent.Key event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matches(INSPECT_KEY, event.getKey(), event.getScanCode())) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return;
         }

         IClientPlayerGunOperator.fromLocalPlayer(player).inspect();
      }
   }

   public static boolean onInspectControllerPress(boolean isPress) {
      if (InputExtraCheck.isInGame() && isPress) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null && !player.isSpectator()) {
            IClientPlayerGunOperator.fromLocalPlayer(player).inspect();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
