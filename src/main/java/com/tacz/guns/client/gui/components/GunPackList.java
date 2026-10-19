package com.tacz.guns.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.PackInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;

public class GunPackList extends ContainerObjectSelectionList<GunPackList.Entry> {
   private final GunSmithTableScreen parent;
   private final List<GunPackList.Checkbox> gunPackList = new ArrayList<>();
   private final Set<String> selectedNamespaces = new HashSet<>();
   private final GunPackList.Checkbox byHandCheckbox;
   private final EditBox byName;

   public GunPackList(
      Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, Map<Identifier, List<Identifier>> recipes, GunSmithTableScreen parent
   ) {
      super(pMinecraft, pWidth, pHeight, pY0, pItemHeight);
      this.parent = parent;
      Set<String> namespaces = new HashSet<>();

      for (List<Identifier> entry : recipes.values()) {
         entry.forEach(resourceLocation -> namespaces.add(resourceLocation.getNamespace()));
      }

      this.byName = new EditBox(pMinecraft.font, 3, 0, 94, 10, Component.empty());
      this.byName.setHint(Component.translatable("gui.tacz.gun_smith_table.filter.search"));
      this.byName.setResponder(pText -> {
         parent.init();
         parent.setIndexPage(0);
      });
      this.addEntry(new GunPackList.Entry(this.byName));
      this.byHandCheckbox = new GunPackList.Checkbox(0, 0, 10, 10, Component.translatable("gui.tacz.gun_smith_table.filter.handgun"), false) {
         @Override
         public void onPress(net.minecraft.client.input.InputWithModifiers input) {
            super.onPress(input);
            parent.init();
            parent.setIndexPage(0);
         }
      };
      this.addEntry(new GunPackList.Entry(this.byHandCheckbox));
      final GunPackList.Checkbox checkbox1 = new GunPackList.Checkbox(0, 0, 10, 10, Component.translatable("gui.tacz.gun_smith_table.filter.all"), true) {
         @Override
         public void onPress(net.minecraft.client.input.InputWithModifiers input) {
            super.onPress(input);
            GunPackList.this.gunPackList.forEach(checkbox -> checkbox.selected = this.selected);
            GunPackList.this.updateSelectedNamespaces();
         }
      };
      this.addEntry(new GunPackList.Entry(checkbox1));

      for (String namespace : namespaces) {
         PackInfo packInfo = ClientAssetsManager.INSTANCE.getPackInfo(namespace);
         Component name = packInfo == null ? Component.literal(namespace) : Component.translatable(packInfo.getName());
         GunPackList.Checkbox checkbox = new GunPackList.Checkbox(0, 0, 10, 10, name, namespace, true) {
            @Override
            public void onPress(net.minecraft.client.input.InputWithModifiers input) {
               super.onPress(input);
               checkbox1.selected = GunPackList.this.gunPackList.stream().allMatch(GunPackList.Checkbox::selected);
               GunPackList.this.updateSelectedNamespaces();
            }
         };
         this.gunPackList.add(checkbox);
         this.selectedNamespaces.add(namespace);
         this.addEntry(new GunPackList.Entry(checkbox));
      }
   }

   public String getSearchText() {
      return this.byName.getValue();
   }

   public boolean isByHandSelected() {
      return this.byHandCheckbox.selected;
   }

   public void setByHandSelected(boolean selected) {
      this.byHandCheckbox.selected = selected;
   }

   public Set<String> namespaceList() {
      return this.selectedNamespaces;
   }

   public void updateSelectedNamespaces() {
      this.selectedNamespaces.clear();
      this.gunPackList.forEach(checkbox -> {
         if (checkbox.selected) {
            this.selectedNamespaces.add(checkbox.getId());
         }
      });
      this.parent.init();
      this.parent.setIndexPage(0);
   }

   public int getRowLeft() {
      return this.getX() + 4;
   }

   public int getRowWidth() {
      return this.width;
   }

   public static class Checkbox extends AbstractButton {
      private static final Identifier CHECKBOX = Identifier.withDefaultNamespace("textures/gui/sprites/widget/checkbox.png");
      private static final Identifier CHECKBOX_HIGHLIGHTED = Identifier.withDefaultNamespace("textures/gui/sprites/widget/checkbox_highlighted.png");
      private static final Identifier CHECKBOX_SELECTED = Identifier.withDefaultNamespace("textures/gui/sprites/widget/checkbox_selected.png");
      private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED = Identifier.withDefaultNamespace("textures/gui/sprites/widget/checkbox_selected_highlighted.png");
      protected boolean selected;
      protected final boolean showLabel;
      private String id;

      public Checkbox(int pX, int pY, int pWidth, int pHeight, Component pMessage, String id, boolean pSelected) {
         this(pX, pY, pWidth, pHeight, pMessage, pSelected, true);
         this.id = id;
      }

      public Checkbox(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected) {
         this(pX, pY, pWidth, pHeight, pMessage, pSelected, true);
      }

      public Checkbox(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected, boolean pShowLabel) {
         super(pX, pY, pWidth, pHeight, pMessage);
         this.selected = pSelected;
         this.showLabel = pShowLabel;
      }

      public String getId() {
         return this.id;
      }

      public void onPress(net.minecraft.client.input.InputWithModifiers input) {
         this.selected = !this.selected;
      }

      public boolean selected() {
         return this.selected;
      }

      public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
         pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
         if (this.active) {
            if (this.isFocused()) {
               pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
               pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
         }
      }

      public void extractContents(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
         Minecraft minecraft = Minecraft.getInstance();
         Font font = minecraft.font;
         Identifier texture;
         if (this.isFocused()) {
            if (this.selected) {
               texture = CHECKBOX_SELECTED_HIGHLIGHTED;
            } else {
               texture = CHECKBOX_HIGHLIGHTED;
            }
         } else if (this.selected) {
            texture = CHECKBOX_SELECTED;
         } else {
            texture = CHECKBOX;
         }

         pGuiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), 0.0F, 0.0F, 10, 10, 10, 10);
         if (this.showLabel) {
            pGuiGraphics.text(
               font,
               this.getMessage(),
               this.getX() + 24,
               this.getY() + (this.height - 8) / 2,
               14737632 | Mth.ceil(this.alpha * 255.0F) << 24
            );
         }
      }
   }

   public static class Entry extends ContainerObjectSelectionList.Entry<GunPackList.Entry> {
      private final AbstractWidget widget;

      public Entry(AbstractWidget widget) {
         this.widget = widget;
      }

      public List<? extends NarratableEntry> narratables() {
         return ImmutableList.of(this.widget);
      }

      @Override
      public void extractContent(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.widget.setX(this.getContentX());
         this.widget.setY(this.getContentY());
         this.widget.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }

      public List<? extends GuiEventListener> children() {
         return ImmutableList.of(this.widget);
      }
   }
}
