package com.tacz.guns.client.resource.pojo.animation.bedrock;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import javax.annotation.Nullable;

public class BedrockAnimation {
   @SerializedName("loop")
   private boolean loop;
   @SerializedName("animation_length")
   private double animationLength;
   @SerializedName("bones")
   @Nullable
   private Map<String, AnimationBone> bones;
   @SerializedName("sound_effects")
   private SoundEffectKeyframes soundEffects;

   public boolean isLoop() {
      return this.loop;
   }

   public double getAnimationLength() {
      return this.animationLength;
   }

   @Nullable
   public Map<String, AnimationBone> getBones() {
      return this.bones;
   }

   public SoundEffectKeyframes getSoundEffects() {
      return this.soundEffects;
   }
}
