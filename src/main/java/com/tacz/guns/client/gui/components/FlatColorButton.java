package com.tacz.guns.client.gui.components;

import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button.OnPress;

public class FlatColorButton extends Button {
   private boolean isSelect = false;
   private List<Component> tooltips;

   public FlatColorButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
      super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
   }

   public FlatColorButton setTooltips(String key) {
      this.tooltips = Collections.singletonList(Component.translatable(key));
      return this;
   }

   public FlatColorButton setTooltips(List<Component> tooltips) {
      this.tooltips = tooltips;
      return this;
   }

   public FlatColorButton setTooltips(Component... tooltips) {
      this.tooltips = List.of(tooltips);
      return this;
   }

   public void renderToolTip(GuiGraphicsExtractor graphics, Screen screen, int pMouseX, int pMouseY) {
      if (this.isHovered && this.tooltips != null) {
         graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltips, pMouseX, pMouseY);
      }
   }

   public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pPartialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      Font font = minecraft.font;
      if (this.isSelect) {
         graphics.fillGradient(
            this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -1356717534, -1356717534
         );
      } else {
         graphics.fillGradient(
            this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -1356717534, -1356717534
         );
      }

      if (this.isHoveredOrFocused()) {
         graphics.fillGradient(
            this.getX(), this.getY() + 1, this.getX() + 1, this.getY() + this.height - 1, -790560, -790560
         );
         graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, -790560, -790560);
         graphics.fillGradient(
            this.getX() + this.width - 1,
            this.getY() + 1,
            this.getX() + this.width,
            this.getY() + this.height - 1,
            -790560,
            -790560
         );
         graphics.fillGradient(
            this.getX(),
            this.getY() + this.height - 1,
            this.getX() + this.width,
            this.getY() + this.height,
            -790560,
            -790560
         );
      }

      graphics.centeredText(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFF000000 | 15986656);
      this.renderToolTip(graphics, minecraft.gui.screen(), mouseX, mouseY);
   }

   public void setSelect(boolean select) {
      this.isSelect = select;
   }
}
