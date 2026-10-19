package com.tacz.guns.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class ModelRendererWrapper {
   private final BedrockPart modelRenderer;

   public ModelRendererWrapper(BedrockPart modelRenderer) {
      this.modelRenderer = modelRenderer;
   }

   public BedrockPart getModelRenderer() {
      return this.modelRenderer;
   }

   public float getRotateAngleX() {
      return this.modelRenderer.xRot;
   }

   public void setRotateAngleX(float xRot) {
      this.modelRenderer.xRot = xRot;
   }

   public float getRotateAngleY() {
      return this.modelRenderer.yRot;
   }

   public void setRotateAngleY(float yRot) {
      this.modelRenderer.yRot = yRot;
   }

   public float getRotateAngleZ() {
      return this.modelRenderer.zRot;
   }

   public void setRotateAngleZ(float zRot) {
      this.modelRenderer.zRot = zRot;
   }

   public float getOffsetX() {
      return this.modelRenderer.offsetX;
   }

   public void setOffsetX(float offsetX) {
      this.modelRenderer.offsetX = offsetX;
   }

   public float getOffsetY() {
      return this.modelRenderer.offsetY;
   }

   public void setOffsetY(float offsetY) {
      this.modelRenderer.offsetY = offsetY;
   }

   public float getOffsetZ() {
      return this.modelRenderer.offsetZ;
   }

   public void setOffsetZ(float offsetZ) {
      this.modelRenderer.offsetZ = offsetZ;
   }

   public void addOffsetX(float offsetX) {
      this.setOffsetX(this.getOffsetX() + offsetX);
   }

   public void addOffsetY(float offsetY) {
      this.setOffsetY(this.getOffsetY() + offsetY);
   }

   public void addOffsetZ(float offsetZ) {
      this.setOffsetZ(this.getOffsetZ() + offsetZ);
   }

   public float getRotationPointX() {
      return this.modelRenderer.x;
   }

   public float getRotationPointY() {
      return this.modelRenderer.y;
   }

   public float getRotationPointZ() {
      return this.modelRenderer.z;
   }

   public boolean isHidden() {
      return !this.modelRenderer.visible;
   }

   public void setHidden(boolean hidden) {
      this.modelRenderer.visible = !hidden;
   }

   public float getInitRotateAngleX() {
      return this.modelRenderer.getInitRotX();
   }

   public float getInitRotateAngleY() {
      return this.modelRenderer.getInitRotY();
   }

   public float getInitRotateAngleZ() {
      return this.modelRenderer.getInitRotZ();
   }

   public Quaternionf getAdditionalQuaternion() {
      return this.modelRenderer.additionalQuaternion;
   }

   public void setAdditionalQuaternion(Quaternionf quaternion) {
      this.modelRenderer.additionalQuaternion = quaternion;
   }

   public float getScaleX() {
      return this.modelRenderer.xScale;
   }

   public void setScaleX(float scaleX) {
      this.modelRenderer.xScale = scaleX;
   }

   public float getScaleY() {
      return this.modelRenderer.yScale;
   }

   public void setScaleY(float scaleY) {
      this.modelRenderer.yScale = scaleY;
   }

   public float getScaleZ() {
      return this.modelRenderer.zScale;
   }

   public void setScaleZ(float scaleZ) {
      this.modelRenderer.zScale = scaleZ;
   }

   public void render(PoseStack poseStack, ItemDisplayContext transformType, VertexConsumer consumer, int light, int overlay) {
      this.modelRenderer.render(poseStack, transformType, consumer, light, overlay);
   }
}
