package com.tacz.guns.api.client.animation;

import com.tacz.guns.api.client.animation.interpolator.Interpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectAnimationChannel {
   public final ObjectAnimationChannel.ChannelType type;
   private final List<AnimationListener> listeners = new ArrayList<>();
   public String node;
   public AnimationChannelContent content;
   public Interpolator interpolator;
   boolean transitioning = false;

   public ObjectAnimationChannel(ObjectAnimationChannel.ChannelType type) {
      this.type = type;
      this.content = new AnimationChannelContent();
   }

   public ObjectAnimationChannel(ObjectAnimationChannel.ChannelType type, AnimationChannelContent content) {
      this.type = type;
      this.content = content;
   }

   public void addListener(AnimationListener listener) {
      if (listener.getType().equals(this.type)) {
         this.listeners.add(listener);
      } else {
         throw new RuntimeException("trying to add wrong type of listener to channel.");
      }
   }

   public void removeListener(AnimationListener listener) {
      this.listeners.remove(listener);
   }

   public void clearListeners() {
      this.listeners.clear();
   }

   public List<AnimationListener> getListeners() {
      return this.listeners;
   }

   public float getEndTimeS() {
      return this.content.keyframeTimeS.length == 0 ? 0.0F : this.content.keyframeTimeS[this.content.keyframeTimeS.length - 1];
   }

   public void update(float timeS, boolean blend) {
      if (!this.transitioning) {
         float[] result = this.getResult(timeS);

         for (AnimationListener listener : this.listeners) {
            listener.update(result, blend);
         }
      }
   }

   public float[] getResult(float timeS) {
      int indexFrom = this.computeIndex(timeS);
      int indexTo = Math.min(this.content.keyframeTimeS.length - 1, indexFrom + 1);
      float alpha = this.computeAlpha(timeS, indexFrom);
      return this.interpolator.interpolate(indexFrom, indexTo, alpha);
   }

   private int computeIndex(float timeS) {
      int index = Arrays.binarySearch(this.content.keyframeTimeS, timeS);
      return index >= 0 ? index : Math.max(0, -index - 2);
   }

   private float computeAlpha(float timeS, int indexFrom) {
      if (timeS <= this.content.keyframeTimeS[0]) {
         return 0.0F;
      }

      if (timeS >= this.content.keyframeTimeS[this.content.keyframeTimeS.length - 1]) {
         return 1.0F;
      }

      float local = timeS - this.content.keyframeTimeS[indexFrom];
      float delta = this.content.keyframeTimeS[indexFrom + 1] - this.content.keyframeTimeS[indexFrom];
      return local / delta;
   }

   public enum ChannelType {
      TRANSLATION,
      ROTATION,
      SCALE;
   }
}
