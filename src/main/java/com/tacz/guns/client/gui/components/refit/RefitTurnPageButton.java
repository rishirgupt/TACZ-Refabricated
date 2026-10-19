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

public class RefitTurnPageButton extends Button implements IComponentTooltip {
   private final boolean isUpPage;

   public RefitTurnPageButton(int pX, int pY, boolean isUpPage, OnPress pOnPress) {
      super(pX, pY, 18, 8, Component.empty(), pOnPress, DEFAULT_NARRATION);
      this.isUpPage = isUpPage;
   }

   public void extractContents(@Nonnull GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
      int x = this.getX();
      int y = this.getY();
      int yOffset = this.isUpPage ? 0 : 80;
      if (this.isHoveredOrFocused()) {
         com.tacz.guns.client.gui.GuiBlit.blit(graphics, GunRefitScreen.TURN_PAGE_TEXTURE, x, y, this.width, this.height, 0.0F, yOffset, 180, 80, 180, 160);
      } else {
         com.tacz.guns.client.gui.GuiBlit.blit(graphics, 
            GunRefitScreen.TURN_PAGE_TEXTURE, x + 1, y + 1, this.width - 2, this.height - 2, 10.0F, yOffset + 10, 160, 60, 180, 160
         );
      }
   }

   @Override
   public void renderTooltip(Consumer<List<Component>> consumer) {
      if (this.isHoveredOrFocused()) {
         String key = this.isUpPage ? "tooltip.tacz.page.previous" : "tooltip.tacz.page.next";
         consumer.accept(Collections.singletonList(Component.translatable(key)));
      }
   }
}
