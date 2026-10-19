package com.tacz.guns.client.animation.statemachine;

import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;

public class ItemAnimationStateContext extends AnimationStateContext {
   private float putAwayTime = 0.0F;
   protected float partialTicks = 0.0F;

   public float getPutAwayTime() {
      return this.putAwayTime;
   }

   public void setPutAwayTime(float putAwayTime) {
      this.putAwayTime = putAwayTime;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public void setPartialTicks(float partialTicks) {
      this.partialTicks = partialTicks;
   }
}
