package cn.sh1rocu.tacz.util.itemhandler;

import cn.sh1rocu.tacz.util.itemhandler.entity.player.PlayerMainInvWrapper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ItemHandlerHelper {
   public static ItemStack insertItem(IItemHandler dest, ItemStack stack, boolean simulate) {
      if (dest != null && !stack.isEmpty()) {
         for (int i = 0; i < dest.getSlots(); i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
               return ItemStack.EMPTY;
            }
         }

         return stack;
      } else {
         return stack;
      }
   }

   public static ItemStack insertItemStacked(IItemHandler inventory, ItemStack stack, boolean simulate) {
      if (inventory == null || stack.isEmpty()) {
         return stack;
      }

      if (!stack.isStackable()) {
         return insertItem(inventory, stack, simulate);
      }

      int sizeInventory = inventory.getSlots();

      for (int i = 0; i < sizeInventory; i++) {
         ItemStack slot = inventory.getStackInSlot(i);
         if (ItemStack.isSameItemSameComponents(slot, stack)) {
            stack = inventory.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
               break;
            }
         }
      }

      if (!stack.isEmpty()) {
         for (int i = 0; i < sizeInventory; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
               stack = inventory.insertItem(i, stack, simulate);
               if (stack.isEmpty()) {
                  break;
               }
            }
         }
      }

      return stack;
   }

   public static void giveItemToPlayer(Player player, ItemStack stack) {
      giveItemToPlayer(player, stack, -1);
   }

   public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
      if (!stack.isEmpty()) {
         IItemHandler inventory = new PlayerMainInvWrapper(player.getInventory());
         Level level = player.level();
         ItemStack remainder = stack;
         if (preferredSlot >= 0 && preferredSlot < inventory.getSlots()) {
            remainder = inventory.insertItem(preferredSlot, stack, false);
         }

         if (!remainder.isEmpty()) {
            remainder = insertItemStacked(inventory, remainder, false);
         }

         if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
            level.playSound(
               null,
               player.getX(),
               player.getY() + 0.5,
               player.getZ(),
               SoundEvents.ITEM_PICKUP,
               SoundSource.PLAYERS,
               0.2F,
               ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
            );
         }

         if (!remainder.isEmpty() && !level.isClientSide()) {
            ItemEntity entityitem = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
            entityitem.setPickUpDelay(40);
            entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            level.addFreshEntity(entityitem);
         }
      }
   }
}
