package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Fires the camera angle and field of view events that the recoil and scope code listens to. */
@Mixin(Camera.class)
public abstract class CameraMixin {
   @Unique
   private float tacz$roll;

   @WrapOperation(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
   private void tacz$computeCameraAngles(Camera instance, float yRot, float xRot, Operation<Void> original, @Local(argsOnly = true) float partialTick) {
      ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles(instance, partialTick, yRot, xRot, 0.0F);
      ((ViewportEvent.CameraCallback)ViewportEvent.CAMERA.invoker()).post(event);
      this.tacz$roll = event.getRoll();
      original.call(instance, event.getYaw(), event.getPitch());
   }

   @ModifyArg(method = "setRotation", at = @At(remap = false, value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;"), index = 2)
   private float tacz$setRollValue(float value) {
      return value + -this.tacz$roll * (float)(Math.PI / 180.0);
   }

   @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
   private float tacz$worldFov(float original, @Local(argsOnly = true) float partialTick) {
      ViewportEvent.ComputeFov event = new ViewportEvent.ComputeFov(Minecraft.getInstance().gameRenderer, (Camera)(Object)this, partialTick, original, true);
      ((ViewportEvent.FovCallback)ViewportEvent.FOV.invoker()).post(event);
      return (float)event.getFOV();
   }

   @ModifyReturnValue(method = "calculateHudFov", at = @At("RETURN"))
   private float tacz$handFov(float original, @Local(argsOnly = true) float partialTick) {
      ViewportEvent.ComputeFov event = new ViewportEvent.ComputeFov(Minecraft.getInstance().gameRenderer, (Camera)(Object)this, partialTick, original, false);
      ((ViewportEvent.FovCallback)ViewportEvent.FOV.invoker()).post(event);
      return (float)event.getFOV();
   }
}
