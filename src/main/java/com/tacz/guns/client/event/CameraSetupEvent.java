package com.tacz.guns.client.event;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent.ComputeCameraAngles;
import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent.ComputeFov;
import cn.sh1rocu.tacz.api.event.ComputeFovModifierEvent;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.client.other.KeepingItemRenderer;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.RecoilModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.math.MathUtil;
import com.tacz.guns.util.math.SecondOrderDynamics;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.client.player.LocalPlayer;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

@Environment(EnvType.CLIENT)
public class CameraSetupEvent {
   public static final SecondOrderDynamics WORLD_FOV_DYNAMICS = new SecondOrderDynamics(0.5F, 1.2F, 0.5F, 0.0F);
   public static final SecondOrderDynamics ITEM_MODEL_FOV_DYNAMICS = new SecondOrderDynamics(0.5F, 1.2F, 0.5F, 0.0F);
   private static PolynomialSplineFunction pitchSplineFunction;
   private static PolynomialSplineFunction yawSplineFunction;
   private static long shootTimeStamp = -1L;
   private static double xRotO = 0.0;
   private static double yRotO = 0.0;

   public static void applyLevelCameraAnimation(ComputeCameraAngles event) {
      if ((Boolean)Minecraft.getInstance().options.bobView().get()) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (com.tacz.guns.client.render26.GeoItemRegistry.get(stack.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
               renderer.applyLevelCameraAnimation(event, stack, player);
            }
         }
      }
   }

   public static void applyItemInHandCameraAnimation(BeforeRenderHandEvent event) {
      if ((Boolean)Minecraft.getInstance().options.bobView().get()) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (com.tacz.guns.client.render26.GeoItemRegistry.get(stack.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
               renderer.applyItemInHandCameraAnimation(event, stack, player);
            }
         }
      }
   }

   public static void applyScopeMagnification(ComputeFov event) {
      if (event.usedConfiguredFov()) {
         if (event.getCamera().entity() instanceof LivingEntity livingEntity) {
            ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (!(stack.getItem() instanceof IGun iGun)) {
               float fov = WORLD_FOV_DYNAMICS.update((float)event.getFOV());
               event.setFOV(fov);
               return;
            }

            float var10 = iGun.getAimingZoom(stack);
            if (livingEntity instanceof LocalPlayer localPlayer) {
               IClientPlayerGunOperator gunOperator = IClientPlayerGunOperator.fromLocalPlayer(localPlayer);
               float aimingProgress = gunOperator.getClientAimingProgress((float)event.getPartialTick());
               float fov = WORLD_FOV_DYNAMICS.update((float)MathUtil.magnificationToFov(1.0F + (var10 - 1.0F) * aimingProgress, event.getFOV()));
               event.setFOV(fov);
            } else {
               IGunOperator gunOperator = IGunOperator.fromLivingEntity(livingEntity);
               float aimingProgress = gunOperator.getSynAimingProgress();
               float fov = WORLD_FOV_DYNAMICS.update((float)MathUtil.magnificationToFov(1.0F + (var10 - 1.0F) * aimingProgress, event.getFOV()));
               event.setFOV(fov);
            }
         }
      }
   }

   public static void applyGunModelFovModifying(ComputeFov event) {
      if (!event.usedConfiguredFov()) {
         if (event.getCamera().entity() instanceof LivingEntity livingEntity) {
            ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (!(stack.getItem() instanceof IGun iGun)) {
               float fov = ITEM_MODEL_FOV_DYNAMICS.update((float)event.getFOV());
               event.setFOV(fov);
               return;
            }

            Identifier scopeItemId = iGun.getAttachmentId(stack, AttachmentType.SCOPE);
            if (scopeItemId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
               scopeItemId = iGun.getBuiltInAttachmentId(stack, AttachmentType.SCOPE);
            }

            CompoundTag scopeTag = iGun.getAttachmentTag(stack, AttachmentType.SCOPE);
            int zoomNumber = AttachmentItemDataAccessor.getZoomNumberFromTag(scopeTag);
            float modifiedFov = TimelessAPI.getClientAttachmentIndex(scopeItemId).map(index -> {
               float[] viewsFov = index.getViewsFov();
               return viewsFov[zoomNumber % viewsFov.length];
            }).orElse(TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getZoomModelFov).orElse((float)event.getFOV()));
            if (livingEntity instanceof LocalPlayer localPlayer) {
               IClientPlayerGunOperator gunOperator = IClientPlayerGunOperator.fromLocalPlayer(localPlayer);
               float aimingProgress = gunOperator.getClientAimingProgress((float)event.getPartialTick());
               float fov = ITEM_MODEL_FOV_DYNAMICS.update(Mth.lerp(aimingProgress, (float)event.getFOV(), modifiedFov));
               event.setFOV(fov);
            } else {
               IGunOperator gunOperator = IGunOperator.fromLivingEntity(livingEntity);
               float aimingProgress = gunOperator.getSynAimingProgress();
               float fov = ITEM_MODEL_FOV_DYNAMICS.update(Mth.lerp(aimingProgress, (float)event.getFOV(), modifiedFov));
               event.setFOV(fov);
            }
         }
      }
   }

   public static void initialCameraRecoil(GunFireEvent event) {
      if (event.getLogicalSide().isClient()) {
         LivingEntity shooter = event.getShooter();
         LocalPlayer player = Minecraft.getInstance().player;
         if (!shooter.equals(player)) {
            return;
         }

         ItemStack mainHandItem = player.getMainHandItem();
         if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return;
         }

         AttachmentCacheProperty cacheProperty = IGunOperator.fromLivingEntity(player).getCacheProperty();
         if (cacheProperty == null) {
            return;
         }

         Identifier gunId = iGun.getGunId(mainHandItem);
         Optional<ClientGunIndex> gunIndexOptional = TimelessAPI.getClientGunIndex(gunId);
         if (gunIndexOptional.isEmpty()) {
            return;
         }

         ClientGunIndex gunIndex = gunIndexOptional.get();
         GunData gunData = gunIndex.getGunData();
         ParameterizedCachePair<Float, Float> attachmentRecoilModifier = cacheProperty.getCache(RecoilModifier.ID);
         IClientPlayerGunOperator clientPlayerGunOperator = IClientPlayerGunOperator.fromLocalPlayer(player);
         float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
         float aimingProgress = clientPlayerGunOperator.getClientAimingProgress(partialTicks);
         float zoom = iGun.getAimingZoom(mainHandItem);
         float aimingRecoilModifier = 1.0F - aimingProgress + aimingProgress / (float)Math.min(Math.sqrt(zoom), 1.5);
         if (!player.isSwimming() && player.getPose() == Pose.SWIMMING) {
            aimingRecoilModifier *= gunData.getCrawlRecoilMultiplier();
         }

         pitchSplineFunction = gunData.getRecoil().genPitchSplineFunction((float)attachmentRecoilModifier.left().eval(aimingRecoilModifier));
         yawSplineFunction = gunData.getRecoil().genYawSplineFunction((float)attachmentRecoilModifier.right().eval(aimingRecoilModifier));
         shootTimeStamp = System.currentTimeMillis();
         xRotO = 0.0;
         yRotO = 0.0;
      }
   }

   public static void applyCameraRecoil(ComputeCameraAngles event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         long timeTotal = System.currentTimeMillis() - shootTimeStamp;
         if (pitchSplineFunction != null && pitchSplineFunction.isValidPoint(timeTotal)) {
            double value = pitchSplineFunction.value(timeTotal);
            player.setXRot(player.getXRot() - (float)(value - xRotO));

            xRotO = value;
         }

         if (yawSplineFunction != null && yawSplineFunction.isValidPoint(timeTotal)) {
            double value = yawSplineFunction.value(timeTotal);
            player.setYRot(player.getYRot() - (float)(value - yRotO));

            yRotO = value;
         }
      }
   }

   public static void onComputeMovementFov(ComputeFovModifierEvent event) {
      if ((Boolean)RenderConfig.DISABLE_MOVEMENT_ATTRIBUTE_FOV.get()) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            float f = 1.0F;
            if (player.getMainHandItem().getItem() instanceof AbstractGunItem) {
               if (player.getAbilities().flying) {
                  f *= 1.1F;
               }

               event.setNewFovModifier(player.isSprinting() ? 1.15F * f : f);
            }
         }
      }
   }
}
