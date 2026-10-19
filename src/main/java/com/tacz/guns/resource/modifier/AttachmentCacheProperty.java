package com.tacz.guns.resource.modifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.Experimental;

public class AttachmentCacheProperty {
   private final Map<String, CacheValue> cacheValues = Maps.newHashMap();
   private final Map<String, List<?>> cacheModifiers = Maps.newHashMap();

   public void eval(ItemStack gunItem, GunData gunData) {
      Map<String, IAttachmentModifier<?, ?>> modifiers = AttachmentPropertyManager.getModifiers();
      modifiers.forEach((id, value) -> {
         this.cacheValues.put(id, value.initCache(gunItem, gunData));
         this.cacheModifiers.put(id, Lists.newArrayList());
      });
      AttachmentDataUtils.getAllAttachmentData(gunItem, gunData, data -> data.getModifier().forEach((id, value) -> {
         List objects = this.cacheModifiers.get(id);
         objects.add(value.getValue());
      }));
      this.cacheValues.forEach((id, value) -> {
         List cacheModifier = this.cacheModifiers.get(id);
         if (cacheModifier != null && !cacheModifier.isEmpty()) {
            ((IAttachmentModifier)modifiers.get(id)).eval(cacheModifier, value);
         }
      });
      this.cacheModifiers.clear();
   }

   public <T> T getCache(String id) {
      return (T)this.cacheValues.get(id).getValue();
   }

   @Experimental
   public <T> T getCache(GunProperty<T> key) {
      return key.type().cast(this.cacheValues.get(key.name()).getValue());
   }

   @Experimental
   public <T> void setCache(GunProperty<T> key, T value) {
      if (!key.type().isInstance(value)) {
         throw new IllegalArgumentException(
            "Gun cache type mismatch, needs %s, found %s".formatted(key.type().getSimpleName(), value.getClass().getSimpleName())
         );
      }

      this.cacheValues.get(key.name()).setValue(value);
   }
}
