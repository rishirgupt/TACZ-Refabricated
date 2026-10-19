package com.tacz.guns.api.client.animation;

import com.tacz.guns.util.math.MathUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;

public class ObjectAnimationRunner {
   @Nonnull
   private final ObjectAnimation animation;
   protected long transitionTimeNs;
   protected ArrayList<float[]> valueFrom;
   protected ArrayList<float[]> valueRecover;
   protected ArrayList<ObjectAnimationChannel> transitionFromChannels;
   protected ArrayList<ObjectAnimationChannel> transitionToChannels;
   protected ArrayList<ObjectAnimationChannel> recoverChannels;
   private boolean running = false;
   private boolean pausing = false;
   private long lastUpdateNs;
   private long progressNs;
   private boolean isTransitioning = false;
   @Nullable
   private ObjectAnimationRunner transitionTo;
   private long transitionProgressNs;

   public ObjectAnimationRunner(@Nonnull ObjectAnimation animation) {
      this.animation = Objects.requireNonNull(animation);
   }

   @Nonnull
   public ObjectAnimation getAnimation() {
      return this.animation;
   }

   @Nullable
   public ObjectAnimationRunner getTransitionTo() {
      return this.transitionTo;
   }

   public boolean isTransitioning() {
      return this.isTransitioning;
   }

   public void run() {
      if (!this.running) {
         this.running = true;
         this.lastUpdateNs = System.nanoTime();
      }

      this.pausing = false;
   }

   public void pause() {
      this.running = false;
      this.pausing = true;
   }

   public void hold() {
      this.progressNs = (long)(this.animation.getMaxEndTimeS() * 1.0E9) + 1L;
      this.running = false;
   }

   public void stop() {
      this.progressNs = (long)(this.animation.getMaxEndTimeS() * 1.0E9) + 2L;
      this.running = false;
   }

   public void reset() {
      this.progressNs = 0L;
   }

   public long getProgressNs() {
      return this.progressNs;
   }

   public void setProgressNs(long progressNs) {
      this.progressNs = progressNs;
   }

   public void transition(ObjectAnimationRunner transitionTo, long transitionTimeNS) {
      if (this.transitionTo == null) {
         this.valueFrom = new ArrayList<>();
         this.valueRecover = new ArrayList<>();
         this.transitionFromChannels = new ArrayList<>();
         this.transitionToChannels = new ArrayList<>();
         this.recoverChannels = new ArrayList<>();
         this.transitionTo = transitionTo;
         this.running = false;

         for (Entry<String, List<ObjectAnimationChannel>> entry : this.animation.getChannels().entrySet()) {
            List<ObjectAnimationChannel> toChannels = transitionTo.animation.getChannels().get(entry.getKey());
            if (toChannels != null) {
               for (ObjectAnimationChannel channel : entry.getValue()) {
                  Optional<ObjectAnimationChannel> toChannel = toChannels.stream().filter(c -> c.type.equals(channel.type)).findAny();
                  float[] value = channel.getResult((float)this.progressNs / 1.0E9F);
                  if (channel.type == ObjectAnimationChannel.ChannelType.ROTATION && value.length == 3) {
                     value = MathUtil.toQuaternion(value[0], value[1], value[2]);
                  }

                  if (toChannel.isPresent()) {
                     this.valueFrom.add(value);
                     this.transitionFromChannels.add(channel);
                     this.transitionToChannels.add(toChannel.get());
                     toChannel.get().transitioning = true;
                  } else {
                     this.valueRecover.add(value);
                     this.recoverChannels.add(channel);
                  }
               }
            } else {
               for (ObjectAnimationChannel channel : entry.getValue()) {
                  float[] value = channel.getResult((float)this.progressNs / 1.0E9F);
                  if (channel.type == ObjectAnimationChannel.ChannelType.ROTATION && value.length == 3) {
                     value = MathUtil.toQuaternion(value[0], value[1], value[2]);
                  }

                  this.valueRecover.add(value);
                  this.recoverChannels.add(channel);
               }
            }
         }
      } else if (this.isTransitioning) {
         ArrayList<float[]> newValueFrom = new ArrayList<>();
         ArrayList<float[]> newValueRecover = new ArrayList<>();
         ArrayList<ObjectAnimationChannel> newTransitionFromChannels = new ArrayList<>();
         ArrayList<ObjectAnimationChannel> newTransitionToChannels = new ArrayList<>();
         ArrayList<ObjectAnimationChannel> newRecoverChannels = new ArrayList<>();

         for (int i = 0; i < this.transitionFromChannels.size(); i++) {
            assert this.transitionTo != null;
            ObjectAnimationChannel fromChannel = this.transitionFromChannels.get(i);
            ObjectAnimationChannel toChannel = this.transitionToChannels.get(i);
            float[] from = this.valueFrom.get(i);
            float[] to = toChannel.getResult((float)this.transitionTo.progressNs / 1.0E9F);
            float progress = this.easeOutCubic((float)this.transitionProgressNs / (float)this.transitionTimeNs);
            float[] result;
            if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
               result = new float[3];
               this.lerp(from, to, progress, result);
            } else if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
               result = new float[4];
               if (to.length == 3) {
                  to = MathUtil.toQuaternion(to[0], to[1], to[2]);
               }

               this.slerp(from, to, progress, result);
            } else {
               result = new float[3];
               this.lerp(from, to, progress, result);
            }

            List<ObjectAnimationChannel> newToChannels = transitionTo.animation.getChannels().get(fromChannel.node);
            if (newToChannels != null) {
               Optional<ObjectAnimationChannel> newToChannel = newToChannels.stream().filter(c -> c.type.equals(fromChannel.type)).findAny();
               if (newToChannel.isPresent()) {
                  newValueFrom.add(result);
                  newTransitionFromChannels.add(fromChannel);
                  newTransitionToChannels.add(newToChannel.get());
                  newToChannel.get().transitioning = true;
               } else {
                  newValueRecover.add(result);
                  newRecoverChannels.add(fromChannel);
               }
            } else {
               newValueRecover.add(result);
               newRecoverChannels.add(fromChannel);
            }

            toChannel.transitioning = false;
         }

