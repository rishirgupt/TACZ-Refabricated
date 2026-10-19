package com.tacz.guns.resource.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.util.ResourceScanner;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.resources.FileToIdConverter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class RecipeFilterManager extends SimplePreparableReloadListener<Map<Identifier, List<JsonElement>>> implements INetworkCacheReloadListener {
   private final Map<Identifier, RecipeFilter> filters = Maps.newHashMap();
   private final Gson gson = CommonAssetsManager.GSON;
   private final Marker marker = MarkerFactory.getMarker("RecipeFilter");
   private final FileToIdConverter fileToIdConverter = FileToIdConverter.json("recipe_filters");
   protected Map<Identifier, String> networkCache;
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "recipe_filter_manager");

   @NotNull
   protected Map<Identifier, List<JsonElement>> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      return ResourceScanner.scanDirectoryAll(pResourceManager, this.fileToIdConverter, this.gson);
   }

   protected void apply(Map<Identifier, List<JsonElement>> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.filters.clear();
      Builder<Identifier, String> builder = ImmutableMap.builder();

      for (Entry<Identifier, List<JsonElement>> entry : pObject.entrySet()) {
         Identifier id = entry.getKey();

         for (JsonElement element : entry.getValue()) {
            try {
               RecipeFilter data = this.parseJson(element);
               this.filters.compute(id, (key, value) -> {
                  if (value == null) {
                     return data;
                  }

                  value.merge(data);
                  return (RecipeFilter)value;
               });
            } catch (JsonParseException | IllegalArgumentException e) {
               GunMod.LOGGER.error(this.marker, "Failed to load data file {}", id, e);
            }
         }

         if (this.filters.containsKey(id)) {
            builder.put(id, this.gson.toJson(this.filters.get(id)));
         }
      }

      this.networkCache = builder.build();
   }

   private RecipeFilter parseJson(JsonElement element) {
      return (RecipeFilter)this.gson.fromJson(element, RecipeFilter.class);
   }

   @Override
   public Map<Identifier, String> getNetworkCache() {
      return this.networkCache;
   }

   @Override
   public DataType getType() {
      return DataType.RECIPE_FILTER;
   }

   public Map<Identifier, RecipeFilter> getFilters() {
      return this.filters;
   }

   public RecipeFilter getFilter(Identifier id) {
      return this.filters.get(id);
   }

   public Identifier getFabricId() {
      return ID;
   }
}
