package com.tacz.guns.resource.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.tacz.guns.resource.network.DataType;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.resources.FileToIdConverter;

public class CommonDataManager<T> extends JsonDataManager<T> implements INetworkCacheReloadListener {
   private final DataType type;
   protected Map<Identifier, String> networkCache;
   public final Identifier ID;

   public CommonDataManager(DataType type, Class<T> dataClass, Gson pGson, String directory, String marker) {
      super(dataClass, pGson, directory, marker);
      this.type = type;
      this.ID = Identifier.fromNamespaceAndPath("tacz", marker.toLowerCase(Locale.ROOT));
   }

   public CommonDataManager(DataType type, Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker) {
      super(dataClass, pGson, fileToIdConverter, marker);
      this.type = type;
      this.ID = Identifier.fromNamespaceAndPath("tacz", marker.toLowerCase(Locale.ROOT));
   }

   @Override
   protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      super.apply(pObject, pResourceManager, pProfiler);
      Builder<Identifier, String> builder = ImmutableMap.builder();
      pObject.forEach((id, element) -> builder.put(id, element.toString()));
      this.networkCache = builder.build();
   }

   public void clear() {
      this.dataMap.clear();
   }

   @Override
   public Map<Identifier, String> getNetworkCache() {
      return this.networkCache;
   }

   @Override
   public DataType getType() {
      return this.type;
   }

   @Override
   public Identifier getFabricId() {
      return this.ID;
   }
}
