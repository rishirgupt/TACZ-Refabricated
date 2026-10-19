package com.tacz.guns.api.client.animation;

import java.util.Arrays;

public class AnimationChannelContent {
   public float[] keyframeTimeS;
   public float[][] values;
   public AnimationChannelContent.LerpMode[] lerpModes;

   public AnimationChannelContent() {
   }

   public AnimationChannelContent(AnimationChannelContent source) {
      if (source.keyframeTimeS != null) {
         this.keyframeTimeS = Arrays.copyOf(source.keyframeTimeS, source.keyframeTimeS.length);
      }

      if (source.values != null) {
         this.values = Arrays.stream(source.values).map(values -> Arrays.copyOf(values, values.length)).toArray(float[][]::new);
      }

      if (source.lerpModes != null) {
         this.lerpModes = Arrays.copyOf(source.lerpModes, source.lerpModes.length);
      }
   }

   public enum LerpMode {
      LINEAR,
      SPHERICAL_LINEAR,
      CATMULLROM,
      SPHERICAL_SQUAD;
   }
}
