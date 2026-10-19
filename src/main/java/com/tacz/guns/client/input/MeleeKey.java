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
public class MeleeKey {
   public static final KeyMapping MELEE_KEY = new KeyMapping("key.tacz.melee.desc", Type.KEYSYM, 86, KeyUtil.CATEGORY);

   public static void onMeleeKeyPress(InputEvent.Key event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matches(MELEE_KEY, event.getKey(), event.getScanCode())) {
         doMeleeLogic();
      }
   }

   public static void onMeleeMousePress(InputEvent.MouseButton.Post event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matchesMouse(MELEE_KEY, event.getButton())) {
         doMeleeLogic();
      }
   }

   public static boolean onMeleeControllerPress(boolean isPress) {
      if (InputExtraCheck.isInGame() && isPress) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return false;
         }

         IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
         if (!operator.isAim()) {
            operator.melee();
            return true;
         }
      }

      return false;
   }

   private static void doMeleeLogic() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && !player.isSpectator()) {
         IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
         if (!operator.isAim()) {
            operator.melee();
         }
      }
   }
}
