package com.tacz.guns.client.model.functional;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import com.tacz.guns.compat.ar.ARCompat;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.util.LaserColorUtil;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.tacz.guns.client.render26.MultiBufferSource;

public class BeamRenderer {
   public static final Identifier LASER_BEAM_TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/entity/beam.png");
   private static final LaserConfig DEFAULT_LASER_CONFIG = new LaserConfig();

   public static void renderLaserBeam(ItemStack stack, PoseStack poseStack, ItemDisplayContext transformType, @Nonnull List<BedrockPart> path) {
      if (stack != null && (transformType.firstPerson() || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
         if (!ARCompat.shouldAccelerate() || !renderLaserBeamAccelerated(stack, poseStack, transformType, path)) {
            MultiBufferSource bufferSource = com.tacz.guns.client.render26.GunRender.bufferSource();
            VertexConsumer builder = bufferSource.getBuffer(BeamRenderer.LaserBeamRenderState.getLaserBeam());
            poseStack.pushPose();

            for (int i = 0; i < path.size(); i++) {
               path.get(i).translateAndRotateAndScale(poseStack);
            }

            LaserConfig laserConfig = getLaserConfig(stack);
            int color = LaserColorUtil.getLaserColor(stack, laserConfig);
            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;
            stringVertex(
               transformType.firstPerson() ? -laserConfig.getLength() : -laserConfig.getLengthThird(),
               transformType.firstPerson() ? laserConfig.getWidth() : laserConfig.getWidthThird(),
               builder,
               poseStack.last(),
               r,
               g,
               b,
               (Boolean)RenderConfig.ENABLE_LASER_FADE_OUT.get()
            );
            poseStack.popPose();
         }
      }
   }

   public static boolean renderLaserBeamAccelerated(ItemStack stack, PoseStack poseStack, ItemDisplayContext transformType, @Nonnull List<BedrockPart> path) {
      VertexConsumer builder = com.tacz.guns.client.render26.GunRender.bufferSource().getBuffer(BeamRenderer.LaserBeamRenderState.getLaserBeamEntity());
      if (!ARCompat.isAccelerated(builder)) {
         return false;
      }

      poseStack.pushPose();

      for (int i = 0; i < path.size(); i++) {
         path.get(i).translateAndRotateAndScale(poseStack);
      }

      LaserConfig laserConfig = getLaserConfig(stack);
      ARCompat.renderLaser(
         builder,
         transformType.firstPerson() ? -laserConfig.getLength() : -laserConfig.getLengthThird(),
         transformType.firstPerson() ? laserConfig.getWidth() : laserConfig.getWidthThird(),
         (Boolean)RenderConfig.ENABLE_LASER_FADE_OUT.get(),
         poseStack,
         0xFF000000 | LaserColorUtil.getLaserColor(stack, laserConfig) & 16777215
      );
      poseStack.popPose();
      return true;
   }

   private static LaserConfig getLaserConfig(ItemStack stack) {
      if (stack == null) {
         return DEFAULT_LASER_CONFIG;
      } else if (stack.getItem() instanceof IAttachment iAttachment) {
         return TimelessAPI.getClientAttachmentIndex(iAttachment.getAttachmentId(stack))
            .map(ClientAttachmentIndex::getLaserConfig)
            .orElse(DEFAULT_LASER_CONFIG);
      } else {
         return stack.getItem() instanceof IGun
            ? TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getLaserConfig).orElse(DEFAULT_LASER_CONFIG)
            : DEFAULT_LASER_CONFIG;
      }
   }

   private static void stringVertex(float z, float width, VertexConsumer pConsumer, Pose pPose, int r, int g, int b, boolean fadeOut) {
      float halfWidth = width / 2.0F;
      int endAlpha = fadeOut ? 0 : 255;
      int light = LightCoordsUtil.pack(15, 15);
      pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 0.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 0.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 0.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 0.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 0.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 0.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, 0.0F).setColor(r, g, b, 255).setUv(0.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 1.0F).setLight(light);
      pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1.0F, 0.0F).setLight(light);
   }

   public static class LaserBeamRenderState {
      public static RenderType getLaserBeam() {
         return com.tacz.guns.client.render26.ModRenderTypes.additive(BeamRenderer.LASER_BEAM_TEXTURE);
      }

      public static RenderType getLaserBeamEntity() {
         return getLaserBeam();
      }
   }
}
