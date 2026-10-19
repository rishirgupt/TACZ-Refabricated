package com.tacz.guns.client.renderer.item;

import com.google.common.base.Suppliers;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.pojo.TransformScale;
import java.util.List;
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
import org.joml.Vector3f;

public class AmmoItemRenderer {
   private static final SlotModel SLOT_AMMO_MODEL = new SlotModel();
   public static final Supplier<AmmoItemRenderer> INSTANCE = Suppliers.memoize(() -> {
      Minecraft client = Minecraft.getInstance();
      return new AmmoItemRenderer(client.getBlockEntityRenderDispatcher(), client.getEntityModels());
   });

   public AmmoItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
   }

   private static void applyPositioningNodeTransform(List<BedrockPart> nodePath, PoseStack poseStack, Vector3f scale) {
      if (nodePath != null) {
         if (scale == null) {
            scale = new Vector3f(1.0F, 1.0F, 1.0F);
         }

         poseStack.translate(0.0, 1.5, 0.0);

         for (int i = nodePath.size() - 1; i >= 0; i--) {
            BedrockPart t = nodePath.get(i);
            poseStack.mulPose(Axis.XN.rotation(t.xRot));
            poseStack.mulPose(Axis.YN.rotation(t.yRot));
            poseStack.mulPose(Axis.ZN.rotation(t.zRot));
            if (t.getParent() != null) {
               poseStack.translate(-t.x * scale.x() / 16.0F, -t.y * scale.y() / 16.0F, -t.z * scale.z() / 16.0F);
            } else {
               poseStack.translate(-t.x * scale.x() / 16.0F, (1.5F - t.y / 16.0F) * scale.y(), -t.z * scale.z() / 16.0F);
            }
         }

         poseStack.translate(0.0, -1.5, 0.0);
      }
   }

   public void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
      this.renderByItem(itemStack, itemDisplayContext, poseStack, multiBufferSource, light, overlay);
   }

   public void renderByItem(
      @Nonnull ItemStack stack,
      @Nonnull ItemDisplayContext transformType,
      @Nonnull PoseStack poseStack,
      @Nonnull MultiBufferSource pBuffer,
      int pPackedLight,
      int pPackedOverlay
   ) {
      if (stack.getItem() instanceof IAmmo iAmmo) {
         Identifier var9 = iAmmo.getAmmoId(stack);
         poseStack.pushPose();
         TimelessAPI.getClientAmmoIndex(var9).ifPresentOrElse(ammoIndex -> {
            BedrockAmmoModel ammoModel = ammoIndex.getAmmoModel();
            Identifier modelTexture = ammoIndex.getModelTextureLocation();
            if (transformType != ItemDisplayContext.GUI && ammoModel != null && modelTexture != null) {
               poseStack.translate(0.5, 2.0, 0.5);
               poseStack.scale(-1.0F, -1.0F, 1.0F);
               this.applyPositioningTransform(transformType, ammoIndex.getTransform().getScale(), ammoModel, poseStack);
               this.applyScaleTransform(transformType, ammoIndex.getTransform().getScale(), poseStack);
               RenderType renderType = RenderTypes.entityCutout(modelTexture);
               ammoModel.render(poseStack, transformType, renderType, pPackedLight, pPackedOverlay);
            } else {
               poseStack.translate(0.5, 1.5, 0.5);
               poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
               VertexConsumer buffer = pBuffer.getBuffer(RenderTypes.entityTranslucent(ammoIndex.getSlotTextureLocation()));
               SLOT_AMMO_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay);
            }
         }, () -> {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
            VertexConsumer buffer = pBuffer.getBuffer(RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            SLOT_AMMO_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay);
         });
         poseStack.popPose();
      }
   }

   private void applyPositioningTransform(ItemDisplayContext transformType, TransformScale scale, BedrockAmmoModel model, PoseStack poseStack) {
      switch (transformType) {
         case FIXED:
            applyPositioningNodeTransform(model.getFixedOriginPath(), poseStack, scale.getFixed());
            break;
         case GROUND:
            applyPositioningNodeTransform(model.getGroundOriginPath(), poseStack, scale.getGround());
            break;
         case THIRD_PERSON_RIGHT_HAND:
         case THIRD_PERSON_LEFT_HAND:
            applyPositioningNodeTransform(model.getThirdPersonHandOriginPath(), poseStack, scale.getThirdPerson());
      }
   }

   private void applyScaleTransform(ItemDisplayContext transformType, TransformScale scale, PoseStack poseStack) {
      if (scale != null) {
         Vector3f vector3f = null;
         switch (transformType) {
            case FIXED:
               vector3f = scale.getFixed();
               break;
            case GROUND:
               vector3f = scale.getGround();
               break;
            case THIRD_PERSON_RIGHT_HAND:
            case THIRD_PERSON_LEFT_HAND:
               vector3f = scale.getThirdPerson();
         }

         if (vector3f != null) {
            poseStack.translate(0.0, 1.5, 0.0);
            poseStack.scale(vector3f.x(), vector3f.y(), vector3f.z());
            poseStack.translate(0.0, -1.5, 0.0);
         }
      }
   }
}
