package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AnimationSampler {
   private Integer input;
   private String interpolation;
   private Integer output;

   public Integer getInput() {
      return this.input;
   }

   public void setInput(Integer input) {
      if (input == null) {
         throw new NullPointerException("Invalid value for input: " + input + ", may not be null");
      }

      this.input = input;
   }

   public String getInterpolation() {
      return this.interpolation;
   }

   public void setInterpolation(String interpolation) {
      if (interpolation == null) {
         this.interpolation = interpolation;
      } else {
         if (!"LINEAR".equals(interpolation) && !"STEP".equals(interpolation) && !"CUBICSPLINE".equals(interpolation)) {
            throw new IllegalArgumentException("Invalid value for interpolation: " + interpolation + ", valid: [LINEAR, STEP, CUBICSPLINE]");
         }

         this.interpolation = interpolation;
      }
   }

   public String defaultInterpolation() {
      return "LINEAR";
   }

   public Integer getOutput() {
      return this.output;
   }

   public void setOutput(Integer output) {
      if (output == null) {
         throw new NullPointerException("Invalid value for output: " + output + ", may not be null");
      }

      this.output = output;
   }
}
