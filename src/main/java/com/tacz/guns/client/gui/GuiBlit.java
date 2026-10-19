package com.tacz.guns.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Keeps the old blit argument order (x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight). */
public final class GuiBlit {
   private GuiBlit() {
   }

   public static void blit(GuiGraphicsExtractor graphics, Identifier texture, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, float textureWidth, float textureHeight) {
      graphics.blit(RenderPipelines.GUI_TEXTURED, texture, Math.round(x), Math.round(y), u, v, Math.round(width), Math.round(height), Math.round(uWidth), Math.round(vHeight), Math.round(textureWidth), Math.round(textureHeight));
   }
}
