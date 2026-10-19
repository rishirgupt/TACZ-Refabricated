package com.tacz.guns.client.gui.overlay;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunHeatData;
import java.text.DecimalFormat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

public class HeatBarOverlay {
   private static final Identifier HEATBASE = Identifier.fromNamespaceAndPath("tacz", "textures/hud/heat_base.png");
   private static final DecimalFormat HEAT_FORMAT_PERCENT = new DecimalFormat("0.0%");
   private static float heatScale = 0.25F;
   public static final HeatBarOverlay INSTANCE = new HeatBarOverlay();

   public void render(GuiGraphicsExtractor graphics, @NotNull DeltaTracker deltaTracker) {
      int width = graphics.guiWidth();
      int height = graphics.guiHeight();
      if ((Boolean)RenderConfig.GUN_HUD_ENABLE.get()) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player instanceof IClientPlayerGunOperator) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof IGun iGun) {
               Identifier gunId = iGun.getGunId(stack);
               GunData gunData = TimelessAPI.getClientGunIndex(gunId).map(ClientGunIndex::getGunData).orElse(null);
               GunDisplayInstance display = TimelessAPI.getGunDisplay(stack).orElse(null);
               if (gunData != null && display != null) {
                  org.joml.Matrix3x2fStack poseStack = graphics.pose();
                  if (gunData.getHeatData() != null && iGun.hasHeatData(stack)) {
                     poseStack.pushMatrix();
                     GunHeatData heatData = gunData.getHeatData();
                     float percent = iGun.getHeatAmount(stack) / heatData.getHeatMax();
                     float scaleValue = iGun.getHeatAmount(stack) / heatData.getHeatMax() / 8.0F + 0.75F;
                     if (heatScale < scaleValue) {
                        heatScale += 0.05F;
                     }

                     if (heatScale > scaleValue) {
                        heatScale -= 0.025F;
                     }

                     if (heatScale > scaleValue - 0.03 && heatScale < scaleValue + 0.055) {
                        heatScale = scaleValue;
                     }

                     poseStack.scale(heatScale, heatScale);
                     boolean locked = iGun.isOverheatLocked(stack);
                     int tickCount = player.tickCount;
                     renderOverheat(percent, graphics, (int)(width / heatScale), (int)(height / heatScale), locked, tickCount);
                     poseStack.popMatrix();
                  }
               }
            }
         }
      }
   }

   public static void renderOverheat(float heatPercentage, GuiGraphicsExtractor pGraphics, int w, int h, boolean locked, int tickCount) {
      int barColor = getHeatColor(heatPercentage, locked, tickCount);
      pGraphics.fill(w / 2 - 30, h / 2 + 30, w / 2 - 30 + (int)(heatPercentage * 60.0F), h / 2 + 34, barColor);
      int baseColor = -1;
      if (locked) {
         baseColor = tickCount % 20 < 10 ? 0xFFFF1A1A : 0xFFFFFF1A;
      }

      pGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, HEATBASE, w / 2 - 64, h / 2 - 44, 0.0F, 0.0F, 128, 128, 128, 128, baseColor);
      Font font = Minecraft.getInstance().font;
      String percentString = locked ? "!OVERHEAT!" : HEAT_FORMAT_PERCENT.format(heatPercentage);
      int color = locked ? (tickCount % 20 < 10 ? -65536 : -256) : -1;
      pGraphics.text(font, percentString, w / 2 - font.width(percentString) / 2, h / 2 + 38, color, true);
   }

   public static int getHeatColor(float percent, boolean locked, int tickCount) {
      if (locked) {
         return tickCount % 20 < 10 ? -1610678272 : -1610612992;
      }

      if (percent < 0.4) {
         return -1610612737;
      }

      int color;
      if (percent <= 0.65) {
         color = ARGB.srgbLerp(percent * 4.0F - 1.6F, -1610612737, -1610612992);
      } else {
         color = ARGB.srgbLerp((percent - 0.65F) / 0.35F, -1610612992, -1610678272);
      }

      return color;
   }
}
