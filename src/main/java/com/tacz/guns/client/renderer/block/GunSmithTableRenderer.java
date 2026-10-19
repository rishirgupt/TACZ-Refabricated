package com.tacz.guns.client.renderer.block;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IBlock;
import com.tacz.guns.block.AbstractGunSmithTableBlock;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import com.tacz.guns.config.client.RenderConfig;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.MultiBufferSource;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class GunSmithTableRenderer implements BlockEntityRenderer<GunSmithTableBlockEntity, GunSmithTableRenderer.State> {
   public static class State extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
      public GunSmithTableBlockEntity entity;
      public float partialTick;
   }

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public void extractRenderState(GunSmithTableBlockEntity entity, State state, float partialTick, net.minecraft.world.phys.Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
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
      this.render(state.entity, state.partialTick, poseStack, com.tacz.guns.client.render26.GunRender.bufferSource(), state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
      com.tacz.guns.client.render26.GunRender.end();
   }
   public GunSmithTableRenderer(Context context) {
   }

   public Optional<ClientBlockIndex> getIndex(GunSmithTableBlockEntity blockEntity) {
      Identifier id = blockEntity.getId();
      return id != null && !id.equals(DefaultAssets.EMPTY_BLOCK_ID) ? TimelessAPI.getClientBlockIndex(id) : Optional.empty();
   }

   public static Optional<ClientBlockIndex> getIndex(ItemStack stack) {
      if (stack.getItem() instanceof IBlock iBlock) {
         Identifier id = iBlock.getBlockId(stack);
         return id.equals(DefaultAssets.EMPTY_BLOCK_ID) ? Optional.empty() : TimelessAPI.getClientBlockIndex(id);
      } else {
         return Optional.empty();
      }
   }

   public void render(
      GunSmithTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      this.getIndex(blockEntity).ifPresent(index -> {
         BedrockModel model = index.getModel();
         Identifier texture = index.getTexture();
         if (model != null) {
            BlockState blockState = blockEntity.getBlockState();
            if (blockState.getBlock() instanceof AbstractGunSmithTableBlock block) {
               if (!block.isRoot(blockState)) {
                  return;
               }

               Direction facing = (Direction)blockState.getValue(AbstractGunSmithTableBlock.FACING);
               poseStack.pushPose();
               poseStack.translate(0.5, 1.5, 0.5);
               poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
               poseStack.mulPose(Axis.YN.rotationDegrees(block.parseRotation(facing)));
               RenderType renderType = RenderConfig.BLOCK_ENTITY_TRANSLUCENT.get() ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
               model.render(poseStack, ItemDisplayContext.NONE, renderType, combinedLightIn, combinedOverlayIn);
               poseStack.popPose();
            }
         }
      });
   }

   public boolean shouldRenderOffScreen() {
      return true;
   }
}
