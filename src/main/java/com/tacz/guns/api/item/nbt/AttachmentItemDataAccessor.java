package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IAttachment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public interface AttachmentItemDataAccessor extends IAttachment {
   String ATTACHMENT_ID_TAG = "AttachmentId";
   String SKIN_ID_TAG = "Skin";
   String ZOOM_NUMBER_TAG = "ZoomNumber";
   String LASER_COLOR_TAG = "LaserColor";

   static boolean isAttachmentLike(CompoundTag tag) {
      return tag.contains("AttachmentId");
   }

   @Nonnull
   static Identifier getAttachmentIdFromTag(@Nullable CompoundTag nbt) {
      if (nbt == null) {
         return DefaultAssets.EMPTY_ATTACHMENT_ID;
      } else if (isAttachmentLike(nbt)) {
         Identifier attachmentId = Identifier.tryParse(nbt.getStringOr("AttachmentId", ""));
         return Objects.requireNonNullElse(attachmentId, DefaultAssets.EMPTY_ATTACHMENT_ID);
      } else {
         return DefaultAssets.EMPTY_ATTACHMENT_ID;
      }
   }

   static int getZoomNumberFromTag(@Nullable CompoundTag nbt) {
      if (nbt == null) {
         return 0;
      } else {
         return nbt.contains("ZoomNumber") ? nbt.getIntOr("ZoomNumber", 0) : 0;
      }
   }

   static void setZoomNumberToTag(CompoundTag nbt, int zoomNumber) {
      nbt.putInt("ZoomNumber", zoomNumber);
   }

   static void setLaserColorToTag(CompoundTag nbt, int color) {
      nbt.putInt("LaserColor", color);
   }

   @Nonnull
   @Override
   default Identifier getAttachmentId(ItemStack attachmentStack) {
      CompoundTag nbt = ((CustomData)attachmentStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return getAttachmentIdFromTag(nbt);
   }

   @Override
   default void setAttachmentId(ItemStack attachmentStack, @Nullable Identifier attachmentId) {
      attachmentStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (attachmentId != null) {
            tag.putString("AttachmentId", attachmentId.toString());
         }
      }));
   }

   @Nullable
   @Override
   default Identifier getSkinId(ItemStack attachmentStack) {
      CompoundTag nbt = ((CustomData)attachmentStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("Skin") ? Identifier.tryParse(nbt.getStringOr("Skin", "")) : null;
   }

   @Override
   default void setSkinId(ItemStack attachmentStack, @Nullable Identifier skinId) {
      attachmentStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (skinId != null) {
            tag.putString("Skin", skinId.toString());
         } else {
            tag.remove("Skin");
         }
      }));
   }

   @Override
   default int getZoomNumber(ItemStack attachmentStack) {
      CompoundTag nbt = ((CustomData)attachmentStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return getZoomNumberFromTag(nbt);
   }

   @Override
   default void setZoomNumber(ItemStack attachmentStack, int zoomNumber) {
      attachmentStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> setZoomNumberToTag(tag, zoomNumber)));
   }

   @Override
   default boolean hasCustomLaserColor(ItemStack attachmentStack) {
      CompoundTag nbt = ((CustomData)attachmentStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return nbt.contains("LaserColor");
   }

   @Override
   default int getLaserColor(ItemStack attachmentStack) {
      CompoundTag nbt = ((CustomData)attachmentStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return !this.hasCustomLaserColor(attachmentStack) ? 16711680 : nbt.getIntOr("LaserColor", 0);
   }

   @Override
   default void setLaserColor(ItemStack attachmentStack, int color) {
      attachmentStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> setLaserColorToTag(tag, color)));
   }
}
