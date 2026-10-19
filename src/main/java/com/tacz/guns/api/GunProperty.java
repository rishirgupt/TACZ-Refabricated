package com.tacz.guns.api;

import com.google.common.reflect.TypeToken;
import net.minecraft.util.Util;

public record GunProperty<T>(String name, Class<T> type) {
   public static <T> GunProperty<T> of(String name, Class<T> type) {
      return (GunProperty<T>)Util.make(new GunProperty<>(name, type), property -> GunProperties.ALL.put(name, property));
   }

   public static <T> GunProperty<T> of(String name, TypeToken<T> type) {
      return (GunProperty<T>)Util.make(new GunProperty(name, type.getRawType()), property -> GunProperties.ALL.put(name, property));
   }
}