         this.valueFrom = newValueFrom;
         this.valueRecover = newValueRecover;
         this.transitionToChannels = newTransitionToChannels;
         this.transitionFromChannels = newTransitionFromChannels;
         this.recoverChannels = newRecoverChannels;
         this.transitionTo = transitionTo;
      }

      this.transitionTimeNs = transitionTimeNS;
      this.transitionProgressNs = 0L;
      this.isTransitioning = true;
   }

   public long getTransitionTimeNs() {
      return this.transitionTimeNs;
   }

   public long getTransitionProgressNs() {
      return this.transitionProgressNs;
   }

   public void setTransitionProgressNs(long progressNs) {
      this.transitionProgressNs = progressNs;
   }

   public void stopTransition() {
      this.isTransitioning = false;

      for (ObjectAnimationChannel channel : this.transitionToChannels) {
         channel.transitioning = false;
      }

      this.transitionTimeNs = 0L;
      this.transitionProgressNs = 0L;
      this.transitionFromChannels = null;
      this.transitionToChannels = null;
      this.recoverChannels = null;
      this.valueFrom = null;
      this.valueRecover = null;
   }

   private void updateProgress(long alphaProgress) {
      if (this.running) {
         this.progressNs += alphaProgress;
      }

      switch (this.animation.playType) {
         case PLAY_ONCE_HOLD:
            if (this.progressNs / 1.0E9 > this.animation.getMaxEndTimeS()) {
               this.hold();
            }
            break;
         case PLAY_ONCE_STOP:
            if (this.progressNs / 1.0E9 > this.animation.getMaxEndTimeS()) {
               this.stop();
            }
            break;
         case LOOP:
            if (this.progressNs / 1.0E9 > this.animation.getMaxEndTimeS()) {
               if (this.animation.getMaxEndTimeS() == 0.0F) {
                  this.progressNs = 0L;
               } else {
                  this.progressNs = this.progressNs % (long)(this.animation.getMaxEndTimeS() * 1.0E9);
               }
            }
      }
   }

   public void update(boolean blend) {
      long fromTimeNs = this.progressNs;
      long currentNs = System.nanoTime();
      long alphaProgress = currentNs - this.lastUpdateNs;
      this.updateProgress(alphaProgress);
      this.lastUpdateNs = currentNs;
      if (this.isTransitioning) {
         this.transitionProgressNs += alphaProgress;
         if (this.transitionProgressNs >= this.transitionTimeNs) {
            this.stopTransition();
         } else {
            float transitionProgress = (float)this.transitionProgressNs / (float)this.transitionTimeNs;
            this.updateTransition(this.easeOutCubic(transitionProgress), blend);
         }
      } else {
         this.animation.update(blend, (float)this.progressNs);
         ObjectAnimationSoundChannel soundChannel = this.animation.getSoundChannel();
         if (soundChannel != null && Minecraft.getInstance().player != null) {
            soundChannel.playSound(fromTimeNs / 1.0E9, this.progressNs / 1.0E9, Minecraft.getInstance().player, 16, 1.0F, 1.0F);
         }
      }
   }

   public void updateSoundOnly() {
      long fromTimeNs = this.progressNs;
      long currentNs = System.nanoTime();
      this.updateProgress(currentNs - this.lastUpdateNs);
      this.lastUpdateNs = currentNs;
      ObjectAnimationSoundChannel soundChannel = this.animation.getSoundChannel();
      if (soundChannel != null && Minecraft.getInstance().player != null) {
         soundChannel.playSound(fromTimeNs / 1.0E9, this.progressNs / 1.0E9, Minecraft.getInstance().player, 16, 1.0F, 1.0F);
      }
   }

   public boolean isRunning() {
      return this.running;
   }

   public boolean isPausing() {
      return this.pausing;
   }

   public boolean isHolding() {
      return this.progressNs == (long)(this.getAnimation().getMaxEndTimeS() * 1.0E9) + 1L;
   }

   public boolean isStopped() {
      return this.progressNs == (long)(this.getAnimation().getMaxEndTimeS() * 1.0E9) + 2L;
   }

   private void updateTransition(float progress, boolean blend) {
      assert this.transitionTo != null;

      for (int i = 0; i < this.transitionToChannels.size(); i++) {
         ObjectAnimationChannel fromChannel = this.transitionFromChannels.get(i);
         ObjectAnimationChannel toChannel = this.transitionToChannels.get(i);
         float[] from = this.valueFrom.get(i);
         float[] to = toChannel.getResult((float)this.transitionTo.progressNs / 1.0E9F);
         float[] result;
         if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
            result = new float[3];
            this.lerp(from, to, progress, result);
         } else if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
            result = new float[4];
            if (to.length == 3) {
               to = MathUtil.toQuaternion(to[0], to[1], to[2]);
            }

            this.slerp(from, to, progress, result);
         } else {
            result = new float[3];
            this.lerp(from, to, progress, result);
         }

         for (AnimationListener listener : fromChannel.getListeners()) {
            listener.update(result, blend);
         }
      }

      if (this.animation.playType != ObjectAnimation.PlayType.PLAY_ONCE_STOP) {
         for (int i = 0; i < this.recoverChannels.size(); i++) {
            ObjectAnimationChannel channel = this.recoverChannels.get(i);
            float[] from = this.valueRecover.get(i);
            if (channel.type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
               float[] result = new float[3];
               float[] to = new float[]{0.0F, 0.0F, 0.0F};

               for (AnimationListener listener : channel.getListeners()) {
                  this.lerp(from, to, progress, result);
                  listener.update(result, blend);
               }
            } else if (channel.type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
               float[] result = new float[4];
               float[] to = new float[]{0.0F, 0.0F, 0.0F, 1.0F};

               for (AnimationListener listener : channel.getListeners()) {
                  this.slerp(from, to, progress, result);
                  listener.update(result, blend);
               }
            } else if (channel.type.equals(ObjectAnimationChannel.ChannelType.SCALE)) {
               float[] result = new float[3];
               float[] to = new float[]{1.0F, 1.0F, 1.0F};

               for (AnimationListener listener : channel.getListeners()) {
                  this.lerp(from, to, progress, result);
                  listener.update(result, blend);
               }
            }
         }
      }
   }

   private float easeOutCubic(double x) {
      return (float)(1.0 - Math.pow(1.0 - x, 4.0));
   }

   private void lerp(float[] from, float[] to, float alpha, float[] result) {
      for (int i = 0; i < result.length; i++) {
         result[i] = from[i] * (1.0F - alpha) + to[i] * alpha;
      }
   }

   private void slerp(float[] from, float[] to, float alpha, float[] result) {
      float ax = from[0];
      float ay = from[1];
      float az = from[2];
      float aw = from[3];
      float bx = to[0];
      float by = to[1];
      float bz = to[2];
      float bw = to[3];
      float dot = ax * bx + ay * by + az * bz + aw * bw;
      if (dot < 0.0F) {
         bx = -bx;
         by = -by;
         bz = -bz;
         bw = -bw;
         dot = -dot;
      }

      float epsilon = 1.0E-6F;
      float s0;
      float s1;
      if (1.0 - dot > epsilon) {
         float omega = (float)Math.acos(dot);
         float invSinOmega = 1.0F / (float)Math.sin(omega);
         s0 = (float)Math.sin((1.0 - alpha) * omega) * invSinOmega;
         s1 = (float)Math.sin(alpha * omega) * invSinOmega;
      } else {
         s0 = 1.0F - alpha;
         s1 = alpha;
      }

      float rx = s0 * ax + s1 * bx;
      float ry = s0 * ay + s1 * by;
      float rz = s0 * az + s1 * bz;
      float rw = s0 * aw + s1 * bw;
      result[0] = rx;
      result[1] = ry;
      result[2] = rz;
      result[3] = rw;
   }
}
