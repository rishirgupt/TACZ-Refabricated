package com.tacz.guns.client.model.listener.model;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;

public class ModelScaleListener implements AnimationListener {
   private final ModelRendererWrapper rendererWrapper;

   public ModelScaleListener(ModelRendererWrapper rendererWrapper) {
      this.rendererWrapper = rendererWrapper;
   }

   @Override
   public void update(float[] values, boolean blend) {
      if (blend) {
         this.rendererWrapper.setScaleX(this.rendererWrapper.getScaleX() * values[0]);
         this.rendererWrapper.setScaleY(this.rendererWrapper.getScaleY() * values[1]);
         this.rendererWrapper.setScaleZ(this.rendererWrapper.getScaleZ() * values[2]);
      } else {
         this.rendererWrapper.setScaleX(values[0]);
         this.rendererWrapper.setScaleY(values[1]);
         this.rendererWrapper.setScaleZ(values[2]);
      }
   }

   @Override
   public float[] initialValue() {
      return new float[]{1.0F, 1.0F, 1.0F};
   }

   @Override
   public ObjectAnimationChannel.ChannelType getType() {
      return ObjectAnimationChannel.ChannelType.SCALE;
   }
}
