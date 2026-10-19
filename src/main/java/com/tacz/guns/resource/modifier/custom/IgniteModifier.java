package com.tacz.guns.resource.modifier.custom;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class IgniteModifier implements IAttachmentModifier<Ignite, Ignite> {
   public static final String ID = GunProperties.IGNITE.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Ignite> readJson(String json) {
      IgniteModifier.Data data = (IgniteModifier.Data)CommonAssetsManager.GSON.fromJson(json, IgniteModifier.Data.class);
      return new IgniteModifier.IgniteJsonProperty(data.getIgnite());
   }

   @Override
   public CacheValue<Ignite> initCache(ItemStack gunItem, GunData gunData) {
      Ignite ignite = gunData.getBulletData().getIgnite();
      return new CacheValue<>(ignite);
   }

   @Override
   public void eval(List<Ignite> modifiedValues, CacheValue<Ignite> cache) {
      Ignite cacheValue = cache.getValue();
      List<Boolean> igniteEntityValues = Lists.newArrayList();
      igniteEntityValues.add(cacheValue.isIgniteEntity());
      List<Boolean> igniteBlockValues = Lists.newArrayList();
      igniteBlockValues.add(cacheValue.isIgniteBlock());
      modifiedValues.forEach(v -> {
         igniteEntityValues.add(v.isIgniteEntity());
         igniteBlockValues.add(v.isIgniteBlock());
      });
      boolean igniteEntity = AttachmentPropertyManager.eval(igniteEntityValues, false);
      boolean igniteBlock = AttachmentPropertyManager.eval(igniteBlockValues, false);
      cache.setValue(new Ignite(igniteEntity, igniteBlock));
   }

   private static class Data {
      @SerializedName("ignite")
      private Ignite ignite = new Ignite(false);

      @Nullable
      public Ignite getIgnite() {
         return this.ignite;
      }
   }

   public static class IgniteJsonProperty extends JsonProperty<Ignite> {
      public IgniteJsonProperty(Ignite value) {
         super(value);
      }

      @Override
      public void initComponents() {
         Ignite value = this.getValue();
         if (value != null) {
            if (value.isIgniteEntity()) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.ignite.entity").withStyle(ChatFormatting.GREEN));
            }

            if (value.isIgniteBlock()) {
               this.components.add(Component.translatable("tooltip.tacz.attachment.ignite.block").withStyle(ChatFormatting.GREEN));
            }
         }
      }
   }
}
