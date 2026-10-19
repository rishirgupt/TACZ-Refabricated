package com.tacz.guns.entity.sync.core;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

public record SyncedDataKey<E extends Entity, T>(
   Pair<Identifier, Identifier> pairKey,
   Identifier id,
   SyncedClassKey<E> classKey,
   IDataSerializer<T> serializer,
   Supplier<T> defaultValueSupplier,
   boolean save,
   boolean persistent,
   SyncedDataKey.SyncMode syncMode
) {
   public static <E extends Entity, T> SyncedDataKey.Builder<E, T> builder(SyncedClassKey<E> entityClass, IDataSerializer<T> serializer) {
      return new SyncedDataKey.Builder<>(entityClass, serializer);
   }

   public void setValue(E entity, T value) {
      SyncedEntityData.instance().set(entity, this, value);
   }

   public T getValue(E entity) {
      return SyncedEntityData.instance().get(entity, this);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SyncedDataKey<?, ?> that = (SyncedDataKey<?, ?>)o;
         return Objects.equals(this.pairKey, that.pairKey);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.pairKey.hashCode();
   }

   public static class Builder<E extends Entity, T> {
      private final SyncedClassKey<E> classKey;
      private final IDataSerializer<T> serializer;
      private Identifier id;
      private Supplier<T> defaultValueSupplier;
      private boolean save = false;
      private boolean persistent = true;
      private SyncedDataKey.SyncMode syncMode = SyncedDataKey.SyncMode.ALL;

      private Builder(SyncedClassKey<E> classKey, IDataSerializer<T> serializer) {
         this.classKey = classKey;
         this.serializer = serializer;
      }

      public SyncedDataKey<E, T> build() {
         Validate.notNull(this.id, "Missing 'id' when building synced data key", new Object[0]);
         Validate.notNull(this.defaultValueSupplier, "Missing 'defaultValueSupplier' when building synced data key", new Object[0]);
         Pair<Identifier, Identifier> pairKey = Pair.of(this.classKey.id(), this.id);
         return new SyncedDataKey<>(pairKey, this.id, this.classKey, this.serializer, this.defaultValueSupplier, this.save, this.persistent, this.syncMode);
      }

      public SyncedDataKey.Builder<E, T> id(Identifier id) {
         this.id = id;
         return this;
      }

      public SyncedDataKey.Builder<E, T> id(String id) {
         this.id = Identifier.parse(id);
         return this;
      }

      @Deprecated
      public SyncedDataKey.Builder<E, T> key(String key) {
         return this.id(key);
      }

      public SyncedDataKey.Builder<E, T> defaultValueSupplier(Supplier<T> defaultValueSupplier) {
         this.defaultValueSupplier = defaultValueSupplier;
         return this;
      }

      public SyncedDataKey.Builder<E, T> saveToFile() {
         this.save = true;
         return this;
      }

      public SyncedDataKey.Builder<E, T> resetOnDeath() {
         this.persistent = false;
         return this;
      }

      public SyncedDataKey.Builder<E, T> syncMode(SyncedDataKey.SyncMode mode) {
         this.syncMode = mode;
         return this;
      }
   }

   public enum SyncMode {
      NONE(false, false),
      ALL(true, true),
      TRACKING_ONLY(true, false),
      SELF_ONLY(false, true);

      final boolean tracking;
      final boolean self;

      SyncMode(boolean tracking, boolean self) {
         this.tracking = tracking;
         this.self = self;
      }

      public boolean isTracking() {
         return this.tracking;
      }

      public boolean isSelf() {
         return this.self;
      }
   }
}
