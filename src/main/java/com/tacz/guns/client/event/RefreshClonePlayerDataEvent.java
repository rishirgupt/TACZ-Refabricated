package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.ClientPlayerNetworkEvent;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.util.DelayedTask;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class RefreshClonePlayerDataEvent {
   public static void onClientPlayerClone(ClientPlayerNetworkEvent.Clone event) {
      LocalPlayer newPlayer = event.getNewPlayer();
      DelayedTask.add(() -> IGunOperator.fromLivingEntity(newPlayer).initialData(), 10);
   }

   public static void onClientTick(Minecraft client) {
      try {
         DelayedTask.SUPPLIERS.removeIf(BooleanSupplier::getAsBoolean);
      } catch (Exception e) {
         DelayedTask.SUPPLIERS.clear();
         GunMod.LOGGER.error(e.getMessage(), e);
      }
   }
}
