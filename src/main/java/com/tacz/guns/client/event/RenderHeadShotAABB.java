package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.RenderLivingEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/** Debug overlay: draws an entity's headshot box when hit boxes are shown. */
@Environment(EnvType.CLIENT)
public class RenderHeadShotAABB {
   public static void onRenderEntity(RenderLivingEvent.Post event) {
      if (!Minecraft.getInstance().debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES) || !(Boolean)RenderConfig.HEAD_SHOT_DEBUG_HITBOX.get()) {
         return;
      }

      LivingEntity entity = event.getEntity();
      Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      AABB aabb = HeadShotAABBConfigRead.getAABB(entityId);
      if (aabb == null) {
         float width = entity.getBbWidth();
         float eyeHeight = entity.getEyeHeight();
         aabb = new AABB(-width / 2.0F, eyeHeight - 0.25, -width / 2.0F, width / 2.0F, eyeHeight + 0.25, width / 2.0F).inflate(0.01);
      }

      final AABB box = aabb;
      PoseStack poseStack = event.getPoseStack();
      event.getCollector().submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> drawBox(pose, buffer, box));
   }

   private static void drawBox(PoseStack.Pose pose, VertexConsumer buffer, AABB b) {
      float[][] c = {
         {(float)b.minX, (float)b.minY, (float)b.minZ}, {(float)b.maxX, (float)b.minY, (float)b.minZ},
         {(float)b.maxX, (float)b.minY, (float)b.maxZ}, {(float)b.minX, (float)b.minY, (float)b.maxZ},
         {(float)b.minX, (float)b.maxY, (float)b.minZ}, {(float)b.maxX, (float)b.maxY, (float)b.minZ},
         {(float)b.maxX, (float)b.maxY, (float)b.maxZ}, {(float)b.minX, (float)b.maxY, (float)b.maxZ}
      };
      int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
      for (int[] edge : edges) {
         float[] a = c[edge[0]];
         float[] d = c[edge[1]];
         float nx = d[0] - a[0];
         float ny = d[1] - a[1];
         float nz = d[2] - a[2];
         float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
         nx /= len;
         ny /= len;
         nz /= len;
         buffer.addVertex(pose, a[0], a[1], a[2]).setColor(255, 255, 0, 255).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
         buffer.addVertex(pose, d[0], d[1], d[2]).setColor(255, 255, 0, 255).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
      }
   }
}
