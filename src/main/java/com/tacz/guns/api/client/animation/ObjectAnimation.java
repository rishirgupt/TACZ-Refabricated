package com.tacz.guns.api.client.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectAnimation {
   public final String name;
   private final Map<String, List<ObjectAnimationChannel>> channels = new HashMap<>();
   @Nullable
   private ObjectAnimationSoundChannel soundChannel;
   @Nonnull
   public ObjectAnimation.PlayType playType = ObjectAnimation.PlayType.PLAY_ONCE_HOLD;
   private float maxEndTimeS = 0.0F;

   protected ObjectAnimation(@Nonnull String name) {
      this.name = Objects.requireNonNull(name);
   }

   public ObjectAnimation(ObjectAnimation source) {
      this.name = source.name;
      this.playType = source.playType;
      this.maxEndTimeS = source.maxEndTimeS;

      for (Entry<String, List<ObjectAnimationChannel>> entry : source.channels.entrySet()) {
         List<ObjectAnimationChannel> newList = new ArrayList<>();

         for (ObjectAnimationChannel channel : entry.getValue()) {
            ObjectAnimationChannel newChannel = new ObjectAnimationChannel(channel.type, channel.content);
            newChannel.node = channel.node;
            newChannel.interpolator = channel.interpolator;
            newList.add(newChannel);
         }

         this.channels.put(entry.getKey(), newList);
      }

      if (source.soundChannel != null) {
         this.soundChannel = new ObjectAnimationSoundChannel(source.soundChannel.content);
      }
   }

   protected void addChannel(ObjectAnimationChannel channel) {
      this.channels.compute(channel.node, (node, list) -> {
         if (list == null) {
            list = new ArrayList<>();
         }

         list.add(channel);
         return list;
      });
      if (channel.getEndTimeS() > this.maxEndTimeS) {
         this.maxEndTimeS = channel.getEndTimeS();
      }
   }

   protected void setSoundChannel(@Nonnull ObjectAnimationSoundChannel soundChannel) {
      if (soundChannel.getEndTimeS() > this.maxEndTimeS) {
         this.maxEndTimeS = (float)soundChannel.getEndTimeS();
      }

      this.soundChannel = soundChannel;
   }

   public Map<String, List<ObjectAnimationChannel>> getChannels() {
      return this.channels;
   }

   @Nullable
   public ObjectAnimationSoundChannel getSoundChannel() {
      return this.soundChannel;
   }

   public void applyAnimationListeners(AnimationListenerSupplier supplier) {
      for (List<ObjectAnimationChannel> channelList : this.channels.values()) {
         for (ObjectAnimationChannel channel : channelList) {
            AnimationListener listener = supplier.supplyListeners(channel.node, channel.type);
            if (listener != null) {
               channel.addListener(listener);
            }
         }
      }
   }

   public void update(boolean blend, float timeNs) {
      for (List<ObjectAnimationChannel> channels : this.channels.values()) {
         for (ObjectAnimationChannel channel : channels) {
            channel.update(timeNs / 1.0E9F, blend);
         }
      }
   }

   public float getMaxEndTimeS() {
      return this.maxEndTimeS;
   }

   public enum PlayType {
      PLAY_ONCE_HOLD,
      PLAY_ONCE_STOP,
      LOOP;
   }
}
