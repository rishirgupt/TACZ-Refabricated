package com.tacz.guns.client.model.listener.camera;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.AnimationListenerSupplier;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import org.joml.Quaternionf;

public class CameraAnimationObject implements AnimationListenerSupplier {
   public Quaternionf rotationQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   public ModelRendererWrapper cameraRenderer;

   @Override
   public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
      if (!nodeName.equals("camera")) {
         return null;
      } else {
         return type.equals(ObjectAnimationChannel.ChannelType.ROTATION) ? new CameraRotateListener(this) : null;
      }
   }
}
