package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.client.KeyConfig;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class AimKey {
   public static final KeyMapping AIM_KEY = new KeyMapping("key.tacz.aim.desc", Type.MOUSE, 1, KeyUtil.CATEGORY);

   public static void onAimPress(InputEvent.MouseButton.Post event) {
      if (InputExtraCheck.isInGame() && KeyUtil.matchesMouse(AIM_KEY, event.getButton())) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return;
         }

         if (!(player instanceof IClientPlayerGunOperator operator)) {
            return;
         }

         if (IGun.mainHandHoldGun(player)) {
            boolean action = true;
            if (!(Boolean)KeyConfig.HOLD_TO_AIM.get()) {
               action = !operator.isAim();
            }

            if (event.getAction() == 1) {
               IClientPlayerGunOperator.fromLocalPlayer(player).aim(action);
            }

            if ((Boolean)KeyConfig.HOLD_TO_AIM.get() && event.getAction() == 0) {
               IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
            }
         }
      }
   }

   public static void onAimHoldingPreInput(Minecraft client) {
      if ((Boolean)KeyConfig.HOLD_TO_AIM.get()) {
         Minecraft mc = Minecraft.getInstance();
         boolean press = AIM_KEY.isDown();
         if (InputExtraCheck.isInGame()) {
            LocalPlayer player = mc.player;
            if (player == null || player.isSpectator()) {
               return;
            }

            if (!(player instanceof IClientPlayerGunOperator operator)) {
               return;
            }

            if (operator.isAim() && press) {
               return;
            }

            if (!operator.isAim() && !press) {
               return;
            }

            if (IGun.mainHandHoldGun(player)) {
               IClientPlayerGunOperator.fromLocalPlayer(player).aim(press);
            }
         }
      }
   }

   public static boolean onAimControllerPress(boolean isPress) {
      if (!InputExtraCheck.isInGame()) {
         return false;
      }

      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null || player.isSpectator()) {
         return false;
      }

      if (player instanceof IClientPlayerGunOperator operator) {
         if (!IGun.mainHandHoldGun(player)) {
            return false;
         }

         boolean action = true;
         if (!(Boolean)KeyConfig.HOLD_TO_AIM.get()) {
            action = !operator.isAim();
         }

         if (isPress) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(action);
            return true;
         } else if ((Boolean)KeyConfig.HOLD_TO_AIM.get()) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void cancelAim(Minecraft mc) {
      LocalPlayer player = mc.player;
      if (player instanceof IClientPlayerGunOperator operator) {
         if (operator.isAim() && (!InputExtraCheck.isInGame() || player.isSpectator())) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
         }
      }
   }
}
