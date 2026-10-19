package com.tacz.guns.client.renderer.item;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent.ComputeCameraAngles;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation.IFPAnimationInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPGeoItemRenderer;
import com.maydaymemory.mae.basic.DummyPose;
import com.maydaymemory.mae.basic.Pose;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.util.math.MathUtil;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public abstract class AnimateGeoItemRenderer<M extends BedrockAnimatedModel, CTX extends ItemAnimationStateContext>
   implements IFPGeoItemRenderer {
   @Nullable
   protected LuaAnimationStateMachine<CTX> stateMachine;
   protected M model;
   public Identifier textureLocation;

   public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
      this.renderByItem(stack, mode, matrices, vertexConsumers, light, overlay);
   }

   public AnimateGeoItemRenderer() {
   }

   public void setModel(M model) {
      this.model = model;
   }

   public M getModel(ItemStack stack) {
      return this.model;
   }

   @Nullable
   public LuaAnimationStateMachine<CTX> getStateMachine(ItemStack stack) {
      return this.stateMachine;
   }

   public Identifier getTextureLocation(ItemStack stack) {
      return this.textureLocation;
   }

   public RenderType getRenderType(ItemStack stack) {
      return RenderTypes.entityCutout(this.getTextureLocation(stack));
   }

   public boolean needReInit(ItemStack stack) {
      LuaAnimationStateMachine<CTX> stateMachine = this.getStateMachine(stack);
      return stateMachine == null ? false : !stateMachine.isInitialized() && stateMachine.getExitingTime() < System.currentTimeMillis();
   }

   public abstract CTX initContext(ItemStack var1, Player var2, float var3);

   public abstract void updateContext(CTX var1, ItemStack var2, Player var3, float var4);

   public long getPutAwayTime(ItemStack stack) {
      return 0L;
   }

   public void tryInit(ItemStack stack, Player player, float partialTick) {
      LuaAnimationStateMachine<CTX> stateMachine = this.getStateMachine(stack);
      if (stateMachine != null) {
         if (stateMachine.isInitialized()) {
            stateMachine.exit();
         }

         stateMachine.setContext(this.initContext(stack, player, partialTick));
         stateMachine.initialize();
         stateMachine.trigger("draw");
      }
   }

   public void tryExit(ItemStack stack, long putAwayTime) {
      LuaAnimationStateMachine<CTX> stateMachine = this.getStateMachine(stack);
      if (stateMachine != null) {
         stateMachine.processContextIfExist(context -> context.setPutAwayTime((float)putAwayTime / 1000.0F));
         if (stateMachine.isInitialized()) {
            stateMachine.trigger("put_away");
            stateMachine.exit();
            stateMachine.setExitingTime(putAwayTime + 50L);
         }
      }
   }

   public void triggerAnimation(ItemStack stack, String input) {
      LuaAnimationStateMachine<CTX> stateMachine = this.getStateMachine(stack);
      if (stateMachine != null) {
         stateMachine.trigger(input);
      }
   }

   public void visualUpdate(ItemStack stack) {
      LuaAnimationStateMachine<CTX> stateMachine = this.getStateMachine(stack);
      if (stateMachine != null) {
         stateMachine.visualUpdate();
      }
   }

   public void applyLevelCameraAnimation(ComputeCameraAngles event, ItemStack stack, LocalPlayer player) {
      this.applyLevelCameraAnimation(event, stack, 1.0F);
   }

   public void applyLevelCameraAnimation(ComputeCameraAngles event, ItemStack stack, float multiplier) {
      M model = this.getModel(stack);
      if (model != null) {
         Quaternionf q = MathUtil.multiplyQuaternion(model.getCameraAnimationObject().rotationQuaternion, multiplier);
         double yaw = Math.asin(2.0F * (q.w() * q.y() - q.x() * q.z()));
         double pitch = Math.atan2(2.0F * (q.w() * q.x() + q.y() * q.z()), 1.0F - 2.0F * (q.x() * q.x() + q.y() * q.y()));
         double roll = Math.atan2(2.0F * (q.w() * q.z() + q.x() * q.y()), 1.0F - 2.0F * (q.y() * q.y() + q.z() * q.z()));
         yaw = Math.toDegrees(yaw);
         pitch = Math.toDegrees(pitch);
         roll = Math.toDegrees(roll);
         event.setYaw((float)yaw + event.getYaw());
         event.setPitch((float)pitch + event.getPitch());
         event.setRoll((float)roll + event.getRoll());
      }
   }

   public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, LocalPlayer player) {
      this.applyItemInHandCameraAnimation(event, stack, 1.0F);
   }

   public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, float multiplier) {
      M model = this.getModel(stack);
      if (model != null) {
         Quaternionf quaternion = MathUtil.multiplyQuaternion(model.getCameraAnimationObject().rotationQuaternion, multiplier);
         PoseStack poseStack = event.getPoseStack();
         poseStack.mulPose(quaternion);
      }
   }

   public void doExtraTransforms(PoseStack poseStack, M model, ItemStack stack) {
      applyFirstPersonPositioningTransform(poseStack, model, stack);
   }

   public void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
      M model = this.getModel(stack);
      if (model != null) {
         poseStack.pushPose();
         float xRotOffset = Mth.lerp(partialTick, player.xBobO, player.xBob);
         float yRotOffset = Mth.lerp(partialTick, player.yBobO, player.yBob);
         float xRot = player.getViewXRot(partialTick) - xRotOffset;
         float yRot = player.getViewYRot(partialTick) - yRotOffset;
         poseStack.mulPose(Axis.XP.rotationDegrees(xRot * -0.1F));
         poseStack.mulPose(Axis.YP.rotationDegrees(yRot * -0.1F));
         BedrockPart rootNode = model.getRootNode();
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
         this.doExtraTransforms(poseStack, model, stack);
         LuaAnimationStateMachine<CTX> stateMachine = this.getStateMachine(stack);
         if (stateMachine != null) {
            stateMachine.processContextIfExist(context -> this.updateContext(context, stack, player, partialTick));
            stateMachine.update();
         }
         if (!com.tacz.guns.client.gui.overlay.ScopeOverlay.shouldHideGun()) {
            model.render(poseStack, ctx, this.getRenderType(stack), light, OverlayTexture.NO_OVERLAY);
         }

         model.cleanAnimationTransform();
         poseStack.popPose();
      }
   }

   @ParametersAreNonnullByDefault
   public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
      if (!ctx.firstPerson()) {
         M model = this.getModel(stack);
         if (model != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            model.render(poseStack, ctx, RenderTypes.entityCutout(this.getTextureLocation(stack)), light, overlay);
            poseStack.popPose();
         }
      }
   }

   @Nonnull
   public static Matrix4f getPositioningNodeInverse(List<BedrockPart> nodePath) {
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

   public static void applyFirstPersonPositioningTransform(PoseStack poseStack, BedrockAnimatedModel model, ItemStack stack) {
      Matrix4f transformMatrix = new Matrix4f();
      transformMatrix.identity();
      List<BedrockPart> idleNodePath = model.getIdleSightPath();
      Matrix4f idleViewMatrix = getPositioningNodeInverse(idleNodePath);
      MathUtil.applyMatrixLerp(transformMatrix, idleViewMatrix, transformMatrix, 1.0F);
      poseStack.translate(0.0F, 1.5F, 0.0F);
      poseStack.mulPose(transformMatrix);
      poseStack.translate(0.0F, -1.5F, 0.0F);
   }

   public long getPutAwayDuration(ItemStack stack) {
      return this.getPutAwayTime(stack);
   }

   @Nullable
   public IFPAnimationInstance createAnimationInstance(ItemStack stack, Entity entity) {
      return new IFPAnimationInstance() {
         private boolean drawn = false;
         private ItemStack lastItem = stack;

         public ItemStack currentItem() {
            return this.lastItem;
         }

         public Pose getPose() {
            return DummyPose.INSTANCE;
         }

         public void tick(float v) {
         }

         @NotNull
         public Quaternionf getCameraRotation() {
            return new Quaternionf();
         }

         public void setCameraRotation(@NotNull Quaternionf quaternionf) {
         }

         public Pose getCachedPose() {
            return DummyPose.INSTANCE;
         }

         public void updateItem(ItemStack itemStack) {
            this.lastItem = itemStack;
         }

         public void triggerDraw() {
            if (!this.drawn) {
               this.drawn = true;
               AnimateGeoItemRenderer.this.tryInit(this.lastItem, Minecraft.getInstance().player, 0.0F);
               if (Minecraft.getInstance().player != null) {
                  TimelessAPI.getGunDisplay(this.lastItem).ifPresent(display -> {
                     SoundPlayManager.stopPlayGunSound();
                     SoundPlayManager.playDrawSound(Minecraft.getInstance().player, display);
                  });
               }
            }
         }

         public void triggerPutAway() {
            AnimateGeoItemRenderer.this.tryExit(this.lastItem, AnimateGeoItemRenderer.this.getPutAwayTime(this.lastItem));
            if (Minecraft.getInstance().player != null) {
               TimelessAPI.getGunDisplay(this.lastItem).ifPresent(display -> {
                  SoundPlayManager.stopPlayGunSound();
                  SoundPlayManager.playPutAwaySound(Minecraft.getInstance().player, display);
               });
            }
         }
      };
   }

   public boolean isSameItem(ItemStack oldStack, ItemStack newStack) {
      return oldStack.getItem() instanceof IAnimationItem item ? item.isSame(oldStack, newStack) : ItemStack.matches(oldStack, newStack);
   }

   public boolean blockOffhandRender() {
      return true;
   }
}
