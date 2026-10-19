package com.tacz.guns.client.model.functional;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.display.gun.MuzzleFlash;
import com.tacz.guns.compat.iris.IrisCompat;
import com.tacz.guns.resource.modifier.custom.SilenceModifier;
import it.unimi.dsi.fastutil.Pair;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.render26.MultiBufferSource;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import com.tacz.guns.client.render26.MultiBufferSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MuzzleFlashRender implements IFunctionalRenderer {
   private static final SlotModel MUZZLE_FLASH_MODEL = new SlotModel(true);
   private static final long TIME_RANGE = 50L;
   public static boolean isSelf = false;
   private static long shootTimeStamp = -1L;
   private static boolean muzzleFlashStartMark = false;
   private static float muzzleFlashRandomRotate = 0.0F;
   private static Matrix3f muzzleFlashNormal = new Matrix3f();
   private static Matrix4f muzzleFlashPose = new Matrix4f();
   private final BedrockGunModel bedrockGunModel;

   public MuzzleFlashRender(BedrockGunModel bedrockGunModel) {
      this.bedrockGunModel = bedrockGunModel;
   }

   public static void onShoot() {
      shootTimeStamp = System.currentTimeMillis();
      muzzleFlashStartMark = true;
      muzzleFlashRandomRotate = (float)(Math.random() * 360.0);
   }

   private static void renderMuzzleFlash(GunDisplayInstance display, PoseStack poseStack, BedrockModel bedrockModel, long time) {
      MuzzleFlash muzzleFlash = display.getMuzzleFlash();
      if (muzzleFlash != null) {
         if (muzzleFlashStartMark) {
            muzzleFlashNormal = new Matrix3f(poseStack.last().normal());
            muzzleFlashPose = new Matrix4f(poseStack.last().pose());
         }

         bedrockModel.delegateRender((poseStack1, vertexConsumer1, transformType1, light, overlay) -> doRender(light, overlay, muzzleFlash, time));
      }
   }

   private static void doRender(int light, int overlay, MuzzleFlash muzzleFlash, long time) {
      if (muzzleFlashNormal != null && muzzleFlashPose != null) {
         float scale = 0.5F * muzzleFlash.getScale();
         float scaleTime = 25.0F;
         scale = (float)time < scaleTime ? scale * ((float)time / scaleTime) : scale;
         muzzleFlashStartMark = false;
         MultiBufferSource multiBufferSource = com.tacz.guns.client.render26.GunRender.bufferSource();

         PoseStack poseStack2 = new PoseStack();
         poseStack2.last().normal().mul(muzzleFlashNormal);
         poseStack2.last().pose().mul(muzzleFlashPose);
         poseStack2.pushPose();
         poseStack2.scale(scale, scale, scale);
         poseStack2.mulPose(Axis.ZP.rotationDegrees(muzzleFlashRandomRotate));
         poseStack2.translate(0.0F, -1.0F, 0.0F);
         RenderType renderTypeBg = RenderTypes.entityTranslucent(muzzleFlash.getTexture());
         MUZZLE_FLASH_MODEL.renderToBuffer(poseStack2, multiBufferSource.getBuffer(renderTypeBg), light, overlay);
         poseStack2.popPose();
         poseStack2.pushPose();
         poseStack2.scale(scale / 2.0F, scale / 2.0F, scale / 2.0F);
         poseStack2.mulPose(Axis.ZP.rotationDegrees(muzzleFlashRandomRotate));
         poseStack2.translate(0.0, -0.9, 0.0);
         renderTypeBg = RenderTypes.energySwirl(muzzleFlash.getTexture(), 1.0F, 1.0F);
         MUZZLE_FLASH_MODEL.renderToBuffer(poseStack2, multiBufferSource.getBuffer(renderTypeBg), light, overlay);
         poseStack2.popPose();
      }
   }

   @Override
   public void render(PoseStack poseStack, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
      if (!IrisCompat.isRenderShadow()) {
         if (isSelf) {
            long time = System.currentTimeMillis() - shootTimeStamp;
            if (time <= 50L) {
               ItemStack currentGunItem = this.bedrockGunModel.getCurrentGunItem();
               TimelessAPI.getGunDisplay(currentGunItem)
                  .ifPresent(
                     display -> {
                        ItemStack muzzleAttachment = this.bedrockGunModel.getCurrentAttachmentItem().get(AttachmentType.MUZZLE);
                        IAttachment iAttachment = IAttachment.getIAttachmentOrNull(muzzleAttachment);
                        if (iAttachment != null) {
                           Identifier attachmentId = iAttachment.getAttachmentId(muzzleAttachment);
                           TimelessAPI.getCommonAttachmentIndex(attachmentId)
                              .ifPresent(
                                 index -> {
                                    Map<String, JsonProperty<?>> modifier = index.getData().getModifier();
                                    if (!modifier.containsKey(SilenceModifier.ID)
                                       || !(modifier.get(SilenceModifier.ID).getValue() instanceof Pair<?, ?> pair && (Boolean)pair.right())) {
                                       renderMuzzleFlash(display, poseStack, this.bedrockGunModel, time);
                                    }
                                 }
                              );
                        } else {
                           renderMuzzleFlash(display, poseStack, this.bedrockGunModel, time);
                        }
                     }
                  );
            }
         }
      }
   }
}
