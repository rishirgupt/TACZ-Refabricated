package com.tacz.guns.client.gui.toast;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GunLevelUpToast implements Toast {
   private final Component title;
   private final Component subTitle;
   private final ItemStack icon;

   public GunLevelUpToast(ItemStack icon, Component titleComponent, @Nullable Component subtitle) {
      this.icon = icon;
      this.title = titleComponent;
      this.subTitle = subtitle;
   }

      private Visibility wantedVisibility = Visibility.SHOW;

   @Override
   public Visibility getWantedVisibility() {
      return this.wantedVisibility;
   }

   @Override
   public void update(net.minecraft.client.gui.components.toasts.ToastManager manager, long fullyVisibleForMs) {
      this.wantedVisibility = fullyVisibleForMs >= 5000L ? Visibility.HIDE : Visibility.SHOW;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, long fullyVisibleForMs) {
   }
}
