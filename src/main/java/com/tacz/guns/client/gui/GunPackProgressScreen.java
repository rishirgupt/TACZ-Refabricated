package com.tacz.guns.client.gui;

import javax.annotation.Nullable;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.GameNarrator;
import net.minecraft.util.ProgressListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class GunPackProgressScreen extends Screen implements ProgressListener {
   @Nullable
   private Component header;
   @Nullable
   private Component stage;
   private int progress;
   private boolean stop;

   public GunPackProgressScreen() {
      super(GameNarrator.NO_TITLE);
   }

   protected void init() {
      Button button = Button.builder(Component.translatable("gui.tacz.client_gun_pack_downloader.background_download"), b -> this.stop())
         .bounds((this.width - 200) / 2, 120, 200, 20)
         .build();
      this.addRenderableWidget(button);
   }

   public void render(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
      if (this.stop) {
         net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);
      } else {
         this.extractBackground(gui, mouseX, mouseY, partialTick);
         if (this.header != null) {
            gui.centeredText(this.font, this.header, this.width / 2, 70, (0xFF000000 | 16777215));
         }

         if (this.stage != null && this.progress > 0) {
            MutableComponent text = this.stage.copy().append(" " + this.progress + "%");
            gui.centeredText(this.font, text, this.width / 2, 90, (0xFF000000 | 16777215));
         }

         super.extractRenderState(gui, mouseX, mouseY, partialTick);
      }
   }

   public void progressStartNoAbort(Component component) {
      this.progressStart(component);
   }

   public void progressStart(Component header) {
      this.header = Component.translatable("gui.tacz.client_gun_pack_downloader.downloading");
   }

   public void progressStage(Component component) {
      this.stage = component;
      this.progressStagePercentage(0);
   }

   public void progressStagePercentage(int progress) {
      this.progress = progress;
   }

   public void stop() {
      this.stop = true;
   }
}
