package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.event.common.GunFireSelectEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunFireSelect;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LivingEntityFireSelect {
   private final LivingEntity shooter;
   private final ShooterDataHolder data;

   public LivingEntityFireSelect(LivingEntity shooter, ShooterDataHolder data) {
      this.shooter = shooter;
      this.data = data;
   }

   public void fireSelect() {
      if (this.data.currentGunItem != null) {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof IGun iGun) {
            GunFireSelectEvent var5 = new GunFireSelectEvent(this.shooter, currentGunItem, LogicalSide.SERVER);
            ((GunFireSelectEvent.Callback)GunFireSelectEvent.CALLBACK.invoker()).post(var5);
            if (!var5.isCanceled()) {
               NetworkHandler.sendToTrackingEntity(new ServerMessageGunFireSelect(this.shooter.getId(), currentGunItem), this.shooter);
               if (iGun instanceof AbstractGunItem logicGun) {
                  logicGun.fireSelect(this.data, currentGunItem);
                  AttachmentPropertyManager.postChangeEvent(this.shooter, currentGunItem);
               }
            }
         }
      }
   }
}
