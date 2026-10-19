package com.tacz.guns.init;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipe {
   public static RecipeSerializer<GunSmithTableRecipe> GUN_SMITH_TABLE_RECIPE_SERIALIZER = registerSerializer(
      "gun_smith_table_crafting", new RecipeSerializer<>(GunSmithTableRecipe.CODEC, GunSmithTableRecipe.STREAM_CODEC)
   );
   public static RecipeType<GunSmithTableRecipe> GUN_SMITH_TABLE_CRAFTING = registerRecipe("gun_smith_table_crafting", new RecipeType<GunSmithTableRecipe>() {
      @Override
      public String toString() {
         return "tacz:gun_smith_table_crafting";
      }
   });

   public static void init() {
   }

   public static java.util.List<net.minecraft.world.item.crafting.RecipeHolder<GunSmithTableRecipe>> allRecipes(net.minecraft.world.item.crafting.RecipeManager manager) {
      java.util.List<net.minecraft.world.item.crafting.RecipeHolder<GunSmithTableRecipe>> list = new java.util.ArrayList<>();
      for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : manager.getRecipes()) {
         if (holder.value() instanceof GunSmithTableRecipe recipe) {
            list.add(new net.minecraft.world.item.crafting.RecipeHolder<>(holder.id(), recipe));
         }
      }

      return list;
   }

   private static <T extends Recipe<?>> RecipeSerializer<T> registerSerializer(String name, RecipeSerializer<T> serializer) {
      return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("tacz", name), serializer);
   }

   private static <T extends Recipe<?>> RecipeType<T> registerRecipe(String name, RecipeType<T> type) {
      return Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath("tacz", name), type);
   }
}
