package cn.sh1rocu.tacz.util.itemhandler.entity.player;

import cn.sh1rocu.tacz.util.itemhandler.InvWrapper;
import cn.sh1rocu.tacz.util.itemhandler.RangedWrapper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot.Type;

public class PlayerArmorInvWrapper extends RangedWrapper {
   private final Inventory inventoryPlayer;

   public PlayerArmorInvWrapper(Inventory inv) {
      super(new InvWrapper(inv), Inventory.INVENTORY_SIZE, Inventory.INVENTORY_SIZE + 4);
      this.inventoryPlayer = inv;
   }

   @Override
   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      EquipmentSlot equ = null;

      for (EquipmentSlot s : EquipmentSlot.values()) {
         if (s.getType() == Type.HUMANOID_ARMOR && s.getIndex() == slot) {
            equ = s;
            break;
         }
      }

      return equ != null && slot < 4 && !stack.isEmpty() && this.getInventoryPlayer().player.getEquipmentSlotForItem(stack) == equ
         ? super.insertItem(slot, stack, simulate)
         : stack;
   }

   public Inventory getInventoryPlayer() {
      return this.inventoryPlayer;
   }
}
