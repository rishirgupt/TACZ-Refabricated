package com.tacz.guns.client.resource.pojo.animation.bedrock;

import com.google.gson.annotations.SerializedName;

public class AnimationBone {
   @SerializedName("position")
   private AnimationKeyframes position;
   @SerializedName("rotation")
   private AnimationKeyframes rotation;
   @SerializedName("scale")
   private AnimationKeyframes scale;

   public AnimationKeyframes getPosition() {
      return this.position;
   }

   public AnimationKeyframes getRotation() {
      return this.rotation;
   }

   public AnimationKeyframes getScale() {
      return this.scale;
   }
}
