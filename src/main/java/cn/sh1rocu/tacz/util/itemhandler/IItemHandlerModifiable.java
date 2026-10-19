package cn.sh1rocu.tacz.util.itemhandler;

import net.minecraft.world.item.ItemStack;

public interface IItemHandlerModifiable extends IItemHandler {
   void setStackInSlot(int var1, ItemStack var2);
}
