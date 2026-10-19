package com.tacz.guns.client.renderer.item;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent.ComputeCameraAngles;
import com.google.common.base.Suppliers;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.screen.RefitTransform;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.event.CameraSetupEvent;
import com.tacz.guns.client.event.FirstPersonRenderGunEvent;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.TransformScale;
import com.tacz.guns.util.RenderDistance;
import com.tacz.guns.util.math.MathUtil;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GunItemRendererWrapper extends AnimateGeoItemRenderer<BedrockGunModel, GunAnimationStateContext> {
   private static final SlotModel SLOT_GUN_MODEL = new SlotModel();
   private static BedrockGunModel lastModel = null;
   public static final Vector3f muzzleRenderOffset = new Vector3f();
   public static final Supplier<GunItemRendererWrapper> INSTANCE = Suppliers.memoize(GunItemRendererWrapper::new);

   public GunAnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
      GunAnimationStateContext context = new GunAnimationStateContext();
      this.updateContext(context, stack, player, partialTick);
      return context;
   }

   public void updateContext(GunAnimationStateContext context, ItemStack stack, Player player, float partialTick) {
      context.setPartialTicks(partialTick);
      context.setCurrentGunItem(stack);
   }

   @Override
   public void tryInit(ItemStack stack, Player player, float partialTick) {
      super.tryInit(stack, player, partialTick);
   }

   @Override
   public void tryExit(ItemStack stack, long putAwayTime) {
      LuaAnimationStateMachine<GunAnimationStateContext> stateMachine = this.getStateMachine(stack);
      if (stateMachine != null) {
         stateMachine.processContextIfExist(context -> {
            context.setPutAwayTime((float)putAwayTime / 1000.0F);
            context.setCurrentGunItem(stack);
         });
         if (stateMachine.isInitialized()) {
            stateMachine.trigger("put_away");
            stateMachine.exit();
            stateMachine.setExitingTime(putAwayTime + 50L);
         }
      }
   }

   @Override
   public long getPutAwayTime(ItemStack stack) {
      return stack.getItem() instanceof IGun iGun
         ? TimelessAPI.getCommonGunIndex(iGun.getGunId(stack)).map(index -> (long)(index.getGunData().getPutAwayTime() * 1000.0F)).orElse(0L)
         : 0L;
   }

   @Nullable
   @Override
   public LuaAnimationStateMachine<GunAnimationStateContext> getStateMachine(ItemStack stack) {
      return TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getAnimationStateMachine).orElse(null);
   }

   public BedrockGunModel getModel(ItemStack stack) {
      return TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getGunModel).orElse(null);
   }

   @Override
   public Identifier getTextureLocation(ItemStack stack) {
      return TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getModelTexture).orElse(null);
   }

   @Override
   public void applyLevelCameraAnimation(ComputeCameraAngles event, ItemStack stack, LocalPlayer player) {
      if (stack.getItem() instanceof IGun iGun) {
         Optional.ofNullable(this.getModel(stack)).ifPresent(model -> {
            if (lastModel != model) {
               model.cleanCameraAnimationTransform();
               lastModel = model;
            }

            IClientPlayerGunOperator clientPlayerGunOperator = IClientPlayerGunOperator.fromLocalPlayer(player);
            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float aimingProgress = clientPlayerGunOperator.getClientAimingProgress(partialTicks);
            float zoom = iGun.getAimingZoom(stack);
            float multiplier = 1.0F - aimingProgress + aimingProgress / (float)Math.sqrt(zoom);
            this.applyLevelCameraAnimation(event, stack, multiplier);
         });
      }
   }

   @Override
   public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, LocalPlayer player) {
      if (stack.getItem() instanceof IGun iGun) {
         Optional.ofNullable(this.getModel(stack)).ifPresent(model -> {
            PoseStack poseStack = event.getPoseStack();
            IClientPlayerGunOperator clientPlayerGunOperator = IClientPlayerGunOperator.fromLocalPlayer(player);
            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float aimingProgress = clientPlayerGunOperator.getClientAimingProgress(partialTicks);
            float zoom = iGun.getAimingZoom(stack);
            float multiplier = 1.0F - aimingProgress + aimingProgress / (float)Math.sqrt(zoom);
            Quaternionf quaternion = MathUtil.multiplyQuaternion(model.getCameraAnimationObject().rotationQuaternion, multiplier);
            poseStack.mulPose(quaternion);
            model.cleanCameraAnimationTransform();
         });
      }
   }

   @Override
   public void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
      if (stack.getItem() instanceof IGun) {
         TimelessAPI.getGunDisplay(stack)
            .ifPresent(
               display -> {
                  BedrockGunModel gunModel = display.getGunModel();
                  LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = display.getAnimationStateMachine();
                  if (gunModel != null) {
                     if (animationStateMachine != null) {
                        animationStateMachine.processContextIfExist(context -> this.updateContext(context, stack, player, partialTick));
                        animationStateMachine.update();
                     }

                     poseStack.pushPose();
                     float xRotOffset = Mth.lerp(partialTick, player.xBobO, player.xBob);
                     float yRotOffset = Mth.lerp(partialTick, player.yBobO, player.yBob);
                     float xRot = player.getViewXRot(partialTick) - xRotOffset;
                     float yRot = player.getViewYRot(partialTick) - yRotOffset;
                     poseStack.mulPose(Axis.XP.rotationDegrees(xRot * -0.1F));
                     poseStack.mulPose(Axis.YP.rotationDegrees(yRot * -0.1F));
                     BedrockPart rootNode = gunModel.getRootNode();
                     if (rootNode != null) {
                        xRot = (float)Math.tanh(xRot / 25.0F) * 25.0F;
                        yRot = (float)Math.tanh(yRot / 25.0F) * 25.0F;
                        rootNode.offsetX += yRot * 0.1F / 16.0F / 3.0F;
                        rootNode.offsetY += -xRot * 0.1F / 16.0F / 3.0F;
                        rootNode.additionalQuaternion.mul(Axis.XP.rotationDegrees(xRot * 0.05F));
                        rootNode.additionalQuaternion.mul(Axis.YP.rotationDegrees(yRot * 0.05F));
                     }

                     poseStack.translate(0.0F, 1.5F, 0.0F);
                     poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                     FirstPersonRenderGunEvent.applyFirstPersonGunTransform(player, stack, poseStack, gunModel, partialTick);
                     MuzzleFlashRender.isSelf = true;
                     ShellRender.isSelf = true;
                     boolean renderHand = gunModel.getRenderHand();
                     if (RefitTransform.getOpeningProgress() != 0.0F) {
                        gunModel.setRenderHand(false);
                     }

                     RenderType renderType = display.enablesTransparency()
                        ? RenderTypes.entityTranslucent(display.getModelTexture())
                        : RenderTypes.entityCutout(display.getModelTexture());
                     gunModel.render(poseStack, stack, ctx, renderType, light, OverlayTexture.NO_OVERLAY);
                     cacheMuzzlePosition(poseStack, gunModel);
                     if (com.tacz.guns.client.gui.overlay.ScopeOverlay.shouldHideGun()) {
                        com.tacz.guns.client.render26.GunRender.discardPending();
                     }

                     gunModel.setRenderHand(renderHand);
                     poseStack.popPose();
                     gunModel.cleanAnimationTransform();
                     MuzzleFlashRender.isSelf = false;
                     ShellRender.isSelf = false;
                  }
               }
            );
      }
   }

   private static void cacheMuzzlePosition(PoseStack poseStack, BedrockGunModel gunModel) {
      if (gunModel.getMuzzleFlashPosPath() != null) {
         poseStack.pushPose();

         for (BedrockPart bedrockPart : gunModel.getMuzzleFlashPosPath()) {
            bedrockPart.translateAndRotateAndScale(poseStack);
         }

         Matrix4f pose = poseStack.last().pose();
         double itemRenderFov = CameraSetupEvent.ITEM_MODEL_FOV_DYNAMICS.get();
         double levelRenderFov = CameraSetupEvent.WORLD_FOV_DYNAMICS.get();
         poseStack.popPose();
         muzzleRenderOffset.set(
            pose.m30(), pose.m31(), pose.m32() * Math.tan(itemRenderFov / 2.0 * Math.PI / 180.0) / Math.tan(levelRenderFov / 2.0 * Math.PI / 180.0)
         );
      }
   }

   @Override
   public void renderByItem(
      @Nonnull ItemStack stack,
      @Nonnull ItemDisplayContext transformType,
      @Nonnull PoseStack poseStack,
      @Nonnull MultiBufferSource pBuffer,
      int pPackedLight,
      int pPackedOverlay
   ) {
      if (stack.getItem() instanceof IGun) {
         poseStack.pushPose();
         TimelessAPI.getGunDisplay(stack).ifPresentOrElse(gunIndex -> {
            if (transformType != ItemDisplayContext.FIRST_PERSON_LEFT_HAND && transformType != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
               if (transformType != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                  if (transformType == ItemDisplayContext.GUI) {
                     renderSlotTexture(poseStack, pBuffer, pPackedLight, pPackedOverlay, gunIndex.getSlotTexture());
                  } else {
                     Pair<BedrockGunModel, Identifier> lodModel = gunIndex.getLodModel();
                     BedrockGunModel gunModel;
                     Identifier gunTexture;
                     if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack)) {
                        gunModel = (BedrockGunModel)lodModel.getLeft();
                        gunTexture = (Identifier)lodModel.getRight();
                     } else {
                        gunModel = gunIndex.getGunModel();
                        gunTexture = gunIndex.getModelTexture();
                     }

                     if (gunModel == null) {
                        renderSlotTexture(poseStack, pBuffer, pPackedLight, pPackedOverlay, gunIndex.getSlotTexture());
                     } else {
                        poseStack.translate(0.5, 2.0, 0.5);
                        poseStack.scale(-1.0F, -1.0F, 1.0F);
                        applyPositioningTransform(transformType, gunIndex.getTransform().getScale(), gunModel, poseStack);
                        applyScaleTransform(transformType, gunIndex.getTransform().getScale(), poseStack);
                        RenderType renderType = RenderTypes.entityCutout(gunTexture);
                        gunModel.render(poseStack, stack, transformType, renderType, pPackedLight, pPackedOverlay);
                     }
                  }
               }
            }
         }, () -> renderSlotTexture(poseStack, pBuffer, pPackedLight, pPackedOverlay, MissingTextureAtlasSprite.getLocation()));
         poseStack.popPose();
      }
   }

   private static void renderSlotTexture(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Identifier texture) {
      poseStack.translate(0.5, 1.5, 0.5);
      poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
      VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.entityTranslucent(texture));
      SLOT_GUN_MODEL.renderToBuffer(poseStack, buffer, packedLight, packedOverlay);
   }

   private static void applyPositioningTransform(ItemDisplayContext transformType, TransformScale scale, BedrockGunModel model, PoseStack poseStack) {
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

   private static void applyScaleTransform(ItemDisplayContext transformType, TransformScale scale, PoseStack poseStack) {
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
}
