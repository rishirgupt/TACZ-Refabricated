package com.tacz.guns.compat.jei.category;

import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class GunSmithTableCategory implements IRecipeCategory<GunSmithTableRecipe> {
   private final Component title;
   private final IDrawableStatic bgDraw;
   private final IDrawable slotDraw;
   private final IDrawable iconDraw;
   private final IRecipeType<GunSmithTableRecipe> type;

   public GunSmithTableCategory(IGuiHelper guiHelper, ItemStack icon, IRecipeType<GunSmithTableRecipe> type, Component title) {
      this.bgDraw = guiHelper.createBlankDrawable(160, 40);
      this.slotDraw = guiHelper.getSlotDrawable();
      this.iconDraw = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);
      this.type = type;
      this.title = title;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, GunSmithTableRecipe recipe, IFocusGroup focuses) {
      ItemStack output = recipe.getOutput();
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.OUTPUT, 3, 12).addItemStack(output)).setBackground(this.slotDraw, -1, -1);
      List<GunSmithTableIngredient> inputs = recipe.getInputs();
      int size = inputs.size();
      if (size < 7) {
         for (int i = 0; i < size; i++) {
            int xOffset = 35 + 20 * i;
            int yOffset = 12;
            ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, xOffset, yOffset).addItemStacks(this.getInput(inputs, i)))
               .setBackground(this.slotDraw, -1, -1);
         }
      } else {
         for (int i = 0; i < 6; i++) {
            int xOffset = 35 + 20 * i;
            int yOffset = 2;
            ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, xOffset, yOffset).addItemStacks(this.getInput(inputs, i)))
               .setBackground(this.slotDraw, -1, -1);
         }

         for (int i = 6; i < size; i++) {
            int xOffset = 35 + 20 * (i - 6);
            int yOffset = 22;
            ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, xOffset, yOffset).addItemStacks(this.getInput(inputs, i)))
               .setBackground(this.slotDraw, -1, -1);
         }
      }
   }

   private List<ItemStack> getInput(List<GunSmithTableIngredient> inputs, int index) {
      if (index < inputs.size()) {
         GunSmithTableIngredient ingredient = inputs.get(index);
         return ingredient.getIngredient().items().map(holder -> new ItemStack(holder, ingredient.getCount())).toList();
      } else {
         return Collections.singletonList(ItemStack.EMPTY);
      }
   }

   public Component getTitle() {
      return this.title;
   }

   public int getWidth() {
      return this.bgDraw.getWidth();
   }

   @Override
   public int getHeight() {
      return this.bgDraw.getHeight();
   }

   public IDrawable getIcon() {
      return this.iconDraw;
   }

   public IRecipeType<GunSmithTableRecipe> getRecipeType() {
      return this.type;
   }
}
