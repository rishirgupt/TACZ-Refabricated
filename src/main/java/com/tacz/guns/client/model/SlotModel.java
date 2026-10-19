package com.tacz.guns.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.bedrock.BedrockCubePerFace;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.pojo.model.FaceUVsItem;
import net.minecraft.world.item.ItemDisplayContext;

/** A single flat quad showing an item's 2D slot picture, drawn in model space. */
public class SlotModel {
   private final BedrockPart bone = new BedrockPart("slot");

   public SlotModel(boolean illuminated) {
      this.bone.setPos(8.0F, 24.0F, -10.0F);
      this.bone.cubes.add(new BedrockCubePerFace(-16.0F, -16.0F, 9.5F, 16.0F, 16.0F, 0.0F, 0.0F, 16.0F, 16.0F, FaceUVsItem.singleSouthFace()));
      this.bone.illuminated = illuminated;
   }

   public SlotModel() {
      this(false);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.bone.render(poseStack, ItemDisplayContext.GUI, buffer, packedLight, packedOverlay);
   }
}
