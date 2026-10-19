package cn.sh1rocu.tacz.compat.rei;

import cn.sh1rocu.tacz.compat.rei.category.AttachmentQueryCategory;
import cn.sh1rocu.tacz.compat.rei.category.GunSmithTableCategory;
import cn.sh1rocu.tacz.compat.rei.display.AttachmentQueryDisplay;
import cn.sh1rocu.tacz.compat.rei.display.GunSmithTableDisplay;
import cn.sh1rocu.tacz.compat.rei.entry.AttachmentQueryEntry;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.BlockItemBuilder;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.resource.index.CommonBlockIndex;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;

public class REIClientPlugin implements me.shedaniel.rei.api.client.plugins.REIClientPlugin {
   public static final CategoryIdentifier<AttachmentQueryDisplay> ATTACHMENT_QUERY = CategoryIdentifier.of("tacz", "plugins/attachment_query");
   public static final Map<Identifier, CategoryIdentifier<GunSmithTableDisplay>> displays = new HashMap<>();

   public void registerCategories(CategoryRegistry registry) {
      for (Entry<Identifier, CommonBlockIndex> entry : TimelessAPI.getAllCommonBlockIndex()) {
         BlockItem item = entry.getValue().getBlock();
         ItemStack icon = BlockItemBuilder.create(item).setId(entry.getKey()).build();
         CategoryIdentifier<GunSmithTableDisplay> id = CategoryIdentifier.of("tacz", "plugins/gun_smith_table/" + entry.getKey().toString().replace(':', '_'));
         registry.add(new GunSmithTableCategory(Component.translatable(entry.getValue().getPojo().getName()), icon, id));
         displays.put(entry.getKey(), id);
         registry.addWorkstations(id, new EntryStack[]{EntryStacks.of(icon)});
      }

      registry.add(new AttachmentQueryCategory());
   }

   public void registerDisplays(DisplayRegistry registry) {
      if (Minecraft.getInstance().level != null) {
         RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
         List<RecipeHolder<GunSmithTableRecipe>> recipes = ModRecipe.allRecipes(recipeManager);

         for (Entry<Identifier, CategoryIdentifier<GunSmithTableDisplay>> entry : displays.entrySet()) {
            TimelessAPI.getCommonBlockIndex(entry.getKey())
               .ifPresent(
                  blockIndex -> {
                     List<GunSmithTableRecipe> recipeList = blockIndex.getFilter()
                        .filter(recipes, RecipeHolder::id)
                        .stream()
                        .<GunSmithTableRecipe>map(RecipeHolder::value)
                        .collect(Collectors.toList());
                     recipeList.removeIf(
                        recipe -> blockIndex.getData().getTabs().stream().noneMatch(tab -> Objects.equals(tab.id(), recipe.getResult().getGroup()))
                     );
                     recipeList.forEach(recipe -> registry.add(new GunSmithTableDisplay(recipe, entry)));
                  }
               );
         }

         AttachmentQueryEntry.getAllAttachmentQueryEntries().forEach(entryx -> registry.add(new AttachmentQueryDisplay(entryx)));
      }
   }
}
