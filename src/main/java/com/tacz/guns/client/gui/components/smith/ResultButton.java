package com.tacz.guns.client.gui.components.smith;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import org.jetbrains.annotations.NotNull;

public class ResultButton extends Button {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/gun_smith_table.png");
   private final ItemStack stack;
   private boolean isSelected = false;

   public ResultButton(int pX, int pY, ItemStack stack, OnPress onPress) {
      super(pX, pY, 94, 16, Component.empty(), onPress, DEFAULT_NARRATION);
      this.stack = stack;
   }

   public void extractContents(@NotNull GuiGraphicsExtractor gui, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.isSelected) {
         if (this.isHoveredOrFocused()) {
            gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() - 1, this.getY() - 1, 52.0F, 229.0F, this.width + 2, this.height + 2, 256, 256);
         } else {
            gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 53.0F, 230.0F, this.width, this.height, 256, 256);
         }
      } else if (this.isHoveredOrFocused()) {
         gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() - 1, this.getY() - 1, 52.0F, 211.0F, this.width + 2, this.height + 2, 256, 256);
      } else {
         gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 53.0F, 212.0F, this.width, this.height, 256, 256);
      }

      Minecraft mc = Minecraft.getInstance();
      gui.item(this.stack, this.getX() + 1, this.getY());
      Component hoverName = this.stack.getHoverName();
      gui.text(mc.font, hoverName, this.getX() + 20, this.getY() + 4, 0xFFFFFFFF, false);
   }

   public void onPress(net.minecraft.client.input.InputWithModifiers input) {
      this.isSelected = true;
      this.onPress.onPress(this);
   }

   public void setSelected(boolean selected) {
      this.isSelected = selected;
   }

   public void renderTooltips(Consumer<ItemStack> consumer) {
      if (this.isHovered() && !this.stack.isEmpty()) {
         consumer.accept(this.stack);
      }
   }
}
