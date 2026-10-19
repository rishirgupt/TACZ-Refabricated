package com.tacz.guns.client.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.resource.CommonAssetsManager;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class PackInfoManager extends SimplePreparableReloadListener<Map<String, PackInfo>> implements IdentifiableResourceReloadListener {
   private static final Marker MARKER = MarkerFactory.getMarker("PackInfoLoader");
   private static final String PACK_INFO_NAME = "gunpack_info.json";
   private final Map<String, PackInfo> dataMap = Maps.newHashMap();
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "packinfo_manager");

   protected Map<String, PackInfo> prepare(ResourceManager manager, ProfilerFiller pProfiler) {
      Map<String, PackInfo> output = Maps.newHashMap();

      for (String namespaces : manager.getNamespaces()) {
         manager.getResource(Identifier.fromNamespaceAndPath(namespaces, "gunpack_info.json")).ifPresent(rl -> {
            try (Reader reader = rl.openAsReader()) {
               PackInfo packInfo = (PackInfo)com.tacz.guns.util.LenientJson.fromJson(CommonAssetsManager.GSON, reader, PackInfo.class);
               PackInfo packInfo1 = output.put(namespaces, packInfo);
               if (packInfo1 != null) {
                  throw new IllegalStateException("Duplicate data file ignored with namespace " + namespaces);
               }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
               GunMod.LOGGER.error(MARKER, "Couldn't parse pack info for namespace '{}' from {}", new Object[]{namespaces, rl, jsonparseexception});
            }
         });
      }

      return output;
   }

   protected void apply(Map<String, PackInfo> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.dataMap.clear();
      this.dataMap.putAll(pObject);
   }

   public PackInfo getData(String namespace) {
      return this.dataMap.get(namespace);
   }

   public Identifier getFabricId() {
      return ID;
   }
}
