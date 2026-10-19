package com.tacz.guns.client.model.bedrock;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.LightCoordsUtil;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class BedrockPart {
   @Nullable
   public final String name;
   public final ObjectList<BedrockCube> cubes = new ObjectArrayList();
   public final ObjectList<BedrockPart> children = new ObjectArrayList();
   public float x;
   public float y;
   public float z;
   public float xRot;
   public float yRot;
   public float zRot;
   public float offsetX;
   public float offsetY;
   public float offsetZ;
   public boolean visible = true;
   public boolean illuminated = false;
   public boolean mirror;
   public Quaternionf additionalQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   public float xScale = 1.0F;
   public float yScale = 1.0F;
   public float zScale = 1.0F;
   protected BedrockPart parent;
   private float initRotX;
   private float initRotY;
   private float initRotZ;

   public BedrockPart(@Nullable String name) {
      this.name = name;
   }

   public void setPos(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void render(PoseStack poseStack, ItemDisplayContext transformType, VertexConsumer consumer, int light, int overlay) {
      this.render(poseStack, transformType, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void render(
      PoseStack poseStack, ItemDisplayContext transformType, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha
   ) {
      int cubePackedLight = light;
      if (this.illuminated) {
         cubePackedLight = LightCoordsUtil.pack(15, 15);
      }

      if (this.visible && (!this.cubes.isEmpty() || !this.children.isEmpty())) {
         poseStack.pushPose();
         this.translateAndRotateAndScale(poseStack);
         this.compile(poseStack.last(), consumer, cubePackedLight, overlay, red, green, blue, alpha);
         ObjectListIterator var11 = this.children.iterator();

         while (var11.hasNext()) {
            BedrockPart part = (BedrockPart)var11.next();
            part.render(poseStack, transformType, consumer, cubePackedLight, overlay, red, green, blue, alpha);
         }

         poseStack.popPose();
      }
   }

   public void translateAndRotateAndScale(PoseStack poseStack) {
      poseStack.translate(this.offsetX, this.offsetY, this.offsetZ);
      poseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
      if (this.zRot != 0.0F) {
         poseStack.mulPose(Axis.ZP.rotation(this.zRot));
      }

      if (this.yRot != 0.0F) {
         poseStack.mulPose(Axis.YP.rotation(this.yRot));
      }

      if (this.xRot != 0.0F) {
         poseStack.mulPose(Axis.XP.rotation(this.xRot));
      }

      poseStack.mulPose(this.additionalQuaternion);
      poseStack.scale(this.xScale, this.yScale, this.zScale);
   }

   public void compile(Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      ObjectListIterator var9 = this.cubes.iterator();

      while (var9.hasNext()) {
         BedrockCube bedrockCube = (BedrockCube)var9.next();
         bedrockCube.compile(pose, consumer, light, overlay, red, green, blue, alpha);
      }
   }

   public BedrockCube getRandomCube(Random random) {
      return (BedrockCube)this.cubes.get(random.nextInt(this.cubes.size()));
   }

   public boolean isEmpty() {
      return this.cubes.isEmpty();
   }

   public void setInitRotationAngle(float x, float y, float z) {
      this.initRotX = x;
      this.initRotY = y;
      this.initRotZ = z;
   }

   public float getInitRotX() {
      return this.initRotX;
   }

   public float getInitRotY() {
      return this.initRotY;
   }

   public float getInitRotZ() {
      return this.initRotZ;
   }

   public void addChild(BedrockPart model) {
      this.children.add(model);
   }

   public BedrockPart getParent() {
      return this.parent;
   }
}
