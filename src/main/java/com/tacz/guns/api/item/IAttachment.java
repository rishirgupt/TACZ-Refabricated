package com.tacz.guns.api.item;

import com.tacz.guns.api.item.attachment.AttachmentType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public interface IAttachment {
   @Nullable
   static IAttachment getIAttachmentOrNull(@Nullable ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         return stack.getItem() instanceof IAttachment iAttachment ? iAttachment : null;
      }
   }

   @Nonnull
   Identifier getAttachmentId(ItemStack var1);

   void setAttachmentId(ItemStack var1, @Nullable Identifier var2);

   @Deprecated
   @Nullable
   Identifier getSkinId(ItemStack var1);

   @Deprecated
   void setSkinId(ItemStack var1, @Nullable Identifier var2);

   int getZoomNumber(ItemStack var1);

   void setZoomNumber(ItemStack var1, int var2);

   @Nonnull
   AttachmentType getType(ItemStack var1);

   boolean hasCustomLaserColor(ItemStack var1);

   int getLaserColor(ItemStack var1);

   void setLaserColor(ItemStack var1, int var2);
}
