package com.tacz.guns.compat.kubejs.util;

import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.item.AttachmentItem;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class AttachmentNbtFactory extends TimelessItemNbtFactory<AttachmentItem, AttachmentNbtFactory> {
   public AttachmentNbtFactory(@Nonnull AttachmentItem item) {
      super(item);
   }

   public AttachmentNbtFactory() {
      super((AttachmentItem)TimelessItemType.ATTACHMENT.getItem());
   }

   @Deprecated
   public void setSkinId(Identifier skinId) {
   }

   @Override
   public ItemStack build() {
      ItemStack stack = new ItemStack(this.item, this.count);
      if (this.item instanceof IAttachment iAttachment) {
         iAttachment.setAttachmentId(stack, this.id);
      }

      return stack;
   }
}
