package com.tacz.guns.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.resource.pojo.PackInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.StopWatch;

public class PackConvertor {
   public static final Path FOLDER = Paths.get("config", "tacz", "custom");
   public static final Pattern PACK_INFO_PATTERN = Pattern.compile("^(\\w+)/pack\\.json$");
   public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   private static Component pre(Component component) {
      return Component.translatable("message.tacz.pre").append(component);
   }

   private static void msg(CommandSourceStack source, Component component) {
      source.sendSystemMessage(pre(component));
   }

   public static void convert(CommandSourceStack source) {
      Path resourcePacksPath = FabricLoader.getInstance().getGameDir().resolve("tacz");
      File folder = resourcePacksPath.toFile();
      if (!folder.isDirectory()) {
         try {
            Files.createDirectories(folder.toPath());
         } catch (Exception e) {
            msg(source, Component.translatable("message.tacz.converter.init_failed"));
            GunMod.LOGGER.error("Failed to init tacz directory...", e);
            return;
         }
      }

      File[] files = FOLDER.toFile().listFiles();
      int cnt = 0;
      int skip = 0;
      int error = 0;
      if (files != null && files.length > 0) {
         StopWatch watch = StopWatch.createStarted();
         msg(source, Component.translatable("message.tacz.converter.start"));
         GunMod.LOGGER.info("Start converting legacy packs...");

         for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
               PackConvertor.LegacyPack pack = fromZipFile(file);
               if (pack != null) {
                  msg(source, Component.translatable("message.tacz.converter.pack.start", new Object[]{file.getName()}));
                  GunMod.LOGGER.info("Attempt to converting legacy pack: {}", file.getName());

                  try {
                     pack.convert();
                  } catch (FileAlreadyExistsException e) {
                     msg(source, Component.translatable("message.tacz.converter.pack.exist"));
                     GunMod.LOGGER.warn("Target file already exists: {}", file.getName());
                     skip++;
                     continue;
                  } catch (Exception e) {
                     msg(source, Component.translatable("message.tacz.converter.pack.failed", new Object[]{file.getName()}));
                     GunMod.LOGGER.error("Failed to convert legacy pack: {}", file.getName(), e);
                     error++;
                     continue;
                  }

                  cnt++;
                  msg(source, Component.translatable("message.tacz.converter.pack.finish", new Object[]{file.getName()}));
                  GunMod.LOGGER.info("Legacy pack converted: {}", file.getName());
               }
            } else {
               msg(source, Component.translatable("message.tacz.converter.pack.folder", new Object[]{file.getName()}));
               GunMod.LOGGER.warn("Skip folder: {}", file.getName());
               skip++;
            }
         }

