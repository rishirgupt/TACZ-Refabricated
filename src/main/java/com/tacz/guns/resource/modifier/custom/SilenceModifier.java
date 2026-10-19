package com.tacz.guns.resource.modifier.custom;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class SilenceModifier implements IAttachmentModifier<Pair<Modifier, Boolean>, Pair<Integer, Boolean>> {
   public static final String ID = GunProperties.SILENCE.name();

   @Override
   public String getId() {
      return ID;
   }

   public SilenceModifier.SilenceJsonProperty readJson(String json) {
      SilenceModifier.Data data = (SilenceModifier.Data)CommonAssetsManager.GSON.fromJson(json, SilenceModifier.Data.class);
      SilenceModifier.Silence silence = data.getSilence();
      if (silence == null) {
         return new SilenceModifier.SilenceJsonProperty(Pair.of(new Modifier(), false));
      }

      Modifier distance = silence.getDistance();
      if (distance == null) {
         distance = new Modifier();
         distance.setAddend(silence.getDistanceAddend());
      }

      return new SilenceModifier.SilenceJsonProperty(Pair.of(distance, silence.isUseSilenceSound()));
   }

   @Override
   public CacheValue<Pair<Integer, Boolean>> initCache(ItemStack gunItem, GunData gunData) {
      int defaultDistance = (Integer)GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get();
      return new CacheValue<>(Pair.of(defaultDistance, false));
   }

   @Override
   public void eval(List<Pair<Modifier, Boolean>> modifiedValues, CacheValue<Pair<Integer, Boolean>> cache) {
      List<Modifier> distanceModifiers = Lists.newArrayList();
      List<Boolean> useSilenceSoundModifiers = Lists.newArrayList();
      modifiedValues.forEach(v -> {
         distanceModifiers.add((Modifier)v.left());
         useSilenceSoundModifiers.add((Boolean)v.right());
      });
      Pair<Integer, Boolean> cacheValue = cache.getValue();
      double evalDistance = AttachmentPropertyManager.eval(distanceModifiers, ((Integer)cacheValue.left()).intValue());
      boolean useSilenceSound = AttachmentPropertyManager.eval(useSilenceSoundModifiers, (Boolean)cacheValue.right());
      cache.setValue(Pair.of((int)Math.round(evalDistance), useSilenceSound));
   }

   private static class Data {
      @Nullable
      @SerializedName("silence")
      private SilenceModifier.Silence silence = null;

      @Nullable
      public SilenceModifier.Silence getSilence() {
         return this.silence;
      }
   }

   private static class Silence {
      @Deprecated
      @SerializedName("distance_addend")
      private int distanceAddend = 0;
      @Nullable
      @SerializedName("distance")
      private Modifier distance = null;
      @SerializedName("use_silence_sound")
      private boolean useSilenceSound = false;

      @Deprecated
      public int getDistanceAddend() {
         return this.distanceAddend;
      }

      @Nullable
      public Modifier getDistance() {
         return this.distance;
      }

      public boolean isUseSilenceSound() {
         return this.useSilenceSound;
      }
   }

   public static class SilenceJsonProperty extends JsonProperty<Pair<Modifier, Boolean>> {
      public SilenceJsonProperty(Pair<Modifier, Boolean> value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Pair<Modifier, Boolean> value = this.getValue();
         if (value != null) {
            int defaultDistance = (Integer)GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get();
            double eval = AttachmentPropertyManager.eval((Modifier)value.left(), defaultDistance);
            int distance = (int)Math.round(eval);
            if (distance > defaultDistance) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.sound_distance.increase").withStyle(ChatFormatting.RED));
            } else if (distance < defaultDistance) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.sound_distance.increase").withStyle(ChatFormatting.GREEN));
            }

            if ((Boolean)value.right()) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.silence").withStyle(ChatFormatting.GREEN));
            }
         }
      }
   }
}
