package com.tacz.guns.client.model.functional;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.resource.pojo.display.gun.ShellEjection;
import com.tacz.guns.compat.iris.IrisCompat;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ShellRender implements IFunctionalRenderer {
   private final ConcurrentLinkedDeque<ShellRender.Data> SHELL_QUEUE = new ConcurrentLinkedDeque<>();
   public static boolean isSelf = false;
   private final BedrockGunModel bedrockGunModel;

   public ShellRender(BedrockGunModel bedrockGunModel) {
      this.bedrockGunModel = bedrockGunModel;
   }

   public void addShell(Vector3f randomVelocity) {
      if (this.SHELL_QUEUE.size() > 128) {
         this.SHELL_QUEUE.pollFirst();
      }

      double xRandom = Math.random() * randomVelocity.x();
      double yRandom = Math.random() * randomVelocity.y();
      double zRandom = Math.random() * randomVelocity.z();
      Vector3f vector3f = new Vector3f((float)xRandom, (float)yRandom, (float)zRandom);
      this.SHELL_QUEUE.offerLast(new ShellRender.Data(System.currentTimeMillis(), vector3f));
   }

   private void renderShell(GunDisplayInstance display, GunData gunData, PoseStack poseStack, BedrockGunModel gunModel) {
      ShellEjection shellEjection = display.getShellEjection();
      if (shellEjection == null) {
         this.SHELL_QUEUE.clear();
      } else {
         TimelessAPI.getClientAmmoIndex(gunData.getAmmoId())
            .ifPresent(
               ammoIndex -> {
                  BedrockAmmoModel model = ammoIndex.getShellModel();
                  if (model != null) {
                     Identifier location = ammoIndex.getShellTextureLocation();
                     if (location != null) {
                        long lifeTime = (long)(shellEjection.getLivingTime() * 1000.0F);
                        this.checkShellQueue(lifeTime);
                        Vector3f initialVelocity = shellEjection.getInitialVelocity();
                        Vector3f acceleration = shellEjection.getAcceleration();
                        Vector3f angularVelocity = shellEjection.getAngularVelocity();

                        for (ShellRender.Data data : this.SHELL_QUEUE) {
                           if (data.normal == null && data.pose == null) {
                              data.normal = new Matrix3f(poseStack.last().normal());
                              data.pose = new Matrix4f(poseStack.last().pose());
                           }
                        }

                        gunModel.delegateRender(
                           (poseStack1, vertexConsumer1, transformType1, light, overlay) -> this.SHELL_QUEUE
                              .forEach(
                                 datax -> this.renderSingleShell(
                                    transformType1, light, overlay, datax, initialVelocity, acceleration, angularVelocity, model, location
                                 )
                              )
                        );
                     }
                  }
               }
            );
      }
   }

   private void renderSingleShell(
      ItemDisplayContext transformType1,
      int light,
      int overlay,
      ShellRender.Data data,
      Vector3f initialVelocity,
      Vector3f acceleration,
      Vector3f angularVelocity,
      BedrockAmmoModel model,
      Identifier location
   ) {
      if (data.normal != null || data.pose != null) {
         PoseStack poseStack2 = new PoseStack();
         poseStack2.last().normal().mul(data.normal);
         poseStack2.last().pose().mul(data.pose);
         long remindTime = System.currentTimeMillis() - data.timeStamp;
         double time = remindTime / 1000.0;
         Vector3f randomOffset = data.randomOffset;
         double x = (initialVelocity.x() + randomOffset.x()) * time + 0.5 * acceleration.x() * time * time;
         double y = (initialVelocity.y() + randomOffset.y()) * time + 0.5 * acceleration.y() * time * time;
         double z = (initialVelocity.z() + randomOffset.z()) * time + 0.5 * acceleration.z() * time * time;
         poseStack2.translate(-x, -y, z);
         double xw = time * angularVelocity.x();
         double yw = time * angularVelocity.y();
         double zw = time * angularVelocity.z();
         poseStack2.mulPose(Axis.XN.rotationDegrees((float)xw));
         poseStack2.mulPose(Axis.YN.rotationDegrees((float)yw));
         poseStack2.mulPose(Axis.ZP.rotationDegrees((float)zw));
         poseStack2.translate(0.0, -1.5, 0.0);
         model.render(poseStack2, transformType1, RenderTypes.entityCutout(location), light, overlay);
      }
   }

   private void checkShellQueue(long lifeTime) {
      if (!this.SHELL_QUEUE.isEmpty()) {
         ShellRender.Data data = this.SHELL_QUEUE.peekFirst();
         if (System.currentTimeMillis() - data.timeStamp > lifeTime) {
            this.SHELL_QUEUE.pollFirst();
            this.checkShellQueue(lifeTime);
         }
      }
   }

   @Override
   public void render(PoseStack poseStack, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
      if (!IrisCompat.isRenderShadow()) {
         if (isSelf) {
            ItemStack currentGunItem = this.bedrockGunModel.getCurrentGunItem();
            IGun iGun = IGun.getIGunOrNull(currentGunItem);
            if (iGun != null) {
               GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(currentGunItem)).map(ClientGunIndex::getGunData).orElse(null);
               if (gunData != null) {
                  TimelessAPI.getGunDisplay(currentGunItem).ifPresent(display -> this.renderShell(display, gunData, poseStack, this.bedrockGunModel));
               }
            }
         }
      }
   }

   public static class Data {
      public final long timeStamp;
      public final Vector3f randomOffset;
      public Matrix3f normal = null;
      public Matrix4f pose = null;

      public Data(long timeStamp, Vector3f randomOffset) {
         this.timeStamp = timeStamp;
         this.randomOffset = randomOffset;
      }
   }
}
