package com.tacz.guns.client.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/** Implemented by GameRenderer: the view bobbing the first-person hand gets, applied to a pose stack. */
public interface HandBob {
   void tacz$applyBob(CameraRenderState cameraState, PoseStack poseStack);
}
