package com.tacz.guns.compat.cloth.widget;

import com.tacz.guns.client.renderer.crosshair.CrosshairType;
import java.util.function.Function;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.DefaultSelectionCellCreator;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.DefaultSelectionCellElement;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.DefaultSelectionTopCellElement;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.SelectionCellCreator;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.SelectionCellElement;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.SelectionTopCellElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class CrosshairDropdown {
   public static SelectionTopCellElement<CrosshairType> of(CrosshairType type) {
      return new DefaultSelectionTopCellElement<CrosshairType>(type, name -> {
         for (CrosshairType crosshairType : CrosshairType.values()) {
            if (crosshairType.name().equals(name)) {
               return crosshairType;
            }
         }

         return null;
      }, id -> Component.literal(id.toString())) {
         public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
            this.textFieldWidget.setX(x + 4);
            this.textFieldWidget.setY(y + 6);
            this.textFieldWidget.setWidth(width - 4 - 20);
            this.textFieldWidget.setEditable(this.getParent().isEditable());
            this.textFieldWidget.setTextColor(this.getPreferredTextColor());
            this.textFieldWidget.extractRenderState(graphics, mouseX, mouseY, delta);
            Identifier location = CrosshairType.getTextureLocation((CrosshairType)this.value);
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, location, x + width - 18, y + 2, 0.0F, 0.0F, 16, 16, 16, 16);
         }
      };
   }

   public static SelectionCellCreator<CrosshairType> of() {
      return new DefaultSelectionCellCreator<CrosshairType>(i -> Component.literal(i.name())) {
         public SelectionCellElement<CrosshairType> create(CrosshairType selection) {
            return new DefaultSelectionCellElement<CrosshairType>(selection, this.toTextFunction) {
               public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                  this.rendering = true;
                  this.x = x;
                  this.y = y;
                  this.width = width;
                  this.height = height;
                  boolean isHover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                  if (isHover) {
                     graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                  }

                  FormattedCharSequence text = ((Component)this.toTextFunction.apply((CrosshairType)this.r)).getVisualOrderText();
                  int color = isHover ? 0xFFFFFFFF : 0xFF888888;
                  graphics.text(Minecraft.getInstance().font, text, x + 6 + 18, y + 6, color, false);
                  Identifier location = CrosshairType.getTextureLocation((CrosshairType)this.r);
                  graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, location, x + 4, y + 2, 0.0F, 0.0F, 16, 16, 16, 16);
               }
            };
         }

         public int getCellHeight() {
            return 20;
         }

         public int getCellWidth() {
            return 146;
         }

         public int getDropBoxMaxHeight() {
            return this.getCellHeight() * CrosshairType.values().length;
         }
      };
   }
}
