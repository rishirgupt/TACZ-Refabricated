package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AccessorSparseValues {
   private Integer bufferView;
   private Integer byteOffset;

   public Integer getBufferView() {
      return this.bufferView;
   }

   public void setBufferView(Integer bufferView) {
      if (bufferView == null) {
         throw new NullPointerException("Invalid value for bufferView: " + bufferView + ", may not be null");
      }

      this.bufferView = bufferView;
   }

   public Integer getByteOffset() {
      return this.byteOffset;
   }

   public void setByteOffset(Integer byteOffset) {
      if (byteOffset == null) {
         this.byteOffset = byteOffset;
      } else {
         if (byteOffset < 0) {
            throw new IllegalArgumentException("byteOffset < 0");
         }

         this.byteOffset = byteOffset;
      }
   }

   public Integer defaultByteOffset() {
      return 0;
   }
}
