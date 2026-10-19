package com.tacz.guns.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultTableItem extends GunSmithTableItem {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "gun_smith_table");

   public DefaultTableItem(Block block) {
      super(block);
   }

   @NotNull
   @Override
   public Identifier getBlockId(ItemStack block) {
      return ID;
   }

   @Override
   public void setBlockId(ItemStack block, @Nullable Identifier blockId) {
   }
}
