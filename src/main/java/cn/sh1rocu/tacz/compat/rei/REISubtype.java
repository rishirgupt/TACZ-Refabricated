package cn.sh1rocu.tacz.compat.rei;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IBlock;
import com.tacz.guns.api.item.IGun;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import net.minecraft.world.item.ItemStack;

public class REISubtype {
   public static EntryComparator<ItemStack> getAmmoSubtype() {
      return (context, stack) -> stack.getItem() instanceof IAmmo iAmmo ? iAmmo.getAmmoId(stack).hashCode() : 0L;
   }

   public static EntryComparator<ItemStack> getGunSubtype() {
      return (context, stack) -> stack.getItem() instanceof IGun iGun ? iGun.getGunId(stack).hashCode() : 0L;
   }

   public static EntryComparator<ItemStack> getAttachmentSubtype() {
      return (context, stack) -> stack.getItem() instanceof IAttachment iAttachment ? iAttachment.getAttachmentId(stack).hashCode() : 0L;
   }

   public static EntryComparator<ItemStack> getTableSubType() {
      return (context, stack) -> stack.getItem() instanceof IBlock iBlock ? iBlock.getBlockId(stack).hashCode() : 0L;
   }

   public static EntryComparator<ItemStack> getAmmoBoxSubtype() {
      return (context, stack) -> {
         if (stack.getItem() instanceof IAmmoBox iAmmoBox) {
            if (iAmmoBox.isAllTypeCreative(stack)) {
               return "all_type_creative".hashCode();
            } else {
               return iAmmoBox.isCreative(stack) ? "creative".hashCode() : String.format("level_%d", iAmmoBox.getAmmoLevel(stack)).hashCode();
            }
         } else {
            return 0L;
         }
      };
   }
}
