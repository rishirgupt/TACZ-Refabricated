package com.tacz.guns.compat.immediatelyfast;

import net.minecraft.world.item.ItemStack;

/** ImmediatelyFast batches HUD item drawing; the pre/post hooks it needed are not used on 26.2. */
public class ImmediatelyFastCompat {
   public static void init() {
   }

   public static void renderHotbarItem(ItemStack stack, boolean pre) {
   }
}
