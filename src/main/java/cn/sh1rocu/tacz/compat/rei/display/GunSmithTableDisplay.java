package cn.sh1rocu.tacz.compat.rei.display;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import java.util.Collections;
import java.util.Optional;
import java.util.Map.Entry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;

public class GunSmithTableDisplay extends BasicDisplay {
   private final GunSmithTableRecipe recipe;
   private final Entry<Identifier, CategoryIdentifier<GunSmithTableDisplay>> entry;

   public GunSmithTableDisplay(GunSmithTableRecipe recipe, Entry<Identifier, CategoryIdentifier<GunSmithTableDisplay>> entry) {
      super(
         EntryIngredients.ofIngredients(recipe.getIngredients()),
         Collections.singletonList(EntryIngredients.of(recipe.getOutput())),
         Optional.ofNullable(entry.getKey())
      );
      this.recipe = recipe;
      this.entry = entry;
   }

   public GunSmithTableRecipe getRecipe() {
      return this.recipe;
   }

   public CategoryIdentifier<?> getCategoryIdentifier() {
      return this.entry.getValue();
   }
}
