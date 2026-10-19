package com.tacz.guns.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.render26.GunRender;
import com.tacz.guns.client.render26.MultiBufferSource;
import com.tacz.guns.client.renderer.SkinHelper;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.entity.TargetMinecart;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(EnvType.CLIENT)
public class TargetMinecartRenderer extends AbstractMinecartRenderer<TargetMinecart, TargetMinecartRenderer.State> {
   public static class State extends MinecartRenderState {
      public TargetMinecart minecart;
   }

   public TargetMinecartRenderer(Context ctx) {
      super(ctx, ModelLayers.TNT_MINECART);
      this.shadowRadius = 0.25F;
   }

   public static Optional<BedrockModel> getModel() {
      return InternalAssetLoader.getBedrockModel(InternalAssetLoader.TARGET_MINECART_MODEL_LOCATION);
   }

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public void extractRenderState(TargetMinecart minecart, State state, float partialTicks) {
      super.extractRenderState(minecart, state, partialTicks);
      state.minecart = minecart;
   }

   @Override
   protected void submitMinecartContents(State state, BlockModelRenderState blockModel, PoseStack poseStack, SubmitNodeCollector collector, int light) {
      if (state.minecart == null) {
         return;
      }

      GunRender.begin(collector);
      try {
         this.renderContents(state.minecart, poseStack, GunRender.bufferSource(), light);
      } finally {
         GunRender.end();
      }
   }

   private void renderContents(TargetMinecart targetMinecart, PoseStack stack, MultiBufferSource buffer, int light) {
      getModel().ifPresent(model -> {
         BedrockPart headModel = model.getNode("head");
         BedrockPart head2Model = model.getNode("head2");
         headModel.visible = false;
         head2Model.visible = false;
         stack.pushPose();
         stack.translate(0.0, 0.375, 0.0);
         stack.scale(1.5F, 1.5F, 1.5F);
         stack.mulPose(Axis.ZN.rotationDegrees(180.0F));
         stack.mulPose(Axis.YN.rotationDegrees(90.0F));
         RenderType renderType = RenderTypes.entityTranslucent(InternalAssetLoader.TARGET_MINECART_TEXTURE_LOCATION);
         model.render(stack, ItemDisplayContext.NONE, renderType, light, OverlayTexture.NO_OVERLAY);
         if (targetMinecart.getGameProfile() != null) {
            stack.translate(0.0, 1.0, -0.28125);
            Identifier skin = SkinHelper.skin(targetMinecart.getGameProfile());
            headModel.visible = true;
            RenderType skullRenderType = RenderTypes.entityTranslucent(skin);
            headModel.render(stack, ItemDisplayContext.NONE, buffer.getBuffer(skullRenderType), light, OverlayTexture.NO_OVERLAY);
            head2Model.visible = true;
            stack.translate(0.0, 0.0, 0.01);
            head2Model.render(stack, ItemDisplayContext.NONE, buffer.getBuffer(skullRenderType), light, OverlayTexture.NO_OVERLAY);
         }

         stack.popPose();
      });
   }
}
