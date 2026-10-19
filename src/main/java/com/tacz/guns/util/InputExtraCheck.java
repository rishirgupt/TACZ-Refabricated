package com.tacz.guns.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class InputExtraCheck {
   public static boolean isInGame() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null) {
         return false;
      } else if (mc.gui.screen() != null) {
         return false;
      } else {
         return !mc.mouseHandler.isMouseGrabbed() ? false : mc.isWindowActive();
      }
   }
}
