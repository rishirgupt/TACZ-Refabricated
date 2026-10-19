package com.tacz.guns.client.resource.manager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.resource.pojo.display.IDisplay;
import com.tacz.guns.resource.manager.JsonDataManager;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.resources.FileToIdConverter;

public class DisplayManager<T extends IDisplay> extends JsonDataManager<T> {
   public DisplayManager(Class<T> dataClass, Gson pGson, String directory, String marker) {
      super(dataClass, pGson, FileToIdConverter.json(directory), marker);
   }

   public DisplayManager(Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker) {
      super(dataClass, pGson, fileToIdConverter, marker);
   }

   @Override
   protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.dataMap.clear();

      for (Entry<Identifier, JsonElement> entry : pObject.entrySet()) {
         Identifier id = entry.getKey();
         JsonElement element = entry.getValue();

         try {
            T data = (T)this.getGson().fromJson(element, this.getDataClass());
            if (data != null) {
               data.init();
               this.dataMap.put(id, data);
            }
         } catch (JsonParseException | IllegalArgumentException e) {
            GunMod.LOGGER.error(this.getMarker(), "Failed to load data file {}", id, e);
         }
      }
   }
}
