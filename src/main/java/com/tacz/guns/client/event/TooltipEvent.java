package com.tacz.guns.client.event;

import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.api.item.nbt.BlockItemDataAccessor;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.init.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item.TooltipContext;

public class TooltipEvent {
   public static void onTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      if (flag.isAdvanced() && (Boolean)RenderConfig.ENABLE_TACZ_ID_IN_TOOLTIP.get()) {
         if (stack.getItem() instanceof GunItemDataAccessor item) {
            lines.add(formatTooltip("GunId", item.getGunId(stack)));
         } else if (stack.getItem() instanceof AmmoItemDataAccessor item) {
            lines.add(formatTooltip("AmmoId", item.getAmmoId(stack)));
         } else if (stack.getItem() instanceof AttachmentItemDataAccessor item) {
            lines.add(formatTooltip("AttachmentId", item.getAttachmentId(stack)));
         } else if (stack.getItem() instanceof BlockItemDataAccessor item && !ModItems.GUN_SMITH_TABLE.equals(item)) {
            lines.add(formatTooltip("BlockId", item.getBlockId(stack)));
         }
      }
   }

   public static Component formatTooltip(String key, Identifier value) {
      return Component.literal(String.format("%s: \"%s\"", key, value)).withStyle(ChatFormatting.DARK_GRAY);
   }
}
