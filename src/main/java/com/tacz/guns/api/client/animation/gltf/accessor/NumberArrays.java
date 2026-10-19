package com.tacz.guns.api.client.animation.gltf.accessor;

class NumberArrays {
   private NumberArrays() {
   }

   static Number[] asNumbers(int[] array) {
      Number[] result = new Number[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   static Number[] asNumbers(long[] array) {
      Number[] result = new Number[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   static Number[] asNumbers(float[] array) {
      Number[] result = new Number[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }
}
