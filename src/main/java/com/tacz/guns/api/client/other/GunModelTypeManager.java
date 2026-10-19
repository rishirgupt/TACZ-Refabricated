package com.tacz.guns.api.client.other;

import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class GunModelTypeManager {
   public static final Map<String, BiFunction<BedrockModelPOJO, BedrockVersion, ? extends BedrockGunModel>> GUN_MODEL_TYPE_MAP = new HashMap<>();

   public static synchronized void registerModelType(String typeName, BiFunction<BedrockModelPOJO, BedrockVersion, ? extends BedrockGunModel> constructor) {
      GUN_MODEL_TYPE_MAP.put(typeName, constructor);
   }

   public static synchronized BiFunction<BedrockModelPOJO, BedrockVersion, ? extends BedrockGunModel> getModelInstanceConstructor(String typeName) {
      return GUN_MODEL_TYPE_MAP.getOrDefault(typeName, BedrockGunModel::new);
   }
}
