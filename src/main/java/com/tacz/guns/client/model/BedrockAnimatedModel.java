package com.tacz.guns.client.model;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.AnimationListenerSupplier;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.listener.camera.CameraAnimationObject;
import com.tacz.guns.client.model.listener.constraint.ConstraintObject;
import com.tacz.guns.client.model.listener.model.ModelRotateListener;
import com.tacz.guns.client.model.listener.model.ModelScaleListener;
import com.tacz.guns.client.model.listener.model.ModelTranslateListener;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.client.resource.pojo.model.BonesItem;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Quaternionf;

public class BedrockAnimatedModel extends BedrockModel implements AnimationListenerSupplier {
   public static final String CAMERA_NODE_NAME = "camera";
   public static final String CONSTRAINT_NODE = "constraint";
   private final CameraAnimationObject cameraAnimationObject = new CameraAnimationObject();
   @Nullable
   protected List<BedrockPart> constraintPath;
   @Nullable
   private ConstraintObject constraintObject;
   @Nullable
   protected BedrockPart root;
   @Nullable
   protected List<BedrockPart> idleSightPath;

   public BedrockAnimatedModel(BedrockModelPOJO pojo, BedrockVersion version) {
      super(pojo, version);
      ModelRendererWrapper cameraRendererWrapper = this.modelMap.get("camera");
      if (cameraRendererWrapper != null) {
         this.cameraAnimationObject.cameraRenderer = cameraRendererWrapper;
      }

      this.constraintPath = this.getPath(this.modelMap.get("constraint"));
      if (this.constraintPath != null) {
         this.constraintObject = new ConstraintObject();
         BedrockPart constraintNode = this.constraintPath.get(this.constraintPath.size() - 1);
         if (this.shouldRender.contains(constraintNode)) {
            this.constraintObject.bonesItem = this.indexBones.get("constraint");
         } else {
            this.constraintObject.node = constraintNode;
         }
      }

      this.root = Optional.ofNullable(this.modelMap.get("root")).map(ModelRendererWrapper::getModelRenderer).orElse(null);
      this.idleSightPath = this.getPath(this.modelMap.get("idle_view"));
   }

   @Nullable
   public List<BedrockPart> getConstraintPath() {
      return this.constraintPath;
   }

   public void cleanAnimationTransform() {
      for (ModelRendererWrapper rendererWrapper : this.modelMap.values()) {
         rendererWrapper.setOffsetX(0.0F);
         rendererWrapper.setOffsetY(0.0F);
         rendererWrapper.setOffsetZ(0.0F);
         rendererWrapper.getAdditionalQuaternion().set(0.0F, 0.0F, 0.0F, 1.0F);
         rendererWrapper.setScaleX(1.0F);
         rendererWrapper.setScaleY(1.0F);
         rendererWrapper.setScaleZ(1.0F);
      }

      if (this.constraintObject != null) {
         this.constraintObject.rotationConstraint.set(0.0F, 0.0F, 0.0F);
         this.constraintObject.translationConstraint.set(0.0F, 0.0F, 0.0F);
      }
   }

   public void cleanCameraAnimationTransform() {
      this.cameraAnimationObject.rotationQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   }

   public void setFunctionalRenderer(String node, Function<BedrockPart, IFunctionalRenderer> function) {
      ModelRendererWrapper wrapper = this.modelMap.get(node);
      if (wrapper == null) {
         FunctionalBedrockPart functionalPart = new FunctionalBedrockPart(function, node);
         this.modelMap.put(node, new ModelRendererWrapper(functionalPart));
      } else if (wrapper.getModelRenderer() instanceof FunctionalBedrockPart functionalPart) {
         functionalPart.functionalRenderer = function;
      }
   }

   @Nonnull
   public CameraAnimationObject getCameraAnimationObject() {
      return this.cameraAnimationObject;
   }

   @Nullable
   public ConstraintObject getConstraintObject() {
      return this.constraintObject;
   }

   @Override
   protected void loadNewModel(BedrockModelPOJO pojo) {
      assert pojo.getGeometryModelNew() != null;
      pojo.getGeometryModelNew().deco();
      if (pojo.getGeometryModelNew().getBones() != null) {
         for (BonesItem bones : pojo.getGeometryModelNew().getBones()) {
            FunctionalBedrockPart bedrockPart = new FunctionalBedrockPart(null, bones.getName());
            this.modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(bedrockPart));
         }

         super.loadNewModel(pojo);
      }
   }

   @Override
   protected void loadLegacyModel(BedrockModelPOJO pojo) {
      assert pojo.getGeometryModelLegacy() != null;
      pojo.getGeometryModelLegacy().deco();
      if (pojo.getGeometryModelLegacy().getBones() != null) {
         for (BonesItem bones : pojo.getGeometryModelLegacy().getBones()) {
            FunctionalBedrockPart bedrockPart = new FunctionalBedrockPart(null, bones.getName());
            this.modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(bedrockPart));
         }

         super.loadLegacyModel(pojo);
      }
   }

   @Override
   public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
      ModelRendererWrapper model = this.modelMap.get(nodeName);
      if (model == null) {
         return null;
      }

      AnimationListener cameraListener = this.cameraAnimationObject.supplyListeners(nodeName, type);
      if (cameraListener != null) {
         return cameraListener;
      }

      if (this.constraintObject != null) {
         AnimationListener constraintListener = this.constraintObject.supplyListeners(nodeName, type);
         if (constraintListener != null) {
            return constraintListener;
         }
      }

      if (type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
         return new ModelTranslateListener(this, model, nodeName);
      } else if (type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
         return new ModelRotateListener(model);
      } else {
         return type.equals(ObjectAnimationChannel.ChannelType.SCALE) ? new ModelScaleListener(model) : null;
      }
   }

   public boolean getRenderHand() {
      return true;
   }

   public BedrockPart getRootNode() {
      return this.root;
   }

   public List<BedrockPart> getIdleSightPath() {
      return this.idleSightPath;
   }
}
