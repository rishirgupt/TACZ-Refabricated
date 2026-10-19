package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IBlock;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public interface BlockItemDataAccessor extends IBlock {
   String BLOCK_ID = "BlockId";

   @Nonnull
   @Override
   default Identifier getBlockId(ItemStack block) {
      CompoundTag nbt = ((CustomData)block.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      if (nbt.contains("BlockId")) {
         Identifier gunId = Identifier.tryParse(nbt.getStringOr("BlockId", ""));
         return Objects.requireNonNullElse(gunId, DefaultAssets.EMPTY_BLOCK_ID);
      } else {
         return DefaultAssets.EMPTY_BLOCK_ID;
      }
   }

   @Override
   default void setBlockId(ItemStack block, @Nullable Identifier blockId) {
      block.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (blockId != null) {
            tag.putString("BlockId", blockId.toString());
         } else {
            tag.putString("BlockId", DefaultAssets.EMPTY_BLOCK_ID.toString());
         }
      }));
   }
}
