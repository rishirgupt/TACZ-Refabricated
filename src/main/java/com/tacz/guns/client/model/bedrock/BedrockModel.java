package com.tacz.guns.client.model.bedrock;

import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.client.resource.pojo.model.BonesItem;
import com.tacz.guns.client.resource.pojo.model.CubesItem;
import com.tacz.guns.client.resource.pojo.model.Description;
import com.tacz.guns.client.resource.pojo.model.FaceUVsItem;
import com.tacz.guns.compat.iris.IrisCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.ItemDisplayContext;
import com.tacz.guns.client.render26.MultiBufferSource;

public class BedrockModel {
   public static BedrockModel dummyModel = new BedrockModel();
   protected final HashMap<String, ModelRendererWrapper> modelMap = new HashMap<>();
   protected final HashMap<String, BonesItem> indexBones = new HashMap<>();
   protected final List<BedrockPart> shouldRender = new LinkedList<>();
   protected List<IFunctionalRenderer> delegateRenderers = new ArrayList<>();
   @Nullable
   protected Vec3 offset = null;
   @Nullable
   protected Vec2 size = null;

   public BedrockModel(BedrockModelPOJO pojo, BedrockVersion version) {
      if (version == BedrockVersion.LEGACY) {
         this.loadLegacyModel(pojo);
      }

      if (version == BedrockVersion.NEW) {
         this.loadNewModel(pojo);
      }

      for (ModelRendererWrapper rendererWrapper : this.modelMap.values()) {
         if (rendererWrapper.getModelRenderer().name != null && rendererWrapper.getModelRenderer().name.endsWith("_illuminated")) {
            rendererWrapper.getModelRenderer().illuminated = true;
         }
      }
   }

   protected BedrockModel() {
   }

   public void delegateRender(IFunctionalRenderer renderer) {
      this.delegateRenderers.add(renderer);
   }

   private void setRotationAngle(BedrockPart modelRenderer, float x, float y, float z) {
      modelRenderer.xRot = x;
      modelRenderer.yRot = y;
      modelRenderer.zRot = z;
      modelRenderer.setInitRotationAngle(x, y, z);
   }

