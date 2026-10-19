package com.tacz.guns.client.model.listener.model;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.resource.pojo.model.BonesItem;
import javax.annotation.Nullable;

public class ModelTranslateListener implements AnimationListener {
   private final ModelRendererWrapper rendererWrapper;
   @Nullable
   private final BonesItem bonesItem;

   public ModelTranslateListener(BedrockAnimatedModel model, ModelRendererWrapper rendererWrapper, String nodeName) {
      this.rendererWrapper = rendererWrapper;
      if (model.getShouldRender().contains(rendererWrapper.getModelRenderer())) {
         this.bonesItem = model.getIndexBones().get(nodeName);
      } else {
         this.bonesItem = null;
      }
   }

   @Override
   public void update(float[] values, boolean blend) {
      if (blend) {
         this.rendererWrapper.addOffsetX(values[0]);
         this.rendererWrapper.addOffsetY(-values[1]);
         this.rendererWrapper.addOffsetZ(values[2]);
      } else {
         this.rendererWrapper.setOffsetX(values[0]);
         this.rendererWrapper.setOffsetY(-values[1]);
         this.rendererWrapper.setOffsetZ(values[2]);
      }
   }

   @Override
   public float[] initialValue() {
      float[] recover = new float[3];
      if (this.bonesItem != null) {
         recover[0] = this.bonesItem.getPivot().get(0) / 16.0F;
         recover[1] = -this.bonesItem.getPivot().get(1) / 16.0F;
         recover[2] = this.bonesItem.getPivot().get(2) / 16.0F;
      } else {
         recover[0] = this.rendererWrapper.getRotationPointX() / 16.0F;
         recover[1] = this.rendererWrapper.getRotationPointY() / 16.0F;
         recover[2] = this.rendererWrapper.getRotationPointZ() / 16.0F;
      }

      return recover;
   }

   @Override
   public ObjectAnimationChannel.ChannelType getType() {
      return ObjectAnimationChannel.ChannelType.TRANSLATION;
   }
}
