package com.tacz.guns.client.renderer.item;

import com.google.common.base.Suppliers;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.renderer.block.GunSmithTableRenderer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.model.geom.EntityModelSet;
import com.mojang.math.Axis;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

public class GunSmithTableItemRenderer {
   private static final SlotModel SLOT_BLOCK_MODEL = new SlotModel();
   public static final Supplier<GunSmithTableItemRenderer> INSTANCE = Suppliers.memoize(() -> {
      Minecraft client = Minecraft.getInstance();
      return new GunSmithTableItemRenderer(client.getBlockEntityRenderDispatcher(), client.getEntityModels());
   });

   public GunSmithTableItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
   }

   public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
      this.renderByItem(stack, mode, matrices, vertexConsumers, light, overlay);
   }

   public void renderByItem(
      @Nonnull ItemStack stack,
      @Nonnull ItemDisplayContext transformType,
      @Nonnull PoseStack poseStack,
      @Nonnull MultiBufferSource pBuffer,
      int pPackedLight,
      int pPackedOverlay
   ) {
      GunSmithTableRenderer.getIndex(stack).ifPresentOrElse(index -> {
         BedrockModel model = index.getModel();
         Identifier texture = index.getTexture();
         if (model != null) {
            poseStack.pushPose();
            ItemTransforms transforms = index.getTransforms();
            if (transforms != null) {
               poseStack.translate(0.5F, 0.5F, 0.5F);
               ItemTransform transform = transforms.getTransform(transformType);
               transform.apply(false, poseStack.last());
               poseStack.translate(-0.5F, -0.5F, -0.5F);
            }

            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
            RenderType renderType = RenderTypes.entityTranslucent(texture);
            model.render(poseStack, transformType, renderType, pPackedLight, pPackedOverlay);
            poseStack.popPose();
         }
      }, () -> {
         poseStack.translate(0.5, 1.5, 0.5);
         poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
         VertexConsumer buffer = pBuffer.getBuffer(RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
         SLOT_BLOCK_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay);
      });
   }
}
