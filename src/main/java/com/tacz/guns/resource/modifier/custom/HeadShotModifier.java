package com.tacz.guns.resource.modifier.custom;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
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

public class HeadShotModifier implements IAttachmentModifier<Modifier, Float> {
   public static final String ID = GunProperties.HEADSHOT_MULTIPLIER.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      HeadShotModifier.Data data = (HeadShotModifier.Data)CommonAssetsManager.GSON.fromJson(json, HeadShotModifier.Data.class);
      return new HeadShotModifier.HeadShotJsonProperty(data.getHeadShot());
   }

   @Override
   public CacheValue<Float> initCache(ItemStack gunItem, GunData gunData) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      ExtraDamage extraDamage = bulletData.getExtraDamage();
      float finalBase = extraDamage != null ? extraDamage.getHeadShotMultiplier() : 0.0F;
      finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getHeadShotMultiplier() : finalBase;
      finalBase = (float)(finalBase * (Double)SyncConfig.HEAD_SHOT_BASE_MULTIPLIER.get());
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
      ExtraDamage extraDamage = bulletData.getExtraDamage();
      float finalBase = extraDamage != null ? extraDamage.getHeadShotMultiplier() : 0.0F;
      finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getHeadShotMultiplier() : finalBase;
      finalBase = (float)(finalBase * (Double)SyncConfig.HEAD_SHOT_BASE_MULTIPLIER.get());
      float modifiedValue = cacheProperty.<Float>getCache(ID);
      float modifier = modifiedValue - finalBase;
      double percent = Mth.clamp(finalBase / 5.0, 0.0, 1.0);
      double modifierPercent = Mth.clamp(modifier / 5.0, 0.0, 1.0);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.head_shot";
      String positivelyString = String.format("x%.1f §a(+%.1f)", modifiedValue, modifier);
      String negativelyString = String.format("x%.1f §c(%.1f)", modifiedValue, modifier);
      String defaultString = String.format("x%.1f", modifiedValue);
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
      @SerializedName("head_shot")
      @Nullable
      private Modifier headShot = null;

      @Nullable
      public Modifier getHeadShot() {
         return this.headShot;
      }
   }

   public static class HeadShotJsonProperty extends JsonProperty<Modifier> {
      public HeadShotJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier value = this.getValue();
         if (value != null) {
            double eval = AttachmentPropertyManager.eval(value, 2.0);
            if (eval > 2.0) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.head_shot.increase").withStyle(ChatFormatting.GREEN));
            } else if (eval < 2.0) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.head_shot.decrease").withStyle(ChatFormatting.RED));
            }
         }
      }
   }
}
