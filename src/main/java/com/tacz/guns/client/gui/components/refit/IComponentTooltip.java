package com.tacz.guns.client.gui.components.refit;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag.Default;

public interface IComponentTooltip {
   static List<Component> getTooltipFromItem(ItemStack stack) {
      Options options = Minecraft.getInstance().options;
      LocalPlayer player = Minecraft.getInstance().player;
      return stack.getTooltipLines(TooltipContext.of(player.level()), player, options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL);
   }

   void renderTooltip(Consumer<List<Component>> var1);
}
