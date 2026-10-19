package com.tacz.guns.client.tooltip;

import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class ClientAmmoBoxTooltip implements ClientTooltipComponent {
   private final ItemStack ammo;
   private final Component count;
   private final Component ammoName;

   public ClientAmmoBoxTooltip(AmmoBoxTooltip tooltip) {
      this.ammo = tooltip.getAmmo();
      ItemStack ammoBox = tooltip.getAmmoBox();
      if (ammoBox.getItem() instanceof IAmmoBox box && box.isCreative(ammoBox)) {
         this.count = Component.literal("∞");
      } else {
         this.count = Component.translatable("tooltip.tacz.ammo_box.count", new Object[]{tooltip.getCount()});
      }

      this.ammoName = this.ammo.getHoverName();
   }

   public int getHeight(Font font) {
      return 28;
   }

   public int getWidth(Font font) {
      return Math.max(font.width(this.ammoName), font.width(this.count)) + 22;
   }

   public void extractText(GuiGraphicsExtractor textGraphics, Font font, int pX, int pY) {
      textGraphics.text(font, this.ammoName, pX + 20, pY + 4, (0xFF000000 | 16755200), false);
      textGraphics.text(font, this.count, pX + 20, pY + 15, (0xFF000000 | 6710886), false);
   }

   public void extractImage(Font pFont, int pX, int pY, int imageWidth, int imageHeight, GuiGraphicsExtractor pGuiGraphics) {
      pGuiGraphics.item(this.ammo, pX, pY + 5);
   }
}
