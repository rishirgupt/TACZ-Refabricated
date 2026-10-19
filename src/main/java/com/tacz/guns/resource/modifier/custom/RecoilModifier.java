package com.tacz.guns.resource.modifier.custom;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import com.tacz.guns.resource.pojo.data.gun.GunRecoilKeyFrame;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class RecoilModifier implements IAttachmentModifier<Pair<Modifier, Modifier>, ParameterizedCachePair<Float, Float>> {
   public static final String ID = GunProperties.RECOIL.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public String getOptionalFields() {
      return "recoil_modifier";
   }

   @Override
   public JsonProperty<Pair<Modifier, Modifier>> readJson(String json) {
      RecoilModifier.Data data = (RecoilModifier.Data)CommonAssetsManager.GSON.fromJson(json, RecoilModifier.Data.class);
      RecoilModifier.NewRecoilData newRecoilData = data.newRecoilData;
      RecoilModifier.OldRecoilData oldRecoilData = data.oldRecoilData;
      if (newRecoilData == null && oldRecoilData != null) {
         Modifier pitch = new Modifier();
         Modifier yaw = new Modifier();
         pitch.setPercent(oldRecoilData.getPitch());
         yaw.setPercent(oldRecoilData.getYaw());
         return new RecoilModifier.RecoilJsonProperty(Pair.of(pitch, yaw));
      } else {
         assert newRecoilData != null;
         return new RecoilModifier.RecoilJsonProperty(Pair.of(newRecoilData.getPitch(), newRecoilData.getYaw()));
      }
   }

   @Override
   public CacheValue<ParameterizedCachePair<Float, Float>> initCache(ItemStack gunItem, GunData gunData) {
      GunRecoil recoil = gunData.getRecoil();
      if (recoil == null) {
         return new CacheValue<>(ParameterizedCachePair.of(0.0F, 0.0F));
      }

      float pitch = getMaxInGunRecoilKeyFrame(recoil.getPitch());
      float yaw = getMaxInGunRecoilKeyFrame(recoil.getYaw());
      return new CacheValue<>(ParameterizedCachePair.of(pitch, yaw));
   }

   @Override
   public void eval(List<Pair<Modifier, Modifier>> modifiedValues, CacheValue<ParameterizedCachePair<Float, Float>> cache) {
      List<Modifier> yaw = Lists.newArrayList();
      List<Modifier> pitch = Lists.newArrayList();

      for (Pair<Modifier, Modifier> modifiedValue : modifiedValues) {
         pitch.add((Modifier)modifiedValue.left());
         yaw.add((Modifier)modifiedValue.right());
      }

      ParameterizedCachePair<Float, Float> newCache = ParameterizedCachePair.of(
         pitch, yaw, cache.getValue().left().getDefaultValue(), cache.getValue().right().getDefaultValue()
      );
      cache.setValue(newCache);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      ParameterizedCachePair<Float, Float> propertyCache = cacheProperty.getCache(ID);
      GunRecoil recoil = gunData.getRecoil();
      double pitch = propertyCache.left().getDefaultValue().floatValue();
      double modifiedPitch = propertyCache.left().eval(getMaxInGunRecoilKeyFrame(recoil.getPitch()));
      double pitchModifier = modifiedPitch - pitch;
      double pitchPercent = Math.min(pitch / 5.0, 1.0);
      double pitchModifierPercent = Math.min(pitchModifier / 5.0, 1.0);
      String pitchTitleKey = "gui.tacz.gun_refit.property_diagrams.pitch";
      String pitchPositivelyString = String.format("%.2f §c(+%.2f)", modifiedPitch, pitchModifier);
      String pitchNegativelyString = String.format("%.2f §a(%.2f)", modifiedPitch, pitchModifier);
      String pitchDefaultString = String.format("%.2f", modifiedPitch);
      double yaw = propertyCache.right().getDefaultValue().floatValue();
      double modifiedYaw = propertyCache.right().eval(getMaxInGunRecoilKeyFrame(recoil.getYaw()));
      double yawModifier = modifiedYaw - yaw;
      double yawPercent = Math.min(yaw / 5.0, 1.0);
      double yawModifierPercent = Math.min(yawModifier / 5.0, 1.0);
      String yawTitleKey = "gui.tacz.gun_refit.property_diagrams.yaw";
      String yawPositivelyString = String.format("%.2f §c(+%.2f)", modifiedYaw, yawModifier);
      String yawNegativelyString = String.format("%.2f §a(%.2f)", modifiedYaw, yawModifier);
      String yawDefaultString = String.format("%.2f", modifiedYaw);
      boolean positivelyBetter = false;
      IAttachmentModifier.DiagramsData pitchData = new IAttachmentModifier.DiagramsData(
         pitchPercent, pitchModifierPercent, pitchModifier, pitchTitleKey, pitchPositivelyString, pitchNegativelyString, pitchDefaultString, positivelyBetter
      );
      IAttachmentModifier.DiagramsData yawData = new IAttachmentModifier.DiagramsData(
         yawPercent, yawModifierPercent, yawModifier, yawTitleKey, yawPositivelyString, yawNegativelyString, yawDefaultString, positivelyBetter
      );
      return List.of(pitchData, yawData);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public int getDiagramsDataSize() {
      return 2;
   }

   private static float getMaxInGunRecoilKeyFrame(GunRecoilKeyFrame[] frames) {
      if (frames.length == 0) {
         return 0.0F;
      }

      float[] value = frames[0].getValue();
      float leftValue = Math.abs(value[0]);
      float rightValue = Math.abs(value[1]);
      return Math.max(leftValue, rightValue);
   }

   public static class Data {
      @SerializedName("recoil_modifier")
      @Deprecated
      @Nullable
      private RecoilModifier.OldRecoilData oldRecoilData = null;
      @SerializedName("recoil")
      @Nullable
      private RecoilModifier.NewRecoilData newRecoilData = null;
   }

   private static class NewRecoilData {
      @SerializedName("pitch")
      private Modifier pitch = new Modifier();
      @SerializedName("yaw")
      private Modifier yaw = new Modifier();

      public Modifier getPitch() {
         return this.pitch;
      }

      public Modifier getYaw() {
         return this.yaw;
      }
   }

   @Deprecated
   private static class OldRecoilData {
      @SerializedName("pitch")
      private float pitch = 0.0F;
      @SerializedName("yaw")
      private float yaw = 0.0F;

      public float getPitch() {
         return this.pitch;
      }

      public float getYaw() {
         return this.yaw;
      }
   }

   public static class RecoilJsonProperty extends JsonProperty<Pair<Modifier, Modifier>> {
      public RecoilJsonProperty(Pair<Modifier, Modifier> value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Pair<Modifier, Modifier> modified = this.getValue();
         float pitch = 1.0F;
         float yaw = 1.0F;
         if (modified != null) {
            pitch = (float)AttachmentPropertyManager.eval((Modifier)modified.left(), 1.0);
            yaw = (float)AttachmentPropertyManager.eval((Modifier)modified.right(), 1.0);
         }

         if (pitch > 1.0F) {
            this.components.add(Component.translatable("tooltip.tacz.attachment.pitch.increase").withStyle(ChatFormatting.RED));
         } else if (pitch < 1.0F) {
            this.components.add(Component.translatable("tooltip.tacz.attachment.pitch.decrease").withStyle(ChatFormatting.GREEN));
         }

         if (yaw > 1.0F) {
            this.components.add(Component.translatable("tooltip.tacz.attachment.yaw.increase").withStyle(ChatFormatting.RED));
         } else if (yaw < 1.0F) {
            this.components.add(Component.translatable("tooltip.tacz.attachment.yaw.decrease").withStyle(ChatFormatting.GREEN));
         }
      }
   }
}
