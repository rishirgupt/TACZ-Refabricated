package com.tacz.guns.resource.modifier.custom;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunFireModeAdjustData;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class InaccuracyModifier implements IAttachmentModifier<Map<InaccuracyType, Modifier>, Map<InaccuracyType, Float>> {
   public static final String ID = GunProperties.INACCURACY.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public String getOptionalFields() {
      return "inaccuracy_addend";
   }

   @Override
   public JsonProperty<Map<InaccuracyType, Modifier>> readJson(String json) {
      InaccuracyModifier.Data data = (InaccuracyModifier.Data)CommonAssetsManager.GSON.fromJson(json, InaccuracyModifier.Data.class);
      Modifier inaccuracy = data.getInaccuracy();
      Modifier aimInaccuracy = data.getAimInaccuracy();
      Modifier sneakInaccuracy = data.getSneakInaccuracy();
      Modifier lieInaccuracy = data.getLieInaccuracy();
      if (inaccuracy == null) {
         float inaccuracyAddendTime = data.getInaccuracyAddendTime();
         inaccuracy = new Modifier();
         inaccuracy.setAddend(inaccuracyAddendTime);
      }

      Map<InaccuracyType, Modifier> jsonProperties = Maps.newHashMap();

      for (InaccuracyType type : InaccuracyType.values()) {
         switch (type) {
            case AIM:
               if (aimInaccuracy != null) {
                  jsonProperties.put(type, aimInaccuracy);
               }
               break;
            case SNEAK:
               if (sneakInaccuracy != null) {
                  jsonProperties.put(type, sneakInaccuracy);
               }
               break;
            case LIE:
               if (lieInaccuracy != null) {
                  jsonProperties.put(type, lieInaccuracy);
               }
               break;
            default:
               jsonProperties.put(type, inaccuracy);
         }
      }

      return new InaccuracyModifier.InaccuracyJsonProperty(jsonProperties);
   }

   @Override
   public CacheValue<Map<InaccuracyType, Float>> initCache(ItemStack gunItem, GunData gunData) {
      Map<InaccuracyType, Float> tmp = Maps.newHashMap();
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      gunData.getInaccuracy().forEach((type, value) -> {
         float inaccuracyAddend = 0.0F;
         GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
         if (fireModeAdjustData != null) {
            if (type == InaccuracyType.AIM) {
               inaccuracyAddend = fireModeAdjustData.getAimInaccuracy();
            } else {
               inaccuracyAddend = fireModeAdjustData.getOtherInaccuracy();
            }
         }

         float inaccuracy = gunData.getInaccuracy(type, inaccuracyAddend);
         tmp.put(type, inaccuracy);
      });
      return new CacheValue<>(tmp);
   }

   @Override
   public void eval(List<Map<InaccuracyType, Modifier>> modifiedValues, CacheValue<Map<InaccuracyType, Float>> cache) {
      Map<InaccuracyType, Float> result = Maps.newHashMap();
      Map<InaccuracyType, List<Modifier>> tmpModified = Maps.newHashMap();

      for (InaccuracyType type : InaccuracyType.values()) {
         List<Modifier> tmp = Lists.newArrayList();

         for (Map<InaccuracyType, Modifier> value : modifiedValues) {
            if (value.get(type) != null) {
               tmp.add(value.get(type));
            }
         }

         tmpModified.put(type, tmp);
      }

      cache.getValue().forEach((typex, valuex) -> {
         double eval = AttachmentPropertyManager.eval(tmpModified.get(typex), cache.getValue().get(typex).floatValue());
         result.put(typex, (float)eval);
      });
      cache.setValue(result);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      return List.of(
         this.buildNormal(gunData, cacheProperty, fireModeAdjustData, InaccuracyType.STAND, "gui.tacz.gun_refit.property_diagrams.hipfire_inaccuracy", 10.0),
         this.buildNormal(gunData, cacheProperty, fireModeAdjustData, InaccuracyType.SNEAK, "gui.tacz.gun_refit.property_diagrams.sneak_inaccuracy", 5.0),
         this.buildNormal(gunData, cacheProperty, fireModeAdjustData, InaccuracyType.LIE, "gui.tacz.gun_refit.property_diagrams.lie_inaccuracy", 5.0),
         this.buildAim(gunData, cacheProperty, fireModeAdjustData)
      );
   }

   @NotNull
   private IAttachmentModifier.DiagramsData buildNormal(
      GunData gunData,
      AttachmentCacheProperty cacheProperty,
      GunFireModeAdjustData fireModeAdjustData,
      InaccuracyType type,
      String titleKey,
      double referenceValue
   ) {
      float inaccuracy = gunData.getInaccuracy(type);
      if (fireModeAdjustData != null) {
         inaccuracy += fireModeAdjustData.getOtherInaccuracy();
      }

      float modifiedValue = (Float)cacheProperty.<Map>getCache(ID).get(type);
      float inaccuracyModifier = modifiedValue - inaccuracy;
      double standInaccuracyPercent = Math.min(inaccuracy / referenceValue, 1.0);
      double inaccuracyModifierPercent = Math.min(inaccuracyModifier / referenceValue, 1.0);
      String positivelyString = String.format("%.2f §c(+%.2f)", modifiedValue, inaccuracyModifier);
      String negativelyString = String.format("%.2f §a(%.2f)", modifiedValue, inaccuracyModifier);
      String defaultString = String.format("%.2f", modifiedValue);
      boolean positivelyBetter = false;
      return new IAttachmentModifier.DiagramsData(
         standInaccuracyPercent, inaccuracyModifierPercent, inaccuracyModifier, titleKey, positivelyString, negativelyString, defaultString, positivelyBetter
      );
   }

   @NotNull
   private IAttachmentModifier.DiagramsData buildAim(GunData gunData, AttachmentCacheProperty cacheProperty, GunFireModeAdjustData fireModeAdjustData) {
      float aimInaccuracy = gunData.getInaccuracy(InaccuracyType.AIM);
      if (fireModeAdjustData != null) {
         aimInaccuracy += fireModeAdjustData.getAimInaccuracy();
      }

      aimInaccuracy = 1.0F - aimInaccuracy;
      float modifiedValue = 1.0F - (Float)cacheProperty.<Map>getCache(ID).get(InaccuracyType.AIM);
      aimInaccuracy = Mth.clamp(aimInaccuracy, 0.0F, 1.0F);
      modifiedValue = Mth.clamp(modifiedValue, 0.0F, 1.0F);
      float inaccuracyModifier = modifiedValue - aimInaccuracy;
      double aimInaccuracyPercent = Mth.clamp(aimInaccuracy, 0.0F, 1.0F);
      double inaccuracyModifierPercent = Mth.clamp(inaccuracyModifier, 0.0F, 1.0F);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.aim_inaccuracy";
      String positivelyString = String.format("%.1f%% §a(+%.1f%%)", modifiedValue * 100.0F, inaccuracyModifier * 100.0F);
      String negativelyString = String.format("%.1f%% §c(%.1f%%)", modifiedValue * 100.0F, inaccuracyModifier * 100.0F);
      String defaultString = String.format("%.1f%%", modifiedValue * 100.0F);
      boolean positivelyBetter = true;
      return new IAttachmentModifier.DiagramsData(
         aimInaccuracyPercent, inaccuracyModifierPercent, inaccuracyModifier, titleKey, positivelyString, negativelyString, defaultString, positivelyBetter
      );
   }

   @Environment(EnvType.CLIENT)
   @Override
   public int getDiagramsDataSize() {
      return 3;
   }

   public static class Data {
      @Nullable
      @SerializedName("inaccuracy")
      private Modifier inaccuracy;
      @Nullable
      @SerializedName("aim_inaccuracy")
      private Modifier aimInaccuracy;
      @Nullable
      @SerializedName("sneak_inaccuracy")
      private Modifier sneakInaccuracy;
      @Nullable
      @SerializedName("lie_inaccuracy")
      private Modifier lieInaccuracy;
      @SerializedName("inaccuracy_addend")
      @Deprecated
      private float adsAddendTime = 0.0F;

      @Nullable
      public Modifier getInaccuracy() {
         return this.inaccuracy;
      }

      @Nullable
      public Modifier getAimInaccuracy() {
         return this.aimInaccuracy;
      }

      @Nullable
      public Modifier getSneakInaccuracy() {
         return this.sneakInaccuracy;
      }

      @Nullable
      public Modifier getLieInaccuracy() {
         return this.lieInaccuracy;
      }

      @Deprecated
      public float getInaccuracyAddendTime() {
         return this.adsAddendTime;
      }
   }

   public static class InaccuracyJsonProperty extends JsonProperty<Map<InaccuracyType, Modifier>> {
      public InaccuracyJsonProperty(Map<InaccuracyType, Modifier> value) {
         super(value);
      }

      @Override
      public void initComponents() {
         this.createEntry(InaccuracyType.STAND, "tooltip.tacz.attachment.inaccuracy.decrease", "tooltip.tacz.attachment.inaccuracy.increase");
         this.createEntry(InaccuracyType.AIM, "tooltip.tacz.attachment.aim_inaccuracy.decrease", "tooltip.tacz.attachment.aim_inaccuracy.increase");
         this.createEntry(InaccuracyType.SNEAK, "tooltip.tacz.attachment.sneak_inaccuracy.decrease", "tooltip.tacz.attachment.sneak_inaccuracy.increase");
         this.createEntry(InaccuracyType.LIE, "tooltip.tacz.attachment.lie_inaccuracy.decrease", "tooltip.tacz.attachment.lie_inaccuracy.increase");
      }

      private void createEntry(InaccuracyType type, String decreaseKey, String increaseKey) {
         Map<InaccuracyType, Modifier> value = this.getValue();
         float inaccuracyAddend = 0.0F;
         if (value != null && value.containsKey(type)) {
            double eval = AttachmentPropertyManager.eval(value.get(type), 5.0);
            inaccuracyAddend = (float)(eval - 5.0);
         }

         if (inaccuracyAddend > 0.0F) {
            this.components.add(Component.translatable(decreaseKey).withStyle(ChatFormatting.RED));
         } else if (inaccuracyAddend < 0.0F) {
            this.components.add(Component.translatable(increaseKey).withStyle(ChatFormatting.GREEN));
         }
      }
   }
}
