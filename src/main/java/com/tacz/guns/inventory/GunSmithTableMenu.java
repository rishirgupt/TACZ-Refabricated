package com.tacz.guns.inventory;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageCraft;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonBlockIndex;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class GunSmithTableMenu extends AbstractContainerMenu {
   public static final MenuType<GunSmithTableMenu> TYPE = new ExtendedMenuType<>(GunSmithTableMenu::new, Identifier.STREAM_CODEC);
   private final Identifier blockId;
   private final RecipeFilter filter;

   public GunSmithTableMenu(int id, Inventory inventory, @Nullable Identifier resourceLocation) {
      super(TYPE, id);
      this.blockId = resourceLocation;
      this.filter = TimelessAPI.getCommonBlockIndex(this.getBlockId()).map(CommonBlockIndex::getFilter).orElse(null);
   }

   @Nullable
   public Identifier getBlockId() {
      return this.blockId;
   }

   public ItemStack quickMoveStack(Player player, int pIndex) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return player.isAlive();
   }

   @Nullable
   private GunSmithTableRecipe getRecipe(Identifier recipeId, RecipeManager recipeManager) {
      if ((!DefaultAssets.DEFAULT_BLOCK_ID.equals(this.getBlockId()) || (Boolean)SyncConfig.ENABLE_TABLE_FILTER.get())
         && this.filter != null
         && !this.filter.contains(recipeId)) {
         return null;
      }

      RecipeHolder<?> holder = (RecipeHolder<?>)recipeManager.byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, recipeId)).orElse(null);
      if (holder != null && holder.value() instanceof GunSmithTableRecipe gunSmithTableRecipe) {
         boolean flag = TimelessAPI.getCommonBlockIndex(this.getBlockId())
            .map(blockIndex -> blockIndex.getData().getTabs().stream().noneMatch(tab -> tab.id().equals(gunSmithTableRecipe.getTab())))
            .orElse(true);
         if (DefaultAssets.DEFAULT_BLOCK_ID.equals(this.getBlockId()) && !(Boolean)SyncConfig.ENABLE_TABLE_FILTER.get()) {
            flag = false;
         }

         return flag ? null : gunSmithTableRecipe;
      } else {
         return null;
      }
   }

   public void doCraft(Identifier recipeId, Player player) {
      GunSmithTableRecipe recipe = this.getRecipe(recipeId, player.level().getServer().getRecipeManager());
      if (recipe != null) {
         player.tacz$getItemHandler(null)
            .ifPresent(
               handler -> {
                  if (!player.isCreative()) {
                     Int2IntArrayMap recordCount = new Int2IntArrayMap();

                     for (GunSmithTableIngredient ingredient : recipe.getInputs()) {
                        int count = 0;

                        for (int slotIndex = 0; slotIndex < handler.getSlots(); slotIndex++) {
                           ItemStack stack = handler.getStackInSlot(slotIndex);
                           int stackCount = stack.getCount();
                           if (!stack.isEmpty() && ingredient.getIngredient().test(stack)) {
                              count += stackCount;
                              if (count > ingredient.getCount()) {
                                 int remaining = count - ingredient.getCount();
                                 recordCount.put(slotIndex, stackCount - remaining);
                                 break;
                              }

                              recordCount.put(slotIndex, stackCount);
                           }
                        }

                        if (count < ingredient.getCount()) {
                           return;
                        }
                     }

                     IntIterator var16 = recordCount.keySet().iterator();

                     while (var16.hasNext()) {
                        int slotIndex = (Integer)var16.next();
                        handler.extractItem(slotIndex, recordCount.get(slotIndex), false);
                     }
                  }

                  Level level = player.level();
                  if (!level.isClientSide()) {
                     ItemEntity itemEntity = new ItemEntity(
                        level,
                        player.getX(),
                        player.getY() + 0.5,
                        player.getZ(),
                        recipe.getResultItem(player.level().registryAccess()).copy()
                     );
                     itemEntity.setPickUpDelay(0);
                     level.addFreshEntity(itemEntity);
                  }

                  player.inventoryMenu.broadcastFullState();
                  if (player instanceof ServerPlayer serverPlayer) {
                     NetworkHandler.sendToClientPlayer(new ServerMessageCraft(this.containerId), serverPlayer);
                  }
               }
            );
      }
   }
}
