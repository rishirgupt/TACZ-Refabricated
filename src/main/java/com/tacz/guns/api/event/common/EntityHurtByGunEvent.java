package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class EntityHurtByGunEvent extends BaseEvent implements KubeJSGunEventPoster<EntityHurtByGunEvent> {
   protected final Entity bullet;
   @Nullable
   protected Entity hurtEntity;
   @Nullable
   protected LivingEntity attacker;
   protected Identifier gunId;
   protected Identifier gunDisplayId;
   protected float baseAmount;
   protected DamageSource nonApPartDamageSource;
   protected DamageSource apPartDamageSource;
   protected boolean isHeadShot;
   protected float headshotMultiplier;
   protected final LogicalSide logicalSide;
   public static final Event<EntityHurtByGunEvent.PreCallBack> PRE = EventFactory.createArrayBacked(
      EntityHurtByGunEvent.PreCallBack.class, callbacks -> event -> {
         for (EntityHurtByGunEvent.PreCallBack callback : callbacks) {
            callback.post(event);
         }
      }
   );
   public static final Event<EntityHurtByGunEvent.PostCallBack> POST = EventFactory.createArrayBacked(
      EntityHurtByGunEvent.PostCallBack.class, callbacks -> event -> {
         for (EntityHurtByGunEvent.PostCallBack callback : callbacks) {
            callback.post(event);
         }
      }
   );

   @Internal
   protected EntityHurtByGunEvent(
      Entity bullet,
      @Nullable Entity hurtEntity,
      @Nullable LivingEntity attacker,
      Identifier gunId,
      Identifier gunDisplayId,
      float baseAmount,
      @Nullable Pair<DamageSource, DamageSource> sources,
      boolean isHeadShot,
      float headshotMultiplier,
      LogicalSide logicalSide
   ) {
      this.bullet = bullet;
      this.hurtEntity = hurtEntity;
      this.attacker = attacker;
      this.gunId = gunId;
      this.baseAmount = baseAmount;
      this.nonApPartDamageSource = Optional.ofNullable(sources).<DamageSource>map(Pair::getLeft).orElse(null);
      this.apPartDamageSource = Optional.ofNullable(sources).<DamageSource>map(Pair::getRight).orElse(null);
      this.isHeadShot = isHeadShot;
      this.headshotMultiplier = headshotMultiplier;
      this.logicalSide = logicalSide;
   }

   public Entity getBullet() {
      return this.bullet;
   }

   @Nullable
   public Entity getHurtEntity() {
      return this.hurtEntity;
   }

   @Nullable
   public LivingEntity getAttacker() {
      return this.attacker;
   }

   public Identifier getGunId() {
      return this.gunId;
   }

   public Identifier getGunDisplayId() {
      return this.gunDisplayId;
   }

   @Obsolete
   public float getAmount() {
      return this.baseAmount * this.headshotMultiplier;
   }

   public float getBaseAmount() {
      return this.baseAmount;
   }

   public DamageSource getDamageSource(GunDamageSourcePart part) {
      if (this.logicalSide.isClient()) {
         throw new UnsupportedOperationException("DamageSource about gun hit is not available on client side!");
      } else {
         return part == GunDamageSourcePart.ARMOR_PIERCING ? this.apPartDamageSource : this.nonApPartDamageSource;
      }
   }

   public float getHeadshotMultiplier() {
      return this.headshotMultiplier;
   }

   public boolean isHeadShot() {
      return this.isHeadShot;
   }

   public LogicalSide getLogicalSide() {
      return this.logicalSide;
   }

   public static class Post extends EntityHurtByGunEvent {
      @Internal
      public Post(
         Entity bullet,
         @Nullable Entity hurtEntity,
         @Nullable LivingEntity attacker,
         Identifier gunId,
         Identifier gunDisplayId,
         float amount,
         @Nullable Pair<DamageSource, DamageSource> sources,
         boolean isHeadShot,
         float headshotMultiplier,
         LogicalSide logicalSide
      ) {
         super(bullet, hurtEntity, attacker, gunId, gunDisplayId, amount, sources, isHeadShot, headshotMultiplier, logicalSide);
         this.postEventToKubeJS(this);
      }
   }

   public interface PostCallBack {
      void post(EntityHurtByGunEvent.Post var1);
   }

   public static class Pre extends EntityHurtByGunEvent implements ICancellableEvent {
      @Internal
      public Pre(
         Entity bullet,
         @Nullable Entity hurtEntity,
         @Nullable LivingEntity attacker,
         Identifier gunId,
         Identifier gunDisplayId,
         float amount,
         @Nullable Pair<DamageSource, DamageSource> sources,
         boolean isHeadShot,
         float headshotMultiplier,
         LogicalSide logicalSide
      ) {
         super(bullet, hurtEntity, attacker, gunId, gunDisplayId, amount, sources, isHeadShot, headshotMultiplier, logicalSide);
         this.headshotMultiplier = headshotMultiplier;
         this.postEventToKubeJS(this);
      }

      public final void setHurtEntity(@Nullable Entity hurtEntity) {
         this.hurtEntity = hurtEntity;
      }

      public final void setAttacker(@Nullable LivingEntity attacker) {
         this.attacker = attacker;
      }

      public final void setGunId(Identifier gunId) {
         this.gunId = gunId;
      }

      public final void setBaseAmount(float baseAmount) {
         this.baseAmount = baseAmount;
      }

      public final void setDamageSource(GunDamageSourcePart part, DamageSource value) {
         if (this.logicalSide.isClient()) {
            throw new UnsupportedOperationException("DamageSource about gun hit is not available on client side!");
         }

         if (part == GunDamageSourcePart.ARMOR_PIERCING) {
            this.apPartDamageSource = value;
         } else {
            this.nonApPartDamageSource = value;
         }
      }

      public final void setHeadshot(boolean headshot) {
         this.isHeadShot = headshot;
      }

      public final void setHeadshotMultiplier(float headshotMultiplier) {
         this.headshotMultiplier = headshotMultiplier;
      }
   }

   public interface PreCallBack {
      void post(EntityHurtByGunEvent.Pre var1);
   }
}
