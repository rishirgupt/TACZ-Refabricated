package com.tacz.guns.entity.sync.core;

import cn.sh1rocu.tacz.util.forge.LazyOptional;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup.Provider;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

public class DataHolderCapabilityProvider implements Component {
   public static final ComponentKey<DataHolderCapabilityProvider> CAPABILITY = ComponentRegistry.getOrCreate(
      Identifier.fromNamespaceAndPath("tacz", "synced_entity_data"), DataHolderCapabilityProvider.class
   );
   private final DataHolder holder = new DataHolder();
   private final LazyOptional<DataHolder> optional = LazyOptional.of(() -> this.holder);

   public void invalidate() {
      this.optional.invalidate();
   }

   public Optional<DataHolder> getDataHolder() {
      return this.optional.resolve();
   }

   private static volatile Provider serverProvider = net.minecraft.core.RegistryAccess.EMPTY;

   static {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(server -> serverProvider = server.registryAccess());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverProvider = net.minecraft.core.RegistryAccess.EMPTY);
   }

   private java.util.List<CompoundTag> serializeNBT(@NotNull Provider provider) {
      java.util.List<CompoundTag> list = new java.util.ArrayList<>();
      this.holder.dataMap.forEach((key, entry) -> {
         if (key.save()) {
            CompoundTag keyTag = new CompoundTag();
            keyTag.putString("ClassKey", key.classKey().id().toString());
            keyTag.putString("DataKey", key.id().toString());
            keyTag.put("Value", entry.writeValue(provider));
            list.add(keyTag);
         }
      });
      return list;
   }

   private void deserializeNBT(@NotNull Provider provider, java.util.List<CompoundTag> listTag) {
      this.holder.dataMap.clear();
      listTag.forEach(entryTag -> {
         CompoundTag keyTag = entryTag;
         Identifier classKey = Identifier.tryParse(keyTag.getStringOr("ClassKey", ""));
         Identifier dataKey = Identifier.tryParse(keyTag.getStringOr("DataKey", ""));
         Tag value = keyTag.get("Value");
         SyncedClassKey<?> syncedClassKey = SyncedEntityData.instance().getClassKey(classKey);
         if (syncedClassKey != null) {
            SyncedDataKey<?, ?> syncedDataKey = SyncedEntityData.instance().getKey(syncedClassKey, dataKey);
            if (syncedDataKey != null && syncedDataKey.save()) {
               DataEntry<?, ?> entry = new DataEntry<>(syncedDataKey);
               entry.readValue(provider, value);
               this.holder.dataMap.put(syncedDataKey, entry);
            }
         }
      });
   }

   @Override
   public void readData(net.minecraft.world.level.storage.ValueInput input) {
      this.deserializeNBT(input.lookup(), input.read("DataHolder", CompoundTag.CODEC.listOf()).orElse(java.util.List.of()));
   }

   @Override
   public void writeData(net.minecraft.world.level.storage.ValueOutput output) {
      output.store("DataHolder", CompoundTag.CODEC.listOf(), this.serializeNBT(serverProvider));
   }
}
