package com.tacz.guns.client.resource.pojo.animation.bedrock;

import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import javax.annotation.Nullable;
import org.joml.Vector3f;

public class AnimationKeyframes {
   private final Double2ObjectRBTreeMap<AnimationKeyframes.Keyframe> keyframes;

   public AnimationKeyframes(Double2ObjectRBTreeMap<AnimationKeyframes.Keyframe> keyframes) {
      this.keyframes = keyframes;
   }

   public Double2ObjectRBTreeMap<AnimationKeyframes.Keyframe> getKeyframes() {
      return this.keyframes;
   }

   public record Keyframe(@Nullable Vector3f pre, @Nullable Vector3f post, @Nullable Vector3f data, @Nullable String lerpMode) {
   }
}
