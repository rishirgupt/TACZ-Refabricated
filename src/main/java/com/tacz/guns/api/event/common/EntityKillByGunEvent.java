package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;

public class EntityKillByGunEvent extends BaseEvent implements KubeJSGunEventPoster<EntityKillByGunEvent> {
   private final Entity bullet;
   @Nullable
   private final LivingEntity killedEntity;
   @Nullable
   private final LivingEntity attacker;
   private final Identifier gunId;
   private final Identifier gunDisplayId;
   private final float baseDamage;
   private final DamageSource nonApPartDamageSource;
   private final DamageSource apPartDamageSource;
   private final boolean isHeadShot;
   private final float headshotMultiplier;
   private final LogicalSide logicalSide;
   public static final Event<EntityKillByGunEvent.Callback> CALLBACK = EventFactory.createArrayBacked(
      EntityKillByGunEvent.Callback.class, callbacks -> event -> {
         for (EntityKillByGunEvent.Callback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public EntityKillByGunEvent(
      Entity bullet,
      @Nullable LivingEntity hurtEntity,
      @Nullable LivingEntity attacker,
      Identifier gunId,
      Identifier gunDisplayId,
      float baseDamage,
      @Nullable Pair<DamageSource, DamageSource> sources,
      boolean isHeadShot,
      float headshotMultiplier,
      LogicalSide logicalSide
   ) {
      this.bullet = bullet;
      this.killedEntity = hurtEntity;
      this.attacker = attacker;
      this.gunId = gunId;
      this.gunDisplayId = gunDisplayId;
      this.baseDamage = baseDamage;
      this.nonApPartDamageSource = Optional.ofNullable(sources).<DamageSource>map(Pair::getLeft).orElse(null);
      this.apPartDamageSource = Optional.ofNullable(sources).<DamageSource>map(Pair::getRight).orElse(null);
      this.isHeadShot = isHeadShot;
      this.headshotMultiplier = headshotMultiplier;
      this.logicalSide = logicalSide;
      this.postEventToKubeJS(this);
   }

   public Entity getBullet() {
      return this.bullet;
   }

   @Nullable
   public LivingEntity getKilledEntity() {
      return this.killedEntity;
   }

   @Nullable
   public LivingEntity getAttacker() {
      return this.attacker;
   }

   public Identifier getGunId() {
      return this.gunId;
   }

   public float getBaseDamage() {
      return this.baseDamage;
   }

   public DamageSource getDamageSource(GunDamageSourcePart part) {
      if (this.logicalSide.isClient()) {
         throw new UnsupportedOperationException("DamageSource about gun hit is not available on client side!");
      } else {
         return part == GunDamageSourcePart.ARMOR_PIERCING ? this.apPartDamageSource : this.nonApPartDamageSource;
      }
   }

   public boolean isHeadShot() {
      return this.isHeadShot;
   }

   public float getHeadshotMultiplier() {
      return this.headshotMultiplier;
   }

   public LogicalSide getLogicalSide() {
      return this.logicalSide;
   }

   public Identifier getGunDisplayId() {
      return this.gunDisplayId;
   }

   public interface Callback {
      void post(EntityKillByGunEvent var1);
   }
}
