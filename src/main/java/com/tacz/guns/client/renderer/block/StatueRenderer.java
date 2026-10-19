package com.tacz.guns.client.renderer.block;

import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.config.client.RenderConfig;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class StatueRenderer implements BlockEntityRenderer<StatueBlockEntity, StatueRenderer.State> {
   public static class State extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
      public StatueBlockEntity entity;
      public float partialTick;
   }

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public void extractRenderState(StatueBlockEntity entity, State state, float partialTick, Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
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
   public StatueRenderer(Context context) {
   }

   public static Optional<BedrockModel> getModel() {
      return InternalAssetLoader.getBedrockModel(InternalAssetLoader.STATUE_MODEL_LOCATION);
   }

   public void render(StatueBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
      getModel()
         .ifPresent(
            model -> {
               Level level = blockEntity.getLevel();
               if (level != null) {
                  poseStack.pushPose();
                  BlockState blockState = blockEntity.getBlockState();
                  Direction facing = (Direction)blockState.getValue(TargetBlock.FACING);
                  poseStack.translate(0.5, 1.5, 0.5);
                  poseStack.mulPose(Axis.YN.rotationDegrees((facing.get2DDataValue() + 2) % 4 * 90));
                  poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
                  RenderType renderType = RenderConfig.BLOCK_ENTITY_TRANSLUCENT.get()
                     ? RenderTypes.entityTranslucent(getTextureLocation())
                     : RenderTypes.entityCutout(getTextureLocation());
                  model.render(poseStack, ItemDisplayContext.NONE, renderType, combinedLightIn, combinedOverlayIn);
                  poseStack.scale(0.5F, 0.5F, 0.5F);
                  poseStack.translate(0.0, -0.875, -1.2);
                  poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                  double offset = Math.sin(Util.getMillis() / 500.0) * 0.1;
                  poseStack.translate(0.0, offset, 0.0);
                  ItemStack stack = blockEntity.getGunItem();
                  net.minecraft.client.renderer.item.ItemStackRenderState itemState = new net.minecraft.client.renderer.item.ItemStackRenderState();
                  Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, level, null, 0);
                  com.tacz.guns.client.render26.GunRender.flushNow();
                  itemState.submit(poseStack, com.tacz.guns.client.render26.GunRender.collector(), LightCoordsUtil.pack(15, 15), OverlayTexture.NO_OVERLAY, 0);
                  poseStack.popPose();
               }
            }
         );
   }

   public static Identifier getTextureLocation() {
      return InternalAssetLoader.STATUE_TEXTURE_LOCATION;
   }

   public int getViewDistance() {
      return (Integer)RenderConfig.TARGET_RENDER_DISTANCE.get();
   }

   public boolean shouldRenderOffScreen() {
      return true;
   }

   public boolean shouldRender(StatueBlockEntity pBlockEntity, Vec3 pCameraPos) {
      return Vec3.atCenterOf(pBlockEntity.getBlockPos().above()).closerThan(pCameraPos, this.getViewDistance());
   }
}
