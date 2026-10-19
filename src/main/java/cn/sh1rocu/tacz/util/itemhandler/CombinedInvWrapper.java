package cn.sh1rocu.tacz.util.itemhandler;

import net.minecraft.world.item.ItemStack;

public class CombinedInvWrapper implements IItemHandlerModifiable {
   protected final IItemHandlerModifiable[] itemHandler;
   protected final int[] baseIndex;
   protected final int slotCount;

   public CombinedInvWrapper(IItemHandlerModifiable... itemHandler) {
      this.itemHandler = itemHandler;
      this.baseIndex = new int[itemHandler.length];
      int index = 0;

      for (int i = 0; i < itemHandler.length; i++) {
         index += itemHandler[i].getSlots();
         this.baseIndex[i] = index;
      }

      this.slotCount = index;
   }

   protected int getIndexForSlot(int slot) {
      if (slot < 0) {
         return -1;
      }

      for (int i = 0; i < this.baseIndex.length; i++) {
         if (slot - this.baseIndex[i] < 0) {
            return i;
         }
      }

      return -1;
   }

   protected IItemHandlerModifiable getHandlerFromIndex(int index) {
      return index >= 0 && index < this.itemHandler.length ? this.itemHandler[index] : (IItemHandlerModifiable)EmptyItemHandler.INSTANCE;
   }

   protected int getSlotFromIndex(int slot, int index) {
      return index > 0 && index < this.baseIndex.length ? slot - this.baseIndex[index - 1] : slot;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      int index = this.getIndexForSlot(slot);
      IItemHandlerModifiable handler = this.getHandlerFromIndex(index);
      slot = this.getSlotFromIndex(slot, index);
      handler.setStackInSlot(slot, stack);
   }

   @Override
   public int getSlots() {
      return this.slotCount;
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      int index = this.getIndexForSlot(slot);
      IItemHandlerModifiable handler = this.getHandlerFromIndex(index);
      slot = this.getSlotFromIndex(slot, index);
      return handler.getStackInSlot(slot);
   }

   @Override
   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      int index = this.getIndexForSlot(slot);
      IItemHandlerModifiable handler = this.getHandlerFromIndex(index);
      slot = this.getSlotFromIndex(slot, index);
      return handler.insertItem(slot, stack, simulate);
   }

   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      int index = this.getIndexForSlot(slot);
      IItemHandlerModifiable handler = this.getHandlerFromIndex(index);
      slot = this.getSlotFromIndex(slot, index);
      return handler.extractItem(slot, amount, simulate);
   }

   @Override
   public int getSlotLimit(int slot) {
      int index = this.getIndexForSlot(slot);
      IItemHandlerModifiable handler = this.getHandlerFromIndex(index);
      int localSlot = this.getSlotFromIndex(slot, index);
      return handler.getSlotLimit(localSlot);
   }

   @Override
   public boolean isItemValid(int slot, ItemStack stack) {
      int index = this.getIndexForSlot(slot);
      IItemHandlerModifiable handler = this.getHandlerFromIndex(index);
      int localSlot = this.getSlotFromIndex(slot, index);
      return handler.isItemValid(localSlot, stack);
   }
}
