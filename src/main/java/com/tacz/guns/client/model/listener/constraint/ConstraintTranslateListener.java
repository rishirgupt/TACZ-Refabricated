package com.tacz.guns.client.model.listener.constraint;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;

public class ConstraintTranslateListener implements AnimationListener {
   private final ConstraintObject constraint;

   public ConstraintTranslateListener(ConstraintObject constraint) {
      this.constraint = constraint;
   }

   @Override
   public void update(float[] values, boolean blend) {
      if (blend) {
         this.constraint
            .translationConstraint
            .set(
               Math.max(this.constraint.translationConstraint.x(), values[0] * 16.0F),
               Math.max(this.constraint.translationConstraint.y(), values[1] * 16.0F),
               Math.max(this.constraint.translationConstraint.z(), values[2] * 16.0F)
            );
      } else {
         this.constraint.translationConstraint.set(values[0] * 16.0F, values[1] * 16.0F, values[2] * 16.0F);
      }
   }

   @Override
   public float[] initialValue() {
      float[] recover = new float[3];
      if (this.constraint.bonesItem != null) {
         recover[0] = this.constraint.bonesItem.getPivot().get(0) / 16.0F;
         recover[1] = -this.constraint.bonesItem.getPivot().get(1) / 16.0F;
         recover[2] = this.constraint.bonesItem.getPivot().get(2) / 16.0F;
      } else {
         recover[0] = this.constraint.node.x / 16.0F;
         recover[1] = this.constraint.node.y / 16.0F;
         recover[2] = this.constraint.node.z / 16.0F;
      }

      return recover;
   }

   @Override
   public ObjectAnimationChannel.ChannelType getType() {
      return ObjectAnimationChannel.ChannelType.TRANSLATION;
   }
}
