package com.tacz.guns.client.resource.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.api.client.animation.gltf.AnimationStructure;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.animation.gltf.RawAnimationStructure;
import com.tacz.guns.resource.manager.LazyJsonDataManager;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import org.jetbrains.annotations.Nullable;

public class GltfManager extends LazyJsonDataManager<AnimationStructure> {
   private static final FileToIdConverter FILE_TO_ID_CONVERTER = new FileToIdConverter("animations", ".gltf");

   public GltfManager() {
      super(AnimationStructure.class, ClientAssetsManager.GSON, FILE_TO_ID_CONVERTER, "GltfAnimationLoader", id -> "tacz".equals(id.getNamespace()));
   }

   @Nullable
   protected AnimationStructure parseJson(JsonElement element) throws JsonParseException {
      RawAnimationStructure rawStructure = (RawAnimationStructure)this.getGson().fromJson(element, RawAnimationStructure.class);
      return rawStructure == null ? null : new AnimationStructure(rawStructure);
   }

   public AnimationStructure getGltfAnimation(Identifier id) {
      return this.getData(id);
   }
}
