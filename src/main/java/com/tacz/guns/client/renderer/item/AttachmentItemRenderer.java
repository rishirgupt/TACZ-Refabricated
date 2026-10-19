package com.tacz.guns.client.renderer.item;

import com.google.common.base.Suppliers;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.util.RenderDistance;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class AttachmentItemRenderer {
   public static final SlotModel SLOT_ATTACHMENT_MODEL = new SlotModel();
   public static final Supplier<AttachmentItemRenderer> INSTANCE = Suppliers.memoize(() -> {
      Minecraft client = Minecraft.getInstance();
      return new AttachmentItemRenderer(client.getBlockEntityRenderDispatcher(), client.getEntityModels());
   });

   public AttachmentItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
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
      if (stack.getItem() instanceof IAttachment iAttachment) {
         Identifier attachmentId = iAttachment.getAttachmentId(stack);
         poseStack.pushPose();
         TimelessAPI.getClientAttachmentIndex(attachmentId).ifPresentOrElse(attachmentIndex -> {
            if (transformType == ItemDisplayContext.GUI) {
               poseStack.translate(0.5, 1.5, 0.5);
               poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
               VertexConsumer buffer = pBuffer.getBuffer(RenderTypes.entityTranslucent(attachmentIndex.getSlotTexture()));
               SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay);
            } else {
               poseStack.translate(0.5, 2.0, 0.5);
               poseStack.scale(-1.0F, -1.0F, 1.0F);
               if (transformType == ItemDisplayContext.FIXED) {
                  poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
               }

               this.renderDefaultAttachment(transformType, poseStack, pBuffer, pPackedLight, pPackedOverlay, attachmentIndex);
            }
         }, () -> {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
            VertexConsumer buffer = pBuffer.getBuffer(RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay);
         });
         poseStack.popPose();
      }
   }

   private void renderDefaultAttachment(
      @NotNull ItemDisplayContext transformType,
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource pBuffer,
      int pPackedLight,
      int pPackedOverlay,
      ClientAttachmentIndex attachmentIndex
   ) {
      BedrockAttachmentModel model = attachmentIndex.getAttachmentModel();
      Identifier texture = attachmentIndex.getModelTexture();
      if (model != null && texture != null) {
         Pair<BedrockAttachmentModel, Identifier> lodModel = attachmentIndex.getLodModel();
         if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack) && !transformType.firstPerson()) {
            model = (BedrockAttachmentModel)lodModel.getLeft();
            texture = (Identifier)lodModel.getRight();
         }

         RenderType renderType = RenderTypes.entityCutout(texture);
         model.render(null, null, poseStack, transformType, renderType, pPackedLight, pPackedOverlay);
      } else {
         poseStack.translate(0.0, 0.5, 0.0);
         if (transformType == ItemDisplayContext.FIXED) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
         }

         VertexConsumer buffer = pBuffer.getBuffer(RenderTypes.entityTranslucent(attachmentIndex.getSlotTexture()));
         SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay);
      }
   }
}
