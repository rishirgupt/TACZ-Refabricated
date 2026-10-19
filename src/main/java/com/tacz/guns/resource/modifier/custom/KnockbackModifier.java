package com.tacz.guns.resource.modifier.custom;

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
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunFireModeAdjustData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class KnockbackModifier implements IAttachmentModifier<Modifier, Float> {
   public static final String ID = GunProperties.KNOCKBACK.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      KnockbackModifier.Data data = (KnockbackModifier.Data)CommonAssetsManager.GSON.fromJson(json, KnockbackModifier.Data.class);
      return new KnockbackModifier.KnockbackJsonProperty(data.getKnockback());
   }

   @Override
   public CacheValue<Float> initCache(ItemStack gunItem, GunData gunData) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      float finalBase = bulletData.getKnockback();
      finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getKnockback() : finalBase;
      return new CacheValue<>(finalBase);
   }

   @Override
   public void eval(List<Modifier> modifiers, CacheValue<Float> cache) {
      double eval = AttachmentPropertyManager.eval(modifiers, cache.getValue().floatValue());
      cache.setValue((float)eval);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      float finalBase = bulletData.getKnockback();
      finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getKnockback() : finalBase;
      float modifiedValue = cacheProperty.<Float>getCache(ID);
      float modifier = modifiedValue - finalBase;
      double percent = Mth.clamp(finalBase, 0.0F, 1.0F);
      double modifierPercent = Mth.clamp(modifier, 0.0F, 1.0F);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.knockback";
      String positivelyString = String.format("%.2f §a(+%.2f)", modifiedValue, modifier);
      String negativelyString = String.format("%.2f §c(%.2f)", modifiedValue, modifier);
      String defaultString = String.format("%.2f", modifiedValue);
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
      @SerializedName("knockback")
      @Nullable
      private Modifier knockback = null;

      @Nullable
      public Modifier getKnockback() {
         return this.knockback;
      }
   }

   public static class KnockbackJsonProperty extends JsonProperty<Modifier> {
      public KnockbackJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier value = this.getValue();
         if (value != null) {
            double eval = AttachmentPropertyManager.eval(value, 0.2);
            if (eval > 0.2) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.knockback.increase").withStyle(ChatFormatting.GREEN));
            } else if (eval < 0.2) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.knockback.decrease").withStyle(ChatFormatting.RED));
            }
         }
      }
   }
}
