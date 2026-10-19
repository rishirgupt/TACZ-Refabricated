package com.tacz.guns.item;

import com.google.common.base.Suppliers;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import com.tacz.guns.command.sub.DebugCommand;
import com.tacz.guns.debug.GunMeleeDebug;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.attachment.EffectData;
import com.tacz.guns.resource.pojo.data.attachment.MeleeData;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunDefaultMeleeData;
import com.tacz.guns.resource.pojo.data.gun.GunHeatData;
import com.tacz.guns.resource.pojo.data.gun.GunMeleeData;
import com.tacz.guns.resource.pojo.data.gun.GunReloadData;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item.Properties;
import org.joml.Vector2d;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.slf4j.MarkerFactory;

public class ModernKineticGunItem extends AbstractGunItem implements GunItemDataAccessor {
   public static final String TYPE_NAME = "modern_kinetic";
   private static final DoubleFunction<AttributeModifier> AM_FACTORY = amount -> new AttributeModifier(
      Identifier.fromNamespaceAndPath("tacz", "melee_damage"), amount, Operation.ADD_VALUE
   );
   public final ModernKineticGunItem.DefaultPropertyModification defaultPropertyModification = new ModernKineticGunItem.DefaultPropertyModification();

   public ModernKineticGunItem() {
      super(new Properties().stacksTo(1));
   }

   @Override
   public boolean startBolt(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      CommonGunIndex gunIndex = api.getGunIndex();
      return gunIndex == null
         ? false
         : Optional.ofNullable(gunIndex.getScript())
            .map(script -> this.checkFunction(script.get("start_bolt")))
            .map(func -> func.call(CoerceJavaToLua.coerce(api)).checkboolean())
            .orElse(true);
   }

   @Override
   public boolean tickBolt(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      CommonGunIndex gunIndex = api.getGunIndex();
      return gunIndex == null
         ? false
         : Optional.ofNullable(gunIndex.getScript())
            .map(script -> this.checkFunction(script.get("tick_bolt")))
            .map(func -> func.call(CoerceJavaToLua.coerce(api)).checkboolean())
            .orElseGet(() -> this.defaultTickBolt(api));
   }

