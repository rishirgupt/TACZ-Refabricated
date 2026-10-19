package com.tacz.guns.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Magnified scope lens for the held gun.
 *
 * 26.2 has no stencil buffer, and a second world render would fight with shader packs, so the lens is built from the
 * finished frame instead: the main framebuffer (with whatever shader pack drew it) is copied once the world is done,
 * and a magnified crop of it is drawn into the lens outline on the HUD. The crop is centered on the point the scope
 * is pointing at, so it shows what the barrel is aimed at, not what the player's crosshair is on.
 */
public final class ScopeLens {
   private static final Matrix4f HAND_VIEW = new Matrix4f();
   private static final Matrix4f COMMIT_VIEW = new Matrix4f();
   private static final Matrix4f COMMIT_VIEW_TMP = new Matrix4f();
   private static float commitFov = 70.0F;
   private static float handFov = 70.0F;
   private static boolean haveHandView;

   private static final List<float[]> POINTS = new ArrayList<>();
   private static final List<List<float[]>> PATHS = new ArrayList<>();
   private static List<float[]> current = new ArrayList<>();
   private static final Vector3f AXIS = new Vector3f();
   private static boolean active;
   private static boolean copyPending;

   private static com.mojang.blaze3d.pipeline.RenderPipeline pipeline;
   private static GpuTexture frame;
   private static GpuTextureView frameView;
   private static GpuSampler sampler;

   private ScopeLens() {
   }

   /** Records the matrices the first-person item is drawn with (see GameRendererMixin). */
   public static void setHandView(Matrix4fc view, float fov) {
      HAND_VIEW.set(view);
      handFov = fov;
      haveHandView = true;
   }

   /** Collects the vertices of the lens mesh while it is walked with the gun's pose. */
   public static VertexConsumer pointSink() {
      return SINK;
   }

   /** Marks the collected points as this frame's lens; {@code axis} is the scope's forward direction in pose space. */
   public static void commit(float axisX, float axisY, float axisZ) {
      List<float[]> best = null;
      float bestDepth = Float.MAX_VALUE;
      for (List<float[]> path : PATHS) {
         if (path.size() < 3) {
            continue;
         }

         Vector4f c = new Vector4f(0, 0, 0, 1);
         for (float[] p : path) {
            c.add(p[0], p[1], p[2], 0);
         }

         c.set(c.x / path.size(), c.y / path.size(), c.z / path.size(), 1.0F);
         HAND_VIEW.transform(c);
         if (-c.z < bestDepth) {
            bestDepth = -c.z;
            best = path;
         }
      }

      if (haveHandView && best != null) {
         POINTS.clear();
         COMMIT_VIEW_TMP.set(HAND_VIEW);
         POINTS.addAll(rearFace(best));
         COMMIT_VIEW.set(HAND_VIEW);
         commitFov = handFov;
         AXIS.set(axisX, axisY, axisZ);
         active = true;
      }
   }

   /**
    * A lens modelled as a thick disc (or a few stacked strips) has rear and front faces and side walls. The glass the
    * player sees is the faces that point at the camera and share the dominant normal.
    */
   private static List<float[]> rearFace(List<float[]> path) {
      int quads = path.size() / 4;
      Vector3f[] normals = new Vector3f[quads];
      float[] areas = new float[quads];
      boolean[] facing = new boolean[quads];
      Vector3f e1 = new Vector3f();
      Vector3f e2 = new Vector3f();
      Vector4f c = new Vector4f();
      Vector4f nv = new Vector4f();
      for (int q = 0; q < quads; q++) {
         float[] p0 = path.get(q * 4);
         float[] p1 = path.get(q * 4 + 1);
         float[] p2 = path.get(q * 4 + 2);
         float[] p3 = path.get(q * 4 + 3);
         e1.set(p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2]);
         e2.set(p2[0] - p0[0], p2[1] - p0[1], p2[2] - p0[2]);
         areas[q] = e1.cross(e2, new Vector3f()).length();
         if (areas[q] < 1.0E-9F) {
            continue;
         }

         Vector3f n = new Vector3f(p0[3] + p1[3] + p2[3] + p3[3], p0[4] + p1[4] + p2[4] + p3[4], p0[5] + p1[5] + p2[5] + p3[5]);
         if (n.lengthSquared() < 1.0E-8F) {
            n = e1.cross(e2, new Vector3f());
         }

         normals[q] = n.normalize();
         c.set((p0[0] + p1[0] + p2[0] + p3[0]) * 0.25F, (p0[1] + p1[1] + p2[1] + p3[1]) * 0.25F, (p0[2] + p1[2] + p2[2] + p3[2]) * 0.25F, 1.0F);
         COMMIT_VIEW_TMP.transform(c);
         nv.set(normals[q].x, normals[q].y, normals[q].z, 0.0F);
         COMMIT_VIEW_TMP.transform(nv);
         facing[q] = nv.x * -c.x + nv.y * -c.y + nv.z * -c.z > 0.0F;
      }

