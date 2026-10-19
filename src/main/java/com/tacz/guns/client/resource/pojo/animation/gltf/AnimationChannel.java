package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AnimationChannel {
   private Integer sampler;
   private AnimationChannelTarget target;

   public Integer getSampler() {
      return this.sampler;
   }

   public void setSampler(Integer sampler) {
      if (sampler == null) {
         throw new NullPointerException("Invalid value for sampler: " + sampler + ", may not be null");
      }

      this.sampler = sampler;
   }

   public AnimationChannelTarget getTarget() {
      return this.target;
   }

   public void setTarget(AnimationChannelTarget target) {
      if (target == null) {
         throw new NullPointerException("Invalid value for target: " + target + ", may not be null");
      }

      this.target = target;
   }
}
