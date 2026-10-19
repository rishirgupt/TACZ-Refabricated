package cn.sh1rocu.tacz.util.itemhandler.entity;

import cn.sh1rocu.tacz.util.forge.LazyOptional;
import cn.sh1rocu.tacz.util.itemhandler.CombinedInvWrapper;
import cn.sh1rocu.tacz.util.itemhandler.IItemHandlerModifiable;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot.Type;

public abstract class EntityEquipmentInvWrapper implements IItemHandlerModifiable {
   protected final LivingEntity entity;
   protected final List<EquipmentSlot> slots;

   public EntityEquipmentInvWrapper(LivingEntity entity, Type slotType) {
      this.entity = entity;
      List<EquipmentSlot> slots = new ArrayList<>();

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == slotType) {
            slots.add(slot);
         }
      }

      this.slots = ImmutableList.copyOf(slots);
   }

   @Override
   public int getSlots() {
      return this.slots.size();
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      return this.entity.getItemBySlot(this.validateSlotIndex(slot));
   }

   @Override
   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      }

      EquipmentSlot equipmentSlot = this.validateSlotIndex(slot);
      ItemStack existing = this.entity.getItemBySlot(equipmentSlot);
      int limit = this.getStackLimit(slot, stack);
      if (!existing.isEmpty()) {
         if (!ItemStack.isSameItemSameComponents(stack, existing)) {
            return stack;
         }

         limit -= existing.getCount();
      }

      if (limit <= 0) {
         return stack;
      }

      boolean reachedLimit = stack.getCount() > limit;
      if (!simulate) {
         if (existing.isEmpty()) {
            this.entity.setItemSlot(equipmentSlot, reachedLimit ? stack.copyWithCount(limit) : stack);
         } else {
            existing.grow(reachedLimit ? limit : stack.getCount());
         }
      }

      return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
   }

   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (amount == 0) {
         return ItemStack.EMPTY;
      }

      EquipmentSlot equipmentSlot = this.validateSlotIndex(slot);
      ItemStack existing = this.entity.getItemBySlot(equipmentSlot);
      if (existing.isEmpty()) {
         return ItemStack.EMPTY;
      }

      int toExtract = Math.min(amount, existing.getMaxStackSize());
      if (existing.getCount() <= toExtract) {
         if (!simulate) {
            this.entity.setItemSlot(equipmentSlot, ItemStack.EMPTY);
         }

         return existing;
      } else {
         if (!simulate) {
            this.entity.setItemSlot(equipmentSlot, existing.copyWithCount(existing.getCount() - toExtract));
         }

         return existing.copyWithCount(toExtract);
      }
   }

   @Override
   public int getSlotLimit(int slot) {
      EquipmentSlot equipmentSlot = this.validateSlotIndex(slot);
      return equipmentSlot.getType() == Type.HUMANOID_ARMOR ? 1 : 99;
   }

   protected int getStackLimit(int slot, ItemStack stack) {
      return Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      EquipmentSlot equipmentSlot = this.validateSlotIndex(slot);
      if (!ItemStack.matches(this.entity.getItemBySlot(equipmentSlot), stack)) {
         this.entity.setItemSlot(equipmentSlot, stack);
      }
   }

   @Override
   public boolean isItemValid(int slot, ItemStack stack) {
      return true;
   }

   protected EquipmentSlot validateSlotIndex(int slot) {
      if (slot >= 0 && slot < this.slots.size()) {
         return this.slots.get(slot);
      } else {
         throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + this.slots.size() + ")");
      }
   }

   public static LazyOptional<IItemHandlerModifiable>[] create(LivingEntity entity) {
      LazyOptional<IItemHandlerModifiable>[] ret = new LazyOptional[]{
         LazyOptional.of(() -> new EntityHandsInvWrapper(entity)), LazyOptional.of(() -> new EntityArmorInvWrapper(entity)), null
      };
      ret[2] = LazyOptional.of(() -> new CombinedInvWrapper(ret[0].orElse(null), ret[1].orElse(null)));
      return ret;
   }
}
