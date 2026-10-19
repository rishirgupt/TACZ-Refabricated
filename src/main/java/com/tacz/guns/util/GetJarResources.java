package com.tacz.guns.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tacz.guns.GunMod;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;

public final class GetJarResources {
   private static final Instant BACKUP_TIME = Instant.parse("2024-02-26T12:28:08.000Z");
   private static final Path BACKUP_PATH = Paths.get("config", "tacz", "backup");
   private static final SimpleDateFormat BACKUP_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
   private static final int MAX_BACKUP_COUNT = 10;
   private static final String EXPORT_STATE_FILE_NAME = ".export-state.json";
   private static final int EXPORT_STATE_VERSION = 1;
   private static final Gson EXPORT_STATE_GSON = new GsonBuilder().setPrettyPrinting().create();

   private GetJarResources() {
   }

   public static void copyModFile(String srcPath, Path root, String path) {
      URL url = GunMod.class.getResource(srcPath);

      try {
         if (url != null) {
            FileUtils.copyURLToFile(url, root.resolve(path).toFile());
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void copyModDirectory(Class<?> resourceClass, String srcPath, Path root, String path) {
      URL url = resourceClass.getResource(srcPath);

      try {
         if (url != null) {
            exportFolderIfChanged(resourceClass, srcPath, url, root, path);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void copyModDirectory(String srcPath, Path root, String path) {
      copyModDirectory(GunMod.class, srcPath, root, path);
   }

   @Nullable
   public static InputStream readModFile(String filePath) {
      URL url = GunMod.class.getResource(filePath);

      try {
         if (url != null) {
            return url.openStream();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

      return null;
   }

   private static void exportFolderIfChanged(Class<?> resourceClass, String srcPath, URL sourceUrl, Path root, String path) throws IOException {
      String stateKey = getExportStateKey(resourceClass, srcPath, path);
      String sourceFingerprint = calculateSourceFingerprint(sourceUrl);
      GetJarResources.ExportStateFile stateFile = readExportState(root);
      Path targetPath = root.resolve(path);
      String previousFingerprint = stateFile.entries.get(stateKey);
      if (Files.isDirectory(targetPath) && sourceFingerprint.equals(previousFingerprint)) {
         GunMod.LOGGER.debug("Skipping unchanged exported resource {}", targetPath);
      } else {
         GunMod.LOGGER.info("Exporting resource pack {} to {}", srcPath, targetPath);
         copyFolder(sourceUrl, targetPath);
         stateFile.version = 1;
         stateFile.entries.put(stateKey, sourceFingerprint);
         writeExportState(root, stateFile);
      }
   }

   private static String getExportStateKey(Class<?> resourceClass, String srcPath, String path) {
      return resourceClass.getName() + "|" + srcPath + "|" + path;
   }

   private static GetJarResources.ExportStateFile readExportState(Path root) {
      Path statePath = root.resolve(".export-state.json");
      if (!Files.isRegularFile(statePath)) {
         return new GetJarResources.ExportStateFile();
      }

      try (Reader reader = Files.newBufferedReader(statePath, StandardCharsets.UTF_8)) {
         GetJarResources.ExportStateFile state = (GetJarResources.ExportStateFile)EXPORT_STATE_GSON.fromJson(reader, GetJarResources.ExportStateFile.class);
         return state != null && state.version == 1 && state.entries != null ? state : new GetJarResources.ExportStateFile();
      } catch (Exception exception) {
         GunMod.LOGGER.warn("Failed to read export state from {}, forcing full export", statePath, exception);
         return new GetJarResources.ExportStateFile();
      }
   }

   private static void writeExportState(Path root, GetJarResources.ExportStateFile stateFile) throws IOException {
      Files.createDirectories(root);
      Path statePath = root.resolve(".export-state.json");

      try (Writer writer = Files.newBufferedWriter(
            statePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
         )) {
         EXPORT_STATE_GSON.toJson(stateFile, writer);
      }
   }

   private static String calculateSourceFingerprint(URL sourceUrl) throws IOException {
      List<String> lines = "jar".equals(sourceUrl.getProtocol())
         ? collectJarFingerprintLines(sourceUrl)
         : collectPathFingerprintLines(resolveSourcePath(sourceUrl));
      Collections.sort(lines);
      return Md5Utils.md5Hex(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
   }

   private static List<String> collectPathFingerprintLines(Path sourceRoot) throws IOException {
      List<String> lines = new ArrayList<>();

      try (Stream<Path> stream = Files.walk(sourceRoot, Integer.MAX_VALUE)) {
         stream.filter(x$0 -> Files.isRegularFile(x$0)).forEach(path -> {
            try {
               BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
               String relativePath = sourceRoot.relativize(path).toString().replace('\\', '/');
               lines.add(relativePath + "|" + attributes.size() + "|" + attributes.lastModifiedTime().toMillis());
            } catch (IOException exception) {
               throw new UncheckedIOException(exception);
            }
         });
         return lines;
      } catch (UncheckedIOException exception) {
         throw exception.getCause();
      }
   }

   private static List<String> collectJarFingerprintLines(URL sourceUrl) throws IOException {
      JarURLConnection connection = (JarURLConnection)sourceUrl.openConnection();
      connection.setUseCaches(false);
      String rootEntry = normalizeDirectoryEntryName(connection.getEntryName());
      List<String> lines = new ArrayList<>();

      try (JarFile jarFile = connection.getJarFile()) {
         Enumeration<JarEntry> entries = jarFile.entries();

         while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().startsWith(rootEntry)) {
               String relativePath = entry.getName().substring(rootEntry.length());
               if (!relativePath.isEmpty()) {
                  lines.add(relativePath + "|" + entry.getSize() + "|" + entry.getTime() + "|" + entry.getCrc());
               }
            }
         }
      }

      return lines;
   }

   private static void copyFolder(URL sourceUrl, Path targetPath) throws IOException {
      if (Files.isDirectory(targetPath)) {
         backupFiles(targetPath);
         deleteFiles(targetPath);
      } else if (Files.exists(targetPath)) {
         Files.delete(targetPath);
      }

      if ("jar".equals(sourceUrl.getProtocol())) {
         copyJarProtocolFolder(sourceUrl, targetPath);
      } else {
         copyPathBackedFolder(resolveSourcePath(sourceUrl), targetPath);
      }
   }

   private static void copyPathBackedFolder(Path sourceRoot, Path targetPath) throws IOException {
      Files.createDirectories(targetPath);

      try (Stream<Path> stream = Files.walk(sourceRoot, Integer.MAX_VALUE)) {
         stream.forEach(source -> {
            Path target = targetPath.resolve(sourceRoot.relativize(source).toString());

            try {
               if (Files.isDirectory(source)) {
                  Files.createDirectories(target);
               } else {
                  Files.createDirectories(target.getParent());
                  Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
               }
            } catch (IOException exception) {
               throw new UncheckedIOException(exception);
            }
         });
      } catch (UncheckedIOException exception) {
         throw exception.getCause();
      }
   }

   private static void copyJarProtocolFolder(URL sourceUrl, Path targetPath) throws IOException {
      JarURLConnection connection = (JarURLConnection)sourceUrl.openConnection();
      connection.setUseCaches(false);
      String rootEntry = normalizeDirectoryEntryName(connection.getEntryName());
      Files.createDirectories(targetPath);

      try (JarFile jarFile = connection.getJarFile()) {
         Enumeration<JarEntry> entries = jarFile.entries();

         while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(rootEntry)) {
               String relativePath = entry.getName().substring(rootEntry.length());
               if (!relativePath.isEmpty()) {
                  Path target = targetPath.resolve(relativePath);
                  if (entry.isDirectory()) {
                     Files.createDirectories(target);
                  } else {
                     Files.createDirectories(target.getParent());

                     try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                     }
                  }
               }
            }
         }
      }
   }

   private static String normalizeDirectoryEntryName(String entryName) {
      if (entryName != null && !entryName.isEmpty()) {
         return entryName.endsWith("/") ? entryName : entryName + "/";
      } else {
         return "";
      }
   }

   private static Path resolveSourcePath(URL sourceUrl) throws IOException {
      try {
         return Paths.get(sourceUrl.toURI());
      } catch (Exception exception) {
         throw new IOException("Failed to resolve source path " + sourceUrl, exception);
      }
   }

   private static void backupFiles(Path targetPath) throws IOException {
      String dirName = targetPath.getFileName().toString();
      Path resourcePacksPath = FabricLoader.getInstance().getGameDir().resolve("tacz_backup");
      Path backupPath = resourcePacksPath.resolve(dirName);
      if (!Files.isDirectory(backupPath)) {
         Files.createDirectories(backupPath);
      }

      Set<String> cacheMd5 = checkOldBackups(backupPath);
      File tempFile = File.createTempFile(dirName, ".tmp");
      FileTime fileTime = FileTime.from(BACKUP_TIME);

      try (
         ZipOutputStream zs = new ZipOutputStream(new FileOutputStream(tempFile));
         Stream<Path> fileWalks = Files.walk(targetPath);
      ) {
         fileWalks.filter(x$0 -> Files.isRegularFile(x$0)).forEach(path -> {
            String entryPath = targetPath.relativize(path).toString();
            ZipEntry zipEntry = new ZipEntry(entryPath);
            zipEntry.setLastModifiedTime(fileTime);

            try {
               zs.putNextEntry(zipEntry);
               Files.copy(path, zs);
               zs.closeEntry();
            } catch (IOException e) {
               GunMod.LOGGER.info("Error in zip file: {}", e.getMessage());
            }
         });
      }

      try (FileInputStream var17 = new FileInputStream(tempFile)) {
         String md5Hex = Md5Utils.md5Hex(var17);
         if (cacheMd5.contains(md5Hex)) {
            tempFile.deleteOnExit();
         } else {
            String dataName = BACKUP_DATE_FORMAT.format(new Date()).toLowerCase(Locale.ENGLISH);
            Path backupZipFilePath = backupPath.resolve(String.format("backup-%s-%s.zip", dataName, md5Hex));
            FileUtils.copyFile(tempFile, backupZipFilePath.toFile());
         }
      }
   }

   private static Set<String> checkOldBackups(Path backupPath) {
      Set<String> allMd5Hex = Sets.newHashSet();
      if (!Files.isDirectory(backupPath)) {
         return allMd5Hex;
      }

      try {
         List<File> delFiles = Lists.newArrayList(FileUtils.listFiles(backupPath.toFile(), TrueFileFilter.TRUE, null));
         delFiles.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
         int count = 1;

         for (File file : delFiles) {
            if (count >= 10) {
               GunMod.LOGGER.info("Deleting old backup gun pack {}", file.getName());
               FileUtils.deleteQuietly(file);
            } else {
               try (FileInputStream inputStream = new FileInputStream(file)) {
                  allMd5Hex.add(Md5Utils.md5Hex(inputStream));
               }
            }

            count++;
         }
      } catch (Exception exception) {
         GunMod.LOGGER.error("Error while checking old backup gun pack : {}", exception.getMessage());
      }

      return allMd5Hex;
   }

   private static void deleteFiles(Path targetPath) throws IOException {
      Files.walkFileTree(targetPath, new SimpleFileVisitor<Path>() {
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
         }
      });
   }

   private static final class ExportStateFile {
      private int version = 1;
      private Map<String, String> entries = new HashMap<>();
   }
}
