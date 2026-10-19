package com.tacz.guns.client.model.listener.constraint;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.AnimationListenerSupplier;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.pojo.model.BonesItem;
import javax.annotation.Nullable;
import org.joml.Vector3f;

public class ConstraintObject implements AnimationListenerSupplier {
   public Vector3f translationConstraint = new Vector3f(0.0F, 0.0F, 0.0F);
   public Vector3f rotationConstraint = new Vector3f(0.0F, 0.0F, 0.0F);
   public BedrockPart node;
   public BonesItem bonesItem;

   @Nullable
   @Override
   public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
      if (!nodeName.equals("constraint")) {
         return null;
      } else if (type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
         return new ConstraintRotateListener(this);
      } else {
         return type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION) ? new ConstraintTranslateListener(this) : null;
      }
   }
}
