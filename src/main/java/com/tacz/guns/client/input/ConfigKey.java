package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.init.CompatRegistry;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.player.LocalPlayer;


import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class ConfigKey {
   public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping("key.tacz.open_config.desc", Type.KEYSYM, 84, KeyUtil.CATEGORY);

   public static void onOpenConfig(InputEvent.Key event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matches(OPEN_CONFIG_KEY, event.getKey(), event.getScanCode())) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return;
         }

         if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            ClickEvent clickEvent = new ClickEvent.OpenUrl(java.net.URI.create("https://www.curseforge.com/minecraft/mc-mods/cloth-config"));
            HoverEvent hoverEvent = new HoverEvent.ShowText(Component.translatable("gui.tacz.cloth_config_warning.download"));
            MutableComponent component = Component.translatable("gui.tacz.cloth_config_warning.tips")
               .withStyle(
                  style -> style.applyFormat(ChatFormatting.BLUE).applyFormat(ChatFormatting.UNDERLINE).withClickEvent(clickEvent).withHoverEvent(hoverEvent)
               );
            player.sendSystemMessage(component);
         } else {
            CompatRegistry.checkModLoad("cloth-config", () -> {
               try {
                  Class<?> clazz = Class.forName("com.tacz.guns.compat.cloth.MenuIntegration");
                  Object screen = clazz.getMethod("getConfigScreen", net.minecraft.client.gui.screens.Screen.class).invoke(null, (Object)null);
                  Minecraft.getInstance().gui.setScreen((net.minecraft.client.gui.screens.Screen)screen);
               } catch (ReflectiveOperationException e) {
                  player.sendSystemMessage(Component.literal("Cloth Config integration is not available in this build."));
               }
            });
         }
      }
   }
}
