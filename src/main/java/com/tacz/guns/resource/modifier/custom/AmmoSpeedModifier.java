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

public class AmmoSpeedModifier implements IAttachmentModifier<Modifier, Float> {
   public static final String ID = GunProperties.AMMO_SPEED.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      AmmoSpeedModifier.Data data = (AmmoSpeedModifier.Data)CommonAssetsManager.GSON.fromJson(json, AmmoSpeedModifier.Data.class);
      return new AmmoSpeedModifier.BulletSpeedJsonProperty(data.getAmmoSpeed());
   }

   @Override
   public CacheValue<Float> initCache(ItemStack gunItem, GunData gunData) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      float speed = gunData.getBulletData().getSpeed();
      if (fireModeAdjustData != null) {
         speed += fireModeAdjustData.getSpeed();
      }

      return new CacheValue<>(speed);
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
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      float ammoSpeed = gunData.getBulletData().getSpeed();
      if (fireModeAdjustData != null) {
         ammoSpeed += fireModeAdjustData.getSpeed();
      }

      float modifiedAmmoSpeed = cacheProperty.<Float>getCache(ID);
      float ammoSpeedModifier = modifiedAmmoSpeed - ammoSpeed;
      double ammoSpeedPercent = Math.min(ammoSpeed / 600.0, 1.0);
      double ammoSpeedModifierPercent = Math.min(ammoSpeedModifier / 600.0, 1.0);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.ammo_speed";
      String positivelyString = String.format("%dm/s §a(+%d)", Math.round(modifiedAmmoSpeed), Math.round(ammoSpeedModifier));
      String negativelyString = String.format("%dm/s §c(%d)", Math.round(modifiedAmmoSpeed), Math.round(ammoSpeedModifier));
      String defaultString = String.format("%dm/s", Math.round(modifiedAmmoSpeed));
      boolean positivelyBetter = true;
      IAttachmentModifier.DiagramsData diagramsData = new IAttachmentModifier.DiagramsData(
         ammoSpeedPercent, ammoSpeedModifierPercent, ammoSpeedModifier, titleKey, positivelyString, negativelyString, defaultString, positivelyBetter
      );
      return Collections.singletonList(diagramsData);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public int getDiagramsDataSize() {
      return 1;
   }

   public static class BulletSpeedJsonProperty extends JsonProperty<Modifier> {
      public BulletSpeedJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier ammoSpeed = this.getValue();
         if (ammoSpeed != null) {
            double eval = AttachmentPropertyManager.eval(ammoSpeed, 300.0);
            if (eval > 300.0) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.ammo_speed.increase").withStyle(ChatFormatting.GREEN));
            } else if (eval < 300.0) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.ammo_speed.decrease").withStyle(ChatFormatting.RED));
            }
         }
      }
   }

   public static class Data {
      @SerializedName("ammo_speed")
      @Nullable
      private Modifier ammoSpeed = null;

      @Nullable
      public Modifier getAmmoSpeed() {
         return this.ammoSpeed;
      }
   }
}
