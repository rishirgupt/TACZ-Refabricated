package com.tacz.guns.client.gui.components.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.client.gui.GunRefitScreen;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;

public class RefitUnloadButton extends Button implements IComponentTooltip {
   public RefitUnloadButton(int pX, int pY, OnPress pOnPress) {
      super(pX, pY, 8, 8, Component.empty(), pOnPress, DEFAULT_NARRATION);
   }

   public void extractContents(@Nonnull GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
      int x = this.getX();
      int y = this.getY();
      if (this.isHoveredOrFocused()) {
         com.tacz.guns.client.gui.GuiBlit.blit(graphics, GunRefitScreen.UNLOAD_TEXTURE, x, y, this.width, this.height, 0.0F, 0.0F, 80, 80, 160, 80);
      } else {
         com.tacz.guns.client.gui.GuiBlit.blit(graphics, GunRefitScreen.UNLOAD_TEXTURE, x, y, this.width, this.height, 80.0F, 0.0F, 80, 80, 160, 80);
      }
   }

   @Override
   public void renderTooltip(Consumer<List<Component>> consumer) {
      if (this.isHoveredOrFocused()) {
         consumer.accept(Collections.singletonList(Component.translatable("tooltip.tacz.refit.unload")));
      }
   }
}
