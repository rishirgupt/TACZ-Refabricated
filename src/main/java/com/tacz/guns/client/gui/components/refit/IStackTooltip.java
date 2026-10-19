package com.tacz.guns.client.gui.components.refit;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

public interface IStackTooltip {
   void renderTooltip(Consumer<ItemStack> var1);
}
