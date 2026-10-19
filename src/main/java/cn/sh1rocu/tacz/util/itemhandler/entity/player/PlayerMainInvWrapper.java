package cn.sh1rocu.tacz.util.itemhandler.entity.player;

import cn.sh1rocu.tacz.util.itemhandler.InvWrapper;
import cn.sh1rocu.tacz.util.itemhandler.RangedWrapper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public class PlayerMainInvWrapper extends RangedWrapper {
   private final Inventory inventoryPlayer;

   public PlayerMainInvWrapper(Inventory inv) {
      super(new InvWrapper(inv), 0, Inventory.INVENTORY_SIZE);
      this.inventoryPlayer = inv;
   }

   @Override
   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      ItemStack rest = super.insertItem(slot, stack, simulate);
      if (rest.getCount() != stack.getCount()) {
         ItemStack inSlot = this.getStackInSlot(slot);
         if (!inSlot.isEmpty()) {
            if (this.getInventoryPlayer().player.level().isClientSide()) {
               inSlot.setPopTime(5);
            } else if (this.getInventoryPlayer().player instanceof ServerPlayer) {
               this.getInventoryPlayer().player.containerMenu.broadcastChanges();
            }
         }
      }

      return rest;
   }

   public Inventory getInventoryPlayer() {
      return this.inventoryPlayer;
   }
}
