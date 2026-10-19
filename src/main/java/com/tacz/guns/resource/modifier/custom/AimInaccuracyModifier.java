package com.tacz.guns.resource.modifier.custom;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

@Deprecated
public class AimInaccuracyModifier implements IAttachmentModifier<Map<InaccuracyType, Modifier>, Map<InaccuracyType, Float>> {
   public static final String ID = GunProperties.AIM_INACCURACY.name();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JsonProperty<Map<InaccuracyType, Modifier>> readJson(String json) {
      Map<InaccuracyType, Modifier> jsonProperties = Maps.newHashMap();
      return new AimInaccuracyModifier.AimInaccuracyJsonProperty(jsonProperties);
   }

   @Override
   public CacheValue<Map<InaccuracyType, Float>> initCache(ItemStack gunItem, GunData gunData) {
      Map<InaccuracyType, Float> tmp = Maps.newHashMap();
      return new CacheValue<>(tmp);
   }

   @Override
   public void eval(List<Map<InaccuracyType, Modifier>> modifiedValues, CacheValue<Map<InaccuracyType, Float>> cache) {
   }

   @Environment(EnvType.CLIENT)
   @Override
   public List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      return List.of();
   }

   @Environment(EnvType.CLIENT)
   @Override
   public int getDiagramsDataSize() {
      return 0;
   }

   public static class AimInaccuracyJsonProperty extends JsonProperty<Map<InaccuracyType, Modifier>> {
      public AimInaccuracyJsonProperty(Map<InaccuracyType, Modifier> value) {
         super(value);
      }

      @Override
      public void initComponents() {
      }
   }

   public static class Data {
      @Nullable
      @SerializedName("aim_inaccuracy")
      private Modifier aimInaccuracy;

      @Nullable
      public Modifier getAimInaccuracy() {
         return this.aimInaccuracy;
      }
   }
}
