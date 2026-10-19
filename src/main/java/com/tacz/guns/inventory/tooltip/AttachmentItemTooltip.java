package com.tacz.guns.inventory.tooltip;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class AttachmentItemTooltip implements TooltipComponent {
   private final ItemStack attachmentItem;
   private final Identifier attachmentId;
   private final AttachmentType type;

   public AttachmentItemTooltip(Identifier attachmentId, AttachmentType type, ItemStack attachmentItem) {
      this.attachmentId = attachmentId;
      this.type = type;
      this.attachmentItem = attachmentItem;
   }

   public Identifier getAttachmentId() {
      return this.attachmentId;
   }

   public AttachmentType getType() {
      return this.type;
   }

   public ItemStack getAttachmentItem() {
      return this.attachmentItem;
   }
}
