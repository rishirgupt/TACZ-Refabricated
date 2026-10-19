package com.tacz.guns.api.item;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public interface IBlock {
   Identifier getBlockId(ItemStack var1);

   void setBlockId(ItemStack var1, @Nullable Identifier var2);
}
