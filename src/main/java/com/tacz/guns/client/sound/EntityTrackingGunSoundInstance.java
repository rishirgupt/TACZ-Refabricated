package com.tacz.guns.client.sound;

import java.lang.ref.WeakReference;
import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class EntityTrackingGunSoundInstance extends GunSoundInstance implements TickableSoundInstance {
   private final WeakReference<Entity> entityRef;
   private boolean stopped;

   public EntityTrackingGunSoundInstance(
      SoundEvent soundEvent,
      SoundSource source,
      float volume,
      float pitch,
      Entity entity,
      int soundDistance,
      @Nullable Identifier registryName,
      boolean mono
   ) {
      super(soundEvent, source, volume, pitch, entity, soundDistance, registryName, mono, false);
      this.entityRef = new WeakReference<>(entity);
   }

   @Override
   public boolean canPlaySound() {
      Entity entity = this.entityRef.get();
      return super.canPlaySound() && entity != null && !entity.isSilent();
   }

   public boolean isStopped() {
      return this.stopped;
   }

   public void tick() {
      Entity entity = this.entityRef.get();
      if (entity != null && !entity.isRemoved() && !(entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying())) {
         this.x = entity.getX();
         this.y = entity.getEyeY();
         this.z = entity.getZ();
      } else {
         this.setStop();
      }
   }

   @Override
   public void setStop() {
      this.stopped = true;
      super.setStop();
   }
}
