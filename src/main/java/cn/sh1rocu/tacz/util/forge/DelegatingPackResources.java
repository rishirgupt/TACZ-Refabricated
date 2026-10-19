package cn.sh1rocu.tacz.util.forge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.repository.Pack.Metadata;
import net.minecraft.server.packs.repository.Pack.ResourcesSupplier;

public class DelegatingPackResources extends AbstractPackResources implements ResourcesSupplier {
   private final PackMetadataSection packMeta;
   private final List<PackResources> delegates;
   private final Map<String, List<PackResources>> namespacesAssets;
   private final Map<String, List<PackResources>> namespacesData;

   public DelegatingPackResources(PackLocationInfo info, PackMetadataSection packMeta, List<? extends PackResources> packs) {
      super(info);
      this.packMeta = packMeta;
      this.delegates = ImmutableList.copyOf(packs);
      this.namespacesAssets = this.buildNamespaceMap(PackType.CLIENT_RESOURCES, this.delegates);
      this.namespacesData = this.buildNamespaceMap(PackType.SERVER_DATA, this.delegates);
   }

   private Map<String, List<PackResources>> buildNamespaceMap(PackType type, List<PackResources> packList) {
      Map<String, List<PackResources>> map = new HashMap<>();

      for (PackResources pack : packList) {
         for (String namespace : pack.getNamespaces(type)) {
            map.computeIfAbsent(namespace, k -> new ArrayList<>()).add(pack);
         }
      }

      map.replaceAll((k, list) -> ImmutableList.copyOf(list));
      return ImmutableMap.copyOf(map);
   }

   @Nullable
   public <T> T getMetadataSection(MetadataSectionType<T> deserializer) throws IOException {
      return (T)(deserializer.name().equals("pack") ? this.packMeta : null);
   }

   public void listResources(PackType type, String resourceNamespace, String paths, ResourceOutput resourceOutput) {
      for (PackResources delegate : this.delegates) {
         delegate.listResources(type, resourceNamespace, paths, resourceOutput);
      }
   }

   public Set<String> getNamespaces(PackType type) {
      return type == PackType.CLIENT_RESOURCES ? this.namespacesAssets.keySet() : this.namespacesData.keySet();
   }

   public void close() {
      for (PackResources pack : this.delegates) {
         pack.close();
      }
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... paths) {
      return null;
   }

   @Nullable
   public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
      for (PackResources pack : this.getCandidatePacks(type, location)) {
         IoSupplier<InputStream> ioSupplier = pack.getResource(type, location);
         if (ioSupplier != null) {
            return ioSupplier;
         }
      }

      return null;
   }

   @Nullable
   public Collection<PackResources> getChildren() {
      return this.delegates;
   }

   private List<PackResources> getCandidatePacks(PackType type, Identifier location) {
      Map<String, List<PackResources>> map = type == PackType.CLIENT_RESOURCES ? this.namespacesAssets : this.namespacesData;
      List<PackResources> packsWithNamespace = map.get(location.getNamespace());
      return packsWithNamespace == null ? Collections.emptyList() : packsWithNamespace;
   }

   public PackResources openPrimary(PackLocationInfo packLocationInfo) {
      return this;
   }

   public PackResources openFull(PackLocationInfo packLocationInfo, Metadata metadata) {
      return this;
   }
}
