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

public class PierceModifier implements IAttachmentModifier<Modifier, Integer> {
   public static final String ID = GunProperties.PIERCE.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Modifier> readJson(String json) {
      PierceModifier.Data data = (PierceModifier.Data)CommonAssetsManager.GSON.fromJson(json, PierceModifier.Data.class);
      return new PierceModifier.PierceJsonProperty(data.getPierce());
   }

   @Override
   public CacheValue<Integer> initCache(ItemStack gunItem, GunData gunData) {
      int pierce = gunData.getBulletData().getPierce();
      return new CacheValue<>(pierce);
   }

   @Override
   public void eval(List<Modifier> modifiers, CacheValue<Integer> cache) {
      double eval = AttachmentPropertyManager.eval(modifiers, cache.getValue().intValue());
      cache.setValue((int)Math.round(eval));
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      int pierce = gunData.getBulletData().getPierce();
      int modifiedValue = cacheProperty.<Integer>getCache(ID);
      int pierceModifier = modifiedValue - pierce;
      double piercePercent = Math.min(pierce / 5.0, 1.0);
      double pierceModifierPercent = Math.min(pierceModifier / 5.0, 1.0);
      String titleKey = "gui.tacz.gun_refit.property_diagrams.pierce";
      String positivelyString = String.format("%d §a(+%d)", modifiedValue, pierceModifier);
      String negativelyString = String.format("%d §c(%d)", modifiedValue, pierceModifier);
      String defaultString = String.format("%d", modifiedValue);
      boolean positivelyBetter = true;
      IAttachmentModifier.DiagramsData diagramsData = new IAttachmentModifier.DiagramsData(
         piercePercent, pierceModifierPercent, pierceModifier, titleKey, positivelyString, negativelyString, defaultString, positivelyBetter
      );
      return Collections.singletonList(diagramsData);
   }

   @Environment(EnvType.CLIENT)
   @Override
   public int getDiagramsDataSize() {
      return 1;
   }

   public static class Data {
      @SerializedName("pierce")
      @Nullable
      private Modifier pierce = null;

      @Nullable
      public Modifier getPierce() {
         return this.pierce;
      }
   }

   public static class PierceJsonProperty extends JsonProperty<Modifier> {
      public PierceJsonProperty(Modifier value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Modifier pierce = this.getValue();
         if (pierce != null) {
            long eval = Math.round(AttachmentPropertyManager.eval(pierce, 5.0));
            eval = Math.max(eval, 1L);
            if (eval > 5L) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.pierce.increase").withStyle(ChatFormatting.GREEN));
            } else if (eval < 5L) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.pierce.decrease").withStyle(ChatFormatting.RED));
            }
         }
      }
   }
}
