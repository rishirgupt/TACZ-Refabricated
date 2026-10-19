package com.tacz.guns.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public enum GunTooltipPart {
   DESCRIPTION,
   AMMO_INFO,
   BASE_INFO,
   EXTRA_DAMAGE_INFO,
   UPGRADES_TIP,
   PACK_INFO;

   private final int mask = 1 << this.ordinal();

   public int getMask() {
      return this.mask;
   }

   public static int getHideFlags(ItemStack stack) {
      CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
      return tag.contains("HideFlags") ? tag.getIntOr("HideFlags", 0) : 0;
   }

   public static void setHideFlags(ItemStack stack, int mask) {
      net.minecraft.world.item.component.TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT);
      if ((mask & 1) == 1) {
         display = new net.minecraft.world.item.component.TooltipDisplay(true, display.hiddenComponents());
      }

      if ((mask & 2) == 2) {
         display = display.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true);
      }

      stack.set(DataComponents.TOOLTIP_DISPLAY, display);
   }
}
