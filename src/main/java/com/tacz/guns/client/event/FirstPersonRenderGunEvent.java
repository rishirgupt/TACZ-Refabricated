package com.tacz.guns.client.event;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.event.RenderItemInHandBobEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.client.other.KeepingItemRenderer;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.client.animation.screen.RefitTransform;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.math.Easing;
import com.tacz.guns.util.math.MathUtil;
import com.tacz.guns.util.math.PerlinNoise;
import com.tacz.guns.util.math.SecondOrderDynamics;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class FirstPersonRenderGunEvent {
   private static final SecondOrderDynamics AIMING_DYNAMICS = new SecondOrderDynamics(1.2F, 1.2F, 0.5F, 0.0F);
   private static SecondOrderDynamics SWITCH_VIEW_DYNAMICS;
   private static final SecondOrderDynamics REFIT_OPENING_DYNAMICS = new SecondOrderDynamics(1.0F, 1.2F, 0.5F, 0.0F);
   private static final SecondOrderDynamics JUMPING_DYNAMICS = new SecondOrderDynamics(0.28F, 1.0F, 0.65F, 0.0F);
   private static final float JUMPING_Y_SWAY = -2.0F;
   private static final float JUMPING_SWAY_TIME = 0.3F;
   private static final float LANDING_SWAY_TIME = 0.15F;
   private static final PerlinNoise SHOOT_X_SWAY_NOISE = new PerlinNoise(-0.2F, 0.2F, 400L);
   private static final PerlinNoise SHOOT_Y_ROTATION_NOISE = new PerlinNoise(-0.0136F, 0.0136F, 100L);
   private static final float SHOOT_Y_SWAY = -0.1F;
   private static final float SHOOT_ANIMATION_TIME = 0.3F;
   private static float jumpingSwayProgress = 0.0F;
   private static boolean lastOnGround = false;
   private static long jumpingTimeStamp = -1L;
   private static long shootTimeStamp = -1L;
   private static Matrix4f oldAimingViewMatrix;
   private static float oldViewIndex;
   private static int currentViewIndex = -1;

   public static void cancelItemInHandViewBobbing(RenderItemInHandBobEvent.BobView event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         ItemStack itemStack = KeepingItemRenderer.getRenderer().getCurrentItem();
         if (IGun.getIGunOrNull(itemStack) != null) {
            event.setCanceled(true);
         }
      }
   }

   public static void onGunFire(GunFireEvent event) {
      if (event.getLogicalSide().isClient()) {
         LivingEntity shooter = event.getShooter();
         LocalPlayer player = Minecraft.getInstance().player;
         if (!shooter.equals(player)) {
            return;
         }

         ItemStack mainHandItem = player.getMainHandItem();
         IGun iGun = IGun.getIGunOrNull(mainHandItem);
         if (iGun == null) {
            return;
         }

         TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).ifPresent(gunIndex -> {
            shootTimeStamp = System.currentTimeMillis();
            MuzzleFlashRender.onShoot();
         });
      }
   }

   private static boolean bulletFromPlayer(Entity entity) {
      return entity instanceof EntityKineticBullet entityBullet ? entityBullet.getOwner() instanceof LocalPlayer : false;
   }

   public static void applyFirstPersonGunTransform(LocalPlayer player, ItemStack gunItemStack, PoseStack poseStack, BedrockGunModel model, float partialTicks) {
      float refitScreenOpeningProgress = REFIT_OPENING_DYNAMICS.update(RefitTransform.getOpeningProgress());
      float aimingProgress = AIMING_DYNAMICS.update(IClientPlayerGunOperator.fromLocalPlayer(player).getClientAimingProgress(partialTicks));
      applyGunMovements(model, aimingProgress, partialTicks);
      applyFirstPersonPositioningTransform(poseStack, model, gunItemStack, aimingProgress, refitScreenOpeningProgress);
      applyAnimationConstraintTransform(poseStack, model, aimingProgress * (1.0F - refitScreenOpeningProgress));
   }

   private static void applyGunMovements(BedrockGunModel model, float aimingProgress, float partialTicks) {
      applyShootSwayAndRotation(model, aimingProgress);
      applyJumpingSway(model, partialTicks);
   }

   private static void applyFirstPersonPositioningTransform(
      PoseStack poseStack, BedrockGunModel model, ItemStack stack, float aimingProgress, float refitScreenOpeningProgress
   ) {
      IGun iGun = IGun.getIGunOrNull(stack);
      if (iGun != null) {
         Matrix4f transformMatrix = new Matrix4f();
         transformMatrix.identity();
         List<BedrockPart> idleNodePath = model.getIdleSightPath();
         List<BedrockPart> aimingNodePath = null;
         Identifier scopeId = iGun.getAttachmentId(stack, AttachmentType.SCOPE);
         if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = iGun.getBuiltInAttachmentId(stack, AttachmentType.SCOPE);
         }

         CompoundTag scopeTag = iGun.getAttachmentTag(stack, AttachmentType.SCOPE);
         int zoomNumber = AttachmentItemDataAccessor.getZoomNumberFromTag(scopeTag);
         int viewIndex = 1;
         if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            aimingNodePath = model.getIronSightPath();
         } else {
            List<BedrockPart> scopeNodePath = model.getScopePosPath();
            if (scopeNodePath != null) {
               aimingNodePath = new ArrayList<>(scopeNodePath);
               Optional<ClientAttachmentIndex> indexOptional = TimelessAPI.getClientAttachmentIndex(scopeId);
               if (indexOptional.isPresent()) {
                  BedrockAttachmentModel attachmentModel = indexOptional.get().getAttachmentModel();
                  int[] views = indexOptional.get().getViews();
                  viewIndex = views[zoomNumber % views.length] - 1;
                  if (attachmentModel != null) {
                     List<BedrockPart> scopeViewPath = attachmentModel.getScopeViewPath(currentViewIndex == -1 ? viewIndex : currentViewIndex);
                     if (scopeViewPath != null) {
                        aimingNodePath.addAll(scopeViewPath);
                     }
                  }
               }
            }
         }

         Matrix4f aimingViewMatrix = getPositioningNodeInverse(aimingNodePath);
         if (currentViewIndex == -1) {
            currentViewIndex = viewIndex;
            oldViewIndex = viewIndex;
            oldAimingViewMatrix = aimingViewMatrix;
            SWITCH_VIEW_DYNAMICS = new SecondOrderDynamics(0.35F, 1.2F, 0.3F, viewIndex);
         }

         float view_interpret = SWITCH_VIEW_DYNAMICS.update(viewIndex);
         float span = currentViewIndex - oldViewIndex;
         float switchingProgress = Math.abs(span) < 0.05 ? 1.0F : (view_interpret - oldViewIndex) / span;
         MathUtil.applyMatrixLerp(aimingViewMatrix, oldAimingViewMatrix, aimingViewMatrix, 1.0F - switchingProgress);
         if (currentViewIndex != viewIndex) {
            oldAimingViewMatrix = aimingViewMatrix;
            oldViewIndex = view_interpret;
            currentViewIndex = viewIndex;
         }

         MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(idleNodePath), transformMatrix, 1.0F - refitScreenOpeningProgress);
         MathUtil.applyMatrixLerp(transformMatrix, aimingViewMatrix, transformMatrix, (1.0F - refitScreenOpeningProgress) * aimingProgress);
         float refitTransformProgress = (float)Easing.easeOutCubic(RefitTransform.getTransformProgress());
         AttachmentType oldType = RefitTransform.getOldTransformType();
         AttachmentType currentType = RefitTransform.getCurrentTransformType();
         List<BedrockPart> fromNode = model.getRefitAttachmentViewPath(oldType);
         List<BedrockPart> toNode = model.getRefitAttachmentViewPath(currentType);
         MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(fromNode), transformMatrix, refitScreenOpeningProgress);
         MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(toNode), transformMatrix, refitScreenOpeningProgress * refitTransformProgress);
         poseStack.translate(0.0F, 1.5F, 0.0F);
         poseStack.mulPose(transformMatrix);
         poseStack.translate(0.0F, -1.5F, 0.0F);
      }
   }

   @Nonnull
   private static Matrix4f getPositioningNodeInverse(List<BedrockPart> nodePath) {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.identity();
      if (nodePath != null) {
         for (int i = nodePath.size() - 1; i >= 0; i--) {
            BedrockPart part = nodePath.get(i);
            matrix4f.rotate(Axis.XN.rotation(part.xRot));
            matrix4f.rotate(Axis.YN.rotation(part.yRot));
            matrix4f.rotate(Axis.ZN.rotation(part.zRot));
            if (part.getParent() != null) {
               matrix4f.translate(-part.x / 16.0F, -part.y / 16.0F, -part.z / 16.0F);
            } else {
               matrix4f.translate(-part.x / 16.0F, 1.5F - part.y / 16.0F, -part.z / 16.0F);
            }
         }
      }

      return matrix4f;
   }

   private static void applyShootSwayAndRotation(BedrockGunModel model, float aimingProgress) {
      BedrockPart rootNode = model.getRootNode();
      if (rootNode != null) {
         float progress = 1.0F - (float)(System.currentTimeMillis() - shootTimeStamp) / 300.0F;
         if (progress < 0.0F) {
            progress = 0.0F;
         }

         progress = (float)Easing.easeOutCubic(progress);
         rootNode.offsetX = rootNode.offsetX + SHOOT_X_SWAY_NOISE.getValue() / 16.0F * progress * (1.0F - aimingProgress);
         rootNode.offsetY += 0.00625F * progress * (1.0F - aimingProgress);
         rootNode.additionalQuaternion.mul(Axis.YP.rotation(SHOOT_Y_ROTATION_NOISE.getValue() * progress));
      }
   }

   private static void applyJumpingSway(BedrockGunModel model, float partialTicks) {
      if (jumpingTimeStamp == -1L) {
         jumpingTimeStamp = System.currentTimeMillis();
      }

      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         double posY = Mth.lerp(partialTicks, Minecraft.getInstance().player.yOld, Minecraft.getInstance().player.getY());
         float velocityY = (float)(posY - Minecraft.getInstance().player.yOld) / partialTicks;
         if (player.onGround()) {
            if (!lastOnGround) {
               jumpingSwayProgress = velocityY / -0.1F;
               if (jumpingSwayProgress > 1.0F) {
                  jumpingSwayProgress = 1.0F;
               }

               lastOnGround = true;
            } else {
               jumpingSwayProgress = jumpingSwayProgress - (float)(System.currentTimeMillis() - jumpingTimeStamp) / 150.0F;
               if (jumpingSwayProgress < 0.0F) {
                  jumpingSwayProgress = 0.0F;
               }
            }
         } else if (lastOnGround) {
            jumpingSwayProgress = velocityY / 0.42F;
            if (jumpingSwayProgress > 1.0F) {
               jumpingSwayProgress = 1.0F;
            }

            lastOnGround = false;
         } else {
            jumpingSwayProgress = jumpingSwayProgress - (float)(System.currentTimeMillis() - jumpingTimeStamp) / 300.0F;
            if (jumpingSwayProgress < 0.0F) {
               jumpingSwayProgress = 0.0F;
            }
         }
      }

      jumpingTimeStamp = System.currentTimeMillis();
      float ySway = JUMPING_DYNAMICS.update(-2.0F * jumpingSwayProgress);
      BedrockPart rootNode = model.getRootNode();
      if (rootNode != null) {
         rootNode.offsetY += -ySway / 16.0F;
      }
   }

   private static void getAnimationConstraintTransform(
      List<BedrockPart> nodePath, @Nonnull Vector3f originTranslation, @Nonnull Vector3f animatedTranslation, @Nonnull Vector3f rotation
   ) {
      if (nodePath != null) {
         Matrix4f animeMatrix = new Matrix4f();
         Matrix4f originMatrix = new Matrix4f();
         animeMatrix.identity();
         originMatrix.identity();
         BedrockPart constrainNode = nodePath.get(nodePath.size() - 1);

         for (BedrockPart part : nodePath) {
            if (part != constrainNode) {
               animeMatrix.translate(part.offsetX, part.offsetY, part.offsetZ);
            }

            if (part.getParent() != null) {
               animeMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
               animeMatrix.translate(part.x / 16.0F, part.y / 16.0F - 1.5F, part.z / 16.0F);
            }

            if (part != constrainNode) {
               animeMatrix.rotate(part.additionalQuaternion);
            }

            animeMatrix.rotate(Axis.ZP.rotation(part.zRot));
            animeMatrix.rotate(Axis.YP.rotation(part.yRot));
            animeMatrix.rotate(Axis.XP.rotation(part.xRot));
            if (part.getParent() != null) {
               originMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
               originMatrix.translate(part.x / 16.0F, part.y / 16.0F - 1.5F, part.z / 16.0F);
            }

            originMatrix.rotate(Axis.ZP.rotation(part.zRot));
            originMatrix.rotate(Axis.YP.rotation(part.yRot));
            originMatrix.rotate(Axis.XP.rotation(part.xRot));
         }

         animeMatrix.getTranslation(animatedTranslation);
         originMatrix.getTranslation(originTranslation);
         Vector3f animatedRotation = MathUtil.getEulerAngles(animeMatrix);
         Vector3f originRotation = MathUtil.getEulerAngles(originMatrix);
         animatedRotation.sub(originRotation);
         rotation.set(animatedRotation.x(), animatedRotation.y(), animatedRotation.z());
      }
   }

   public static void applyAnimationConstraintTransform(PoseStack poseStack, BedrockGunModel gunModel, float weight) {
      List<BedrockPart> nodePath = gunModel.getConstraintPath();
      if (nodePath != null) {
         if (gunModel.getConstraintObject() != null) {
            Vector3f originTranslation = new Vector3f();
            Vector3f animatedTranslation = new Vector3f();
            Vector3f rotation = new Vector3f();
            Vector3f translationICA = gunModel.getConstraintObject().translationConstraint;
            Vector3f rotationICA = gunModel.getConstraintObject().rotationConstraint;
            getAnimationConstraintTransform(nodePath, originTranslation, animatedTranslation, rotation);
            Vector3f inverseTranslation = new Vector3f(originTranslation);
            inverseTranslation.sub(animatedTranslation);
            inverseTranslation.mulDirection(poseStack.last().pose());
            inverseTranslation.mul(translationICA.x() - 1.0F, translationICA.y() - 1.0F, 1.0F - translationICA.z());
            Vector3f inverseRotation = new Vector3f(rotation);
            inverseRotation.mul(rotationICA.x() - 1.0F, rotationICA.y() - 1.0F, rotationICA.z() - 1.0F);
            poseStack.translate(animatedTranslation.x(), animatedTranslation.y() + 1.5F, animatedTranslation.z());
            poseStack.mulPose(Axis.XP.rotation(inverseRotation.x() * weight));
            poseStack.mulPose(Axis.YP.rotation(inverseRotation.y() * weight));
            poseStack.mulPose(Axis.ZP.rotation(inverseRotation.z() * weight));
            poseStack.translate(-animatedTranslation.x(), -animatedTranslation.y() - 1.5F, -animatedTranslation.z());
            Matrix4f poseMatrix = poseStack.last().pose();
            poseMatrix.m30(poseMatrix.m30() - inverseTranslation.x() * weight);
            poseMatrix.m31(poseMatrix.m31() - inverseTranslation.y() * weight);
            poseMatrix.m32(poseMatrix.m32() + inverseTranslation.z() * weight);
         }
      }
   }
}
