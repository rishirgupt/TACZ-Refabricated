package cn.sh1rocu.tacz.util.itemhandler;

import com.google.common.base.Preconditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.HolderLookup.Provider;

public class RangedWrapper implements IItemHandlerModifiable {
   private final IItemHandlerModifiable compose;
   private final int minSlot;
   private final int maxSlot;

   public RangedWrapper(IItemHandlerModifiable compose, int minSlot, int maxSlotExclusive) {
      Preconditions.checkArgument(maxSlotExclusive > minSlot, "Max slot must be greater than min slot");
      this.compose = compose;
      this.minSlot = minSlot;
      this.maxSlot = maxSlotExclusive;
   }

   @Override
   public int getSlots() {
      return this.maxSlot - this.minSlot;
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      return this.checkSlot(slot) ? this.compose.getStackInSlot(slot + this.minSlot) : ItemStack.EMPTY;
   }

   @Override
   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      return this.checkSlot(slot) ? this.compose.insertItem(slot + this.minSlot, stack, simulate) : stack;
   }

   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return this.checkSlot(slot) ? this.compose.extractItem(slot + this.minSlot, amount, simulate) : ItemStack.EMPTY;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (this.checkSlot(slot)) {
         this.compose.setStackInSlot(slot + this.minSlot, stack);
      }
   }

   @Override
   public int getSlotLimit(int slot) {
      return this.checkSlot(slot) ? this.compose.getSlotLimit(slot + this.minSlot) : 0;
   }

   @Override
   public boolean isItemValid(int slot, ItemStack stack) {
      return this.checkSlot(slot) ? this.compose.isItemValid(slot + this.minSlot, stack) : false;
   }

   private boolean checkSlot(int localSlot) {
      return localSlot + this.minSlot < this.maxSlot;
   }

   public CompoundTag serializeNBT(Provider provider) {
      ListTag nbtTagList = new ListTag();

      for (int i = 0; i < this.getSlots(); i++) {
         if (!this.getStackInSlot(i).isEmpty()) {
            CompoundTag itemTag = (CompoundTag)ItemStack.CODEC.encodeStart(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), this.getStackInSlot(i)).getOrThrow();
            itemTag.putInt("Slot", i);
            nbtTagList.add(itemTag);
         }
      }

      CompoundTag nbt = new CompoundTag();
      nbt.put("Items", nbtTagList);
      nbt.putInt("Size", this.getSlots());
      return nbt;
   }

   public void deserializeNBT(Provider provider, CompoundTag nbt) {
      int size = nbt.contains("Size") ? nbt.getIntOr("Size", 0) : this.getSlots();
      ListTag tagList = nbt.getListOrEmpty("Items");

      for (int i = 0; i < tagList.size(); i++) {
         CompoundTag itemTags = tagList.getCompoundOrEmpty(i);
         int slot = itemTags.getIntOr("Slot", 0);
         if (slot >= 0 && slot < size) {
            ItemStack.CODEC.parse(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), itemTags).result().ifPresent(stack -> this.setStackInSlot(slot, stack));
         }
      }
   }
}
