package com.tacz.guns.mixin.common;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.KnockBackModifier;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.entity.shooter.LivingEntityAim;
import com.tacz.guns.entity.shooter.LivingEntityAmmoCheck;
import com.tacz.guns.entity.shooter.LivingEntityBolt;
import com.tacz.guns.entity.shooter.LivingEntityCrawl;
import com.tacz.guns.entity.shooter.LivingEntityDrawGun;
import com.tacz.guns.entity.shooter.LivingEntityFireSelect;
import com.tacz.guns.entity.shooter.LivingEntityHeat;
import com.tacz.guns.entity.shooter.LivingEntityMelee;
import com.tacz.guns.entity.shooter.LivingEntityReload;
import com.tacz.guns.entity.shooter.LivingEntityShoot;
import com.tacz.guns.entity.shooter.LivingEntitySpeedModifier;
import com.tacz.guns.entity.shooter.LivingEntitySprint;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.entity.sync.ModSyncedEntityData;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IGunOperator, KnockBackModifier {
   @Unique
   private final LivingEntity tacz$shooter = (LivingEntity)(Object)this;
   @Unique
   private final ShooterDataHolder tacz$data = new ShooterDataHolder();
   @Unique
   private final LivingEntityDrawGun tacz$draw = new LivingEntityDrawGun(this.tacz$shooter, this.tacz$data);
   @Unique
   private final LivingEntityAim tacz$aim = new LivingEntityAim(this.tacz$shooter, this.tacz$data);
   @Unique
   private final LivingEntityCrawl tacz$crawl = new LivingEntityCrawl(this.tacz$shooter, this.tacz$data);
   @Unique
   private final LivingEntityAmmoCheck tacz$ammoCheck = new LivingEntityAmmoCheck(this.tacz$shooter);
   @Unique
   private final LivingEntityFireSelect tacz$fireSelect = new LivingEntityFireSelect(this.tacz$shooter, this.tacz$data);
   @Unique
   private final LivingEntityMelee tacz$melee = new LivingEntityMelee(this.tacz$shooter, this.tacz$data, this.tacz$draw);
   @Unique
   private final LivingEntityShoot tacz$shoot = new LivingEntityShoot(this.tacz$shooter, this.tacz$data, this.tacz$draw);
   @Unique
   private final LivingEntityBolt tacz$bolt = new LivingEntityBolt(this.tacz$data, this.tacz$shooter, this.tacz$draw, this.tacz$shoot);
   @Unique
   private final LivingEntityReload tacz$reload = new LivingEntityReload(this.tacz$shooter, this.tacz$data, this.tacz$draw, this.tacz$shoot);
   @Unique
   private final LivingEntitySpeedModifier tacz$speed = new LivingEntitySpeedModifier(this.tacz$shooter, this.tacz$data);
   @Unique
   private final LivingEntitySprint tacz$sprint = new LivingEntitySprint(this.tacz$shooter, this.tacz$data);
   @Unique
   private final LivingEntityHeat tacz$heat = new LivingEntityHeat(this.tacz$shooter, this.tacz$data);

   public LivingEntityMixin(EntityType<?> entityType, Level level) {
      super(entityType, level);
   }

   @Unique
   @Override
   public long getSynShootCoolDown() {
      return ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.getValue(this.tacz$shooter);
   }

   @Override
   public long getSynMeleeCoolDown() {
      return ModSyncedEntityData.MELEE_COOL_DOWN_KEY.getValue(this.tacz$shooter);
   }

   @Unique
   @Override
   public long getSynDrawCoolDown() {
      return ModSyncedEntityData.DRAW_COOL_DOWN_KEY.getValue(this.tacz$shooter);
   }

   @Unique
   @Override
   public boolean getSynIsBolting() {
      return ModSyncedEntityData.IS_BOLTING_KEY.getValue(this.tacz$shooter);
   }

   @Unique
   @Override
   public ReloadState getSynReloadState() {
      return ModSyncedEntityData.RELOAD_STATE_KEY.getValue(this.tacz$shooter);
   }

   @Unique
   @Override
   public float getSynAimingProgress() {
      return ModSyncedEntityData.AIMING_PROGRESS_KEY.getValue(this.tacz$shooter);
   }

   @Unique
   @Override
   public float getSynSprintTime() {
      return ModSyncedEntityData.SPRINT_TIME_KEY.getValue(this.tacz$shooter);
   }

   @Unique
   @Override
   public boolean getSynIsAiming() {
      return ModSyncedEntityData.IS_AIMING_KEY.getValue(this.tacz$shooter);
   }

   @Unique
   @Override
   public void initialData() {
      this.tacz$data.initialData();
      this.tacz$data.currentGunItem = () -> this.tacz$shooter.getMainHandItem();
      AttachmentPropertyManager.postChangeEvent(this.tacz$shooter, this.tacz$shooter.getMainHandItem());
   }

   @Unique
   @Override
   public void draw(Supplier<ItemStack> gunItemSupplier) {
      this.tacz$draw.draw(gunItemSupplier);
   }

   @Unique
   @Override
   public void bolt() {
      this.tacz$bolt.bolt();
   }

   @Unique
   @Override
   public void reload() {
      this.tacz$reload.reload();
   }

   @Unique
   @Override
   public void cancelReload() {
      this.tacz$reload.cancelReload();
   }

   @Override
   public void melee() {
      this.tacz$melee.melee();
   }

   @Unique
   @Override
   public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
      return this.shoot(pitch, yaw, System.currentTimeMillis() - this.tacz$data.baseTimestamp);
   }

   @Unique
   @Override
   public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
      return this.tacz$shoot.shoot(pitch, yaw, timestamp);
   }

   @Unique
   @Override
   public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
      return this.tacz$shoot.shoot(pitch, yaw, timestamp, chargeProgress);
   }

   @Unique
   @Override
   public boolean needCheckAmmo() {
      return this.tacz$ammoCheck.needCheckAmmo();
   }

   @Unique
   @Override
   public boolean consumesAmmoOrNot() {
      return this.tacz$ammoCheck.consumesAmmoOrNot();
   }

   @Unique
   @Override
   public boolean getProcessedSprintStatus(boolean sprint) {
      return this.tacz$sprint.getProcessedSprintStatus(sprint);
   }

   @Unique
   @Override
   public void aim(boolean isAim) {
      this.tacz$aim.aim(isAim);
   }

   @Override
   public void crawl(boolean isCrawl) {
      this.tacz$crawl.crawl(isCrawl);
   }

   @Override
   public void updateCacheProperty(AttachmentCacheProperty cacheProperty) {
      this.tacz$data.cacheProperty = cacheProperty;
   }

   @Nullable
   @Override
   public AttachmentCacheProperty getCacheProperty() {
      return this.tacz$data.cacheProperty;
   }

   @Override
   public ShooterDataHolder getDataHolder() {
      return this.tacz$data;
   }

   @Override
   public boolean nextBulletIsTracer(int tracerCountInterval) {
      this.tacz$data.shootCount++;
      return tracerCountInterval == -1 ? false : this.tacz$data.shootCount % (tracerCountInterval + 1) == 0;
   }

   @Unique
   @Override
   public void fireSelect() {
      this.tacz$fireSelect.fireSelect();
   }

   @Unique
   @Override
   public void zoom() {
      this.tacz$aim.zoom();
   }

   @Inject(method = "tick", at = @At("RETURN"))
   private void onTickServerSide(CallbackInfo ci) {
      if (!this.level().isClientSide()) {
         ReloadState reloadState = this.tacz$reload.tickReloadState();
         this.tacz$aim.tickAimingProgress();
         this.tacz$aim.tickSprint();
         this.tacz$crawl.tickCrawling();
         this.tacz$bolt.tickBolt();
         this.tacz$melee.scheduleTickMelee();
         this.tacz$speed.updateSpeedModifier();
         this.tacz$heat.tickHeat();
         this.tacz$shooter.setSprinting(this.getProcessedSprintStatus(this.tacz$shooter.isSprinting()));
         ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$shoot.getShootCoolDown());
         ModSyncedEntityData.MELEE_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$melee.getMeleeCoolDown());
         ModSyncedEntityData.DRAW_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$draw.getDrawCoolDown());
         ModSyncedEntityData.IS_BOLTING_KEY.setValue(this.tacz$shooter, this.tacz$data.isBolting);
         ModSyncedEntityData.RELOAD_STATE_KEY.setValue(this.tacz$shooter, reloadState);
         ModSyncedEntityData.AIMING_PROGRESS_KEY.setValue(this.tacz$shooter, this.tacz$data.aimingProgress);
         ModSyncedEntityData.IS_AIMING_KEY.setValue(this.tacz$shooter, this.tacz$data.isAiming);
         ModSyncedEntityData.SPRINT_TIME_KEY.setValue(this.tacz$shooter, this.tacz$data.sprintTimeS);
      }
   }

   @Unique
   @Override
   public void resetKnockBackStrength() {
      this.tacz$data.knockbackStrength = -1.0;
   }

   @Unique
   @Override
   public double getKnockBackStrength() {
      return this.tacz$data.knockbackStrength;
   }

   @Unique
   @Override
   public void setKnockBackStrength(double strength) {
      this.tacz$data.knockbackStrength = strength;
   }
}
