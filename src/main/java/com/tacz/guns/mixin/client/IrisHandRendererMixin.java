package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.gui.overlay.HandBob;
import com.tacz.guns.client.gui.overlay.ScopeLens;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * With a shader pack Iris draws the held item itself. It leaves the pose stack empty and folds the view bobbing into
 * the projection instead, so the scope lens needs the bob (not vanilla's view matrix) to find the ocular on screen.
 * Only applied when Iris is installed.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pathways.HandRenderer", remap = false)
public abstract class IrisHandRendererMixin {
   @Inject(method = "setupGlState", at = @At("HEAD"), remap = false, require = 0)
   private void tacz$captureHandView(GameRenderer renderer, CameraRenderState camera, Matrix4fc view, float partialTick, CallbackInfoReturnable<PoseStack> cir) {
      PoseStack stack = new PoseStack();
      stack.pushPose();
      ((HandBob)renderer).tacz$applyBob(camera, stack);
      ScopeLens.setHandView(new Matrix4f(stack.last().pose()), camera.hudFov);
   }
}
