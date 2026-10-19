package com.tacz.guns.client.model;

import com.tacz.guns.client.model.bedrock.BedrockPart;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;

public class FunctionalBedrockPart extends BedrockPart {
   @Nullable
   public Function<BedrockPart, IFunctionalRenderer> functionalRenderer;

   public FunctionalBedrockPart(@Nullable Function<BedrockPart, IFunctionalRenderer> functionalRenderer, @Nonnull String name) {
      super(name);
      this.functionalRenderer = functionalRenderer;
   }

   public FunctionalBedrockPart(@Nullable Function<BedrockPart, IFunctionalRenderer> functionalRenderer, @Nonnull BedrockPart part) {
      super(part.name);
      this.cubes.addAll(part.cubes);
      this.children.addAll(part.children);
      this.x = part.x;
      this.y = part.y;
      this.z = part.z;
      this.xRot = part.xRot;
      this.yRot = part.yRot;
      this.zRot = part.zRot;
      this.offsetX = part.offsetX;
      this.offsetY = part.offsetY;
      this.offsetZ = part.offsetZ;
      this.visible = part.visible;
      this.mirror = part.mirror;
      this.setInitRotationAngle(part.getInitRotX(), part.getInitRotY(), part.getInitRotZ());
      this.xScale = part.xScale;
      this.yScale = part.yScale;
      this.zScale = part.zScale;
      this.functionalRenderer = functionalRenderer;
   }

   @Override
   public void render(
      PoseStack poseStack, ItemDisplayContext transformType, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha
   ) {
      int cubePackedLight = light;
      if (this.illuminated) {
         cubePackedLight = LightCoordsUtil.pack(15, 15);
      }

      poseStack.pushPose();
      this.translateAndRotateAndScale(poseStack);
      if (this.functionalRenderer != null) {
         IFunctionalRenderer renderer = this.functionalRenderer.apply(this);
         if (renderer != null) {
            renderer.render(poseStack, consumer, transformType, cubePackedLight, overlay);
         } else if (this.visible) {
            super.compile(poseStack.last(), consumer, cubePackedLight, overlay, red, green, blue, alpha);
            ObjectListIterator part = this.children.iterator();

            while (part.hasNext()) {
               BedrockPart partx = (BedrockPart)part.next();
               partx.render(poseStack, transformType, consumer, cubePackedLight, overlay, red, green, blue, alpha);
            }
         }
      } else if (this.visible) {
         super.compile(poseStack.last(), consumer, cubePackedLight, overlay, red, green, blue, alpha);
         ObjectListIterator var14 = this.children.iterator();

         while (var14.hasNext()) {
            BedrockPart part = (BedrockPart)var14.next();
            part.render(poseStack, transformType, consumer, cubePackedLight, overlay, red, green, blue, alpha);
         }
      }

      poseStack.popPose();
   }
}
