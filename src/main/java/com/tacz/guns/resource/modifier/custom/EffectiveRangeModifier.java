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
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class EffectiveRangeModifier implements IAttachmentModifier<Modifier, Float> {
   public static final String ID = GunProperties.EFFECTIVE_RANGE.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      EffectiveRangeModifier.Data data = (EffectiveRangeModifier.Data)CommonAssetsManager.GSON.fromJson(json, EffectiveRangeModifier.Data.class);
      return new EffectiveRangeModifier.EffectiveRangeJsonProperty(data.getEffectiveRange());
   }

   @Override
   public CacheValue<Float> initCache(ItemStack gunItem, GunData gunData) {
      LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust = null;
      if (gunData.getBulletData().getExtraDamage() != null) {
         damageAdjust = gunData.getBulletData().getExtraDamage().getDamageAdjust();
      }

      float effectiveRange;
      if (damageAdjust != null) {
         effectiveRange = damageAdjust.get(0).getDistance();
      } else {
         effectiveRange = 2.1474836E9F;
      }

      return new CacheValue<>(effectiveRange);
   }

   @Override
   public void eval(List<Modifier> modifiers, CacheValue<Float> cache) {
      double eval = AttachmentPropertyManager.eval(modifiers, cache.getValue().floatValue());
      cache.setValue((float)eval);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      float modifiedDistance = cacheProperty.<Float>getCache(ID);
      LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust = null;
      if (gunData.getBulletData().getExtraDamage() != null) {
         damageAdjust = gunData.getBulletData().getExtraDamage().getDamageAdjust();
      }

      float effectiveRange;
      if (damageAdjust != null) {
         effectiveRange = damageAdjust.get(0).getDistance();
      } else {
         effectiveRange = 0.0F;
      }

      modifiedDistance = Math.min(modifiedDistance, 1024.0F);
      effectiveRange = Math.min(effectiveRange, 1024.0F);
      float modifier = modifiedDistance - effectiveRange;
      double percent = Math.min(effectiveRange / 100.0, 1.0);
      double modifierPercent = Math.min(modifier / 100.0, 1.0);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.effective_range";
      String positivelyString = String.format("%.1fm §a(+%.1f)", effectiveRange, modifier);
      String negativelyString = String.format("%.1fm §c(%.1f)", effectiveRange, modifier);
      String defaultString = String.format("%.1fm", effectiveRange);
      boolean positivelyBetter = true;
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
      @SerializedName("effective_range")
      @Nullable
      private Modifier effectiveRange = null;

      @Nullable
      public Modifier getEffectiveRange() {
         return this.effectiveRange;
      }
   }

   public static class EffectiveRangeJsonProperty extends JsonProperty<Modifier> {
      public EffectiveRangeJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier value = this.getValue();
         if (value != null) {
            double eval = AttachmentPropertyManager.eval(value, 25.0);
            if (eval > 25.0) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.effective_range.increase").withStyle(ChatFormatting.GREEN));
            } else if (eval < 25.0) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.effective_range.decrease").withStyle(ChatFormatting.RED));
            }
         }
      }
   }
}
