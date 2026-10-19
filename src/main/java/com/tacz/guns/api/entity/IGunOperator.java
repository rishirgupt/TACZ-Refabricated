package com.tacz.guns.api.entity;

import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IGunOperator {
   static IGunOperator fromLivingEntity(LivingEntity entity) {
      return (IGunOperator)entity;
   }

   long getSynShootCoolDown();

   long getSynMeleeCoolDown();

   long getSynDrawCoolDown();

   boolean getSynIsBolting();

   ReloadState getSynReloadState();

   float getSynAimingProgress();

   boolean getSynIsAiming();

   float getSynSprintTime();

   void initialData();

   void draw(Supplier<ItemStack> var1);

   void bolt();

   void reload();

   void cancelReload();

   void fireSelect();

   void zoom();

   void melee();

   ShootResult shoot(Supplier<Float> var1, Supplier<Float> var2);

   ShootResult shoot(Supplier<Float> var1, Supplier<Float> var2, long var3);

   default ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
      return this.shoot(pitch, yaw, timestamp);
   }

   boolean needCheckAmmo();

   boolean consumesAmmoOrNot();

   boolean getProcessedSprintStatus(boolean var1);

   void aim(boolean var1);

   void crawl(boolean var1);

   void updateCacheProperty(AttachmentCacheProperty var1);

   @Nullable
   AttachmentCacheProperty getCacheProperty();

   ShooterDataHolder getDataHolder();

   boolean nextBulletIsTracer(int var1);
}
