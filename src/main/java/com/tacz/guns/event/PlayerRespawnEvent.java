package com.tacz.guns.event;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import net.minecraft.server.level.ServerPlayer;

public class PlayerRespawnEvent {
   public static void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
      if ((Boolean)GunConfig.AUTO_RELOAD_WHEN_RESPAWN.get()) {
         newPlayer.getInventory().getNonEquipmentItems().forEach(itemStack -> {
            if (itemStack.getItem() instanceof IGun) {
               ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
               api.setItemStack(itemStack);
               api.setShooter(newPlayer);
               boolean useInventoryAmmo = api.getGunIndex().getGunData().getReloadData().getType() == FeedType.INVENTORY;
               if (!useInventoryAmmo) {
                  boolean isFuel = api.getGunIndex().getGunData().getReloadData().getType() == FeedType.FUEL;
                  int needAmmoCount = api.getNeededAmmoAmount();
                  if (newPlayer.isCreative()) {
                     api.putAmmoInMagazine(needAmmoCount);
                  } else {
                     int consumedAmount = api.consumeAmmoFromPlayer(isFuel ? 1 : needAmmoCount);
                     api.putAmmoInMagazine(isFuel ? needAmmoCount * consumedAmount : consumedAmount);
                  }
               }
            }
         });
      }
   }
}
