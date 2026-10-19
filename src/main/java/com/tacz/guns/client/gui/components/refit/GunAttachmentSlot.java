package com.tacz.guns.client.gui.components.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.gui.GunRefitScreen;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import org.jetbrains.annotations.NotNull;

public class GunAttachmentSlot extends Button implements IStackTooltip {
   private final AttachmentType type;
   private final Inventory inventory;
   private final int gunItemIndex;
   private final String nameKey;
   private boolean selected = false;
   private ItemStack attachmentItem = ItemStack.EMPTY;

   public GunAttachmentSlot(int pX, int pY, AttachmentType type, int gunItemIndex, Inventory inventory, OnPress onPress) {
      super(pX, pY, 18, 18, Component.empty(), onPress, Button.DEFAULT_NARRATION);
      this.type = type;
      this.inventory = inventory;
      this.gunItemIndex = gunItemIndex;
      this.nameKey = String.format("tooltip.tacz.attachment.%s", type.name().toLowerCase(Locale.US));
   }

   @Override
   public void renderTooltip(Consumer<ItemStack> consumer) {
      if (this.isHoveredOrFocused() && !this.attachmentItem.isEmpty()) {
         consumer.accept(this.attachmentItem);
      }
   }

   public void extractContents(@NotNull GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.isHoveredOrFocused()) {
         Font font = Minecraft.getInstance().font;
         int yOffset = this.getY() + 20;
         if (this.selected && !this.attachmentItem.isEmpty()) {
            yOffset = this.getY() + 30;
         }

         graphics.centeredText(
            font, Component.translatable(this.nameKey), this.getX() + this.getWidth() / 2, yOffset, 0xFFFFFFFF
         );
      }

      ItemStack gunItem = this.inventory.getItem(this.gunItemIndex);
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun != null) {
         int x = this.getX();
         int y = this.getY();
         if (!this.isHoveredOrFocused() && !this.selected) {
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GunRefitScreen.SLOT_TEXTURE, x + 1, y + 1, 1.0F, 1.0F, this.width - 2, this.height - 2, 18, 18);
         } else {
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GunRefitScreen.SLOT_TEXTURE, x, y, 0.0F, 0.0F, this.width, this.height, 18, 18);
         }

         this.attachmentItem = iGun.getAttachment(Minecraft.getInstance().level.registryAccess(), gunItem, this.type);
         if (!this.attachmentItem.isEmpty()) {
            graphics.item(this.attachmentItem, x + 1, y + 1);
         } else {
            int xOffset = GunRefitScreen.getSlotTextureXOffset(gunItem, this.type);
            com.tacz.guns.client.gui.GuiBlit.blit(graphics, 
               GunRefitScreen.ICONS_TEXTURE,
               x + 2,
               y + 2,
               this.width - 4,
               this.height - 4,
               xOffset,
               0.0F,
               32,
               32,
               GunRefitScreen.getSlotsTextureWidth(),
               32
            );
         }
      }
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   public AttachmentType getType() {
      return this.type;
   }

   public ItemStack getAttachmentItem() {
      ItemStack gunItem = this.inventory.getItem(this.gunItemIndex);
      IGun iGun = IGun.getIGunOrNull(gunItem);
      return iGun == null ? ItemStack.EMPTY : iGun.getAttachment(Minecraft.getInstance().level.registryAccess(), gunItem, this.type);
   }

   public boolean isAllow() {
      ItemStack gunItem = this.inventory.getItem(this.gunItemIndex);
      IGun iGun = IGun.getIGunOrNull(gunItem);
      return iGun == null ? false : iGun.allowAttachmentType(gunItem, this.type);
   }
}
