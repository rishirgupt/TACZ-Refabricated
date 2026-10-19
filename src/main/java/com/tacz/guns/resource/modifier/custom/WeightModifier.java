package com.tacz.guns.resource.modifier.custom;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class WeightModifier implements IAttachmentModifier<Modifier, Float> {
   public static final String ID = GunProperties.WEIGHT.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      WeightModifier.Data data = (WeightModifier.Data)CommonAssetsManager.GSON.fromJson(json, WeightModifier.Data.class);
      Modifier weightModifier = data.getWeightModifier();
      if (weightModifier == null) {
         weightModifier = new Modifier();
         weightModifier.setAddend(data.getWeightAddend());
      }

      return new WeightModifier.WeightJsonProperty(weightModifier);
   }

   @Override
   public CacheValue<Float> initCache(ItemStack gunItem, GunData gunData) {
      return new CacheValue<>(gunData.getWeight());
   }

   @Override
   public void eval(List<Modifier> modifiers, CacheValue<Float> cache) {
      double eval = AttachmentPropertyManager.eval(modifiers, cache.getValue().floatValue());
      cache.setValue((float)eval);
   }

   @Override
   public String getOptionalFields() {
      return "weight";
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      float weight = gunData.getWeight();
      float modifiedValue = cacheProperty.<Float>getCache(ID);
      float modifier = modifiedValue - weight;
      double percent = Math.min(weight / 20.0, 1.0);
      double modifierPercent = Math.min(modifier / 20.0, 1.0);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.weight";
      String positivelyString = String.format("%.2fkg §c(+%.2f)", modifiedValue, modifier);
      String negativelyString = String.format("%.2fkg §a(%.2f)", modifiedValue, modifier);
      String defaultString = String.format("%.2fkg", modifiedValue);
      boolean positivelyBetter = false;
      IAttachmentModifier.DiagramsData diagramsData = new IAttachmentModifier.DiagramsData(
         percent, modifierPercent, modifier, titleKey, positivelyString, negativelyString, defaultString, positivelyBetter
      );
      return Collections.singletonList(diagramsData);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public int getDiagramsDataSize() {
      return 1;
   }

   public static class Data {
      @Nullable
      @SerializedName("weight_modifier")
      private Modifier weightModifier;
      @SerializedName("weight")
      @Deprecated
      private float weightAddend = 0.0F;

      @Nullable
      public Modifier getWeightModifier() {
         return this.weightModifier;
      }

      @Deprecated
      public float getWeightAddend() {
         return this.weightAddend;
      }
   }

   public static class WeightJsonProperty extends JsonProperty<Modifier> {
      public WeightJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier value = this.getValue();
         float adsAddendTime = 0.0F;
         if (value != null) {
            double eval = AttachmentPropertyManager.eval(value, 0.2);
            adsAddendTime = (float)(eval - 0.2);
         }

         if (adsAddendTime > 0.0F) {
            this.components.add(Component.translatable("tooltip.tacz.attachment.weight.increase").withStyle(ChatFormatting.RED));
         } else if (adsAddendTime < 0.0F) {
            this.components.add(Component.translatable("tooltip.tacz.attachment.weight.decrease").withStyle(ChatFormatting.GREEN));
         }
      }
   }
}
