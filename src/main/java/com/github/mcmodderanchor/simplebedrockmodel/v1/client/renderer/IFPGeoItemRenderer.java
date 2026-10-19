package com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation.IFPAnimationInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IFPGeoItemRenderer {
   default boolean isSameItem(ItemStack oldStack, ItemStack newStack) {
      return ItemStack.matches(oldStack, newStack);
   }

   @Nullable
   default IFPAnimationInstance createAnimationInstance(ItemStack stack, Entity entity) {
      return null;
   }

   default long getPutAwayDuration(ItemStack stack) {
      return 0L;
   }

   default boolean blockOffhandRender() {
      return false;
   }

   void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick);
}
