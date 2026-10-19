package com.tacz.guns.client.tooltip;

import com.google.common.collect.Lists;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.inventory.tooltip.BlockItemTooltip;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.Nullable;

public class ClientBlockItemTooltip implements ClientTooltipComponent {
   private final Identifier blockId;
   private final List<Component> components = Lists.newArrayList();
   @Nullable
   private MutableComponent packInfo;

   public ClientBlockItemTooltip(BlockItemTooltip tooltip) {
      this.blockId = tooltip.getBlockId();
      this.addText();
      this.addPackInfo();
   }

   private void addPackInfo() {
      PackInfo packInfoObject = ClientAssetsManager.INSTANCE.getPackInfo(this.blockId);
      if (packInfoObject != null) {
         this.packInfo = Component.translatable(packInfoObject.getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
      }
   }

   public int getHeight(Font font) {
      return this.components.size() * 10 + (this.packInfo != null ? 16 : 0);
   }

   public int getWidth(Font font) {
      int[] width = new int[]{0};
      if (this.packInfo != null) {
         width[0] = Math.max(width[0], font.width(this.packInfo) + 4);
      }

      this.components.forEach(c -> width[0] = Math.max(width[0], font.width(c)));
      return width[0];
   }

   public void extractText(GuiGraphicsExtractor textGraphics, Font font, int pX, int pY) {
      int yOffset = pY;

      for (Component component : this.components) {
         textGraphics.text(font, component, pX, yOffset, (0xFF000000 | 16755200), false);
         yOffset += 10;
      }

      if (this.packInfo != null) {
         textGraphics.text(font, this.packInfo, pX, yOffset + 6, (0xFF000000 | 16777215), false);
      }
   }

   public void extractImage(Font font, int mouseX, int mouseY, int imageWidth, int imageHeight, GuiGraphicsExtractor gui) {
   }

   private void addText() {
      TimelessAPI.getClientBlockIndex(this.blockId).ifPresent(index -> {
         String tooltipKey = index.getTooltipKey();
         if (tooltipKey != null) {
            String text = I18n.get(tooltipKey, new Object[0]);
            String[] split = text.split("\n");
            Arrays.stream(split).forEach(s -> this.components.add(Component.literal(s).withStyle(ChatFormatting.GRAY)));
         }
      });
   }
}
