package com.tacz.guns.client.gui.overlay;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.client.RenderConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.DeltaTracker;
import org.jetbrains.annotations.NotNull;

public class KillAmountOverlay {
   private static long killTimestamp = -1L;
   private static int killAmount = 0;
   public static final KillAmountOverlay INSTANCE = new KillAmountOverlay();

   public void render(GuiGraphicsExtractor graphics, @NotNull DeltaTracker deltaTracker) {
      int width = graphics.guiWidth();
      int height = graphics.guiHeight();
      if ((Boolean)RenderConfig.KILL_AMOUNT_ENABLE.get()) {
         int timeout = (int)((Double)RenderConfig.KILL_AMOUNT_DURATION_SECOND.get() * 1000.0);
         float colorCount = 30.0F;
         long remainTime = System.currentTimeMillis() - killTimestamp;
         if (remainTime <= timeout) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player instanceof IClientPlayerGunOperator) {
               ItemStack stack = player.getMainHandItem();
               if (stack.getItem() instanceof IGun) {
                  String text;
                  if (killAmount < 10) {
                     text = "☠ x 0" + killAmount;
                  } else {
                     text = "☠ x " + killAmount;
                  }

                  int fontWith = mc.font.width(text);
                  double fadeOutTime = timeout / 3.0 * 2.0;
                  float hue = (1.0F - Math.min(killAmount / colorCount, 1.0F)) * 0.15F;
                  int alpha = 255;
                  if (remainTime > fadeOutTime) {
                     alpha = 255 - (int)((remainTime - fadeOutTime) / (timeout - fadeOutTime) * 240.0);
                  }

                  int color = Mth.hsvToRgb(hue, 0.75F, 1.0F) + (alpha << 24);
                  org.joml.Matrix3x2fStack poseStack = graphics.pose();
                  poseStack.pushMatrix();
                  poseStack.scale(0.5F, 0.5F);
                  graphics.text(mc.font, text, (int)(width - fontWith / 2.0F), (height - 45) * 2 - 1, color);
                  poseStack.popMatrix();
               }
            }
         }
      }
   }

   public static void markTimestamp() {
      int timeout = (int)((Double)RenderConfig.KILL_AMOUNT_DURATION_SECOND.get() * 1000.0);
      if (System.currentTimeMillis() - killTimestamp > timeout) {
         killAmount = 0;
      }

      killTimestamp = System.currentTimeMillis();
      killAmount++;
   }
}
