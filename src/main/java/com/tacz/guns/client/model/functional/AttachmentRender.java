package com.tacz.guns.client.model.functional;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.renderer.item.AttachmentItemRenderer;
import com.tacz.guns.util.RenderDistance;
import java.util.EnumMap;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class AttachmentRender implements IFunctionalRenderer {
   private final BedrockGunModel bedrockGunModel;
   private final AttachmentType type;

   public AttachmentRender(BedrockGunModel bedrockGunModel, AttachmentType type) {
      this.bedrockGunModel = bedrockGunModel;
      this.type = type;
   }

   public static void renderAttachment(ItemStack attachmentItem, ItemStack gunItem, PoseStack poseStack, ItemDisplayContext transformType, int light, int overlay) {
      poseStack.translate(0.0, -1.5, 0.0);
      if (attachmentItem.getItem() instanceof IAttachment iAttachment) {
         Identifier attachmentId = iAttachment.getAttachmentId(attachmentItem);
         TimelessAPI.getClientAttachmentIndex(attachmentId).ifPresentOrElse(attachmentIndex -> {
            BedrockAttachmentModel model = attachmentIndex.getAttachmentModel();
            Identifier texture = attachmentIndex.getModelTexture();
            if (model != null && texture != null) {
               Pair<BedrockAttachmentModel, Identifier> lodModel = attachmentIndex.getLodModel();
               if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack) && !transformType.firstPerson()) {
                  model = (BedrockAttachmentModel)lodModel.getLeft();
                  texture = (Identifier)lodModel.getRight();
               }

               RenderType renderType = RenderTypes.entityCutout(texture);
               model.setReticleTexture(texture);
               model.render(attachmentItem, gunItem, poseStack, transformType, renderType, light, overlay);
            }
         }, () -> {
            MultiBufferSource bufferSource = com.tacz.guns.client.render26.GunRender.bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            AttachmentItemRenderer.SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, light, overlay);
         });
      }
   }

   @Override
   public void render(PoseStack poseStack, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
      EnumMap<AttachmentType, ItemStack> currentAttachmentItem = this.bedrockGunModel.getCurrentAttachmentItem();
      ItemStack attachmentItem = currentAttachmentItem.get(this.type);
      if (attachmentItem != null && !attachmentItem.isEmpty()) {
         Matrix3f normal = new Matrix3f(poseStack.last().normal());
         Matrix4f pose = new Matrix4f(poseStack.last().pose());
         this.bedrockGunModel.delegateRender((poseStack1, vertexBuffer1, transformType1, light1, overlay1) -> {
            PoseStack poseStack2 = new PoseStack();
            poseStack2.last().normal().mul(normal);
            poseStack2.last().pose().mul(pose);
            renderAttachment(attachmentItem, this.bedrockGunModel.getCurrentGunItem(), poseStack2, transformType, light, overlay);
         });
      }
   }
}
