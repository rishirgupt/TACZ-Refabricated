package com.tacz.guns.compat.jei;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.BlockItemBuilder;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.compat.jei.category.AttachmentQueryCategory;
import com.tacz.guns.compat.jei.category.GunSmithTableCategory;
import com.tacz.guns.compat.jei.entry.AttachmentQueryEntry;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.resource.index.CommonBlockIndex;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public class GunModPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.fromNamespaceAndPath("tacz", "jei");
   private Map<Identifier, IRecipeType<GunSmithTableRecipe>> recipeTypeMap = new HashMap<>();

   public void registerCategories(IRecipeCategoryRegistration registration) {
      this.recipeTypeMap.clear();

      for (Entry<Identifier, CommonBlockIndex> entry : TimelessAPI.getAllCommonBlockIndex()) {
         BlockItem item = entry.getValue().getBlock();
         ItemStack icon = BlockItemBuilder.create(item).setId(entry.getKey()).build();
         IRecipeType<GunSmithTableRecipe> type = IRecipeType.create(
            "tacz", "gun_smith_table/" + entry.getKey().toString().replace(':', '_'), GunSmithTableRecipe.class
         );
         registration.addRecipeCategories(
            new IRecipeCategory[]{
               new GunSmithTableCategory(registration.getJeiHelpers().getGuiHelper(), icon, type, Component.translatable(entry.getValue().getPojo().getName()))
            }
         );
         this.recipeTypeMap.put(entry.getKey(), type);
      }

      registration.addRecipeCategories(new IRecipeCategory[]{new AttachmentQueryCategory(registration.getJeiHelpers().getGuiHelper())});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      if (Minecraft.getInstance().level != null) {
         List<com.tacz.guns.client.resource.ClientRecipeCache.Entry> recipes = com.tacz.guns.client.resource.ClientRecipeCache.entries();

         for (Entry<Identifier, IRecipeType<GunSmithTableRecipe>> entry : this.recipeTypeMap.entrySet()) {
            TimelessAPI.getCommonBlockIndex(entry.getKey())
               .ifPresent(
                  blockIndex -> {
                     List<GunSmithTableRecipe> recipeList = blockIndex.getFilter()
                        .filter(recipes, com.tacz.guns.client.resource.ClientRecipeCache.Entry::id)
                        .stream()
                        .<GunSmithTableRecipe>map(com.tacz.guns.client.resource.ClientRecipeCache.Entry::value)
                        .collect(Collectors.toList());
                     recipeList.removeIf(
                        recipe -> blockIndex.getData().getTabs().stream().noneMatch(tab -> Objects.equals(tab.id(), recipe.getResult().getGroup()))
                     );
                     registration.addRecipes(entry.getValue(), recipeList);
                  }
               );
         }

         registration.addRecipes(AttachmentQueryCategory.ATTACHMENT_QUERY, AttachmentQueryEntry.getAllAttachmentQueryEntries());
      }
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      for (Entry<Identifier, IRecipeType<GunSmithTableRecipe>> entry : this.recipeTypeMap.entrySet()) {
         TimelessAPI.getCommonBlockIndex(entry.getKey()).ifPresent(blockIndex -> {
            ItemStack stack = BlockItemBuilder.create(blockIndex.getBlock()).setId(entry.getKey()).build();
            registration.addRecipeCatalyst(stack, new IRecipeType<?>[]{entry.getValue()});
         });
      }
   }

   public void registerItemSubtypes(ISubtypeRegistration registration) {
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.AMMO, GunModSubtype.getAmmoSubtype());
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.ATTACHMENT, GunModSubtype.getAttachmentSubtype());
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.AMMO_BOX, GunModSubtype.getAmmoBoxSubtype());
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.WORKBENCH_111, GunModSubtype.getTableSubType());
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.WORKBENCH_121, GunModSubtype.getTableSubType());
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.WORKBENCH_211, GunModSubtype.getTableSubType());
      GunItemManager.getAllGunItems().forEach(item -> registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, GunModSubtype.getGunSubtype()));
   }

   public Identifier getPluginUid() {
      return UID;
   }
}
