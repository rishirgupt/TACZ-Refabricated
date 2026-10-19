package cn.sh1rocu.tacz.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Items and blocks must know their registry key when their properties are built (26.x). The mod's classes build
 * their own properties inside their constructors, so the key being registered is held here for the duration of
 * the constructor and picked up by the properties constructors (see the properties mixins).
 */
public final class RegistrationContext {
   private static final ThreadLocal<ResourceKey<Item>> ITEM = new ThreadLocal<>();
   private static final ThreadLocal<ResourceKey<Block>> BLOCK = new ThreadLocal<>();

   private RegistrationContext() {
   }

   public static ResourceKey<Item> item() {
      return ITEM.get();
   }

   public static ResourceKey<Block> block() {
      return BLOCK.get();
   }

   public static <T> T item(String name, java.util.function.Supplier<T> factory) {
      ITEM.set(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("tacz", name)));
      try {
         return factory.get();
      } finally {
         ITEM.remove();
      }
   }

   public static <T> T block(String name, java.util.function.Supplier<T> factory) {
      BLOCK.set(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("tacz", name)));
      try {
         return factory.get();
      } finally {
         BLOCK.remove();
      }
   }
}
