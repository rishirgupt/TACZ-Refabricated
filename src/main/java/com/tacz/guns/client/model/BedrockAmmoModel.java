package com.tacz.guns.client.model;

import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import java.util.List;
import javax.annotation.Nullable;

public class BedrockAmmoModel extends BedrockModel {
   private static final String FIXED_ORIGIN_NODE = "fixed";
   private static final String GROUND_ORIGIN_NODE = "ground";
   private static final String THIRD_PERSON_HAND_ORIGIN_NODE = "thirdperson_hand";
   @Nullable
   protected List<BedrockPart> fixedOriginPath = this.getPath(this.modelMap.get("fixed"));
   @Nullable
   protected List<BedrockPart> groundOriginPath = this.getPath(this.modelMap.get("ground"));
   @Nullable
   protected List<BedrockPart> thirdPersonHandOriginPath = this.getPath(this.modelMap.get("thirdperson_hand"));

   public BedrockAmmoModel(BedrockModelPOJO pojo, BedrockVersion version) {
      super(pojo, version);
   }

   @Nullable
   public List<BedrockPart> getFixedOriginPath() {
      return this.fixedOriginPath;
   }

   @Nullable
   public List<BedrockPart> getGroundOriginPath() {
      return this.groundOriginPath;
   }

   @Nullable
   public List<BedrockPart> getThirdPersonHandOriginPath() {
      return this.thirdPersonHandOriginPath;
   }
}
