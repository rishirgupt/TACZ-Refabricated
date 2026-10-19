package cn.sh1rocu.tacz.util.itemhandler;

import net.minecraft.world.item.ItemStack;

public interface IItemHandler {
   int getSlots();

   ItemStack getStackInSlot(int var1);

   ItemStack insertItem(int var1, ItemStack var2, boolean var3);

   ItemStack extractItem(int var1, int var2, boolean var3);

   int getSlotLimit(int var1);

   boolean isItemValid(int var1, ItemStack var2);
}