      for (int pass = 0; pass < 2; pass++) {
         int best = -1;
         float bestArea = 0.0F;
         for (int a = 0; a < quads; a++) {
            if (normals[a] == null || (pass == 0 && !facing[a])) {
               continue;
            }

            float sum = 0.0F;
            for (int b = 0; b < quads; b++) {
               if (normals[b] != null && (pass == 1 || facing[b]) && normals[a].dot(normals[b]) > 0.97F) {
                  sum += areas[b];
               }
            }

            if (sum > bestArea) {
               bestArea = sum;
               best = a;
            }
         }

         if (best >= 0) {
            List<float[]> result = new ArrayList<>();
            for (int b = 0; b < quads; b++) {
               if (normals[b] != null && (pass == 1 || facing[b]) && normals[best].dot(normals[b]) > 0.97F) {
                  for (int i = 0; i < 4; i++) {
                     result.add(path.get(b * 4 + i));
                  }
               }
            }

            return result;
         }
      }

      return path;
   }

   public static void abandon() {
      PATHS.clear();
   }

   /** Starts a new lens mesh; a scope can have several oculars (eyepiece and objective) and only the nearest is the visible lens. */
   public static void startPath() {
      current = new ArrayList<>();
      PATHS.add(current);
   }

   private static final VertexConsumer SINK = new VertexConsumer() {
      @Override
      public VertexConsumer addVertex(float x, float y, float z) {
         current.add(new float[]{x, y, z, 0.0F, 0.0F, 0.0F});
         return this;
      }

      @Override
      public VertexConsumer setColor(int r, int g, int b, int a) {
         return this;
      }

      @Override
      public VertexConsumer setColor(int color) {
         return this;
      }

      @Override
      public VertexConsumer setUv(float u, float v) {
         return this;
      }

      @Override
      public VertexConsumer setUv1(int u, int v) {
         return this;
      }

      @Override
      public VertexConsumer setUv2(int u, int v) {
         return this;
      }

      @Override
      public VertexConsumer setNormal(float x, float y, float z) {
         if (!current.isEmpty()) {
            float[] last = current.get(current.size() - 1);
            last[3] = x;
            last[4] = y;
            last[5] = z;
         }

         return this;
      }

      @Override
      public VertexConsumer setLineWidth(float width) {
         return this;
      }
   };

   private static Matrix4f projection() {
      Minecraft mc = Minecraft.getInstance();
      float aspect = (float)mc.getWindow().getWidth() / (float)Math.max(1, mc.getWindow().getHeight());
      return new Matrix4f().perspective((float)Math.toRadians(commitFov), aspect, 0.05F, 512.0F);
   }

   /** Projects pose-space positions (or directions) onto the screen as 0..1 fractions; false if behind the camera. */
   private static boolean project(Matrix4f projection, float x, float y, float z, float w, float[] out) {
      Vector4f v = new Vector4f(x, y, z, w);
      COMMIT_VIEW.transform(v);
      projection.transform(v);
      if (v.w <= 1.0E-4F) {
         return false;
      }

      out[0] = v.x / v.w * 0.5F + 0.5F;
      out[1] = 0.5F - v.y / v.w * 0.5F;
      return true;
   }

   private static List<float[]> convexHull(List<float[]> pts) {
      List<float[]> sorted = new ArrayList<>(pts);
      sorted.sort(Comparator.<float[]>comparingDouble(p -> p[0]).thenComparingDouble(p -> p[1]));
      int n = sorted.size();
      float[][] hull = new float[2 * n][];
      int k = 0;
      for (int i = 0; i < n; i++) {
         while (k >= 2 && cross(hull[k - 2], hull[k - 1], sorted.get(i)) <= 0) {
            k--;
         }

         hull[k++] = sorted.get(i);
      }

      for (int i = n - 2, t = k + 1; i >= 0; i--) {
         while (k >= t && cross(hull[k - 2], hull[k - 1], sorted.get(i)) <= 0) {
            k--;
         }

         hull[k++] = sorted.get(i);
      }

      List<float[]> result = new ArrayList<>();
      for (int i = 0; i < k - 1; i++) {
         result.add(hull[i]);
      }

      return result;
   }

   private static float cross(float[] o, float[] a, float[] b) {
      return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
   }

   private static void ensureFrame(GpuTexture source) {
      if (frame != null && !frame.isClosed() && frame.getWidth(0) == source.getWidth(0) && frame.getHeight(0) == source.getHeight(0)) {
         return;
      }

      release();
      var device = RenderSystem.getDevice();
      frame = device.createTexture("tacz_scope_lens", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, source.getFormat(), source.getWidth(0), source.getHeight(0), 1, 1);
      frameView = device.createTextureView(frame);
      if (sampler == null) {
         sampler = device.createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty());
      }
   }

   /** Copies the finished world image just before the GUI is drawn, when shader packs have composited it too. */
   public static void captureFrame() {
      if (!copyPending) {
         return;
      }

      copyPending = false;
      GpuTexture source = Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTexture();
      if (source != null && frame != null && !frame.isClosed() && frame.getWidth(0) == source.getWidth(0) && frame.getHeight(0) == source.getHeight(0)) {
         RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(source, frame, 0, 0, 0, 0, 0, source.getWidth(0), source.getHeight(0));
      }
   }

   private static com.mojang.blaze3d.pipeline.RenderPipeline lensPipeline() {
      if (pipeline == null) {
         pipeline = com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("tacz", "pipeline/scope_lens"))
            .withFragmentShader(net.minecraft.resources.Identifier.fromNamespaceAndPath("tacz", "core/scope_lens"))
            .build();
      }

      return pipeline;
   }

   public static void release() {
      if (frameView != null) {
         frameView.close();
         frameView = null;
      }

      if (frame != null) {
         frame.close();
         frame = null;
      }
   }

   /** Draws the lens for this frame, if the scope was part of it. Runs after the world, before the rest of the HUD. */
   public static void render(GuiGraphicsExtractor graphics) {
      if (!active) {
         return;
      }

      active = false;
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null || ScopeOverlay.shouldHideGun()) {
         return;
      }

      ItemStack stack = player.getMainHandItem();
      if (!(stack.getItem() instanceof IGun iGun) || !(player instanceof IClientPlayerGunOperator operator)) {
         return;
      }

      Matrix4f projection = projection();
      List<float[]> screen = new ArrayList<>();
      float[] tmp = new float[2];
      for (float[] p : POINTS) {
         if (!project(projection, p[0], p[1], p[2], 1.0F, tmp)) {
            return;
         }

         screen.add(new float[]{tmp[0], tmp[1]});
      }

      float[] vanishing = new float[2];
      if (!project(projection, AXIS.x, AXIS.y, AXIS.z, 0.0F, vanishing)) {
         return;
      }

      List<float[]> hull = convexHull(screen);
      if (hull.size() < 3) {
         return;
      }

      float width = graphics.guiWidth();
      float height = graphics.guiHeight();
      float minY = Float.MAX_VALUE;
      float maxY = -Float.MAX_VALUE;
      float cx = 0.0F;
      float cy = 0.0F;
      for (float[] p : hull) {
         p[0] *= width;
         p[1] *= height;
         minY = Math.min(minY, p[1]);
         maxY = Math.max(maxY, p[1]);
         cx += p[0];
         cy += p[1];
      }

      cx /= hull.size();
      cy /= hull.size();
      if (maxY - minY < 3.0F || maxY - minY > height) {
         return;
      }

      // The world is already magnified by the aiming fov; the lens supplies the rest of the scope's power.
      float zoom = Math.max(1.0F, iGun.getAimingZoom(stack));
      float aiming = operator.getClientAimingProgress(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
      float lensZoom = Math.max(1.0F, zoom / (1.0F + (zoom - 1.0F) * aiming));

      GpuTexture source = mc.gameRenderer.mainRenderTarget().getColorTexture();
      if (source == null) {
         return;
      }

      ensureFrame(source);
      copyPending = true;

      int rowStep = (maxY - minY) > 240.0F ? 2 : 1;
      int top = (int)Math.floor(minY);
      int bottom = (int)Math.ceil(maxY);
      float radius = (maxY - minY) * 0.5F;
      for (int y = top; y < bottom; y += rowStep) {
         float yc = y + rowStep * 0.5F;
         float xa = Float.MAX_VALUE;
         float xb = -Float.MAX_VALUE;
         for (int i = 0; i < hull.size(); i++) {
            float[] a = hull.get(i);
            float[] b = hull.get((i + 1) % hull.size());
            if ((a[1] <= yc && b[1] > yc) || (b[1] <= yc && a[1] > yc)) {
               float x = a[0] + (yc - a[1]) / (b[1] - a[1]) * (b[0] - a[0]);
               xa = Math.min(xa, x);
               xb = Math.max(xb, x);
            }
         }

         if (xb - xa < 1.0F) {
            continue;
         }

         int x0 = Math.round(xa);
         int x1 = Math.round(xb);
         float u0 = vanishing[0] + (x0 - cx) / (lensZoom * width);
         float u1 = vanishing[0] + (x1 - cx) / (lensZoom * width);
         float v = 1.0F - (vanishing[1] + (yc - cy) / (lensZoom * height)); // framebuffer rows run bottom-up
         // Shader packs leave alpha at 0 in the frame, so the lens is drawn with a pipeline that ignores it.
         ((com.tacz.guns.mixin.client.GuiGraphicsExtractorAccessor)graphics).tacz$innerBlit(
            lensPipeline(), frameView, sampler, x0, y, x1, y + rowStep, u0, u1, v, v, -1
         );

         // Glass: faint cool tint, and a dark rim that gives the lens depth.
         graphics.fill(x0, y, x1, y + rowStep, 0x1A1E3A5A);
         if (yc - minY < 2.0F || maxY - yc < 2.0F) {
            graphics.fill(x0, y, x1, y + rowStep, 0xA0000000);
         } else {
            graphics.fill(x0, y, x0 + 2, y + rowStep, 0xA0000000);
            graphics.fill(x1 - 2, y, x1, y + rowStep, 0xA0000000);
         }
      }

      // Fine reticle through the middle of the lens.
      int lcx = Math.round(cx);
      int lcy = Math.round(cy);
      int arm = Math.max(3, (int)(radius * 0.8F));
      int gap = Math.max(1, arm / 8);
      int line = 0xE0000000;
      graphics.fill(lcx - arm, lcy, lcx - gap, lcy + 1, line);
      graphics.fill(lcx + gap, lcy, lcx + arm, lcy + 1, line);
      graphics.fill(lcx, lcy - arm, lcx + 1, lcy - gap, line);
      graphics.fill(lcx, lcy + gap, lcx + 1, lcy + arm, line);
      graphics.fill(lcx, lcy, lcx + 1, lcy + 1, 0xFFE02020);
   }
}
