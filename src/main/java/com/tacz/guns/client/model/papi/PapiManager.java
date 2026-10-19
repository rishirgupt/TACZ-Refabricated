package com.tacz.guns.client.model.papi;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class PapiManager {
   private static final Map<String, Function<ItemStack, String>> PAPI = Maps.newHashMap();

   public static void addPapi(String textKey, Function<ItemStack, String> function) {
      textKey = "%" + textKey + "%";
      PAPI.put(textKey, function);
   }

   public static String getTextShow(String textKey, ItemStack stack) {
      String text = net.minecraft.locale.Language.getInstance().getOrDefault(textKey);

      for (Entry<String, Function<ItemStack, String>> entry : PAPI.entrySet()) {
         String placeholder = entry.getKey();
         String data = entry.getValue().apply(stack);
         text = text.replace(placeholder, data);
      }

      return text;
   }

   static {
      addPapi("player_name", new PlayerNamePapi());
      addPapi("ammo_count", new AmmoCountPapi());
   }
}
