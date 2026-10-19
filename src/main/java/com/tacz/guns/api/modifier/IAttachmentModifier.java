package com.tacz.guns.api.modifier;

import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

public interface IAttachmentModifier<T, K> {
   String getId();

   default String getOptionalFields() {
      return "";
   }

   JsonProperty<T> readJson(String var1);

   CacheValue<K> initCache(ItemStack var1, GunData var2);

   void eval(List<T> var1, CacheValue<K> var2);

   @Environment(EnvType.CLIENT)
   default List<IAttachmentModifier.DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
      return Collections.emptyList();
   }

   @Environment(EnvType.CLIENT)
   default int getDiagramsDataSize() {
      return 0;
   }

   @Environment(EnvType.CLIENT)
   record DiagramsData(
      double defaultPercent,
      double modifierPercent,
      Number modifier,
      String titleKey,
      String positivelyString,
      String negativeString,
      String defaultString,
      boolean positivelyBetter
   ) {
   }
}
