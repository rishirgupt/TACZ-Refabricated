package com.tacz.guns.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import com.tacz.guns.util.ResourceScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.resources.FileToIdConverter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LootInjectionManager extends SimplePreparableReloadListener<Map<Identifier, List<JsonElement>>> implements IdentifiableResourceReloadListener {
   private final Map<Identifier, List<LootTableInjection>> injections = Maps.newHashMap();
   private final Gson gson = CommonAssetsManager.GSON;
   private final Marker marker = MarkerFactory.getMarker("LootInjection");
   private final FileToIdConverter fileToIdConverter = FileToIdConverter.json("tacz_loot_injectors");
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "loot_injection_loader");

   @NotNull
   protected Map<Identifier, List<JsonElement>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
      return ResourceScanner.scanDirectoryAll(resourceManager, this.fileToIdConverter, this.gson);
   }

   protected void apply(Map<Identifier, List<JsonElement>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
      this.injections.clear();

      for (Entry<Identifier, List<JsonElement>> entry : object.entrySet()) {
         Identifier id = entry.getKey();

         for (JsonElement element : entry.getValue()) {
            try {
               LootTableInjection injection = LootTableInjection.fromJson(id, element);

               for (Identifier lootTable : injection.lootTables()) {
                  this.injections.computeIfAbsent(lootTable, key -> new ArrayList<>()).add(injection);
               }
            } catch (JsonParseException | IllegalArgumentException e) {
               GunMod.LOGGER.error(this.marker, "Failed to load loot injection {}", id, e);
            }
         }
      }
   }

   public List<LootTableInjection> getInjections(Identifier lootTable) {
      return this.injections.getOrDefault(lootTable, Collections.emptyList());
   }

   public Identifier getFabricId() {
      return ID;
   }
}
