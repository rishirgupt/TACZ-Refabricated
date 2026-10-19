package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunDrawEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunDraw;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class LivingEntityDrawGun {
   private final LivingEntity shooter;
   private final ShooterDataHolder data;

   public LivingEntityDrawGun(LivingEntity shooter, ShooterDataHolder data) {
      this.shooter = shooter;
      this.data = data;
   }

   public void draw(Supplier<ItemStack> gunItemSupplier) {
      this.data.initialData();
      if (this.data.drawTimestamp == -1L) {
         this.data.drawTimestamp = System.currentTimeMillis();
      }

      if (this.data.heatTimestamp == -1L) {
         this.data.heatTimestamp = System.currentTimeMillis();
      }

      long drawTime = System.currentTimeMillis() - this.data.drawTimestamp;
      if (drawTime >= 0L) {
         if ((float)drawTime < this.data.currentPutAwayTimeS * 1000.0F) {
            this.data.drawTimestamp = System.currentTimeMillis() + drawTime;
         } else {
            this.data.drawTimestamp = System.currentTimeMillis() + (long)(this.data.currentPutAwayTimeS * 1000.0F);
         }
      }

      ItemStack lastItem = this.data.currentGunItem == null ? ItemStack.EMPTY : this.data.currentGunItem.get();
      ((GunDrawEvent.Callback)GunDrawEvent.CALLBACK.invoker()).post(new GunDrawEvent(this.shooter, lastItem, gunItemSupplier.get(), LogicalSide.SERVER));
      NetworkHandler.sendToTrackingEntity(new ServerMessageGunDraw(this.shooter.getId(), lastItem, gunItemSupplier.get()), this.shooter);
      this.data.currentGunItem = gunItemSupplier;
      AttachmentPropertyManager.postChangeEvent(this.shooter, gunItemSupplier.get());
      this.updatePutAwayTime();
   }

   public long getDrawCoolDown() {
      if (this.data.currentGunItem == null) {
         return 0L;
      } else {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof IGun iGun) {
            Identifier gunId = iGun.getGunId(currentGunItem);
            Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
            return gunIndex.<Long>map(index -> {
               long coolDown = (long)(index.getGunData().getDrawTime() * 1000.0F) - (System.currentTimeMillis() - this.data.drawTimestamp);
               coolDown -= 5L;
               return coolDown < 0L ? 0L : coolDown;
            }).orElse(-1L);
         } else {
            return 0L;
         }
      }
   }

   private void updatePutAwayTime() {
      ItemStack gunItem = this.data.currentGunItem == null ? ItemStack.EMPTY : this.data.currentGunItem.get();
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun != null) {
         Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem));
         this.data.currentPutAwayTimeS = gunIndex.<Float>map(index -> index.getGunData().getPutAwayTime()).orElse(0.0F);
      } else {
         this.data.currentPutAwayTimeS = 0.0F;
      }
   }
}
