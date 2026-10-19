package com.tacz.guns.crafting;

import com.mojang.serialization.MapCodec;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.core.HolderLookup.Provider;

public class GunSmithTableRecipe implements Recipe<SmithingRecipeInput> {
   public static final MapCodec<GunSmithTableRecipe> CODEC = TableRecipe.CODEC
      .xmap(GunSmithTableRecipe::new, recipe -> new TableRecipe(recipe.inputs, recipe.result));
   public static final StreamCodec<RegistryFriendlyByteBuf, GunSmithTableRecipe> STREAM_CODEC = TableRecipe.STREAM_CODEC
      .map(GunSmithTableRecipe::new, recipe -> new TableRecipe(recipe.inputs, recipe.result));
   private final GunSmithTableResult result;
   private final List<GunSmithTableIngredient> inputs;

   public GunSmithTableRecipe(GunSmithTableResult result, List<GunSmithTableIngredient> inputs) {
      this.result = result;
      this.inputs = inputs;
   }

   public GunSmithTableRecipe(TableRecipe tableRecipe) {
      this(tableRecipe.getResult(), tableRecipe.getMaterials());
   }

   @Deprecated
   public boolean matches(SmithingRecipeInput smithingRecipeInput, Level level) {
      return false;
   }

   @Deprecated
   @Override
   public ItemStack assemble(SmithingRecipeInput smithingRecipeInput) {
      return ItemStack.EMPTY;
   }

   @Override
   public boolean showNotification() {
      return false;
   }

   @Override
   public String group() {
      return "";
   }

   @Override
   public net.minecraft.world.item.crafting.PlacementInfo placementInfo() {
      return net.minecraft.world.item.crafting.PlacementInfo.NOT_PLACEABLE;
   }

   @Override
   public net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory() {
      return net.minecraft.world.item.crafting.RecipeBookCategories.CRAFTING_MISC;
   }

   public ItemStack getResultItem(Provider provider) {
      return this.result.getResult().copy();
   }

   public RecipeSerializer<GunSmithTableRecipe> getSerializer() {
      return ModRecipe.GUN_SMITH_TABLE_RECIPE_SERIALIZER;
   }

   public RecipeType<GunSmithTableRecipe> getType() {
      return ModRecipe.GUN_SMITH_TABLE_CRAFTING;
   }

   public ItemStack getOutput() {
      return this.result.getResult();
   }

   public List<GunSmithTableIngredient> getInputs() {
      return this.inputs;
   }

   public GunSmithTableResult getResult() {
      return this.result;
   }

   public void init(Provider provider) {
      this.result.init(provider);
   }

   public Identifier getTab() {
      return this.result.getGroup();
   }
}
