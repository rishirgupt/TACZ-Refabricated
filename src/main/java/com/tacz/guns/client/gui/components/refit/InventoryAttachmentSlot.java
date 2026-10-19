package com.tacz.guns.client.gui.components.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.client.gui.GunRefitScreen;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;

public class InventoryAttachmentSlot extends Button implements IStackTooltip {
   private final int slotIndex;
   private final Inventory inventory;

   public InventoryAttachmentSlot(int pX, int pY, int slotIndex, Inventory inventory, OnPress onPress) {
      super(pX, pY, 18, 18, Component.empty(), onPress, DEFAULT_NARRATION);
      this.slotIndex = slotIndex;
      this.inventory = inventory;
   }

   @Override
   public void renderTooltip(Consumer<ItemStack> consumer) {
      if (this.isHoveredOrFocused() && 0 <= this.slotIndex && this.slotIndex < this.inventory.getContainerSize()) {
         ItemStack item = this.inventory.getItem(this.slotIndex);
         consumer.accept(item);
      }
   }

   public void extractContents(@Nonnull GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
      int x = this.getX();
      int y = this.getY();
      if (this.isHoveredOrFocused()) {
         graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GunRefitScreen.SLOT_TEXTURE, x, y, 0.0F, 0.0F, this.width, this.height, 18, 18);
      } else {
         graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GunRefitScreen.SLOT_TEXTURE, x + 1, y + 1, 1.0F, 1.0F, this.width - 2, this.height - 2, 18, 18);
      }

      graphics.item(this.inventory.getItem(this.slotIndex), x + 1, y + 1);
   }

   public int getSlotIndex() {
      return this.slotIndex;
   }
}
