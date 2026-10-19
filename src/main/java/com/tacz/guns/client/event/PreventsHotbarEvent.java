package com.tacz.guns.client.event;

import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class PreventsHotbarEvent {
   public static void onRenderHotbarEvent(AtomicBoolean cancelled) {
      Screen screen = Minecraft.getInstance().gui.screen();
      if (screen instanceof GunSmithTableScreen) {
         cancelled.set(true);
      } else {
         if (screen instanceof GunRefitScreen) {
            cancelled.set(true);
         }
      }
   }
}
