package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.network.message.ClientMessagePlayerZoom;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class ZoomKey {
   public static final KeyMapping ZOOM_KEY = new KeyMapping("key.tacz.zoom.desc", Type.KEYSYM, 86, KeyUtil.CATEGORY);

   public static void onZoomKeyPress(InputEvent.Key event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matches(ZOOM_KEY, event.getKey(), event.getScanCode())) {
         doZoomLogic();
      }
   }

   public static void onZoomMousePress(InputEvent.MouseButton.Post event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matchesMouse(ZOOM_KEY, event.getButton())) {
         doZoomLogic();
      }
   }

   public static boolean onZoomControllerPress(boolean isPress) {
      if (InputExtraCheck.isInGame() && isPress) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return false;
         }

         IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
         if (operator.isAim()) {
            ClientPlayNetworking.send(ClientMessagePlayerZoom.INSTANCE);
            return true;
         }
      }

      return false;
   }

   private static void doZoomLogic() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && !player.isSpectator()) {
         IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
         if (operator.isAim()) {
            ClientPlayNetworking.send(ClientMessagePlayerZoom.INSTANCE);
         }
      }
   }
}
