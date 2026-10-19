package com.tacz.guns.client.resource;

import com.google.common.collect.Maps;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.pojo.animation.bedrock.BedrockAnimationFile;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;

public class InternalAssetLoader {
   public static final Identifier DEFAULT_BULLET_TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/entity/basic_bullet.png");
   public static final Identifier DEFAULT_BULLET_MODEL = Identifier.fromNamespaceAndPath("tacz", "models/bedrock/basic_bullet.json");
   public static final Identifier TARGET_MINECART_MODEL_LOCATION = Identifier.fromNamespaceAndPath("tacz", "models/bedrock/target_minecart.json");
   public static final Identifier TARGET_MINECART_TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("tacz", "textures/entity/target_minecart.png");
   public static final Identifier ENTITY_EMPTY_TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/entity/empty.png");
   public static final Identifier TARGET_MODEL_LOCATION = Identifier.fromNamespaceAndPath("tacz", "models/bedrock/target.json");
   public static final Identifier TARGET_TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("tacz", "textures/block/target.png");
   public static final Identifier STATUE_MODEL_LOCATION = Identifier.fromNamespaceAndPath("tacz", "models/bedrock/statue.json");
   public static final Identifier STATUE_TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("tacz", "textures/block/statue.png");
   public static final Identifier SMITH_TABLE_MODEL_LOCATION = Identifier.fromNamespaceAndPath("tacz", "models/bedrock/gun_smith_table.json");
   public static final Identifier SMITH_TABLE_TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("tacz", "textures/block/gun_smith_table.png");
   private static final Identifier DEFAULT_PISTOL_ANIMATIONS_LOC = Identifier.fromNamespaceAndPath("tacz", "animations/pistol_default.animation.json");
   private static final Identifier DEFAULT_RIFLE_ANIMATIONS_LOC = Identifier.fromNamespaceAndPath("tacz", "animations/rifle_default.animation.json");
   private static final Map<Identifier, BedrockModel> BEDROCK_MODELS = Maps.newHashMap();
   private static List<ObjectAnimation> defaultPistolAnimations;
   private static List<ObjectAnimation> defaultRifleAnimations;

   public static void onResourceReload() {
      BedrockAnimationFile pistolAnimationFile = loadAnimations(DEFAULT_PISTOL_ANIMATIONS_LOC);
      BedrockAnimationFile rifleAnimationFile = loadAnimations(DEFAULT_RIFLE_ANIMATIONS_LOC);
      defaultPistolAnimations = Animations.createAnimationFromBedrock(pistolAnimationFile);
      defaultRifleAnimations = Animations.createAnimationFromBedrock(rifleAnimationFile);
      BEDROCK_MODELS.clear();
      loadBedrockModels(SMITH_TABLE_MODEL_LOCATION);
      loadBedrockModels(TARGET_MODEL_LOCATION);
      loadBedrockModels(TARGET_MINECART_MODEL_LOCATION);
      loadBedrockModels(DEFAULT_BULLET_MODEL);
      loadBedrockModels(STATUE_MODEL_LOCATION);
   }

   private static BedrockAnimationFile loadAnimations(Identifier resourceLocation) {
      try (InputStream inputStream = Minecraft.getInstance().getResourceManager().open(resourceLocation)) {
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
         JsonObject json = JsonParser.parseReader(bufferedReader).getAsJsonObject();
         return (BedrockAnimationFile)ClientAssetsManager.GSON.fromJson(json, BedrockAnimationFile.class);
      } catch (IOException | JsonSyntaxException | JsonIOException e) {
         throw new RuntimeException(e);
      }
   }

   private static void loadBedrockModels(Identifier location) {
      try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location)) {
         BedrockModelPOJO pojo = (BedrockModelPOJO)ClientAssetsManager.GSON
            .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), BedrockModelPOJO.class);
         BEDROCK_MODELS.put(location, new BedrockModel(pojo, BedrockVersion.NEW));
      } catch (IOException | JsonSyntaxException | JsonIOException e) {
         e.fillInStackTrace();
      }
   }

   public static List<ObjectAnimation> getDefaultPistolAnimations() {
      return defaultPistolAnimations;
   }

   public static List<ObjectAnimation> getDefaultRifleAnimations() {
      return defaultRifleAnimations;
   }

   public static Optional<BedrockModel> getBedrockModel(Identifier location) {
      return Optional.ofNullable(BEDROCK_MODELS.get(location));
   }
}
