package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AccessorSparse {
   private Integer count;
   private AccessorSparseIndices indices;
   private AccessorSparseValues values;

   public Integer getCount() {
      return this.count;
   }

   public void setCount(Integer count) {
      if (count == null) {
         throw new NullPointerException("Invalid value for count: " + count + ", may not be null");
      }

      if (count < 1) {
         throw new IllegalArgumentException("count < 1");
      }

      this.count = count;
   }

   public AccessorSparseIndices getIndices() {
      return this.indices;
   }

   public void setIndices(AccessorSparseIndices indices) {
      if (indices == null) {
         throw new NullPointerException("Invalid value for indices: " + indices + ", may not be null");
      }

      this.indices = indices;
   }

   public AccessorSparseValues getValues() {
      return this.values;
   }

   public void setValues(AccessorSparseValues values) {
      if (values == null) {
         throw new NullPointerException("Invalid value for values: " + values + ", may not be null");
      }

      this.values = values;
   }
}