   protected void loadNewModel(BedrockModelPOJO pojo) {
      assert pojo.getGeometryModelNew() != null;
      pojo.getGeometryModelNew().deco();
      if (pojo.getGeometryModelNew().getBones() != null) {
         Description description = pojo.getGeometryModelNew().getDescription();
         int texWidth = description.getTextureWidth();
         int texHeight = description.getTextureHeight();
         List<Float> offset = description.getVisibleBoundsOffset();
         float offsetX = offset.get(0);
         float offsetY = offset.get(1);
         float offsetZ = offset.get(2);
         this.offset = new Vec3(offsetX, offsetY, offsetZ);
         float width = description.getVisibleBoundsWidth() / 2.0F;
         float height = description.getVisibleBoundsHeight() / 2.0F;
         this.size = new Vec2(width, height);

         for (BonesItem bones : pojo.getGeometryModelNew().getBones()) {
            this.indexBones.putIfAbsent(bones.getName(), bones);
            this.modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(new BedrockPart(bones.getName())));
         }

         for (BonesItem bones : pojo.getGeometryModelNew().getBones()) {
            String name = bones.getName();
            List<Float> rotation = bones.getRotation();
            String parent = bones.getParent();
            BedrockPart model = this.modelMap.get(name).getModelRenderer();
            model.mirror = bones.isMirror();
            model.setPos(this.convertPivot(bones, 0), this.convertPivot(bones, 1), this.convertPivot(bones, 2));
            if (rotation != null) {
               this.setRotationAngle(model, this.convertRotation(rotation.get(0)), this.convertRotation(rotation.get(1)), this.convertRotation(rotation.get(2)));
            }

            if (parent != null) {
               BedrockPart parentPart = this.modelMap.get(parent).getModelRenderer();
               parentPart.addChild(model);
               model.parent = parentPart;
            } else {
               this.shouldRender.add(model);
               model.parent = null;
            }

            if (bones.getCubes() != null) {
               for (CubesItem cube : bones.getCubes()) {
                  List<Float> uv = cube.getUv();
                  FaceUVsItem faceUv = cube.getFaceUv();
                  List<Float> size = cube.getSize();
                  List<Float> cubeRotation = cube.getRotation();
                  boolean mirror = cube.isMirror();
                  float inflate = cube.getInflate();
                  if (cubeRotation == null) {
                     if (faceUv == null) {
                        model.cubes
                           .add(
                              new BedrockCubeBox(
                                 uv.get(0),
                                 uv.get(1),
                                 this.convertOrigin(bones, cube, 0),
                                 this.convertOrigin(bones, cube, 1),
                                 this.convertOrigin(bones, cube, 2),
                                 size.get(0),
                                 size.get(1),
                                 size.get(2),
                                 inflate,
                                 mirror,
                                 texWidth,
                                 texHeight
                              )
                           );
                     } else {
                        model.cubes
                           .add(
                              new BedrockCubePerFace(
                                 this.convertOrigin(bones, cube, 0),
                                 this.convertOrigin(bones, cube, 1),
                                 this.convertOrigin(bones, cube, 2),
                                 size.get(0),
                                 size.get(1),
                                 size.get(2),
                                 inflate,
                                 texWidth,
                                 texHeight,
                                 faceUv
                              )
                           );
                     }
                  } else {
                     BedrockPart cubeRenderer = new BedrockPart(null);
                     cubeRenderer.setPos(this.convertPivot(bones, cube, 0), this.convertPivot(bones, cube, 1), this.convertPivot(bones, cube, 2));
                     this.setRotationAngle(
                        cubeRenderer,
                        this.convertRotation(cubeRotation.get(0)),
                        this.convertRotation(cubeRotation.get(1)),
                        this.convertRotation(cubeRotation.get(2))
                     );
                     if (faceUv == null) {
                        cubeRenderer.cubes
                           .add(
                              new BedrockCubeBox(
                                 uv.get(0),
                                 uv.get(1),
                                 this.convertOrigin(cube, 0),
                                 this.convertOrigin(cube, 1),
                                 this.convertOrigin(cube, 2),
                                 size.get(0),
                                 size.get(1),
                                 size.get(2),
                                 inflate,
                                 mirror,
                                 texWidth,
                                 texHeight
                              )
                           );
                     } else {
                        cubeRenderer.cubes
                           .add(
                              new BedrockCubePerFace(
                                 this.convertOrigin(cube, 0),
                                 this.convertOrigin(cube, 1),
                                 this.convertOrigin(cube, 2),
                                 size.get(0),
                                 size.get(1),
                                 size.get(2),
                                 inflate,
                                 texWidth,
                                 texHeight,
                                 faceUv
                              )
                           );
                     }

                     model.addChild(cubeRenderer);
                  }
               }
            }
         }
      }
   }

   protected void loadLegacyModel(BedrockModelPOJO pojo) {
      assert pojo.getGeometryModelLegacy() != null;
      pojo.getGeometryModelLegacy().deco();
      if (pojo.getGeometryModelLegacy().getBones() != null) {
         int texWidth = pojo.getGeometryModelLegacy().getTextureWidth();
         int texHeight = pojo.getGeometryModelLegacy().getTextureHeight();
         List<Float> offset = pojo.getGeometryModelLegacy().getVisibleBoundsOffset();
         float offsetX = offset.get(0);
         float offsetY = offset.get(1);
         float offsetZ = offset.get(2);
         this.offset = new Vec3(offsetX, offsetY, offsetZ);
         float width = pojo.getGeometryModelLegacy().getVisibleBoundsWidth() / 2.0F;
         float height = pojo.getGeometryModelLegacy().getVisibleBoundsHeight() / 2.0F;
         this.size = new Vec2(width, height);

         for (BonesItem bones : pojo.getGeometryModelLegacy().getBones()) {
            this.indexBones.putIfAbsent(bones.getName(), bones);
            this.modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(new BedrockPart(bones.getName())));
         }

         for (BonesItem bones : pojo.getGeometryModelLegacy().getBones()) {
            String name = bones.getName();
            List<Float> rotation = bones.getRotation();
            String parent = bones.getParent();
            BedrockPart model = this.modelMap.get(name).getModelRenderer();
            model.mirror = bones.isMirror();
            model.setPos(this.convertPivot(bones, 0), this.convertPivot(bones, 1), this.convertPivot(bones, 2));
            if (rotation != null) {
               this.setRotationAngle(model, this.convertRotation(rotation.get(0)), this.convertRotation(rotation.get(1)), this.convertRotation(rotation.get(2)));
            }

            if (parent != null) {
               this.modelMap.get(parent).getModelRenderer().addChild(model);
            } else {
               this.shouldRender.add(model);
            }

            if (bones.getCubes() != null) {
               for (CubesItem cube : bones.getCubes()) {
                  List<Float> uv = cube.getUv();
                  List<Float> size = cube.getSize();
                  boolean mirror = cube.isMirror();
                  float inflate = cube.getInflate();
                  model.cubes
                     .add(
                        new BedrockCubeBox(
                           uv.get(0),
                           uv.get(1),
                           this.convertOrigin(bones, cube, 0),
                           this.convertOrigin(bones, cube, 1),
                           this.convertOrigin(bones, cube, 2),
                           size.get(0),
                           size.get(1),
                           size.get(2),
                           inflate,
                           mirror,
                           texWidth,
                           texHeight
                        )
                     );
               }
            }
         }
      }
   }

   protected float convertPivot(BonesItem bones, int index) {
      if (bones.getParent() != null) {
         return index == 1
            ? this.indexBones.get(bones.getParent()).getPivot().get(index) - bones.getPivot().get(index)
            : bones.getPivot().get(index) - this.indexBones.get(bones.getParent()).getPivot().get(index);
      } else {
         return index == 1 ? 24.0F - bones.getPivot().get(index) : bones.getPivot().get(index);
      }
   }

   protected float convertPivot(BonesItem parent, CubesItem cube, int index) {
      assert cube.getPivot() != null;
      return index == 1 ? parent.getPivot().get(index) - cube.getPivot().get(index) : cube.getPivot().get(index) - parent.getPivot().get(index);
   }

   protected float convertOrigin(BonesItem bone, CubesItem cube, int index) {
      return index == 1
         ? bone.getPivot().get(index) - cube.getOrigin().get(index) - cube.getSize().get(index)
         : cube.getOrigin().get(index) - bone.getPivot().get(index);
   }

   protected float convertOrigin(CubesItem cube, int index) {
      assert cube.getPivot() != null;
      return index == 1
         ? cube.getPivot().get(index) - cube.getOrigin().get(index) - cube.getSize().get(index)
         : cube.getOrigin().get(index) - cube.getPivot().get(index);
   }

   protected float convertRotation(float degree) {
      return (float)(degree * Math.PI / 180.0);
   }

   public BedrockPart getNode(String nodeName) {
      ModelRendererWrapper rendererWrapper = this.modelMap.get(nodeName);
      return rendererWrapper != null ? rendererWrapper.getModelRenderer() : null;
   }

   public BonesItem getBone(String name) {
      return this.indexBones.get(name);
   }

   public void render(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
      this.render(matrixStack, transformType, renderType, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void render(
      PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, float red, float green, float blue, float alpha
   ) {
      MultiBufferSource bufferSource = com.tacz.guns.client.render26.GunRender.bufferSource();
      VertexConsumer builder = bufferSource.getBuffer(renderType);
      matrixStack.pushPose();

      for (BedrockPart model : this.shouldRender) {
         model.render(matrixStack, transformType, builder, light, overlay, red, green, blue, alpha);
      }

      matrixStack.popPose();
      if (!IrisCompat.endBatch(bufferSource)) {
         bufferSource.endBatch(renderType);
      }

      for (IFunctionalRenderer renderer : this.delegateRenderers) {
         renderer.render(matrixStack, builder, transformType, light, overlay);
      }

      this.delegateRenderers = new ArrayList<>();
   }

   protected List<BedrockPart> getPath(@Nullable ModelRendererWrapper rendererWrapper) {
      if (rendererWrapper == null) {
         return null;
      }

      BedrockPart part = rendererWrapper.getModelRenderer();
      List<BedrockPart> path = new ArrayList<>();
      Stack<BedrockPart> stack = new Stack<>();

      do {
         stack.push(part);
         part = part.getParent();
      } while (part != null);

      while (!stack.isEmpty()) {
         part = stack.pop();
         path.add(part);
      }

      return path;
   }

   @Nullable
   public Vec3 getOffset() {
      return this.offset;
   }

   @Nullable
   public Vec2 getSize() {
      return this.size;
   }

   public List<BedrockPart> getShouldRender() {
      return this.shouldRender;
   }

   public HashMap<String, BonesItem> getIndexBones() {
      return this.indexBones;
   }
}
