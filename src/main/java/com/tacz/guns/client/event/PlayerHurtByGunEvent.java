package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.client.renderer.other.GunHurtBobTweak;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class PlayerHurtByGunEvent {
   public static void onPlayerHurtByGun(EntityHurtByGunEvent.Post event) {
      LogicalSide logicalSide = event.getLogicalSide();
      if (logicalSide == LogicalSide.CLIENT) {
         Entity hurtEntity = event.getHurtEntity();
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null && player.equals(hurtEntity)) {
            Identifier gunId = event.getGunId();
            TimelessAPI.getCommonGunIndex(gunId).ifPresent(index -> {
               float tweakMultiplier = index.getGunData().getHurtBobTweakMultiplier();
               GunHurtBobTweak.markTimestamp(tweakMultiplier);
            });
         }
      }
   }
}
