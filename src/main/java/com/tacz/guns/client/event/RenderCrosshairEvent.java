package com.tacz.guns.client.event;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import net.minecraft.client.renderer.RenderPipelines;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.client.renderer.crosshair.CrosshairType;
import com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat;
import com.tacz.guns.config.client.RenderConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.DeltaTracker;

@Environment(EnvType.CLIENT)
public class RenderCrosshairEvent {
   private static final Identifier HIT_ICON = Identifier.fromNamespaceAndPath("tacz", "textures/crosshair/hit/hit_marker.png");
   private static final long KEEP_TIME = 300L;
   private static boolean isRefitScreen = false;
   private static long hitTimestamp = -1L;
   private static long killTimestamp = -1L;
   private static long headShotTimestamp = -1L;

   public static void onRenderOverlay(GuiGraphicsExtractor guiGraphics, Window window, DeltaTracker deltaTracker, java.util.concurrent.atomic.AtomicBoolean cancelled) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         if (IGun.mainHandHoldGun(player)) {
            cancelled.set(true);
            renderHitMarker(guiGraphics, window);
            ReloadState reloadState = IGunOperator.fromLivingEntity(player).getSynReloadState();
            if (!reloadState.getStateType().isReloading()) {
               if (!isRefitScreen) {
                  ItemStack stack = player.getMainHandItem();
                  if (stack.getItem() instanceof IGun) {
                     IClientPlayerGunOperator playerGunOperator = IClientPlayerGunOperator.fromLocalPlayer(player);
                     TimelessAPI.getGunDisplay(stack).ifPresent(gunIndex -> {
                        if (playerGunOperator.getClientAimingProgress(deltaTracker.getGameTimeDeltaPartialTick(false)) > 0.9) {
                           boolean forceShow = gunIndex.isShowCrosshair();
                           boolean shoulderSurfingForceShow = ShoulderSurfingCompat.showCrosshair();
                           if (!forceShow && !shoulderSurfingForceShow) {
                              return;
                           }
                        }

                        AnimationStateMachine<?> animationStateMachine = gunIndex.getAnimationStateMachine();
                        if (animationStateMachine == null) {
                           renderCrosshair(guiGraphics, window);
                        } else {
                           AnimationStateContext context = animationStateMachine.getContext();
                           if (context == null || !context.shouldHideCrossHair()) {
                              renderCrosshair(guiGraphics, window);
                           }
                        }
                     });
                  }
               }
            }
         }
      }
   }

   public static void onRenderTick(RenderTickEvent event) {
      isRefitScreen = Minecraft.getInstance().gui.screen() instanceof GunRefitScreen;
   }

   private static void renderCrosshair(GuiGraphicsExtractor graphics, Window window) {
      Options options = Minecraft.getInstance().options;
      boolean shoulderSurfingForceShow = ShoulderSurfingCompat.showCrosshair();
      if (options.getCameraType().isFirstPerson() || shoulderSurfingForceShow) {
         if (true) {
            MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
            if (gameMode != null) {
               if (gameMode.getPlayerMode() != GameType.SPECTATOR) {
                  int width = window.getGuiScaledWidth();
                  int height = window.getGuiScaledHeight();
                  Identifier location = CrosshairType.getTextureLocation((CrosshairType)RenderConfig.CROSSHAIR_TYPE.get());
                  float x = width / 2.0F - 8.0F;
                  float y = height / 2.0F - 8.0F;
                  graphics.blit(RenderPipelines.GUI_TEXTURED, location, (int)x, (int)y, 0.0F, 0.0F, 16, 16, 16, 16, 0xE6FFFFFF);
               }
            }
         }
      }
   }

   private static void renderHitMarker(GuiGraphicsExtractor graphics, Window window) {
      long remainHitTime = System.currentTimeMillis() - hitTimestamp;
      long remainKillTime = System.currentTimeMillis() - killTimestamp;
      long remainHeadShotTime = System.currentTimeMillis() - headShotTimestamp;
      float offset = ((Double)RenderConfig.HIT_MARKET_START_POSITION.get()).floatValue();
      float fadeTime;
      if (remainKillTime > 300L) {
         if (remainHitTime > 300L) {
            return;
         }

         fadeTime = (float)remainHitTime;
      } else {
         offset += (float)remainKillTime * 4.0F / 300.0F;
         fadeTime = (float)remainKillTime;
      }

      int width = window.getGuiScaledWidth();
      int height = window.getGuiScaledHeight();
      float x = width / 2.0F - 8.0F;
      float y = height / 2.0F - 8.0F;
      int alpha = Math.max(0, Math.min(255, (int)((1.0F - fadeTime / 300.0F) * 255.0F)));
      int color = remainHeadShotTime > 300L ? (alpha << 24) | 0xFFFFFF : (alpha << 24) | 0xFF0000;

      graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int)(x - offset), (int)(y - offset), 0.0F, 0.0F, 8, 8, 16, 16, color);
      graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int)(x + 8.0F + offset), (int)(y - offset), 8.0F, 0.0F, 8, 8, 16, 16, color);
      graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int)(x - offset), (int)(y + 8.0F + offset), 0.0F, 8.0F, 8, 8, 16, 16, color);
      graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int)(x + 8.0F + offset), (int)(y + 8.0F + offset), 8.0F, 8.0F, 8, 8, 16, 16, color);
   }

   public static void markHitTimestamp() {
      hitTimestamp = System.currentTimeMillis();
   }

   public static void markKillTimestamp() {
      killTimestamp = System.currentTimeMillis();
   }

   public static void markHeadShotTimestamp() {
      headShotTimestamp = System.currentTimeMillis();
   }
}
