package com.tacz.guns.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.GunRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(EnvType.CLIENT)
public final class RenderHelper {
   private RenderHelper() {
   }

   /** Draws the player's bare arm into the gun render that is currently being collected. */
   public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack poseStack, int combinedLight) {
      if (com.tacz.guns.client.gui.overlay.ScopeOverlay.shouldHideGun()) {
         return;
      }

      SubmitNodeCollector collector = GunRender.collector();
      if (collector == null) {
         return;
      }

      AvatarRenderer<AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
      Identifier skin = player.getSkin().body().texturePath();
      // Arms are submitted straight to the collector, so geometry recorded so far has to go first to keep the draw order.
      GunRender.flushNow();
      if (hand == HumanoidArm.RIGHT) {
         renderer.renderRightHand(poseStack, collector, combinedLight, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
      } else {
         renderer.renderLeftHand(poseStack, collector, combinedLight, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
      }
   }
}