         watch.stop();
         double time = watch.getTime(TimeUnit.MICROSECONDS) / 1000.0;
         msg(source, Component.translatable("message.tacz.converter.finish", new Object[]{time, cnt, skip, error}));
         GunMod.LOGGER
            .info(
               "Convert finished! Total time: {} ms. Success : {}. Skipped {}. Failed: {}. Restart the game to load new packs!",
               new Object[]{time, cnt, skip, error}
            );
      }
   }

   public static PackConvertor.LegacyPack fromZipFile(File file) {
      try (ZipFile zipFile = new ZipFile(file)) {
         Enumeration<? extends ZipEntry> iteration = zipFile.entries();

         while (iteration.hasMoreElements()) {
            String path = iteration.nextElement().getName();
            Matcher matcher = PACK_INFO_PATTERN.matcher(path);
            if (matcher.find()) {
               String namespace = matcher.group(1);
               ZipEntry entry = zipFile.getEntry(path);

               try (InputStream stream = zipFile.getInputStream(entry)) {
                  PackInfo info = (PackInfo)GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackInfo.class);
                  if (info != null) {
                     return new PackConvertor.LegacyPack(file, namespace, info);
                  }
               } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                  GunMod.LOGGER.warn(exception.getMessage());
               }
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

      return null;
   }

   public record LegacyPack(File file, String namespace, PackInfo info) {
      private static final Pattern DISPLAY_PATTERN = Pattern.compile("^(\\w+)/(\\w+)/display/([\\w/.]+)$");
      private static final Pattern INDEX_PATTERN = Pattern.compile("^(\\w+)/(\\w+)/index/([\\w/.]+)$");
      private static final Pattern DATA_PATTERN = Pattern.compile("^(\\w+)/(\\w+)/data/([\\w/.]+)$");
      private static final Pattern MODELS_PATTERN = Pattern.compile("^(\\w+)/models/([\\w/.]+)$");
      private static final Pattern LANG_PATTERN = Pattern.compile("^(\\w+)/lang/([\\w/.]+)$");
      private static final Pattern ANIMATION_PATTERN = Pattern.compile("^(\\w+)/animations/([\\w/.]+)$");
      private static final Pattern TEXTURE_PATTERN = Pattern.compile("^(\\w+)/textures/([\\w/.]+)$");
      private static final Pattern SOUND_PATTERN = Pattern.compile("^(\\w+)/sounds/([\\w/.]+)$");
      private static final Pattern PLAYER_ANIMATOR_PATTERN = Pattern.compile("^(\\w+)/player_animator/([\\w/.]+)$");
      private static final Pattern TAGS_PATTERN = Pattern.compile("^(\\w+)/tags/([\\w/.]+)$");
      private static final Pattern RECIPE_PATTERN = Pattern.compile("^(\\w+)/recipes/([\\w/.]+)$");
      private static final Pattern PACK_INFO = Pattern.compile("^(\\w+)/pack\\.json$");

      private boolean parsePackInfo(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = PACK_INFO.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String newPath = "assets/" + namespace + "/gunpack_info.json";
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseRecipe(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = RECIPE_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "data/" + namespace + "/recipes/" + path;

            try (InputStream stream = oldPack.getInputStream(entry)) {
               JsonObject object = (JsonObject)PackConvertor.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
               if (object != null) {
                  object.addProperty("type", "tacz:gun_smith_table_crafting");
                  newZip.putNextEntry(new ZipEntry(newPath));
                  newZip.write(PackConvertor.GSON.toJson(object).getBytes());
                  newZip.closeEntry();
               }
            } catch (JsonParseException e) {
               GunMod.LOGGER.warn("Failed to parse recipe to new style: {}", entry.getName());
            }

            return true;
         } else {
            return false;
         }
      }

      private boolean parseTags(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = TAGS_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "data/" + namespace + "/tacz_tags/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parsePlayerAnimator(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = PLAYER_ANIMATOR_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "assets/" + namespace + "/player_animator/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseSound(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = SOUND_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "assets/" + namespace + "/tacz_sounds/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseTexture(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = TEXTURE_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "assets/" + namespace + "/textures/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseAnimation(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = ANIMATION_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "assets/" + namespace + "/animations/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseLang(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = LANG_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "assets/" + namespace + "/lang/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseModels(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = MODELS_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            String newPath = "assets/" + namespace + "/geo_models/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseDisplay(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = DISPLAY_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String type = matcher.group(2);
            String path = matcher.group(3);
            String newPath = "assets/" + namespace + "/display/" + type + "/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseIndex(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = INDEX_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String type = matcher.group(2);
            String path = matcher.group(3);
            String newPath = "data/" + namespace + "/index/" + type + "/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private boolean parseData(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
         Matcher matcher = DATA_PATTERN.matcher(entry.getName());
         if (matcher.find()) {
            String namespace = matcher.group(1);
            String type = matcher.group(2);
            String path = matcher.group(3);
            String newPath = "data/" + namespace + "/data/" + type + "/" + path;
            this.writeEntry(newZip, entry, oldPack, newPath);
            return true;
         } else {
            return false;
         }
      }

      private void writeEntry(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack, String newPath) throws IOException {
         newZip.putNextEntry(new ZipEntry(newPath));

         try (InputStream is = oldPack.getInputStream(entry)) {
            byte[] buffer = new byte[1024];

            int len;
            while ((len = is.read(buffer)) > 0) {
               newZip.write(buffer, 0, len);
            }
         }

         newZip.closeEntry();
      }

      private void addMeta(ZipOutputStream newZip) throws IOException {
         ZipEntry entry = new ZipEntry("gunpack.meta.json");
         newZip.putNextEntry(entry);
         PackMeta meta = new PackMeta(this.namespace, null);
         newZip.write(PackConvertor.GSON.toJson(meta).getBytes());
         newZip.closeEntry();
      }

      public void convert() throws FileAlreadyExistsException {
         Path newPath = FabricLoader.getInstance().getGameDir().resolve("tacz");
         String newName = this.file.getName().replace(".zip", "") + "_converted.zip";
         File newFile = new File(newPath.toFile(), newName);
         if (newFile.isFile() && newFile.exists()) {
            throw new FileAlreadyExistsException("File already exists: " + newFile.getName());
         }

         try (
            ZipFile oldPack = new ZipFile(this.file);
            FileOutputStream outputStream = new FileOutputStream(newFile);
            ZipOutputStream newZip = new ZipOutputStream(outputStream);
         ) {
            this.addMeta(newZip);
            Enumeration<? extends ZipEntry> iteration = oldPack.entries();

            while (iteration.hasMoreElements()) {
               ZipEntry entry = iteration.nextElement();
               if (!this.parseDisplay(newZip, entry, oldPack)
                  && !this.parseIndex(newZip, entry, oldPack)
                  && !this.parseData(newZip, entry, oldPack)
                  && !this.parseModels(newZip, entry, oldPack)
                  && !this.parseLang(newZip, entry, oldPack)
                  && !this.parseAnimation(newZip, entry, oldPack)
                  && !this.parseTexture(newZip, entry, oldPack)
                  && !this.parseSound(newZip, entry, oldPack)
                  && !this.parsePlayerAnimator(newZip, entry, oldPack)
                  && !this.parseTags(newZip, entry, oldPack)
                  && !this.parseRecipe(newZip, entry, oldPack)
                  && this.parsePackInfo(newZip, entry, oldPack)) {
               }
            }
         } catch (IOException e) {
            GunMod.LOGGER.warn("Failed to convert pack: {}", this.file.getName());
         }
      }
   }
}
