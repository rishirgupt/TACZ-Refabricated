package com.tacz.guns.resource.modifier.custom;

import com.google.common.collect.Lists;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class DamageModifier implements IAttachmentModifier<Modifier, LinkedList<ExtraDamage.DistanceDamagePair>> {
   public static final String ID = GunProperties.DAMAGE.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      DamageModifier.Data data = (DamageModifier.Data)CommonAssetsManager.GSON.fromJson(json, DamageModifier.Data.class);
      return new DamageModifier.DamageJsonProperty(data.getDamage());
   }

   @Override
   public CacheValue<LinkedList<ExtraDamage.DistanceDamagePair>> initCache(ItemStack gunItem, GunData gunData) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      float rawDamage = bulletData.getDamageAmount();
      ExtraDamage extraDamage = bulletData.getExtraDamage();
      float fireAdjustDamageAmount = fireModeAdjustData != null ? fireModeAdjustData.getDamageAmount() : 0.0F;
      LinkedList<ExtraDamage.DistanceDamagePair> cacheValue = Lists.newLinkedList();
      if (extraDamage != null && extraDamage.getDamageAdjust() != null) {
         for (ExtraDamage.DistanceDamagePair pair : extraDamage.getDamageAdjust()) {
            float finalBaseDamage = pair.getDamage() + fireAdjustDamageAmount;
            cacheValue.add(new ExtraDamage.DistanceDamagePair(pair.getDistance(), (float)(finalBaseDamage * (Double)SyncConfig.DAMAGE_BASE_MULTIPLIER.get())));
         }
      } else {
         float finalBaseDamage = rawDamage + fireAdjustDamageAmount;
         cacheValue.add(new ExtraDamage.DistanceDamagePair(2.1474836E9F, (float)(finalBaseDamage * (Double)SyncConfig.DAMAGE_BASE_MULTIPLIER.get())));
      }

      return new CacheValue<>(cacheValue);
   }

   @Override
   public void eval(List<Modifier> modifiers, CacheValue<LinkedList<ExtraDamage.DistanceDamagePair>> cache) {
      LinkedList<ExtraDamage.DistanceDamagePair> cacheValue = cache.getValue();
      LinkedList<ExtraDamage.DistanceDamagePair> modifiedValue = new LinkedList<>();

      for (ExtraDamage.DistanceDamagePair pair : cacheValue) {
         float base = pair.getDamage();
         float eval = (float)AttachmentPropertyManager.eval(modifiers, base);
         modifiedValue.add(new ExtraDamage.DistanceDamagePair(pair.getDistance(), eval));
      }

      cache.setValue(modifiedValue);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      LinkedList<ExtraDamage.DistanceDamagePair> damagePairModifier = cacheProperty.getCache(ID);
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      float rawDamage = bulletData.getDamageAmount();
      ExtraDamage extraDamage = bulletData.getExtraDamage();
      float finalBase = fireModeAdjustData != null ? fireModeAdjustData.getDamageAmount() : 0.0F;
      if (extraDamage != null && extraDamage.getDamageAdjust() != null) {
         finalBase += extraDamage.getDamageAdjust().get(0).getDamage();
      } else {
         finalBase += rawDamage;
      }

      finalBase = (float)(finalBase * (Double)SyncConfig.DAMAGE_BASE_MULTIPLIER.get());
      float modifiedValue = damagePairModifier.get(0).getDamage();
      float modifier = modifiedValue - finalBase;
      double percent = Math.min(finalBase / 50.0, 1.0);
      double modifierPercent = Math.min(modifier / 50.0, 1.0);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.damage";
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

   public static class DamageJsonProperty extends JsonProperty<Modifier> {
      public DamageJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier value = this.getValue();
         if (value != null) {
            double eval = AttachmentPropertyManager.eval(value, 9.0);
            int damage = (int)Math.round(eval);
            if (damage > 9) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.damage.increase").withStyle(ChatFormatting.GREEN));
            } else if (damage < 9) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.damage.decrease").withStyle(ChatFormatting.RED));
            }
         }
      }
   }

   public static class Data {
      @SerializedName("damage")
      @Nullable
      private Modifier damage = null;

      @Nullable
      public Modifier getDamage() {
         return this.damage;
      }
   }
}
