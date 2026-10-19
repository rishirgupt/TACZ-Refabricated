package com.tacz.guns.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.FileToIdConverter;

public class ResourceScanner {
   public static Map<Identifier, JsonElement> scanDirectory(ResourceManager pResourceManager, String pName, Gson pGson) {
      return scanDirectory(pResourceManager, FileToIdConverter.json(pName), pGson);
   }

   public static Map<Identifier, JsonElement> scanDirectory(ResourceManager pResourceManager, FileToIdConverter filetoidconverter, Gson pGson) {
      Map<Identifier, JsonElement> output = Maps.newHashMap();

      for (Entry<Identifier, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
         Identifier resourcelocation = entry.getKey();
         Identifier resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

         try (Reader reader = entry.getValue().openAsReader()) {
            JsonElement jsonelement = (JsonElement)com.tacz.guns.util.LenientJson.fromJson(pGson, reader, JsonElement.class);
            JsonElement jsonelement1 = output.put(resourcelocation1, jsonelement);
            if (jsonelement1 != null) {
               throw new IllegalStateException("Duplicate data file ignored with ID " + resourcelocation1);
            }
         } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
            GunMod.LOGGER.error("Couldn't parse data file {} from {}", new Object[]{resourcelocation1, resourcelocation, jsonparseexception});
         }
      }

      return output;
   }

   public static Map<Identifier, Identifier> scanDirectoryResources(ResourceManager pResourceManager, FileToIdConverter filetoidconverter) {
      Map<Identifier, Identifier> output = Maps.newHashMap();

      for (Entry<Identifier, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
         Identifier rawLocation = entry.getKey();
         Identifier id = filetoidconverter.fileToId(rawLocation);
         Identifier old = output.put(id, rawLocation);
         if (old != null) {
            throw new IllegalStateException("Duplicate data file ignored with ID " + id);
         }
      }

      return output;
   }

   public static Map<Identifier, List<JsonElement>> scanDirectoryAll(ResourceManager pResourceManager, FileToIdConverter filetoidconverter, Gson pGson) {
      Map<Identifier, List<JsonElement>> output = Maps.newHashMap();

      for (Entry<Identifier, List<Resource>> entry : filetoidconverter.listMatchingResourceStacks(pResourceManager).entrySet()) {
         Identifier resourcelocation = entry.getKey();
         Identifier resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

         for (Resource resource : entry.getValue()) {
            try (Reader reader = resource.openAsReader()) {
               JsonElement jsonelement = (JsonElement)com.tacz.guns.util.LenientJson.fromJson(pGson, reader, JsonElement.class);
               List<JsonElement> list = output.computeIfAbsent(resourcelocation1, k -> Lists.newArrayList());
               list.add(jsonelement);
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
               GunMod.LOGGER.error("Couldn't parse data file {} from {}", new Object[]{resourcelocation1, resourcelocation, jsonparseexception});
            }
         }
      }

      return output;
   }
}
