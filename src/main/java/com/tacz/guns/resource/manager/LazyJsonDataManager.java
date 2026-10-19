package com.tacz.guns.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.util.ResourceScanner;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.resources.FileToIdConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LazyJsonDataManager<T> extends SimplePreparableReloadListener<LazyJsonDataManager.PreparedResult<T>> implements IdentifiableResourceReloadListener {
   protected final Map<Identifier, T> dataMap = Maps.newHashMap();
   protected final Map<Identifier, Supplier<LazyJsonDataManager.LoadResult<T>>> lazyLoaderMap = Maps.newHashMap();
   protected final Set<Identifier> failedData = new HashSet<>();
   protected final Set<Identifier> allResourceIds = new HashSet<>();
   private final Gson gson;
   private final Class<T> dataClass;
   private final Marker marker;
   private final FileToIdConverter fileToIdConverter;
   private final Predicate<Identifier> eagerLoadPredicate;
   public final Identifier ID;

   public LazyJsonDataManager(Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker) {
      this(dataClass, pGson, fileToIdConverter, marker, id -> true);
   }

   public LazyJsonDataManager(Class<T> dataClass, Gson pGson, String directory, String marker, Predicate<Identifier> eagerLoadPredicate) {
      this(dataClass, pGson, FileToIdConverter.json(directory), marker, eagerLoadPredicate);
   }

   public LazyJsonDataManager(Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker, Predicate<Identifier> eagerLoadPredicate) {
      this.gson = pGson;
      this.dataClass = dataClass;
      this.marker = MarkerFactory.getMarker(marker);
      this.ID = Identifier.fromNamespaceAndPath("tacz", marker.toLowerCase(Locale.ROOT));
      this.fileToIdConverter = fileToIdConverter;
      this.eagerLoadPredicate = eagerLoadPredicate;
   }

   @NotNull
   protected LazyJsonDataManager.PreparedResult<T> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      Map<Identifier, Identifier> scannedResources = ResourceScanner.scanDirectoryResources(pResourceManager, this.fileToIdConverter);
      Map<Identifier, LazyJsonDataManager.PreparedEntry<T>> preparedEntries = Maps.newHashMapWithExpectedSize(scannedResources.size());

      for (Entry<Identifier, Identifier> entry : scannedResources.entrySet()) {
         Identifier id = entry.getKey();
         Identifier resourcePath = entry.getValue();
         if (this.shouldEagerLoad(id)) {
            JsonElement sourceElement = this.readResourceElement(pResourceManager, resourcePath);
            if (sourceElement == null) {
               preparedEntries.put(id, LazyJsonDataManager.PreparedEntry.failure());
            } else {
               preparedEntries.put(id, LazyJsonDataManager.PreparedEntry.eager(sourceElement));
            }
         } else {
            preparedEntries.put(id, LazyJsonDataManager.PreparedEntry.lazy(() -> this.loadResource(id, resourcePath, pResourceManager)));
         }
      }

      return new LazyJsonDataManager.PreparedResult<>(preparedEntries, Set.copyOf(scannedResources.keySet()));
   }

   protected void apply(LazyJsonDataManager.PreparedResult<T> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.dataMap.clear();
      this.lazyLoaderMap.clear();
      this.failedData.clear();
      this.allResourceIds.clear();
      this.allResourceIds.addAll(pObject.allResourceIds());

      for (Entry<Identifier, LazyJsonDataManager.PreparedEntry<T>> entry : pObject.entries().entrySet()) {
         Identifier id = entry.getKey();
         LazyJsonDataManager.PreparedEntry<T> preparedEntry = entry.getValue();
         if (preparedEntry.failed()) {
            this.failedData.add(id);
         } else if (preparedEntry.loader() != null) {
            this.lazyLoaderMap.put(id, preparedEntry.loader());
         } else {
            LazyJsonDataManager.LoadResult<T> loaded = this.loadResourceFromElement(id, preparedEntry.sourceElement());
            if (loaded.failed()) {
               this.failedData.add(id);
            } else if (loaded.data() != null) {
               this.dataMap.put(id, loaded.data());
            }
         }
      }
   }

   @Nullable
   protected T parseJson(JsonElement element) {
      return (T)this.gson.fromJson(element, this.getDataClass());
   }

   protected void postLoad(T data) {
   }

   protected final LazyJsonDataManager.LoadResult<T> loadResource(Identifier id, Identifier resourcePath, ResourceManager manager) {
      JsonElement sourceElement = this.readResourceElement(manager, resourcePath);
      return sourceElement == null ? LazyJsonDataManager.LoadResult.failure() : this.loadResourceFromElement(id, sourceElement);
   }

   protected final LazyJsonDataManager.LoadResult<T> loadResourceFromElement(Identifier id, JsonElement sourceElement) {
      try {
         T data = this.parseJson(sourceElement);
         if (data != null) {
            this.postLoad(data);
         }

         return LazyJsonDataManager.LoadResult.success(data);
      } catch (JsonParseException | IllegalArgumentException e) {
         GunMod.LOGGER.error(this.marker, "Failed to load data file {}", id, e);
         return LazyJsonDataManager.LoadResult.failure();
      }
   }

   @Nullable
   protected final Reader openReader(ResourceManager manager, Identifier resourcePath) throws IOException {
      Resource resource = (Resource)manager.getResource(resourcePath).orElse(null);
      return resource == null ? null : resource.openAsReader();
   }

   @Nullable
   protected final JsonElement readResourceElement(ResourceManager manager, Identifier resourcePath) {
      try (Reader reader = this.openReader(manager, resourcePath)) {
         return reader == null ? null : (JsonElement)com.tacz.guns.util.LenientJson.fromJson(this.gson, reader, JsonElement.class);
      } catch (IOException | JsonParseException | IllegalArgumentException exception) {
         GunMod.LOGGER.error(this.marker, "Failed to read raw data file {}", resourcePath, exception);
         return null;
      }
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
      T data = this.dataMap.get(id);
      if (data == null && !this.failedData.contains(id)) {
         Supplier<LazyJsonDataManager.LoadResult<T>> loader = this.lazyLoaderMap.get(id);
         if (loader == null) {
            return null;
         }

         synchronized (this) {
            data = this.dataMap.get(id);
            if (data == null && !this.failedData.contains(id)) {
               loader = this.lazyLoaderMap.remove(id);
               if (loader == null) {
                  return null;
               }

               LazyJsonDataManager.LoadResult<T> loaded = loader.get();
               if (loaded.failed()) {
                  this.failedData.add(id);
                  return null;
               }

               data = loaded.data();
               if (data != null) {
                  this.dataMap.put(id, data);
               }

               this.failedData.remove(id);
               return data;
            } else {
               return data;
            }
         }
      } else {
         return data;
      }
   }

   public Map<Identifier, T> getAllData() {
      return this.dataMap;
   }

   public Set<Identifier> getAllIds() {
      return this.allResourceIds;
   }

   protected final boolean shouldEagerLoad(Identifier id) {
      return this.eagerLoadPredicate.test(id);
   }

   protected final FileToIdConverter getFileToIdConverter() {
      return this.fileToIdConverter;
   }

   public Identifier getFabricId() {
      return this.ID;
   }

   protected record LoadResult<T>(@Nullable T data, boolean failed) {
      protected static <T> LazyJsonDataManager.LoadResult<T> success(@Nullable T data) {
         return new LazyJsonDataManager.LoadResult<>(data, false);
      }

      protected static <T> LazyJsonDataManager.LoadResult<T> failure() {
         return new LazyJsonDataManager.LoadResult<>(null, true);
      }
   }

   protected record PreparedEntry<T>(@Nullable JsonElement sourceElement, @Nullable Supplier<LazyJsonDataManager.LoadResult<T>> loader, boolean failed) {
      protected static <T> LazyJsonDataManager.PreparedEntry<T> eager(@Nullable JsonElement sourceElement) {
         return new LazyJsonDataManager.PreparedEntry<>(sourceElement, null, false);
      }

      protected static <T> LazyJsonDataManager.PreparedEntry<T> lazy(Supplier<LazyJsonDataManager.LoadResult<T>> loader) {
         return new LazyJsonDataManager.PreparedEntry<>(null, loader, false);
      }

      protected static <T> LazyJsonDataManager.PreparedEntry<T> failure() {
         return new LazyJsonDataManager.PreparedEntry<>(null, null, true);
      }
   }

   protected record PreparedResult<T>(Map<Identifier, LazyJsonDataManager.PreparedEntry<T>> entries, Set<Identifier> allResourceIds) {
   }
}
