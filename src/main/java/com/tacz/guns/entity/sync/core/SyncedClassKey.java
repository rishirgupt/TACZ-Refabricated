package com.tacz.guns.entity.sync.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;

public record SyncedClassKey<E extends Entity>(Class<E> entityClass, Identifier id) {
   public static final SyncedClassKey<LivingEntity> LIVING_ENTITY = new SyncedClassKey<>(LivingEntity.class, Identifier.parse("living_entity"));

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SyncedClassKey<?> that = (SyncedClassKey<?>)o;
         return this.entityClass.getName().equals(that.entityClass.getName());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.entityClass.getName().hashCode();
   }
}
