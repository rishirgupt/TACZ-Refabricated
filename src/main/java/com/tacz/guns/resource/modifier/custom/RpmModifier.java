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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class RpmModifier implements IAttachmentModifier<Modifier, Integer> {
   public static final String ID = GunProperties.ROUNDS_PER_MINUTE.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      RpmModifier.Data data = (RpmModifier.Data)CommonAssetsManager.GSON.fromJson(json, RpmModifier.Data.class);
      return new RpmModifier.RpmJsonProperty(data.getRpm());
   }

   @Override
   public CacheValue<Integer> initCache(ItemStack gunItem, GunData gunData) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      int roundsPerMinute = gunData.getRoundsPerMinute(fireMode);
      return new CacheValue<>(roundsPerMinute);
   }

   @Override
   public void eval(List<Modifier> modifiers, CacheValue<Integer> cache) {
      double eval = AttachmentPropertyManager.eval(modifiers, cache.getValue().intValue());
      cache.setValue((int)Math.round(eval));
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      IGun iGun = Objects.requireNonNull(IGun.getIGunOrNull(gunItem));
      FireMode fireMode = iGun.getFireMode(gunItem);
      int rpm = gunData.getRoundsPerMinute(fireMode);
      int modifiedValue = cacheProperty.<Integer>getCache(ID);
      int rpmModifier = modifiedValue - rpm;
      double rpmPercent = Math.min(rpm / 1200.0, 1.0);
      double rpmModifierPercent = Math.min(rpmModifier / 1200.0, 1.0);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.rpm";
      String positivelyString = String.format("%drpm §a(+%d)", modifiedValue, rpmModifier);
      String negativelyString = String.format("%drpm §c(%d)", modifiedValue, rpmModifier);
      String defaultString = String.format("%drpm", modifiedValue);
      boolean positivelyBetter = true;
      IAttachmentModifier.DiagramsData diagramsData = new IAttachmentModifier.DiagramsData(
         rpmPercent, rpmModifierPercent, rpmModifier, titleKey, positivelyString, negativelyString, defaultString, positivelyBetter
      );
      return Collections.singletonList(diagramsData);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public int getDiagramsDataSize() {
      return 1;
   }

   public static class Data {
      @SerializedName("rpm")
      @Nullable
      private Modifier rpm = null;

      @Nullable
      public Modifier getRpm() {
         return this.rpm;
      }
   }

   public static class RpmJsonProperty extends JsonProperty<Modifier> {
      public RpmJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier value = this.getValue();
         if (value != null) {
            double eval = AttachmentPropertyManager.eval(value, 300.0);
            int rpm = (int)Math.round(eval);
            if (rpm > 300) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.rpm.increase").withStyle(ChatFormatting.GREEN));
            } else if (rpm < 300) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.rpm.decrease").withStyle(ChatFormatting.RED));
            }
         }
      }
   }
}
