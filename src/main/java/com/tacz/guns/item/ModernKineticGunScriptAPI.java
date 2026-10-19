package com.tacz.guns.item;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.util.LuaEntityAccessor;
import com.tacz.guns.api.util.LuaNbtAccessor;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunFire;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.SilenceModifier;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunHeatData;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import com.tacz.guns.sound.SoundManager;
import com.tacz.guns.util.AttachmentDataUtils;
import com.tacz.guns.util.CycleTaskHelper;
import it.unimi.dsi.fastutil.Pair;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class ModernKineticGunScriptAPI {
   public static String MARKER = "ScriptAPI";
   private LivingEntity shooter;
   private ShooterDataHolder dataHolder;
   private ItemStack itemStack;
   private AbstractGunItem abstractGunItem;
   private CommonGunIndex gunIndex;
   private Identifier gunId;
   private Identifier gunDisplayId;
   private Supplier<Float> pitchSupplier;
   private Supplier<Float> yawSupplier;
   private LuaNbtAccessor nbtUtil;
   private LuaEntityAccessor entityAccessor;
   private float shotDamageMultiplier = 1.0F;
   private float projectileSpeedMultiplier = 1.0F;

   public LuaValue getCachedProperty(String id) {
      GunProperty<?> property = GunProperties.all().get(id);
      if (property == null) {
         throw new LuaError("unknown gun property: " + id);
      }

      AttachmentCacheProperty cache = IGunOperator.fromLivingEntity(this.shooter).getCacheProperty();
      return cache == null ? LuaValue.NIL : CoerceJavaToLua.coerce(cache.getCache(id));
   }

   public void shootOnce(boolean consumeAmmo) {
      GunData gunData = this.gunIndex.getGunData();
      BulletData bulletData = this.gunIndex.getBulletData();
      IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.shooter);
      float shotDamageMultiplier = this.shotDamageMultiplier;
      float projectileSpeedMultiplier = this.projectileSpeedMultiplier;
      AttachmentCacheProperty cacheProperty = gunOperator.getCacheProperty();
      if (cacheProperty != null) {
         float heatInaccuracy = 1.0F;
         if (this.hasHeatData()) {
            GunHeatData heatData = Objects.requireNonNull(this.gunIndex.getGunData().getHeatData());
            float heatMax = this.modifyProperty("max_heat", Float.class, heatData.getHeatMax());
            float heatPercentage = this.getHeatAmount() / heatMax;
            heatInaccuracy *= Mth.lerp(heatPercentage, heatData.getMinInaccuracy(), heatData.getMaxInaccuracy());
         }

         InaccuracyType inaccuracyType = InaccuracyType.getInaccuracyType(this.shooter);
         float unmodifiedInaccuracy = cacheProperty.getCache(GunProperties.INACCURACY).get(inaccuracyType) * heatInaccuracy;
         float inaccuracy = Math.max(0.0F, this.modifyProperty(GunProperties.INACCURACY, Float.class, unmodifiedInaccuracy));
         Pair<Integer, Boolean> silence = cacheProperty.getCache(SilenceModifier.ID);
         int soundDistance = this.modifyProperty("sound_distance", Integer.class, (Integer)silence.left());
         boolean useSilenceSound = (Boolean)silence.right();
         float speed = this.modifyProperty(GunProperties.AMMO_SPEED, Float.class, cacheProperty.getCache(GunProperties.AMMO_SPEED));
         speed = (float)(speed * (Double)AmmoConfig.GLOBAL_BULLET_SPEED_MODIFIER.get());
         float processedSpeed = Mth.clamp(speed / 20.0F * projectileSpeedMultiplier, 0.0F, Float.MAX_VALUE);
         int bulletAmount = this.modifyProperty("bullet_amount", Integer.class, Math.max(bulletData.getBulletAmount(), 1));
         FireMode fireMode = this.abstractGunItem.getFireMode(this.itemStack);
         int cycles = this.modifyProperty("burst_count", Integer.class, fireMode == FireMode.BURST ? gunData.getBurstData().getCount() : 1);
         long period = this.modifyProperty("burst_shoot_interval", Long.class, fireMode == FireMode.BURST ? gunData.getBurstShootInterval() : 1L);
         CycleTaskHelper.addCycleTask(
            () -> {
               if (this.shooter.isDeadOrDying()) {
                  return false;
               }

               if (this.shooter.getMainHandItem().equals(this.itemStack) && !this.shooter.getMainHandItem().isEmpty()) {
                  GunFireEvent gunFireEvent = new GunFireEvent(this.shooter, this.itemStack, LogicalSide.SERVER);
                  ((GunFireEvent.Callback)GunFireEvent.CALLBACK.invoker()).post(gunFireEvent);
                  boolean fire = !gunFireEvent.isCanceled();
                  if (fire) {
                     NetworkHandler.sendToTrackingEntity(new ServerMessageGunFire(this.shooter.getId(), this.itemStack), this.shooter);
                     if (consumeAmmo && !this.reduceAmmoOnce()) {
                        return false;
                     }

                     if (this.gunIndex.getGunData().hasHeatData()) {
                        Optional.ofNullable(this.gunIndex.getScript())
                           .map(script -> this.checkFunction(script.get("handle_shoot_heat")))
                           .ifPresentOrElse(func -> func.call(CoerceJavaToLua.coerce(this)), this::handleShootHeat);
                     }

                     float pitch = this.pitchSupplier != null ? this.pitchSupplier.get() : this.shooter.getXRot();
                     float yaw = this.yawSupplier != null ? this.yawSupplier.get() : this.shooter.getYRot();
                     Level world = this.shooter.level();
                     Identifier ammoId = gunData.getAmmoId();

                     for (int i = 0; i < bulletAmount; i++) {
                        boolean isTracer = bulletData.hasTracerAmmo() && gunOperator.nextBulletIsTracer(bulletData.getTracerCountInterval());
                        EntityKineticBullet bullet = new EntityKineticBullet(
                           world, this.shooter, this.itemStack, ammoId, this.gunId, this.gunDisplayId, isTracer, gunData, bulletData
                        );
                        bullet.applyShotgunDamageSpread(bulletAmount);
                        bullet.setShotDamageMultiplier(shotDamageMultiplier);
                        this.abstractGunItem.doBulletSpread(this.dataHolder, this.itemStack, this.shooter, bullet, i, processedSpeed, inaccuracy, pitch, yaw);
                        world.addFreshEntity(bullet);
                     }

                     if (soundDistance > 0) {
                        String soundId = useSilenceSound ? SoundManager.SILENCE_3P_SOUND : SoundManager.SHOOT_3P_SOUND;
                        SoundManager.sendSoundToNearby(
                           this.shooter,
                           soundDistance,
                           this.gunId,
                           this.gunDisplayId,
                           soundId,
                           0.8F,
                           0.9F + this.shooter.getRandom().nextFloat() * 0.125F
                        );
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            },
            period,
            cycles
         );
      }
   }

   private <T> T modifyProperty(GunProperty<?> property, Class<T> type, T value) {
      return this.abstractGunItem.modifyProperty(this.dataHolder, this.itemStack, this.shooter, property, type, value);
   }

   private <T> T modifyProperty(String id, Class<T> type, T value) {
      return this.abstractGunItem.modifyProperty(this.dataHolder, this.itemStack, this.shooter, id, type, value);
   }

   public void handleShootHeat() {
      GunHeatData heatData = this.gunIndex.getGunData().getHeatData();
      if (heatData != null) {
         float newHeat = Math.min(this.abstractGunItem.getHeatAmount(this.itemStack) + heatData.getHeatPerShot(), heatData.getHeatMax());
         this.abstractGunItem.setHeatAmount(this.itemStack, newHeat);
         if (newHeat >= heatData.getHeatMax()) {
            this.abstractGunItem.setOverheatLocked(this.itemStack, true);
         }
      }
   }

   public boolean reduceAmmoOnce() {
      Bolt boltType = TimelessAPI.getCommonGunIndex(this.abstractGunItem.getGunId(this.itemStack)).map(index -> index.getGunData().getBolt()).orElse(null);
      boolean hasAmmoInBarrel = this.abstractGunItem.hasBulletInBarrel(this.itemStack) && boltType != Bolt.OPEN_BOLT;
      boolean hasInventoryAmmo = this.abstractGunItem.hasInventoryAmmo(this.shooter, this.itemStack, this.isReloadingNeedConsumeAmmo());
      boolean noAmmo = this.useInventoryAmmo() && !hasInventoryAmmo || !this.useInventoryAmmo() && this.abstractGunItem.getCurrentAmmoCount(this.itemStack) < 1;
      if (boltType == null) {
         return false;
      }

      if (boltType == Bolt.MANUAL_ACTION) {
         if (!hasAmmoInBarrel) {
            return false;
         }

         this.abstractGunItem.setBulletInBarrel(this.itemStack, false);
         return true;
      } else if (boltType == Bolt.CLOSED_BOLT) {
         if (!noAmmo) {
            if (this.useInventoryAmmo()) {
               return this.consumeAmmoFromPlayer(1) == 1;
            }

            this.abstractGunItem.reduceCurrentAmmoCount(this.itemStack);
            return true;
         } else {
            if (!hasAmmoInBarrel) {
               return false;
            }

            this.abstractGunItem.setBulletInBarrel(this.itemStack, false);
            return true;
         }
      } else if (boltType == Bolt.OPEN_BOLT) {
         if (noAmmo) {
            return false;
         }

         if (this.useInventoryAmmo()) {
            return this.consumeAmmoFromPlayer(1) == 1;
         }

         this.abstractGunItem.reduceCurrentAmmoCount(this.itemStack);
         return true;
      } else {
         return false;
      }
   }

   public long getReloadTime() {
      return this.dataHolder.reloadTimestamp == -1L ? 0L : System.currentTimeMillis() - this.dataHolder.reloadTimestamp;
   }

   public long getBoltTime() {
      return !this.dataHolder.isBolting ? 0L : System.currentTimeMillis() - this.dataHolder.boltTimestamp;
   }

   public long getShootInterval() {
      FireMode fireMode = this.abstractGunItem.getFireMode(this.itemStack);
      if (fireMode == FireMode.BURST) {
         long coolDown = (long)(this.gunIndex.getGunData().getBurstData().getMinInterval() * 1000.0);
         coolDown -= 5L;
         return Math.max(coolDown, 0L);
      } else {
         long coolDown = this.gunIndex.getGunData().getShootInterval(this.shooter, fireMode, this.itemStack);
         coolDown -= 5L;
         return Math.max(coolDown, 0L);
      }
   }

   public long getLastShootTimestamp() {
      return this.dataHolder.lastShootTimestamp + this.dataHolder.baseTimestamp;
   }

   public void adjustShootInterval(long alpha) {
      this.dataHolder.shootTimestamp += alpha;
   }

   public void adjustReloadTime(long alpha) {
      this.dataHolder.reloadTimestamp -= alpha;
   }

   public void adjustBoltTime(long alpha) {
      this.dataHolder.boltTimestamp -= alpha;
   }

   public float getAimingProgress() {
      return this.dataHolder.aimingProgress;
   }

   public float getChargeProgress() {
      return this.dataHolder.chargeProgress;
   }

   public boolean hasChargeData() {
      return this.getChargeData() != null;
   }

   public float getMaxCharge() {
      ChargeData chargeData = this.getChargeData();
      return chargeData == null ? 0.0F : chargeData.getMaxCharge();
   }

   public float getFireThreshold() {
      ChargeData chargeData = this.getChargeData();
      return chargeData == null ? 0.0F : chargeData.getFireThreshold();
   }

   public float getChargeRatio() {
      float maxCharge = this.getMaxCharge();
      return maxCharge <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(this.getChargeProgress() / maxCharge, 1.0F));
   }

   public void setShotDamageMultiplier(float multiplier) {
      this.shotDamageMultiplier = this.clampMultiplier(multiplier);
   }

   public float getShotDamageMultiplier() {
      return this.shotDamageMultiplier;
   }

   public void setProjectileSpeedMultiplier(float multiplier) {
      this.projectileSpeedMultiplier = this.clampMultiplier(multiplier);
   }

   public float getProjectileSpeedMultiplier() {
      return this.projectileSpeedMultiplier;
   }

   private float clampMultiplier(float multiplier) {
      return Mth.clamp(multiplier, 0.0F, 256.0F);
   }

   public int getReloadStateType() {
      return this.dataHolder.reloadStateType.ordinal();
   }

   public int getFireMode() {
      return this.abstractGunItem.getFireMode(this.itemStack).ordinal();
   }

   public boolean isShootingNeedConsumeAmmo() {
      return IGunOperator.fromLivingEntity(this.shooter).consumesAmmoOrNot();
   }

   public boolean isReloadingNeedConsumeAmmo() {
      return IGunOperator.fromLivingEntity(this.shooter).needCheckAmmo();
   }

   public int getNeededAmmoAmount() {
      int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(this.itemStack, this.gunIndex.getGunData());
      int currentAmmoCount = this.abstractGunItem.getCurrentAmmoCount(this.itemStack);
      return maxAmmoCount - currentAmmoCount;
   }

   public int getAmmoAmount() {
      return this.abstractGunItem.getCurrentAmmoCount(this.itemStack);
   }

   public int getMaxAmmoCount() {
      return AttachmentDataUtils.getAmmoCountWithAttachment(this.itemStack, this.gunIndex.getGunData());
   }

   public int getMagExtentLevel() {
      return AttachmentDataUtils.getMagExtendLevel(this.itemStack, this.gunIndex.getGunData());
   }

   public int consumeAmmoFromPlayer(int neededAmount) {
      if (this.useInventoryAmmo() && !this.isReloadingNeedConsumeAmmo()) {
         return neededAmount;
      } else {
         return this.abstractGunItem.useDummyAmmo(this.itemStack)
            ? this.abstractGunItem.findAndExtractDummyAmmo(this.itemStack, neededAmount)
            : this.shooter.tacz$getItemHandler(null).map(cap -> this.abstractGunItem.findAndExtractInventoryAmmo(cap, this.itemStack, neededAmount)).orElse(0);
      }
   }

   public boolean hasAmmoToConsume() {
      if (!this.isReloadingNeedConsumeAmmo()) {
         return true;
      } else {
         return this.abstractGunItem.useDummyAmmo(this.itemStack)
            ? this.abstractGunItem.getDummyAmmoAmount(this.itemStack) > 0
            : this.shooter.tacz$getItemHandler(null).map(cap -> {
               for (int i = 0; i < cap.getSlots(); i++) {
                  ItemStack checkAmmoStack = cap.getStackInSlot(i);
                  if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(this.itemStack, checkAmmoStack)) {
                     return true;
                  }

                  if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(this.itemStack, checkAmmoStack)) {
                     return true;
                  }
               }

               return false;
            }).orElse(false);
      }
   }

   public int putAmmoInMagazine(int amount) {
      if (amount < 0) {
         return 0;
      } else {
         int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(this.itemStack, this.gunIndex.getGunData());
         int currentAmmoCount = this.abstractGunItem.getCurrentAmmoCount(this.itemStack);
         int newAmmoCount = currentAmmoCount + amount;
         if (maxAmmoCount < newAmmoCount) {
            this.abstractGunItem.setCurrentAmmoCount(this.itemStack, maxAmmoCount);
            return newAmmoCount - maxAmmoCount;
         } else {
            this.abstractGunItem.setCurrentAmmoCount(this.itemStack, newAmmoCount);
            return 0;
         }
      }
   }

   public int removeAmmoFromMagazine(int amount) {
      if (amount < 0) {
         return 0;
      } else {
         int currentAmmoCount = this.abstractGunItem.getCurrentAmmoCount(this.itemStack);
         if (currentAmmoCount < amount) {
            this.abstractGunItem.setCurrentAmmoCount(this.itemStack, 0);
            return currentAmmoCount;
         } else {
            this.abstractGunItem.setCurrentAmmoCount(this.itemStack, currentAmmoCount - amount);
            return amount;
         }
      }
   }

   public int getAmmoCountInMagazine() {
      return this.abstractGunItem.getCurrentAmmoCount(this.itemStack);
   }

   public boolean hasAmmoInBarrel() {
      Bolt boltType = this.gunIndex.getGunData().getBolt();
      return boltType != Bolt.OPEN_BOLT && this.abstractGunItem.hasBulletInBarrel(this.itemStack);
   }

   public void setAmmoInBarrel(boolean ammoInBarrel) {
      this.abstractGunItem.setBulletInBarrel(this.itemStack, ammoInBarrel);
   }

   public void cacheScriptData(LuaValue luaValue) {
      this.dataHolder.scriptData = luaValue;
   }

   public LuaValue getCachedScriptData() {
      return this.dataHolder.scriptData;
   }

   public LuaTable getScriptParams() {
      LuaTable param = this.gunIndex.getScriptParam();
      return param == null ? new LuaTable() : param;
   }

   public void safeAsyncTask(LuaValue value, long delayMs, long periodMs, int cycles) {
      LuaFunction func = value.checkfunction();
      CycleTaskHelper.addCycleTask(() -> func.call().checkboolean(), delayMs, periodMs, cycles);
   }

   public long getCurrentTimestamp() {
      return System.currentTimeMillis();
   }

   public String getAttachment(String type) {
      try {
         AttachmentType t = AttachmentType.valueOf(type);
         return this.abstractGunItem.getAttachmentId(this.itemStack, t).toString();
      } catch (IllegalArgumentException e) {
         return DefaultAssets.EMPTY_ATTACHMENT_ID.toString();
      }
   }

   public LuaNbtAccessor getNbt() {
      return this.nbtUtil;
   }

   public LuaEntityAccessor getEntityUtil() {
      if (this.entityAccessor == null) {
         this.entityAccessor = new LuaEntityAccessor(this.shooter);
      }

      return this.entityAccessor;
   }

   public void setShooter(LivingEntity shooter) {
      this.shooter = shooter;
   }

   public void setItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
      this.initGunItem();
   }

   public void setPitchSupplier(Supplier<Float> pitchSupplier) {
      this.pitchSupplier = pitchSupplier;
   }

   public void setYawSupplier(Supplier<Float> yawSupplier) {
      this.yawSupplier = yawSupplier;
   }

   public LivingEntity getShooter() {
      return this.shooter;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public AbstractGunItem getAbstractGunItem() {
      return this.abstractGunItem;
   }

   public CommonGunIndex getGunIndex() {
      return this.gunIndex;
   }

   public void setHeatAmount(float amount) {
      this.abstractGunItem.setHeatAmount(this.itemStack, amount);
   }

   public float getHeatAmount() {
      return this.abstractGunItem.getHeatAmount(this.itemStack);
   }

   public boolean hasHeatData() {
      return this.gunIndex.getGunData().getHeatData() != null;
   }

   public float getHeatMinRpm() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getMinRpmMod() : 0.0F;
   }

   public float getHeatMaxRpm() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getMaxRpmMod() : 0.0F;
   }

   public float getHeatMinInaccuracy() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getMinInaccuracy() : 0.0F;
   }

   public float getHeatMaxInaccuracy() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getMaxInaccuracy() : 0.0F;
   }

   public float getHeatMax() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getHeatMax() : 0.0F;
   }

   public float getHeatPerShot() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getHeatPerShot() : 0.0F;
   }

   public boolean isOverheatLocked() {
      return this.abstractGunItem.isOverheatLocked(this.itemStack);
   }

   public void setOverheatLocked(boolean locked) {
      this.abstractGunItem.setOverheatLocked(this.itemStack, locked);
   }

   public long getOverheatTime() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getOverHeatTime() : 0L;
   }

   public long getCoolingDelay() {
      return this.hasHeatData() ? this.gunIndex.getGunData().getHeatData().getCoolingDelay() : 0L;
   }

   public float calcHeatReduction(long heatTimestamp) {
      GunHeatData heatData = this.gunIndex.getGunData().getHeatData();
      return heatData != null ? (float)(System.currentTimeMillis() - heatTimestamp) / 10000.0F * heatData.getCoolingMultiplier() : 0.0F;
   }

   public int getBoltByInt() {
      Bolt bolt = this.gunIndex.getGunData().getBolt();
      if (bolt == Bolt.MANUAL_ACTION) {
         return 1;
      } else if (bolt == Bolt.CLOSED_BOLT) {
         return 2;
      } else {
         return bolt == Bolt.OPEN_BOLT ? 3 : 0;
      }
   }

   public Bolt getBolt() {
      return this.gunIndex.getGunData().getBolt();
   }

   private ChargeData getChargeData() {
      FireMode fireMode = this.abstractGunItem.getFireMode(this.itemStack);
      return this.gunIndex.getGunData().getChargeData(fireMode);
   }

   public void setDataHolder(ShooterDataHolder dataHolder) {
      this.dataHolder = dataHolder;
   }

   public boolean useInventoryAmmo() {
      return this.abstractGunItem.useInventoryAmmo(this.itemStack);
   }

   ShooterDataHolder getDataHolder() {
      return this.dataHolder;
   }

   private void initGunItem() {
      if (this.itemStack != null && this.itemStack.getItem() instanceof AbstractGunItem gunItem) {
         this.gunId = gunItem.getGunId(this.itemStack);
         this.gunDisplayId = gunItem.getGunDisplayId(this.itemStack);
         Optional var3 = TimelessAPI.getCommonGunIndex(this.gunId);
         this.gunIndex = (CommonGunIndex)var3.orElse(null);
         this.abstractGunItem = gunItem;
         if (this.itemStack.has(DataComponents.CUSTOM_DATA)) {
            this.nbtUtil = new LuaNbtAccessor(((CustomData)this.itemStack.get(DataComponents.CUSTOM_DATA)).copyTag());
         }
      } else {
         this.gunIndex = null;
         this.abstractGunItem = null;
      }
   }

   private LuaFunction checkFunction(LuaValue luaValue) {
      if (luaValue.isfunction()) {
         return (LuaFunction)luaValue;
      } else if (luaValue.isnil()) {
         return null;
      } else {
         throw new LuaError("bad argument: function or nil expected, got " + luaValue.typename());
      }
   }
}
