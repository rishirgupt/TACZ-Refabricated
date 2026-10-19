package com.tacz.guns.resource;

import cn.sh1rocu.tacz.util.forge.DelegatingPackResources;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.resource.ResourceManager;
import com.tacz.guns.config.PreLoadConfig;
import com.tacz.guns.util.GetJarResources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.FilePackResources.FileResourcesSupplier;
import net.minecraft.server.packs.PathPackResources.PathResourcesSupplier;
import net.minecraft.server.packs.repository.Pack.Position;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public enum GunPackLoader implements RepositorySource {
   INSTANCE;

   private static final Marker MARKER = MarkerFactory.getMarker("GunPackFinder");
   public PackType packType;
   private boolean firstLoad = true;

   public void loadPacks(Consumer<Pack> pOnLoad) {
      Pack extensionsPack = this.discoverExtensions();
      if (extensionsPack != null) {
         pOnLoad.accept(extensionsPack);
      }
   }

   public Pack discoverExtensions() {
      Path resourcePacksPath = FabricLoader.getInstance().getGameDir().resolve("tacz");
      File folder = resourcePacksPath.toFile();
      if (!folder.isDirectory()) {
         try {
            Files.createDirectories(folder.toPath());
         } catch (Exception e) {
            GunMod.LOGGER.warn(MARKER, "Failed to init tacz resource directory...", e);
            return null;
         }
      }

      if (this.firstLoad) {
         if (!(Boolean)PreLoadConfig.override.get()) {
            for (ResourceManager.ExtraEntry entry : ResourceManager.EXTRA_ENTRIES) {
               GetJarResources.copyModDirectory(entry.modMainClass(), entry.srcPath(), resourcePacksPath, entry.extraDirName());
            }
         }

         this.firstLoad = false;
      }

      GunMod.LOGGER.info(MARKER, "Start scanning for gun packs in {}", resourcePacksPath);
      List<GunPackLoader.GunPack> gunPacks = scanExtensions(resourcePacksPath);
      GunMod.LOGGER.info(MARKER, "Found {} possible gunpack(s) and added them to resource set.", gunPacks.size());
      List<PackResources> extensionPacks = new ArrayList<>();

      for (GunPackLoader.GunPack gunPack : gunPacks) {
         PackLocationInfo gunPackInfo = new PackLocationInfo(gunPack.name, Component.literal(gunPack.name), PackSource.BUILT_IN, Optional.empty());
         PackResources packResources;
         if (Files.isDirectory(gunPack.path)) {
            packResources = new PathResourcesSupplier(gunPack.path).openPrimary(gunPackInfo);
         } else {
            packResources = new FileResourcesSupplier(gunPack.path).openPrimary(gunPackInfo);
         }

         extensionPacks.add(packResources);
      }

      PackLocationInfo packLocationInfo = new PackLocationInfo("tacz_resources", Component.literal("TACZ Resources"), PackSource.BUILT_IN, Optional.empty());
      return Pack.readMetaAndCreate(
         packLocationInfo,
         new DelegatingPackResources(
            packLocationInfo,
            new PackMetadataSection(Component.translatable("tacz.resources.modresources"), new net.minecraft.util.InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(this.packType))),
            extensionPacks
         ) {
            @Override
            public IoSupplier<InputStream> getRootResource(String... paths) {
               if (paths.length == 1 && paths[0].equals("pack.png")) {
                  Path logoPath = GunPackLoader.getModIcon("tacz");
                  if (logoPath != null) {
                     return IoSupplier.create(logoPath);
                  }
               }

               return null;
            }
         },
         this.packType,
         new PackSelectionConfig(true, Position.BOTTOM, false)
      );
   }

   @Nullable
   public static Path getModIcon(String modId) {
      Optional<ModContainer> m = FabricLoader.getInstance().getModContainer(modId);
      if (m.isPresent()) {
         Optional<Path> logoPath = m.get().findPath("icon.png");
         if (logoPath.isPresent() && Files.exists(logoPath.get())) {
            return logoPath.get();
         }
      }

      return null;
   }

   private static GunPackLoader.GunPack fromDirPath(Path path) throws IOException {
      Path packInfoFilePath = path.resolve("gunpack.meta.json");

      try (InputStream stream = Files.newInputStream(packInfoFilePath)) {
         PackMeta info = (PackMeta)CommonAssetsManager.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);
         if (info == null) {
            GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath.getFileName());
            return null;
         } else if (info.getDependencies() != null && !modVersionAllMatch(info)) {
            GunMod.LOGGER.warn(MARKER, "Mod version mismatch: {}", packInfoFilePath.getFileName());
            return null;
         } else {
            return new GunPackLoader.GunPack(path, info.getName());
         }
      } catch (IOException | JsonSyntaxException | JsonIOException | VersionParsingException exception) {
         GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath.getFileName());
         GunMod.LOGGER.warn(exception.getMessage());
         return null;
      }
   }

   private static GunPackLoader.GunPack fromZipPath(Path path) {
      try (ZipFile zipFile = new ZipFile(path.toFile())) {
         ZipEntry extDescriptorEntry = zipFile.getEntry("gunpack.meta.json");
         if (extDescriptorEntry == null) {
            GunMod.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), "No gunpack.meta.json found");
            return null;
         }

         try (InputStream stream = zipFile.getInputStream(extDescriptorEntry)) {
            PackMeta info = (PackMeta)CommonAssetsManager.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);
            if (info == null) {
               GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", path.getFileName());
               return null;
            } else if (info.getDependencies() != null && !modVersionAllMatch(info)) {
               GunMod.LOGGER.warn(MARKER, "Mod version mismatch: {}", path.getFileName());
               return null;
            } else {
               return new GunPackLoader.GunPack(path, info.getName());
            }
         } catch (IOException | JsonSyntaxException | JsonIOException | VersionParsingException e) {
            GunMod.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), e);
            return null;
         }
      } catch (IOException e) {
         GunMod.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), e);
         return null;
      }
   }

   private static List<GunPackLoader.GunPack> scanExtensions(Path extensionsPath) {
      List<GunPackLoader.GunPack> gunPacks = new ArrayList<>();

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsPath)) {
         for (Path entry : stream) {
            GunPackLoader.GunPack gunPack = null;
            if (Files.isDirectory(entry)) {
               gunPack = fromDirPath(entry);
            } else if (entry.toString().endsWith(".zip")) {
               gunPack = fromZipPath(entry);
            }

            if (gunPack != null) {
               GunMod.LOGGER.info(MARKER, "- {}, Main namespace: {}", gunPack.path.getFileName(), gunPack.name);
               gunPacks.add(gunPack);
            }
         }
      } catch (IOException e) {
         GunMod.LOGGER.error(MARKER, "Failed to scan extensions from {}. Error: {}", extensionsPath, e);
      }

      return gunPacks;
   }

   private static boolean modVersionAllMatch(PackMeta info) throws VersionParsingException {
      HashMap<String, String> dependencies = info.getDependencies();

      for (String modId : dependencies.keySet()) {
         if (!modVersionMatch(modId, dependencies.get(modId))) {
            return false;
         }
      }

      return true;
   }

   private static boolean modVersionMatch(String modId, String version) throws VersionParsingException {
      VersionPredicate versionRange = VersionPredicate.parse(version);
      return FabricLoader.getInstance().getModContainer(modId).map(mod -> {
         Version modVersion = mod.getMetadata().getVersion();
         return versionRange.test(modVersion);
      }).orElse(false);
   }

   public record GunPack(Path path, String name) {
   }
}
