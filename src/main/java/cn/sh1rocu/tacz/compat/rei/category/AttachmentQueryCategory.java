package cn.sh1rocu.tacz.compat.rei.category;

import cn.sh1rocu.tacz.compat.rei.REIClientPlugin;
import cn.sh1rocu.tacz.compat.rei.display.AttachmentQueryDisplay;
import com.tacz.guns.init.ModCreativeTabs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class AttachmentQueryCategory implements DisplayCategory<AttachmentQueryDisplay> {
   public static final int MAX_GUN_SHOW_COUNT = 60;
   private static final Component TITLE = Component.translatable("jei.tacz.attachment_query.title");
   private final Renderer icon = EntryStacks.of(ModCreativeTabs.ATTACHMENT_SCOPE_TAB.getIconItem());

   public List<Widget> setupDisplay(AttachmentQueryDisplay display, Rectangle bounds) {
      List<EntryIngredient> inputs = display.getInputEntries();
      List<EntryIngredient> outputs = display.getOutputEntries();
      List<Widget> widgets = new ArrayList<>();
      int startX = bounds.x + 5;
      int startY = bounds.y + 5;
      widgets.add(Widgets.createRecipeBase(bounds));
      widgets.add(Widgets.createSlot(new Point(startX + 72, startY)).entries((Collection)outputs.get(0)));
      int xOffset = 0;
      int yOffset = 20;

      for (int i = 0; i < Math.min(inputs.size(), 60); i++) {
         int column = i % 9;
         int row = i / 9;
         xOffset = column * 18;
         yOffset = 20 + row * 18;
         widgets.add(Widgets.createSlot(new Point(startX + xOffset, startY + yOffset)).entries((Collection)inputs.get(i)));
      }

      if (inputs.size() > 60) {
         Font font = Minecraft.getInstance().font;
         widgets.add(
            Widgets.createDrawableWidget(
               (guiGraphics, mouseX, mouseY, delta) -> guiGraphics.drawString(
                  font, Component.translatable("jei.tacz.attachment_query.more"), startX + 128, startY + 134, 5592405, false
               )
            )
         );
         widgets.add(Widgets.createSlot(new Point(startX + xOffset + 18, startY + yOffset)).entries((Collection)inputs.get(inputs.size() - 1)));
      }

      return widgets;
   }

   public CategoryIdentifier<? extends AttachmentQueryDisplay> getCategoryIdentifier() {
      return REIClientPlugin.ATTACHMENT_QUERY;
   }

   public Component getTitle() {
      return TITLE;
   }

   public Renderer getIcon() {
      return this.icon;
   }

   public int getDisplayHeight() {
      return 145;
   }

   public int getDisplayWidth(AttachmentQueryDisplay display) {
      return 170;
   }
}
