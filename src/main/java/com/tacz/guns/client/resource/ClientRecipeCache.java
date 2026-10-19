package com.tacz.guns.client.resource;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Gun smith table recipes as last sent by the server. */
public final class ClientRecipeCache {
   private static Map<Identifier, GunSmithTableRecipe> recipes = Collections.emptyMap();

   public record Entry(Identifier id, GunSmithTableRecipe value) {
   }

   private ClientRecipeCache() {
   }

   public static java.util.List<Entry> entries() {
      java.util.List<Entry> list = new java.util.ArrayList<>();
      recipes.forEach((id, recipe) -> list.add(new Entry(id, recipe)));
      return list;
   }

   @Nullable
   public static Entry entry(Identifier id) {
      GunSmithTableRecipe recipe = recipes.get(id);
      return recipe == null ? null : new Entry(id, recipe);
   }

   public static void set(Map<Identifier, GunSmithTableRecipe> synced, @Nullable ClientLevel level) {
      Map<Identifier, GunSmithTableRecipe> copy = new LinkedHashMap<>(synced);
      if (level != null) {
         copy.values().forEach(recipe -> recipe.init(level.registryAccess()));
      }

      recipes = copy;
   }

   public static Map<Identifier, GunSmithTableRecipe> all() {
      return recipes;
   }

   @Nullable
   public static GunSmithTableRecipe get(Identifier id) {
      return recipes.get(id);
   }
}
