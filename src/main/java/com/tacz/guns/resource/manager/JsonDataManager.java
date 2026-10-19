package com.tacz.guns.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.util.ResourceScanner;
import java.util.Locale;
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

public class JsonDataManager<T> extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> implements IdentifiableResourceReloadListener {
   protected final Map<Identifier, T> dataMap = Maps.newHashMap();
   private final Gson gson;
   private final Class<T> dataClass;
   private final Marker marker;
   private final FileToIdConverter fileToIdConverter;
   public final Identifier ID;

   public JsonDataManager(Class<T> dataClass, Gson pGson, String directory, String marker) {
      this(dataClass, pGson, FileToIdConverter.json(directory), marker);
   }

   public JsonDataManager(Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker) {
      this.gson = pGson;
      this.dataClass = dataClass;
      this.marker = MarkerFactory.getMarker(marker);
      this.ID = Identifier.fromNamespaceAndPath("tacz", marker.toLowerCase(Locale.ROOT));
      this.fileToIdConverter = fileToIdConverter;
   }

   @NotNull
   protected Map<Identifier, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      return ResourceScanner.scanDirectory(pResourceManager, this.fileToIdConverter, this.gson);
   }

   protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.dataMap.clear();

      for (Entry<Identifier, JsonElement> entry : pObject.entrySet()) {
         Identifier id = entry.getKey();
         JsonElement element = entry.getValue();

         try {
            T data = this.parseJson(element);
            if (data != null) {
               this.dataMap.put(id, data);
            }
         } catch (JsonParseException | IllegalArgumentException e) {
            GunMod.LOGGER.error(this.marker, "Failed to load data file {}", id, e);
         }
      }
   }

   protected T parseJson(JsonElement element) {
      return (T)this.gson.fromJson(element, this.getDataClass());
   }

   public Class<T> getDataClass() {
      return this.dataClass;
   }

   public Marker getMarker() {
      return this.marker;
   }

   public Gson getGson() {
      return this.gson;
   }

   public T getData(Identifier id) {
      return this.dataMap.get(id);
   }

   public Map<Identifier, T> getAllData() {
      return this.dataMap;
   }

   public Identifier getFabricId() {
      return this.ID;
   }
}
