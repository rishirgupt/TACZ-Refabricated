package com.tacz.guns.client.renderer.entity;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.compat.iris.IrisCompat;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.entity.EntityKineticBullet;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class EntityBulletRenderer extends EntityRenderer<EntityKineticBullet, EntityBulletRenderer.State> {
   public static class State extends net.minecraft.client.renderer.entity.state.EntityRenderState {
      public EntityKineticBullet bullet;
      public float partialTicks;
   }

   public EntityBulletRenderer(Context pContext) {
      super(pContext);
   }

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public void extractRenderState(EntityKineticBullet bullet, State state, float partialTicks) {
      super.extractRenderState(bullet, state, partialTicks);
      state.bullet = bullet;
      state.partialTicks = partialTicks;
   }

   @Override
   public void submit(State state, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
      if (state.bullet == null) {
         return;
      }

      com.tacz.guns.client.render26.GunRender.begin(collector);
      this.render(state.bullet, 0.0F, state.partialTicks, poseStack, com.tacz.guns.client.render26.GunRender.bufferSource(), 15728880);
      com.tacz.guns.client.render26.GunRender.end();
   }

   public static Optional<BedrockModel> getModel() {
      return InternalAssetLoader.getBedrockModel(InternalAssetLoader.DEFAULT_BULLET_MODEL);
   }

   public void render(EntityKineticBullet bullet, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      Identifier gunId = bullet.getGunId();
      Identifier gunDisplayId = bullet.getGunDisplayId();
      Optional<GunDisplayInstance> display = TimelessAPI.getGunDisplay(gunDisplayId, gunId);
      if (!display.isEmpty()) {
         float[] tracerColor = bullet.getTracerColorOverride().orElse(display.get().getTracerColor());
         Identifier ammoId = bullet.getAmmoId();
         TimelessAPI.getClientAmmoIndex(ammoId)
            .ifPresent(
               ammoIndex -> {
                  BedrockAmmoModel ammoEntityModel = ammoIndex.getAmmoEntityModel();
                  Identifier textureLocation = ammoIndex.getAmmoEntityTextureLocation();
                  if (ammoEntityModel != null && textureLocation != null) {
                     poseStack.mulPose(
                        Axis.YP.rotationDegrees(Mth.lerp(partialTicks, bullet.yRotO, bullet.getYRot()) - 180.0F)
                     );
                     poseStack.mulPose(
                        Axis.XP.rotationDegrees(Mth.lerp(partialTicks, bullet.xRotO, bullet.getXRot()))
                     );
                     poseStack.pushPose();
                     poseStack.translate(0.0, 1.5, 0.0);
                     poseStack.scale(-1.0F, -1.0F, 1.0F);
                     ammoEntityModel.render(poseStack, ItemDisplayContext.GROUND, RenderTypes.entityTranslucent(textureLocation), packedLight, OverlayTexture.NO_OVERLAY);
                     poseStack.popPose();
                  }

                  if (bullet.isTracerAmmo()) {
                     float[] actualTracerColor = Objects.requireNonNullElse(tracerColor, ammoIndex.getTracerColor());
                     this.renderTracerAmmo(bullet, actualTracerColor, partialTicks, poseStack, packedLight);
                  }
               }
            );
      }
   }

   public void renderTracerAmmo(EntityKineticBullet bullet, float[] tracerColor, float partialTicks, PoseStack poseStack, int packedLight) {
      getModel()
         .ifPresent(
            model -> {
               Entity shooter = bullet.getOwner();
               if (shooter != null) {
                  boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson() && shooter instanceof LocalPlayer;
                  if (!isFirstPerson || (Boolean)RenderConfig.FIRST_PERSON_BULLET_TRACER_ENABLE.get()) {
                     poseStack.pushPose();
                     float width = 0.005F;
                     Vec3 bulletPosition = bullet.getPosition(partialTicks);
                     double trailLength = 0.85 * bullet.getDeltaMovement().length();
                     double disToEye = bulletPosition.distanceTo(shooter.getEyePosition(partialTicks));
                     trailLength = Math.min(trailLength, disToEye * 0.8);
                     if (isFirstPerson) {
                        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
                        Vector3f offset = bullet.getFirstPersonRenderOffset();
                        if (offset == null) {
                           offset = new Vector3f(GunItemRendererWrapper.muzzleRenderOffset);
                           bullet.setCameraXRot(camera.xRot());
                           bullet.setCameraYRot(camera.yRot());
                           bullet.setFirstPersonRenderOffset(offset);
                        }

                        double offsetReducer = Math.max(0.0, 50.0 - disToEye) / 50.0;
                        if (IrisCompat.isPackInUseQuick()) {
                           poseStack.mulPose(Axis.YN.rotationDegrees(bullet.getCameraYRot() + 180.0F));
                           poseStack.mulPose(Axis.XN.rotationDegrees(bullet.getCameraXRot()));
                        }

                        poseStack.translate(offset.x * offsetReducer, offset.y * offsetReducer, offset.z * offsetReducer);
                        if (IrisCompat.isPackInUseQuick()) {
                           poseStack.mulPose(Axis.XP.rotationDegrees(bullet.getCameraXRot()));
                           poseStack.mulPose(Axis.YP.rotationDegrees(bullet.getCameraYRot() + 180.0F));
                        }
                     }

                     width *= bullet.getTracerSizeOverride();
                     width *= (float)Math.max(1.0, disToEye / 3.5);
                     poseStack.mulPose(
                        Axis.YP.rotationDegrees(Mth.lerp(partialTicks, bullet.yRotO, bullet.getYRot()) - 180.0F)
                     );
                     poseStack.mulPose(
                        Axis.XP.rotationDegrees(Mth.lerp(partialTicks, bullet.xRotO, bullet.getXRot()))
                     );
                     poseStack.translate(0.0, isFirstPerson ? 0.0 : -0.2, trailLength / 2.0);
                     poseStack.scale(width, width, (float)trailLength);
                     double bulletDistance = bulletPosition.distanceTo(shooter.getEyePosition());
                     if (bullet.tickCount >= 5 || bulletDistance > 2.0) {
                        RenderType type = RenderTypes.energySwirl(InternalAssetLoader.DEFAULT_BULLET_TEXTURE, 15.0F, 15.0F);
                        model.render(
                           poseStack, ItemDisplayContext.NONE, type, packedLight, OverlayTexture.NO_OVERLAY, tracerColor[0], tracerColor[1], tracerColor[2], 1.0F
                        );
                     }

                     poseStack.popPose();
                  }
               }
            }
         );
   }

   @Override
   public boolean shouldRender(EntityKineticBullet bullet, Frustum camera, double pCamX, double pCamY, double pCamZ) {
      AABB aabb = bullet.getBoundingBox().inflate(0.5);
      if (aabb.hasNaN() || aabb.getSize() == 0.0) {
         aabb = new AABB(
            bullet.getX() - 2.0,
            bullet.getY() - 2.0,
            bullet.getZ() - 2.0,
            bullet.getX() + 2.0,
            bullet.getY() + 2.0,
            bullet.getZ() + 2.0
         );
      }

      return camera.isVisible(aabb);
   }
}
