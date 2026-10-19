package com.tacz.guns.resource.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.util.ResourceScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.resources.FileToIdConverter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class AttachmentsTagManager extends SimplePreparableReloadListener<Map<Identifier, List<JsonElement>>> implements INetworkCacheReloadListener {
   private final Map<Identifier, Set<String>> tags = Maps.newHashMap();
   private final Map<Identifier, Set<String>> allow_attachments = Maps.newHashMap();
   private final Gson gson = CommonAssetsManager.GSON;
   private final Marker marker = MarkerFactory.getMarker("AllowTagManager");
   private final FileToIdConverter fileToIdConverter = FileToIdConverter.json("tacz_tags/attachments");
   protected Map<Identifier, String> networkCache;
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "attachments_tag_manager");

   @NotNull
   protected Map<Identifier, List<JsonElement>> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      return ResourceScanner.scanDirectoryAll(pResourceManager, this.fileToIdConverter, this.gson);
   }

   protected void apply(Map<Identifier, List<JsonElement>> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.tags.clear();
      this.allow_attachments.clear();
      Builder<Identifier, String> builder = ImmutableMap.builder();

      for (Entry<Identifier, List<JsonElement>> entry : pObject.entrySet()) {
         Identifier id = entry.getKey();
         List<String> temp = new ArrayList<>();

         for (JsonElement element : entry.getValue()) {
            try {
               List<String> data = this.parseJson(element);
               if (data != null) {
                  if (data.stream().anyMatch(Objects::isNull)) {
                     throw new JsonParseException("Null value found in JSON data");
                  }

                  temp.addAll(data);
               }
            } catch (JsonParseException e) {
               GunMod.LOGGER.error(this.marker, "Failed to parse data file {}", id, e);
            }
         }

         if (id.getPath().startsWith("allow_attachments/") && id.getPath().length() > 18) {
            Identifier gunId = id.withPath(id.getPath().substring(18));
            this.allow_attachments.computeIfAbsent(gunId, v -> Sets.newHashSet()).addAll(temp);
         } else {
            this.tags.computeIfAbsent(id, v -> Sets.newHashSet()).addAll(temp);
         }

         builder.put(entry.getKey(), this.gson.toJson(temp));
      }

      this.networkCache = builder.build();
   }

   private List<String> parseJson(JsonElement element) {
      return (List<String>)this.gson.fromJson(element, new TypeToken<List<String>>() {});
   }

   @Override
   public Map<Identifier, String> getNetworkCache() {
      return this.networkCache;
   }

   @Override
   public DataType getType() {
      return DataType.ATTACHMENT_TAGS;
   }

   public Set<String> getAttachmentTags(Identifier registryName) {
      return this.tags.get(registryName);
   }

   public Set<String> getAllowAttachmentTags(Identifier registryName) {
      return this.allow_attachments.get(registryName);
   }

   public Identifier getFabricId() {
      return null;
   }
}
