package com.tacz.guns.item;

import cn.sh1rocu.tacz.api.extension.IItem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.client.renderer.item.AttachmentItemRenderer;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.inventory.tooltip.AttachmentItemTooltip;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.util.datafixer.AttachmentIdFix;
import java.util.Comparator;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class AttachmentItem extends Item implements AttachmentItemDataAccessor, IItem {
   public AttachmentItem() {
      super(new Properties().stacksTo(1));
   }

   @Nonnull
   @Environment(EnvType.CLIENT)
   public Component getName(@Nonnull ItemStack stack) {
      Identifier attachmentId = this.getAttachmentId(stack);
      Optional<ClientAttachmentIndex> attachmentIndex = TimelessAPI.getClientAttachmentIndex(attachmentId);
      return (Component)(attachmentIndex.isPresent() ? Component.translatable(attachmentIndex.get().getName()) : super.getName(stack));
   }

   private static Comparator<Entry<Identifier, CommonAttachmentIndex>> idNameSort() {
      return Comparator.comparingInt(m -> m.getValue().getSort());
   }

   public static NonNullList<ItemStack> fillItemCategory(AttachmentType type) {
      NonNullList<ItemStack> stacks = NonNullList.create();
      TimelessAPI.getAllCommonAttachmentIndex().stream().sorted(idNameSort()).forEach(entry -> {
         if (!((CommonAttachmentIndex)entry.getValue()).getPojo().isHidden()) {
            if (type.equals(entry.getValue().getType())) {
               ItemStack itemStack = AttachmentItemBuilder.create().setId(entry.getKey()).build();
               stacks.add(itemStack);
            }
         }
      });
      return stacks;
   }

   @Nonnull
   @Override
   public AttachmentType getType(ItemStack attachmentStack) {
      IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentStack);
      if (iAttachment != null) {
         Identifier id = iAttachment.getAttachmentId(attachmentStack);
         return TimelessAPI.getCommonAttachmentIndex(id).map(CommonAttachmentIndex::getType).orElse(AttachmentType.NONE);
      } else {
         return AttachmentType.NONE;
      }
   }

   public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
      return Optional.of(new AttachmentItemTooltip(this.getAttachmentId(stack), this.getType(stack), stack));
   }

   public void verifyTagAfterLoad(@NotNull CompoundTag tag) {
      AttachmentIdFix.updateAttachmentIdInTag(tag);
   }
}
