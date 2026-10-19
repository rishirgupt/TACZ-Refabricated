package com.tacz.guns.client.renderer.block;

import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.config.client.RenderConfig;
import java.util.Optional;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class TargetRenderer implements BlockEntityRenderer<TargetBlockEntity, TargetRenderer.State> {
   public static class State extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
      public TargetBlockEntity entity;
      public float partialTick;
   }

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public void extractRenderState(TargetBlockEntity entity, State state, float partialTick, net.minecraft.world.phys.Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
      net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(entity, state, overlay);
      state.entity = entity;
      state.partialTick = partialTick;
   }

   @Override
   public void submit(State state, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
      if (state.entity == null) {
         return;
      }

      com.tacz.guns.client.render26.GunRender.begin(collector);
      this.render(state.entity, state.partialTick, poseStack, com.tacz.guns.client.render26.GunRender.bufferSource(), state.lightCoords, OverlayTexture.NO_OVERLAY);
      com.tacz.guns.client.render26.GunRender.end();
   }
   private static final String UPPER_NAME = "target_upper";
   private static final String HEAD_NAME = "head";

   public TargetRenderer(Context context) {
   }

   public static Optional<BedrockModel> getModel() {
      return InternalAssetLoader.getBedrockModel(InternalAssetLoader.TARGET_MODEL_LOCATION);
   }

   public void render(TargetBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
      getModel().ifPresent(model -> {
         BlockState blockState = blockEntity.getBlockState();
         Direction facing = (Direction)blockState.getValue(TargetBlock.FACING);
         BedrockPart headModel = model.getNode("head");
         BedrockPart upperModel = model.getNode("target_upper");
         float deg = -Mth.lerp(partialTick, blockEntity.oRot, blockEntity.rot);
         upperModel.xRot = (float)Math.toRadians(deg);
         headModel.visible = false;
         poseStack.pushPose();
         poseStack.translate(0.5, 0.225, 0.5);
         poseStack.mulPose(Axis.YN.rotationDegrees(facing.get2DDataValue() * 90));
         poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
         poseStack.translate(0.0, -1.275, 0.0125);
         RenderType renderType = RenderTypes.entityTranslucent(InternalAssetLoader.TARGET_TEXTURE_LOCATION);
         model.render(poseStack, ItemDisplayContext.NONE, renderType, combinedLightIn, combinedOverlayIn);
         if (blockEntity.getOwner() != null) {
            poseStack.translate(0.0, 1.25, 0.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(deg));
            Minecraft minecraft = Minecraft.getInstance();
            Identifier skin = com.tacz.guns.client.renderer.SkinHelper.skin(blockEntity.getOwner());
            headModel.visible = true;
            RenderType skullRenderType = RenderTypes.entityCutout(skin);
            headModel.render(poseStack, ItemDisplayContext.NONE, bufferIn.getBuffer(skullRenderType), combinedLightIn, OverlayTexture.NO_OVERLAY);
         }

         poseStack.popPose();
      });
   }

   public int getViewDistance() {
      return (Integer)RenderConfig.TARGET_RENDER_DISTANCE.get();
   }

   public boolean shouldRenderOffScreen() {
      return true;
   }
}
