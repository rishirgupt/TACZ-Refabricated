package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AccessorSparseIndices {
   private Integer bufferView;
   private Integer byteOffset;
   private Integer componentType;

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

   public Integer getComponentType() {
      return this.componentType;
   }

   public void setComponentType(Integer componentType) {
      if (componentType == null) {
         throw new NullPointerException("Invalid value for componentType: " + componentType + ", may not be null");
      }

      if (componentType != 5121 && componentType != 5123 && componentType != 5125) {
         throw new IllegalArgumentException("Invalid value for componentType: " + componentType + ", valid: [5121, 5123, 5125]");
      }

      this.componentType = componentType;
   }
}
