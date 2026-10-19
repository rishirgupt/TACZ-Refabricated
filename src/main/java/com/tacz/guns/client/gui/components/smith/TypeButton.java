package com.tacz.guns.client.gui.components.smith;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import org.jetbrains.annotations.NotNull;

public class TypeButton extends Button {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/gun_smith_table.png");
   private final ItemStack stack;
   private boolean isSelected = false;

   public TypeButton(int pX, int pY, ItemStack stack, OnPress onPress) {
      super(pX, pY, 24, 25, Component.empty(), onPress, DEFAULT_NARRATION);
      this.stack = stack;
   }

   public void extractContents(@NotNull GuiGraphicsExtractor gui, int pMouseX, int pMouseY, float pPartialTick) {
      int vOffset = this.isHoveredOrFocused() ? 204 + this.height : 204;
      if (this.isSelected) {
         gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 0.0F, vOffset, this.width, this.height, 256, 256);
      } else {
         gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 26.0F, vOffset, this.width, this.height, 256, 256);
      }

      gui.item(this.stack, this.getX() + 4, this.getY() + 5);
   }

   public void onPress(net.minecraft.client.input.InputWithModifiers input) {
      this.isSelected = true;
      this.onPress.onPress(this);
   }

   public void setSelected(boolean selected) {
      this.isSelected = selected;
   }
}
