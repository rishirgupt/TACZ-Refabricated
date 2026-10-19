package com.tacz.guns.client.render26;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/** Items that are drawn by an animated geo item renderer (first-person view, animation state machine). */
public final class GeoItemRegistry {
   private static final Map<Item, Object> RENDERERS = new HashMap<>();

   private GeoItemRegistry() {
   }

   public static void register(Item item, Object renderer) {
      RENDERERS.put(item, renderer);
   }

   @Nullable
   public static Object get(Item item) {
      return RENDERERS.get(item);
   }
}
