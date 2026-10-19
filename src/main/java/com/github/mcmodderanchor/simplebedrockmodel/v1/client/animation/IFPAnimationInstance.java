package com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation;

import com.maydaymemory.mae.basic.Pose;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public interface IFPAnimationInstance {
   ItemStack currentItem();

   Pose getPose();

   void tick(float partialTick);

   @NotNull
   Quaternionf getCameraRotation();

   void setCameraRotation(@NotNull Quaternionf rotation);

   Pose getCachedPose();

   void updateItem(ItemStack stack);

   void triggerDraw();

   void triggerPutAway();

   default boolean shouldRenderHand() {
      return false;
   }
}
