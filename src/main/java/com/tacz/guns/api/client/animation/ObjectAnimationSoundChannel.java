package com.tacz.guns.api.client.animation;

import com.tacz.guns.client.sound.SoundPlayManager;
import java.util.Arrays;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ObjectAnimationSoundChannel {
   public AnimationSoundChannelContent content;

   public ObjectAnimationSoundChannel() {
   }

   public ObjectAnimationSoundChannel(AnimationSoundChannelContent content) {
      this.content = content;
   }

   public void playSound(double fromTimeS, double toTimeS, Entity entity, int distance, float volume, float pitch) {
      if (this.content != null) {
         if (fromTimeS != toTimeS) {
            if (fromTimeS > toTimeS && fromTimeS <= this.getEndTimeS()) {
               this.playSound(0.0, toTimeS, entity, distance, volume, pitch);
               toTimeS = this.getEndTimeS();
            }

            int to = this.computeIndex(toTimeS, false);
            int from = this.computeIndex(fromTimeS, true);
            float mixVolume = volume;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
               mixVolume *= 1.0F - Math.min(1.0F, (float)Math.sqrt(player.distanceToSqr(entity.getPosition(0.0F))) / distance);
               mixVolume *= mixVolume;
            }

            for (int i = from + 1; i <= to; i++) {
               Identifier name = this.content.keyframeSoundName[i];
               SoundPlayManager.playAnimationSound(entity, name, mixVolume, pitch, distance);
            }
         }
      }
   }

   public double getEndTimeS() {
      return this.content.keyframeTimeS[this.content.keyframeTimeS.length - 1];
   }

   private int computeIndex(double timeS, boolean open) {
      int index = Arrays.binarySearch(this.content.keyframeTimeS, timeS);
      if (index >= 0) {
         return open ? index - 1 : index;
      } else {
         return -index - 2;
      }
   }
}