   @Override
   public void shoot(ShooterDataHolder dataHolder, ItemStack gunItem, Supplier<Float> pitch, Supplier<Float> yaw, LivingEntity shooter) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      api.setPitchSupplier(pitch);
      api.setYawSupplier(yaw);
      CommonGunIndex gunIndex = api.getGunIndex();
      if (gunIndex != null) {
         Optional.ofNullable(gunIndex.getScript())
            .map(script -> this.checkFunction(script.get("shoot")))
            .ifPresentOrElse(func -> func.call(CoerceJavaToLua.coerce(api)), () -> api.shootOnce(api.isShootingNeedConsumeAmmo()));
      }
   }

   @Override
   public boolean startReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      CommonGunIndex gunIndex = api.getGunIndex();
      return gunIndex == null
         ? false
         : Optional.ofNullable(gunIndex.getScript())
            .map(script -> this.checkFunction(script.get("start_reload")))
            .map(func -> func.call(CoerceJavaToLua.coerce(api)).checkboolean())
            .orElse(true);
   }

   @Override
   public ReloadState tickReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      CommonGunIndex gunIndex = api.getGunIndex();
      return gunIndex == null
         ? new ReloadState()
         : Optional.ofNullable(gunIndex.getScript()).map(script -> this.checkFunction(script.get("tick_reload"))).map(func -> {
            ReloadState reloadState = new ReloadState();
            Varargs varargs = func.invoke(CoerceJavaToLua.coerce(api));
            int typeOrdinary = varargs.arg(1).checkint();
            long countDown = varargs.arg(2).checklong();
            reloadState.setStateType(ReloadState.StateType.values()[typeOrdinary]);
            reloadState.setCountDown(countDown);
            return reloadState;
         }).orElseGet(() -> this.defaultTickReload(api));
   }

   @Override
   public void interruptReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      CommonGunIndex gunIndex = api.getGunIndex();
      if (gunIndex != null) {
         Optional.ofNullable(gunIndex.getScript())
            .map(script -> this.checkFunction(script.get("interrupt_reload")))
            .ifPresent(func -> func.call(CoerceJavaToLua.coerce(api)));
      }
   }

   @Override
   public void melee(ShooterDataHolder dataHolder, LivingEntity user, ItemStack gunItem) {
      Identifier gunId = this.getGunId(gunItem);
      TimelessAPI.getCommonGunIndex(gunId)
         .ifPresent(
            gunIndex -> {
               GunMeleeData meleeData = gunIndex.getGunData().getMeleeData();
               float distance = meleeData.getDistance();
               Identifier muzzleId = this.getAttachmentId(gunItem, AttachmentType.MUZZLE);
               MeleeData muzzleData = this.getMeleeData(muzzleId);
               if (muzzleData != null) {
                  this.doMelee(
                     user,
                     distance,
                     muzzleData.getDistance(),
                     muzzleData.getRangeAngle(),
                     muzzleData.getKnockback(),
                     muzzleData.getDamage(),
                     muzzleData.getEffects()
                  );
               } else {
                  Identifier stockId = this.getAttachmentId(gunItem, AttachmentType.STOCK);
                  MeleeData stockData = this.getMeleeData(stockId);
                  if (stockData != null) {
                     this.doMelee(
                        user,
                        distance,
                        stockData.getDistance(),
                        stockData.getRangeAngle(),
                        stockData.getKnockback(),
                        stockData.getDamage(),
                        stockData.getEffects()
                     );
                  } else {
                     GunDefaultMeleeData defaultData = meleeData.getDefaultMeleeData();
                     if (defaultData != null) {
                        this.doMelee(
                           user,
                           distance,
                           defaultData.getDistance(),
                           defaultData.getRangeAngle(),
                           defaultData.getKnockback(),
                           defaultData.getDamage(),
                           Collections.emptyList()
                        );
                     }
                  }
               }
            }
         );
   }

   @Override
   public void tickHeat(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      long heatTimestamp = dataHolder.heatTimestamp;
      CommonGunIndex gunIndex = api.getGunIndex();
      if (gunIndex != null) {
         Optional.ofNullable(gunIndex.getScript())
            .map(script -> this.checkFunction(script.get("tick_heat")))
            .ifPresentOrElse(
               func -> func.call(CoerceJavaToLua.coerce(api), LuaValue.valueOf(heatTimestamp)), () -> this.defaultTickHeat(heatTimestamp, gunItem)
            );
      }
   }

   private void defaultTickHeat(long heatTimestamp, ItemStack gunItem) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun != null) {
         TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem)).map(index -> index.getGunData().getHeatData()).ifPresent(heatData -> {
            if (!(iGun.getHeatAmount(gunItem) <= 0.0F)) {
               if (iGun.isOverheatLocked(gunItem)) {
                  this.tickLocked(iGun, gunItem, heatData, heatTimestamp);
               } else {
                  this.tickNormal(iGun, gunItem, heatData, heatTimestamp);
               }
            }
         });
      }
   }

   public void tickLocked(IGun iGun, ItemStack gunStack, GunHeatData heatData, long heatTimestamp) {
      if (System.currentTimeMillis() - heatTimestamp >= heatData.getOverHeatTime()) {
         float heatAmount = iGun.getHeatAmount(gunStack) - (float)(System.currentTimeMillis() - heatTimestamp) / 10000.0F * heatData.getCoolingMultiplier();
         iGun.setHeatAmount(gunStack, heatAmount);
         if (heatAmount <= 0.0F) {
            iGun.setOverheatLocked(gunStack, false);
         }
      }
   }

   public void tickNormal(IGun iGun, ItemStack gunStack, GunHeatData heatData, long heatTimestamp) {
      if (System.currentTimeMillis() - heatTimestamp >= heatData.getCoolingDelay()) {
         float heatAmount = iGun.getHeatAmount(gunStack) - (float)(System.currentTimeMillis() - heatTimestamp) / 10000.0F * heatData.getCoolingMultiplier();
         iGun.setHeatAmount(gunStack, heatAmount);
      }
   }

   @Override
   public <T> T modifyProperty(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, String luaMethodName, String id, Class<T> type, T original) {
      ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
      api.setItemStack(gunItem);
      api.setShooter(shooter);
      api.setDataHolder(dataHolder);
      CommonGunIndex gunIndex = api.getGunIndex();
      if (gunIndex == null) {
         return original;
      }

      T afterDefaultModification = this.defaultPropertyModification.modify(gunItem, shooter, gunIndex, id, original);

      try {
         return Optional.ofNullable(gunIndex.getScript())
            .map(script -> this.checkFunction(script.get(luaMethodName)))
            .map(func -> func.call(CoerceJavaToLua.coerce(api), LuaValue.valueOf(id), CoerceJavaToLua.coerce(afterDefaultModification)))
            .map(luaValue -> type.cast(CoerceLuaToJava.coerce(luaValue, type)))
            .orElse(afterDefaultModification);
      } catch (Exception exception) {
         GunMod.LOGGER.warn(MarkerFactory.getMarker("Gun Script"), "Failed to modify gun property {}", id, exception);
         return afterDefaultModification;
      }
   }

   @Override
   public void doBulletSpread(
      ShooterDataHolder dataHolder,
      ItemStack gunItem,
      LivingEntity shooter,
      Projectile projectile,
      int bulletCnt,
      float processedSpeed,
      float inaccuracy,
      float pitch,
      float yaw
   ) {
      if (projectile instanceof EntityKineticBullet bullet) {
         ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
         api.setItemStack(gunItem);
         api.setShooter(shooter);
         api.setDataHolder(dataHolder);
         CommonGunIndex gunIndex = api.getGunIndex();
         if (gunIndex != null) {
            Optional.ofNullable(gunIndex.getScript())
               .map(script -> this.checkFunction(script.get("calcSpread")))
               .map(func -> func.call(CoerceJavaToLua.coerce(api), LuaValue.valueOf(bulletCnt), LuaValue.valueOf(inaccuracy)))
               .map(luaValue -> {
                  if (luaValue.istable()) {
                     LuaTable table = luaValue.checktable();
                     return new Vector2d(table.get(1).checkdouble(), table.get(2).checkdouble());
                  } else {
                     return null;
                  }
               })
               .ifPresentOrElse(
                  vector2d -> bullet.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, vector2d),
                  () -> bullet.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, inaccuracy)
               );
         }
      }
   }

   private boolean defaultTickBolt(ModernKineticGunScriptAPI api) {
      GunData gunData = api.getGunIndex().getGunData();
      long boltActionTime = (long)(gunData.getBoltActionTime() * 1000.0F);
      float rawBoltFeedTime = gunData.getBoltFeedTime();
      long boltFeedTime = rawBoltFeedTime == -1.0F ? boltActionTime : (long)(gunData.getBoltFeedTime() * 1000.0F);
      if (api.getBoltTime() < boltFeedTime) {
         return true;
      }

      if (!api.hasAmmoInBarrel()) {
         if (api.useInventoryAmmo()) {
            if (api.consumeAmmoFromPlayer(1) == 1) {
               api.setAmmoInBarrel(true);
            }
         } else if (api.removeAmmoFromMagazine(1) != 0) {
            api.setAmmoInBarrel(true);
         }
      }

      return api.getBoltTime() < boltActionTime;
   }

   private ReloadState defaultTickReload(ModernKineticGunScriptAPI api) {
      CommonGunIndex gunIndex = api.getGunIndex();
      GunData gunData = gunIndex.getGunData();
      GunReloadData reloadData = gunData.getReloadData();
      ReloadState.StateType oldStateType = ReloadState.StateType.values()[api.getReloadStateType()];
      long progressTime = api.getReloadTime();
      long countDown;
      ReloadState.StateType stateType;
      if (oldStateType.isReloadingEmpty()) {
         long feedTime = (long)(reloadData.getFeed().getEmptyTime() * 1000.0F);
         long finishingTime = (long)(reloadData.getCooldown().getEmptyTime() * 1000.0F);
         if (progressTime < feedTime) {
            stateType = ReloadState.StateType.EMPTY_RELOAD_FEEDING;
            countDown = feedTime - progressTime;
         } else if (progressTime < finishingTime) {
            stateType = ReloadState.StateType.EMPTY_RELOAD_FINISHING;
            countDown = finishingTime - progressTime;
         } else {
            stateType = ReloadState.StateType.NOT_RELOADING;
            countDown = -1L;
         }
      } else if (oldStateType.isReloadingTactical()) {
         long feedTime = (long)(reloadData.getFeed().getTacticalTime() * 1000.0F);
         long finishingTime = (long)(reloadData.getCooldown().getTacticalTime() * 1000.0F);
         if (progressTime < feedTime) {
            stateType = ReloadState.StateType.TACTICAL_RELOAD_FEEDING;
            countDown = feedTime - progressTime;
         } else if (progressTime < finishingTime) {
            stateType = ReloadState.StateType.TACTICAL_RELOAD_FINISHING;
            countDown = finishingTime - progressTime;
         } else {
            stateType = ReloadState.StateType.NOT_RELOADING;
            countDown = -1L;
         }
      } else {
         stateType = ReloadState.StateType.NOT_RELOADING;
         countDown = -1L;
      }

      if (oldStateType == ReloadState.StateType.EMPTY_RELOAD_FEEDING && oldStateType != stateType) {
         this.defaultReloadFinishing(api, false);
      }

      if (oldStateType == ReloadState.StateType.TACTICAL_RELOAD_FEEDING && oldStateType != stateType) {
         this.defaultReloadFinishing(api, true);
      }

      ReloadState reloadState = new ReloadState();
      reloadState.setStateType(stateType);
      reloadState.setCountDown(countDown);
      return reloadState;
   }

   private void defaultReloadFinishing(ModernKineticGunScriptAPI api, boolean isTactical) {
      GunData data = api.getGunIndex().getGunData();
      int needAmmoCount = api.getNeededAmmoAmount();
      boolean needConsumeAmmo = api.isReloadingNeedConsumeAmmo();
      boolean infinite = data.getReloadData().isInfinite();
      needConsumeAmmo = needConsumeAmmo && !infinite;
      switch (data.getReloadData().getType()) {
         case MAGAZINE:
            if (needConsumeAmmo) {
               int consumedAmount = api.consumeAmmoFromPlayer(needAmmoCount);
               api.putAmmoInMagazine(consumedAmount);
            } else {
               api.putAmmoInMagazine(needAmmoCount);
            }
            break;
         case FUEL:
            if (needConsumeAmmo) {
               int consumedAmount = api.consumeAmmoFromPlayer(1);
               api.putAmmoInMagazine(needAmmoCount * consumedAmount);
            } else {
               api.putAmmoInMagazine(needAmmoCount);
            }
      }

      Bolt boltType = api.getGunIndex().getGunData().getBolt();
      if (!isTactical && (boltType == Bolt.MANUAL_ACTION || boltType == Bolt.CLOSED_BOLT)) {
         int i = api.removeAmmoFromMagazine(1);
         if (i != 0) {
            api.setAmmoInBarrel(true);
         }
      }
   }

   private void doMelee(LivingEntity user, float gunDistance, float meleeDistance, float rangeAngle, float knockback, float damage, List<EffectData> effects) {
      double distance = gunDistance + meleeDistance;
      float xRot = (float)Math.toRadians(-user.getXRot());
      float yRot = (float)Math.toRadians(-user.getYRot());
      Vec3 eyeVec = new Vec3(0.0, 0.0, 1.0).xRot(xRot).yRot(yRot).normalize().scale(distance);
      Vec3 centrePos = user.getEyePosition().subtract(eyeVec);
      List<LivingEntity> entityList = user.level().getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(distance));
      Supplier<Float> realDamage = Suppliers.memoize(() -> {
         AttributeInstance instance = user.getAttribute(Attributes.ATTACK_DAMAGE);
         if (instance == null) {
            return damage;
         }

         double oldBase = instance.getBaseValue();
         AttributeModifier modifier = AM_FACTORY.apply(damage);

         try {
            instance.setBaseValue(0.0);
            instance.addTransientModifier(modifier);
            return (float)instance.getValue();
         } finally {
            instance.setBaseValue(oldBase);
            instance.removeModifier(modifier);
         }
      });

      for (LivingEntity living : entityList) {
         Vec3 targetVec = living.getEyePosition().subtract(centrePos);
         double targetLength = targetVec.length();
         if (!(targetLength < distance)) {
            double degree = Math.toDegrees(Math.acos(targetVec.dot(eyeVec) / (targetLength * distance)));
            if (degree < rangeAngle / 2.0F && user.hasLineOfSight(living)) {
               doPerLivingHurt(user, living, knockback, realDamage.get(), effects);
            }
         }
      }

      if (user instanceof Player player) {
         player.causeFoodExhaustion(0.1F);
      }

      if (DebugCommand.DEBUG) {
         GunMeleeDebug.showRange(user, (int)Math.round(distance), centrePos, eyeVec, rangeAngle);
      }
   }

   private static void doPerLivingHurt(LivingEntity user, LivingEntity target, float knockback, float damage, List<EffectData> effects) {
      if (!target.equals(user)) {
         DamageSource source;
         if (user instanceof Player player) {
            source = user.damageSources().playerAttack(player);
         } else {
            source = user.damageSources().mobAttack(user);
         }

         target.knockback(knockback, Math.sin(Math.toRadians(user.getYRot())), -Math.cos(Math.toRadians(user.getYRot())), source, 0.0F);
         target.hurt(source, damage);
         if (target.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, target, source);
         }

         if (target.isAlive()) {
            for (EffectData data : effects) {
               Holder<MobEffect> mobEffect = (Holder<MobEffect>)BuiltInRegistries.MOB_EFFECT.get(data.getEffectId()).orElse(null);
               if (mobEffect != null) {
                  int time = Math.max(0, data.getTime() * 20);
                  int amplifier = Math.max(0, data.getAmplifier());
                  MobEffectInstance effectInstance = new MobEffectInstance(mobEffect, time, amplifier, false, data.isHideParticles());
                  target.addEffect(effectInstance);
               }
            }

            if (user.level() instanceof ServerLevel serverLevel) {
               int count = (int)(damage * 0.5);
               serverLevel.sendParticles(
                  ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), count, 0.1, 0.0, 0.1, 0.2
               );
            }
         }
      }
   }

   @Nullable
   private MeleeData getMeleeData(Identifier attachmentId) {
      return DefaultAssets.isEmptyAttachmentId(attachmentId)
         ? null
         : TimelessAPI.getCommonAttachmentIndex(attachmentId).map(index -> index.getData().getMeleeData()).orElse(null);
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

   @Override
   public void fireSelect(ShooterDataHolder dataHolder, ItemStack gunItem) {
      Identifier gunId = this.getGunId(gunItem);
      TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> {
         FireMode fireMode = this.getFireMode(gunItem);
         List<FireMode> fireModeSet = gunIndex.getGunData().getFireModeSet();
         int nextIndex = (fireModeSet.indexOf(fireMode) + 1) % fireModeSet.size();
         FireMode nextFireMode = fireModeSet.get(nextIndex);
         this.setFireMode(gunItem, nextFireMode);
         return nextFireMode;
      });
   }

   @Override
   public int getLevel(int exp) {
      return 0;
   }

   @Override
   public int getExp(int level) {
      return 0;
   }

   @Override
   public int getMaxLevel() {
      return 0;
   }

   public class DefaultPropertyModification {
      public static final Identifier SLUGS = Identifier.fromNamespaceAndPath("tacz", "intrinsic/slug");

      public <T> T modify(ItemStack gunItem, LivingEntity shooter, CommonGunIndex gunIndex, String id, T original) {
         return (T)("bullet_amount".equals(id)
               && AllowAttachmentTagMatcher.matchTag(SLUGS, ModernKineticGunItem.this.getAttachmentId(gunItem, AttachmentType.EXTENDED_MAG))
            ? 1
            : original);
      }
   }
}
