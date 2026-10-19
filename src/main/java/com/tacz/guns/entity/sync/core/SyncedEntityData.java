package com.tacz.guns.entity.sync.core;

import com.google.common.collect.ImmutableSet;
import com.tacz.guns.GunMod;
import com.tacz.guns.init.CommonRegistry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SyncedEntityData {
   private static final Marker SYNCED_ENTITY_DATA_MARKER = MarkerFactory.getMarker("SYNCED_ENTITY_DATA_TAC_COPY");
   private static SyncedEntityData INSTANCE;
   private final Set<SyncedClassKey<?>> registeredClassKeys = new HashSet<>();
   private final Object2ObjectMap<Identifier, SyncedClassKey<?>> idToClassKey = new Object2ObjectOpenHashMap();
   private final Object2ObjectMap<String, SyncedClassKey<?>> classNameToClassKey = new Object2ObjectOpenHashMap();
   private final Map<String, Boolean> clientClassNameCapabilityCache = new ConcurrentHashMap<>();
   private final Map<String, Boolean> serverClassNameCapabilityCache = new ConcurrentHashMap<>();
   private final Set<SyncedDataKey<?, ?>> registeredDataKeys = new HashSet<>();
   private final Reference2ObjectMap<SyncedClassKey<?>, HashMap<Identifier, SyncedDataKey<?, ?>>> classToKeys = new Reference2ObjectOpenHashMap();
   private final Reference2IntMap<SyncedDataKey<?, ?>> internalIds = new Reference2IntOpenHashMap();
   private final Int2ReferenceMap<SyncedDataKey<?, ?>> syncedIdToKey = new Int2ReferenceOpenHashMap();
   private final AtomicInteger nextIdTracker = new AtomicInteger();
   private final List<Entity> dirtyEntities = new ArrayList<>();
   private boolean dirty = false;

   private SyncedEntityData() {
   }

   public static SyncedEntityData instance() {
      if (INSTANCE == null) {
         INSTANCE = new SyncedEntityData();
      }

      return INSTANCE;
   }

   private <E extends Entity> void registerClassKey(SyncedClassKey<E> classKey) {
      if (!this.registeredClassKeys.contains(classKey)) {
         this.registeredClassKeys.add(classKey);
         this.idToClassKey.put(classKey.id(), classKey);
         this.classNameToClassKey.put(classKey.entityClass().getName(), classKey);
      }
   }

   public synchronized <E extends Entity, T> void registerDataKey(SyncedDataKey<E, T> dataKey) {
      Identifier keyId = dataKey.id();
      SyncedClassKey<E> classKey = dataKey.classKey();
      if (CommonRegistry.isLoadComplete()) {
         throw new IllegalStateException(String.format("Tried to register synced data key %s for %s after game initialization", keyId, classKey.id()));
      }

      if (this.registeredDataKeys.contains(dataKey)) {
         throw new IllegalArgumentException(String.format("The synced data key %s for %s is already registered", keyId, classKey.id()));
      }

      this.registerClassKey(dataKey.classKey());
      this.registeredDataKeys.add(dataKey);
      ((HashMap)this.classToKeys.computeIfAbsent(classKey, c -> new HashMap())).put(keyId, dataKey);
      int nextId = this.nextIdTracker.getAndIncrement();
      this.internalIds.put(dataKey, nextId);
      this.syncedIdToKey.put(nextId, dataKey);
      GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered synced data key {} for {}", dataKey.id(), classKey.id());
   }

   public <E extends Entity, T> void set(E entity, SyncedDataKey<?, ?> key, T value) {
      if (!this.registeredDataKeys.contains(key)) {
         String keys = this.registeredDataKeys.stream().map(k -> k.pairKey().toString()).collect(Collectors.joining(",", "[", "]"));
         GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered keys before throwing exception: {}", keys);
         throw new IllegalArgumentException(String.format("The synced data key %s for %s is not registered!", key.id(), key.classKey().id()));
      }

      DataHolder holder = this.getDataHolder(entity);
      if (holder != null && holder.set(entity, key, value) && !entity.level().isClientSide()) {
         this.dirty = true;
         this.dirtyEntities.add(entity);
      }
   }

   public <E extends Entity, T> T get(E entity, SyncedDataKey<E, T> key) {
      if (!this.registeredDataKeys.contains(key)) {
         String keys = this.registeredDataKeys.stream().map(k -> k.pairKey().toString()).collect(Collectors.joining(",", "[", "]"));
         GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered keys before throwing exception: {}", keys);
         throw new IllegalArgumentException(String.format("The synced data key %s for %s is not registered!", key.id(), key.classKey().id()));
      } else {
         DataHolder holder = this.getDataHolder(entity);
         return holder != null ? holder.get(key) : key.defaultValueSupplier().get();
      }
   }

   public int getInternalId(SyncedDataKey<?, ?> key) {
      return this.internalIds.getInt(key);
   }

   @Nullable
   public SyncedClassKey<?> getClassKey(Identifier id) {
      return (SyncedClassKey<?>)this.idToClassKey.get(id);
   }

   @Nullable
   public SyncedDataKey<?, ?> getKey(int id) {
      return (SyncedDataKey<?, ?>)this.syncedIdToKey.get(id);
   }

   @Nullable
   public SyncedDataKey<?, ?> getKey(SyncedClassKey<?> classKey, Identifier dataKey) {
      Map<Identifier, SyncedDataKey<?, ?>> keys = (Map<Identifier, SyncedDataKey<?, ?>>)instance().classToKeys.get(classKey);
      return keys == null ? null : keys.get(dataKey);
   }

   public Set<SyncedDataKey<?, ?>> getKeys() {
      return ImmutableSet.copyOf(this.registeredDataKeys);
   }

   @Nullable
   public DataHolder getDataHolder(Entity entity) {
      return DataHolderCapabilityProvider.CAPABILITY.maybeGet(entity).flatMap(DataHolderCapabilityProvider::getDataHolder).orElse(null);
   }

   public boolean hasSyncedDataKey(Class<? extends Entity> entityClass) {
      return this.getClassNameCapabilityCache(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT).computeIfAbsent(entityClass.getName(), c -> {
         for (Class<?> targetClass = entityClass; !targetClass.isAssignableFrom(Entity.class); targetClass = targetClass.getSuperclass()) {
            if (this.classNameToClassKey.containsKey(targetClass.getName())) {
               return true;
            }
         }

         return false;
      });
   }

   private Map<String, Boolean> getClassNameCapabilityCache(boolean client) {
      return client ? this.clientClassNameCapabilityCache : this.serverClassNameCapabilityCache;
   }

   public boolean updateMappings(Map<Identifier, List<Pair<Identifier, Integer>>> keyMap) {
      this.syncedIdToKey.clear();
      List<Pair<Identifier, Identifier>> missingKeys = new ArrayList<>();
      keyMap.forEach((classId, list) -> {
         SyncedClassKey<?> classKey = (SyncedClassKey<?>)this.idToClassKey.get(classId);
         if (classKey != null && this.classToKeys.containsKey(classKey)) {
            Map<Identifier, SyncedDataKey<?, ?>> keysx = (Map<Identifier, SyncedDataKey<?, ?>>)this.classToKeys.get(classKey);
            list.forEach(pair -> {
               SyncedDataKey<?, ?> syncedDataKey = keysx.get(pair.getLeft());
               if (syncedDataKey == null) {
                  missingKeys.add(Pair.of(classId, (Identifier)pair.getLeft()));
               } else {
                  this.syncedIdToKey.put((Integer)pair.getRight(), syncedDataKey);
               }
            });
         } else {
            list.forEach(pair -> missingKeys.add(Pair.of(classId, (Identifier)pair.getLeft())));
         }
      });
      if (!missingKeys.isEmpty()) {
         String keys = missingKeys.stream().map(Object::toString).collect(Collectors.joining(",", "[", "]"));
         GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Received unknown synced keys: {}", keys);
      }

      return missingKeys.isEmpty();
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void setDirty(boolean dirty) {
      this.dirty = dirty;
   }

   public List<Entity> getDirtyEntities() {
      return this.dirtyEntities;
   }
}
