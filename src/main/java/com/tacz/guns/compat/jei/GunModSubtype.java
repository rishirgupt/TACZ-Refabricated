package com.tacz.guns.compat.jei;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IBlock;
import com.tacz.guns.api.item.IGun;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GunModSubtype {
   public static ISubtypeInterpreter<ItemStack> getAmmoSubtype() {
      return new ISubtypeInterpreter<ItemStack>() {
         @Nullable
         public Object getSubtypeData(ItemStack stack, UidContext uidContext) {
            return this.getLegacyStringSubtypeInfo(stack, uidContext);
         }

         public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
            return stack.getItem() instanceof IAmmo iAmmo ? iAmmo.getAmmoId(stack).toString() : "";
         }
      };
   }

   public static ISubtypeInterpreter<ItemStack> getGunSubtype() {
      return new ISubtypeInterpreter<ItemStack>() {
         @Nullable
         public Object getSubtypeData(ItemStack stack, UidContext uidContext) {
            return this.getLegacyStringSubtypeInfo(stack, uidContext);
         }

         public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
            return stack.getItem() instanceof IGun iGun ? iGun.getGunId(stack).toString() : "";
         }
      };
   }

   public static ISubtypeInterpreter<ItemStack> getAttachmentSubtype() {
      return new ISubtypeInterpreter<ItemStack>() {
         @Nullable
         public Object getSubtypeData(ItemStack stack, UidContext uidContext) {
            return this.getLegacyStringSubtypeInfo(stack, uidContext);
         }

         public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
            return stack.getItem() instanceof IAttachment iAttachment ? iAttachment.getAttachmentId(stack).toString() : "";
         }
      };
   }

   public static ISubtypeInterpreter<ItemStack> getTableSubType() {
      return new ISubtypeInterpreter<ItemStack>() {
         @Nullable
         public Object getSubtypeData(ItemStack stack, UidContext uidContext) {
            return this.getLegacyStringSubtypeInfo(stack, uidContext);
         }

         public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
            return stack.getItem() instanceof IBlock iBlock ? iBlock.getBlockId(stack).toString() : "";
         }
      };
   }

   public static ISubtypeInterpreter<ItemStack> getAmmoBoxSubtype() {
      return new ISubtypeInterpreter<ItemStack>() {
         @Nullable
         public Object getSubtypeData(ItemStack stack, UidContext uidContext) {
            return this.getLegacyStringSubtypeInfo(stack, uidContext);
         }

         public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
            if (stack.getItem() instanceof IAmmoBox iAmmoBox) {
               if (iAmmoBox.isAllTypeCreative(stack)) {
                  return "all_type_creative";
               } else {
                  return iAmmoBox.isCreative(stack) ? "creative" : String.format("level_%d", iAmmoBox.getAmmoLevel(stack));
               }
            } else {
               return "";
            }
         }
      };
   }
}
