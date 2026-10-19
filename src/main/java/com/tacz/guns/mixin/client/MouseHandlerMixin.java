package com.tacz.guns.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.config.client.ZoomConfig;
import com.tacz.guns.util.math.MathUtil;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.Pose;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
   @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
   public void reduceSensitivity(LocalPlayer player, double yaw, double pitch, Operation<Void> original) {
      ItemStack mainHandItem = player.getMainHandItem();
      IGun iGun = IGun.getIGunOrNull(mainHandItem);
      if (iGun == null) {
         original.call(new Object[]{player, yaw, pitch});
      } else {
         Identifier scopeId = iGun.getAttachmentId(mainHandItem, AttachmentType.SCOPE);
         if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = iGun.getBuiltInAttachmentId(mainHandItem, AttachmentType.SCOPE);
         }

         float zoomLevel = 1.0F;
         if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            zoomLevel = TimelessAPI.getGunDisplay(mainHandItem).map(GunDisplayInstance::getIronZoom).orElse(1.0F);
         } else {
            Optional<ClientAttachmentIndex> optional = TimelessAPI.getClientAttachmentIndex(scopeId);
            if (optional.isPresent()) {
               float[] zoom = optional.get().getZoom();
               if (zoom != null && zoom.length > 0) {
                  CompoundTag attachmentTag = iGun.getAttachmentTag(mainHandItem, AttachmentType.SCOPE);
                  zoomLevel = zoom[AttachmentItemDataAccessor.getZoomNumberFromTag(attachmentTag) % zoom.length];
               }
            }
         }

         Minecraft minecraft = Minecraft.getInstance();
         float progress = IGunOperator.fromLivingEntity(player).getSynAimingProgress();
         double sensitivityMultiplier = (Double)ZoomConfig.ZOOM_SENSITIVITY_BASE_MULTIPLIER.get();
         sensitivityMultiplier = 1.0 + (sensitivityMultiplier - 1.0) * progress;
         double originalFov = ((Integer)minecraft.options.fov().get()).intValue();
         double currentFov = MathUtil.magnificationToFov(1.0F + (zoomLevel - 1.0F) * progress, originalFov);
         double coefficient = (Double)ZoomConfig.SCREEN_DISTANCE_COEFFICIENT.get();
         double denominator = MathUtil.zoomSensitivityRatio(currentFov, originalFov, coefficient) * sensitivityMultiplier;
         double finalYaw = yaw * denominator;
         double finalPitch = getCrawlPitch(player, pitch, denominator);
         original.call(new Object[]{player, finalYaw, finalPitch});
      }
   }

   @Unique
   private static double getCrawlPitch(LocalPlayer player, double pitch, double denominator) {
      double finalPitch = pitch * denominator;
      if (!player.isSwimming() && player.getPose() == Pose.SWIMMING) {
         float playerPitch = -player.getXRot();
         if (playerPitch > 45.0F) {
            finalPitch = Math.max(finalPitch, 0.0);
         }

         if (playerPitch < -30.0F) {
            finalPitch = Math.min(finalPitch, 0.0);
         }
      }

      return finalPitch;
   }
}
